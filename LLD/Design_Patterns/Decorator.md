# Decorator Design Pattern

**Category:** Structural
**Intent:** Attach additional responsibilities to an object dynamically by wrapping it. Decorators provide a flexible alternative to subclassing for extending behavior.
**Use cases:** Notification services with optional channels, HTTP client pipelines (retry, caching, auth, metrics), game damage modifiers, I/O stream wrappers (Java's `BufferedInputStream`, `DataInputStream`).

---

## The Problem: Combinatorial Behavior Explosion

### Scenario

You have a `NotificationService` that sends email. Now clients want:
- Client A: Email + WhatsApp
- Client B: Email + SMS
- Client C: Email + WhatsApp + Slack
- Others may want retries, rate limiting, templating, audit logs...

These behaviors are **optional**, **combinable**, and must be **configurable per tenant**.

### BAD Option 1: Subclass Per Combination

```java
class EmailNotifier implements Notifier { /* email */ }
class EmailAndWhatsAppNotifier implements Notifier { /* email + whatsapp */ }
class EmailAndSmsNotifier implements Notifier { /* email + sms */ }
class EmailAndWhatsAppAndSmsNotifier implements Notifier { /* email + wa + sms */ }
// With N channels: up to 2^N - 1 combinations!
```

**Problems:**
- **Combinatorial explosion:** N channels = 2^N - 1 classes
- **Rigid:** Adding ONE new channel forces many new classes
- **Duplication:** Common behavior (formatting, retries) copy-pasted across classes
- **OCP violation:** The type lattice must be edited for every new combination

### BAD Option 2: One Class with Boolean Flags

```java
// BAD: God class with flags
class ConfigurableNotifier implements Notifier {
    private final boolean enableWhatsApp;
    private final boolean enableSms;
    private final boolean enableSlack;

    public void notify(String text) {
        email.send(text);
        if (enableWhatsApp) wa.send(text);
        if (enableSms) sms.send(text);
        // later: if (enableSlack) ...
    }
}
```

**Problems:**
- **SRP violation:** One class decides which channels, how to send, where to log
- **OCP violation:** New channel = edit the `notify()` method
- **Constructor bloat:** Growing parameter list with nulls for unused dependencies
- **Testing nightmare:** Many flag combinations to cover

---

## The Solution: Decorator Pattern

### The Idea

Like wrapping a parcel with optional services: fragile sticker, insurance, tracking. Each wrapper **adds behavior** without changing the core. Stack layers, remove layers, keep the core unchanged. Each wrapper is still a `Notifier` -- clients don't know how many layers exist.

### How It Works

1. Keep a stable **component interface** (e.g., `Notifier`)
2. Implement the **baseline** as a concrete component (e.g., `EmailNotifier`)
3. Add optional behaviors as **decorators** that:
   - Implement the SAME interface
   - Wrap another component (composition)
   - Forward the call and add behavior before/after

### The Structure

```
Component (interface)
    |
    +-- ConcreteComponent (e.g., EmailNotifier)
    |
    +-- Decorator (abstract, wraps a Component)
            |
            +-- ConcreteDecoratorA (e.g., WhatsAppDecorator)
            +-- ConcreteDecoratorB (e.g., SmsDecorator)
            +-- ConcreteDecoratorC (e.g., MetricsDecorator)
```

---

## Example 1: HTTP Client Pipeline

This is the most exam-friendly example -- retries, caching, auth, compression, metrics as stackable decorators.

### Component Interface and Concrete Component

```java
// DTOs
public class HttpRequest {
    public final String method;
    public final String url;
    public final Map<String, String> headers;
    public final byte[] body;

    public HttpRequest(String method, String url,
                       Map<String, String> headers, byte[] body) {
        this.method = method;
        this.url = url;
        this.headers = headers != null ? new LinkedHashMap<>(headers) : new LinkedHashMap<>();
        this.body = body != null ? body.clone() : null;
    }

    public HttpRequest withHeader(String k, String v) {
        Map<String, String> h = new LinkedHashMap<>(this.headers);
        h.put(k, v);
        return new HttpRequest(this.method, this.url, h, this.body);
    }
}

public class HttpResponse {
    public final int status;
    public final Map<String, String> headers;
    public final byte[] body;
    // constructor...
}

// Component interface
public interface HttpClient {
    HttpResponse send(HttpRequest req);
}

// Concrete component -- the real HTTP call
public class BaseHttpClient implements HttpClient {
    @Override
    public HttpResponse send(HttpRequest req) {
        String echo = "BaseHttpClient -> " + req.method + " " + req.url;
        return new HttpResponse(200, Map.of("X-Echo", "ok"),
            echo.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
```

### Decorator Base Class

```java
public abstract class HttpClientDecorator implements HttpClient {
    protected final HttpClient inner;

    protected HttpClientDecorator(HttpClient inner) {
        this.inner = inner;
    }

    @Override
    public HttpResponse send(HttpRequest req) {
        return inner.send(req);  // default: pass through
    }
}
```

**Key:** The decorator base class:
- **Implements** `HttpClient` (same interface as the component)
- **Wraps** another `HttpClient` (composition)
- **Delegates** by default (subclasses override to add behavior)

### Concrete Decorators

**Retries with backoff:**
```java
public class RetryingHttpClient extends HttpClientDecorator {
    private final int maxAttempts;
    private final long baseBackoffMillis;

    public RetryingHttpClient(HttpClient inner, int maxAttempts, long baseBackoffMillis) {
        super(inner);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.baseBackoffMillis = Math.max(0, baseBackoffMillis);
    }

    @Override
    public HttpResponse send(HttpRequest req) {
        int attempt = 0;
        HttpResponse last = null;
        while (attempt < maxAttempts) {
            attempt++;
            last = inner.send(req);
            if (last.status < 500) return last;
            try { Thread.sleep(baseBackoffMillis * attempt); }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return last;
    }
}
```

**Caching (GET only, with TTL):**
```java
public class CachingHttpClient extends HttpClientDecorator {
    private final ConcurrentMap<String, Entry> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public CachingHttpClient(HttpClient inner, long ttlMillis) {
        super(inner);
        this.ttlMillis = ttlMillis;
    }

    @Override
    public HttpResponse send(HttpRequest req) {
        if ("GET".equalsIgnoreCase(req.method)) {
            String k = req.method + " " + req.url;
            Entry e = cache.get(k);
            long now = System.currentTimeMillis();
            if (e != null && e.expiry > now) return e.resp;
            HttpResponse fresh = inner.send(req);
            cache.put(k, new Entry(fresh, now + ttlMillis));
            return fresh;
        }
        return inner.send(req);
    }

    private static final class Entry {
        final HttpResponse resp; final long expiry;
        Entry(HttpResponse r, long e) { resp = r; expiry = e; }
    }
}
```

**Authentication header:**
```java
public class AuthHttpClient extends HttpClientDecorator {
    private final Supplier<String> tokenSupplier;

    public AuthHttpClient(HttpClient inner, Supplier<String> tokenSupplier) {
        super(inner);
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public HttpResponse send(HttpRequest req) {
        HttpRequest with = req.withHeader("Authorization", "Bearer " + tokenSupplier.get());
        return inner.send(with);
    }
}
```

**Metrics (timing + counting):**
```java
public class MetricsHttpClient extends HttpClientDecorator {
    private final AtomicLong calls = new AtomicLong();
    private final AtomicLong totalNanos = new AtomicLong();

    public MetricsHttpClient(HttpClient inner) { super(inner); }

    @Override
    public HttpResponse send(HttpRequest req) {
        long t0 = System.nanoTime();
        try { return inner.send(req); }
        finally {
            long dur = System.nanoTime() - t0;
            calls.incrementAndGet();
            totalNanos.addAndGet(dur);
        }
    }
}
```

### Wiring -- Order Matters!

```java
HttpClient base = new BaseHttpClient();

// Order A: Metrics outside, Retry outside Cache
HttpClient clientA = new MetricsHttpClient(
    new RetryingHttpClient(
        new CachingHttpClient(
            new AuthHttpClient(
                new CompressingHttpClient(base),
                () -> "token-abc"),
            5_000),
        3, 50));

clientA.send(request);  // Metrics -> Retry -> Cache -> Auth -> Compress -> Base
```

**Key insight:** Decorator order affects semantics. E.g.:
- Metrics **outside** Retry = measures total time including retries
- Metrics **inside** Retry = measures each individual attempt
- Cache **outside** Retry = cache prevents retries for cached responses
- Cache **inside** Retry = each retry attempt checks cache

---

## Example 2: Game Damage Modifiers

```java
public interface DamageSource {
    double applyTo(double baseHp);
    String describe();
}

public class BaseHit implements DamageSource {
    private final double damage;
    public BaseHit(double damage) { this.damage = damage; }
    public double applyTo(double baseHp) { return Math.max(0, baseHp - damage); }
    public String describe() { return "Base(" + damage + ")"; }
}

public abstract class DamageDecorator implements DamageSource {
    protected final DamageSource inner;
    protected DamageDecorator(DamageSource inner) { this.inner = inner; }
    public double applyTo(double baseHp) { return inner.applyTo(baseHp); }
    public String describe() { return inner.describe(); }
}

public class PoisonDamage extends DamageDecorator {
    private final double dot;
    public PoisonDamage(DamageSource inner, double dot) {
        super(inner); this.dot = dot;
    }
    public double applyTo(double baseHp) {
        double hpAfter = inner.applyTo(baseHp);
        return Math.max(0, hpAfter - dot);
    }
    public String describe() { return inner.describe() + " -> Poison(" + dot + ")"; }
}

public class CriticalStrike extends DamageDecorator {
    private final double multiplier;
    public CriticalStrike(DamageSource inner, double multiplier) {
        super(inner); this.multiplier = multiplier;
    }
    public double applyTo(double baseHp) {
        double hpAfter = inner.applyTo(baseHp);
        double delta = baseHp - hpAfter;
        double critDelta = delta * multiplier;
        return Math.max(0, baseHp - critDelta);
    }
    public String describe() { return inner.describe() + " -> Crit(x" + multiplier + ")"; }
}

// Wiring: stack order matters!
DamageSource build = new PoisonDamage(
    new CriticalStrike(new BaseHit(40), 1.5), 8);
double hp = 150;
System.out.println(build.describe() + " => HP " + hp + " -> " + build.applyTo(hp));
```

---

## Design Heuristics

- Keep the **component interface minimal** so decorators remain simple
- **Document ordering** when it affects semantics (e.g., metrics outermost, auth before network)
- Prefer **stateless** decorators; if stateful (cache, rate limit), keep state encapsulated
- Expose **configuration** via constructors/builders, not runtime `instanceof`
- **Preserve contracts (LSP):** a decorator must not change the expected semantics of the interface

---

## Decorator vs Related Patterns

| Pattern | Intent | Key Difference |
|---------|--------|---------------|
| **Decorator** | Add behavior by wrapping, same interface | Stackable, combinable, order-sensitive |
| **Adapter** | Convert one interface to another | Changes the interface, not the behavior |
| **Proxy** | Control access (lazy load, security, remote) | Same interface, but controls access, not behavior |
| **Strategy** | Swap entire algorithm | One algorithm at a time, not stackable |

**Exam Tip:** The examiner WILL ask "how is Decorator different from Proxy/Adapter/Strategy?" Know the table above.

---

## SOLID Connection

| Principle | How Decorator Follows It |
|-----------|------------------------|
| **SRP** | One responsibility per decorator (SMS send, retry, metrics) |
| **OCP** | Add new behaviors by adding classes, not editing existing ones |
| **LSP** | Decorators remain substitutable for the component |
| **ISP** | Component interface is lean |
| **DIP** | Clients depend on `HttpClient`/`Notifier` abstraction, not concrete stacks |

---

## Pitfalls and How to Avoid Them

| Pitfall | Prevention |
|---------|-----------|
| **Order confusion** | Document recommended orders (metrics outermost, auth before network) |
| **State leakage** | Use immutable configs and request-scoped state; avoid globals |
| **Swallowed errors** | Log at boundaries and rethrow; avoid double-wrapping exceptions |
| **Over-decoration** | Measure latency per layer; prune low-value layers |
| **Type checks in decorators** | Use constructor-injected collaborators, not `instanceof` |

---

## When to Use / When NOT to Use

**Use Decorator when:**
- Behaviors are **optional, combinable, and order-sensitive**
- You need **runtime flexibility** (per-tenant, per-request configuration)
- You want to avoid **inheritance explosion** for optional features

**Do NOT use when:**
- One algorithm choice suffices (use **Strategy**)
- You need to adapt interfaces (use **Adapter**)
- You need to control access (use **Proxy**)
- Behavior is not stackable

---

## Big Picture

- Decorator is a **Structural** pattern that wraps objects to add behavior
- Java I/O streams are the classic example: `new BufferedReader(new InputStreamReader(new FileInputStream("f.txt")))`
- Each layer is transparent to the client (same interface)
- Order of wrapping affects behavior -- think of it like an onion
- Connected to: **Adapter** (changes interface), **Proxy** (controls access), **Composite** (tree structure with same interface)

---

## Exam Tips (Quick Recall)

1. Decorator = **wrap** an object to add behavior, **same interface**
2. Each decorator **implements** the component interface and **wraps** another component
3. Decorators are **stackable** and **order-sensitive**
4. Abstract decorator base class holds a reference to `inner` and delegates by default
5. **Not the same as Proxy** (access control) or **Adapter** (interface change)
6. Avoids 2^N subclass explosion for N optional behaviors
7. Classic Java example: `BufferedInputStream(FileInputStream)` -- I/O stream wrapping

---

## Viva Questions

**Q1: What is the Decorator pattern?**
A structural pattern that attaches additional responsibilities to an object dynamically by wrapping it. The decorator implements the same interface as the component, holds a reference to the wrapped object, and forwards calls while adding behavior before or after.

**Q2: What problem does Decorator solve?**
It eliminates subclass explosion when you have N optional, combinable behaviors. Instead of 2^N subclasses for every combination, you create N decorator classes and compose them at runtime.

**Q3: How is Decorator different from inheritance?**
Inheritance is static (fixed at compile time) and creates tight coupling. Decorator uses composition (wrapping), is dynamic (stack/unstack at runtime), and each decorator has a single responsibility. Inheritance leads to combinatorial explosion; Decorator avoids it.

**Q4: How is Decorator different from Proxy?**
Decorator ADDS responsibilities (retry, caching, metrics). Proxy CONTROLS ACCESS (lazy loading, security checks, remote delegation). Both use the same interface and composition, but their intent is different.

**Q5: How is Decorator different from Adapter?**
Adapter CHANGES the interface (makes incompatible interfaces work together). Decorator KEEPS the same interface and adds behavior. Adapter translates; Decorator enhances.

**Q6: Why does order matter in Decorator?**
Because each decorator wraps the next. The outermost decorator executes first. Example: Metrics outside Retry measures total time (including retries). Metrics inside Retry measures each attempt separately.

**Q7: What is the abstract decorator base class for?**
It implements the component interface, holds a reference to the wrapped component (`inner`), and provides a default pass-through implementation. Concrete decorators extend it and override to add behavior. It reduces boilerplate.

**Q8: Can decorators change the return type or throw new exceptions?**
No. Decorators must honor the interface contract (LSP). They should not change return types, widen/narrow types, or throw unexpected exceptions.

**Q9: How does Decorator follow OCP?**
New behaviors are added by creating new decorator classes. Existing decorators and the base component are never modified. Open for extension, closed for modification.

**Q10: Name a real-world Java example of Decorator.**
Java I/O streams: `new BufferedReader(new InputStreamReader(new FileInputStream("file.txt")))`. Each wrapper adds behavior (buffering, character decoding) without changing the `Reader` interface.

---

## MCQ Quiz

**1. Decorator pattern is classified as:**
a) Creational
b) Structural
c) Behavioral
d) Concurrency

<details><summary>Answer</summary>b) Structural</details>

**2. The primary intent of Decorator is:**
a) Convert an incompatible interface
b) Add responsibilities to an object dynamically by wrapping
c) Ensure only one instance exists
d) Swap algorithms at runtime

<details><summary>Answer</summary>b) Add responsibilities to an object dynamically by wrapping</details>

**3. A decorator must:**
a) Extend the concrete component class
b) Implement the same interface as the component AND wrap another component
c) Use reflection
d) Be abstract

<details><summary>Answer</summary>b) Implement the same interface AND wrap another component</details>

**4. With 5 optional behaviors, inheritance needs up to:**
a) 5 classes
b) 10 classes
c) 2^5 - 1 = 31 classes
d) 25 classes

<details><summary>Answer</summary>c) 2^5 - 1 = 31 classes for every combination</details>

**5. With 5 optional behaviors, Decorator needs:**
a) 31 classes
b) 5 decorator classes + 1 base component = 6
c) 25 classes
d) 1 class

<details><summary>Answer</summary>b) 5 decorator classes + 1 base component (compose at runtime)</details>

**6. In the HTTP example, `RetryingHttpClient extends HttpClientDecorator`. What does `HttpClientDecorator` do?**
a) Makes HTTP calls
b) Holds a reference to `inner`, implements `HttpClient`, delegates by default
c) Manages the cache
d) Handles authentication

<details><summary>Answer</summary>b) Holds `inner`, implements `HttpClient`, provides pass-through default</details>

**7. What determines the execution order of decorators?**
a) Alphabetical order
b) The order they are wrapped (outermost executes first)
c) Random
d) The interface defines order

<details><summary>Answer</summary>b) The order of wrapping -- outermost decorator executes first</details>

**8. If Metrics wraps Retry wraps Cache, the call order is:**
a) Cache -> Retry -> Metrics -> Base
b) Metrics -> Retry -> Cache -> Base
c) Base -> Cache -> Retry -> Metrics
d) Random

<details><summary>Answer</summary>b) Metrics -> Retry -> Cache -> Base (outer to inner)</details>

**9. How is Decorator different from Strategy?**
a) Decorator swaps algorithms; Strategy wraps objects
b) Strategy swaps ONE algorithm; Decorator adds MULTIPLE stackable behaviors
c) They are identical
d) Decorator is behavioral; Strategy is structural

<details><summary>Answer</summary>b) Strategy: one interchangeable algorithm. Decorator: multiple stackable behaviors</details>

**10. Which Java API is a classic Decorator example?**
a) `java.util.Collections`
b) `java.io` stream wrappers (BufferedInputStream, DataInputStream)
c) `java.lang.String`
d) `java.util.HashMap`

<details><summary>Answer</summary>b) java.io stream wrappers</details>

**11. Can decorators be removed at runtime?**
a) No, they are permanent
b) Yes, by recomposing the chain without the unwanted decorator
c) Only through reflection
d) Only by restarting the JVM

<details><summary>Answer</summary>b) Yes -- recompose without the unwanted decorator</details>

**12. What does the `inner.send(req)` call in a decorator do?**
a) Sends the request directly to the server
b) Delegates to the next layer in the chain (which may be another decorator or the base)
c) Creates a new decorator
d) Returns null

<details><summary>Answer</summary>b) Delegates to the next layer in the decorator chain</details>

**13. A decorator should NOT:**
a) Implement the component interface
b) Forward calls to the wrapped object
c) Change the return type or violate the interface contract
d) Add behavior before/after the delegated call

<details><summary>Answer</summary>c) Should NOT change return type or violate contract (LSP)</details>

**14. `CachingHttpClient` only caches GET requests because:**
a) POST/PUT have side effects and shouldn't be cached
b) GET requests are slower
c) The specification requires it
d) It's a design flaw

<details><summary>Answer</summary>a) POST/PUT have side effects -- caching them would be incorrect</details>

**15. What is "over-decoration"?**
a) Using too many design patterns
b) Adding too many low-value decorator layers, increasing latency and complexity
c) Using decorators instead of inheritance
d) Having a minimal interface

<details><summary>Answer</summary>b) Too many low-value layers -- measure latency per layer and prune</details>

**16. The abstract decorator base class is:**
a) Required by the pattern
b) Optional but reduces boilerplate -- provides default pass-through
c) The same as the concrete component
d) An interface

<details><summary>Answer</summary>b) Optional but practical -- reduces boilerplate in concrete decorators</details>

**17. In the game example, `PoisonDamage(CriticalStrike(BaseHit(40)))` means:**
a) Poison is applied first, then crit, then base
b) Base hit first, then crit multiplier, then poison DOT
c) Only poison damage is applied
d) The order doesn't matter

<details><summary>Answer</summary>b) Base hit damage first, then crit amplifies it, then poison applies additional DOT</details>

**18. Why use a `Supplier<String>` for the auth token instead of a fixed string?**
a) It's slower
b) Tokens expire; `Supplier` fetches a fresh one each time
c) Java requires it
d) It saves memory

<details><summary>Answer</summary>b) Tokens expire -- the Supplier can fetch/refresh the token on each call</details>

**19. Decorator vs Composite pattern:**
a) They are unrelated
b) Both use the same interface recursively; Decorator adds behavior, Composite builds tree structures
c) Composite adds behavior; Decorator builds trees
d) They are identical

<details><summary>Answer</summary>b) Both use recursive composition on the same interface, but with different intents</details>

**20. Which wiring gives "retry THEN cache" (retry on cache miss only)?**
a) `new RetryingHttpClient(new CachingHttpClient(base))`
b) `new CachingHttpClient(new RetryingHttpClient(base))`
c) `new CachingHttpClient(base)`
d) `new RetryingHttpClient(base)`

<details><summary>Answer</summary>b) `CachingHttpClient(RetryingHttpClient(base))` -- cache checks first, on miss delegates to retry which delegates to base</details>

**21. The boolean-flags approach (BAD Option 2) violates which principles?**
a) Only SRP
b) SRP, OCP, and leads to constructor bloat
c) Only OCP
d) None -- it's a valid approach

<details><summary>Answer</summary>b) SRP (too many reasons to change), OCP (new channel = edit notify()), plus constructor bloat and testing nightmare</details>

### Scoring
- **19-21:** Decorator mastered.
- **15-18:** Good. Review order semantics and pattern distinctions.
- **11-14:** Revisit the HTTP pipeline example.
- **Below 11:** Re-read from the beginning.

---

## Coding Exam Questions

### Problem 1: Implement a Notification Decorator Chain

Given:
```java
interface Notifier {
    void notify(String message);
}

class EmailNotifier implements Notifier {
    public void notify(String message) {
        System.out.println("Email: " + message);
    }
}
```

**Task:** Create `WhatsAppDecorator`, `SmsDecorator`, and `LoggingDecorator` as decorators. Wire them for:
- Client A: Email + WhatsApp
- Client B: Email + SMS + Logging

<details><summary>Solution</summary>

```java
abstract class NotifierDecorator implements Notifier {
    protected final Notifier inner;
    NotifierDecorator(Notifier inner) { this.inner = inner; }
    public void notify(String message) { inner.notify(message); }
}

class WhatsAppDecorator extends NotifierDecorator {
    WhatsAppDecorator(Notifier inner) { super(inner); }
    @Override public void notify(String message) {
        inner.notify(message);
        System.out.println("WhatsApp: " + message);
    }
}

class SmsDecorator extends NotifierDecorator {
    SmsDecorator(Notifier inner) { super(inner); }
    @Override public void notify(String message) {
        inner.notify(message);
        System.out.println("SMS: " + message);
    }
}

class LoggingDecorator extends NotifierDecorator {
    LoggingDecorator(Notifier inner) { super(inner); }
    @Override public void notify(String message) {
        System.out.println("[LOG] Sending: " + message);
        inner.notify(message);
        System.out.println("[LOG] Sent successfully");
    }
}

// Client A: Email + WhatsApp
Notifier clientA = new WhatsAppDecorator(new EmailNotifier());
clientA.notify("Order confirmed");
// Output: Email: Order confirmed
//         WhatsApp: Order confirmed

// Client B: Logging + Email + SMS
Notifier clientB = new LoggingDecorator(
    new SmsDecorator(new EmailNotifier()));
clientB.notify("Payment received");
// Output: [LOG] Sending: Payment received
//         Email: Payment received
//         SMS: Payment received
//         [LOG] Sent successfully
```
</details>

---

### Problem 2: Identify the Pattern

For each snippet, identify Decorator, Adapter, Proxy, or Strategy:

**A:**
```java
class CompressedStream implements OutputStream {
    private OutputStream inner;
    CompressedStream(OutputStream inner) { this.inner = inner; }
    void write(byte[] data) {
        byte[] compressed = compress(data);
        inner.write(compressed);
    }
}
```

**B:**
```java
class LazyImage implements Image {
    private RealImage real;
    void draw() {
        if (real == null) real = new RealImage(path); // load on first use
        real.draw();
    }
}
```

**C:**
```java
class XmlToJsonAdapter implements JsonSource {
    private XmlService xml;
    String getJson(String id) {
        String xmlData = xml.fetchXml(id);
        return convertToJson(xmlData);
    }
}
```

**D:**
```java
class PaymentService {
    private PaymentMethod method;
    void setMethod(PaymentMethod m) { this.method = m; }
    void pay(double amount) { method.process(amount); }
}
```

<details><summary>Solution</summary>

- **A: Decorator** -- same interface (`OutputStream`), wraps inner, adds compression behavior
- **B: Proxy** -- same interface (`Image`), controls access (lazy loading), not adding behavior
- **C: Adapter** -- different interface (`XmlService` -> `JsonSource`), converts/translates
- **D: Strategy** -- swaps entire algorithm (`PaymentMethod`), context delegates to strategy
</details>

---

### Problem 3: Fix the Broken Decorator

```java
interface Logger {
    void log(String msg);
}

class ConsoleLogger implements Logger {
    public void log(String msg) { System.out.println(msg); }
}

class TimestampLogger implements Logger {
    public void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "] " + msg);
    }
}
```

**Task:** `TimestampLogger` is supposed to be a decorator that ADDS a timestamp and then delegates to another logger. But it's implemented as a standalone logger (doesn't wrap anything). Fix it.

<details><summary>Solution</summary>

**Problem:** `TimestampLogger` doesn't wrap an inner `Logger`. It's a separate implementation, not a decorator. You can't compose it with other loggers.

**Fixed:**
```java
class TimestampLogger implements Logger {
    private final Logger inner;

    TimestampLogger(Logger inner) { this.inner = inner; }

    @Override
    public void log(String msg) {
        String timestamped = "[" + System.currentTimeMillis() + "] " + msg;
        inner.log(timestamped);  // delegate to wrapped logger
    }
}

// Now composable:
Logger logger = new TimestampLogger(new ConsoleLogger());
logger.log("Server started");
// Output: [1712345678901] Server started   (via ConsoleLogger)

// Can stack with other decorators:
Logger fancy = new TimestampLogger(new UpperCaseLogger(new ConsoleLogger()));
```

**Key fix:** Added `inner` field (composition) and delegation. Now it's a true decorator that can wrap any `Logger`.
</details>
