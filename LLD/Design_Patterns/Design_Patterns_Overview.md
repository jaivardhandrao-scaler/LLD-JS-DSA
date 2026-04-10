# Design Patterns Overview -- Creational, Structural, Behavioral

Design patterns are **reusable solutions to common design problems**. They are NOT library code you copy-paste -- they are templates for how to structure classes and objects to solve a specific kind of problem.

The GoF (Gang of Four) book groups patterns into **3 categories** based on what kind of problem they solve.

---

## The Three Categories

### Creational Patterns -- "How do I create objects?"

**Problem they solve:** Object creation can be complex, expensive, or tightly coupled. Creational patterns decouple the **client** from the **concrete classes** it instantiates.

**Core idea:** Instead of `new ConcreteClass()` scattered everywhere, put creation behind an abstraction.

| Pattern | What it does | When to use |
|---------|-------------|-------------|
| **Singleton** | Ensure exactly one instance exists | Loggers, config managers, connection pools |
| **Factory Method** | Superclass defines algorithm, subclasses decide which object to create | Creation policy varies per subclass (stone spawner: random vs equalized) |
| **Simple Factory** | Static method centralizes `new` logic (not a GoF pattern, but a technique) | You just want to hide concrete classes in one place |
| **Builder** | Construct complex objects step-by-step via fluent API | Many parameters (some optional), immutable objects |
| **Prototype** | Clone a pre-configured exemplar instead of constructing from scratch | Expensive construction, runtime-extensible set of products |

**Exam Tip:** Simple Factory is NOT a GoF pattern. It's a pragmatic technique. Factory Method IS a GoF pattern. Know the difference.

---

### Structural Patterns -- "How do I compose objects?"

**Problem they solve:** You have existing classes/interfaces that don't fit together, or you need to add behavior without changing existing code. Structural patterns deal with **how objects are connected and composed**.

**Core idea:** Use composition and wrapping to build larger structures from smaller parts.

| Pattern | What it does | When to use |
|---------|-------------|-------------|
| **Adapter** | Translates one interface into another the client expects | Integrating legacy/third-party services with incompatible APIs |
| **Decorator** | Wraps an object to add optional, stackable behavior dynamically | Cross-cutting concerns (logging, retry, caching, auth) that are optional and combinable |

**Other structural patterns (not in syllabus but good to know exist):** Facade (simplify a complex subsystem), Proxy (control access to an object), Composite (tree structures), Bridge (separate abstraction from implementation).

---

### Behavioral Patterns -- "How do objects interact?"

**Problem they solve:** You have an algorithm or behavior that needs to vary, or objects need to communicate in a flexible way. Behavioral patterns deal with **algorithms and responsibility assignment**.

**Core idea:** Encapsulate varying behavior behind an interface so it can be swapped, composed, or delegated.

| Pattern | What it does | When to use |
|---------|-------------|-------------|
| **Strategy** | Encapsulate a family of algorithms; client picks one at runtime | Sorting policies, fee computation, AI movement, validation rules |

**Other behavioral patterns (not in syllabus but good to know exist):** Observer (event notification), Template Method (algorithm skeleton with overridable steps), State (behavior changes as internal state changes), Command (encapsulate a request as an object).

---

## Master Comparison Table

| Aspect | Singleton | Factory Method | Builder | Prototype | Adapter | Decorator | Strategy |
|--------|-----------|---------------|---------|-----------|---------|-----------|----------|
| **Category** | Creational | Creational | Creational | Creational | Structural | Structural | Behavioral |
| **Core mechanism** | Private constructor + static access | Abstract method overridden by subclasses | Fluent API + `build()` | `clone()` / `copy()` | Composition (wraps adaptee) | Composition (wraps component) | Composition (delegates to strategy) |
| **Number of instances** | Exactly 1 | Many (1 per factory call) | 1 (the built object) | Many (clones) | 1 adapter per provider | N decorators stacked | 1 strategy at a time |
| **Key SOLID principle** | -- | OCP, DIP | SRP | OCP | All 5 | OCP, SRP | OCP, SRP, DIP |
| **Solves** | Global access + single instance | Varying creation policy | Complex construction | Expensive/dynamic creation | Incompatible interfaces | Optional stackable behaviors | Interchangeable algorithms |

---

## When to Use Which -- Decision Flowchart

**"I need to create an object..."**

- Only one instance ever? -> **Singleton**
- Many parameters, some optional? -> **Builder**
- Object is expensive to construct, or types are defined at runtime? -> **Prototype**
- Creation policy varies per subclass (fixed algorithm, varying product)? -> **Factory Method**
- Just want to hide `new` behind a clean method? -> **Simple Factory**

**"I have objects that don't fit together..."**

- Legacy/third-party API doesn't match what my code expects? -> **Adapter**
- I need to add optional, stackable behavior to an existing interface? -> **Decorator**

**"I need different behavior at runtime..."**

- One algorithm selected from a family, swappable at runtime? -> **Strategy**
- Multiple optional behaviors layered on top of each other? -> **Decorator** (not Strategy!)

---

## Common Exam Confusions

### Decorator vs Strategy

| | Decorator | Strategy |
|-|-----------|----------|
| **How many at once** | Multiple stacked | One at a time |
| **Adds behavior** | Before/after the wrapped call | Replaces the entire algorithm |
| **Client knows about layering?** | No -- sees one interface | No -- sees one interface |
| **Example** | Retry + Cache + Auth on HTTP client | CountSort vs QuickSort vs InsertionSort |

**Quick test:** "Can I stack them?" If yes -> Decorator. "Do I pick one?" If yes -> Strategy.

### Adapter vs Decorator

| | Adapter | Decorator |
|-|---------|-----------|
| **Changes interface?** | Yes (translates one interface to another) | No (same interface in and out) |
| **Adds behavior?** | No (only translates) | Yes (adds new behavior) |
| **Goal** | Make incompatible things work together | Extend existing behavior dynamically |

### Factory Method vs Simple Factory

| | Simple Factory | Factory Method |
|-|---------------|---------------|
| **Structure** | Static method with switch/if | Abstract method overridden by subclasses |
| **GoF pattern?** | No (technique) | Yes |
| **When creation policy varies per instance?** | Can't do it | This is exactly what it's for |
| **Example** | `StoneFactory.create(SMALL)` | `RandomStoneSpawner.createStone()` vs `EqualizedStoneSpawner.createStone()` |

### Prototype vs Factory

| | Prototype | Factory Method |
|-|-----------|---------------|
| **Creates by** | Cloning an exemplar | Calling `new` in a subclass method |
| **Set of products known at** | Runtime (registry) | Compile time (subclass hierarchy) |
| **Best when** | Construction is expensive | Creation policy varies per subclass |

---

## How Patterns Combine

Patterns don't live in isolation. In real systems:

- **Factory + Prototype:** A factory can internally clone prototypes from a registry instead of calling `new`.
- **Strategy + Factory:** A factory can create the right strategy based on config/context.
- **Decorator + Strategy:** A decorator might use a strategy internally (e.g., a caching decorator with a configurable eviction strategy).
- **Builder + Singleton:** A builder can return a singleton (e.g., `Configuration.builder()...build()` always returns the same config).
- **Adapter + Decorator:** An adapter translates the interface, then decorators add cross-cutting behavior on top.

---

## Pattern Category Quick Recall

**Creational** (create objects cleanly): **S**ingleton, **F**actory, **B**uilder, **P**rototype
- Mnemonic: **S**ome **F**actories **B**uild **P**roducts

**Structural** (compose objects): **A**dapter, **D**ecorator
- Mnemonic: **A**dapt and **D**ecorate

**Behavioral** (object interaction): **S**trategy
- Mnemonic: **S**elect a strategy

---

## Exam Tips

- "Which category does X belong to?" -- memorize the table. This is a free MCQ point.
- "What's the difference between Adapter and Decorator?" -- interface change vs behavior addition.
- "What's the difference between Strategy and Decorator?" -- one-at-a-time vs stackable.
- "Is Simple Factory a design pattern?" -- No, it's a technique. Factory Method is the GoF pattern.
- "Can patterns be combined?" -- Yes, and knowing combinations shows depth.
- Every pattern in this course uses **composition over inheritance** except Singleton (which doesn't involve composition at all) and Factory Method (which uses inheritance for the creator hierarchy).
