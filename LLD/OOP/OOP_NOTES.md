# Object-Oriented Programming & Design

**Scope:** The four pillars of OOP -- Encapsulation, Abstraction, Inheritance, Polymorphism -- plus key supporting concepts (access modifiers, `this`, `super`, method overloading/overriding, abstract classes, interfaces, upcasting/downcasting).

**Language:** Java (all examples are self-contained and runnable).

---

## 1. Encapsulation

**One-liner:** Bundle data (fields) and the operations on that data (methods) into a single unit (class), and **restrict direct access** to the internals.

### The Problem (BAD)

```java
// BAD: Fields are public -- anyone can set invalid state
class BankAccount {
    public double balance;  // anyone can do: account.balance = -5000;
}

class Main {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount();
        acc.balance = -5000;  // No validation, no protection
        System.out.println(acc.balance);  // -5000 (broken state!)
    }
}
```

**What's wrong?** Any code can directly set `balance` to a negative value. There's no gatekeeper to enforce business rules.

### The Fix (GOOD)

```java
// GOOD: Fields are private, access via methods with validation
class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        if (initialBalance < 0) throw new IllegalArgumentException("Cannot start negative");
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        balance -= amount;
    }
}

class Main {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount(1000);
        acc.deposit(500);
        acc.withdraw(200);
        System.out.println(acc.getBalance());  // 1300.0
        // acc.balance = -5000;  // COMPILE ERROR: balance is private
    }
}
```

**Why it works:**
- `balance` is `private` -- only `BankAccount` itself can touch it.
- `deposit()` and `withdraw()` enforce rules before changing state.
- Outside code uses the **public API** and can never create invalid state.

### Access Modifiers (Java)

| Modifier | Same Class | Same Package | Subclass (other pkg) | Everywhere |
|-----------|:---:|:---:|:---:|:---:|
| `private` | Y | N | N | N |
| (default/package) | Y | Y | N | N |
| `protected` | Y | Y | Y | N |
| `public` | Y | Y | Y | Y |

**Exam Tip:** "Default" means NO keyword. `protected` allows subclass access even across packages. This table is a classic MCQ target.

### The `this` Keyword

`this` refers to the **current object**. Most common uses:

```java
class Student {
    private String name;
    private int age;

    // 1. Disambiguate field vs parameter
    public Student(String name, int age) {
        this.name = name;  // this.name = field, name = parameter
        this.age = age;
    }

    // 2. Call another constructor (constructor chaining)
    public Student(String name) {
        this(name, 18);  // calls the 2-arg constructor
    }

    // 3. Return current object for fluent API
    public Student setName(String name) {
        this.name = name;
        return this;
    }
}
```

**Exam Tip:** `this(...)` must be the **first statement** in a constructor. You cannot call both `this()` and `super()` in the same constructor.

---

## 2. Abstraction

**One-liner:** Hide **how** something works and expose only **what** it does. The caller doesn't need to know the internal implementation.

### Real-world analogy

You press the brake pedal (what) without knowing the hydraulic mechanism (how).

### The Problem (BAD)

```java
// BAD: Client code knows too much about the implementation
class EmailService {
    public void connectToSMTPServer(String host, int port) { /* ... */ }
    public void authenticate(String user, String pass) { /* ... */ }
    public void setMimeHeaders(String from, String to) { /* ... */ }
    public void encodeBody(String body) { /* ... */ }
    public void transmit() { /* ... */ }
    public void disconnect() { /* ... */ }
}

class Main {
    public static void main(String[] args) {
        EmailService svc = new EmailService();
        // Caller must know the exact sequence of 6 calls!
        svc.connectToSMTPServer("smtp.example.com", 587);
        svc.authenticate("user", "pass");
        svc.setMimeHeaders("a@b.com", "c@d.com");
        svc.encodeBody("Hello");
        svc.transmit();
        svc.disconnect();
    }
}
```

**What's wrong?** The caller is coupled to every internal step. If the implementation changes (e.g., switch from SMTP to an API), all callers break.

### The Fix (GOOD) -- Abstract Classes

```java
// GOOD: Caller sees only "send". Implementation is hidden.
abstract class NotificationService {
    // What it does (public contract)
    public abstract void send(String to, String message);

    // Shared helper (concrete method in abstract class)
    protected String formatTimestamp() {
        return java.time.Instant.now().toString();
    }
}

class EmailNotificationService extends NotificationService {
    @Override
    public void send(String to, String message) {
        // How it does it (hidden from caller)
        System.out.println("[" + formatTimestamp() + "] Email to " + to + ": " + message);
        // internally: connect, authenticate, set headers, encode, transmit, disconnect
    }
}

class SmsNotificationService extends NotificationService {
    @Override
    public void send(String to, String message) {
        System.out.println("[" + formatTimestamp() + "] SMS to " + to + ": " + message);
    }
}

class Main {
    public static void main(String[] args) {
        NotificationService svc = new EmailNotificationService();
        svc.send("user@example.com", "Your order shipped!");
        // Caller doesn't know or care about SMTP details
    }
}
```

### The Fix (GOOD) -- Interfaces

```java
// Interface: pure contract, no state, no constructor
interface PaymentProcessor {
    boolean charge(String customerId, double amount);
    boolean refund(String transactionId);
}

class StripeProcessor implements PaymentProcessor {
    @Override
    public boolean charge(String customerId, double amount) {
        System.out.println("Stripe charging " + customerId + " $" + amount);
        return true;
    }

    @Override
    public boolean refund(String transactionId) {
        System.out.println("Stripe refunding " + transactionId);
        return true;
    }
}

class RazorpayProcessor implements PaymentProcessor {
    @Override
    public boolean charge(String customerId, double amount) {
        System.out.println("Razorpay charging " + customerId + " Rs." + amount);
        return true;
    }

    @Override
    public boolean refund(String transactionId) {
        System.out.println("Razorpay refunding " + transactionId);
        return true;
    }
}

class CheckoutService {
    private final PaymentProcessor processor;

    // Depends on abstraction, not concrete class
    public CheckoutService(PaymentProcessor processor) {
        this.processor = processor;
    }

    public void checkout(String customerId, double amount) {
        processor.charge(customerId, amount);
    }
}
```

### Abstract Class vs Interface

| Feature | Abstract Class | Interface |
|---------|:---:|:---:|
| `extends` / `implements` | `extends` (single) | `implements` (multiple) |
| Constructors | Yes | No |
| Instance fields | Yes | No (only `static final`) |
| Concrete methods | Yes | Yes (via `default`, Java 8+) |
| Access modifiers on methods | Any | `public` (implicitly) |
| **When to use** | Shared state + partial implementation | Pure contract / capability |

**Exam Tip:** "Can a class extend an abstract class AND implement an interface?" -- **Yes.** `class Dog extends Animal implements Trainable { }`. This is a very common MCQ.

**Exam Tip:** Abstract classes **cannot be instantiated** directly. `new NotificationService()` gives a compile error if `NotificationService` is abstract.

---

## 3. Inheritance

**One-liner:** A child class **reuses** fields and methods from a parent class, and can **add or override** behavior.

### The Problem (BAD -- Code Duplication)

```java
// BAD: Copy-paste across similar classes
class Dog {
    String name;
    int age;
    void eat() { System.out.println(name + " eats"); }
    void sleep() { System.out.println(name + " sleeps"); }
    void bark() { System.out.println(name + " barks"); }
}

class Cat {
    String name;
    int age;
    void eat() { System.out.println(name + " eats"); }     // DUPLICATED
    void sleep() { System.out.println(name + " sleeps"); }  // DUPLICATED
    void meow() { System.out.println(name + " meows"); }
}
```

**What's wrong?** `name`, `age`, `eat()`, `sleep()` are duplicated. If you add a `health` field, you must edit every class.

### The Fix (GOOD -- Inheritance)

```java
// GOOD: Common stuff in parent, specific stuff in child
class Animal {
    protected String name;
    protected int age;

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void eat() { System.out.println(name + " eats"); }
    public void sleep() { System.out.println(name + " sleeps"); }
}

class Dog extends Animal {
    public Dog(String name, int age) {
        super(name, age);  // call parent constructor
    }

    public void bark() { System.out.println(name + " barks"); }
}

class Cat extends Animal {
    public Cat(String name, int age) {
        super(name, age);
    }

    public void meow() { System.out.println(name + " meows"); }
}

class Main {
    public static void main(String[] args) {
        Dog d = new Dog("Buddy", 3);
        d.eat();    // inherited from Animal
        d.bark();   // Dog's own method
    }
}
```

### The `super` Keyword

```java
class Vehicle {
    protected int speed;

    public Vehicle(int speed) {
        this.speed = speed;
    }

    public String describe() {
        return "Vehicle with speed " + speed;
    }
}

class Car extends Vehicle {
    private int doors;

    public Car(int speed, int doors) {
        super(speed);  // 1. Call parent constructor (MUST be first line)
        this.doors = doors;
    }

    @Override
    public String describe() {
        return super.describe() + ", doors=" + doors;  // 2. Call parent method
    }
}
```

**Exam Tip:** `super()` is automatically inserted by the compiler if you don't write it, but ONLY the no-arg version. If the parent has no no-arg constructor, you **must** explicitly call `super(args)`.

### Method Overriding Rules

| Rule | Detail |
|------|--------|
| Method signature | Must be **exactly the same** (name + parameter types) |
| Return type | Same or **covariant** (subtype) |
| Access | Same or **wider** (e.g., `protected` -> `public` OK, `public` -> `private` NOT OK) |
| `static` methods | Cannot override (they **hide**, not override) |
| `final` methods | Cannot be overridden |
| `private` methods | Not inherited, so not overridden |
| `@Override` | Optional but **strongly recommended** (catches typos at compile time) |

**Exam Tip:** "Can you narrow access when overriding?" -- **No.** This is a classic MCQ trap. If parent says `public`, child cannot say `protected`.

### Types of Inheritance in Java

| Type | Supported? | Example |
|------|:---:|---------|
| Single | Yes | `Dog extends Animal` |
| Multilevel | Yes | `Puppy extends Dog extends Animal` |
| Hierarchical | Yes | `Dog extends Animal`, `Cat extends Animal` |
| Multiple (classes) | **No** | Java does NOT allow `class C extends A, B` |
| Multiple (interfaces) | Yes | `class C implements X, Y` |

**Exam Tip:** "Why no multiple inheritance of classes?" -- **Diamond problem.** If `A` and `B` both have `method()`, and `C extends A, B`, which `method()` does `C` inherit? Java avoids this ambiguity. Interfaces solve it because the implementing class **must** provide the body.

### Composition vs Inheritance

```java
// INHERITANCE: "Car IS-A Vehicle" (tight coupling)
class Car extends Vehicle { }

// COMPOSITION: "Car HAS-A Engine" (loose coupling, preferred)
class Car {
    private final Engine engine;  // Car doesn't know Engine's internals

    public Car(Engine engine) {
        this.engine = engine;
    }

    public void start() {
        engine.ignite();
    }
}
```

**Exam Tip:** Design patterns (Strategy, Decorator, Adapter) all prefer **composition over inheritance**. If an examiner asks "what principle do design patterns follow?" -- this is a strong answer.

---

## 4. Polymorphism

**One-liner:** One interface, multiple behaviors. The **same method call** does different things depending on the object's actual type.

### Two Types

| Type | Mechanism | Resolved at | Also called |
|------|-----------|-------------|-------------|
| **Compile-time** | Method **overloading** | Compile time | Static polymorphism / Early binding |
| **Runtime** | Method **overriding** | Runtime | Dynamic polymorphism / Late binding / Dynamic dispatch |

### Compile-Time Polymorphism (Method Overloading)

```java
class Calculator {
    // Same name, different parameter lists
    public int add(int a, int b) {
        return a + b;
    }

    public double add(double a, double b) {
        return a + b;
    }

    public int add(int a, int b, int c) {
        return a + b + c;
    }
}

class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println(calc.add(1, 2));        // calls add(int, int) -> 3
        System.out.println(calc.add(1.5, 2.5));    // calls add(double, double) -> 4.0
        System.out.println(calc.add(1, 2, 3));     // calls add(int, int, int) -> 6
    }
}
```

**Overloading Rules:**
- **Must** differ in parameter types, count, or order.
- Return type alone does NOT count. `int add(int a)` and `double add(int a)` is a **compile error**.
- Access modifiers can differ.

**Exam Tip:** "Is changing only the return type overloading?" -- **No.** Compile error.

### Runtime Polymorphism (Method Overriding + Upcasting)

```java
class Shape {
    public double area() {
        return 0;
    }
}

class Circle extends Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    private double width, height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

class Main {
    public static void main(String[] args) {
        // Upcasting: parent reference, child object
        Shape s1 = new Circle(5);
        Shape s2 = new Rectangle(4, 6);

        // Same method call, different behavior
        System.out.println(s1.area());  // 78.54 (Circle's area)
        System.out.println(s2.area());  // 24.0  (Rectangle's area)

        // Works with arrays/collections too
        Shape[] shapes = { new Circle(3), new Rectangle(2, 5), new Circle(1) };
        for (Shape s : shapes) {
            System.out.println(s.area());  // JVM picks the right method at RUNTIME
        }
    }
}
```

**How it works:** The variable type is `Shape` (parent), but the actual object is `Circle` or `Rectangle`. At runtime, the JVM looks at the **actual object type** to decide which `area()` to call. This is called **dynamic dispatch**.

### Upcasting and Downcasting

```java
class Animal {
    public void eat() { System.out.println("Animal eats"); }
}

class Dog extends Animal {
    public void bark() { System.out.println("Dog barks"); }
}

class Main {
    public static void main(String[] args) {
        // UPCASTING: child -> parent (implicit, always safe)
        Animal a = new Dog();
        a.eat();    // OK -- Animal has eat()
        // a.bark(); // COMPILE ERROR -- Animal reference doesn't see bark()

        // DOWNCASTING: parent -> child (explicit, risky)
        if (a instanceof Dog) {
            Dog d = (Dog) a;  // safe because we checked
            d.bark();  // OK now
        }

        // DANGEROUS downcast without check
        Animal a2 = new Animal();
        // Dog d2 = (Dog) a2;  // RUNTIME ERROR: ClassCastException!
    }
}
```

**Exam Tip:** Upcasting is **implicit and safe**. Downcasting is **explicit and risky** -- always use `instanceof` first. Classic MCQ: "What happens when you downcast an Animal that's not a Dog?" -- `ClassCastException` at runtime, NOT a compile error.

### Polymorphism with Interfaces

```java
interface Drawable {
    void draw();
}

class Circle implements Drawable {
    @Override
    public void draw() { System.out.println("Drawing circle"); }
}

class Square implements Drawable {
    @Override
    public void draw() { System.out.println("Drawing square"); }
}

class Canvas {
    public void render(Drawable[] items) {
        for (Drawable d : items) {
            d.draw();  // polymorphic call
        }
    }
}
```

---

## 5. Putting It All Together -- Example

```java
// ABSTRACTION: interface defines what, not how
interface Sortable {
    void sort(int[] data);
    String name();
}

// ENCAPSULATION: internal state is hidden
class BubbleSorter implements Sortable {
    private int swapCount = 0;  // private -- encapsulated

    @Override
    public void sort(int[] data) {
        swapCount = 0;
        for (int i = 0; i < data.length - 1; i++) {
            for (int j = 0; j < data.length - 1 - i; j++) {
                if (data[j] > data[j + 1]) {
                    int tmp = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = tmp;
                    swapCount++;
                }
            }
        }
    }

    @Override
    public String name() { return "BubbleSort"; }

    public int getSwapCount() { return swapCount; }
}

// INHERITANCE: QuickSorter could extend a base AbstractSorter
// POLYMORPHISM: caller uses Sortable reference, JVM picks the right sort()
class SortDemo {
    public static void benchmark(Sortable sorter, int[] data) {
        sorter.sort(data);  // polymorphic call
        System.out.println(sorter.name() + " sorted " + data.length + " elements");
    }
}
```

---

## How OOP Connects to SOLID and Design Patterns

| OOP Pillar | SOLID Connection | Design Pattern Example |
|------------|-----------------|----------------------|
| **Encapsulation** | SRP (each class owns its data) | Builder (hides construction steps) |
| **Abstraction** | DIP (depend on abstractions) | Strategy (interface hides algorithm details) |
| **Inheritance** | LSP (subtypes must be substitutable) | Factory Method (subclass overrides creation) |
| **Polymorphism** | OCP (extend via new classes, not modifying old ones) | Decorator, Adapter, Strategy (all rely on polymorphic dispatch) |

---

## Quick Revision Table

| Concept | What It Means | Key Mechanism | Classic Exam Trap |
|---------|--------------|--------------|-------------------|
| Encapsulation | Hide data, expose API | `private` fields + public methods | "Encapsulation = getters/setters" -- **incomplete**. It's about controlling access, not just having getters. |
| Abstraction | Hide implementation | Abstract classes / Interfaces | "Abstract class vs Interface?" -- know the table above cold |
| Inheritance | Reuse + extend | `extends` / `super` | "Can you narrow access when overriding?" -- **No** |
| Compile-time poly. | Overloading | Same name, different params | "Changing only return type?" -- **Not overloading** |
| Runtime poly. | Overriding + upcasting | Dynamic dispatch | "Which method runs?" -- depends on **actual object type**, not reference type |
| Upcasting | child -> parent ref | Implicit, always safe | Can't access child-specific methods |
| Downcasting | parent -> child ref | Explicit, needs `instanceof` | `ClassCastException` if wrong |

---

## Exam Tips

- **Encapsulation != just private fields.** It's private fields + controlled access via methods + validation.
- **Abstract class with all concrete methods is legal.** It just can't be instantiated.
- **Interface with `default` methods is legal since Java 8.** But it's still primarily a contract.
- **`final` class** cannot be extended. `final` method cannot be overridden. `final` variable cannot be reassigned.
- **Constructor is NOT inherited.** Child must call `super()` explicitly or implicitly.
- **`static` methods belong to the class**, not the object. They are **hidden**, not overridden.
- When asked "what type of polymorphism is this?" -- check: is it overloading (compile-time) or overriding (runtime)?
- **Composition over inheritance** is the design mantra. Know why: looser coupling, easier testing, no diamond problem.
