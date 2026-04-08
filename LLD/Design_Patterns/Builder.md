# Builder Design Pattern

**Category:** Creational
**Intent:** Simplify the creation of complex immutable objects by separating construction from representation.
**Use cases:** Classes with many parameters (some optional), configuration objects, constructing immutable aggregates.

---

## The Problem: Telescoping Constructors

When a class has many fields (8-10+), constructors become unreadable.

### BAD: Telescoping Constructors

```java
public class Order {
    private final String customerId;
    private final int priority;
    private final boolean giftWrap;
    private final boolean expressDelivery;

    // Which boolean is which? Caller has no idea.
    public Order(String customerId, int priority,
                 boolean giftWrap, boolean expressDelivery) {
        this.customerId = customerId;
        this.priority = priority;
        this.giftWrap = giftWrap;
        this.expressDelivery = expressDelivery;
    }
}

// Usage -- confusing: what does true, false mean here?
Order order = new Order("C101", 2, true, false);
```

**Problems:**
- **Unreadable:** `true, false` -- which is giftWrap, which is expressDelivery?
- **Rigid:** Adding a new field means changing every constructor call
- **Error-prone:** Easy to swap boolean arguments and the compiler won't catch it

### BAD: Multiple Overloaded Constructors

```java
public Order(String customerId) { ... }
public Order(String customerId, int priority) { ... }
public Order(String customerId, int priority, boolean giftWrap) { ... }
public Order(String customerId, int priority, boolean giftWrap, boolean express) { ... }
```

This "telescoping" pattern gets worse with every new field. 10 fields = 10+ constructors.

---

## The Solution: Builder Pattern

### GOOD: Builder with Fluent API

```java
public class Order {
    private final String customerId;
    private final int priority;
    private final boolean giftWrap;
    private final boolean expressDelivery;

    private Order(Builder b) {
        this.customerId = b.customerId;
        this.priority = b.priority;
        this.giftWrap = b.giftWrap;
        this.expressDelivery = b.expressDelivery;
    }

    public static class Builder {
        private String customerId;
        private int priority = 1;            // default
        private boolean giftWrap = false;     // default
        private boolean expressDelivery = false; // default

        public Builder customerId(String id) {
            this.customerId = id; return this;
        }
        public Builder priority(int p) {
            this.priority = p; return this;
        }
        public Builder giftWrap(boolean g) {
            this.giftWrap = g; return this;
        }
        public Builder expressDelivery(boolean e) {
            this.expressDelivery = e; return this;
        }

        public Order build() {
            if (customerId == null)
                throw new IllegalStateException("customerId required");
            return new Order(this);
        }
    }
}
```

**Usage -- crystal clear:**
```java
Order order = new Order.Builder()
    .customerId("C101")
    .priority(2)
    .giftWrap(true)
    .build();
```

**Why this is better:**
- Every field is **named** at the call site -- no ambiguity
- Optional fields have **defaults** -- set only what you need
- The object is **immutable** (all fields are `final`)
- **Validation** happens in `build()` before the object exists

---

## Key Design Decisions

### 1. Enforcing Required Fields

**Option A: Validate in `build()`**
```java
public Order build() {
    if (customerId == null)
        throw new IllegalStateException("customerId required");
    return new Order(this);
}
```

**Option B: Require in Builder's constructor**
```java
public static class Builder {
    private final String customerId; // required

    public Builder(String customerId) {
        this.customerId = Objects.requireNonNull(customerId);
    }
    // ... optional setters
}

// Usage
Order order = new Order.Builder("C101")  // must provide customerId
    .priority(2)
    .build();
```

**Exam Tip:** Option B is stronger -- the compiler forces the required field. Option A gives a runtime error. Mention BOTH in a viva.

### 2. Immutability

- The outer class constructor is **`private`** -- nobody can bypass the Builder
- All fields are **`final`** -- set once, never changed
- Collections should use **defensive copies**: `List.copyOf(b.rules)`

### 3. Fluent API

Each setter returns `this` (the Builder itself), enabling method chaining:
```java
.customerId("C101").priority(2).giftWrap(true).build();
```

---

## Complex Example: GameConfig with Collections

```java
import java.util.*;

public final class GameConfig {
    private final String name;
    private final List<String> rules;

    private GameConfig(Builder b) {
        this.name = b.name;
        this.rules = List.copyOf(b.rules); // defensive copy -- immutable
    }

    public String name() { return name; }
    public List<String> rules() { return rules; }

    public static class Builder {
        private String name;
        private List<String> rules = new ArrayList<>();

        public Builder name(String n) { this.name = n; return this; }
        public Builder addRule(String r) { this.rules.add(r); return this; }
        public Builder addAllRules(List<String> rs) {
            this.rules.addAll(rs); return this;
        }

        public GameConfig build() {
            if (name == null)
                throw new IllegalStateException("Name required");
            return new GameConfig(this);
        }
    }
}
```

**Usage:**
```java
GameConfig config = new GameConfig.Builder()
    .name("Battle Royale")
    .addRule("No cheating")
    .addRule("Time limit: 10 min")
    .build();
```

**Key points:**
- `List.copyOf()` makes a **defensive copy** -- the caller can't modify the internal list after build
- `addRule()` accumulates items step by step
- The built `GameConfig` is fully immutable

---

## Practical Considerations

| Aspect | Guidance |
|--------|----------|
| **When to use** | Classes with 4+ fields, especially if some are optional |
| **Defaults** | Set sensible defaults in the Builder fields |
| **Validation** | Always validate in `build()`, not in setters |
| **Copy-builder** | A `toBuilder()` method lets you create a modified copy |
| **Lombok** | `@Builder` auto-generates builders, but may hide validation logic |
| **Thread safety** | Builders are NOT thread-safe by default. Don't share a Builder across threads |

---

## SOLID Connection

| Principle | How Builder Relates |
|-----------|---------------------|
| **SRP** | Outer class holds data. Builder handles construction. Separate responsibilities |
| **OCP** | New optional fields can be added to the Builder without breaking existing callers (they just don't call the new setter) |
| **DIP** | Client depends on the Builder API, not on constructor internals |

---

## Big Picture

- Builder is a **Creational** pattern focused on **step-by-step construction**
- Often used with **immutable classes** (all fields `final`, no setters)
- Contrast with **Factory**: Factory decides WHICH class to create; Builder decides HOW to configure a single class
- Contrast with **Prototype**: Prototype copies an existing object; Builder constructs from scratch

---

## Exam Tips (Quick Recall)

1. Builder solves the **telescoping constructor** problem
2. Each setter returns `this` for **fluent chaining**
3. Outer class constructor is **private** -- forces use of Builder
4. Validate required fields in **`build()`** or Builder constructor
5. Use **`List.copyOf()`** for defensive copies of collections
6. Builder creates **immutable** objects (all fields `final`)
7. Builder is NOT thread-safe -- don't share across threads

---

## Viva Questions

**Q1: What problem does the Builder pattern solve?**
It solves the telescoping constructor problem -- when a class has many parameters (some optional), constructors become unreadable and error-prone. Builder lets you set fields by name, step by step, with defaults for optional fields.

**Q2: How can required fields be enforced in a Builder?**
Two ways: (1) Require them in the Builder's constructor: `new Builder(requiredField)`. (2) Validate in `build()` and throw an exception if missing. Option 1 is compile-time safe; option 2 is runtime.

**Q3: Why is Builder often used with immutable objects?**
Because immutable objects need all fields set at construction time (no setters). Builder accumulates the values step by step, then creates the fully-initialized immutable object in one shot via `build()`.

**Q4: Compare Builder with telescoping constructors.**
Telescoping constructors: unreadable (what does `true, false, 3` mean?), rigid (adding a field changes all call sites), error-prone (swapped arguments). Builder: named fields, optional defaults, validation, readable call sites.

**Q5: What is a fluent API and how does Builder use it?**
A fluent API returns `this` from setter methods so calls can be chained: `.name("x").priority(2).build()`. Builder uses this to allow step-by-step construction in a single readable expression.

**Q6: Why is the outer class constructor private in the Builder pattern?**
To force all construction through the Builder. This ensures validation always runs and prevents someone from bypassing the Builder and creating an invalid object directly.

**Q7: What is a defensive copy and why is it important in Builder?**
A defensive copy (e.g., `List.copyOf(b.rules)`) creates an independent copy of a collection. Without it, the caller could modify the internal list after building, breaking immutability.

**Q8: Can you reuse a Builder to create multiple objects?**
Yes, but be careful. If the Builder has mutable state (like a list), the second `build()` will include state from the first. Best practice: either document this or create a new Builder each time.

**Q9: How is Builder different from Factory?**
Factory decides WHICH class to instantiate (returns different subtypes). Builder decides HOW to configure a single class (step-by-step construction with optional fields). Factory varies the product type; Builder varies the product configuration.

**Q10: What is `toBuilder()` and when would you use it?**
A method on the built object that returns a pre-filled Builder, allowing you to create a modified copy: `config.toBuilder().name("NewName").build()`. Useful when you want a slightly different version of an existing immutable object.

---

## MCQ Quiz

**1. What problem does the Builder pattern primarily solve?**
a) Creating families of related objects
b) Telescoping constructor / many-parameter constructor confusion
c) Ensuring only one instance exists
d) Cloning existing objects

<details><summary>Answer</summary>b) Telescoping constructor / many-parameter constructor confusion</details>

**2. What does each setter method in a Builder return?**
a) void
b) The built object
c) `this` (the Builder itself)
d) A new Builder instance

<details><summary>Answer</summary>c) `this` (the Builder itself) -- enables fluent chaining</details>

**3. Why is the outer class constructor typically `private` in the Builder pattern?**
a) To prevent inheritance
b) To force construction through the Builder
c) To make the class abstract
d) To enable serialization

<details><summary>Answer</summary>b) To force construction through the Builder</details>

**4. Where should required field validation happen?**
a) In each setter method
b) In the outer class constructor
c) In the `build()` method
d) In the outer class's `toString()`

<details><summary>Answer</summary>c) In the `build()` method (or the Builder's constructor for compile-time enforcement)</details>

**5. What is `List.copyOf()` used for in a Builder?**
a) To sort the list
b) To make a defensive copy preserving immutability
c) To convert the list to an array
d) To remove duplicates

<details><summary>Answer</summary>b) To make a defensive copy preserving immutability</details>

**6. Which statement about Builder is FALSE?**
a) Builder is a creational pattern
b) Builder is inherently thread-safe
c) Builder helps create immutable objects
d) Builder uses fluent setters

<details><summary>Answer</summary>b) Builder is inherently thread-safe -- Builders are NOT thread-safe by default</details>

**7. How is Builder different from Factory Method?**
a) Builder varies the product type; Factory varies configuration
b) Factory varies the product type; Builder varies the configuration of a single type
c) They are the same pattern
d) Builder is structural; Factory is creational

<details><summary>Answer</summary>b) Factory varies the product type; Builder varies the configuration of a single type</details>

**8. What happens if you call `build()` without setting a required field (validated in build)?**
a) Returns null
b) Throws an exception
c) Uses a default value
d) Compiler error

<details><summary>Answer</summary>b) Throws an exception (e.g., IllegalStateException)</details>

**9. In the Builder pattern, where are default values for optional fields typically set?**
a) In the outer class
b) In the Builder's field declarations
c) In the `build()` method
d) By the caller

<details><summary>Answer</summary>b) In the Builder's field declarations (e.g., `private int priority = 1;`)</details>

**10. What is a "copy-builder" or `toBuilder()`?**
a) A builder that clones another builder
b) A method on the built object that returns a pre-filled builder for creating modified copies
c) A static factory method
d) A constructor that takes another object

<details><summary>Answer</summary>b) A method on the built object that returns a pre-filled builder for creating modified copies</details>

**11. Which SOLID principle does Builder most directly support?**
a) Liskov Substitution
b) Single Responsibility (construction logic separated from the data class)
c) Interface Segregation
d) Dependency Inversion

<details><summary>Answer</summary>b) Single Responsibility -- the Builder handles construction, the outer class holds data</details>

**12. Why is `new Order("C101", 2, true, false)` problematic?**
a) Too many arguments
b) Boolean arguments are ambiguous -- which is giftWrap, which is expressDelivery?
c) Strings can't be the first parameter
d) The priority should be a String

<details><summary>Answer</summary>b) Boolean arguments are ambiguous -- no way to tell which is which at the call site</details>

**13. Can a Builder pattern be used for mutable objects?**
a) No, only for immutable objects
b) Yes, but it's most beneficial for immutable objects
c) Yes, and it's equally useful for both
d) Only with the Factory pattern

<details><summary>Answer</summary>b) Yes, but it's most beneficial for immutable objects where all fields must be set at construction</details>

**14. What access modifier should the Builder class itself have?**
a) private
b) protected
c) public static (inner class)
d) package-private

<details><summary>Answer</summary>c) public static -- it must be accessible without an instance of the outer class</details>

**15. What does `Objects.requireNonNull(customerId)` do?**
a) Returns null if customerId is null
b) Throws NullPointerException if customerId is null
c) Creates a default value
d) Logs a warning

<details><summary>Answer</summary>b) Throws NullPointerException if customerId is null</details>

**16. What library annotation auto-generates a Builder in Java?**
a) `@Singleton`
b) `@Builder` (Lombok)
c) `@Factory`
d) `@Immutable`

<details><summary>Answer</summary>b) `@Builder` from Project Lombok</details>

**17. If a Builder accumulates items in a list with `addRule()`, what happens on the second `build()` call?**
a) The list is reset
b) The second object includes rules from both build calls
c) An exception is thrown
d) The first object is modified

<details><summary>Answer</summary>b) The second object includes rules from both build calls (unless the Builder is reset)</details>

**18. Which is the correct order of operations in the Builder pattern?**
a) Create object -> set fields -> validate
b) Create Builder -> set fields -> validate in build() -> create object
c) Validate -> create Builder -> set fields -> create object
d) Create object -> validate -> set fields

<details><summary>Answer</summary>b) Create Builder -> set fields -> validate in build() -> create object</details>

**19. Why use Builder instead of setters on the object itself?**
a) Setters are faster
b) Builder creates the object in a valid, immutable state; setters allow partially constructed mutable objects
c) Builder uses less memory
d) Setters can't handle multiple fields

<details><summary>Answer</summary>b) Builder creates the object in a valid, immutable state; setters allow partially constructed mutable objects</details>

**20. Which is NOT a characteristic of a well-designed Builder?**
a) Fluent setter methods returning `this`
b) Validation in the `build()` method
c) A public constructor on the outer class
d) Sensible defaults for optional fields

<details><summary>Answer</summary>c) A public constructor on the outer class -- it should be private to force use of the Builder</details>

### Scoring
- **18-20:** Builder mastered. Move on.
- **14-17:** Solid understanding. Review defensive copies.
- **10-13:** Revisit the required vs optional field enforcement.
- **Below 10:** Re-read from the beginning.

---

## Coding Exam Questions

### Problem 1: Spot the Builder Violations

```java
public class HttpRequest {
    public String method;
    public String url;
    public String body;

    public static class Builder {
        public String method;
        public String url;
        public String body;

        public void method(String m) { this.method = m; }
        public void url(String u) { this.url = u; }
        public void body(String b) { this.body = b; }

        public HttpRequest build() {
            HttpRequest r = new HttpRequest();
            r.method = method;
            r.url = url;
            r.body = body;
            return r;
        }
    }
}
```

**Task:** Identify all violations of proper Builder pattern and fix them.

<details><summary>Solution</summary>

**Issues:**
1. Outer class fields are `public` and not `final` -- object is mutable
2. Outer class has default (public) constructor -- can bypass Builder
3. Setter methods return `void` -- no fluent chaining
4. No validation in `build()` (method and url should be required)
5. Builder fields are `public` -- should be `private`

**Fixed:**
```java
public final class HttpRequest {
    private final String method;
    private final String url;
    private final String body;

    private HttpRequest(Builder b) {
        this.method = b.method;
        this.url = b.url;
        this.body = b.body;
    }

    public String method() { return method; }
    public String url() { return url; }
    public String body() { return body; }

    public static class Builder {
        private String method;
        private String url;
        private String body;

        public Builder method(String m) { this.method = m; return this; }
        public Builder url(String u) { this.url = u; return this; }
        public Builder body(String b) { this.body = b; return this; }

        public HttpRequest build() {
            if (method == null) throw new IllegalStateException("method required");
            if (url == null) throw new IllegalStateException("url required");
            return new HttpRequest(this);
        }
    }
}
```
</details>

---

### Problem 2: Builder with Nested Immutable Object

Design a `Pizza` class using Builder pattern with:
- Required: `size` (SMALL, MEDIUM, LARGE)
- Optional: `cheese` (boolean, default false), `pepperoni` (boolean, default false), `toppings` (List<String>, can add one by one)
- The built Pizza must be immutable

<details><summary>Solution</summary>

```java
import java.util.*;

public final class Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }

    private final Size size;
    private final boolean cheese;
    private final boolean pepperoni;
    private final List<String> toppings;

    private Pizza(Builder b) {
        this.size = b.size;
        this.cheese = b.cheese;
        this.pepperoni = b.pepperoni;
        this.toppings = List.copyOf(b.toppings); // defensive copy
    }

    public Size size() { return size; }
    public boolean cheese() { return cheese; }
    public boolean pepperoni() { return pepperoni; }
    public List<String> toppings() { return toppings; } // already unmodifiable

    @Override
    public String toString() {
        return size + " pizza" +
               (cheese ? " +cheese" : "") +
               (pepperoni ? " +pepperoni" : "") +
               (toppings.isEmpty() ? "" : " +toppings=" + toppings);
    }

    public static class Builder {
        private final Size size;           // required
        private boolean cheese = false;
        private boolean pepperoni = false;
        private List<String> toppings = new ArrayList<>();

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        public Builder cheese(boolean c) { this.cheese = c; return this; }
        public Builder pepperoni(boolean p) { this.pepperoni = p; return this; }
        public Builder addTopping(String t) { this.toppings.add(t); return this; }

        public Pizza build() {
            return new Pizza(this);
        }
    }

    public static void main(String[] args) {
        Pizza p = new Pizza.Builder(Size.LARGE)
            .cheese(true)
            .addTopping("olives")
            .addTopping("mushrooms")
            .build();

        System.out.println(p);
        // LARGE pizza +cheese +toppings=[olives, mushrooms]
    }
}
```
</details>

---

### Problem 3: Builder with `toBuilder()` Support

Extend the `Order` class to support a `toBuilder()` method that creates a pre-filled Builder from an existing Order. Demonstrate creating a modified copy.

<details><summary>Solution</summary>

```java
import java.util.Objects;

public final class Order {
    private final String customerId;
    private final int priority;
    private final boolean giftWrap;
    private final boolean expressDelivery;

    private Order(Builder b) {
        this.customerId = b.customerId;
        this.priority = b.priority;
        this.giftWrap = b.giftWrap;
        this.expressDelivery = b.expressDelivery;
    }

    public String customerId() { return customerId; }
    public int priority() { return priority; }
    public boolean giftWrap() { return giftWrap; }
    public boolean expressDelivery() { return expressDelivery; }

    // toBuilder: returns a pre-filled builder
    public Builder toBuilder() {
        return new Builder()
            .customerId(this.customerId)
            .priority(this.priority)
            .giftWrap(this.giftWrap)
            .expressDelivery(this.expressDelivery);
    }

    @Override
    public String toString() {
        return "Order{" + customerId + ", p=" + priority +
               ", gift=" + giftWrap + ", express=" + expressDelivery + "}";
    }

    public static class Builder {
        private String customerId;
        private int priority = 1;
        private boolean giftWrap = false;
        private boolean expressDelivery = false;

        public Builder customerId(String id) { this.customerId = id; return this; }
        public Builder priority(int p) { this.priority = p; return this; }
        public Builder giftWrap(boolean g) { this.giftWrap = g; return this; }
        public Builder expressDelivery(boolean e) { this.expressDelivery = e; return this; }

        public Order build() {
            Objects.requireNonNull(customerId, "customerId required");
            return new Order(this);
        }
    }

    public static void main(String[] args) {
        Order original = new Order.Builder()
            .customerId("C101")
            .priority(2)
            .giftWrap(true)
            .build();

        // Create a modified copy: change priority and add express delivery
        Order modified = original.toBuilder()
            .priority(5)
            .expressDelivery(true)
            .build();

        System.out.println(original);
        // Order{C101, p=2, gift=true, express=false}
        System.out.println(modified);
        // Order{C101, p=5, gift=true, express=true}
    }
}
```

**Key insight:** `toBuilder()` enables creating modified copies of immutable objects without exposing setters. The original stays unchanged.
</details>
