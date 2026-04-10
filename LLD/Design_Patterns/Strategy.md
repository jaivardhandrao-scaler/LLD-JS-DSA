# Strategy Design Pattern

**Category:** Behavioral
**Intent:** Encapsulate a family of algorithms, make them interchangeable, and let the client select one at runtime. The host object (context) delegates work to a strategy interface, not a concrete algorithm.
**Use cases:** Sorting algorithms selected per data shape, payment fee computation, game AI movement/pathfinding, compression algorithms, validation rules.

---

## The Problem: Algorithm Locked in Inheritance

### Scenario

You're building a list type with `sort()` and `print()`. Printing varies (horizontal vs vertical), and sorting varies per data shape (binary data -> counting sort, nearly sorted -> insertion sort, random -> quicksort).

### BAD: Subclass Explosion

```java
abstract class MyIntListBase { /* reverse/get/set/sort/print */ }
class HorizontalPrintList extends MyIntListBase { /* horizontal print */ }
class VerticalPrintList   extends MyIntListBase { /* vertical print   */ }

// Now add sorting variants:
class HorizontalBinaryList extends HorizontalPrintList { /* count sort */ }
class VerticalBinaryList   extends VerticalPrintList   { /* count sort */ }
class HorizontalAlmostSortedList extends HorizontalPrintList { /* insertion sort */ }
// ... 2 print modes x 4 sort modes = 8 classes. And growing.
```

**Problems:**
- **Combinatorial explosion:** 2 axes (print x sort) = N x M subclasses
- **SRP violation:** Each subclass mixes printing AND sorting
- **OCP violation:** New sort algorithm = new classes for EVERY print mode

### BAD: Giant if/else Inside sort()

```java
// BAD: One method knows every algorithm
class MyIntListBad {
    void sort(String dataProfile) {
        if ("BINARY".equals(dataProfile))      { /* count sort */ }
        else if ("ALMOST".equals(dataProfile)) { /* insertion sort */ }
        else if ("DESC".equals(dataProfile))   { /* reverse */ }
        else                                   { /* quicksort */ }
    }
}
```

**Problems:**
- **SRP violation:** `sort()` knows every algorithm
- **OCP violation:** New algorithm = edit this method
- **Untestable:** Can't test one algorithm in isolation

### BAD: Static "God" Utility

```java
SortUtils.sortByProfile(list, profile);
```

Couples clients to a concrete utility, hides state, hard to mock. Violates DIP.

---

## The Solution: Strategy Pattern

### The Insight

Keep **printing** as a class specialization (two small subclasses -- that's fine). Extract **sorting** into a `SortStrategy` interface. Now you can choose the optimal sorter at runtime per client or even per call.

### Step 1: Strategy Interface

```java
import java.util.List;

public interface SortStrategy {
    void sort(List<Integer> data);
    default String name() { return getClass().getSimpleName(); }
}
```

**Key:** The interface is **minimal** -- one method that does one thing.

### Step 2: Concrete Strategies

**CountSort01 -- for binary data (only 0s and 1s):**
```java
import java.util.List;

public final class CountSort01 implements SortStrategy {
    @Override public void sort(List<Integer> data) {
        int zeros = 0;
        for (int x : data) if (x == 0) zeros++;
        for (int i = 0; i < data.size(); i++)
            data.set(i, i < zeros ? 0 : 1);
    }
}
```

**InsertionSort -- for nearly-sorted data:**
```java
import java.util.List;

public final class InsertionSort implements SortStrategy {
    @Override public void sort(List<Integer> data) {
        for (int i = 1; i < data.size(); i++) {
            int key = data.get(i);
            int j = i - 1;
            while (j >= 0 && data.get(j) > key) {
                data.set(j + 1, data.get(j));
                j--;
            }
            data.set(j + 1, key);
        }
    }
}
```

**ReverseAlreadyDesc -- for known-descending data:**
```java
import java.util.Collections;
import java.util.List;

public final class ReverseAlreadyDesc implements SortStrategy {
    @Override public void sort(List<Integer> data) {
        Collections.reverse(data);  // O(n), no comparison needed
    }
}
```

**QuickSort -- general purpose:**
```java
import java.util.List;
import java.util.Random;

public final class QuickSort implements SortStrategy {
    private final Random rnd = new Random(42);

    @Override public void sort(List<Integer> a) {
        qsort(a, 0, a.size() - 1);
    }

    private void qsort(List<Integer> a, int lo, int hi) {
        if (lo >= hi) return;
        int p = lo + rnd.nextInt(hi - lo + 1);
        int pivot = a.get(p);
        int i = lo, j = hi;
        while (i <= j) {
            while (a.get(i) < pivot) i++;
            while (a.get(j) > pivot) j--;
            if (i <= j) {
                int tmp = a.get(i); a.set(i, a.get(j)); a.set(j, tmp);
                i++; j--;
            }
        }
        if (lo < j) qsort(a, lo, j);
        if (i < hi) qsort(a, i, hi);
    }
}
```

### Step 3: Context -- The Host Object

```java
import java.util.Objects;

public class StrategyEnabledList extends HorizontalPrintList {
    private SortStrategy sorter;

    public StrategyEnabledList(SortStrategy initial) {
        this.sorter = Objects.requireNonNull(initial);
    }

    // Hot-swap at runtime!
    public void setSorter(SortStrategy s) {
        this.sorter = Objects.requireNonNull(s);
    }

    @Override public void sort() {
        sorter.sort(this.data);  // delegate to strategy
    }
}
```

### Step 4: Client Picks Strategy Per Data Shape

```java
class Demo {
    public static void main(String[] args) {
        // Client 1: binary data -> counting sort
        StrategyEnabledList bin = new StrategyEnabledList(new CountSort01());
        for (int x : new int[]{1,0,1,1,0,0,1}) bin.add(x);
        bin.sort(); bin.print();

        // Client 2: almost sorted -> insertion sort
        StrategyEnabledList near = new StrategyEnabledList(new InsertionSort());
        for (int x : new int[]{1,2,3,5,4,6,7}) near.add(x);
        near.sort(); near.print();

        // Client 3: known descending -> just reverse
        StrategyEnabledList desc = new StrategyEnabledList(new ReverseAlreadyDesc());
        for (int x : new int[]{9,8,7,6,5}) desc.add(x);
        desc.sort(); desc.print();

        // Client 4: random -> quicksort
        StrategyEnabledList rnd = new StrategyEnabledList(new QuickSort());
        for (int x : new int[]{9,3,7,1,8,2,5}) rnd.add(x);
        rnd.sort(); rnd.print();

        // Hot-swap at runtime!
        rnd.setSorter(new InsertionSort());  // next sort will use insertion
    }
}
```

**Why this is better than inheritance:**
- Printing stays a small, stable specialization (2 subclasses)
- Sorting is a **separate axis of variability** -- composed via `SortStrategy`
- No subclass explosion (2 print x 4 sort = still just 2 + 4 classes, not 8)
- Runtime switching supported (A/B testing, client-specific tuning)
- Each strategy is independently testable

---

## Second Example: Checkout Fee Strategies

### Strategy Interface

```java
public interface FeeStrategy {
    Money fee(Money subtotal, CheckoutContext ctx);
    default String name() { return getClass().getSimpleName(); }
}
```

### Concrete Strategies

**FixedFee:**
```java
public final class FixedFee implements FeeStrategy {
    private final Money fixed;

    public FixedFee(Money fixed) { this.fixed = fixed; }

    @Override public Money fee(Money subtotal, CheckoutContext ctx) {
        return Money.of(subtotal.currency(), fixed.amount().toPlainString());
    }
}
```

**PercentageFee:**
```java
public final class PercentageFee implements FeeStrategy {
    private final BigDecimal pct; // e.g., 2.9 means 2.9%

    public PercentageFee(BigDecimal pct) { this.pct = pct; }

    @Override public Money fee(Money subtotal, CheckoutContext ctx) {
        return subtotal.percent(pct);
    }
}
```

**Key point:** Each strategy encapsulates ONE fee computation algorithm. The checkout service selects the right strategy based on country/plan and delegates.

### Advanced Fee Strategies (from PDF)

The PDF extends this example with **TieredFee** and **MixedFee** -- real-world patterns you'd see in payment platforms:

**TieredFee** -- piecewise percentage schedule (like tax brackets):

```java
public final class TieredFee implements FeeStrategy {
    public static final class Tier {
        public final BigDecimal upToInclusive;  // e.g., 100.00
        public final BigDecimal pct;             // e.g., 1.9 for 1.9%
        public Tier(BigDecimal upToInclusive, BigDecimal pct) {
            this.upToInclusive = upToInclusive; this.pct = pct;
        }
    }

    private final List<Tier> tiers;
    private final BigDecimal lastPct;  // overflow rate for amount beyond last tier

    public TieredFee(List<Tier> tiers, BigDecimal lastPct) {
        this.tiers = new ArrayList<>(tiers);
        this.lastPct = lastPct;
    }

    @Override public Money fee(Money subtotal, CheckoutContext ctx) {
        BigDecimal remaining = subtotal.amount();
        BigDecimal acc = BigDecimal.ZERO;
        BigDecimal prev = BigDecimal.ZERO;

        for (Tier t : tiers) {
            BigDecimal span = t.upToInclusive.subtract(prev);
            BigDecimal take = remaining.min(span).max(BigDecimal.ZERO);
            if (take.signum() > 0) {
                acc = acc.add(take.multiply(t.pct.movePointLeft(2)));
                remaining = remaining.subtract(take);
            }
            prev = t.upToInclusive;
            if (remaining.signum() <= 0) break;
        }
        if (remaining.signum() > 0) {
            acc = acc.add(remaining.multiply(lastPct.movePointLeft(2)));
        }
        return new Money(subtotal.currency(), acc);
    }
}
```

**MixedFee** -- maximum of a percentage fee and a fixed floor:

```java
public final class MixedFee implements FeeStrategy {
    private final PercentageFee percentage;
    private final FixedFee floor;

    public MixedFee(PercentageFee percentage, FixedFee floor) {
        this.percentage = percentage; this.floor = floor;
    }

    @Override public Money fee(Money subtotal, CheckoutContext ctx) {
        Money a = percentage.fee(subtotal, ctx);
        Money b = floor.fee(subtotal, ctx);
        return a.max(b);  // whichever is higher
    }
}
```

**StrategyRegistry** -- maps (country, plan) to the right fee strategy at runtime:

```java
public final class StrategyRegistry {
    private final Map<String, FeeStrategy> byKey = new ConcurrentHashMap<>();

    private static String key(String country, String plan) {
        return country + "::" + plan;
    }

    public void register(String country, String plan, FeeStrategy s) {
        byKey.put(key(country, plan), Objects.requireNonNull(s));
    }

    public FeeStrategy resolve(CheckoutContext ctx, FeeStrategy fallback) {
        return byKey.getOrDefault(key(ctx.country(), ctx.merchantPlan()), fallback);
    }
}

// Usage:
StrategyRegistry reg = new StrategyRegistry();
reg.register("US", "BASIC", pct29);
reg.register("US", "PRO",   mixed);
// Engine resolves strategy based on checkout context
```

**Exam Tip:** The registry pattern + strategy = runtime-configurable behavior without `if/else` chains. This combo is very common in production systems and a strong exam/viva answer.

---

### Third Example: Game AI Steering (from PDF)

NPCs in a game need different movement behaviors (Seek, Evade) that can be hot-swapped at runtime:

```java
public final class Vector2 {
    public final double x, y;
    public Vector2(double x, double y) { this.x = x; this.y = y; }
    public Vector2 add(Vector2 o) { return new Vector2(x + o.x, y + o.y); }
    public Vector2 sub(Vector2 o) { return new Vector2(x - o.x, y - o.y); }
    public Vector2 scale(double s) { return new Vector2(x * s, y * s); }
    public double length() { return Math.hypot(x, y); }
    public Vector2 normalized() { double L = length(); return L == 0 ? this : scale(1.0/L); }
}

// Strategy interface
public interface SteeringStrategy {
    Vector2 steer(SteeringState s);
    default String name() { return getClass().getSimpleName(); }
}

// Concrete strategy: move toward target
public final class Seek implements SteeringStrategy {
    @Override public Vector2 steer(SteeringState s) {
        Vector2 dir = s.target.sub(s.position).normalized();
        return dir.scale(s.maxAccel);
    }
}

// Concrete strategy: move away from threat
public final class Evade implements SteeringStrategy {
    @Override public Vector2 steer(SteeringState s) {
        Vector2 away = s.position.sub(s.threat).normalized();
        return away.scale(s.maxAccel);
    }
}

// Context: the NPC
public final class Mover {
    private Vector2 velocity = new Vector2(0, 0);
    private SteeringStrategy strategy;

    public Mover(SteeringStrategy initial) { this.strategy = initial; }
    public void setStrategy(SteeringStrategy s) { this.strategy = s; }

    public Vector2 update(SteeringState state, double dt) {
        Vector2 accel = strategy.steer(state);
        velocity = velocity.add(accel.scale(dt));
        return state.position.add(velocity.scale(dt));
    }
}

// Usage: NPC starts with Seek, switches to Evade when low health
Mover npc = new Mover(new Seek());
// ... later, health drops:
npc.setStrategy(new Evade());  // hot-swap at runtime!
```

**Why this is Strategy:** The Mover has a FIXED update loop (`update()`), but the steering behavior is swappable. Adding `Wander`, `Patrol`, or `Flee` requires ZERO changes to `Mover`.

**Strategy vs State distinction:** Strategy externalizes *which* algorithm to apply (chosen by configuration/context). State models an object's internal state machine where behavior changes *as it transitions*. You may change a Strategy arbitrarily; State transitions are constrained by the state model.

---

## Design Heuristics

- Keep the strategy interface **minimal** (one method for sorting, one for fees)
- Prefer **stateless** strategies with immutable configuration
- Select via **constructor injection**, factory, or configuration mapping -- avoid `instanceof`
- Document **pre/post conditions** (e.g., "sorts in-place, ascending")
- Be explicit about **time/space complexity** and data-shape assumptions

---

## SOLID Connection

| Principle | How Strategy Relates |
|-----------|---------------------|
| **SRP** | Each strategy class has ONE responsibility (one algorithm) |
| **OCP** | New algorithms = new strategy classes. Context unchanged |
| **LSP** | All strategies are interchangeable via the same interface |
| **ISP** | Strategy interface is minimal -- one method |
| **DIP** | Context depends on `SortStrategy` interface, not concrete algorithms |

---

## Strategy vs Related Patterns

| Pattern | Intent | When to Use |
|---------|--------|-------------|
| **Strategy** | Swap ONE algorithm at runtime | The "what" varies (which algorithm) |
| **Template Method** | Fixed algorithm skeleton with overridable steps | The "how" varies (specific steps in a fixed order) |
| **State** | Change behavior based on internal state | Object behaves differently as its state changes |
| **Decorator** | Add behavior by wrapping | Multiple optional, stackable behaviors |

---

## Big Picture

- Strategy is a **Behavioral** pattern focused on **interchangeable algorithms**
- The context (host) holds a reference to a strategy and delegates
- Algorithms are composed, not inherited -- avoids subclass explosion
- Supports **runtime switching** (A/B tests, feature flags, per-client tuning)
- Connected to SOLID: SRP per strategy, OCP for new strategies, DIP via interface

---

## Exam Tips (Quick Recall)

1. Strategy = **encapsulate** a family of algorithms + make them **interchangeable**
2. Context holds a **reference** to a strategy interface, not a concrete algorithm
3. Strategy uses **composition** (has-a), not inheritance (is-a)
4. Each strategy class has **one responsibility** (SRP)
5. New algorithm = new class, context untouched (OCP)
6. Supports **runtime hot-swapping** via setter
7. Avoid subclass explosion: use Strategy for independent axes of variability

---

## Viva Questions

**Q1: What is the Strategy pattern?**
A behavioral pattern that encapsulates a family of algorithms behind a common interface and lets the client (context) select one at runtime. The context delegates work to the strategy, not to a hardcoded algorithm.

**Q2: What problem does Strategy solve?**
It eliminates giant if/else blocks for algorithm selection and prevents subclass explosion when multiple axes of behavior vary independently. It also enables runtime algorithm switching.

**Q3: How is Strategy different from Template Method?**
Strategy: the ENTIRE algorithm is swapped (different strategy objects). Template Method: the algorithm skeleton is FIXED in a base class; only specific steps are overridden by subclasses. Strategy uses composition; Template Method uses inheritance.

**Q4: What are the roles in Strategy?**
- **Strategy interface:** declares the algorithm signature (e.g., `SortStrategy.sort()`)
- **Concrete strategies:** implement specific algorithms (e.g., `QuickSort`, `InsertionSort`)
- **Context:** the host object that holds a strategy reference and delegates (e.g., `StrategyEnabledList`)

**Q5: How does Strategy support OCP?**
Adding a new algorithm means creating a new strategy class. The context class is never modified. Open for extension (new strategies), closed for modification.

**Q6: Can you swap strategies at runtime?**
Yes. The context typically has a `setSorter(SortStrategy)` method (or equivalent). This enables runtime switching for A/B tests, feature flags, or per-client configuration.

**Q7: Why is Strategy better than a giant switch/if-else?**
SRP: each algorithm is a separate class. OCP: new algorithms don't require editing existing code. Testability: each strategy can be unit-tested in isolation. Readability: no 500-line method with branches.

**Q8: How is Strategy different from State?**
Strategy: the CLIENT explicitly chooses the algorithm. State: the OBJECT changes its behavior based on internal state transitions. In State, the object itself decides which behavior to use; in Strategy, the client decides.

**Q9: Should strategies be stateless?**
Prefer stateless strategies with immutable configuration. Stateful strategies (like `EqualizedStoneSpawner` with round-robin counter) work but require care around thread safety and reuse.

**Q10: When should you NOT use Strategy?**
When there's only ONE algorithm and it's unlikely to change. Adding Strategy for a single implementation is over-engineering. Also avoid if the algorithm selection is truly fixed and never changes at runtime.

---

## MCQ Quiz

**1. Strategy pattern is classified as:**
a) Creational
b) Structural
c) Behavioral
d) Concurrency

<details><summary>Answer</summary>c) Behavioral</details>

**2. The primary intent of Strategy is to:**
a) Create objects by cloning
b) Encapsulate a family of algorithms and make them interchangeable
c) Convert an incompatible interface
d) Add behavior by wrapping

<details><summary>Answer</summary>b) Encapsulate a family of algorithms and make them interchangeable</details>

**3. In Strategy, the "context" is:**
a) The algorithm itself
b) The host object that holds a strategy reference and delegates work
c) The client that creates objects
d) The interface

<details><summary>Answer</summary>b) The host object that holds a strategy reference and delegates work</details>

**4. Strategy uses which OOP mechanism?**
a) Inheritance (is-a)
b) Composition (has-a)
c) Reflection
d) Static methods

<details><summary>Answer</summary>b) Composition -- the context holds a reference to a strategy object</details>

**5. What happens when you add a new sorting algorithm to a Strategy-based system?**
a) You modify the context class
b) You create a new strategy class implementing the strategy interface
c) You add a case to a switch statement
d) You create a new context subclass

<details><summary>Answer</summary>b) Create a new strategy class -- OCP</details>

**6. Which is NOT a valid reason to use Strategy?**
a) You have multiple algorithms for the same task
b) You want to avoid subclass explosion
c) You need to change the algorithm at runtime
d) You have exactly one algorithm that never changes

<details><summary>Answer</summary>d) If there's only one algorithm, Strategy is over-engineering</details>

**7. How is Strategy different from Decorator?**
a) Strategy swaps the entire algorithm; Decorator adds behavior to an existing one
b) They are the same
c) Decorator swaps algorithms; Strategy adds behavior
d) Strategy is structural; Decorator is behavioral

<details><summary>Answer</summary>a) Strategy replaces the algorithm; Decorator wraps and enhances</details>

**8. In the sorting example, which strategy would you pick for nearly-sorted data?**
a) QuickSort
b) CountSort01
c) InsertionSort
d) ReverseAlreadyDesc

<details><summary>Answer</summary>c) InsertionSort -- O(n) for nearly-sorted data</details>

**9. The `setSorter()` method enables:**
a) Compile-time algorithm selection
b) Runtime algorithm hot-swapping
c) Algorithm deletion
d) Strategy creation

<details><summary>Answer</summary>b) Runtime algorithm hot-swapping</details>

**10. Which SOLID principle is MOST directly supported by each strategy being a separate class?**
a) LSP
b) SRP (Single Responsibility)
c) ISP
d) DIP

<details><summary>Answer</summary>b) SRP -- each class has one responsibility (one algorithm)</details>

**11. Strategy pattern avoids subclass explosion by:**
a) Using inheritance for every combination
b) Using composition for independent axes of variability
c) Eliminating all subclasses
d) Using reflection

<details><summary>Answer</summary>b) Composition for independent axes (e.g., print x sort = 2 + 4 classes, not 8)</details>

**12. How does the context know which strategy to use?**
a) Hardcoded at compile time
b) Constructor injection, setter injection, or factory/config mapping
c) Reflection
d) The strategy self-selects

<details><summary>Answer</summary>b) Constructor injection, setter, or configuration -- the client selects</details>

**13. Strategy vs State pattern:**
a) In Strategy, the CLIENT chooses; in State, the OBJECT changes behavior based on its state
b) They are identical
c) In State, the client chooses; in Strategy, the object changes
d) Neither uses interfaces

<details><summary>Answer</summary>a) Strategy: client picks. State: object's internal state determines behavior</details>

**14. A stateless strategy is preferred because:**
a) It uses more memory
b) It's thread-safe and reusable without side effects
c) It's slower
d) Java requires it

<details><summary>Answer</summary>b) Thread-safe and reusable</details>

**15. In the checkout example, `PercentageFee` and `FixedFee` share:**
a) The same implementation
b) The same `FeeStrategy` interface
c) The same base class
d) Nothing

<details><summary>Answer</summary>b) The same `FeeStrategy` interface -- they're interchangeable</details>

**16. Which is the correct relationship: Context -> Strategy?**
a) Context IS-A Strategy (inheritance)
b) Context HAS-A Strategy (composition)
c) Strategy IS-A Context
d) They are unrelated

<details><summary>Answer</summary>b) Context HAS-A Strategy (composition)</details>

**17. What makes `CountSort01` the right strategy for binary data?**
a) It's the fastest general-purpose sort
b) It's O(n) for data with only 0s and 1s -- no comparisons needed
c) It uses the least memory
d) It's the simplest to implement

<details><summary>Answer</summary>b) O(n) for binary data -- counts zeros, fills in order</details>

**18. If a system has 3 print modes and 5 sort algorithms, how many classes does Strategy need vs inheritance?**
a) Strategy: 3 + 5 = 8, Inheritance: 3 x 5 = 15
b) Strategy: 15, Inheritance: 8
c) Both need 8
d) Both need 15

<details><summary>Answer</summary>a) Strategy avoids the combinatorial explosion: 3 + 5 instead of 3 x 5</details>

**19. What pattern does the `generateWave` + `createStone` example ALSO demonstrate?**
a) Only Strategy
b) Factory Method (with Strategy for selection policy)
c) Singleton
d) Builder

<details><summary>Answer</summary>b) Factory Method -- the creation step is overridden by subclasses. Strategy is related but the Stone example is Factory Method.</details>

**20. The `default String name()` method in the strategy interface is used for:**
a) Sorting
b) Telemetry/logging -- identifying which strategy was used
c) Creating new strategies
d) Nothing useful

<details><summary>Answer</summary>b) Telemetry and logging -- helps track which algorithm ran</details>

### Scoring
- **18-20:** Strategy mastered.
- **14-17:** Good. Review Strategy vs State/Template Method.
- **10-13:** Revisit composition vs inheritance.
- **Below 10:** Re-read from the beginning.

---

## Coding Exam Questions

### Problem 1: Refactor to Strategy

The following code has all discount logic in one method. Refactor it to use Strategy.

```java
class DiscountCalculator {
    double calculate(double price, String type) {
        if (type.equals("FLAT"))         return price - 50;
        if (type.equals("PERCENT"))      return price * 0.8;  // 20% off
        if (type.equals("BUY2GET1"))     return price * 2.0 / 3.0;
        if (type.equals("SEASONAL"))     return price * 0.7;  // 30% off
        return price;
    }
}
```

<details><summary>Solution</summary>

```java
// Strategy interface
interface DiscountStrategy {
    double apply(double price);
}

// Concrete strategies
class FlatDiscount implements DiscountStrategy {
    private final double amount;
    FlatDiscount(double amount) { this.amount = amount; }
    public double apply(double price) { return Math.max(0, price - amount); }
}

class PercentDiscount implements DiscountStrategy {
    private final double pct; // 0.0 to 1.0
    PercentDiscount(double pct) { this.pct = pct; }
    public double apply(double price) { return price * (1 - pct); }
}

class Buy2Get1Discount implements DiscountStrategy {
    public double apply(double price) { return price * 2.0 / 3.0; }
}

class SeasonalDiscount implements DiscountStrategy {
    private final double pct;
    SeasonalDiscount(double pct) { this.pct = pct; }
    public double apply(double price) { return price * (1 - pct); }
}

// Context
class DiscountCalculator {
    private final DiscountStrategy strategy;
    DiscountCalculator(DiscountStrategy strategy) { this.strategy = strategy; }

    double calculate(double price) {
        return strategy.apply(price);
    }
}

// Usage
DiscountCalculator calc = new DiscountCalculator(new PercentDiscount(0.2));
System.out.println(calc.calculate(100)); // 80.0
```
</details>

---

### Problem 2: Strategy with Runtime Switching

Design a `TextFormatter` that supports multiple formatting strategies (`UpperCase`, `LowerCase`, `TitleCase`) and can switch at runtime. Show that the same formatter object can produce different outputs.

<details><summary>Solution</summary>

```java
// Strategy
interface FormatStrategy {
    String format(String text);
}

class UpperCaseStrategy implements FormatStrategy {
    public String format(String text) { return text.toUpperCase(); }
}

class LowerCaseStrategy implements FormatStrategy {
    public String format(String text) { return text.toLowerCase(); }
}

class TitleCaseStrategy implements FormatStrategy {
    public String format(String text) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}

// Context
class TextFormatter {
    private FormatStrategy strategy;

    TextFormatter(FormatStrategy strategy) { this.strategy = strategy; }

    void setStrategy(FormatStrategy strategy) { this.strategy = strategy; }

    String format(String text) { return strategy.format(text); }
}

// Demo
class Demo {
    public static void main(String[] args) {
        TextFormatter fmt = new TextFormatter(new UpperCaseStrategy());
        System.out.println(fmt.format("hello world")); // HELLO WORLD

        fmt.setStrategy(new LowerCaseStrategy());
        System.out.println(fmt.format("HELLO WORLD")); // hello world

        fmt.setStrategy(new TitleCaseStrategy());
        System.out.println(fmt.format("hello world")); // Hello World
    }
}
```
</details>

---

### Problem 3: Identify Strategy vs Template Method

**Code A:**
```java
abstract class DataProcessor {
    final void process(List<String> data) {
        validate(data);
        transform(data);
        save(data);
    }
    abstract void validate(List<String> data);
    abstract void transform(List<String> data);
    void save(List<String> data) { System.out.println("Saved: " + data); }
}
```

**Code B:**
```java
interface CompressionAlgorithm {
    byte[] compress(byte[] data);
}
class FileCompressor {
    private CompressionAlgorithm algo;
    FileCompressor(CompressionAlgorithm algo) { this.algo = algo; }
    byte[] compressFile(byte[] fileData) { return algo.compress(fileData); }
}
```

Which is Strategy? Which is Template Method? Explain why.

<details><summary>Solution</summary>

- **Code A: Template Method.** The base class defines a FIXED algorithm skeleton (`process()` calls `validate`, `transform`, `save` in order). Subclasses override STEPS within the algorithm. Uses **inheritance**.

- **Code B: Strategy.** The context (`FileCompressor`) holds a reference to a `CompressionAlgorithm` and DELEGATES the entire compression task to it. The algorithm is not broken into steps -- it's a single interchangeable unit. Uses **composition**.

**Key differences:**
| Aspect | Template Method (Code A) | Strategy (Code B) |
|--------|-------------------------|-------------------|
| Mechanism | Inheritance | Composition |
| What varies | Individual steps within a fixed skeleton | The entire algorithm |
| Runtime swap? | No (class is fixed at construction) | Yes (can change `algo` via setter) |
| Coupling | Tight (subclass + base class) | Loose (interface-based) |
</details>
