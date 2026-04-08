# SOLID Principles - Viva Questions & Answers

> Covers the kind of questions Kshitij might ask in your viva. Practice answering these out loud.

---

## General SOLID Questions

### Q1: What does SOLID stand for?
**A:** Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion.

### Q2: Why do we need SOLID principles?
**A:** To make code easier to understand, extend, and maintain. Following SOLID reduces the risk of breaking existing functionality when adding new features, makes testing easier, and keeps classes focused and loosely coupled.

### Q3: Can a single piece of code violate multiple SOLID principles at once?
**A:** Yes, absolutely. For example, a class with if/else chains for different types that also creates concrete objects internally violates SRP (multiple responsibilities), OCP (must modify to extend), and DIP (depends on concretions). Violations often cluster together.

### Q4: Is it possible to over-apply SOLID?
**A:** Yes. Blindly splitting everything into tiny classes can lead to unnecessary complexity. SOLID is a guideline, not a law. If a class is small and unlikely to change, forcing an interface + injection for it is over-engineering. Apply SOLID where it reduces pain, not everywhere.

---

## SRP - Single Responsibility Principle

### Q5: Define SRP in one sentence.
**A:** A class should have only one reason to change.

### Q6: What are quick checks to spot SRP violations?
**A:** (From curriculum)
1. Multiple if/else blocks handling different behaviors
2. Unspecified Util/Helper classes doing unrelated things
3. Monster methods that generate, convert, mail, and return in one function

### Q7: In the Employee example from class, why is having `save()` inside Employee a violation?
**A:** Because Employee has two reasons to change: (1) business attributes change, (2) storage mechanism changes (file to SQL to Mongo). The `save()` method couples persistence logic to the domain object. A separate `EmployeeRepository` class should handle persistence.

### Q8: Does SRP mean a class should have only one method?
**A:** No. A class can have multiple methods as long as they all serve the **same responsibility**. For example, `EmployeeRepository` can have `save()`, `findById()`, `delete()` — all related to persistence. SRP is about **one reason to change**, not one method.

### Q9: What's wrong with a Util class that has `rupeeToDollar()`, `roundOffDouble()`, `calculateIncomeTax()`, and `toString()`?
**A:** Four completely unrelated responsibilities. Currency conversion, math operations, tax rules, and serialization have different reasons to change. Should be split into `CurrencyConverter`, `MathUtil`, `TaxCalculator`, etc.

---

## OCP - Open/Closed Principle

### Q10: What does "open for extension, closed for modification" mean practically?
**A:** You should be able to add new behavior (new employee type, new shape, new payment provider) by **adding new classes** rather than **editing existing ones**. This is typically achieved through polymorphism — abstract classes or interfaces.

### Q11: Walk me through the Tax Calculation example. How did we fix the OCP violation?
**A:** Initially, `TaxCalculationUtil.calculate()` had if/else for FTE and Intern — adding ContractEmployee meant editing this class. Fix: make `TaxCalculationUtil` abstract with an abstract `calculate()` method. Each employee type gets its own subclass (`FTETaxCalculation`, `InternTaxCalculation`). Adding a new type means adding a new class, not touching existing ones.

### Q12: How does the Adapter pattern from your study material demonstrate OCP?
**A:** `SellerRankingService` depends on the `SellerSearch` interface. When Snapdeal acquired Exclusively (a new provider), we didn't modify `SellerRankingService`. We just added `ExSearchAdapter` — a new class. The ranking service was **closed for modification**, **open for extension**.

### Q13: What's the role of polymorphism in OCP?
**A:** Polymorphism allows client code to work with an abstraction (interface/abstract class). New behaviors are added by creating new implementations of that abstraction. The client code never needs to change because it only knows about the abstraction.

### Q14: If I have a switch statement on an enum, is that always an OCP violation?
**A:** Not always, but it's a code smell. If the enum is stable and unlikely to grow (e.g., `DayOfWeek`), a switch is fine. But if you expect new cases to be added frequently (new payment types, new report formats), that switch will need modification every time — that's an OCP violation, and polymorphism is the better approach.

---

## LSP - Liskov Substitution Principle

### Q15: Explain LSP in simple terms.
**A:** If class B extends class A, then you should be able to use B anywhere you use A without the program breaking or behaving unexpectedly. The child must honor everything the parent promises.

### Q16: Why does the Kiwi example from class violate LSP?
**A:** `Bird` has an abstract `fly()` method. `Kiwi` extends `Bird` but throws `UnsupportedOperationException` in `fly()`. Any code that calls `bird.fly()` expecting all Birds to fly will crash when given a Kiwi. Kiwi cannot substitute for Bird.

### Q17: How do you fix the Kiwi problem?
**A:** Remove `fly()` from the `Bird` class — not all birds fly. Create a separate `Flyable` interface. `Eagle` implements both `Bird` and `Flyable`. `Kiwi` only extends `Bird`. Now the `Bird` contract only promises things all birds can do (like `eat()`).

### Q18: Explain the Rectangle-Square problem.
**A:** Mathematically, a Square "is a" Rectangle. But in code, `Square extends Rectangle` violates LSP because Square overrides `setWidth()` and `setHeight()` to keep both sides equal. Code that sets width=5 and height=4 on a Rectangle expects area=20, but gets area=16 with a Square. The substitution breaks the caller's expectations.

### Q19: What's the relationship between LSP and ISP?
**A:** They often work together. LSP violations frequently occur because a class is forced to implement methods it can't truly support (which is also an ISP issue). Fixing ISP by splitting interfaces often resolves LSP violations too — like separating `Flyable` from `Bird`.

### Q20: What are red flags for LSP violations?
**A:**
- `throw new UnsupportedOperationException()` in a subclass
- Empty method bodies that should do something
- Methods that do the **opposite** of the parent's contract
- Subclasses that add preconditions the parent didn't have

---

## ISP - Interface Segregation Principle

### Q21: What does ISP say?
**A:** No client should be forced to depend on methods it does not use. Prefer many small, client-specific interfaces over one large general-purpose interface.

### Q22: From the curriculum — why is having `fly()`, `flapWings()`, and `takeOff()` in one Flyable interface a problem?
**A:** Superman and MiG-21 can fly but don't flap wings. They're forced to implement `flapWings()` with an empty body or exception — they depend on a method they don't use. Solution: split into `Flyable`, `WingFlappable`, and `TakeOffable`.

### Q23: Explain the Employee/UnpaidIntern ISP violation from class.
**A:** The `Employee` interface has `getEmail()`, `processPayment()`, and `getSalary()`. UnpaidInterns don't get paid, so they're forced to implement `processPayment()` meaninglessly. Fix: split into `Employee` (with `getEmail()`, `getName()`) and `Payable` (with `processPayment()`, `getSalary()`). UnpaidIntern only implements `Employee`.

### Q24: How does ISP relate to the SellerSearch interface in the Adapter PDF?
**A:** `SellerSearch` only exposes `getSellersBySku()` and `getSellerWithMaxDiscount()` — exactly what the ranking service needs. It doesn't include provider-specific methods like pagination or score lookups. That's a minimal, client-specific interface — ISP followed.

### Q25: Can having too many interfaces be a problem?
**A:** Yes. If you split every single method into its own interface, you get interface pollution — too many tiny interfaces that are hard to discover and manage. The goal is to group **cohesive** methods that change together. Don't split interfaces that are already thin and focused.

---

## DIP - Dependency Inversion Principle

### Q26: What are the two parts of DIP?
**A:**
1. High-level modules should not depend on low-level modules. Both should depend on abstractions.
2. Abstractions should not depend on details. Details should depend on abstractions.

### Q27: Walk me through the PaymentProcessor example from class.
**A:** Initially, `PaymentProcessor` creates `new SqlProductRepo()` inside itself — high-level module (PP) depends on low-level module (SqlProductRepo). Fix: create `ProductRepo` interface. Both `SqlProductRepo` and `MongoProductRepo` implement it. `PaymentProcessor` takes `ProductRepo` in its constructor. Now switching databases means passing a different implementation — PaymentProcessor never changes.

### Q28: What is dependency injection? How does it relate to DIP?
**A:** Dependency injection is the **mechanism** to achieve DIP. Instead of a class creating its dependencies internally (`new SqlProductRepo()`), you **inject** them from outside — typically via the constructor. DIP is the **principle**; DI is the **technique**.

### Q29: What are the different ways to inject dependencies?
**A:**
1. **Constructor injection** (most common, recommended): pass via constructor parameter
2. **Setter injection**: set via a setter method after construction
3. **Interface injection**: implement an injection interface

Constructor injection is preferred because it makes dependencies explicit and ensures the object is fully initialized.

### Q30: From the Adapter PDF — how does SellerRankingService follow DIP?
**A:** `SellerRankingService` depends on `SellerSearch` (an interface/abstraction), not on `SDSellerSearchService` or `ExclusivelySellerSearchService` (concretions). The abstraction is injected via constructor. The high-level module (ranking) and low-level modules (provider services) both depend on the abstraction (`SellerSearch`).

### Q31: If I'm writing a small utility class that will never change, do I still need DIP?
**A:** Not necessarily. DIP is most valuable at **boundaries** — where modules interact, where implementations might be swapped, or where you need testability. For stable, internal utilities (like a math helper), directly using the class is fine. Apply DIP where the cost of coupling is real.

---

## Cross-Cutting / Tricky Questions

### Q32: A class has a method with a giant switch statement on employee type. Which principles does it violate?
**A:** SRP (the class handles logic for multiple types), OCP (adding a new type means modifying the switch), and potentially LSP (if the switch exists because subclasses can't properly substitute for the parent).

### Q33: How do design patterns relate to SOLID?
**A:** Design patterns are **proven solutions** that often enforce SOLID:
- **Strategy** pattern → OCP + DIP (swap algorithms without modifying context)
- **Adapter** pattern → OCP + DIP + SRP + ISP (translate at boundary, depend on abstraction)
- **Factory** pattern → OCP + DIP (abstract object creation)
- **Decorator** pattern → OCP + SRP (add behavior without modifying existing classes)

### Q34: Can you have a class that follows all 5 SOLID principles? Give an example.
**A:** Yes. `SellerRankingService` from the Adapter PDF:
- **SRP**: Only ranks sellers — single responsibility
- **OCP**: New providers added via new adapters, ranking code untouched
- **LSP**: All `SellerSearch` implementations provide equivalent behavior
- **ISP**: `SellerSearch` interface is minimal — only methods ranking needs
- **DIP**: Depends on `SellerSearch` abstraction, injected via constructor

### Q35: What's the difference between LSP and ISP? They seem similar.
**A:** LSP is about **behavioral correctness** — can a subclass truly substitute for its parent without breaking things? ISP is about **interface design** — does the interface force implementors to depend on methods they don't need? They overlap (fixing ISP often fixes LSP), but they ask different questions. LSP asks "does the child honor the contract?" ISP asks "is the contract too broad?"

---

*Tip: Practice answering these out loud in under 60 seconds each. Your viva is about demonstrating understanding, not reciting definitions.*
