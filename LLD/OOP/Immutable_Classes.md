# Immutable Classes in Java

**Intent:** Objects whose state **cannot change** after construction.
**Use cases:** Payment receipts, order snapshots, currency/money values, configuration objects, DTOs shared across threads, HashMap keys.

---

## Why Immutability?

- **Thread-safe by default:** No synchronization needed -- if the object can't change, concurrent reads can't conflict.
- **Easy to reason about:** No surprise mutations from other code holding a reference.
- **Safe as HashMap keys:** If fields never change, `hashCode()` never changes.
- **Prevents defensive bugs:** No accidental state corruption from shared references.

---

## Step 1: Simple Immutable Class

**Rules:**
1. Make all fields `private final`
2. Set values via constructor only
3. No setters

```java
import java.time.Instant;

public final class PaymentReceipt {
    private final String id;
    private final double amount;
    private final Instant timestamp;

    public PaymentReceipt(String id, double amount, Instant timestamp) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        this.id = id;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
}
```

This works because `String`, `double`, and `Instant` are all **already immutable**. No special handling needed.

**But what if a field is a mutable type like `List`?**

---

## Step 2: The Mutable Field Trap (BAD)

```java
// BAD: Looks immutable, but isn't!
public final class Order {
    private final List<String> items;

    public Order(List<String> items) {
        this.items = items;  // stores the SAME reference!
    }

    public List<String> getItems() { return items; }
}
```

**The attack:**
```java
List<String> list = new ArrayList<>();
list.add("Laptop");
Order order = new Order(list);

list.add("Phone");  // Mutates the Order's internal list!
System.out.println(order.getItems());  // [Laptop, Phone] -- broken!

order.getItems().add("Tablet");  // Also mutates through getter!
System.out.println(order.getItems());  // [Laptop, Phone, Tablet] -- doubly broken!
```

**Two problems:**
1. The constructor stores the **same reference** -- external code can mutate it.
2. The getter returns the **same reference** -- callers can mutate through it.

---

## Step 3: Defensive Copying (GOOD)

```java
// GOOD: Truly immutable
import java.util.*;

public final class Order {
    private final List<String> items;

    public Order(List<String> items) {
        this.items = List.copyOf(items);  // defensive copy IN
    }

    public List<String> getItems() {
        return Collections.unmodifiableList(items);  // safe view OUT
    }
}
```

**Now the attacks fail:**
```java
List<String> list = new ArrayList<>();
list.add("Laptop");
Order order = new Order(list);

list.add("Phone");  // Only mutates the caller's list, not Order's copy
System.out.println(order.getItems());  // [Laptop] -- safe!

order.getItems().add("Tablet");  // UnsupportedOperationException!
```

**Two defenses:**
1. **Copy IN:** `List.copyOf(items)` in the constructor makes an independent copy.
2. **Protect OUT:** `Collections.unmodifiableList()` in the getter returns a read-only view.

**Exam Tip:** `List.copyOf()` (Java 10+) both copies AND makes it unmodifiable. `Collections.unmodifiableList()` only wraps -- if the underlying list changes, the view changes too. For constructor defense, `List.copyOf()` is safer.

---

## Step 4: Prevent Subclassing

```java
// BAD: Subclass can add a setter and break immutability
public class Config {
    private final String name;
    public Config(String name) { this.name = name; }
    public String getName() { return name; }
}

class MutableConfig extends Config {
    private String hackName;
    public MutableConfig(String name) { super(name); this.hackName = name; }
    public void setName(String n) { this.hackName = n; }
    @Override public String getName() { return hackName; }  // breaks the contract!
}
```

**Fix:** Mark the class `final`:
```java
public final class Config {  // cannot be subclassed
    private final String name;
    // ...
}
```

---

## Complete Checklist for Making a Class Immutable

| Step | What to do | Why |
|------|-----------|-----|
| 1 | Make the class `final` | Prevent subclasses from adding mutability |
| 2 | Make all fields `private final` | No direct access, no reassignment |
| 3 | No setters | No way to change fields after construction |
| 4 | Defensive copy mutable inputs in constructor | Caller can't mutate your internals through the original reference |
| 5 | Return unmodifiable views (or defensive copies) in getters | Caller can't mutate your internals through the returned reference |
| 6 | Validate in constructor | Ensure the object is in a valid state from the start |

---

## Complete Example: GameConfig

```java
import java.util.*;

public final class GameConfig {
    private final String name;
    private final List<String> rules;

    public GameConfig(String name, List<String> rules) {
        this.name = Objects.requireNonNull(name);
        this.rules = List.copyOf(rules);  // defensive copy
    }

    public String getName() { return name; }

    public List<String> getRules() {
        return Collections.unmodifiableList(rules);  // safe view
    }

    @Override
    public String toString() {
        return name + " with rules " + rules;
    }
}
```

**Usage:**
```java
List<String> rules = new ArrayList<>(List.of("No cheating", "Time limit: 10 min"));
GameConfig config = new GameConfig("Battle Royale", rules);

rules.add("Extra rule");  // doesn't affect config
System.out.println(config.getRules());  // [No cheating, Time limit: 10 min]

config.getRules().add("Hack");  // UnsupportedOperationException!
```

---

## How Immutability Connects to Other Topics

| Topic | Connection |
|-------|-----------|
| **Encapsulation** | Immutability is encapsulation taken to the extreme -- not just controlled access, but NO mutation at all |
| **Builder Pattern** | Builder is the go-to way to construct immutable objects with many fields. Builder is mutable; the built product is immutable |
| **Prototype Pattern** | When cloning prototypes, immutable style objects can be shared safely (no need to deep-copy them) |
| **Strategy Pattern** | Strategies should ideally be immutable (stateless or with immutable config) |
| **Thread Safety** | Immutable = thread-safe for free. This is why Singleton + enum works -- enum instances are immutable |
| **`String` in Java** | The most famous immutable class. `String` pool works BECAUSE strings are immutable |

---

## Exam Tips

- **"How do you make a class immutable?"** -- recite the 6-step checklist above.
- **"What's defensive copying?"** -- copying mutable inputs in the constructor AND returning safe views in getters.
- **"Why `final` class?"** -- prevent subclasses from adding setters that break the contract.
- **"`List.copyOf()` vs `Collections.unmodifiableList()`?"** -- `copyOf` makes a new independent copy. `unmodifiableList` is just a read-only wrapper around the SAME list (underlying mutations leak through).
- **"Are immutable objects thread-safe?"** -- Yes, always, by definition.
- **"Downside of immutability?"** -- Every "change" creates a new object (memory/GC cost). For frequently modified data, consider mutable objects with explicit synchronization.

---

## Viva Questions

**Q1: What makes a class immutable?**

Six things: class is `final`, all fields are `private final`, no setters, defensive copy mutable inputs in constructor, return unmodifiable views in getters, validate in constructor. The key insight is that `final` on a reference field only prevents reassignment -- it does NOT prevent mutation of the object the reference points to. That's why defensive copying is needed for mutable types like `List`.

---

**Q2: Why is defensive copying needed if fields are already `final`?**

`final` only prevents the **reference** from being reassigned. It does NOT make the referenced object immutable. `final List<String> items` means you can't do `this.items = anotherList`, but you CAN do `this.items.add("hack")`. Defensive copying ensures the immutable class has its **own independent copy** that nobody else can modify.

---

**Q3: What is the difference between `List.copyOf()` and `Collections.unmodifiableList()`?**

`List.copyOf()` creates a brand new independent list -- changes to the original have no effect. `Collections.unmodifiableList()` returns a **read-only view** of the SAME underlying list -- if someone mutates the original, the view changes too. For constructor defense, `List.copyOf()` is safer.

---

**Q4: Why should an immutable class be `final`?**

Without `final`, a subclass can override methods and add mutable state. A subclass could override `getName()` to return a mutable field instead of the immutable one. Callers holding a parent reference would see mutable behavior, breaking the immutability contract (violates LSP).

---

**Q5: How does immutability relate to thread safety?**

If an object's state cannot change, concurrent reads can never see inconsistent state. No locks, no synchronization, no volatile needed. This is why `String` and `Integer` are safe to share across threads -- they're immutable.

---

**Q6: Name a pattern that relies heavily on immutability.**

Builder pattern. The Builder itself is mutable (you call `.setX()` fluently), but the final product returned by `build()` is immutable. This separation lets you construct complex objects step-by-step while ensuring the result can never be corrupted.

---

**Q7: What happens if you use a mutable object as a HashMap key and then modify it?**

The `hashCode()` changes, but the object is stored in the bucket corresponding to the OLD hash. The map can no longer find it -- `get()` returns `null` even though the object is technically in the map. This is why immutable objects make ideal HashMap keys.

---

## MCQ Quick-Fire (10 questions)

**Q1.** Which of these alone makes a class immutable?
a) `private` fields  b) `final` fields  c) No setters  d) None of these alone

<details><summary>Answer</summary>d) None of these alone. All steps must be applied together.</details>

---

**Q2.** What does `final` on a field prevent?
a) Mutation of the referenced object  b) Reassignment of the reference  c) Both  d) Neither

<details><summary>Answer</summary>b) Reassignment of the reference only. You can still call mutating methods on the object.</details>

---

**Q3.** What is the output?
```java
final List<String> list = new ArrayList<>();
list.add("hello");
System.out.println(list.size());
```
a) Compile error  b) 0  c) 1  d) Runtime error

<details><summary>Answer</summary>c) 1. `final` prevents `list = anotherList`, but `list.add()` is still allowed.</details>

---

**Q4.** Which returns an independent copy?
a) `Collections.unmodifiableList(original)`  b) `List.copyOf(original)`  c) Both  d) Neither

<details><summary>Answer</summary>b) `List.copyOf()`. `unmodifiableList` wraps the same underlying list.</details>

---

**Q5.** Why mark an immutable class `final`?
a) Improve performance  b) Prevent subclasses from breaking immutability  c) Allow garbage collection  d) Required by Java spec

<details><summary>Answer</summary>b) Prevent subclasses from adding setters or overriding methods to expose mutable state.</details>

---

**Q6.** Immutable objects are inherently:
a) Faster  b) Thread-safe  c) Serializable  d) Singleton

<details><summary>Answer</summary>b) Thread-safe. No synchronization needed since state never changes.</details>

---

**Q7.** Which Java class is the most well-known example of immutability?
a) `ArrayList`  b) `StringBuilder`  c) `String`  d) `HashMap`

<details><summary>Answer</summary>c) `String`. It's final, its internal char array is not exposed, and all "modification" methods return new String objects.</details>

---

**Q8.** A defensive copy in the constructor protects against:
a) The caller modifying the passed-in object after construction  b) The getter returning a mutable reference  c) Subclass attacks  d) Reflection attacks

<details><summary>Answer</summary>a) The caller still holds the original reference and could mutate it. The copy ensures the immutable object has its own independent data.</details>

---

**Q9.** Which pattern is specifically designed to construct immutable objects with many fields?
a) Singleton  b) Factory  c) Builder  d) Prototype

<details><summary>Answer</summary>c) Builder. The builder is mutable; the built product is immutable.</details>

---

**Q10.** What exception does `Collections.unmodifiableList(...).add("x")` throw?
a) `IllegalStateException`  b) `UnsupportedOperationException`  c) `ConcurrentModificationException`  d) Compile error

<details><summary>Answer</summary>b) `UnsupportedOperationException`.</details>
