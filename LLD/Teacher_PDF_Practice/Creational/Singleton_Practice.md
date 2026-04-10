# Singleton -- Teacher PDF Practice

> All questions are based on the **DesignPattern_Singleton.pdf** examples (Logger class, 7-step evolution).

---

## Viva Questions

**Q1: Walk me through the evolution of Singleton as shown in class. What are the 7 steps?**

1. **Private constructor** -- prevents external `new Logger()`.
2. **Eager initialization** -- `private static final Logger INSTANCE = new Logger();` Created at class load time. Problem: wastes memory if never used.
3. **Lazy initialization** -- check `instance == null` in `getInstance()`. Problem: not thread-safe.
4. **Synchronized method** -- `public static synchronized Logger getInstance()`. Problem: every call pays the synchronization cost, even after instance exists.
5. **Double-Checked Locking (DCL) without volatile** -- only synchronize on first creation. Problem: BROKEN due to instruction reordering and caching.
6. **DCL with volatile** -- `private static volatile Logger instance;` Fixes visibility and ordering. Correct since Java 5.
7. **Static Inner Class (Holder idiom)** -- `private static class Holder { static final Logger INSTANCE = new Logger(); }`. Lazy, thread-safe, no synchronization. Relies on JVM classloading guarantees.
8. **Enum Singleton** -- `public enum Logger { INSTANCE; }`. Safest: resists reflection and serialization attacks.

---

**Q2: In the PDF, why is DCL without `volatile` broken? Give both reasons.**

Two problems:
1. **Instruction reordering:** `new Logger()` is not atomic. It involves: (a) allocate memory, (b) initialize object, (c) assign reference. Without `volatile`, the JVM can reorder (b) and (c). Thread B sees a non-null reference to a **half-constructed** object.
2. **Caching/visibility:** Without `volatile`, Thread A may write the new instance to its CPU cache. Thread B still reads `null` from its own cache. `volatile` ensures writes are immediately visible to all threads (happens-before guarantee).

---

**Q3: The PDF mentions the Holder idiom. Why doesn't it need `volatile` or `synchronized`?**

The JVM guarantees that a class is loaded and initialized only once, and this initialization is thread-safe. The inner class `Holder` is not loaded until `getInstance()` is first called (because nothing else references it). When it IS loaded, the JVM ensures `INSTANCE` is fully constructed before any thread can access it. This gives us lazy loading + thread safety for free.

---

**Q4: Why does the PDF say Enum Singleton is the "safest"? What attacks does it resist?**

1. **Reflection attacks:** `Constructor.newInstance()` on an enum throws `IllegalArgumentException`. The JVM explicitly prevents reflective construction of enums.
2. **Serialization attacks:** When you deserialize an enum, Java returns the existing constant, not a new object. Regular singletons need `readResolve()` to prevent deserialization from creating a second instance.

Limitation: Enum singletons are eagerly initialized and cannot extend a class.

---

**Q5: The PDF lists use cases: Logging, Config Manager, Feature Flags, Connection Pool, Metrics Reporter. Pick one and explain why Singleton is appropriate.**

**Connection Pool Manager:** You want exactly one pool managing all database connections. Multiple pools would waste connections, exceed DB limits, and make it impossible to enforce pool-wide settings (max connections, timeout). A singleton ensures all application code shares the same pool instance.

---

**Q6: How would you test a Singleton? The PDF mentions this is hard.**

Singletons are hard to mock because you can't substitute a different instance. Solutions:
- Prefer **dependency injection**: pass the singleton as a constructor parameter. Tests inject a mock instead.
- Use a `reset()` method (test-only) to clear the instance between tests.
- The Holder and Enum approaches are especially hard to reset -- DI is the cleanest solution.

---

## MCQ Quiz (20 Questions)

**Q1.** In the PDF's Step 2 (Eager init), when is the Logger instance created?

a) When `getInstance()` is first called
b) When the `Logger` class is loaded by the JVM
c) When `main()` starts
d) When the first thread requests it

<details><summary>Answer</summary>b) When the Logger class is loaded. The `static final` field is initialized during class loading, before any code calls `getInstance()`.</details>

---

**Q2.** In Step 3 (Lazy init), what happens if Thread A and Thread B both call `getInstance()` simultaneously when `instance` is `null`?

a) Only one thread creates the instance
b) Both threads may create separate instances
c) One thread blocks until the other finishes
d) A `ConcurrentModificationException` is thrown

<details><summary>Answer</summary>b) Both threads may see `instance == null`, both enter the `if` block, and both create separate instances. Singleton is broken.</details>

---

**Q3.** In Step 4, the entire `getInstance()` method is `synchronized`. What is the performance problem?

a) Thread creation overhead
b) Every call to `getInstance()` acquires a lock, even after the instance exists
c) The instance is created twice
d) Deadlock risk

<details><summary>Answer</summary>b) After the instance is created, synchronization is unnecessary but still happens on every call. This is wasted overhead.</details>

---

**Q4.** In DCL (Step 5), what does "double-checked" refer to?

a) Two `synchronized` blocks
b) Two `null` checks -- one before and one inside the `synchronized` block
c) Two threads checking simultaneously
d) Two instances being compared

<details><summary>Answer</summary>b) The first check avoids synchronization when the instance already exists. The second check (inside the `synchronized` block) ensures only one thread creates the instance.</details>

---

**Q5.** Why is DCL broken WITHOUT `volatile`? (The PDF gives two reasons)

a) The lock doesn't work
b) Instruction reordering can expose a half-constructed object, and CPU caching can hide the write
c) The `null` check is wrong
d) `synchronized` doesn't prevent multiple instances

<details><summary>Answer</summary>b) Without `volatile`: (1) JVM can reorder object initialization and reference assignment, so another thread sees a non-null reference to an uninitialized object. (2) CPU caches may not propagate the write to other threads.</details>

---

**Q6.** What does `volatile` guarantee in the DCL Singleton?

a) Atomicity of the `new` operation
b) Visibility (writes are immediately seen by other threads) and ordering (happens-before)
c) That only one thread can read the field
d) That the field is stored on the heap

<details><summary>Answer</summary>b) `volatile` ensures visibility (all threads see the latest write) and ordering (construction completes before the reference is published).</details>

---

**Q7.** The Holder idiom in the PDF uses a `private static class Holder`. When is this inner class loaded?

a) When `Logger` is loaded
b) When `getInstance()` is first called
c) When the application starts
d) When any static method of `Logger` is called

<details><summary>Answer</summary>b) The JVM only loads `Holder` when it's first referenced -- which happens inside `getInstance()`. This is what makes it lazy.</details>

---

**Q8.** Which Singleton approach does the PDF recommend as the "cleanest balance of lazy + safe"?

a) Synchronized method
b) DCL with volatile
c) Holder idiom (static inner class)
d) Enum

<details><summary>Answer</summary>c) Holder idiom. The PDF says: "Lazy, thread-safe, clean, no synchronization needed."</details>

---

**Q9.** Which approach does the PDF say is "safest overall"?

a) Holder idiom
b) DCL with volatile
c) Eager initialization
d) Enum Singleton

<details><summary>Answer</summary>d) Enum. The PDF says: "Simplest and safest form of Singleton" -- resists reflection and serialization.</details>

---

**Q10.** What is the limitation of Enum Singleton mentioned in the PDF?

a) Not thread-safe
b) No lazy initialization, cannot subclass
c) Requires `volatile`
d) Cannot have methods

<details><summary>Answer</summary>b) The PDF says: "Limitation: No lazy initialization, cannot subclass." Enums are eagerly loaded and can't extend classes.</details>

---

**Q11.** In the PDF, what does `readResolve()` do for regular Singletons?

a) Creates the instance
b) Prevents serialization from creating a second instance
c) Resolves thread conflicts
d) Reads the instance from disk

<details><summary>Answer</summary>b) During deserialization, `readResolve()` returns the existing singleton instance instead of letting Java create a new one.</details>

---

**Q12.** What is the correct order of object creation steps that gets reordered without `volatile`?

a) Assign reference -> Allocate memory -> Initialize
b) Allocate memory -> Initialize -> Assign reference (correct)
c) Initialize -> Allocate memory -> Assign reference
d) Allocate memory -> Assign reference -> Initialize (reordered, dangerous)

<details><summary>Answer</summary>d) is what happens with reordering. The correct order is (b), but without `volatile`, the JVM can do (d) -- assigning the reference before initialization completes.</details>

---

**Q13.** How many interview questions does the PDF list at the end?

a) 4
b) 6
c) 8
d) 10

<details><summary>Answer</summary>b) 6 questions: (1) How to make a class Singleton, (2) eager init problem, (3) lazy init problem, (4) DCL, (5) two problems without volatile, (6) safest approach.</details>

---

**Q14.** In the PDF's eager init, the field is declared as:

a) `private static Logger instance;`
b) `private static final Logger INSTANCE = new Logger();`
c) `public static Logger INSTANCE;`
d) `private volatile Logger instance;`

<details><summary>Answer</summary>b) `private static final` -- the instance is created immediately and cannot be reassigned.</details>

---

**Q15.** What happens if you try `new Logger()` from outside the class in any of the PDF's implementations?

a) Creates a new Logger
b) Compile error -- constructor is `private`
c) Returns the singleton instance
d) Throws `IllegalAccessException`

<details><summary>Answer</summary>b) Compile error. Every implementation in the PDF starts by making the constructor `private`.</details>

---

**Q16.** In DCL, what do you synchronize on?

a) The instance
b) `this`
c) `Logger.class`
d) A `ReentrantLock`

<details><summary>Answer</summary>c) `synchronized (Logger.class)` -- you sync on the class object since `getInstance()` is static (no `this` available).</details>

---

**Q17.** The PDF's Step 1 (just private constructor) has what problem?

a) Too many instances
b) No one can create even ONE object
c) Thread safety
d) Memory leak

<details><summary>Answer</summary>b) With only a private constructor and nothing else, no code (including the class itself) creates an instance. We need the class to create and expose one.</details>

---

**Q18.** "Correct since Java 5" -- which approach does the PDF say this about?

a) Eager init
b) Holder idiom
c) DCL with volatile
d) Enum

<details><summary>Answer</summary>c) DCL with volatile. The Java Memory Model was fixed in Java 5 to properly support `volatile` semantics.</details>

---

**Q19.** Which Singleton approach requires the MOST code?

a) Eager init
b) Enum
c) DCL with volatile
d) Holder idiom

<details><summary>Answer</summary>c) DCL with volatile -- requires `volatile` field, two `null` checks, a `synchronized` block, and the constructor. The PDF calls it "verbose and harder to read."</details>

---

**Q20.** If you had to pick ONE Singleton approach for a coding exam, which should you write?

a) Eager init (simplest, always correct)
b) DCL with volatile (shows you know threads)
c) Holder idiom (best balance)
d) Depends on the question

<details><summary>Answer</summary>d) Depends, but if the question doesn't specify thread concerns, Holder idiom (c) is the best default -- clean, lazy, thread-safe, no synchronization. If asked specifically about thread safety, write DCL with volatile to show depth.</details>

---

## Self-Scoring

| Score | Level |
|-------|-------|
| 18-20 | Exam ready on Singleton |
| 14-17 | Good, review DCL and Holder |
| 10-13 | Re-read the evolution steps |
| Below 10 | Go back to the PDF |

---

## Coding Problems

### Problem 1: Write the Holder Idiom from Memory

Write a complete `DatabaseConnection` singleton using the Holder idiom. Include:
- Private constructor that prints "Connection created"
- A `query(String sql)` method that prints the SQL
- A `main()` that calls `query()` twice and proves only one instance exists

<details><summary>Solution</summary>

```java
public class DatabaseConnection {
    private DatabaseConnection() {
        System.out.println("Connection created");
    }

    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    public void query(String sql) {
        System.out.println("Executing: " + sql);
    }

    public static void main(String[] args) {
        DatabaseConnection c1 = DatabaseConnection.getInstance();
        DatabaseConnection c2 = DatabaseConnection.getInstance();
        c1.query("SELECT * FROM users");
        c2.query("INSERT INTO logs VALUES(1)");
        System.out.println("Same instance? " + (c1 == c2));  // true
    }
}
// Output:
// Connection created        (only once!)
// Executing: SELECT * FROM users
// Executing: INSERT INTO logs VALUES(1)
// Same instance? true
```
</details>

---

### Problem 2: Fix the Broken DCL

This DCL implementation has bugs. Find and fix them.

```java
public class ConfigManager {
    private static ConfigManager instance;

    public ConfigManager() {}

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
}
```

List every bug and provide the corrected code.

<details><summary>Solution</summary>

**Bugs found:**
1. Constructor is `public` -- anyone can create instances. Must be `private`.
2. `instance` is not `volatile` -- DCL without volatile is broken (reordering + visibility).
3. No `synchronized` block -- this is plain lazy init, not DCL at all.
4. Missing the double-check (second `null` check inside synchronized).

**Corrected:**
```java
public class ConfigManager {
    private static volatile ConfigManager instance;  // FIX: volatile

    private ConfigManager() {}  // FIX: private

    public static ConfigManager getInstance() {
        if (instance == null) {                      // first check (no lock)
            synchronized (ConfigManager.class) {     // FIX: synchronized
                if (instance == null) {              // FIX: second check
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
}
```
</details>

---

### Problem 3: Singleton Evolution -- Convert Between Forms

Given this eager Singleton:

```java
public class Logger {
    private static final Logger INSTANCE = new Logger();
    private Logger() {}
    public static Logger getInstance() { return INSTANCE; }
    public void log(String msg) { System.out.println(msg); }
}
```

**Task A:** Convert to Enum Singleton. Include the `log()` method.

**Task B:** Convert to Holder idiom.

**Task C:** Explain one advantage and one disadvantage of each form vs the original eager init.

<details><summary>Solution</summary>

**Task A -- Enum:**
```java
public enum Logger {
    INSTANCE;

    public void log(String msg) {
        System.out.println(msg);
    }
}
// Usage: Logger.INSTANCE.log("hello");
```

**Task B -- Holder:**
```java
public class Logger {
    private Logger() {}

    private static class Holder {
        private static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() {
        return Holder.INSTANCE;
    }

    public void log(String msg) {
        System.out.println(msg);
    }
}
```

**Task C:**

| Form | Advantage over Eager | Disadvantage vs Eager |
|------|---------------------|----------------------|
| **Enum** | Reflection-proof, serialization-proof | Cannot lazy-load, cannot subclass |
| **Holder** | Lazy loading (instance created only when `getInstance()` first called) | Slightly more complex, not reflection-proof |
| **Eager** (original) | Simplest code, always thread-safe | Wastes memory if instance is never used |

</details>
