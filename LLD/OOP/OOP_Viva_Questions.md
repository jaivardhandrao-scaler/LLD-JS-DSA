# OOP Viva Questions

> Q&A format. Each answer is detailed enough to speak aloud in 30-60 seconds.

---

### Encapsulation

**Q1: What is encapsulation? Give a one-line definition.**

Encapsulation is bundling data (fields) and the methods that operate on that data into a single class, while restricting direct access to the internals using access modifiers like `private`. The key idea is that outside code interacts with the object only through a controlled public API, so the object can enforce its own invariants.

---

**Q2: Is encapsulation just about making fields private and adding getters/setters?**

No. Making fields private is the mechanism, but the real goal is **controlling access and enforcing invariants**. A getter that returns a mutable internal list breaks encapsulation even though the field is private. True encapsulation means the class controls how its state is read and modified -- for example, a `BankAccount` with a `withdraw()` method that validates sufficient funds, rather than a raw setter for `balance`.

---

**Q3: What are the four access modifiers in Java? Explain their scope.**

1. `private` -- visible only within the same class.
2. Default (no keyword) -- visible within the same package.
3. `protected` -- visible within the same package AND to subclasses in other packages.
4. `public` -- visible everywhere.

The key trap: `protected` gives access to subclasses even in different packages, which is wider than default.

---

**Q4: What does the `this` keyword do?**

`this` refers to the current object. Three main uses:
1. Disambiguate field from parameter when they have the same name: `this.name = name;`
2. Constructor chaining -- call another constructor of the same class: `this(name, 18);` (must be first statement).
3. Return the current object for fluent APIs: `return this;`

You cannot use `this` in a `static` context because there is no "current object" for a static method.

---

### Abstraction

**Q5: What is abstraction and how is it different from encapsulation?**

Abstraction is hiding the "how" and exposing only the "what." Encapsulation is the mechanism (private fields, controlled access) that enables abstraction. Think of it this way: abstraction is a design concept (show the interface, hide the details), and encapsulation is the implementation technique (access modifiers, methods as gatekeepers). A `PaymentProcessor` interface is abstraction (callers don't know if it's Stripe or Razorpay); the private fields inside `StripeProcessor` are encapsulation.

---

**Q6: What is the difference between an abstract class and an interface?**

An abstract class can have constructors, instance fields, and a mix of concrete and abstract methods. A class can extend only one abstract class. Use it when subclasses share state or partial implementation.

An interface defines a pure contract -- no constructors, no instance fields (only `static final` constants). A class can implement multiple interfaces. Since Java 8, interfaces can have `default` methods with a body, but they still cannot hold instance state.

Rule of thumb: if you need shared state or a base constructor, use an abstract class. If you need a capability contract that multiple unrelated classes can adopt, use an interface.

---

**Q7: Can an abstract class have all concrete methods and no abstract methods?**

Yes, it's legal. An abstract class with only concrete methods simply cannot be instantiated directly -- you must subclass it. This is sometimes used to prevent direct instantiation while providing a default implementation that subclasses can override.

---

**Q8: Can an interface have method bodies?**

Yes, since Java 8. Interfaces support `default` methods (with a body) and `static` methods. Since Java 9, they can also have `private` helper methods. However, they still cannot have constructors or instance fields.

---

### Inheritance

**Q9: What is the difference between `extends` and `implements`?**

`extends` is used to inherit from a class (abstract or concrete). A class can extend only one class (single inheritance). `implements` is used to adopt an interface contract. A class can implement multiple interfaces. An interface can also `extend` another interface.

---

**Q10: What is the `super` keyword used for?**

Two main uses:
1. Call the parent class constructor: `super(args);` -- must be the first statement in the child constructor.
2. Call a parent method that was overridden: `super.describe();` -- useful when the child wants to extend rather than replace the parent's behavior.

If you don't write `super()` explicitly, the compiler inserts a no-arg `super()` automatically. If the parent has no no-arg constructor, you'll get a compile error.

---

**Q11: Why does Java not support multiple inheritance of classes?**

Because of the **diamond problem**. If class `C` extends both `A` and `B`, and both `A` and `B` have a method `doSomething()`, the compiler can't decide which one `C` inherits. Java avoids this ambiguity entirely by allowing single class inheritance only. Multiple interface implementation is allowed because the implementing class must provide the method body, resolving any ambiguity.

---

**Q12: What are the rules for method overriding?**

1. Method signature (name + parameter types) must be exactly the same.
2. Return type must be the same or a covariant (subtype) return.
3. Access modifier must be the same or wider (e.g., `protected` in parent can become `public` in child, but NOT `private`).
4. Cannot override `static` methods (they are hidden, not overridden).
5. Cannot override `final` methods.
6. Cannot override `private` methods (they're not inherited).
7. The child can throw fewer or narrower checked exceptions, but not broader ones.

---

**Q13: What is constructor chaining?**

Constructor chaining is when one constructor calls another. Within the same class, use `this(args)`. From child to parent, use `super(args)`. Both must be the first statement in the constructor. You cannot use both `this()` and `super()` in the same constructor.

---

**Q14: Explain composition vs inheritance. When do you prefer which?**

Inheritance models "IS-A" (a Dog IS-A Animal). Composition models "HAS-A" (a Car HAS-A Engine). Prefer composition when:
- The relationship is really "has-a" or "uses-a"
- You need loose coupling (swap the engine without changing the car)
- You want to combine behaviors from multiple sources (no diamond problem)

Design patterns like Strategy, Decorator, and Adapter all use composition over inheritance. The general rule: **favor composition over inheritance** unless there's a genuine IS-A relationship with shared state.

---

### Polymorphism

**Q15: What are the two types of polymorphism in Java?**

1. **Compile-time polymorphism** (static binding): achieved through method **overloading**. The compiler decides which method to call based on parameter types at compile time.
2. **Runtime polymorphism** (dynamic binding): achieved through method **overriding** combined with upcasting. The JVM decides which method to call based on the actual object type at runtime. This is also called dynamic dispatch.

---

**Q16: What is method overloading? What are its rules?**

Method overloading is having multiple methods with the same name but different parameter lists (different type, count, or order). Rules:
- Must differ in parameters.
- Return type alone is NOT sufficient -- `int foo(int x)` and `double foo(int x)` is a compile error.
- Access modifiers can differ.
- It's resolved at compile time based on the declared parameter types.

---

**Q17: What is dynamic dispatch? How does the JVM decide which overridden method to call?**

When you call a method on an object reference, the JVM looks at the **actual runtime type** of the object, not the declared type of the reference. So if `Shape s = new Circle();` and you call `s.area()`, the JVM sees that the actual object is a `Circle` and calls `Circle.area()`, not `Shape.area()`. This lookup happens at runtime using the virtual method table (vtable).

---

**Q18: What is upcasting? Is it safe?**

Upcasting is assigning a child object to a parent reference: `Animal a = new Dog();`. It's implicit (no cast needed) and always safe because a `Dog` IS-A `Animal`. The tradeoff: you lose access to child-specific methods through the parent reference. `a.bark()` won't compile because `Animal` doesn't declare `bark()`.

---

**Q19: What is downcasting? When does it fail?**

Downcasting is casting a parent reference back to a child type: `Dog d = (Dog) a;`. It's explicit (you must write the cast) and risky. It succeeds only if the actual object is indeed a `Dog`. If `a` actually points to a `Cat`, you get a `ClassCastException` at runtime. Always check with `instanceof` before downcasting.

---

**Q20: Can you override a static method?**

No. Static methods belong to the class, not to an instance. If a child class defines a static method with the same signature as the parent, it **hides** (not overrides) the parent's method. The method called depends on the **reference type** (not the object type), which is the opposite of dynamic dispatch. This is a common exam trap.

---

### Cross-Cutting

**Q21: How do the four pillars of OOP connect to SOLID principles?**

- **Encapsulation** -> SRP: each class guards its own data and has one reason to change.
- **Abstraction** -> DIP: high-level modules depend on abstract interfaces, not concrete implementations.
- **Inheritance** -> LSP: a subclass must be usable wherever the parent is expected, without breaking behavior.
- **Polymorphism** -> OCP: you can extend behavior by adding new classes (new shapes, new strategies) without modifying existing code that uses the parent type.

---

**Q22: What is the difference between hiding and overriding?**

Overriding applies to instance methods and uses dynamic dispatch (runtime resolution based on actual object type). Hiding applies to static methods and fields -- resolution is based on the declared reference type at compile time. Example: if `Parent.staticMethod()` is hidden by `Child.staticMethod()`, calling `parent.staticMethod()` on a `Parent` reference always calls the parent version, even if the actual object is a `Child`.

---

**Q23: Can a constructor be inherited?**

No. Constructors are never inherited. Each class must define its own constructors. The child constructor can call the parent constructor using `super(args)`, but it doesn't inherit it. If you don't define any constructor, the compiler generates a default no-arg constructor that calls `super()`.

---

**Q24: What is a covariant return type?**

When overriding a method, the return type can be a subtype of the parent method's return type. For example, if the parent returns `Animal`, the child can return `Dog`. This is valid since Java 5. It's called "covariant" because the return type varies in the same direction as the class hierarchy.

---

**Q25: What happens if you call an overridden method from a constructor?**

This is dangerous. If the parent constructor calls an overridden method, the child's version runs -- but the child's fields haven't been initialized yet (child constructor hasn't run). This can lead to bugs where the overridden method sees default values (0, null, false) instead of the intended initial values. Best practice: avoid calling overridable methods in constructors.
