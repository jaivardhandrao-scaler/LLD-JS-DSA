# Builder -- Teacher PDF Practice

> All questions are based on the **DesignPattern_Builder.pdf** examples (Order class, GameConfig, 5-step evolution).

---

## Viva Questions

**Q1: What is the telescoping constructor problem, and how does the PDF's Order class demonstrate it?**

The `Order` class has fields: `customerId`, `priority`, `giftWrap`, `expressDelivery`. To support every combination of optional parameters, you'd need constructors like `Order(int)`, `Order(int, int)`, `Order(int, int, boolean)`, `Order(int, int, boolean, boolean)`, etc. The number of constructors explodes combinatorially. Callers end up writing `new Order(42, 3, false, true)` -- you can't tell which `boolean` means what. Builder solves this by replacing positional arguments with named fluent setters: `new Order.Builder(42).priority(3).expressDelivery(true).build()`.

---

**Q2: Walk through the structure of the PDF's Builder solution for Order. What are the four key pieces?**

1. **Static inner `Builder` class** inside `Order` -- holds the same fields as `Order`.
2. **Fluent setters on Builder** -- each setter (e.g., `priority(int p)`) sets the field and returns `this`, enabling method chaining.
3. **`build()` method** on Builder -- creates and returns a new `Order` by passing the Builder to the private constructor.
4. **Private `Order(Builder b)` constructor** -- copies every field from the Builder into the `Order` instance. Because it's private, the only way to create an `Order` is through the Builder.

---

**Q3: The PDF shows two approaches to enforce required fields in a Builder. What are they?**

**Approach 1 -- Validate in `build()`:** Check required fields inside `build()` and throw `IllegalStateException` if they are missing. Example: `if (customerId == 0) throw new IllegalStateException("customerId is required");`. Downside: the error is at runtime, not compile time.

**Approach 2 -- Require in Builder constructor:** Make the required field a parameter of the Builder's constructor and validate with `Objects.requireNonNull()`. Example: `public Builder(int customerId) { this.customerId = Objects.requireNonNull(customerId); }`. This forces callers to provide the field at Builder creation time, making it impossible to forget.

---

**Q4: Explain the GameConfig example from the PDF. Why is it more complex than Order?**

`GameConfig` has a `name` (String) and a `List<String> rules`. The Builder provides `addRule(String rule)` to add a single rule and `addAllRules(List<String> rules)` to add multiple rules at once. The critical detail is in the `GameConfig` constructor: it uses `List.copyOf(b.rules)` to create a **defensive copy** of the rules list. This means the caller cannot modify the internal list after construction -- the `GameConfig` object is truly immutable. If you just assigned `this.rules = b.rules`, the caller could still mutate the list through the Builder's reference.

---

**Q5: What is a defensive copy and why does the PDF use `List.copyOf()` in the GameConfig constructor?**

A defensive copy creates a new, independent copy of a mutable data structure so the original and the copy are decoupled. `List.copyOf()` returns an unmodifiable list containing the same elements. The PDF uses it in `GameConfig(Builder b)` because if you wrote `this.rules = b.rules`, someone who still has a reference to the Builder's internal `ArrayList` could call `add()` on it and mutate the supposedly immutable `GameConfig`. `List.copyOf()` breaks that link and also makes the returned list unmodifiable.

---

**Q6: The PDF mentions "copy-builder" or `toBuilder()`. What is it and when is it useful?**

A `toBuilder()` method on an immutable object returns a new Builder pre-populated with all the current object's field values. You can then modify some fields and call `build()` to get a new instance. This is useful when you have an existing immutable object and want a slightly modified copy -- instead of manually re-specifying every field, you call `existingOrder.toBuilder().priority(5).build()`. The PDF lists this under practical considerations for Builder.

---

**Q7: Why does the PDF say Builder is especially suited for immutable classes?**

Immutable classes have no setters -- all fields are set at construction time. Without Builder, you'd need a constructor with every field as a parameter (telescoping constructors). Builder lets you set fields one at a time using fluent setters on a mutable Builder object, and then the `build()` method freezes everything into an immutable instance. The Builder is the only mutable "staging area"; the final product is immutable.

---

**Q8: The PDF warns about Lombok's `@Builder`. What is the concern?**

Lombok's `@Builder` annotation auto-generates builder code, which is convenient but can hide what's actually happening. The PDF's concern is that students should understand the mechanics of the Builder pattern (inner class, fluent setters, `build()`, private constructor, defensive copies, required field enforcement) before relying on code generation. Lombok does not enforce required fields or defensive copies by default, so blindly using it can lead to bugs in production code.

---

## MCQ Quiz (15 Questions)

**Q1.** The PDF's `Order` class has which fields?

a) `name`, `email`, `phone`, `address`
b) `customerId`, `priority`, `giftWrap`, `expressDelivery`
c) `orderId`, `items`, `total`, `discount`
d) `customerId`, `items`, `shippingAddress`

<details><summary>Answer</summary>b) `customerId` (int), `priority` (int), `giftWrap` (boolean), `expressDelivery` (boolean).</details>

---

**Q2.** In the PDF's Builder solution, the `Builder` class is:

a) A separate top-level class
b) A `static` inner class inside `Order`
c) An abstract class
d) An interface

<details><summary>Answer</summary>b) A `public static` inner class inside `Order`. This gives it access to `Order`'s private constructor.</details>

---

**Q3.** What do fluent setters on the Builder return?

a) `void`
b) The `Order` object
c) `this` (the Builder itself)
d) A new Builder instance

<details><summary>Answer</summary>c) `this` -- the same Builder instance. This enables method chaining like `.priority(3).giftWrap(true).build()`.</details>

---

**Q4.** The `Order` constructor in the PDF's Builder solution is:

a) `public Order(int customerId, int priority, boolean giftWrap, boolean expressDelivery)`
b) `public Order(Builder b)`
c) `private Order(Builder b)`
d) `protected Order()`

<details><summary>Answer</summary>c) `private Order(Builder b)`. Private ensures the only way to create an Order is through the Builder's `build()` method.</details>

---

**Q5.** To enforce that `customerId` is required using the Builder constructor approach, the PDF does:

a) Checks `customerId != 0` inside `build()`
b) Makes `customerId` a parameter of the Builder's constructor and uses `Objects.requireNonNull()`
c) Makes `customerId` public
d) Uses a default value of -1

<details><summary>Answer</summary>b) The Builder constructor takes `customerId` as a required parameter and validates it with `Objects.requireNonNull()`, ensuring it is provided at Builder creation time.</details>

---

**Q6.** The alternative approach to enforce required fields validates inside which method?

a) The `Order` constructor
b) The fluent setter
c) The `build()` method, throwing `IllegalStateException`
d) The `main()` method

<details><summary>Answer</summary>c) `build()`. Before creating the Order, `build()` checks required fields and throws `IllegalStateException` if any are missing.</details>

---

**Q7.** The PDF's `GameConfig` class has which fields?

a) `name` and `level`
b) `name` and `List<String> rules`
c) `players` and `Map<String, Integer> scores`
d) `id`, `name`, `difficulty`

<details><summary>Answer</summary>b) `name` (String) and `rules` (List of String). The Builder provides `addRule()` and `addAllRules()` for populating the rules list.</details>

---

**Q8.** In the GameConfig Builder, what method adds a single rule?

a) `setRule(String rule)`
b) `rule(String rule)`
c) `addRule(String rule)`
d) `withRule(String rule)`

<details><summary>Answer</summary>c) `addRule(String rule)`. The PDF also shows `addAllRules(List<String> rules)` for adding multiple rules at once.</details>

---

**Q9.** Why does the `GameConfig` constructor use `List.copyOf(b.rules)` instead of `this.rules = b.rules`?

a) `List.copyOf()` is faster
b) It creates a defensive copy so the caller cannot mutate the GameConfig's internal list
c) `b.rules` is null
d) It converts the list to an array

<details><summary>Answer</summary>b) Defensive copy. Without it, someone holding a reference to the Builder's internal ArrayList could add or remove rules after the GameConfig is built, violating immutability.</details>

---

**Q10.** What does `List.copyOf()` return?

a) A mutable `ArrayList`
b) An unmodifiable list containing the same elements
c) A `LinkedList`
d) A synchronized list

<details><summary>Answer</summary>b) An unmodifiable list. Calling `add()`, `remove()`, or `set()` on it throws `UnsupportedOperationException`.</details>

---

**Q11.** According to the PDF's Quick Recap, telescoping constructors are bad because:

a) They are slow at runtime
b) They create too many objects
c) Positional arguments are unreadable and the number of constructors grows combinatorially
d) They violate the Singleton pattern

<details><summary>Answer</summary>c) The PDF says telescoping constructors are bad because you end up with many constructor overloads and callers can't tell which positional argument means what.</details>

---

**Q12.** The PDF says Builder "separates _____ from _____."

a) interface from implementation
b) construction from representation
c) state from behavior
d) abstraction from encapsulation

<details><summary>Answer</summary>b) Construction from representation. The Builder handles the step-by-step construction process; the final product (Order, GameConfig) is the representation.</details>

---

**Q13.** Where does the PDF say default values should be set in a Builder?

a) In the `Order` class fields
b) In the Builder's field declarations (defaults in Builder)
c) In the `build()` method
d) In the client code

<details><summary>Answer</summary>b) Defaults in the Builder. For example, `private int priority = 0;` and `private boolean giftWrap = false;` in the Builder class, so callers only set what they want to override.</details>

---

**Q14.** Which of the following is NOT an interview question listed at the end of the PDF?

a) What problem does Builder solve?
b) How do you enforce required fields in a Builder?
c) Why is Builder used with immutable objects?
d) How does Builder differ from Abstract Factory?

<details><summary>Answer</summary>d) The PDF's interview questions are: (1) What problem does Builder solve? (2) How enforce required fields? (3) Why Builder with immutable objects? (4) Compare Builder vs telescoping constructors. Abstract Factory comparison is not listed.</details>

---

**Q15.** In the PDF, where should validation logic (e.g., checking that priority is non-negative) be placed?

a) In the fluent setter
b) In the `build()` method
c) In the `Order` constructor
d) In the client code

<details><summary>Answer</summary>b) In the `build()` method. The PDF lists "validation in `build()`" as a practical consideration, centralizing all checks before the immutable object is created.</details>

---

## Self-Scoring

| Score | Level |
|-------|-------|
| 14-15 | Exam ready on Builder |
| 11-13 | Good, review GameConfig and defensive copies |
| 8-10 | Re-read Steps 3-5 of the PDF |
| Below 8 | Go back to the PDF |

---

## Coding Problems

### Problem 1: Implement the Order Builder from Memory

Write the complete `Order` class with a static inner `Builder` class exactly as described in the PDF. Include:
- Fields: `customerId`, `priority`, `giftWrap`, `expressDelivery`
- Fluent setters returning `this`
- Private `Order(Builder b)` constructor
- `build()` method
- Enforce `customerId` as required (use the Builder constructor approach with `Objects.requireNonNull`)
- A `main()` that creates an Order with `customerId=42`, `priority=3`, `expressDelivery=true`, default `giftWrap`

<details><summary>Solution</summary>

```java
import java.util.Objects;

public final class Order {
    private final int customerId;
    private final int priority;
    private final boolean giftWrap;
    private final boolean expressDelivery;

    private Order(Builder b) {
        this.customerId = b.customerId;
        this.priority = b.priority;
        this.giftWrap = b.giftWrap;
        this.expressDelivery = b.expressDelivery;
    }

    public int getCustomerId()      { return customerId; }
    public int getPriority()        { return priority; }
    public boolean isGiftWrap()     { return giftWrap; }
    public boolean isExpressDelivery() { return expressDelivery; }

    @Override
    public String toString() {
        return "Order{customerId=" + customerId
             + ", priority=" + priority
             + ", giftWrap=" + giftWrap
             + ", expressDelivery=" + expressDelivery + "}";
    }

    public static class Builder {
        private final int customerId;       // required
        private int priority = 0;           // default
        private boolean giftWrap = false;   // default
        private boolean expressDelivery = false; // default

        public Builder(int customerId) {
            this.customerId = Objects.requireNonNull(customerId);
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder giftWrap(boolean giftWrap) {
            this.giftWrap = giftWrap;
            return this;
        }

        public Builder expressDelivery(boolean expressDelivery) {
            this.expressDelivery = expressDelivery;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

    public static void main(String[] args) {
        Order order = new Order.Builder(42)
                .priority(3)
                .expressDelivery(true)
                .build();

        System.out.println(order);
        // Output: Order{customerId=42, priority=3, giftWrap=false, expressDelivery=true}
    }
}
```

**Key points:**
- `Builder` is `public static` so it can be created without an `Order` instance.
- `customerId` is `final` in the Builder and required in the constructor.
- Optional fields have defaults in the Builder.
- `Order` constructor is `private` -- only `build()` can call it.
- Fluent setters return `this` for chaining.
</details>

---

### Problem 2: Implement GameConfig with Defensive Copies

Write the `GameConfig` class from the PDF with a Builder that supports:
- `name` (required, set in Builder constructor)
- `List<String> rules` (populated via `addRule()` and `addAllRules()`)
- Defensive copy using `List.copyOf()` in the GameConfig constructor
- A `main()` that builds a GameConfig, then proves the internal list cannot be mutated externally

<details><summary>Solution</summary>

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GameConfig {
    private final String name;
    private final List<String> rules;

    private GameConfig(Builder b) {
        this.name = b.name;
        this.rules = List.copyOf(b.rules);  // defensive copy -- unmodifiable
    }

    public String getName()        { return name; }
    public List<String> getRules() { return rules; }  // already unmodifiable

    @Override
    public String toString() {
        return "GameConfig{name='" + name + "', rules=" + rules + "}";
    }

    public static class Builder {
        private final String name;                     // required
        private final List<String> rules = new ArrayList<>();  // mutable during building

        public Builder(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public Builder addRule(String rule) {
            rules.add(rule);
            return this;
        }

        public Builder addAllRules(List<String> rules) {
            this.rules.addAll(rules);
            return this;
        }

        public GameConfig build() {
            return new GameConfig(this);
        }
    }

    public static void main(String[] args) {
        GameConfig.Builder builder = new GameConfig.Builder("Chess");
        builder.addRule("No castling after check")
               .addRule("En passant allowed");

        GameConfig config = builder.build();
        System.out.println(config);
        // GameConfig{name='Chess', rules=[No castling after check, En passant allowed]}

        // Prove defensive copy works:
        // 1. Mutating the builder's list after build() does NOT affect config
        builder.addRule("Time limit 5 min");
        System.out.println("After mutating builder: " + config.getRules());
        // Still: [No castling after check, En passant allowed]

        // 2. Trying to mutate config's list directly throws exception
        try {
            config.getRules().add("Illegal move!");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot mutate: " + e.getClass().getSimpleName());
        }
    }
}
```

**Key points:**
- `List.copyOf(b.rules)` creates a new, unmodifiable list -- decoupled from the Builder's ArrayList.
- Adding to the Builder after `build()` does not affect the already-built GameConfig.
- Calling `add()` on the returned list throws `UnsupportedOperationException`.
</details>

---

### Problem 3: Add toBuilder() and Validation to Order

Extend the Order Builder from Problem 1 with two features from the PDF's practical considerations:
1. **Validation in `build()`:** priority must be >= 0 and <= 10, otherwise throw `IllegalStateException`.
2. **`toBuilder()` method on Order:** returns a new Builder pre-populated with the current Order's values, so you can create a modified copy.

Write a `main()` that:
- Builds an Order with customerId=7, priority=5, giftWrap=true
- Uses `toBuilder()` to create a copy with expressDelivery=true and priority=8
- Demonstrates that invalid priority (e.g., 15) throws IllegalStateException

<details><summary>Solution</summary>

```java
import java.util.Objects;

public final class Order {
    private final int customerId;
    private final int priority;
    private final boolean giftWrap;
    private final boolean expressDelivery;

    private Order(Builder b) {
        this.customerId = b.customerId;
        this.priority = b.priority;
        this.giftWrap = b.giftWrap;
        this.expressDelivery = b.expressDelivery;
    }

    public int getCustomerId()         { return customerId; }
    public int getPriority()           { return priority; }
    public boolean isGiftWrap()        { return giftWrap; }
    public boolean isExpressDelivery() { return expressDelivery; }

    // toBuilder -- returns a Builder pre-populated with this Order's values
    public Builder toBuilder() {
        return new Builder(this.customerId)
                .priority(this.priority)
                .giftWrap(this.giftWrap)
                .expressDelivery(this.expressDelivery);
    }

    @Override
    public String toString() {
        return "Order{customerId=" + customerId
             + ", priority=" + priority
             + ", giftWrap=" + giftWrap
             + ", expressDelivery=" + expressDelivery + "}";
    }

    public static class Builder {
        private final int customerId;
        private int priority = 0;
        private boolean giftWrap = false;
        private boolean expressDelivery = false;

        public Builder(int customerId) {
            this.customerId = Objects.requireNonNull(customerId);
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder giftWrap(boolean giftWrap) {
            this.giftWrap = giftWrap;
            return this;
        }

        public Builder expressDelivery(boolean expressDelivery) {
            this.expressDelivery = expressDelivery;
            return this;
        }

        public Order build() {
            // Validation in build()
            if (priority < 0 || priority > 10) {
                throw new IllegalStateException(
                    "priority must be between 0 and 10, got: " + priority);
            }
            return new Order(this);
        }
    }

    public static void main(String[] args) {
        // 1. Build original order
        Order original = new Order.Builder(7)
                .priority(5)
                .giftWrap(true)
                .build();
        System.out.println("Original: " + original);
        // Order{customerId=7, priority=5, giftWrap=true, expressDelivery=false}

        // 2. Use toBuilder() to create a modified copy
        Order modified = original.toBuilder()
                .expressDelivery(true)
                .priority(8)
                .build();
        System.out.println("Modified: " + modified);
        // Order{customerId=7, priority=8, giftWrap=true, expressDelivery=true}

        // 3. Original is unchanged (immutable)
        System.out.println("Original unchanged: " + original);
        // Order{customerId=7, priority=5, giftWrap=true, expressDelivery=false}

        // 4. Validation -- invalid priority throws IllegalStateException
        try {
            new Order.Builder(99).priority(15).build();
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
            // Caught: priority must be between 0 and 10, got: 15
        }
    }
}
```

**Key points:**
- `toBuilder()` creates a new mutable Builder from an immutable Order, enabling the "modified copy" idiom.
- Validation is centralized in `build()` -- the single point where all constraints are checked before the immutable object is created.
- The original Order is never modified (immutability preserved).
</details>
