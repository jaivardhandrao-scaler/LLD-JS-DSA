# Factory Design Pattern (Simple Factory + Factory Method)

**Category:** Creational
**Intent:** Centralize and abstract object creation so clients don't need to know concrete class names.

This note covers TWO related concepts:
1. **Simple Factory** -- a utility that centralizes `new` logic (technically a technique, not a GoF pattern)
2. **Factory Method** -- a GoF pattern where a superclass algorithm defers creation to subclasses

---

## Part A: Simple Factory

### The Problem

Concrete `new` calls are scattered everywhere:

### BAD: Scattered `new` in Client Code

```java
// BAD: Client knows every concrete class
class GameLogic {
    Stone spawnStone(String type) {
        if (type.equals("SMALL"))  return new SmallStone();
        if (type.equals("MEDIUM")) return new MediumStone();
        if (type.equals("LARGE"))  return new LargeStone();
        throw new IllegalArgumentException("Unknown: " + type);
    }
}
```

**Problems:**
- **Scattered:** Every class that creates stones has this same if/else
- **OCP violation:** Adding `GiantStone` means editing every file that creates stones
- **Coupling:** Client code knows every concrete class

### GOOD: Simple Factory

Move ALL `new` logic into one place:

```java
// Stone domain
enum StoneType { SMALL, MEDIUM, LARGE }

interface Stone {
    String size();
    int damage();
    double weight();
}

final class SmallStone implements Stone {
    public String size()    { return "SMALL"; }
    public int damage()     { return 5; }
    public double weight()  { return 1.0; }
}
final class MediumStone implements Stone {
    public String size()    { return "MEDIUM"; }
    public int damage()     { return 10; }
    public double weight()  { return 2.5; }
}
final class LargeStone implements Stone {
    public String size()    { return "LARGE"; }
    public int damage()     { return 18; }
    public double weight()  { return 4.0; }
}

// Simple Factory -- one place for all creation logic
final class StoneFactory {
    private StoneFactory() {}

    public static Stone create(StoneType t) {
        return switch (t) {
            case SMALL  -> new SmallStone();
            case MEDIUM -> new MediumStone();
            case LARGE  -> new LargeStone();
        };
    }
}

// Client -- clean, decoupled from concretes
class GameLogic {
    Stone spawnOne(StoneType t) {
        return StoneFactory.create(t);  // no concrete class names here
    }
}
```

**Benefits:**
- Centralizes `new` -- one edit point for new types
- Hides concrete classes from callers
- Client depends only on `Stone` interface

**Limitations:**
- Factory grows a big switch as types increase
- Cannot vary creation **policy** (e.g., random vs equalized selection)

**Exam Tip:** Simple Factory is NOT a GoF design pattern. It's a useful **technique**. The examiner may ask you to distinguish it from Factory Method.

---

## Part B: Factory Method Pattern

### New Requirement

Now we need DIFFERENT creation policies:
- **Random spawner:** picks a random stone type each time
- **Equalized spawner:** round-robins through S, M, L equally

The wave-generation **algorithm** stays the same -- only the stone-selection logic varies.

### The Insight

Put the **fixed algorithm** in a base class. Inside that algorithm, call an **overridable factory method** that subclasses implement differently.

### GOOD: Factory Method

```java
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// Creator: owns the algorithm, defers instantiation to subclasses
abstract class StoneSpawner {

    // Factory Method -- subclasses decide which Stone to create
    protected abstract Stone createStone();

    // Fixed algorithm that uses the factory method
    public final List<Stone> generateWave(int count) {
        List<Stone> wave = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Stone s = createStone();  // polymorphic creation
            wave.add(s);
        }
        return wave;
    }
}

// Concrete Creator A: random selection
final class RandomStoneSpawner extends StoneSpawner {
    @Override protected Stone createStone() {
        int pick = ThreadLocalRandom.current().nextInt(3);
        return switch (pick) {
            case 0  -> new SmallStone();
            case 1  -> new MediumStone();
            default -> new LargeStone();
        };
    }
}

// Concrete Creator B: equal distribution via round-robin
final class EqualizedStoneSpawner extends StoneSpawner {
    private int idx = 0;
    @Override protected Stone createStone() {
        Stone s = switch (idx) {
            case 0  -> new SmallStone();
            case 1  -> new MediumStone();
            default -> new LargeStone();
        };
        idx = (idx + 1) % 3;
        return s;
    }
}

// Client
class Game {
    public static void main(String[] args) {
        StoneSpawner random = new RandomStoneSpawner();
        StoneSpawner equal  = new EqualizedStoneSpawner();

        System.out.println("Random: " + random.generateWave(9));
        System.out.println("Equal:  " + equal.generateWave(9));
    }
}
```

### Why this is Factory Method

- **Fixed algorithm** (`generateWave`) that creates objects at one step
- **Subclasses override** a factory method (`createStone()`) to decide the concrete product
- New policies (e.g., `WeightedStoneSpawner`) can be added **without touching** the algorithm or existing spawners

---

## Second Example: Fighter Jet Factory

**Context:** A mission planner needs to build a fleet of fighter jets by generation (Gen 4, Gen 4+, Gen 5). Different manufacturers produce different jets for the same generation.

```java
enum Generation { GEN4, GEN4_PLUS, GEN5 }

interface FighterJet {
    String model();
    Generation generation();
    String manufacturer();
}

// Concrete products (simplified)
final class TejasMk1 implements FighterJet {
    public String model() { return "Tejas Mk1"; }
    public Generation generation() { return Generation.GEN4; }
    public String manufacturer() { return "HAL"; }
}
final class TejasMk2 implements FighterJet {
    public String model() { return "Tejas Mk2"; }
    public Generation generation() { return Generation.GEN4_PLUS; }
    public String manufacturer() { return "HAL"; }
}
final class F15EX implements FighterJet {
    public String model() { return "F-15EX"; }
    public Generation generation() { return Generation.GEN4_PLUS; }
    public String manufacturer() { return "Boeing"; }
}
final class F35A implements FighterJet {
    public String model() { return "F-35A"; }
    public Generation generation() { return Generation.GEN5; }
    public String manufacturer() { return "Lockheed Martin"; }
}

// Factory Method interface
interface FighterJetFactory {
    FighterJet createJet(Generation gen);
}

// Concrete factories
final class HALFactory implements FighterJetFactory {
    @Override public FighterJet createJet(Generation gen) {
        return switch (gen) {
            case GEN4      -> new TejasMk1();
            case GEN4_PLUS -> new TejasMk2();
            case GEN5      -> throw new UnsupportedOperationException("HAL: No Gen 5");
        };
    }
}

final class LockheedFactory implements FighterJetFactory {
    @Override public FighterJet createJet(Generation gen) {
        return switch (gen) {
            case GEN4      -> new F15EX();
            case GEN4_PLUS -> new F15EX();
            case GEN5      -> new F35A();
        };
    }
}

// Algorithm depends only on the factory interface
final class MissionPlanner {
    List<FighterJet> planFleet(FighterJetFactory factory, List<Generation> demand) {
        var result = new ArrayList<FighterJet>(demand.size());
        for (var g : demand) result.add(factory.createJet(g));
        return result;
    }
}
```

**Key point:** Swapping `HALFactory` for `LockheedFactory` changes which jets are produced WITHOUT touching `MissionPlanner`.

---

## Simple Factory vs Factory Method

| Aspect | Simple Factory | Factory Method |
|--------|---------------|----------------|
| **What it is** | A utility/technique | A GoF design pattern |
| **Structure** | Single static method with switch/if | Abstract creator + concrete subclasses |
| **When to use** | Single creation point, no policy variation | Algorithm needs to create objects, policy varies per subclass |
| **Extensibility** | New type = edit the switch | New policy = add a new subclass (OCP) |
| **OCP compliance** | Weak (switch grows) | Strong (new subclass, no edits) |

**Exam Tip:** If the examiner asks "what's the difference between Simple Factory and Factory Method?" -- the answer is: Simple Factory centralizes `new` in one static method; Factory Method uses **inheritance** so a superclass algorithm defers creation to subclasses.

---

## SOLID Connection

| Principle | How Factory Method Relates |
|-----------|--------------------------|
| **SRP** | Creator owns the algorithm; each concrete creator owns its creation policy |
| **OCP** | New creation policies = new subclasses. No modification needed |
| **LSP** | All concrete creators can be used wherever the base creator is expected |
| **DIP** | Client depends on the abstract Creator/Product, not on concretes |

---

## Big Picture

- Simple Factory is a stepping stone -- use when you just need "make me an X"
- Factory Method shines when the **creation policy varies per context** (subclass, configuration, environment)
- Related to **Abstract Factory** (creates families of related objects) and **Prototype** (creates by cloning)
- Can be combined with **Prototype**: factory internally fetches and clones a prototype from a registry

---

## Exam Tips (Quick Recall)

1. **Simple Factory** = static method + switch. NOT a GoF pattern
2. **Factory Method** = base class with algorithm + abstract creation method + subclasses override
3. Factory Method follows **OCP**: add new policy by adding a new subclass
4. The algorithm is **final** (template method style) -- only the factory method is overridable
5. Client chooses which creator **at composition time** (via dependency injection or configuration)
6. Know BOTH examples: Stone spawner (policy variation) AND Fighter jet (manufacturer variation)

---

## Viva Questions

**Q1: What is a Simple Factory?**
A utility class with a static method that creates objects based on a parameter (enum, string). It centralizes `new` logic so clients don't know concrete classes. It's a technique, not a GoF pattern.

**Q2: What is the Factory Method pattern?**
A creational GoF pattern where a base class defines an algorithm that calls an abstract factory method. Subclasses override this method to decide which concrete product to create. The algorithm stays fixed; the creation step is polymorphic.

**Q3: What's the difference between Simple Factory and Factory Method?**
Simple Factory: one static method, one switch. Factory Method: inheritance-based -- base class has the algorithm, subclasses override the factory method to vary the product. Simple Factory can't vary policy per instance; Factory Method can.

**Q4: When should you use Factory Method over Simple Factory?**
When the creation **policy must vary** per context. E.g., random vs equalized stone selection, or different manufacturers producing different jets for the same generation request.

**Q5: How does Factory Method follow OCP?**
Adding a new creation policy means adding a new subclass of the creator. You never modify the base class algorithm or existing subclasses. Open for extension, closed for modification.

**Q6: What is the "creator" and "product" in Factory Method?**
Creator = the class with the algorithm and factory method (e.g., `StoneSpawner`). Product = the object being created (e.g., `Stone`). Concrete creators (e.g., `RandomStoneSpawner`) create concrete products (e.g., `SmallStone`).

**Q7: Can Factory Method return different types?**
The factory method returns the product interface type (e.g., `Stone`). Different concrete creators can return different concrete implementations, but the return type is always the common abstraction.

**Q8: How is Factory Method related to Template Method?**
Factory Method is often used INSIDE a Template Method. The base class algorithm (`generateWave`) is a template method that calls the factory method (`createStone`) as one of its steps.

**Q9: How does Abstract Factory differ from Factory Method?**
Factory Method: one factory method per creator, creates one product. Abstract Factory: a family of factory methods that create related products (e.g., a UI toolkit factory creating buttons, menus, and scrollbars that all match a theme).

**Q10: Can you implement Abstract Factory using Prototype?**
Yes. Instead of subclass factories, you can use a registry of prototypes. Each creation method clones a prototype from the registry.

---

## MCQ Quiz

**1. Simple Factory is:**
a) A GoF design pattern
b) A creational technique (not a GoF pattern)
c) A structural pattern
d) A behavioral pattern

<details><summary>Answer</summary>b) A creational technique (not a GoF pattern)</details>

**2. In Factory Method, the fixed algorithm is in the:**
a) Concrete product
b) Client code
c) Base creator class
d) Interface

<details><summary>Answer</summary>c) Base creator class</details>

**3. What does the factory method return?**
a) void
b) The concrete product class
c) The product interface/abstract type
d) A factory object

<details><summary>Answer</summary>c) The product interface/abstract type</details>

**4. Adding a new creation policy in Factory Method requires:**
a) Modifying the base creator
b) Adding a new subclass of the creator
c) Editing the product interface
d) Changing client code

<details><summary>Answer</summary>b) Adding a new subclass of the creator (OCP)</details>

**5. Simple Factory centralizes creation in:**
a) An abstract class
b) A single static method
c) Multiple subclasses
d) The client code

<details><summary>Answer</summary>b) A single static method</details>

**6. In the Stone example, `generateWave()` is:**
a) The factory method
b) A concrete product method
c) The fixed algorithm (template method) that calls the factory method
d) A static utility method

<details><summary>Answer</summary>c) The fixed algorithm (template method) that calls the factory method</details>

**7. `createStone()` is:**
a) The algorithm
b) The factory method (overridden by subclasses)
c) A constructor
d) A static method

<details><summary>Answer</summary>b) The factory method (overridden by subclasses)</details>

**8. Which SOLID principle does Factory Method most directly support?**
a) SRP
b) OCP (Open/Closed)
c) ISP
d) LSP

<details><summary>Answer</summary>b) OCP -- new policies are added by extension (new subclass), not modification</details>

**9. What is the main limitation of Simple Factory?**
a) It can't create objects
b) It grows a big switch; no support for varying creation policies
c) It requires inheritance
d) It can't return interfaces

<details><summary>Answer</summary>b) It grows a big switch; no support for varying creation policies</details>

**10. In the Fighter Jet example, `MissionPlanner` depends on:**
a) `HALFactory` directly
b) `FighterJetFactory` interface
c) All concrete jet classes
d) The `Generation` enum only

<details><summary>Answer</summary>b) `FighterJetFactory` interface (DIP -- depends on abstraction)</details>

**11. Which is TRUE about Factory Method?**
a) The creator must be a concrete class
b) The factory method must be static
c) Subclasses override the factory method to produce different products
d) It can only create one type of product ever

<details><summary>Answer</summary>c) Subclasses override the factory method to produce different products</details>

**12. `RandomStoneSpawner` and `EqualizedStoneSpawner` differ in:**
a) The algorithm for generating waves
b) Which concrete Stone the factory method returns
c) The Stone interface they use
d) The number of stones per wave

<details><summary>Answer</summary>b) Which concrete Stone the factory method returns (the creation policy)</details>

**13. Factory Method is related to which other pattern?**
a) Singleton
b) Template Method (the algorithm uses the factory method as a step)
c) Observer
d) Flyweight

<details><summary>Answer</summary>b) Template Method</details>

**14. Why is `generateWave()` marked `final`?**
a) For performance
b) To prevent subclasses from changing the algorithm, ensuring they only override the factory method
c) Because abstract methods can't be final
d) It's not final; that's optional

<details><summary>Answer</summary>b) To prevent subclasses from changing the algorithm -- they should only vary the creation step</details>

**15. In Abstract Factory, a factory creates:**
a) A single product
b) A family of related products
c) Only one concrete class
d) Only abstract objects

<details><summary>Answer</summary>b) A family of related products (e.g., themed UI components)</details>

**16. When should you prefer Simple Factory over Factory Method?**
a) When creation policies vary per subclass
b) When you just need a clean, single place to create objects and hide constructors
c) When you need to create families of objects
d) Never; always use Factory Method

<details><summary>Answer</summary>b) When you just need a clean, single place to create objects and hide constructors</details>

**17. What does `protected abstract Stone createStone()` mean?**
a) The base class has a complete implementation
b) Subclasses MUST provide the implementation of this factory method
c) Only the base class can call this method
d) The method returns null by default

<details><summary>Answer</summary>b) Subclasses MUST provide the implementation of this factory method</details>

**18. The client in Factory Method chooses the creation policy by:**
a) Passing a parameter to the factory method
b) Selecting which concrete creator to instantiate
c) Modifying the base class
d) Using reflection

<details><summary>Answer</summary>b) Selecting which concrete creator to instantiate (at composition time)</details>

**19. Which is NOT an advantage of Factory Method?**
a) Supports OCP
b) Decouples client from concrete products
c) Eliminates all switch statements everywhere
d) Allows varying creation policy per subclass

<details><summary>Answer</summary>c) The concrete creators may still use switch internally; the benefit is that the ALGORITHM is decoupled</details>

**20. Can you combine Factory Method with Prototype?**
a) No, they are incompatible
b) Yes -- a factory can internally clone a prototype from a registry instead of calling `new`
c) Only with Abstract Factory
d) Only in C++

<details><summary>Answer</summary>b) Yes -- a factory can internally clone a prototype from a registry instead of calling `new`</details>

### Scoring
- **18-20:** Factory patterns mastered.
- **14-17:** Good. Review Simple vs Factory Method distinction.
- **10-13:** Revisit the Creator/Product roles.
- **Below 10:** Re-read from Part A.

---

## Coding Exam Questions

### Problem 1: Convert to Simple Factory

The following code has creation logic scattered in the client. Refactor it to use a Simple Factory.

```java
class NotificationService {
    void sendNotification(String type, String message) {
        if (type.equals("EMAIL")) {
            EmailNotification e = new EmailNotification();
            e.send(message);
        } else if (type.equals("SMS")) {
            SmsNotification s = new SmsNotification();
            s.send(message);
        } else if (type.equals("PUSH")) {
            PushNotification p = new PushNotification();
            p.send(message);
        }
    }
}
```

<details><summary>Solution</summary>

```java
// Product interface
interface Notification {
    void send(String message);
}

class EmailNotification implements Notification {
    public void send(String message) { System.out.println("Email: " + message); }
}
class SmsNotification implements Notification {
    public void send(String message) { System.out.println("SMS: " + message); }
}
class PushNotification implements Notification {
    public void send(String message) { System.out.println("Push: " + message); }
}

// Simple Factory
enum NotificationType { EMAIL, SMS, PUSH }

final class NotificationFactory {
    private NotificationFactory() {}
    public static Notification create(NotificationType type) {
        return switch (type) {
            case EMAIL -> new EmailNotification();
            case SMS   -> new SmsNotification();
            case PUSH  -> new PushNotification();
        };
    }
}

// Clean client
class NotificationService {
    void sendNotification(NotificationType type, String message) {
        Notification n = NotificationFactory.create(type);
        n.send(message);
    }
}
```
</details>

---

### Problem 2: Implement Factory Method

Design a `ReportExporter` system where:
- Base algorithm: `export(data)` validates data, writes output, logs completion
- Factory method: `createWriter()` returns a `ReportWriter`
- Concrete writers: `CsvWriter`, `JsonWriter`
- Adding `XlsxWriter` should NOT require modifying existing classes

<details><summary>Solution</summary>

```java
import java.util.List;

// Product
interface ReportWriter {
    void write(List<String> data);
    String format();
}

class CsvWriter implements ReportWriter {
    public void write(List<String> data) {
        System.out.println("CSV: " + String.join(",", data));
    }
    public String format() { return "CSV"; }
}

class JsonWriter implements ReportWriter {
    public void write(List<String> data) {
        System.out.println("JSON: " + data);
    }
    public String format() { return "JSON"; }
}

// Creator with factory method
abstract class ReportExporter {
    // Factory Method
    protected abstract ReportWriter createWriter();

    // Fixed algorithm
    public final void export(List<String> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("Error: no data");
            return;
        }
        ReportWriter writer = createWriter();
        writer.write(data);
        System.out.println("Export complete [" + writer.format() + "]");
    }
}

// Concrete creators
class CsvExporter extends ReportExporter {
    @Override protected ReportWriter createWriter() {
        return new CsvWriter();
    }
}

class JsonExporter extends ReportExporter {
    @Override protected ReportWriter createWriter() {
        return new JsonWriter();
    }
}

// Adding XlsxWriter: just add new classes, nothing else changes
class XlsxWriter implements ReportWriter {
    public void write(List<String> data) {
        System.out.println("XLSX: " + data);
    }
    public String format() { return "XLSX"; }
}

class XlsxExporter extends ReportExporter {
    @Override protected ReportWriter createWriter() {
        return new XlsxWriter();
    }
}

// Client
class ExportDemo {
    public static void main(String[] args) {
        var data = List.of("Alice", "Bob", "Charlie");
        ReportExporter csv = new CsvExporter();
        ReportExporter json = new JsonExporter();
        ReportExporter xlsx = new XlsxExporter();

        csv.export(data);   // CSV: Alice,Bob,Charlie
        json.export(data);  // JSON: [Alice, Bob, Charlie]
        xlsx.export(data);  // XLSX: [Alice, Bob, Charlie]
    }
}
```
</details>

---

### Problem 3: Identify the Pattern

For each code snippet, identify whether it's Simple Factory, Factory Method, or Neither:

**Snippet A:**
```java
class ShapeFactory {
    static Shape create(String type) {
        if (type.equals("circle")) return new Circle();
        if (type.equals("square")) return new Square();
        throw new IllegalArgumentException(type);
    }
}
```

**Snippet B:**
```java
abstract class Dialog {
    abstract Button createButton();
    void render() {
        Button b = createButton();
        b.draw();
    }
}
class WindowsDialog extends Dialog {
    Button createButton() { return new WindowsButton(); }
}
```

**Snippet C:**
```java
class App {
    Shape makeShape() {
        return new Circle();
    }
}
```

<details><summary>Solution</summary>

- **Snippet A:** **Simple Factory** -- static method, switch logic, centralizes creation
- **Snippet B:** **Factory Method** -- abstract creator with algorithm (`render`), subclasses override factory method (`createButton`)
- **Snippet C:** **Neither** -- just a regular method calling `new`. No abstraction, no pattern. The method is not abstract, there's no interface, and there's no varying policy.
</details>
