# Prototype Design Pattern (with Registry)

**Category:** Creational
**Intent:** Create new objects by copying (cloning) a configured prototype, rather than constructing from scratch. Use a registry to catalog and retrieve prototypes by key.
**Use cases:** Vector graphics editors (cloning shapes), game objects with heavy configuration, plugin systems where new types can be registered at runtime.

---

## Why Prototype?

### The Problem

Sometimes object construction is expensive or repetitive:
- A shape has styles, constraints, handles, nested parts -- setting up all of this every time is costly
- You want to create objects "by example" without knowing their concrete class
- The set of available types can change at runtime (plugins, feature flags)

### BAD: Hard-Coding Constructors Everywhere

```java
// BAD: Client knows all concrete classes and their complex setup
class Canvas {
    Shape placeShape(String type, int x, int y) {
        Shape s;
        if (type.equals("rect")) {
            s = new Rectangle(120, 80,
                    new StrokeStyle(2.0f, false),
                    new FillStyle(0xFF3366CC));
        } else if (type.equals("ellipse")) {
            s = new Ellipse(60, 40,
                    new StrokeStyle(1.5f, true),
                    new FillStyle(0xAA33CC66));
        } else {
            throw new IllegalArgumentException(type);
        }
        s.setPosition(x, y);
        return s;
    }
}
```

**Problems:**
- Client knows every constructor signature and style configuration
- Adding a new shape means editing client code
- Same complex setup repeated every time a shape is placed
- Cannot add shapes at runtime (plugins)

---

## The Solution: Prototype + Registry

### The Insight

1. Keep one **configured exemplar** (prototype) per shape type
2. To create a new instance, **clone** the exemplar
3. Store prototypes in a **registry** (map of key -> prototype)

### GOOD: Prototype with Registry

```java
// --- Prototype contract ---
interface Shape {
    Shape copy();  // deep enough for independence
    void draw(java.awt.Graphics2D g);
    void setPosition(int x, int y);
}

// --- Style value objects ---
final class StrokeStyle {
    final float width;
    final boolean dashed;
    StrokeStyle(float w, boolean d) { this.width = w; this.dashed = d; }
    StrokeStyle copy() { return new StrokeStyle(width, dashed); }
}

final class FillStyle {
    final int rgba;
    FillStyle(int rgba) { this.rgba = rgba; }
    FillStyle copy() { return new FillStyle(rgba); }
}

// --- Concrete prototypes ---
final class Rectangle implements Shape {
    private int x, y, w, h;
    private final StrokeStyle stroke;
    private final FillStyle fill;

    Rectangle(int w, int h, StrokeStyle s, FillStyle f) {
        this.w = w; this.h = h; this.stroke = s; this.fill = f;
    }

    public Shape copy() {
        return new Rectangle(w, h, stroke.copy(), fill.copy());
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void draw(java.awt.Graphics2D g) { /* draw with stroke/fill */ }
    public String toString() {
        return "Rect(" + w + "x" + h + ")@(" + x + "," + y + ")";
    }
}

final class Ellipse implements Shape {
    private int cx, cy, rx, ry;
    private final StrokeStyle stroke;
    private final FillStyle fill;

    Ellipse(int rx, int ry, StrokeStyle s, FillStyle f) {
        this.rx = rx; this.ry = ry; this.stroke = s; this.fill = f;
    }

    public Shape copy() {
        return new Ellipse(rx, ry, stroke.copy(), fill.copy());
    }

    public void setPosition(int x, int y) { this.cx = x; this.cy = y; }
    public void draw(java.awt.Graphics2D g) { /* draw with stroke/fill */ }
}

final class TextBox implements Shape {
    private int x, y;
    private String text;
    private final FillStyle fill;

    TextBox(String text, FillStyle f) { this.text = text; this.fill = f; }

    public Shape copy() { return new TextBox(text, fill.copy()); }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void draw(java.awt.Graphics2D g) { /* draw text */ }
}
```

### The Registry

```java
final class ShapeRegistry {
    private final java.util.Map<String, Shape> store = new java.util.HashMap<>();

    public void register(String key, Shape proto) {
        store.put(key.toLowerCase(), proto);
    }

    public void unregister(String key) {
        store.remove(key.toLowerCase());
    }

    public Shape create(String key) {
        Shape p = store.get(key.toLowerCase());
        if (p == null) throw new IllegalArgumentException("Unknown: " + key);
        return p.copy();  // clone the prototype
    }
}
```

### Client: Tool / Canvas

```java
final class Tool {
    private final ShapeRegistry registry;
    private final String key;

    Tool(ShapeRegistry r, String key) { this.registry = r; this.key = key; }

    public Shape onClick(int x, int y) {
        Shape s = registry.create(key);  // clone from prototype
        s.setPosition(x, y);
        return s;
    }
}

class Demo {
    public static void main(String[] args) {
        ShapeRegistry reg = new ShapeRegistry();

        // Register configured prototypes
        reg.register("rect",    new Rectangle(120, 80,
            new StrokeStyle(2.0f, false), new FillStyle(0xFF3366CC)));
        reg.register("ellipse", new Ellipse(60, 40,
            new StrokeStyle(1.5f, true), new FillStyle(0xAA33CC66)));
        reg.register("text",    new TextBox("Hello",
            new FillStyle(0xFFFFFFFF)));

        Tool rectTool = new Tool(reg, "rect");
        Tool textTool = new Tool(reg, "text");

        System.out.println(rectTool.onClick(100, 100)); // fresh clone
        System.out.println(textTool.onClick(220, 120)); // fresh clone

        // Plugins can add shapes at runtime!
        // reg.register("swimlane", new Swimlane(...));
    }
}
```

---

## Shallow Copy vs Deep Copy

This is the **most important** design decision in Prototype.

| Type | What it copies | When to use |
|------|---------------|-------------|
| **Shallow copy** | Copies field values; reference fields still point to same objects | When referenced objects are **immutable** or safely shared |
| **Deep copy** | Recursively copies all referenced objects | When referenced objects are **mutable** and must be independent |

### BAD: Shallow Copy with Mutable Fields

```java
// BAD: Shallow copy shares mutable style objects
public Shape copy() {
    Rectangle r = new Rectangle(w, h, stroke, fill); // shares same stroke/fill!
    return r;
}
// Modifying the clone's stroke also changes the original's stroke!
```

### GOOD: Deep Copy

```java
// GOOD: Each clone gets its own copy of mutable objects
public Shape copy() {
    return new Rectangle(w, h, stroke.copy(), fill.copy());
}
```

**Exam Tip:** If style objects are **immutable** (`final` fields, no setters), they CAN be shared safely. But if they're mutable, you MUST deep copy.

### Copy Then Initialize

`copy()` has a uniform signature (no parameters). If you need to customize the clone (e.g., set position), do it AFTER copying:

```java
Shape s = registry.create("rect"); // clone
s.setPosition(100, 200);           // then initialize
```

---

## When to Use Prototype vs Factories

| Pattern | Use When |
|---------|----------|
| **Prototype** | Construction is expensive, objects are better created "by example," or the set of types changes at runtime |
| **Factory Method** | You have a fixed algorithm that needs to create objects, and the concrete product varies by subclass |
| **Abstract Factory** | You must create families of related objects that stay consistent |

**Exam Tip:** You can implement an Abstract Factory using a registry of prototypes -- each creation method returns a clone from the registry.

---

## Roles in Prototype Pattern

| Role | Class | Responsibility |
|------|-------|---------------|
| **Prototype** | `Shape` interface | Declares `copy()` |
| **Concrete Prototype** | `Rectangle`, `Ellipse`, `TextBox` | Implements `copy()` (deep enough for independence) |
| **Registry** | `ShapeRegistry` | Maps keys to prototypes, returns clones |
| **Client** | `Tool`, `Canvas` | Asks registry for clones, positions them |

---

## SOLID Connection

| Principle | How Prototype Relates |
|-----------|----------------------|
| **SRP** | Each concrete prototype handles its own cloning; registry handles lookup |
| **OCP** | New shapes can be added by registering a new prototype -- no client code changes |
| **DIP** | Client depends on `Shape` interface, not on concrete classes |
| **LSP** | All prototypes implement `copy()` with the same contract |

---

## Big Picture

- Prototype is a **Creational** pattern focused on **cloning**
- The Registry makes it a powerful **extensible** creation mechanism (add/remove types at runtime)
- Fewer subclasses: different configurations become different registered prototypes, not new classes
- Contrast with **Builder**: Builder constructs step-by-step from scratch; Prototype copies an existing configured object
- Contrast with **Factory**: Factory creates via `new`; Prototype creates via `copy()`

---

## Exam Tips (Quick Recall)

1. **Prototype** = create by cloning a configured exemplar
2. **Registry** = map of key -> prototype, returns clones via `copy()`
3. **Deep copy** mutable fields; immutable fields can be shared
4. `copy()` has NO parameters -- customize AFTER cloning (setters)
5. Avoids subclass explosion: variations = different registered prototypes
6. Runtime extensibility: plugins register new prototypes on load
7. Java's `Cloneable`/`clone()` exists but is considered broken -- prefer explicit `copy()` methods

---

## Viva Questions

**Q1: What is the Prototype pattern?**
A creational pattern where new objects are created by copying (cloning) an existing configured object (the prototype) rather than constructing from scratch. A registry maps keys to prototypes and returns clones on request.

**Q2: When should you use Prototype?**
When construction is expensive (complex setup, loaded resources), when you want to create objects "by example" without knowing their concrete class, or when the set of available types changes at runtime (plugins).

**Q3: What is the difference between shallow and deep copy?**
Shallow copy copies field values directly -- reference fields point to the SAME objects. Deep copy recursively copies referenced objects so the clone is fully independent. Use deep copy when referenced objects are mutable.

**Q4: Why use an explicit `copy()` instead of Java's `Cloneable` interface?**
Java's `clone()` mechanism is widely considered broken: it requires `Cloneable` marker interface, `clone()` returns `Object` (needs casting), default behavior is shallow copy (easy to get wrong), and it bypasses constructors. An explicit `copy()` is type-safe, clear, and under your control.

**Q5: What is the Prototype Registry?**
A map (e.g., `HashMap<String, Shape>`) that stores prototype instances by key. When asked for a new instance, it looks up the prototype and calls `copy()`. This decouples creation from concrete types and supports runtime extensibility.

**Q6: How does Prototype follow OCP?**
New types can be added by registering a new prototype in the registry. No existing client code or registry code needs modification. The system is open for extension (new prototypes) and closed for modification.

**Q7: What is "copy then initialize"?**
Since `copy()` has a fixed signature (no parameters), you clone the prototype first, then customize the clone via setters (e.g., `setPosition()`). The prototype holds default configuration; the client applies instance-specific changes after cloning.

**Q8: How is Prototype different from Factory?**
Factory creates objects via `new` (possibly with a switch on type). Prototype creates objects via `copy()` on an existing instance. Prototype is better when construction is expensive or the set of types is dynamic.

**Q9: How does Prototype reduce the number of subclasses?**
Instead of creating a subclass for every variation (e.g., `BlueRectangle`, `RedRectangle`), you create ONE Rectangle class and register multiple configured prototypes with different styles. Variations are data, not code.

**Q10: Can Prototype be combined with Factory?**
Yes. A factory can internally use a prototype registry: instead of `new ConcreteProduct()`, it does `registry.get(key).copy()`. This gives you factory's clean API with prototype's cloning efficiency.

---

## MCQ Quiz

**1. Prototype pattern creates new objects by:**
a) Calling constructors with parameters
b) Cloning an existing configured instance
c) Using reflection
d) Deserializing from a file

<details><summary>Answer</summary>b) Cloning an existing configured instance</details>

**2. The Prototype Registry stores:**
a) Class names as strings
b) Constructor references
c) Prototype instances mapped to keys
d) Serialized objects

<details><summary>Answer</summary>c) Prototype instances mapped to keys</details>

**3. What does `registry.create("rect")` do?**
a) Returns the stored prototype directly
b) Looks up the prototype and returns a CLONE of it
c) Creates a new Rectangle using `new`
d) Returns null if not found

<details><summary>Answer</summary>b) Looks up the prototype and returns a CLONE of it (calls `copy()` on the stored prototype)</details>

**4. Shallow copy is safe when referenced objects are:**
a) Mutable
b) Immutable
c) Static
d) Transient

<details><summary>Answer</summary>b) Immutable -- they can't be changed, so sharing is safe</details>

**5. Deep copy is needed when:**
a) All fields are primitive
b) Referenced objects are mutable and must be independent
c) The object has no fields
d) The object implements Serializable

<details><summary>Answer</summary>b) Referenced objects are mutable and must be independent</details>

**6. Why is Java's `Cloneable`/`clone()` considered problematic?**
a) It's too fast
b) It returns Object (needs casting), does shallow copy by default, bypasses constructors
c) It only works with arrays
d) It was removed in Java 8

<details><summary>Answer</summary>b) It returns Object, does shallow copy by default, and bypasses constructors</details>

**7. How do you customize a cloned object if `copy()` takes no parameters?**
a) Override `copy()` with parameters
b) Clone first, then use setters to initialize (copy-then-initialize)
c) Pass parameters to the registry
d) Use reflection

<details><summary>Answer</summary>b) Clone first, then use setters to initialize</details>

**8. Prototype reduces subclass explosion because:**
a) It uses generics
b) Variations become different registered prototypes, not new classes
c) It uses abstract classes
d) It eliminates the need for interfaces

<details><summary>Answer</summary>b) Variations become different registered prototypes, not new classes</details>

**9. Which is NOT a role in the Prototype pattern?**
a) Prototype (interface with `copy()`)
b) Concrete Prototype (implements `copy()`)
c) Registry (maps keys to prototypes)
d) Director (orchestrates construction steps)

<details><summary>Answer</summary>d) Director -- that's a role in the Builder pattern, not Prototype</details>

**10. Prototype supports runtime extensibility because:**
a) New types require recompilation
b) Plugins can register new prototypes at runtime without code changes
c) It uses dynamic proxies
d) It requires a restart

<details><summary>Answer</summary>b) Plugins can register new prototypes at runtime without code changes</details>

**11. What does the client need to know to create a shape using Prototype?**
a) The concrete class name
b) The full constructor signature
c) Only the registry key (e.g., "rect")
d) The internal state of the prototype

<details><summary>Answer</summary>c) Only the registry key -- the client is decoupled from concrete types</details>

**12. In the Shape example, `StrokeStyle.copy()` creates:**
a) A shallow copy
b) A deep copy of the stroke style
c) A reference to the same object
d) A null value

<details><summary>Answer</summary>b) A deep copy -- `new StrokeStyle(width, dashed)` creates an independent instance</details>

**13. Which pattern family does Prototype belong to?**
a) Structural
b) Behavioral
c) Creational
d) Concurrency

<details><summary>Answer</summary>c) Creational</details>

**14. If the prototype's `copy()` method does a shallow copy of a mutable list field, what happens?**
a) The clone and original share the same list -- modifying one affects the other
b) The clone gets an empty list
c) An exception is thrown
d) The list is automatically deep copied by Java

<details><summary>Answer</summary>a) The clone and original share the same list -- modifying one affects the other (this is a bug)</details>

**15. How is Prototype different from Builder?**
a) Builder copies existing objects; Prototype constructs step-by-step
b) Prototype copies an existing object; Builder constructs from scratch step-by-step
c) They are the same pattern
d) Prototype is structural; Builder is behavioral

<details><summary>Answer</summary>b) Prototype copies an existing object; Builder constructs from scratch step-by-step</details>

**16. `unregister("rect")` in the registry:**
a) Deletes the Rectangle class
b) Removes the prototype mapping so no more "rect" clones can be created
c) Destroys all existing Rectangle instances
d) Throws an exception

<details><summary>Answer</summary>b) Removes the prototype mapping -- future `create("rect")` calls will fail</details>

**17. You can implement Abstract Factory using prototypes by:**
a) Having each factory method `new` a product
b) Having each factory method clone a product from a prototype registry
c) Using inheritance only
d) Using Singleton

<details><summary>Answer</summary>b) Having each factory method clone a product from a prototype registry</details>

**18. The `copy()` method is defined in:**
a) The client
b) The registry
c) The Prototype interface (e.g., `Shape`)
d) A static utility class

<details><summary>Answer</summary>c) The Prototype interface</details>

**19. What principle ensures all concrete prototypes can be used interchangeably?**
a) SRP
b) LSP (Liskov Substitution)
c) ISP
d) DRY

<details><summary>Answer</summary>b) LSP -- all implementations of `Shape.copy()` must honor the same contract</details>

**20. When is Prototype LESS useful than Factory?**
a) When construction is cheap and types are fixed at compile time
b) When types change at runtime
c) When objects have complex configuration
d) When you need to copy objects

<details><summary>Answer</summary>a) When construction is cheap and types are fixed, a simple factory or `new` is simpler</details>

### Scoring
- **18-20:** Prototype mastered.
- **14-17:** Good. Review shallow vs deep copy.
- **10-13:** Revisit the Registry concept.
- **Below 10:** Re-read the full notes.

---

## Coding Exam Questions

### Problem 1: Fix the Broken Clone

```java
class Document implements Cloneable {
    String title;
    List<String> pages;  // mutable

    Document(String title, List<String> pages) {
        this.title = title;
        this.pages = pages;
    }

    public Document copy() {
        return new Document(title, pages);  // BROKEN: why?
    }
}
```

**Task:** Explain why the `copy()` is broken and fix it.

<details><summary>Solution</summary>

**Bug:** `pages` is a mutable `List<String>`. The copy shares the SAME list reference as the original. Modifying the clone's pages also modifies the original's pages.

**Fix:**
```java
class Document {
    final String title;
    final List<String> pages;

    Document(String title, List<String> pages) {
        this.title = title;
        this.pages = List.copyOf(pages);  // defensive copy in constructor too
    }

    public Document copy() {
        return new Document(title, new ArrayList<>(pages));  // deep copy the list
    }
}
```

Now each clone has its own independent list.
</details>

---

### Problem 2: Build a Prototype Registry for Game Characters

Design a character creation system for a game:
- `GameCharacter` interface with `copy()`, `setName(String)`, `describe()`
- Concrete types: `Warrior` (has `int armor`), `Mage` (has `int mana`)
- `CharacterRegistry` that stores prototypes and returns clones
- Demonstrate registering prototypes and creating characters

<details><summary>Solution</summary>

```java
import java.util.*;

interface GameCharacter {
    GameCharacter copy();
    void setName(String name);
    String describe();
}

class Warrior implements GameCharacter {
    private String name;
    private int armor;

    Warrior(int armor) { this.armor = armor; }

    public GameCharacter copy() { return new Warrior(armor); }
    public void setName(String name) { this.name = name; }
    public String describe() { return name + " [Warrior, armor=" + armor + "]"; }
}

class Mage implements GameCharacter {
    private String name;
    private int mana;

    Mage(int mana) { this.mana = mana; }

    public GameCharacter copy() { return new Mage(mana); }
    public void setName(String name) { this.name = name; }
    public String describe() { return name + " [Mage, mana=" + mana + "]"; }
}

class CharacterRegistry {
    private final Map<String, GameCharacter> store = new HashMap<>();

    void register(String key, GameCharacter proto) {
        store.put(key.toLowerCase(), proto);
    }

    GameCharacter create(String key) {
        GameCharacter p = store.get(key.toLowerCase());
        if (p == null) throw new IllegalArgumentException("Unknown: " + key);
        return p.copy();
    }
}

class GameDemo {
    public static void main(String[] args) {
        CharacterRegistry reg = new CharacterRegistry();
        reg.register("warrior", new Warrior(50));
        reg.register("mage", new Mage(100));

        GameCharacter c1 = reg.create("warrior");
        c1.setName("Aragorn");

        GameCharacter c2 = reg.create("mage");
        c2.setName("Gandalf");

        GameCharacter c3 = reg.create("warrior");
        c3.setName("Boromir");

        System.out.println(c1.describe()); // Aragorn [Warrior, armor=50]
        System.out.println(c2.describe()); // Gandalf [Mage, mana=100]
        System.out.println(c3.describe()); // Boromir [Warrior, armor=50]
    }
}
```
</details>

---

### Problem 3: Prototype vs Factory -- Refactor

The following code uses a Factory with expensive construction. Refactor it to use Prototype for efficiency.

```java
class ConfigFactory {
    static DatabaseConfig create() {
        // Expensive: reads from file, validates, sets 20+ fields
        Properties props = loadFromFile("db.properties"); // slow I/O
        return new DatabaseConfig(
            props.getProperty("host"),
            Integer.parseInt(props.getProperty("port")),
            props.getProperty("user"),
            // ... 17 more fields
        );
    }
}

// Called 100 times in a loop:
for (int i = 0; i < 100; i++) {
    DatabaseConfig c = ConfigFactory.create(); // reads file 100 times!
    c.setConnectionName("conn-" + i);
    pool.add(c);
}
```

<details><summary>Solution</summary>

```java
class DatabaseConfig {
    private String host;
    private int port;
    private String user;
    private String connectionName;
    // ... other fields

    DatabaseConfig(String host, int port, String user) {
        this.host = host;
        this.port = port;
        this.user = user;
    }

    public DatabaseConfig copy() {
        DatabaseConfig c = new DatabaseConfig(host, port, user);
        // copy all other fields too
        return c;
    }

    public void setConnectionName(String name) {
        this.connectionName = name;
    }
}

// Setup: create ONE prototype by reading the file ONCE
Properties props = loadFromFile("db.properties");
DatabaseConfig prototype = new DatabaseConfig(
    props.getProperty("host"),
    Integer.parseInt(props.getProperty("port")),
    props.getProperty("user")
);

// Now clone 100 times -- no file I/O!
for (int i = 0; i < 100; i++) {
    DatabaseConfig c = prototype.copy();  // fast clone
    c.setConnectionName("conn-" + i);
    pool.add(c);
}
```

**Key improvement:** File I/O happens ONCE. All 100 configs are cloned from the prototype. Massive performance gain.
</details>
