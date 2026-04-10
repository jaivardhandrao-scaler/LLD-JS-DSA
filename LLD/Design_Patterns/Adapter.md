# Adapter Design Pattern

**Category:** Structural
**Intent:** Convert the interface of an existing class into another interface the client expects, allowing classes with incompatible interfaces to work together.
**Use cases:** Integrating legacy/third-party services with different APIs, normalizing data from multiple providers, bridging old and new systems.

---

## The Problem

You have a **client** (e.g., `SellerRankingService`) that expects a specific interface. But the actual services you need to use have **different, incompatible** interfaces.

**Real scenario:** A ranking service needs seller data. Initially it uses Snapdeal's API (`SDSellerSearchService`). Later, Exclusively is acquired -- it provides similar data but with completely different method names, data types, and units.

### Constraints
- You **cannot modify** the legacy services (owned by other teams, have other callers)
- The ranking service must depend on a **stable interface**, not concrete services
- New providers should be **pluggable** with minimal code changes
- Mapping differences (field names, units, ranges) must live in a **small, testable boundary**

---

## Naive Alternatives and Why They Fail

### BAD Option 1: Modify Legacy Services

```java
// BAD: Change SDSellerSearchService to match our interface
// Can't do this -- other teams use it, breaks their code
```

**Violates:** OCP (modifying closed code), SRP (forcing legacy service to serve our needs), DIP (twisting low-level modules for high-level needs)

### BAD Option 2: Giant if/else in Client

```java
// BAD: Client knows about every provider
class SellerRankingService {
    void rank(String sku, String provider) {
        if (provider.equals("snapdeal")) {
            SDSellerSearchService sd = new SDSellerSearchService();
            List<SDVendor> vendors = sd.getSellersBySKU(sku);
            // convert SDVendor to our model...
        } else if (provider.equals("exclusively")) {
            ExclusivelySellerSearchService ex = new ExclusivelySellerSearchService();
            Page<ExMerchant> page = ex.merchantsFor(sku, 1, 50);
            // convert ExMerchant to our model...
        }
    }
}
```

**Violates:** SRP (ranking + mapping + provider detection), OCP (new provider = edit ranking), DIP (depends on concrete services)

### BAD Option 3: God Mapper Utility

A giant `Mapper` class with all conversions. Still a switchyard inside, weak OCP, poor testability.

---

## The Solution: Adapter Pattern

### The Idea

Like a **travel plug adapter**: one side matches the wall socket (legacy provider), the other matches your laptop plug (client's interface). Neither wall nor laptop changes -- only the adapter speaks both dialects.

### Step 1: Define the Target Interface

What does the client actually need?

```java
// Target interface -- what the ranking service expects
import java.util.*;

public interface SellerSearch {
    List<Seller> getSellersBySku(String sku);
    Optional<Seller> getSellerWithMaxDiscount(String sku);
}

// Canonical model -- our domain's shape
public final class Seller {
    public final String id;
    public final String name;
    public final double price;        // major units (rupees)
    public final double discountPct;  // 0..100
    public final double rating;       // 0..5

    public Seller(String id, String name, double price,
                  double discountPct, double rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.discountPct = discountPct;
        this.rating = rating;
    }

    @Override public String toString() {
        return String.format("%s(%s) price=%.2f, discount=%.1f%%, rating=%.1f/5",
            name, id, price, discountPct, rating);
    }
}
```

### Step 2: Legacy Providers (Unmodified)

**Snapdeal** -- uses `SDVendor`, method `getSellersBySKU()`:
```java
public final class SDVendor {
    public final String vendorId;
    public final String shopName;
    public final double listPrice;
    public final double discountPct;  // 0..100
    public final double starRating;   // 0..5
    // constructor...
}

public class SDSellerSearchService {
    public List<SDVendor> getSellersBySKU(String sku) { /* returns vendors */ }
    public SDVendor getSellerwithMaxDiscount(String sku) { /* returns vendor */ }
}
```

**Exclusively** -- uses `ExMerchant`, method `merchantsFor()`, **different units**:
```java
public final class ExMerchant {
    public final String id;
    public final String display;      // seller name
    public final long pricePaise;     // MINOR units (paise, not rupees!)
    public final int off;             // 0..100 discount
    public final int score100;        // 0..100 quality score (NOT 0..5!)
    // constructor...
}

public class ExclusivelySellerSearchService {
    public Page<ExMerchant> merchantsFor(String articleCode, int page, int perPage) { ... }
    public Optional<ExMerchant> maxDiscounted(String articleCode) { ... }
}
```

**Key differences to handle:**
| Aspect | Snapdeal | Exclusively |
|--------|----------|-------------|
| ID field | `vendorId` | `id` |
| Name field | `shopName` | `display` |
| Price | rupees (double) | **paise** (long) -- divide by 100 |
| Rating | 0..5 (double) | **0..100** (int) -- divide by 20 |
| Method name | `getSellersBySKU` | `merchantsFor` |
| Return type | `List<SDVendor>` | `Page<ExMerchant>` |

### Step 3: Write Adapters (Object Adapter via Composition)

**Snapdeal Adapter:**
```java
import java.util.*;

public final class SDSearchAdapter implements SellerSearch {
    private final SDSellerSearchService sd;

    public SDSearchAdapter(SDSellerSearchService sd) { this.sd = sd; }

    @Override
    public List<Seller> getSellersBySku(String sku) {
        List<SDVendor> vendors = sd.getSellersBySKU(sku);
        List<Seller> out = new ArrayList<>();
        for (SDVendor v : vendors) {
            out.add(new Seller(
                v.vendorId, v.shopName,
                v.listPrice, v.discountPct, v.starRating
            ));
        }
        return out;
    }

    @Override
    public Optional<Seller> getSellerWithMaxDiscount(String sku) {
        SDVendor v = sd.getSellerwithMaxDiscount(sku);
        return Optional.ofNullable(v).map(x ->
            new Seller(x.vendorId, x.shopName,
                       x.listPrice, x.discountPct, x.starRating));
    }
}
```

**Exclusively Adapter (with unit conversions):**
```java
import java.util.*;

public final class ExSearchAdapter implements SellerSearch {
    private final ExclusivelySellerSearchService ex;

    public ExSearchAdapter(ExclusivelySellerSearchService ex) { this.ex = ex; }

    @Override
    public List<Seller> getSellersBySku(String sku) {
        Page<ExMerchant> page = ex.merchantsFor(sku, 1, 50);
        List<Seller> out = new ArrayList<>();
        for (ExMerchant m : page.data) {
            double price  = m.pricePaise / 100.0;  // paise -> rupees
            double rating = m.score100 / 20.0;      // 0..100 -> 0..5
            out.add(new Seller(m.id, m.display, price, m.off, rating));
        }
        return out;
    }

    @Override
    public Optional<Seller> getSellerWithMaxDiscount(String sku) {
        return ex.maxDiscounted(sku).map(m ->
            new Seller(m.id, m.display,
                       m.pricePaise / 100.0, m.off, m.score100 / 20.0));
    }
}
```

### Step 4: Client Uses Constructor Injection

```java
import java.util.*;

public final class SellerRankingService {
    private final SellerSearch search;  // depends on abstraction!

    public SellerRankingService(SellerSearch search) {
        this.search = search;
    }

    public List<Seller> rankBySku(String sku) {
        List<Seller> sellers = new ArrayList<>(search.getSellersBySku(sku));
        sellers.sort(Comparator
            .comparingDouble((Seller s) -> s.discountPct).reversed()
            .thenComparingDouble(s -> s.rating).reversed()
            .thenComparingDouble(s -> s.price));
        return sellers;
    }

    public Optional<Seller> bestDiscount(String sku) {
        return search.getSellerWithMaxDiscount(sku);
    }
}

// Wiring -- swap adapters at composition time
class Demo {
    public static void main(String[] args) {
        // Use Snapdeal
        SellerRankingService rankingSD = new SellerRankingService(
            new SDSearchAdapter(new SDSellerSearchService()));

        // Use Exclusively
        SellerRankingService rankingEX = new SellerRankingService(
            new ExSearchAdapter(new ExclusivelySellerSearchService()));

        System.out.println("SD: " + rankingSD.rankBySku("SKU-123"));
        System.out.println("EX: " + rankingEX.rankBySku("SKU-123"));
    }
}
```

**Key insight:** `SellerRankingService` has ZERO knowledge of Snapdeal or Exclusively. It only knows `SellerSearch`. Swapping providers = swapping one line of wiring code.

---

## Object Adapter vs Class Adapter

| Aspect | Object Adapter (Composition) | Class Adapter (Inheritance) |
|--------|-----------------------------|-----------------------------|
| **How it works** | Adapter HAS an adaptee (field) | Adapter EXTENDS the adaptee |
| **Java support** | Yes (standard approach) | Limited (Java has no multiple class inheritance) |
| **Coupling** | Loose -- adapter wraps adaptee | Tight -- adapter IS an adaptee |
| **Flexibility** | Can adapt any subclass of adaptee | Locked to one specific adaptee class |

**Exam Tip:** In Java, always use **Object Adapter** (composition). Class adapters require multiple inheritance, which Java doesn't support for classes.

---

## Practical Concerns for Adapters

| Concern | Guidance |
|---------|----------|
| **Unit conversions** | paise -> rupees, score 0..100 -> rating 0..5. Put ALL conversions in the adapter |
| **Null handling** | Use `Optional`, safe defaults, or domain-specific exceptions |
| **Error translation** | Convert provider-specific exceptions to client-facing error types |
| **Caching** | Adapters are a good place to add read-through caches |
| **Observability** | Log at the boundary; attach request IDs and metrics |

---

## Simpler Example: Employee Data Normalization (from PDF)

A system needs to display employees from 3 different sources (CSV file, database, LDAP directory) in a unified format. Each source has a different data shape.

```java
// Target interface: what our system expects
interface Employee {
    String getId();
    String getFirstName();
    String getLastName();
    String getEmail();
}

// Source A: CSV row (array of strings)
class CsvRow {
    String[] columns;  // [id, fullName, email]
    CsvRow(String[] columns) { this.columns = columns; }
}

// Source B: DB result (different field names)
class DbRecord {
    String empId;
    String fname;
    String lname;
    String emailAddr;
    DbRecord(String empId, String fname, String lname, String emailAddr) {
        this.empId = empId; this.fname = fname; this.lname = lname; this.emailAddr = emailAddr;
    }
}

// Adapter A: CSV -> Employee
class EmployeeCSVAdapter implements Employee {
    private final CsvRow row;
    EmployeeCSVAdapter(CsvRow row) { this.row = row; }

    @Override public String getId() { return row.columns[0]; }
    @Override public String getFirstName() {
        return row.columns[1].split(" ")[0];  // "John Doe" -> "John"
    }
    @Override public String getLastName() {
        String[] parts = row.columns[1].split(" ");
        return parts.length > 1 ? parts[1] : "";
    }
    @Override public String getEmail() { return row.columns[2]; }
}

// Adapter B: DB -> Employee
class EmployeeDBAdapter implements Employee {
    private final DbRecord rec;
    EmployeeDBAdapter(DbRecord rec) { this.rec = rec; }

    @Override public String getId() { return rec.empId; }
    @Override public String getFirstName() { return rec.fname; }
    @Override public String getLastName() { return rec.lname; }
    @Override public String getEmail() { return rec.emailAddr; }
}

// Client: works with Employee interface only
class EmployeeDirectory {
    public void printAll(List<Employee> employees) {
        for (Employee e : employees) {
            System.out.println(e.getId() + " | " + e.getFirstName()
                + " " + e.getLastName() + " | " + e.getEmail());
        }
    }
}
```

**Why this is useful for the exam:** It's a simpler, cleaner Adapter example than the Snapdeal/Exclusively one. If you get a coding question asking "normalize data from different sources using Adapter," this is the template.

---

## How Adapter Honors SOLID

| Principle | How Adapter Follows It |
|-----------|----------------------|
| **SRP** | Ranking ranks. Each adapter adapts one provider. No class juggles multiple responsibilities |
| **OCP** | Adding a new provider = adding a new adapter class. Ranking service untouched |
| **LSP** | All `SellerSearch` implementations provide equivalent semantics to the client |
| **ISP** | `SellerSearch` exposes only what ranking needs -- no fat interfaces |
| **DIP** | `SellerRankingService` depends on abstraction (`SellerSearch`), not concrete services |

**Exam Tip:** Adapter is the BEST pattern for demonstrating all 5 SOLID principles in one example.

---

## When NOT to Use Adapter

- If you **own both sides** and can simply change one API -- do that, delete the adapter
- If models must **truly merge** -- plan a domain convergence; use adapter as a temporary anti-corruption layer
- If the client needs to **orchestrate complex provider workflows** -- consider a **Facade** with adapters behind it

---

## Big Picture

- Adapter is a **Structural** pattern that bridges incompatible interfaces
- It sits at the **boundary** between your code and external/legacy code
- Related to **Facade** (simplifies a complex subsystem), **Decorator** (adds behavior, same interface), and **Proxy** (controls access, same interface)
- Adapter **changes the interface**; Decorator **adds behavior without changing interface**

---

## Exam Tips (Quick Recall)

1. Adapter **converts** an incompatible interface to one the client expects
2. Use **Object Adapter** (composition) in Java -- not class adapter (inheritance)
3. The adapter **wraps** the adaptee and **implements** the target interface
4. Put ALL data mapping (field names, units, ranges) **inside the adapter**
5. Client depends on **target interface** (DIP), not on the concrete service
6. New provider = new adapter class, nothing else changes (OCP)
7. Like a **travel plug adapter** -- one side matches the wall, other matches your device

---

## Viva Questions

**Q1: What is the Adapter pattern?**
A structural pattern that converts the interface of an existing class into another interface the client expects. It allows classes with incompatible interfaces to work together without modifying either side.

**Q2: What is the difference between Object Adapter and Class Adapter?**
Object Adapter uses composition (has-a adaptee). Class Adapter uses inheritance (is-a adaptee). Java favors Object Adapter because Java doesn't support multiple class inheritance. Object Adapter is more flexible and loosely coupled.

**Q3: How does Adapter follow OCP?**
Adding a new provider (e.g., a third seller service) only requires writing a new adapter class. The client (SellerRankingService) and existing adapters are never modified. Open for extension, closed for modification.

**Q4: How is Adapter different from Facade?**
Adapter converts one interface to another (makes incompatible things compatible). Facade provides a simplified interface to a complex subsystem (reduces complexity). Adapter works with a single class; Facade typically wraps multiple classes.

**Q5: How is Adapter different from Decorator?**
Adapter changes the interface (SDSellerSearchService -> SellerSearch). Decorator keeps the SAME interface and adds behavior (e.g., logging, caching). Both use composition, but their intent is different.

**Q6: Where should data conversions (like paise to rupees) happen?**
Inside the adapter. The adapter is the boundary where all shape/semantics translation lives. This keeps conversions in a small, testable place and prevents them from leaking into the client.

**Q7: How does Adapter support DIP?**
The high-level module (SellerRankingService) depends on the abstraction (SellerSearch interface), not on low-level modules (SDSellerSearchService, ExclusivelySellerSearchService). The adapters bridge the gap.

**Q8: What is the "target interface" in Adapter?**
The interface that the client expects and programs against. In our example, `SellerSearch` is the target interface. Adapters implement this interface and translate calls to the adaptee's interface.

**Q9: When should you NOT use Adapter?**
When you own both sides and can simply change one API to match. Or when the interfaces are so different that the adapter becomes as complex as the system itself. In that case, consider a redesign.

**Q10: Can an adapter add caching or error handling?**
Yes. Adapters sit at the boundary and are a natural place to add caching, retry logic, error translation, and observability. But if you're adding lots of behavior, consider whether you need a Decorator on top of the adapter.

---

## MCQ Quiz

**1. Adapter pattern is classified as:**
a) Creational
b) Structural
c) Behavioral
d) Concurrency

<details><summary>Answer</summary>b) Structural</details>

**2. The primary intent of Adapter is to:**
a) Add new behavior to an object
b) Convert an incompatible interface to one the client expects
c) Simplify a complex subsystem
d) Ensure only one instance exists

<details><summary>Answer</summary>b) Convert an incompatible interface to one the client expects</details>

**3. In Java, which adapter variant is preferred?**
a) Class Adapter (inheritance)
b) Object Adapter (composition)
c) Both are equally good
d) Neither -- use Proxy instead

<details><summary>Answer</summary>b) Object Adapter (composition) -- Java lacks multiple class inheritance</details>

**4. In the seller example, `SDSearchAdapter` does what?**
a) Modifies `SDSellerSearchService`
b) Implements `SellerSearch` by wrapping `SDSellerSearchService` and converting its output
c) Extends `SDSellerSearchService`
d) Replaces `SDSellerSearchService`

<details><summary>Answer</summary>b) Implements `SellerSearch` by wrapping and converting</details>

**5. Where should unit conversions (paise -> rupees) happen?**
a) In the client (SellerRankingService)
b) In the adapter
c) In the legacy service
d) In a global utility class

<details><summary>Answer</summary>b) In the adapter -- it's the boundary where translation happens</details>

**6. Adding a third provider (e.g., Amazon) requires:**
a) Modifying SellerRankingService
b) Modifying existing adapters
c) Writing a new adapter class only
d) Rewriting the SellerSearch interface

<details><summary>Answer</summary>c) Writing a new adapter class only (OCP)</details>

**7. `SellerRankingService` depends on:**
a) `SDSellerSearchService` directly
b) `ExclusivelySellerSearchService` directly
c) `SellerSearch` interface (abstraction)
d) All concrete service classes

<details><summary>Answer</summary>c) `SellerSearch` interface -- DIP</details>

**8. How is Adapter different from Proxy?**
a) Adapter changes the interface; Proxy keeps the same interface but controls access
b) They are identical
c) Proxy changes the interface; Adapter controls access
d) Both are behavioral patterns

<details><summary>Answer</summary>a) Adapter changes the interface; Proxy keeps the same interface but controls access</details>

**9. Which is NOT a responsibility of an adapter?**
a) Field name mapping (vendorId -> id)
b) Unit conversion (paise -> rupees)
c) Ranking/sorting the data
d) Exception translation

<details><summary>Answer</summary>c) Ranking is the CLIENT's job, not the adapter's</details>

**10. Object Adapter uses:**
a) Inheritance (extends the adaptee)
b) Composition (has a reference to the adaptee)
c) Reflection
d) Generics

<details><summary>Answer</summary>b) Composition -- the adapter holds a reference to the adaptee</details>

**11. The "target interface" in Adapter refers to:**
a) The adaptee's interface
b) The interface the client expects and programs against
c) The Java `Adapter` interface
d) The factory interface

<details><summary>Answer</summary>b) The interface the client expects (e.g., `SellerSearch`)</details>

**12. Why can't we use Class Adapter easily in Java?**
a) Java doesn't support interfaces
b) Java doesn't support multiple class inheritance
c) Java doesn't have abstract classes
d) Java doesn't support composition

<details><summary>Answer</summary>b) Java doesn't support multiple class inheritance -- adapter would need to extend both target and adaptee</details>

**13. Which SOLID principle does Adapter MOST strongly demonstrate?**
a) SRP only
b) OCP and DIP together
c) LSP only
d) ISP only

<details><summary>Answer</summary>b) OCP (new adapters without modifying client) and DIP (client depends on abstraction)</details>

**14. The analogy for Adapter is:**
a) A factory assembly line
b) A travel plug adapter (one side matches wall, other matches device)
c) A decorator on a cake
d) A remote control

<details><summary>Answer</summary>b) A travel plug adapter</details>

**15. If the adapter becomes extremely complex, what should you consider?**
a) Making the adapter even bigger
b) A redesign or using a Facade with adapters behind it
c) Abandoning the pattern
d) Using reflection

<details><summary>Answer</summary>b) If mapping is too complex, consider redesign or Facade</details>

**16. An "Anti-Corruption Layer" in Domain-Driven Design is essentially a:**
a) Singleton
b) Adapter / set of adapters at the boundary
c) Factory
d) Observer

<details><summary>Answer</summary>b) An adapter or set of adapters that protect your domain from external models</details>

**17. What does the adapter do with `ExMerchant.score100`?**
a) Returns it as-is
b) Converts it from 0..100 to 0..5 scale (divides by 20)
c) Ignores it
d) Multiplies by 100

<details><summary>Answer</summary>b) Converts from 0..100 to 0..5 scale by dividing by 20</details>

**18. How does Adapter ensure LSP?**
a) By throwing exceptions
b) By ensuring all adapters provide equivalent semantics through the target interface
c) By using inheritance
d) By having no abstract methods

<details><summary>Answer</summary>b) All implementations of `SellerSearch` provide consistent semantics</details>

**19. Constructor injection of `SellerSearch` into `SellerRankingService` supports:**
a) Only SRP
b) DIP (depends on abstraction) and testability (can inject mocks)
c) Only OCP
d) Only ISP

<details><summary>Answer</summary>b) DIP and testability -- you can inject a mock SellerSearch for testing</details>

**20. Which pair of patterns are MOST similar in structure?**
a) Adapter and Singleton
b) Adapter and Decorator (both use composition wrapping)
c) Adapter and Builder
d) Adapter and Prototype

<details><summary>Answer</summary>b) Adapter and Decorator -- both wrap an object via composition, but Adapter changes the interface while Decorator preserves it</details>

### Scoring
- **18-20:** Adapter mastered.
- **14-17:** Good. Review Adapter vs Decorator/Proxy distinctions.
- **10-13:** Revisit the SOLID mapping.
- **Below 10:** Re-read from the beginning.

---

## Coding Exam Questions

### Problem 1: Identify the Violations

```java
class PaymentProcessor {
    void processPayment(String gateway, double amount) {
        if (gateway.equals("stripe")) {
            StripeAPI stripe = new StripeAPI();
            stripe.charge((int)(amount * 100), "usd"); // cents
        } else if (gateway.equals("paypal")) {
            PayPalService paypal = new PayPalService();
            paypal.sendPayment(amount, "USD");
        }
    }
}
```

**Task:** Identify the SOLID violations and refactor using the Adapter pattern.

<details><summary>Solution</summary>

**Violations:**
- SRP: PaymentProcessor handles routing AND conversion logic
- OCP: Adding a new gateway requires editing this class
- DIP: Depends on concrete `StripeAPI` and `PayPalService`

**Refactored:**
```java
// Target interface
interface PaymentGateway {
    void pay(double amount, String currency);
}

// Stripe Adapter (converts dollars to cents)
class StripeAdapter implements PaymentGateway {
    private final StripeAPI stripe;
    StripeAdapter(StripeAPI stripe) { this.stripe = stripe; }

    @Override
    public void pay(double amount, String currency) {
        stripe.charge((int)(amount * 100), currency.toLowerCase());
    }
}

// PayPal Adapter (direct pass-through)
class PayPalAdapter implements PaymentGateway {
    private final PayPalService paypal;
    PayPalAdapter(PayPalService paypal) { this.paypal = paypal; }

    @Override
    public void pay(double amount, String currency) {
        paypal.sendPayment(amount, currency);
    }
}

// Clean client
class PaymentProcessor {
    private final PaymentGateway gateway;
    PaymentProcessor(PaymentGateway gateway) { this.gateway = gateway; }

    void processPayment(double amount, String currency) {
        gateway.pay(amount, currency);
    }
}

// Wiring
PaymentProcessor processor = new PaymentProcessor(
    new StripeAdapter(new StripeAPI()));
```
</details>

---

### Problem 2: Write an Adapter with Unit Conversion

Given these two incompatible temperature services:

```java
// Service A: returns Celsius
class CelsiusSensor {
    double readTempC() { return 25.0; }
}

// Service B: returns Fahrenheit
class FahrenheitSensor {
    double readTempF() { return 98.6; }
}
```

Write a `TemperatureReader` interface and adapters so a monitoring system can read from either sensor in **Kelvin**.

<details><summary>Solution</summary>

```java
// Target interface
interface TemperatureReader {
    double readTempKelvin();
    String sensorName();
}

// Celsius Adapter: K = C + 273.15
class CelsiusAdapter implements TemperatureReader {
    private final CelsiusSensor sensor;
    CelsiusAdapter(CelsiusSensor sensor) { this.sensor = sensor; }

    @Override
    public double readTempKelvin() {
        return sensor.readTempC() + 273.15;
    }

    @Override
    public String sensorName() { return "CelsiusSensor"; }
}

// Fahrenheit Adapter: K = (F - 32) * 5/9 + 273.15
class FahrenheitAdapter implements TemperatureReader {
    private final FahrenheitSensor sensor;
    FahrenheitAdapter(FahrenheitSensor sensor) { this.sensor = sensor; }

    @Override
    public double readTempKelvin() {
        return (sensor.readTempF() - 32) * 5.0 / 9.0 + 273.15;
    }

    @Override
    public String sensorName() { return "FahrenheitSensor"; }
}

// Monitor -- provider-agnostic
class TemperatureMonitor {
    private final List<TemperatureReader> readers;
    TemperatureMonitor(List<TemperatureReader> readers) {
        this.readers = readers;
    }

    void report() {
        for (TemperatureReader r : readers) {
            System.out.printf("%s: %.2f K%n", r.sensorName(), r.readTempKelvin());
        }
    }
}
```
</details>

---

### Problem 3: Identify the Pattern

For each scenario, identify whether Adapter, Decorator, or Proxy is the right choice:

**A.** Your logging library expects `Logger.log(String)`, but a third-party library provides `AuditTrail.record(String, Severity)`. You need to bridge them.

**B.** You want to add retry logic around an existing `HttpClient.send()` without changing its interface.

**C.** You want to lazy-load a heavy `Image` object -- only load from disk when `draw()` is first called.

<details><summary>Solution</summary>

- **A: Adapter** -- the interfaces are incompatible (different method names and parameters). You need to translate one interface to another.
- **B: Decorator** -- the interface stays the same (`HttpClient.send()`), but you're adding behavior (retries) around the existing call.
- **C: Proxy** -- the interface stays the same (`Image.draw()`), but you're controlling access (lazy initialization) to the real object.

**Key distinction:**
- **Adapter** = different interface
- **Decorator** = same interface + added behavior
- **Proxy** = same interface + controlled access
</details>
