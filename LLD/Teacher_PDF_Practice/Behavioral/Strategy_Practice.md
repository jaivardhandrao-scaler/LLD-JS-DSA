# Strategy Pattern -- Practice (Based on DesignPattern_Strategy.pdf)

---

## 1. Viva Questions

**Q1: In the List Sorting example, what is MyIntList and what abstract methods does it declare?**

**A1:** `MyIntList` is the abstract base class for integer lists. It declares abstract methods: `reverse()`, `get()`, `set()`, `sort()`, and `print()`. The concrete subclasses `HorizontalPrintList` and `VerticalPrintList` provide different implementations of `print()`, while `sort()` is the method that eventually gets refactored to use the Strategy pattern.

---

**Q2: Why does the PDF say subclass explosion is a problem before introducing Strategy in the sorting example?**

**A2:** Without Strategy, every combination of printing style and sorting algorithm requires its own subclass. You end up with classes like `HorizontalBinaryList`, `VerticalBinaryList`, `HorizontalInsertionList`, `VerticalInsertionList`, etc. If there are P printing styles and S sorting algorithms, you need P x S subclasses. Adding one new sort algorithm means adding P new classes. Strategy eliminates this by extracting the varying sort algorithm into a separate interface, so you only need P print subclasses and S strategy classes (P + S instead of P x S).

---

**Q3: The PDF shows a "giant if/else" anti-pattern in sort(). How does it work and why is it bad?**

**A3:** The anti-pattern uses string-based dispatch inside `sort()`. A string field stores something like `"quick"` or `"insertion"`, and the method contains a chain of if/else blocks checking that string to decide which algorithm to run. This violates the Open-Closed Principle -- adding a new algorithm means modifying the existing `sort()` method. It is also fragile (typos in strings cause silent bugs) and hard to test individual algorithms in isolation.

---

**Q4: What is the SortStrategy interface, including its methods?**

**A4:** `SortStrategy` is a functional-style interface with the method `void sort(List<Integer> data)` that performs an in-place sort on the given list. It also has a default method `name()` that returns a human-readable name for the strategy (useful for logging or display). Concrete implementations include `CountSort01` (for binary data containing only 0s and 1s), `InsertionSort` (for nearly sorted data), `ReverseAlreadyDesc` (for data that is already in descending order), and `QuickSort` (for random/general data).

---

**Q5: How does StrategyEnabledList wire everything together?**

**A5:** `StrategyEnabledList` extends `HorizontalPrintList` (inheriting the horizontal print behavior). It holds a `SortStrategy` field that can be set at runtime via `setSorter(SortStrategy s)`. It overrides `sort()` to delegate entirely to the strategy: `strategy.sort(this.data)`. This means the list object does not know or care which sorting algorithm is being used -- it just calls the strategy. The strategy can be swapped at any time without changing the list class.

---

**Q6: In the Checkout Fees domain, what is the Money value object and why is it immutable?**

**A6:** `Money` is a value object that pairs a currency (e.g., USD, EUR) with a `BigDecimal` amount. It provides arithmetic operations: `plus()`, `minus()`, `percent()`, `multiply()`, and `max()`. Each operation returns a new `Money` instance rather than modifying the existing one -- this is immutability. Immutability matters because fee calculations pass Money through multiple strategies and intermediate steps. If Money were mutable, one strategy could accidentally corrupt the value seen by another, leading to subtle rounding or currency bugs.

---

**Q7: Explain how StrategyRegistry works, including how it maps to a FeeStrategy and what happens on a miss.**

**A7:** `StrategyRegistry` maintains a map whose key is a composite of `(country, merchantPlan)` and whose value is a `FeeStrategy`. When `resolve(CheckoutContext ctx, FeeStrategy fallback)` is called, it looks up the strategy using the country and plan from the context. If a match is found, that strategy is returned. If no match is found (a miss), the provided fallback strategy is returned instead. This allows per-market fee customization while guaranteeing a safe default.

---

**Q8: What are the four concrete FeeStrategy implementations and when would each be used?**

**A8:**
- `FixedFee` -- charges a flat fee regardless of subtotal (e.g., a fixed $2.00 processing fee).
- `PercentageFee` -- charges a percentage of the subtotal (e.g., 2.5% of the cart value).
- `TieredFee` -- uses a `Tier` inner class to define piecewise percentage brackets. Different portions of the subtotal are charged at different rates (e.g., first $100 at 3%, next $400 at 2%, remainder at 1%).
- `MixedFee` -- computes both a percentage fee and a fixed floor, then returns the maximum of the two. This guarantees a minimum fee even on small transactions.

---

**Q9: In the Game AI Steering domain, describe Vector2, SteeringState, and how Seek and Evade differ.**

**A9:** `Vector2` is an immutable 2D vector with operations `add()`, `sub()`, `scale()`, `length()`, and `normalized()`. `SteeringState` bundles the agent's current `position`, `target` position, `threat` position, and `maxAccel` (maximum acceleration magnitude). `Seek` steers toward the target: it computes the direction from position to target, normalizes it, and scales by maxAccel. `Evade` steers away from the threat: it computes the direction from threat to position (the opposite direction), normalizes, and scales by maxAccel. Both implement `SteeringStrategy` with method `Vector2 steer(SteeringState s)`.

---

**Q10: How does the Mover class enable hot-swapping of steering strategies at runtime?**

**A10:** `Mover` holds a velocity vector and a `SteeringStrategy` field. Its `update(SteeringState state, double dt)` method calls `strategy.steer(state)` to get an acceleration vector, then updates velocity accordingly (velocity += acceleration * dt, then position += velocity * dt). The `setStrategy(SteeringStrategy s)` method allows swapping the strategy at any point during the game loop. For example, a game character can switch from `Seek` (chasing a target) to `Evade` (fleeing a threat) in a single frame by calling `setStrategy(new Evade())`, with no change to the Mover class itself.

---

## 2. MCQ Quiz

**Q1.** In the sorting example, which class does `StrategyEnabledList` extend?

- A) MyIntList
- B) VerticalPrintList
- C) HorizontalPrintList
- D) SortStrategy

<details><summary>Answer</summary>C) HorizontalPrintList. StrategyEnabledList extends HorizontalPrintList and adds a SortStrategy field to delegate sorting.</details>

---

**Q2.** What does CountSort01 assume about the input data?

- A) Data is already sorted in ascending order
- B) Data contains only 0s and 1s (binary data)
- C) Data is randomly distributed
- D) Data is nearly sorted

<details><summary>Answer</summary>B) Data contains only 0s and 1s (binary data). CountSort01 is a counting sort optimized for binary values.</details>

---

**Q3.** The `SortStrategy` interface's default method `name()` is used for:

- A) Selecting the strategy at compile time
- B) Logging or displaying a human-readable strategy identifier
- C) Comparing two strategies for equality
- D) Validating the input list

<details><summary>Answer</summary>B) Logging or displaying a human-readable strategy identifier. The default method returns a name string for the strategy.</details>

---

**Q4.** Which sorting strategy is designed for data that is already in descending order?

- A) InsertionSort
- B) QuickSort
- C) CountSort01
- D) ReverseAlreadyDesc

<details><summary>Answer</summary>D) ReverseAlreadyDesc. It simply reverses the list since the data is already sorted in the opposite order.</details>

---

**Q5.** What is the main problem with the "giant if/else" approach inside `sort()`?

- A) It is slower than virtual dispatch
- B) It violates the Open-Closed Principle
- C) It causes NullPointerException
- D) It requires generics

<details><summary>Answer</summary>B) It violates the Open-Closed Principle. Every new algorithm requires modifying the existing sort() method rather than adding a new class.</details>

---

**Q6.** The `Money` value object uses `BigDecimal` for its amount field instead of `double` because:

- A) BigDecimal is faster than double
- B) BigDecimal avoids floating-point rounding errors in financial calculations
- C) BigDecimal supports negative numbers
- D) BigDecimal is required by the Strategy interface

<details><summary>Answer</summary>B) BigDecimal avoids floating-point rounding errors in financial calculations. Monetary values require exact decimal arithmetic.</details>

---

**Q7.** Which operations does the `Money` class provide?

- A) plus, minus, percent, multiply, max
- B) add, subtract, divide, modulo
- C) plus, minus, divide, floor
- D) add, subtract, multiply, round

<details><summary>Answer</summary>A) plus, minus, percent, multiply, max. These are the arithmetic operations defined on the immutable Money value object.</details>

---

**Q8.** What fields does `CheckoutContext` contain?

- A) country, currency, gateway, merchantPlan, cartSubtotal
- B) country, currency, feeStrategy, total
- C) userId, cartItems, discountCode, tax
- D) country, merchantPlan, feeEngine, registry

<details><summary>Answer</summary>A) country, currency, gateway, merchantPlan, cartSubtotal. These fields provide all context needed for fee calculation.</details>

---

**Q9.** The `TieredFee` strategy uses an inner class called:

- A) Bracket
- B) Level
- C) Tier
- D) Range

<details><summary>Answer</summary>C) Tier. The Tier inner class defines piecewise percentage brackets for tiered fee computation.</details>

---

**Q10.** How does `MixedFee` compute the final fee?

- A) Adds a percentage fee and a fixed fee together
- B) Returns the minimum of a percentage fee and a fixed floor
- C) Returns the maximum of a percentage fee and a fixed floor
- D) Multiplies the percentage fee by the fixed fee

<details><summary>Answer</summary>C) Returns the maximum of a percentage fee and a fixed floor. This guarantees a minimum fee even on small transaction amounts.</details>

---

**Q11.** What is the key used by `StrategyRegistry` to look up a `FeeStrategy`?

- A) (currency, gateway)
- B) (country, merchantPlan)
- C) (merchantPlan, cartSubtotal)
- D) (country, currency)

<details><summary>Answer</summary>B) (country, merchantPlan). The registry maps this composite key to a FeeStrategy for per-market fee customization.</details>

---

**Q12.** When `StrategyRegistry.resolve()` does not find a matching entry, it:

- A) Throws a StrategyNotFoundException
- B) Returns null
- C) Returns the provided fallback strategy
- D) Creates a new FixedFee with zero amount

<details><summary>Answer</summary>C) Returns the provided fallback strategy. The resolve method accepts a fallback parameter for exactly this scenario.</details>

---

**Q13.** In `FeeEngine`, what is the role of the optional `StrategyRegistry`?

- A) It stores computed fees for caching
- B) It allows dynamic resolution of the correct FeeStrategy based on context
- C) It validates the Money value after computation
- D) It logs all fee calculations

<details><summary>Answer</summary>B) It allows dynamic resolution of the correct FeeStrategy based on context. FeeEngine's compute(ctx) uses the registry to resolve the appropriate strategy before computing.</details>

---

**Q14.** `Vector2` in the Game AI example is immutable. What does this mean for its operations?

- A) Operations modify the vector in place and return void
- B) Operations return new Vector2 instances and leave the original unchanged
- C) Operations throw exceptions if called more than once
- D) Operations can only be called from within SteeringStrategy

<details><summary>Answer</summary>B) Operations return new Vector2 instances and leave the original unchanged. Immutability ensures thread safety and predictable behavior.</details>

---

**Q15.** What does `SteeringState` bundle together?

- A) position, velocity, acceleration, drag
- B) position, target, threat, maxAccel
- C) position, target, health, armor
- D) velocity, direction, speed, maxSpeed

<details><summary>Answer</summary>B) position, target, threat, maxAccel. These are the inputs a SteeringStrategy needs to compute its acceleration vector.</details>

---

**Q16.** The `Seek` strategy computes acceleration by:

- A) Moving away from the target at maxAccel
- B) Moving toward the target: normalize(target - position) * maxAccel
- C) Moving perpendicular to the target direction
- D) Matching the target's velocity

<details><summary>Answer</summary>B) Moving toward the target: normalize(target - position) * maxAccel. Seek steers directly toward the target position.</details>

---

**Q17.** Which SOLID principle does the Strategy pattern most directly support?

- A) Single Responsibility Principle only
- B) Liskov Substitution Principle only
- C) Open-Closed Principle (extend behavior without modifying existing code)
- D) Interface Segregation Principle only

<details><summary>Answer</summary>C) Open-Closed Principle. New algorithms are added as new strategy classes without modifying the context class. (Strategy also supports SRP and LSP, but OCP is the most direct mapping.)</details>

---

**Q18.** According to the PDF, what is the key difference between Strategy and State?

- A) Strategy uses interfaces, State uses abstract classes
- B) Strategy swaps algorithms independently; State transitions are driven by internal state changes and strategies typically do not know about each other
- C) Strategy is behavioral, State is structural
- D) There is no difference

<details><summary>Answer</summary>B) Strategy swaps algorithms independently, with the client typically choosing the strategy. State transitions are driven by internal state changes, and state objects may trigger transitions to other states. In Strategy, the strategies typically do not know about each other.</details>

---

**Q19.** According to the PDF, what distinguishes Strategy from Template Method?

- A) Strategy uses composition/delegation; Template Method uses inheritance with overridden hook methods
- B) Template Method is faster at runtime
- C) Strategy requires abstract classes; Template Method requires interfaces
- D) They are identical patterns

<details><summary>Answer</summary>A) Strategy uses composition (has-a) and delegates the entire algorithm to a separate object. Template Method uses inheritance (is-a) and lets subclasses override specific steps (hooks) of an algorithm defined in a base class.</details>

---

**Q20.** Which of the following is listed as a pitfall of the Strategy pattern in the PDF?

- A) Strategies cannot be tested independently
- B) Leaky selection logic, hidden mutability, units/rounding issues, and overuse
- C) Strategies must always be singletons
- D) The context class must be final

<details><summary>Answer</summary>B) Leaky selection logic (choosing the strategy bleeds into business code), hidden mutability (strategies should ideally be stateless or immutable), units/rounding issues (especially in financial strategies), and overuse (applying Strategy where a simple conditional would suffice).</details>

---

## 3. Self-Scoring Table

| Section | Total | Your Score |
|---------|-------|------------|
| Viva Questions (1 point each) | /10 | ___/10 |
| MCQ Quiz (1 point each) | /20 | ___/20 |
| Coding Problem 1 | /10 | ___/10 |
| Coding Problem 2 | /10 | ___/10 |
| Coding Problem 3 | /10 | ___/10 |
| **Grand Total** | **/60** | **___/60** |

**Grading Guide:**
- 50-60: Excellent -- ready for viva and exam
- 40-49: Good -- review the areas you missed
- 30-39: Needs work -- re-read the PDF and redo problems
- Below 30: Start from scratch -- study the PDF carefully before retrying

---

## 4. Coding Problems

### Problem 1: Sorting Strategy from Scratch

Implement the full sorting strategy setup from the PDF.

**Requirements:**
1. Create the `SortStrategy` interface with `void sort(List<Integer> data)` and a default `String name()` method.
2. Implement `CountSort01` (counts 0s and 1s, rewrites the list), `InsertionSort`, and `QuickSort`.
3. Create `StrategyEnabledList` that holds a `List<Integer>`, a `SortStrategy`, and provides `setSorter()`, `sort()`, and `printHorizontal()`.
4. In `main`, create a list, set different strategies, sort, and print after each sort to demonstrate hot-swapping.

<details><summary>Solution</summary>

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// --- Strategy Interface ---
interface SortStrategy {
    void sort(List<Integer> data);

    default String name() {
        return getClass().getSimpleName();
    }
}

// --- Concrete Strategies ---
class CountSort01 implements SortStrategy {
    @Override
    public void sort(List<Integer> data) {
        int zeros = 0;
        for (int val : data) {
            if (val == 0) zeros++;
        }
        for (int i = 0; i < data.size(); i++) {
            data.set(i, i < zeros ? 0 : 1);
        }
    }
}

class InsertionSort implements SortStrategy {
    @Override
    public void sort(List<Integer> data) {
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

class QuickSort implements SortStrategy {
    @Override
    public void sort(List<Integer> data) {
        quickSort(data, 0, data.size() - 1);
    }

    private void quickSort(List<Integer> data, int low, int high) {
        if (low < high) {
            int pi = partition(data, low, high);
            quickSort(data, low, pi - 1);
            quickSort(data, pi + 1, high);
        }
    }

    private int partition(List<Integer> data, int low, int high) {
        int pivot = data.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (data.get(j) <= pivot) {
                i++;
                Collections.swap(data, i, j);
            }
        }
        Collections.swap(data, i + 1, high);
        return i + 1;
    }
}

// --- Context ---
class StrategyEnabledList {
    private final List<Integer> data;
    private SortStrategy strategy;

    public StrategyEnabledList(List<Integer> data) {
        this.data = new ArrayList<>(data);
    }

    public void setSorter(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void sort() {
        if (strategy == null) {
            throw new IllegalStateException("No sorting strategy set");
        }
        System.out.println("Sorting with: " + strategy.name());
        strategy.sort(data);
    }

    public void printHorizontal() {
        System.out.println(data);
    }
}

// --- Demo ---
public class SortingStrategyDemo {
    public static void main(String[] args) {
        // Binary data for CountSort01
        List<Integer> binaryData = List.of(1, 0, 1, 1, 0, 0, 1, 0);
        StrategyEnabledList list1 = new StrategyEnabledList(binaryData);
        list1.setSorter(new CountSort01());
        list1.sort();
        list1.printHorizontal();

        // Nearly sorted data for InsertionSort
        List<Integer> nearlySorted = List.of(1, 2, 4, 3, 5, 7, 6, 8);
        StrategyEnabledList list2 = new StrategyEnabledList(nearlySorted);
        list2.setSorter(new InsertionSort());
        list2.sort();
        list2.printHorizontal();

        // Hot-swap: reuse same list object with different strategy
        List<Integer> randomData = List.of(9, 3, 7, 1, 5, 8, 2, 6, 4);
        StrategyEnabledList list3 = new StrategyEnabledList(randomData);

        list3.setSorter(new QuickSort());
        list3.sort();
        list3.printHorizontal();

        // Hot-swap to InsertionSort on the same list
        list3.setSorter(new InsertionSort());
        list3.sort();
        list3.printHorizontal();
    }
}
```

</details>

---

### Problem 2: Checkout Fee Engine with TieredFee and StrategyRegistry

Implement the checkout fee calculation domain from the PDF.

**Requirements:**
1. Create an immutable `Money` class with `currency`, `BigDecimal amount`, and methods `plus()`, `minus()`, `percent()`, `multiply()`, `max()`. Each method returns a new `Money`.
2. Create `CheckoutContext` with fields: `country`, `currency`, `gateway`, `merchantPlan`, `cartSubtotal` (Money).
3. Create the `FeeStrategy` interface: `Money fee(Money subtotal, CheckoutContext ctx)`.
4. Implement `FixedFee`, `PercentageFee`, and `TieredFee` (with a `Tier` inner class holding `upperBound` and `rate`).
5. Implement `MixedFee` that returns `max(percentageFee, fixedFloor)`.
6. Create `StrategyRegistry` that maps `(country, merchantPlan)` to a `FeeStrategy`, with `resolve(CheckoutContext ctx, FeeStrategy fallback)`.
7. Create `FeeEngine` with a default strategy and optional registry. Its `compute(CheckoutContext ctx)` resolves the strategy and returns the fee.
8. In `main`, register different strategies for different country/plan combos, then compute fees for various contexts.

<details><summary>Solution</summary>

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// --- Money Value Object (Immutable) ---
final class Money {
    private final String currency;
    private final BigDecimal amount;

    public Money(String currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money(String currency, double amount) {
        this(currency, BigDecimal.valueOf(amount));
    }

    public String getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }

    public Money plus(Money other) {
        assertSameCurrency(other);
        return new Money(currency, amount.add(other.amount));
    }

    public Money minus(Money other) {
        assertSameCurrency(other);
        return new Money(currency, amount.subtract(other.amount));
    }

    public Money percent(BigDecimal rate) {
        return new Money(currency,
            amount.multiply(rate)
                  .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    public Money multiply(BigDecimal factor) {
        return new Money(currency, amount.multiply(factor));
    }

    public Money max(Money other) {
        assertSameCurrency(other);
        return amount.compareTo(other.amount) >= 0 ? this : other;
    }

    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    @Override
    public String toString() {
        return currency + " " + amount.toPlainString();
    }
}

// --- Checkout Context ---
class CheckoutContext {
    private final String country;
    private final String currency;
    private final String gateway;
    private final String merchantPlan;
    private final Money cartSubtotal;

    public CheckoutContext(String country, String currency, String gateway,
                           String merchantPlan, Money cartSubtotal) {
        this.country = country;
        this.currency = currency;
        this.gateway = gateway;
        this.merchantPlan = merchantPlan;
        this.cartSubtotal = cartSubtotal;
    }

    public String getCountry() { return country; }
    public String getCurrency() { return currency; }
    public String getGateway() { return gateway; }
    public String getMerchantPlan() { return merchantPlan; }
    public Money getCartSubtotal() { return cartSubtotal; }
}

// --- Fee Strategy Interface ---
interface FeeStrategy {
    Money fee(Money subtotal, CheckoutContext ctx);
}

// --- Concrete Strategies ---
class FixedFee implements FeeStrategy {
    private final Money fixedAmount;

    public FixedFee(Money fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    @Override
    public Money fee(Money subtotal, CheckoutContext ctx) {
        return fixedAmount;
    }
}

class PercentageFee implements FeeStrategy {
    private final BigDecimal rate;

    public PercentageFee(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public Money fee(Money subtotal, CheckoutContext ctx) {
        return subtotal.percent(rate);
    }
}

class TieredFee implements FeeStrategy {

    static class Tier {
        private final BigDecimal upperBound;
        private final BigDecimal rate;

        public Tier(BigDecimal upperBound, BigDecimal rate) {
            this.upperBound = upperBound;
            this.rate = rate;
        }

        public BigDecimal getUpperBound() { return upperBound; }
        public BigDecimal getRate() { return rate; }
    }

    private final List<Tier> tiers;

    public TieredFee(List<Tier> tiers) {
        this.tiers = tiers;
    }

    @Override
    public Money fee(Money subtotal, CheckoutContext ctx) {
        BigDecimal remaining = subtotal.getAmount();
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal previousBound = BigDecimal.ZERO;

        for (Tier tier : tiers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal bracketSize = tier.getUpperBound().subtract(previousBound);
            BigDecimal taxable = remaining.min(bracketSize);
            totalFee = totalFee.add(
                taxable.multiply(tier.getRate())
                       .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            remaining = remaining.subtract(taxable);
            previousBound = tier.getUpperBound();
        }

        return new Money(subtotal.getCurrency(), totalFee);
    }
}

class MixedFee implements FeeStrategy {
    private final PercentageFee percentageStrategy;
    private final Money fixedFloor;

    public MixedFee(BigDecimal rate, Money fixedFloor) {
        this.percentageStrategy = new PercentageFee(rate);
        this.fixedFloor = fixedFloor;
    }

    @Override
    public Money fee(Money subtotal, CheckoutContext ctx) {
        Money percentageFee = percentageStrategy.fee(subtotal, ctx);
        return percentageFee.max(fixedFloor);
    }
}

// --- Strategy Registry ---
class StrategyRegistry {
    private final Map<String, FeeStrategy> registry = new HashMap<>();

    public void register(String country, String merchantPlan, FeeStrategy strategy) {
        String key = country + ":" + merchantPlan;
        registry.put(key, strategy);
    }

    public FeeStrategy resolve(CheckoutContext ctx, FeeStrategy fallback) {
        String key = ctx.getCountry() + ":" + ctx.getMerchantPlan();
        return registry.getOrDefault(key, fallback);
    }
}

// --- Fee Engine ---
class FeeEngine {
    private final FeeStrategy defaultStrategy;
    private final StrategyRegistry registry;

    public FeeEngine(FeeStrategy defaultStrategy, StrategyRegistry registry) {
        this.defaultStrategy = defaultStrategy;
        this.registry = registry;
    }

    public FeeEngine(FeeStrategy defaultStrategy) {
        this(defaultStrategy, null);
    }

    public Money compute(CheckoutContext ctx) {
        FeeStrategy strategy;
        if (registry != null) {
            strategy = registry.resolve(ctx, defaultStrategy);
        } else {
            strategy = defaultStrategy;
        }
        return strategy.fee(ctx.getCartSubtotal(), ctx);
    }
}

// --- Demo ---
public class CheckoutFeeDemo {
    public static void main(String[] args) {
        // Set up registry
        StrategyRegistry registry = new StrategyRegistry();

        // US Basic plan: 2.5% fee
        registry.register("US", "BASIC",
            new PercentageFee(new BigDecimal("2.5")));

        // US Premium plan: tiered fee
        registry.register("US", "PREMIUM",
            new TieredFee(List.of(
                new TieredFee.Tier(new BigDecimal("100"), new BigDecimal("3.0")),
                new TieredFee.Tier(new BigDecimal("500"), new BigDecimal("2.0")),
                new TieredFee.Tier(new BigDecimal("999999"), new BigDecimal("1.0"))
            )));

        // IN Basic plan: mixed fee (2% or minimum 50 INR)
        registry.register("IN", "BASIC",
            new MixedFee(new BigDecimal("2.0"), new Money("INR", 50.0)));

        // Default fallback: fixed $1.00
        FeeStrategy fallback = new FixedFee(new Money("USD", 1.00));
        FeeEngine engine = new FeeEngine(fallback, registry);

        // Test 1: US BASIC, $200 cart
        CheckoutContext ctx1 = new CheckoutContext(
            "US", "USD", "stripe", "BASIC", new Money("USD", 200.0));
        System.out.println("US BASIC $200 -> Fee: " + engine.compute(ctx1));

        // Test 2: US PREMIUM, $600 cart (tiered)
        CheckoutContext ctx2 = new CheckoutContext(
            "US", "USD", "stripe", "PREMIUM", new Money("USD", 600.0));
        System.out.println("US PREMIUM $600 -> Fee: " + engine.compute(ctx2));

        // Test 3: IN BASIC, 1000 INR cart (mixed: 2% = 20, floor = 50, max = 50)
        CheckoutContext ctx3 = new CheckoutContext(
            "IN", "INR", "razorpay", "BASIC", new Money("INR", 1000.0));
        System.out.println("IN BASIC 1000 INR -> Fee: " + engine.compute(ctx3));

        // Test 4: Unknown country/plan -> fallback
        CheckoutContext ctx4 = new CheckoutContext(
            "DE", "EUR", "adyen", "GOLD", new Money("EUR", 300.0));
        System.out.println("DE GOLD 300 EUR -> Fee: " + engine.compute(ctx4));
    }
}
```

</details>

---

### Problem 3: Game AI Steering with Hot-Swap

Implement the Game AI steering domain from the PDF.

**Requirements:**
1. Create an immutable `Vector2` class with `x`, `y`, and methods `add()`, `sub()`, `scale()`, `length()`, `normalized()`. Each returns a new `Vector2`.
2. Create `SteeringState` with fields: `position` (Vector2), `target` (Vector2), `threat` (Vector2), `maxAccel` (double).
3. Create the `SteeringStrategy` interface: `Vector2 steer(SteeringState s)`.
4. Implement `Seek` (accelerate toward target) and `Evade` (accelerate away from threat).
5. Create a `Mover` class with `position` (Vector2), `velocity` (Vector2), and a `SteeringStrategy`. It has `update(SteeringState state, double dt)` and `setStrategy(SteeringStrategy s)`.
6. In `main`, simulate a game loop: a mover starts by seeking a target, then after a few frames, hot-swaps to evading a threat. Print position each frame.

<details><summary>Solution</summary>

```java
// --- Immutable Vector2 ---
final class Vector2 {
    private final double x;
    private final double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 sub(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public Vector2 scale(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalized() {
        double len = length();
        if (len == 0) return new Vector2(0, 0);
        return new Vector2(x / len, y / len);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}

// --- Steering State ---
class SteeringState {
    private final Vector2 position;
    private final Vector2 target;
    private final Vector2 threat;
    private final double maxAccel;

    public SteeringState(Vector2 position, Vector2 target,
                         Vector2 threat, double maxAccel) {
        this.position = position;
        this.target = target;
        this.threat = threat;
        this.maxAccel = maxAccel;
    }

    public Vector2 getPosition() { return position; }
    public Vector2 getTarget() { return target; }
    public Vector2 getThreat() { return threat; }
    public double getMaxAccel() { return maxAccel; }
}

// --- Steering Strategy Interface ---
interface SteeringStrategy {
    Vector2 steer(SteeringState s);
}

// --- Concrete Strategies ---
class Seek implements SteeringStrategy {
    @Override
    public Vector2 steer(SteeringState s) {
        // Direction from current position toward target
        Vector2 direction = s.getTarget().sub(s.getPosition());
        return direction.normalized().scale(s.getMaxAccel());
    }
}

class Evade implements SteeringStrategy {
    @Override
    public Vector2 steer(SteeringState s) {
        // Direction from threat away (position - threat)
        Vector2 direction = s.getPosition().sub(s.getThreat());
        return direction.normalized().scale(s.getMaxAccel());
    }
}

// --- Mover (Context) ---
class Mover {
    private Vector2 position;
    private Vector2 velocity;
    private SteeringStrategy strategy;

    public Mover(Vector2 position, Vector2 velocity, SteeringStrategy strategy) {
        this.position = position;
        this.velocity = velocity;
        this.strategy = strategy;
    }

    public void setStrategy(SteeringStrategy strategy) {
        this.strategy = strategy;
    }

    public void update(SteeringState state, double dt) {
        Vector2 acceleration = strategy.steer(state);
        // velocity += acceleration * dt
        velocity = velocity.add(acceleration.scale(dt));
        // position += velocity * dt
        position = position.add(velocity.scale(dt));
    }

    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
}

// --- Demo ---
public class GameSteeringDemo {
    public static void main(String[] args) {
        Vector2 startPos = new Vector2(0, 0);
        Vector2 startVel = new Vector2(0, 0);
        Vector2 target = new Vector2(10, 10);
        Vector2 threat = new Vector2(5, 5);
        double maxAccel = 5.0;
        double dt = 0.5;

        Mover mover = new Mover(startPos, startVel, new Seek());

        System.out.println("=== Phase 1: Seeking target at " + target + " ===");
        for (int frame = 0; frame < 5; frame++) {
            SteeringState state = new SteeringState(
                mover.getPosition(), target, threat, maxAccel);
            mover.update(state, dt);
            System.out.println("Frame " + frame
                + " | Position: " + mover.getPosition()
                + " | Velocity: " + mover.getVelocity());
        }

        // Hot-swap to Evade
        System.out.println("\n=== Hot-swap to Evade (threat at " + threat + ") ===");
        mover.setStrategy(new Evade());

        for (int frame = 5; frame < 10; frame++) {
            SteeringState state = new SteeringState(
                mover.getPosition(), target, threat, maxAccel);
            mover.update(state, dt);
            System.out.println("Frame " + frame
                + " | Position: " + mover.getPosition()
                + " | Velocity: " + mover.getVelocity());
        }

        // Hot-swap back to Seek
        System.out.println("\n=== Hot-swap back to Seek ===");
        mover.setStrategy(new Seek());

        for (int frame = 10; frame < 15; frame++) {
            SteeringState state = new SteeringState(
                mover.getPosition(), target, threat, maxAccel);
            mover.update(state, dt);
            System.out.println("Frame " + frame
                + " | Position: " + mover.getPosition()
                + " | Velocity: " + mover.getVelocity());
        }
    }
}
```

</details>
