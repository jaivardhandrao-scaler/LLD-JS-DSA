# SOLID Principles - Complete Exam Prep Guide

> **Goal:** Understand each principle deeply through multiple examples so you can identify violations, fix them, and explain *why* in your exam/viva.

---

## Why SOLID?

We want code that is:
- **Easy to understand** - new devs can read and reason about it
- **Easy to extend** - add features without rewriting
- **Easy to maintain** - fix bugs without breaking other things

SOLID is a set of **5 principles** that achieve this. They often overlap and reinforce each other.

---

## S - Single Responsibility Principle (SRP)

> **"A class should have only one reason to change."**

A class should do **one thing** and do it well. If a class has two responsibilities, changes to one can break the other.

### Quick Checks for SRP Violations (from your syllabus)
1. **Multiple if/else blocks** handling different behaviors
2. **Unspecified Util/Helper classes** that do everything
3. **Monster methods** that do 10 things in one function

---

### Example 1: Employee Save (From Your Curriculum)

**BAD - Violates SRP:**
```java
class Employee {
    String name, email;
    int id;

    void save() {
        // Serialize the object
        // Open a file
        // Write in the file
        // Close the file
    }
}
```
**Problem:** Employee class has TWO reasons to change:
1. Employee attributes change (business logic)
2. Storage format changes (e.g., file -> SQL -> MongoDB)

If tomorrow we switch from file to SQL, we must modify the Employee class. That's a violation.

**GOOD - Follows SRP:**
```java
class Employee {
    String name, email;
    int id;
}

class EmployeeRepository {
    void save(Employee e) {
        // Serialize the object
        // Open a file
        // Write in the file
    }
}
```
Now each class has **one reason to change**. Employee changes for business reasons. Repository changes for storage reasons.

---

### Example 2: Monster Method (From Your Curriculum)

**BAD:**
```java
int getIncome() {
    // Generate payslip
    // Convert payslip to JSON
    // Mail payslip to the employee
    return this.income;
}
```
This method is doing 4 things! If the mailing logic changes, you're editing an income method.

**GOOD:**
```java
Payslip generatePayslip(Employee e) { ... }
String convertToJson(Payslip p) { ... }
void mailPayslip(String json, String email) { ... }
int getIncome() { return this.income; }
```

---

### Example 3: God Utility Class (From Your Curriculum)

**BAD:**
```java
class Util {
    void rupeeToDollar(double amount) { }
    int roundOffDouble(double d) { }
    int calculateIncomeTax(Employee e) { }
    String toString(Object o) { }
}
```
This class has **4 completely unrelated reasons to change**. Currency conversion has nothing to do with tax calculation.

**GOOD:**
```java
class CurrencyConverter {
    void rupeeToDollar(double amount) { }
}
class MathUtil {
    int roundOffDouble(double d) { }
}
class TaxCalculator {
    int calculateIncomeTax(Employee e) { }
}
```

---

### Example 4: Real-World - Invoice Class

**BAD:**
```java
class Invoice {
    double amount;
    
    double calculateTotal() { /* business logic */ }
    void printInvoice() { /* printing logic */ }
    void saveToDatabase() { /* persistence logic */ }
}
```
Three responsibilities: calculation, printing, persistence. Three reasons to change.

**GOOD:**
```java
class Invoice {
    double calculateTotal() { /* only business logic */ }
}
class InvoicePrinter {
    void print(Invoice invoice) { /* only printing */ }
}
class InvoiceRepository {
    void save(Invoice invoice) { /* only persistence */ }
}
```

---

### SRP Exam Tip
> When asked "does this violate SRP?", count the **reasons the class might change**. If more than one, it violates SRP.

---

## O - Open/Closed Principle (OCP)

> **"Software entities should be open for extension but closed for modification."**

You should be able to **add new behavior without changing existing code**. This reduces the risk of breaking things that already work.

---

### Example 1: Tax Calculation (From Your Curriculum)

**Requirement v2:** Calculate tax for employees.
- Income tax: 20% of income + Professional tax: 2% of income

**Requirement v3:** Different rules per type:
- FTE: Income tax 30% + Professional tax 2%
- Intern: Income tax 15%, no professional tax

**BAD - Violates OCP:**
```java
class TaxCalculationUtil {
    double calculate(Employee e) {
        if (e instanceof FTE) {
            return e.getIncome() * 0.30 + e.getIncome() * 0.02;
        } else if (e instanceof Intern) {
            return e.getIncome() * 0.15;
        }
        // What if we add ContractEmployee tomorrow?
        // We MODIFY this class = OCP violation!
    }
}
```

**GOOD - Follows OCP:**
```java
abstract class TaxCalculationUtil {
    abstract double calculate(Employee e);
}

class FTETaxCalculation extends TaxCalculationUtil {
    double calculate(Employee e) {
        return e.getIncome() * 0.30 + e.getIncome() * 0.02;
    }
}

class InternTaxCalculation extends TaxCalculationUtil {
    double calculate(Employee e) {
        return e.getIncome() * 0.15;
    }
}

// Adding ContractEmployee? Just add a new class!
class ContractTaxCalculation extends TaxCalculationUtil {
    double calculate(Employee e) {
        return e.getIncome() * 0.25;
    }
}
```
**Open for extension** (add new class) + **Closed for modification** (no existing class touched).

---

### Example 2: Bird Flying (From Your Curriculum)

**BAD:**
```java
class Bird {
    void fly(String type) {
        if (type.equals("Eagle")) {
            System.out.println("Soars high");
        } else if (type.equals("Hen")) {
            System.out.println("Short bursts");
        }
        // Adding Parrot? Modify this method = OCP violation
    }
}
```

**GOOD:**
```java
abstract class Bird {
    abstract void fly();
}

class Eagle extends Bird {
    void fly() { System.out.println("Soars high"); }
}

class Hen extends Bird {
    void fly() { System.out.println("Short bursts"); }
}

class Parrot extends Bird {
    void fly() { System.out.println("Flies in flocks"); }
}
```

---

### Example 3: Shape Area Calculator

**BAD:**
```java
class AreaCalculator {
    double calculate(Object shape) {
        if (shape instanceof Circle c) {
            return Math.PI * c.radius * c.radius;
        } else if (shape instanceof Rectangle r) {
            return r.width * r.height;
        }
        // Adding Triangle means modifying this class!
    }
}
```

**GOOD:**
```java
interface Shape {
    double area();
}

class Circle implements Shape {
    double radius;
    public double area() { return Math.PI * radius * radius; }
}

class Rectangle implements Shape {
    double width, height;
    public double area() { return width * height; }
}

class Triangle implements Shape {
    double base, height;
    public double area() { return 0.5 * base * height; }
}

// AreaCalculator never needs to change
class AreaCalculator {
    double calculate(Shape shape) {
        return shape.area();  // polymorphism handles it
    }
}
```

---

### Example 4: From Your Adapter PDF - SellerRankingService

This is OCP in action from your study material:
```java
// SellerRankingService NEVER changes when a new provider is added
public final class SellerRankingService {
    private final SellerSearch search;  // depends on abstraction

    public SellerRankingService(SellerSearch search) {
        this.search = search;
    }

    public List<Seller> rankBySku(String sku) {
        // ranking algorithm stays the same
        // adding Flipkart? Just add FlipkartSearchAdapter
        // This class is CLOSED for modification
    }
}
```

---

### OCP Exam Tip
> If adding a new feature requires **editing** an existing class (especially adding if/else), it violates OCP. The fix is usually **polymorphism** - abstract classes or interfaces.

---

## L - Liskov Substitution Principle (LSP)

> **"Objects of a superclass should be replaceable with objects of its subclasses without breaking the program."**

If `B` extends `A`, then everywhere you use `A`, you should be able to swap in `B` and everything still works correctly.

---

### Example 1: Kiwi Bird (From Your Curriculum)

**BAD - Violates LSP:**
```java
abstract class Bird {
    abstract void fly();
}

class Eagle extends Bird {
    void fly() { System.out.println("Eagle soars"); }
}

class Kiwi extends Bird {
    void fly() {
        // Kiwi CAN'T fly! What do we do here?
        throw new UnsupportedOperationException("Kiwi can't fly!");
    }
}
```

Now consider this code:
```java
void makeBirdFly(Bird bird) {
    bird.fly();  // Works for Eagle, CRASHES for Kiwi!
}
```
We **cannot substitute** Kiwi where Bird is expected. LSP violated.

**GOOD - Follows LSP:**
```java
abstract class Bird {
    abstract void eat();
    // Only things ALL birds can do
}

interface Flyable {
    void fly();
}

class Eagle extends Bird implements Flyable {
    void eat() { System.out.println("Eats prey"); }
    void fly() { System.out.println("Soars high"); }
}

class Kiwi extends Bird {
    void eat() { System.out.println("Eats insects"); }
    // No fly() - Kiwi isn't Flyable, and that's fine
}
```

---

### Example 2: Rectangle & Square (Classic Interview Question)

**BAD - Violates LSP:**
```java
class Rectangle {
    protected int width, height;

    void setWidth(int w) { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}

class Square extends Rectangle {
    @Override
    void setWidth(int w) { this.width = w; this.height = w; }
    @Override
    void setHeight(int h) { this.width = h; this.height = h; }
}
```

Now this breaks:
```java
void testArea(Rectangle r) {
    r.setWidth(5);
    r.setHeight(4);
    assert r.area() == 20;  // Fails for Square! (gives 16)
}
```
Square **changes the behavior** the caller expects from Rectangle.

**GOOD:**
```java
interface Shape {
    int area();
}

class Rectangle implements Shape {
    int width, height;
    int area() { return width * height; }
}

class Square implements Shape {
    int side;
    int area() { return side * side; }
}
```
No inheritance relationship. No broken expectations.

---

### Example 3: Payment Processing

**BAD:**
```java
class Payment {
    void processPayment(double amount) { /* charge card */ }
}

class RefundPayment extends Payment {
    @Override
    void processPayment(double amount) {
        // Actually RETURNS money instead of charging
        // Caller expects money to be charged, gets refund instead!
    }
}
```

**GOOD:**
```java
interface PaymentOperation {
    void execute(double amount);
}

class Charge implements PaymentOperation {
    void execute(double amount) { /* charges amount */ }
}

class Refund implements PaymentOperation {
    void execute(double amount) { /* refunds amount */ }
}
```

---

### LSP Exam Tip
> Ask yourself: "If I replace the parent with this child **everywhere**, will anything break or behave unexpectedly?" If yes, LSP is violated. Common red flags: `throw new UnsupportedOperationException()`, empty method bodies, or methods that do the **opposite** of what the parent contract promises.

---

## I - Interface Segregation Principle (ISP)

> **"No client should be forced to depend on methods it does not use."**
> **"Many client-specific interfaces are better than one general-purpose interface."**

Don't create fat interfaces. Split them into smaller, focused ones.

---

### Example 1: Flyable Interface (From Your Curriculum)

**BAD - Fat Interface:**
```java
interface Flyable {
    void fly();
    void flapWings();
    void takeOff();
}

// Superman can fly but doesn't flap wings!
class Superman implements Flyable {
    void fly() { /* zooms through sky */ }
    void flapWings() { /* DOES NOTHING - forced to implement */ }
    void takeOff() { /* just launches */ }
}

// MiG-21 doesn't flap wings either!
class MiG21 implements Flyable {
    void fly() { /* jet propulsion */ }
    void flapWings() { /* MEANINGLESS for a jet */ }
    void takeOff() { /* runway takeoff */ }
}
```

**GOOD - Segregated Interfaces:**
```java
interface Flyable {
    void fly();
}

interface WingFlappable {
    void flapWings();
}

interface TakeOffable {
    void takeOff();
}

class Eagle implements Flyable, WingFlappable, TakeOffable {
    void fly() { /* soars */ }
    void flapWings() { /* flaps */ }
    void takeOff() { /* jumps and flaps */ }
}

class Superman implements Flyable {
    void fly() { /* zooms */ }
    // No flapWings needed!
}

class MiG21 implements Flyable, TakeOffable {
    void fly() { /* jet propulsion */ }
    void takeOff() { /* runway */ }
    // No flapWings needed!
}
```

---

### Example 2: Employee & UnpaidIntern (From Your Curriculum)

**BAD - Fat Interface:**
```java
interface Employee {
    String getEmail();
    void processPayment();
    double getSalary();
}

// UnpaidIntern doesn't get paid!
class UnpaidIntern implements Employee {
    String getEmail() { return "intern@company.com"; }
    void processPayment() { /* NOTHING to process! ISP violation */ }
    double getSalary() { return 0; /* forced to implement */ }
}
```

**GOOD - Segregated:**
```java
interface Employee {
    String getEmail();
    String getName();
}

interface Payable {
    void processPayment();
    double getSalary();
}

class FTE implements Employee, Payable {
    String getEmail() { return email; }
    String getName() { return name; }
    void processPayment() { /* process salary */ }
    double getSalary() { return salary; }
}

class UnpaidIntern implements Employee {
    String getEmail() { return email; }
    String getName() { return name; }
    // No processPayment() or getSalary() - clean!
}
```

---

### Example 3: Multifunction Printer

**BAD:**
```java
interface Machine {
    void print();
    void scan();
    void fax();
}

// Old printer can only print!
class OldPrinter implements Machine {
    void print() { /* works */ }
    void scan() { throw new UnsupportedOperationException(); }  // forced!
    void fax() { throw new UnsupportedOperationException(); }   // forced!
}
```

**GOOD:**
```java
interface Printer { void print(); }
interface Scanner { void scan(); }
interface Fax { void fax(); }

class OldPrinter implements Printer {
    void print() { /* works */ }
}

class MultiFunctionPrinter implements Printer, Scanner, Fax {
    void print() { /* works */ }
    void scan() { /* works */ }
    void fax() { /* works */ }
}
```

---

### Example 4: From Your Adapter PDF - SellerSearch

```java
// This interface is MINIMAL - only what the ranking client needs
public interface SellerSearch {
    List<Seller> getSellersBySku(String sku);
    Optional<Seller> getSellerWithMaxDiscount(String sku);
}
```
No fat interface with 20 methods. Just the 2 that the client actually uses. This is ISP in action.

---

### ISP Exam Tip
> If a class implements an interface and has to leave methods **empty** or throw `UnsupportedOperationException`, the interface is too fat. Split it. Look for methods that **some** implementors don't need.

---

## D - Dependency Inversion Principle (DIP)

> **"High-level modules should not depend on low-level modules. Both should depend on abstractions."**
> **"Depend upon abstractions, not concretions."**

Don't create objects of concrete classes inside other classes. Inject abstractions instead.

---

### Example 1: PaymentProcessor (From Your Curriculum)

**BAD - Depends on Concretion:**
```java
class SqlProductRepo {
    public Product getProductById(String id) {
        // SQL query to fetch product
    }
}

class PaymentProcessor {
    void pay(String productId) {
        SqlProductRepo repo = new SqlProductRepo();  // HARD DEPENDENCY!
        Product p = repo.getProductById(productId);
        // process payment
    }
}
```
**Problem:** If we switch to MongoDB, we must **modify** PaymentProcessor. High-level module (PaymentProcessor) depends on low-level module (SqlProductRepo).

**GOOD - Depends on Abstraction:**
```java
interface ProductRepo {
    Product getProductById(String productId);
}

class SqlProductRepo implements ProductRepo {
    public Product getProductById(String id) { /* SQL logic */ }
}

class MongoProductRepo implements ProductRepo {
    public Product getProductById(String id) { /* Mongo logic */ }
}

class PaymentProcessor {
    private ProductRepo repo;

    // INJECT the abstraction through constructor
    public PaymentProcessor(ProductRepo repo) {
        this.repo = repo;
    }

    void pay(String productId) {
        Product p = repo.getProductById(productId);
        // process payment - works with ANY repo!
    }
}
```

Usage:
```java
// Using SQL
PaymentProcessor pp = new PaymentProcessor(new SqlProductRepo());

// Switching to Mongo? Change ONE line:
PaymentProcessor pp = new PaymentProcessor(new MongoProductRepo());
```

---

### Example 2: From Your Adapter PDF - SellerRankingService

```java
// HIGH-LEVEL module depends on ABSTRACTION (SellerSearch interface)
public final class SellerRankingService {
    private final SellerSearch search;  // abstraction, not SDSellerSearchService!

    // Dependency injected via constructor
    public SellerRankingService(SellerSearch search) {
        this.search = search;
    }

    public List<Seller> rankBySku(String sku) {
        List<Seller> sellers = search.getSellersBySku(sku);  // works with ANY provider
        // ... ranking logic
    }
}

// Wiring - swap providers without touching SellerRankingService:
new SellerRankingService(new SDSearchAdapter(new SDSellerSearchService()));
new SellerRankingService(new ExSearchAdapter(new ExclusivelySellerSearchService()));
```

---

### Example 3: Notification Service

**BAD:**
```java
class OrderService {
    void placeOrder(Order order) {
        // ... order logic
        EmailService email = new EmailService();  // hard dependency
        email.send("Order placed: " + order.getId());
    }
}
```

**GOOD:**
```java
interface NotificationService {
    void send(String message);
}

class EmailNotification implements NotificationService {
    void send(String msg) { /* send email */ }
}

class SMSNotification implements NotificationService {
    void send(String msg) { /* send SMS */ }
}

class OrderService {
    private NotificationService notifier;

    OrderService(NotificationService notifier) {
        this.notifier = notifier;  // injected!
    }

    void placeOrder(Order order) {
        // ... order logic
        notifier.send("Order placed: " + order.getId());
    }
}
```

---

### Example 4: Logging

**BAD:**
```java
class UserService {
    void createUser(String name) {
        FileLogger logger = new FileLogger();  // concrete dependency
        logger.log("Creating user: " + name);
    }
}
```

**GOOD:**
```java
interface Logger {
    void log(String message);
}

class FileLogger implements Logger { void log(String msg) { /* write to file */ } }
class ConsoleLogger implements Logger { void log(String msg) { /* print to console */ } }
class CloudLogger implements Logger { void log(String msg) { /* send to cloud */ } }

class UserService {
    private Logger logger;

    UserService(Logger logger) {
        this.logger = logger;
    }

    void createUser(String name) {
        logger.log("Creating user: " + name);
    }
}
```

---

### DIP Exam Tip
> Look for `new ConcreteClass()` **inside** another class. That's almost always a DIP violation. The fix: **extract an interface** and **inject via constructor**.

---

## How SOLID Principles Connect (Big Picture)

```
Violation you spot         -->  Principle violated  -->  Fix
----------------------------------------------------------------------
Class does too many things  -->  SRP                -->  Split into focused classes
Adding feature needs        -->  OCP                -->  Use polymorphism
  editing existing code                                  (abstract class / interface)
Subclass breaks parent's    -->  LSP                -->  Rethink inheritance,
  contract                                               maybe use composition
Class forced to implement   -->  ISP                -->  Split fat interface into
  useless methods                                        thin ones
Class creates concrete      -->  DIP                -->  Depend on interface,
  objects internally                                     inject via constructor
```

---

## Exam-Style Practice Questions

### Q1: Identify all SOLID violations:
```java
class ReportGenerator {
    void generateReport(String type, List<Data> data) {
        String report;
        if (type.equals("PDF")) {
            report = formatAsPDF(data);
        } else if (type.equals("HTML")) {
            report = formatAsHTML(data);
        }
        
        MySQLDatabase db = new MySQLDatabase();
        db.save(report);
        
        EmailService email = new EmailService();
        email.send(report);
    }
}
```

**Answer:**
- **SRP**: Report generation, saving, and emailing are 3 responsibilities
- **OCP**: Adding a new format (CSV) requires modifying this class (new if/else)
- **DIP**: `new MySQLDatabase()` and `new EmailService()` are concrete dependencies

### Q2: Is this an LSP violation?
```java
class Duck extends Bird {
    void fly() { /* flies short distances */ }
    void swim() { /* swims */ }
}

class RubberDuck extends Duck {
    void fly() { throw new UnsupportedOperationException(); }
    void swim() { /* floats */ }
}
```

**Answer:** Yes! RubberDuck cannot be used wherever Duck is expected because `fly()` throws an exception. Code that does `duck.fly()` will crash if given a RubberDuck.

### Q3: Fix this ISP violation:
```java
interface Worker {
    void work();
    void eat();
    void sleep();
}

class Robot implements Worker {
    void work() { /* works */ }
    void eat() { /* robots don't eat! */ }
    void sleep() { /* robots don't sleep! */ }
}
```

**Answer:**
```java
interface Workable { void work(); }
interface Eatable { void eat(); }
interface Sleepable { void sleep(); }

class Human implements Workable, Eatable, Sleepable { ... }
class Robot implements Workable { ... }
```

---

## Quick Revision Table

| Principle | One-liner | Red Flag | Fix |
|-----------|-----------|----------|-----|
| **SRP** | One class, one reason to change | Monster methods, God classes, Util dumps | Split classes by responsibility |
| **OCP** | Add, don't modify | if/else chains for types | Polymorphism (abstract class/interface) |
| **LSP** | Child must honor parent's contract | `throw UnsupportedOperationException` in subclass | Rethink hierarchy, use composition |
| **ISP** | Don't force useless methods | Empty implementations, unused methods | Split into smaller interfaces |
| **DIP** | Depend on abstractions | `new ConcreteClass()` inside a class | Extract interface + constructor injection |

---

*References: SOLID Principles.pdf (Kshitij, Term 7), DesignPattern_Adapter.pdf (Adapter & DIP examples), Head First Design Patterns, GoF Design Patterns*
