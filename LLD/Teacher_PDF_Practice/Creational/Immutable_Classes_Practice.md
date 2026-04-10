# Immutable Classes -- Teacher PDF Practice

> All questions are based on the **Immutable_Classes.pdf** examples (PaymentReceipt, Order list mutation, GameConfig, 6-step checklist).

---

## Viva Questions

**Q1: The PDF walks through immutability in 6 steps. List each step and the problem it solves.**

1. **Why Immutability?** Immutable objects are thread-safe (no synchronization needed), easy to reason about (state never changes after construction), and prevent accidental modifications to shared data.
2. **Simple immutable class (PaymentReceipt):** Fields `id` (String), `amount` (double), `timestamp` (Instant) are `private final`. The constructor validates `id`. No setters are provided. This works because all three fields are either primitives or already-immutable types.
3. **Mutable field problem (Order):** The Order class stores `List<String> items`. Writing `this.items = items;` in the constructor is DANGEROUS because the caller retains a reference to the same list. The caller can then call `items.add(...)` or `items.clear()` and mutate the Order's internal state from outside.
4. **Defensive copying:** In the constructor, use `this.items = List.copyOf(items);` to create an independent copy. In the getter, return `Collections.unmodifiableList(items);` so the caller cannot mutate the internal list through the returned reference.
5. **Inheritance risks:** A subclass can add mutable fields or setters, breaking the immutability contract of the parent. Solution: mark the class `final` so it cannot be subclassed.
6. **Thread safety:** Immutable objects need no synchronization because their state never changes after construction. They are also safe to use as keys in HashMaps and members of HashSets because their `hashCode()` never changes.

---

**Q2: In the PDF's PaymentReceipt example, why does it work without defensive copies even though it has three fields?**

All three fields are inherently immutable:
- `id` is a `String`, which is immutable in Java.
- `amount` is a `double`, which is a primitive -- primitives are always copied by value, not by reference.
- `timestamp` is an `Instant`, which is an immutable class from `java.time`.

Since none of these types can be mutated through a reference, a simple `private final` declaration plus no setters is sufficient. Defensive copying is only needed when a field's type is mutable (e.g., `List`, `Date`, arrays).

---

**Q3: The PDF shows the Order class with `this.items = items;` and calls it DANGEROUS. Demonstrate the exact attack.**

```java
List<String> myList = new ArrayList<>();
myList.add("Laptop");
Order order = new Order(myList);

// Attack: caller still holds a reference to 'myList'
myList.add("Free TV");  // mutates the Order's internal list

// order.getItems() now returns ["Laptop", "Free TV"]
// The supposedly-immutable Order has been changed from outside
```

The root cause is aliasing -- both `myList` (outside) and `this.items` (inside) point to the same `ArrayList` object on the heap. Any mutation through either reference affects both.

---

**Q4: The PDF distinguishes between defensive copy and unmodifiable view. What is the difference, and where is each used in GameConfig?**

- **Defensive copy (`List.copyOf`):** Used in the **constructor**. Creates a brand-new, independent list containing the same elements. After copying, the caller's original list and the internal list are completely separate objects. Mutations to the caller's list do not affect the internal list. In GameConfig: `this.rules = List.copyOf(rules);`
- **Unmodifiable view (`Collections.unmodifiableList`):** Used in the **getter**. Wraps the internal list in a read-only view. The caller receives a reference that throws `UnsupportedOperationException` on any mutating operation (`add`, `remove`, `set`, `clear`). In GameConfig: `return Collections.unmodifiableList(rules);`

Both are needed. Without the defensive copy in the constructor, the caller can still mutate via their original reference. Without the unmodifiable wrapper in the getter, the caller can mutate via the returned reference.

---

**Q5: Why does the PDF say to mark immutable classes `final`? Give a concrete example of how a subclass breaks immutability.**

```java
// Suppose GameConfig is NOT final
public class MutableGameConfig extends GameConfig {
    private String extraField;

    public MutableGameConfig(String name, List<String> rules) {
        super(name, rules);
    }

    public void setExtraField(String value) {
        this.extraField = value;  // mutable state added
    }
}
```

Now anywhere the code expects an immutable `GameConfig`, someone can pass a `MutableGameConfig` instead (Liskov substitution). That object can be mutated after construction, violating the immutability guarantee. Marking `GameConfig` as `final` prevents any subclass from existing.

---

**Q6: The PDF mentions that immutable objects work well as hash keys. Why would a mutable object be dangerous as a HashMap key?**

A `HashMap` stores entries in buckets based on `hashCode()`. If an object is used as a key and then mutated, its `hashCode()` changes but it remains in the old bucket. When you later call `map.get(key)`, the map computes the new hash, looks in the wrong bucket, and cannot find the entry. The key-value pair becomes unreachable -- effectively lost -- even though it is still inside the map. Immutable objects guarantee that `hashCode()` never changes after construction, so the key always maps to the correct bucket.

---

**Q7: List the 6 rules from the PDF's Quick Recap for making a class immutable, and for each rule name which step in the PDF introduced it.**

| Rule | PDF Step |
|------|----------|
| 1. Make all fields `private final` | Step 2 (PaymentReceipt) |
| 2. Provide no setters | Step 2 (PaymentReceipt) |
| 3. Defensive copy mutable fields in the constructor | Step 4 (Order fix) |
| 4. Return unmodifiable views from getters | Step 4 (Order fix) |
| 5. Mark the class `final` | Step 5 (Inheritance risks) |
| 6. Validate all constructor arguments | Step 2 (PaymentReceipt validates `id`) and Step 6 (GameConfig uses `Objects.requireNonNull`) |

---

## MCQ Quiz

**Q1.** In the PDF's PaymentReceipt class, which field is validated in the constructor?

a) `amount`
b) `timestamp`
c) `id`
d) All three fields

<details><summary>Answer</summary>c) The constructor validates <code>id</code>. The other two fields (amount as a primitive double, and timestamp as an Instant) are not explicitly validated in the PDF's PaymentReceipt example.</details>

---

**Q2.** Why does PaymentReceipt NOT need defensive copies for its fields?

a) It uses `Collections.unmodifiableList` internally
b) All its fields are either primitives or already-immutable types (String, double, Instant)
c) The class is marked `final`
d) The constructor makes deep clones automatically

<details><summary>Answer</summary>b) String is immutable, double is a primitive (copied by value), and Instant is immutable. No mutable references exist to defend against.</details>

---

**Q3.** In the PDF's Order class, what is the DANGEROUS line?

a) `private final List<String> items;`
b) `this.items = List.copyOf(items);`
c) `this.items = items;`
d) `return Collections.unmodifiableList(items);`

<details><summary>Answer</summary>c) <code>this.items = items;</code> assigns the caller's mutable list directly to the internal field. The caller retains a reference and can mutate the Order's state from outside.</details>

---

**Q4.** What does `List.copyOf(items)` do, as used in the PDF's constructor fix?

a) Returns a mutable copy of the list
b) Returns an unmodifiable, independent copy of the list
c) Returns a reference to the same list wrapped as unmodifiable
d) Returns a synchronized version of the list

<details><summary>Answer</summary>b) <code>List.copyOf</code> creates a new, independent, unmodifiable list containing the same elements. Changes to the original list do not affect the copy.</details>

---

**Q5.** In the PDF's GameConfig class, what does `Objects.requireNonNull` do?

a) Creates a defensive copy of the argument
b) Marks the field as `final`
c) Throws `NullPointerException` immediately if the argument is null
d) Returns an `Optional` wrapping the argument

<details><summary>Answer</summary>c) <code>Objects.requireNonNull</code> throws a <code>NullPointerException</code> at construction time if a null value is passed, failing fast rather than allowing a null to hide inside the object.</details>

---

**Q6.** An Order object is created correctly with defensive copies. A caller then does: `order.getItems().add("Hack");`. What happens if the getter returns `Collections.unmodifiableList(items)`?

a) "Hack" is added to the internal list
b) Nothing happens -- the add is silently ignored
c) `UnsupportedOperationException` is thrown
d) `ConcurrentModificationException` is thrown

<details><summary>Answer</summary>c) <code>Collections.unmodifiableList</code> wraps the list in a read-only view. Any mutating operation (add, remove, set, clear) throws <code>UnsupportedOperationException</code>.</details>

---

**Q7.** According to the PDF, why must an immutable class be marked `final`?

a) To make it eligible for garbage collection
b) To prevent subclasses from adding mutable state or setters
c) To allow the JVM to inline its methods
d) To make its fields `private`

<details><summary>Answer</summary>b) A subclass can add setters or mutable fields, breaking the immutability contract. Marking the class <code>final</code> prevents subclassing entirely.</details>

---

**Q8.** The PDF states that immutable objects need no synchronization for thread safety. Why?

a) The JVM locks immutable objects automatically
b) Their state never changes after construction, so no thread can observe a half-updated state
c) They use `volatile` fields internally
d) They are stored in thread-local memory

<details><summary>Answer</summary>b) Once constructed, an immutable object's state is fixed. Every thread sees the same values. There is no write-after-read conflict possible, so no locks or synchronization are needed.</details>

---

**Q9.** Which combination correctly summarizes the PDF's defense strategy for mutable fields?

a) Defensive copy in the getter, unmodifiable view in the constructor
b) Defensive copy in both the constructor and getter
c) Defensive copy in the constructor, unmodifiable view in the getter
d) Unmodifiable view in both the constructor and getter

<details><summary>Answer</summary>c) The constructor uses <code>List.copyOf</code> (defensive copy) to break the alias with the caller's list. The getter uses <code>Collections.unmodifiableList</code> (unmodifiable view) to prevent callers from mutating through the returned reference.</details>

---

**Q10.** The PDF's GameConfig class has two fields: `name` (String) and `rules` (List of String). Which field requires defensive copying?

a) `name`, because Strings can be modified via reflection
b) `rules`, because `List` is a mutable type
c) Both fields equally
d) Neither -- `final` is sufficient

<details><summary>Answer</summary>b) <code>rules</code> is a <code>List&lt;String&gt;</code>, which is mutable. <code>name</code> is a String, which is immutable in Java and does not need defensive copying.</details>

---

## Self-Scoring

| Category | Total | Your Score |
|----------|-------|------------|
| Viva Questions (1 pt each) | /7 | |
| MCQ Quiz (1 pt each) | /10 | |
| Coding Problem 1 | /5 | |
| Coding Problem 2 | /5 | |
| **Total** | **/27** | |

**Scoring guide for Viva:**
- 1 pt: Complete answer hitting all key points.
- 0.5 pt: Partially correct or missing a key detail.
- 0 pt: Incorrect or blank.

---

## Coding Problems

### Problem 1: Fix the Broken Immutable Class

The following class is supposed to be immutable but has multiple violations of the PDF's 6-rule checklist. Identify every violation and fix them.

```java
public class StudentRecord {
    public final String name;
    private final List<Integer> grades;
    private final Date enrollmentDate;

    public StudentRecord(String name, List<Integer> grades, Date enrollmentDate) {
        this.name = name;
        this.grades = grades;
        this.enrollmentDate = enrollmentDate;
    }

    public String getName() { return name; }
    public List<Integer> getGrades() { return grades; }
    public Date getEnrollmentDate() { return enrollmentDate; }

    public void setName(String name) {
        // no-op, but still here
    }
}
```

<details><summary>Solution</summary>

**Violations found (mapped to the PDF's 6 rules):**

1. **Rule 1 (private final):** `name` is `public final` instead of `private final`. External code can read it directly, bypassing any future getter logic.
2. **Rule 2 (no setters):** `setName` exists. Even though it is a no-op, setters signal mutability and violate the contract.
3. **Rule 3 (defensive copy in constructor):** `this.grades = grades;` is the same DANGEROUS pattern from the PDF's Order class. `this.enrollmentDate = enrollmentDate;` also has the same alias problem (`Date` is mutable).
4. **Rule 4 (unmodifiable out):** `getGrades()` returns the raw internal list. `getEnrollmentDate()` returns the internal `Date` reference, which can be mutated via `setTime()`.
5. **Rule 5 (final class):** The class is not `final`. A subclass can add mutable state.
6. **Rule 6 (validate):** No null checks on any constructor arguments.

**Fixed version:**

```java
import java.util.*;

public final class StudentRecord {
    private final String name;
    private final List<Integer> grades;
    private final Date enrollmentDate;

    public StudentRecord(String name, List<Integer> grades, Date enrollmentDate) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(grades, "grades must not be null");
        Objects.requireNonNull(enrollmentDate, "enrollmentDate must not be null");

        this.name = name;
        this.grades = List.copyOf(grades);
        this.enrollmentDate = new Date(enrollmentDate.getTime());  // defensive copy
    }

    public String getName() { return name; }

    public List<Integer> getGrades() {
        return Collections.unmodifiableList(grades);
    }

    public Date getEnrollmentDate() {
        return new Date(enrollmentDate.getTime());  // defensive copy out
    }

    // no setters
}
```

**Key points:**
- `Date` is mutable (unlike `Instant`), so it needs defensive copies in BOTH the constructor and the getter.
- `List.copyOf` already returns an unmodifiable list, but wrapping in `Collections.unmodifiableList` in the getter is harmless and makes the intent explicit (matching the PDF's pattern).

</details>

---

### Problem 2: Write an Immutable Class from Scratch

Design an immutable `MovieTicket` class with the following fields:
- `movieName` (String)
- `seatNumber` (int)
- `showtime` (Instant)
- `snacks` (List of String)

Apply all 6 rules from the PDF's Quick Recap. Include constructor validation using `Objects.requireNonNull`. Demonstrate that the class resists both the Order-style constructor mutation attack and the getter mutation attack.

<details><summary>Solution</summary>

```java
import java.time.Instant;
import java.util.*;

public final class MovieTicket {
    private final String movieName;
    private final int seatNumber;
    private final Instant showtime;
    private final List<String> snacks;

    public MovieTicket(String movieName, int seatNumber, Instant showtime, List<String> snacks) {
        // Rule 6: validate
        Objects.requireNonNull(movieName, "movieName must not be null");
        Objects.requireNonNull(showtime, "showtime must not be null");
        Objects.requireNonNull(snacks, "snacks must not be null");
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("seatNumber must be positive");
        }

        // Rule 1: private final (declared above)
        this.movieName = movieName;          // String is immutable, no copy needed
        this.seatNumber = seatNumber;        // int is a primitive, copied by value
        this.showtime = showtime;            // Instant is immutable, no copy needed
        this.snacks = List.copyOf(snacks);   // Rule 3: defensive copy in constructor
    }

    // Rule 2: no setters anywhere

    public String getMovieName() { return movieName; }
    public int getSeatNumber() { return seatNumber; }
    public Instant getShowtime() { return showtime; }

    // Rule 4: unmodifiable view out
    public List<String> getSnacks() {
        return Collections.unmodifiableList(snacks);
    }
}
```

**Proof that it resists attacks:**

```java
// Constructor mutation attack (like the PDF's Order example)
List<String> mySnacks = new ArrayList<>(List.of("Popcorn", "Soda"));
MovieTicket ticket = new MovieTicket("Inception", 42, Instant.now(), mySnacks);

mySnacks.add("Free Nachos");  // attacker mutates their original list
System.out.println(ticket.getSnacks());
// Output: [Popcorn, Soda]  -- internal list is unaffected (defensive copy blocked it)

// Getter mutation attack
ticket.getSnacks().add("Free Nachos");
// Throws UnsupportedOperationException (unmodifiable view blocked it)
```

**Checklist verification:**
| Rule | Applied? |
|------|----------|
| 1. All fields `private final` | Yes |
| 2. No setters | Yes |
| 3. Defensive copy in (List.copyOf for snacks) | Yes |
| 4. Unmodifiable out (Collections.unmodifiableList in getter) | Yes |
| 5. Class is `final` | Yes |
| 6. Validate via Objects.requireNonNull + seatNumber check | Yes |

</details>
