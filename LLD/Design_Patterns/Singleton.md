# Singleton Design Pattern

**Category:** Creational
**Intent:** Ensure exactly one instance of a class exists and provide a global access point.
**Use cases:** Logging service, configuration manager, feature flag provider, connection pool manager, metrics reporter.

---

## Why Singleton?

**Problem:** Some classes should have exactly ONE instance in the entire application. If multiple instances exist, they could cause inconsistent state (e.g., two loggers writing to different files, two config managers with different settings).

**What we need:**
- Prevent external code from creating multiple objects
- Provide a single, globally accessible instance
- Thread safety in multi-threaded environments

---

## Evolution of Singleton (BAD to GOOD)

### Step 1: Restrict Object Creation

**The idea:** Make the constructor `private` so nobody outside can call `new`.

```java
public class Logger {
    private Logger() {}  // nobody can do: new Logger()
}
```

**Problem:** Now nobody can create even ONE object. We need the class itself to create and expose the instance.

---

### Step 2: Eager Initialization (Simple but Wasteful)

```java
public class Logger {
    private static final Logger INSTANCE = new Logger();

    private Logger() {}

    public static Logger getInstance() {
        return INSTANCE;
    }
}
```

**How it works:** The instance is created when the class is loaded, before anyone calls `getInstance()`.

**Exam Tip:** This is called **eager initialization** because the object is created eagerly (immediately), not on demand.

**Problem:** The instance is created even if it's never used. If the constructor does heavy work (DB connections, file I/O), this wastes resources.

---

### Step 3: Lazy Initialization (BAD -- Not Thread-Safe)

```java
// BAD: Not thread-safe
public class Logger {
    private static Logger instance;

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
}
```

**Problem:** Two threads can both see `instance == null` at the same time, both enter the `if` block, and create TWO objects. Singleton is broken.

**Exam Tip:** If an examiner asks "what's wrong with lazy singleton?" -- the answer is always **thread safety**.

---

### Step 4: Synchronized Method (Correct but Slow)

```java
// CORRECT but SLOW
public class Logger {
    private static Logger instance;

    private Logger() {}

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
}
```

**Problem:** `synchronized` locks the entire method on EVERY call. After the first call, the instance already exists -- the lock is unnecessary overhead. Every thread waits in line even just to read the reference.

---

### Step 5: Double-Checked Locking -- WITHOUT volatile (BROKEN)

```java
// BROKEN: Missing volatile
public class Logger {
    private static Logger instance;  // no volatile!

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {              // First check (no lock)
            synchronized (Logger.class) {
                if (instance == null) {      // Second check (with lock)
                    instance = new Logger();
                }
            }
        }
        return instance;
    }
}
```

**Why this is BROKEN without `volatile`:**

`new Logger()` is NOT atomic. The JVM does 3 things:
1. **Allocate memory**
2. **Initialize the object** (run constructor)
3. **Assign reference** to the variable

Without `volatile`, the JVM can **reorder** steps 2 and 3. So another thread might see a non-null reference pointing to a **half-constructed object**.

Also: without `volatile`, one thread's write to `instance` might stay in its CPU cache and not be visible to other threads.

**Exam Tip:** This is a TOP viva question. Know both reasons: **instruction reordering** and **cache visibility**.

---

### Step 6: Double-Checked Locking -- WITH volatile (CORRECT)

```java
// CORRECT: Thread-safe and efficient
public class Logger {
    private static volatile Logger instance;

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }
}
```

**Why `volatile` fixes it:**
- **Visibility:** All writes are immediately visible to all threads (no CPU cache staleness)
- **Ordering:** Establishes a happens-before relationship -- object construction completes BEFORE reference assignment

**Exam Tip:** Works correctly since **Java 5** (when the Java Memory Model was fixed).

---

### Step 7: Static Inner Class / Holder Idiom (BEST for most cases)

```java
public class Logger {
    private Logger() {}

    private static class Holder {
        private static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() {
        return Holder.INSTANCE;
    }
}
```

**Why this is elegant:**
- **Lazy:** `Holder` class is not loaded until `getInstance()` is called
- **Thread-safe:** JVM guarantees class loading is thread-safe (only one thread initializes a class)
- **No synchronization needed:** No `volatile`, no `synchronized`
- **Clean:** Simple, readable code

**Exam Tip:** This is the **recommended approach** for most singleton scenarios. Know the JVM classloading guarantee.

---

### Step 8: Enum Singleton (Safest Overall)

```java
public enum Logger {
    INSTANCE;

    public void log(String msg) {
        System.out.println(msg);
    }
}

// Usage:
Logger.INSTANCE.log("Server started");
```

**Why this is the safest:**
- **Reflection-proof:** JVM prevents reflective instantiation of enums
- **Serialization-proof:** Enum serialization is handled by JVM (always returns same instance)
- **Thread-safe:** Enum constants are initialized once by the JVM

**Limitations:** No lazy initialization, cannot extend a class (enums already extend `Enum`).

---

## Comparison Table

| Approach | Lazy? | Thread-Safe? | Reflection-Safe? | Serialization-Safe? | Complexity |
|----------|-------|-------------|------------------|---------------------|------------|
| Eager init | No | Yes | No | No | Low |
| Lazy (no sync) | Yes | **No** | No | No | Low |
| Synchronized method | Yes | Yes | No | No | Low |
| DCL without volatile | Yes | **No** | No | No | Medium |
| DCL with volatile | Yes | Yes | No | No | Medium |
| Holder idiom | Yes | Yes | No | No | Low |
| Enum | No | Yes | **Yes** | **Yes** | Low |

**Exam Tip:** Memorize this table. It's a common MCQ and viva topic.

---

## Special Concerns

### Breaking Singleton with Reflection
```java
// BAD: This breaks non-enum singletons
Constructor<Logger> c = Logger.class.getDeclaredConstructor();
c.setAccessible(true);
Logger second = c.newInstance();  // a SECOND instance!
```
**Fix:** Either use enum, or add a check in the private constructor:
```java
private Logger() {
    if (Holder.INSTANCE != null) {
        throw new RuntimeException("Use getInstance()");
    }
}
```

### Breaking Singleton with Serialization
When you deserialize, Java creates a NEW object by default.
**Fix:** Add `readResolve()`:
```java
private Object readResolve() {
    return getInstance();
}
```

### Testing Singletons
Singletons are hard to mock. **Prefer dependency injection** if testability matters -- inject the singleton instance rather than calling `getInstance()` everywhere.

---

## SOLID Connection

| Principle | How Singleton Relates |
|-----------|----------------------|
| **SRP** | Singleton controls its own lifecycle (creation + access) -- some argue this is a second responsibility |
| **OCP** | Enum singletons are closed for modification. Holder idiom is open for extension |
| **DIP** | Calling `Logger.getInstance()` everywhere couples code to the concrete class. Better: inject via constructor |

---

## Big Picture

- Singleton is the simplest **Creational** pattern
- It controls **object creation** by limiting it to exactly one instance
- Often combined with **Factory** (a factory can be a singleton)
- Related to **Flyweight** (both manage shared instances, but Flyweight has multiple shared instances)

---

## Exam Tips (Quick Recall)

1. **Private constructor** = prevent external `new`
2. **Eager init** = simple but wastes memory if unused
3. **Lazy without sync** = broken in multithreaded code
4. **DCL needs volatile** = prevents instruction reordering + ensures visibility
5. **Holder idiom** = best lazy + thread-safe approach (JVM classloading guarantee)
6. **Enum** = safest (reflection + serialization proof), but not lazy
7. **volatile keyword** does two things: **visibility** and **ordering**

---

## Viva Questions

**Q1: How do you make a class Singleton?**
Make the constructor private, hold one static instance inside the class, and expose it via a public static `getInstance()` method. The caller can never use `new` -- they must go through `getInstance()`.

**Q2: What problem does eager initialization cause?**
The instance is created when the class loads, even if it's never used. If the constructor is expensive (DB connection, file loading), this wastes resources and slows startup.

**Q3: Why is lazy initialization without synchronization unsafe?**
Two threads can simultaneously see `instance == null`, both enter the creation block, and construct two separate objects. The singleton guarantee is broken.

**Q4: What is Double-Checked Locking? How do you implement it?**
DCL checks `instance == null` twice: once without a lock (fast path for already-initialized case), and once inside a `synchronized` block (to prevent race conditions during first creation). The instance field must be `volatile` to prevent instruction reordering and ensure visibility.

**Q5: What two problems arise without `volatile` in DCL?**
1. **Instruction reordering:** The JVM may assign the reference before the constructor finishes, so another thread sees a half-constructed object.
2. **Cache visibility:** One thread's write may stay in CPU cache and not be visible to other threads reading the field.

**Q6: Which Singleton approach is the safest overall, and why?**
The **Enum Singleton**. The JVM guarantees that enum constants are instantiated exactly once, cannot be created via reflection, and serialization always returns the same instance. It's the only approach that's immune to all three attack vectors (multi-threading, reflection, serialization).

**Q7: What is the Holder idiom and why is it preferred?**
A static inner class holds the singleton instance. The inner class is not loaded until `getInstance()` is called (lazy), and the JVM guarantees thread-safe class initialization (no synchronization needed). It gives you lazy + thread-safe + clean code.

**Q8: How can reflection break a Singleton?**
By calling `Constructor.setAccessible(true)` on the private constructor, you can bypass access checks and create a second instance. Fix: use enum, or throw an exception in the constructor if an instance already exists.

**Q9: How can serialization break a Singleton?**
Deserialization creates a new object by default. Fix: implement `readResolve()` to return the existing instance, or use an enum (which handles this automatically).

**Q10: Why are Singletons considered an anti-pattern by some?**
They introduce global state, make unit testing harder (can't easily mock), hide dependencies (callers use `getInstance()` instead of constructor injection), and violate SRP (the class manages its own lifecycle). Dependency injection is often preferred.

---

## MCQ Quiz

**1. What is the primary purpose of the Singleton pattern?**
a) To create multiple instances efficiently
b) To ensure a class has exactly one instance with a global access point
c) To separate object construction from representation
d) To clone existing objects

<details><summary>Answer</summary>b) To ensure a class has exactly one instance with a global access point</details>

**2. Which keyword prevents external instantiation in a Singleton?**
a) `static`
b) `final`
c) `private` (on the constructor)
d) `volatile`

<details><summary>Answer</summary>c) `private` (on the constructor)</details>

**3. What is the main drawback of eager initialization?**
a) It's not thread-safe
b) It creates the instance even if never used
c) It requires synchronization
d) It's vulnerable to reflection

<details><summary>Answer</summary>b) It creates the instance even if never used</details>

**4. Why is lazy initialization (without synchronization) broken in multithreaded code?**
a) The constructor throws an exception
b) Two threads can both see null and create two instances
c) The JVM doesn't support lazy initialization
d) The static field is not visible across threads

<details><summary>Answer</summary>b) Two threads can both see null and create two instances</details>

**5. In DCL, why is the second null check (inside synchronized) needed?**
a) For performance optimization
b) Because a thread that waited for the lock may find the instance was already created by the thread that held the lock
c) To handle exceptions
d) To satisfy the compiler

<details><summary>Answer</summary>b) Because a thread that waited for the lock may find the instance was already created by the thread that held the lock</details>

**6. What does `volatile` prevent in DCL?**
a) Garbage collection of the instance
b) Instruction reordering and cache visibility issues
c) Multiple class loading
d) Reflection attacks

<details><summary>Answer</summary>b) Instruction reordering and cache visibility issues</details>

**7. DCL with volatile works correctly since which Java version?**
a) Java 1.0
b) Java 3
c) Java 5
d) Java 8

<details><summary>Answer</summary>c) Java 5</details>

**8. The Holder idiom relies on which JVM guarantee?**
a) Garbage collection ordering
b) Thread-safe class initialization
c) Static fields are always volatile
d) Inner classes are loaded eagerly

<details><summary>Answer</summary>b) Thread-safe class initialization</details>

**9. Which Singleton approach is immune to reflection attacks?**
a) Eager initialization
b) DCL with volatile
c) Holder idiom
d) Enum Singleton

<details><summary>Answer</summary>d) Enum Singleton</details>

**10. What method must you implement to prevent serialization from breaking a non-enum Singleton?**
a) `writeObject()`
b) `readResolve()`
c) `clone()`
d) `finalize()`

<details><summary>Answer</summary>b) `readResolve()`</details>

**11. Which is NOT a valid concern about the Singleton pattern?**
a) It introduces global state
b) It makes unit testing harder
c) It causes memory leaks
d) It hides dependencies

<details><summary>Answer</summary>c) It causes memory leaks (Singleton itself does not inherently cause memory leaks)</details>

**12. In `synchronized(Logger.class)`, what is being used as the lock?**
a) The Logger instance
b) The Logger class object (the Class<Logger> object)
c) The current thread
d) The JVM

<details><summary>Answer</summary>b) The Logger class object (the Class&lt;Logger&gt; object)</details>

**13. What happens if you remove the first null check in DCL (keeping only the one inside synchronized)?**
a) It breaks thread safety
b) It still works but loses the performance benefit (every call enters synchronized)
c) It causes a NullPointerException
d) It creates multiple instances

<details><summary>Answer</summary>b) It still works but loses the performance benefit (every call enters synchronized)</details>

**14. An enum Singleton cannot:**
a) Have methods
b) Have fields
c) Extend another class
d) Implement an interface

<details><summary>Answer</summary>c) Extend another class (enums implicitly extend java.lang.Enum)</details>

**15. Which approach gives BOTH lazy initialization AND thread safety with the LEAST code complexity?**
a) DCL with volatile
b) Synchronized getInstance()
c) Holder idiom (static inner class)
d) Eager initialization

<details><summary>Answer</summary>c) Holder idiom (static inner class)</details>

**16. `new Logger()` internally involves three steps. What are they?**
a) Compile, link, execute
b) Allocate memory, initialize object, assign reference
c) Load class, verify bytecode, create object
d) Parse, validate, construct

<details><summary>Answer</summary>b) Allocate memory, initialize object, assign reference</details>

**17. Without volatile, which steps can be reordered?**
a) Allocate and initialize
b) Initialize and assign reference
c) All three steps
d) None, Java guarantees order

<details><summary>Answer</summary>b) Initialize and assign reference (the JVM may assign the reference before initialization completes)</details>

**18. What is the best Singleton approach when you need lazy init, thread safety, AND protection against reflection/serialization?**
a) Holder idiom
b) DCL with volatile
c) Enum (if lazy init is not required)
d) There is no single approach that handles all three perfectly

<details><summary>Answer</summary>c) Enum -- it handles thread safety, reflection, and serialization. Note: it's not lazy, but it's the safest overall.</details>

**19. Why can't you subclass a Singleton easily?**
a) Because the constructor is private, subclasses can't call `super()`
b) Because static methods can't be overridden
c) Because Java doesn't support inheritance
d) Because the instance is final

<details><summary>Answer</summary>a) Because the constructor is private, subclasses can't call `super()`</details>

**20. Which is the correct order from LEAST safe to MOST safe?**
a) Eager < Lazy < DCL+volatile < Holder < Enum
b) Lazy < Eager < Holder < DCL+volatile < Enum
c) Lazy (no sync) < DCL (no volatile) < Eager < Synchronized < DCL+volatile < Holder < Enum
d) All approaches are equally safe

<details><summary>Answer</summary>c) Lazy (no sync) < DCL (no volatile) < Eager < Synchronized < DCL+volatile < Holder < Enum</details>

### Scoring
- **18-20:** You know Singleton cold. Move on.
- **14-17:** Good grasp. Review the volatile/DCL section.
- **10-13:** Revisit thread safety concepts.
- **Below 10:** Re-read the notes from Step 3 onward.

---

## Coding Exam Questions

### Problem 1: Fix the Broken Singleton

The following Singleton is broken. Identify ALL issues and fix them.

```java
public class ConfigManager {
    private static ConfigManager instance;

    public ConfigManager() { }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getConfig(String key) {
        return System.getProperty(key);
    }
}
```

**Task:** Identify every violation of proper Singleton and fix the code.

<details><summary>Solution</summary>

**Issues found:**
1. Constructor is `public` -- anyone can do `new ConfigManager()`
2. No thread safety -- two threads can create two instances
3. `instance` field is not `volatile`

**Fixed version (DCL approach):**
```java
public class ConfigManager {
    private static volatile ConfigManager instance;

    private ConfigManager() { }  // FIXED: private

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {  // FIXED: double check
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public String getConfig(String key) {
        return System.getProperty(key);
    }
}
```

**Or even better (Holder idiom):**
```java
public class ConfigManager {
    private ConfigManager() { }

    private static class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    public String getConfig(String key) {
        return System.getProperty(key);
    }
}
```
</details>

---

### Problem 2: Thread-Safe Connection Pool

Design a thread-safe `ConnectionPool` singleton that:
- Holds a pool of max 5 connections (use a `List<String>` to simulate)
- Has `getConnection()` that returns a connection from the pool
- Has `releaseConnection(String conn)` that returns it to the pool
- Uses the Holder idiom

```java
// Write your solution here
```

<details><summary>Solution</summary>

```java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ConnectionPool {
    private static final int MAX_SIZE = 5;
    private final Queue<String> available = new LinkedList<>();
    private final List<String> inUse = new ArrayList<>();

    private ConnectionPool() {
        for (int i = 1; i <= MAX_SIZE; i++) {
            available.add("Connection-" + i);
        }
    }

    private static class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }

    public static ConnectionPool getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized String getConnection() {
        if (available.isEmpty()) {
            throw new RuntimeException("No connections available");
        }
        String conn = available.poll();
        inUse.add(conn);
        return conn;
    }

    public synchronized void releaseConnection(String conn) {
        if (inUse.remove(conn)) {
            available.offer(conn);
        }
    }

    public synchronized int availableCount() {
        return available.size();
    }
}
```

**Key points:**
- Holder idiom for singleton (lazy + thread-safe)
- `synchronized` on `getConnection`/`releaseConnection` for thread-safe pool access
- Private constructor initializes the pool
</details>

---

### Problem 3: Enum Singleton with State

Create an enum-based `AppRegistry` singleton that:
- Stores key-value pairs (`Map<String, String>`)
- Has `register(key, value)`, `lookup(key)`, and `deregister(key)` methods
- Demonstrate that two references to `AppRegistry.INSTANCE` are the same object

```java
// Write your solution here
```

<details><summary>Solution</summary>

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

public enum AppRegistry {
    INSTANCE;

    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void register(String key, String value) {
        store.put(key, value);
    }

    public Optional<String> lookup(String key) {
        return Optional.ofNullable(store.get(key));
    }

    public void deregister(String key) {
        store.remove(key);
    }

    public int size() {
        return store.size();
    }
}

// Demo
class RegistryDemo {
    public static void main(String[] args) {
        AppRegistry r1 = AppRegistry.INSTANCE;
        AppRegistry r2 = AppRegistry.INSTANCE;

        r1.register("db.host", "localhost");
        System.out.println(r2.lookup("db.host"));  // Optional[localhost]
        System.out.println(r1 == r2);               // true -- same instance

        r2.deregister("db.host");
        System.out.println(r1.lookup("db.host"));  // Optional.empty
    }
}
```

**Key points:**
- `ConcurrentHashMap` for thread-safe storage
- `Optional` return type for lookups (avoids null)
- `r1 == r2` proves it's the same instance
</details>
