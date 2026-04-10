# Factory -- Teacher PDF Practice

> All questions from **DesignPattern_Factory.pdf** (Stone game, Fighter Jet producers).

---

## Viva Questions

**Q1: The PDF starts with a Stone game. What was the pain point that led to Simple Factory?**

`new SmallStone()`, `new MediumStone()`, `new LargeStone()` were scattered across gameplay code. When a new size (Giant) appears, you must edit every file that creates stones. Simple Factory centralizes all `new` logic into one static method -- `StoneFactory.create(StoneType t)` -- so gameplay code depends only on the `Stone` interface.

---

**Q2: In the PDF, what is the difference between Simple Factory and Factory Method? Use the Stone example.**

- **Simple Factory (`StoneFactory`):** A single static method with a `switch` that returns a `Stone` based on `StoneType`. No per-instance policy. All callers get the same creation logic.
- **Factory Method (`StoneSpawner`):** An abstract class owns the `generateWave()` algorithm. The abstract `createStone()` method is overridden by `RandomStoneSpawner` (random mix) and `EqualizedStoneSpawner` (round-robin S->M->L). Different spawner instances have different creation policies.

Key distinction: Simple Factory can't vary policy per instance. Factory Method can.

---

**Q3: The PDF says Simple Factory is "not a GoF pattern." What is it?**

It's a pragmatic **technique** (or idiom). The GoF book doesn't list it as a pattern. Factory Method IS a GoF pattern. Simple Factory is just "move all `new` into one place." Factory Method uses inheritance and polymorphism to let subclasses decide what to create.

---

**Q4: In the Fighter Jet example, what are the roles in Factory Method?**

- **Product interface:** `FighterJet` (with `model()`, `generation()`, `manufacturer()`)
- **Concrete products:** `TejasMk1`, `TejasMk2`, `Su30MKI`, `F15EX`, `F22`, `F35A`
- **Creator interface:** `FighterJetFactory` with `createJet(Generation gen)`
- **Concrete creators:** `HALFactory` (maps Gen4->TejasMk1, Gen4+->TejasMk2) and `LockheedMartinFactory` (maps Gen4->F15EX, Gen5->F35A)
- **Algorithm/Client:** `MissionPlanner.planFleet()` -- iterates over demand, calls `factory.createJet(g)` for each generation

---

**Q5: In the PDF, what happens when HALFactory gets a Gen5 request?**

It throws `UnsupportedOperationException("HAL: Gen 5 not available in this demo")`. This shows that factories can throw on unsupported requests or fall back to the closest available model.

---

**Q6: The PDF mentions "Hybrid with Prototype." What does that mean?**

If individual jet variants are heavy to configure, each factory can internally fetch a pre-configured prototype from a registry and clone it before returning. So `HALFactory.createJet(GEN4)` would do `registry.get("tejas-mk1").clone()` instead of `new TejasMk1()`.

---

**Q7: In `generateWave()`, why is the method marked `final`?**

The PDF's `StoneSpawner.generateWave()` is `public final` because the wave-generation ALGORITHM should not change -- only the stone selection policy (`createStone()`) should vary per subclass. This is the Template Method aspect of Factory Method.

---

## MCQ Quiz (20 Questions)

**Q1.** In the PDF's Simple Factory, `StoneFactory.create()` is:

a) An instance method
b) A `static` method in a utility class with private constructor
c) An abstract method
d) A default method in an interface

<details><summary>Answer</summary>b) `StoneFactory` has a `private` constructor and a `public static Stone create(StoneType t)` method.</details>

---

**Q2.** How many concrete `Stone` classes does the PDF define?

a) 2
b) 3
c) 4
d) 5

<details><summary>Answer</summary>b) 3: `SmallStone`, `MediumStone`, `LargeStone`.</details>

---

**Q3.** In the PDF, `StoneType` is:

a) A class
b) An interface
c) An enum
d) A String constant

<details><summary>Answer</summary>c) `enum StoneType { SMALL, MEDIUM, LARGE; }`</details>

---

**Q4.** What does `EqualizedStoneSpawner` do differently from `RandomStoneSpawner`?

a) Creates only large stones
b) Uses round-robin (S->M->L->S->...) for equal distribution
c) Creates stones randomly with weights
d) Doesn't create stones

<details><summary>Answer</summary>b) It uses `idx = (idx + 1) % 3` to cycle through Small, Medium, Large in equal proportions.</details>

---

**Q5.** In the PDF, `StoneSpawner` is:

a) An interface
b) A concrete class
c) An abstract class
d) An enum

<details><summary>Answer</summary>c) `abstract class StoneSpawner` with abstract `createStone()` and final `generateWave(int count)`.</details>

---

**Q6.** The PDF's `FighterJetFactory` is:

a) An abstract class
b) An interface with `createJet(Generation gen)`
c) A concrete class
d) An enum

<details><summary>Answer</summary>b) An interface: `interface FighterJetFactory { FighterJet createJet(Generation gen); }`</details>

---

**Q7.** How many `Generation` values does the Fighter Jet PDF define?

a) 2
b) 3
c) 4
d) 5

<details><summary>Answer</summary>b) 3: `GEN4`, `GEN4_PLUS`, `GEN5`.</details>

---

**Q8.** `LockheedMartinFactory` maps `GEN4` to:

a) `F22`
b) `F35A`
c) `F15EX`
d) `TejasMk1`

<details><summary>Answer</summary>c) `F15EX`. Both Gen4 and Gen4+ map to F15EX. Gen5 maps to F35A.</details>

---

**Q9.** In the PDF, `MissionPlanner.planFleet()` receives:

a) A list of `FighterJet` objects
b) A `FighterJetFactory` and a `List<Generation>`
c) A `StoneSpawner`
d) A `StoneType`

<details><summary>Answer</summary>b) It takes a factory and a demand list, then calls `factory.createJet(g)` for each generation in the list.</details>

---

**Q10.** What is the key Socratic question the PDF poses for Factory Method?

a) "Can we make object creation faster?"
b) "Can we keep the algorithm identical and swap only the creation policy?"
c) "How do we make objects thread-safe?"
d) "Should we use inheritance or composition?"

<details><summary>Answer</summary>b) The PDF asks: "Can we keep the 'generate wave' algorithm identical and swap only the stone-selection policy?"</details>

---

**Q11.** The PDF's Simple Factory `StoneFactory` has what drawback?

a) Too many classes
b) The factory grows a big switch; no support for differing policies
c) It's thread-unsafe
d) It can't create stones

<details><summary>Answer</summary>b) The PDF says: "factory grows a big switch; no support for differing policies (random / equalized) -- that's not its job."</details>

---

**Q12.** In Factory Method, what can you add WITHOUT touching the algorithm or call sites?

a) New stone types
b) New creation policies (e.g., `WeightedStoneSpawner`)
c) New game rules
d) New print methods

<details><summary>Answer</summary>b) New policies -- just create a new subclass of `StoneSpawner` with its own `createStone()`. OCP in action.</details>

---

**Q13.** The PDF lists 4 more Factory Method contexts in the appendix. Which is NOT one of them?

a) Maze variants
b) Projectile spawners
c) Report exporters
d) Payment processors

<details><summary>Answer</summary>d) Payment processors are not in the Factory PDF appendix. The four are: Maze, Projectile spawners, Report exporters, Dialogs.</details>

---

**Q14.** In the PDF's Fighter Jet example, swapping `HALFactory` for `LockheedMartinFactory` requires changes to:

a) `MissionPlanner`
b) `FighterJet` interface
c) Only the composition/wiring code (e.g., `main()`)
d) All concrete jet classes

<details><summary>Answer</summary>c) Only the wiring: `planner.planFleet(new LockheedMartinFactory(), demand)` instead of `new HALFactory()`. MissionPlanner is untouched.</details>

---

**Q15.** The PDF defines `SmallStone.damage()` as:

a) 1
b) 5
c) 10
d) 18

<details><summary>Answer</summary>b) 5. Medium is 10, Large is 18.</details>

---

**Q16.** What pattern does the PDF say to use when "creation policy must vary per game instance"?

a) Simple Factory
b) Factory Method
c) Singleton
d) Prototype

<details><summary>Answer</summary>b) Factory Method. "Part B — Factory Method (when creation policy must vary)."</details>

---

**Q17.** `RandomStoneSpawner` uses what class for randomness?

a) `Math.random()`
b) `Random`
c) `ThreadLocalRandom`
d) `SecureRandom`

<details><summary>Answer</summary>c) `ThreadLocalRandom.current().nextInt(3)`.</details>

---

**Q18.** The `Stone` interface in the PDF has which methods?

a) `size()`, `damage()`
b) `size()`, `damage()`, `weight()`
c) `create()`, `destroy()`
d) `type()`, `value()`

<details><summary>Answer</summary>b) `String size()`, `int damage()`, `double weight()`.</details>

---

**Q19.** The PDF says: "Simple Factory is perfect when..."

a) You need per-instance creation policies
b) You want a clean place to create a single kind of product and hide constructors
c) You need thread safety
d) Objects are expensive to construct

<details><summary>Answer</summary>b) "Simple Factory is perfect when you want a clean place to create a single kind of product and hide constructors."</details>

---

**Q20.** What GoF book does the PDF reference?

a) Clean Code by Robert Martin
b) Refactoring by Martin Fowler
c) Design Patterns by Gamma, Helm, Johnson, Vlissides
d) Head First Java

<details><summary>Answer</summary>c) "E. Gamma, R. Helm, R. Johnson, J. Vlissides. Design Patterns: Elements of Reusable Object-Oriented Software."</details>

---

## Coding Problems

### Problem 1: Implement the Stone Simple Factory from Memory

Write the complete `Stone` interface, 3 concrete stones (`SmallStone`, `MediumStone`, `LargeStone`), `StoneType` enum, and `StoneFactory` exactly as described in the PDF. Then write a client that spawns one of each.

<details><summary>Solution</summary>

```java
enum StoneType { SMALL, MEDIUM, LARGE }

interface Stone {
    String size();
    int damage();
    double weight();
}

final class SmallStone implements Stone {
    public String size()   { return "SMALL"; }
    public int damage()    { return 5; }
    public double weight() { return 1.0; }
}
final class MediumStone implements Stone {
    public String size()   { return "MEDIUM"; }
    public int damage()    { return 10; }
    public double weight() { return 2.5; }
}
final class LargeStone implements Stone {
    public String size()   { return "LARGE"; }
    public int damage()    { return 18; }
    public double weight() { return 4.0; }
}

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

class Main {
    public static void main(String[] args) {
        for (StoneType t : StoneType.values()) {
            Stone s = StoneFactory.create(t);
            System.out.println(s.size() + " dmg=" + s.damage() + " wt=" + s.weight());
        }
    }
}
```
</details>

---

### Problem 2: Add a New Spawner Without Touching Existing Code

Given the PDF's `StoneSpawner` (abstract), `RandomStoneSpawner`, and `EqualizedStoneSpawner`, create a `WeightedStoneSpawner` that spawns:
- 50% Large stones
- 30% Medium stones
- 20% Small stones

Show that `generateWave()` works without any changes.

<details><summary>Solution</summary>

```java
import java.util.concurrent.ThreadLocalRandom;

final class WeightedStoneSpawner extends StoneSpawner {
    @Override
    protected Stone createStone() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 20) return new SmallStone();       // 0-19: 20%
        else if (roll < 50) return new MediumStone();  // 20-49: 30%
        else return new LargeStone();                  // 50-99: 50%
    }
}

// Usage -- generateWave() is inherited, unchanged:
class Demo {
    public static void main(String[] args) {
        StoneSpawner weighted = new WeightedStoneSpawner();
        System.out.println(weighted.generateWave(10));
        // Produces ~5 Large, ~3 Medium, ~2 Small
    }
}
```

**Key point:** `generateWave()` is `final` in the base class and calls `createStone()` polymorphically. Adding `WeightedStoneSpawner` required ZERO changes to `StoneSpawner`, `RandomStoneSpawner`, `EqualizedStoneSpawner`, or any client code. This is OCP.
</details>

---

### Problem 3: Convert Fighter Jet Factory Method to Simple Factory

The PDF's Fighter Jet example uses Factory Method (abstract creator + subclasses). Rewrite it as a Simple Factory instead. Then explain what you LOSE by doing this.

<details><summary>Solution</summary>

```java
// Simple Factory version
final class JetFactory {
    private JetFactory() {}

    public static FighterJet create(String manufacturer, Generation gen) {
        if ("HAL".equals(manufacturer)) {
            return switch (gen) {
                case GEN4      -> new TejasMk1();
                case GEN4_PLUS -> new TejasMk2();
                case GEN5      -> throw new UnsupportedOperationException("HAL: no Gen5");
            };
        } else if ("LOCKHEED".equals(manufacturer)) {
            return switch (gen) {
                case GEN4, GEN4_PLUS -> new F15EX();
                case GEN5            -> new F35A();
            };
        }
        throw new IllegalArgumentException("Unknown manufacturer: " + manufacturer);
    }
}
```

**What you LOSE:**
1. **OCP:** Adding a new manufacturer (e.g., Sukhoi) requires editing the `if/else` chain inside `JetFactory`. With Factory Method, you just add a new `SukhoiFactory` class.
2. **Per-instance policy:** Simple Factory is a static method -- you can't have two factory instances with different policies.
3. **Polymorphism:** `MissionPlanner` would depend on the concrete `JetFactory` class instead of an abstract `FighterJetFactory` interface (violates DIP).
4. **Testability:** Harder to mock a static method than to inject a factory interface.
</details>
