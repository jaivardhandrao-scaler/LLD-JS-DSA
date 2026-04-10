# OOP Coding Exam Questions

> 3 problems. Each has: problem code with issues, your task, and a complete solution.

---

## Problem 1: Fix the Broken Shape Hierarchy

**Difficulty:** Medium

### Problem Code

The following code is meant to represent a shape hierarchy where you can compute the area of different shapes polymorphically. It has **multiple OOP violations**. Identify and fix them all.

```java
class Shape {
    public String type;
    public double radius;
    public double width;
    public double height;

    public double computeArea() {
        if (type.equals("circle")) {
            return Math.PI * radius * radius;
        } else if (type.equals("rectangle")) {
            return width * height;
        } else if (type.equals("triangle")) {
            return 0.5 * width * height;
        }
        return 0;
    }
}

class Main {
    public static void main(String[] args) {
        Shape s1 = new Shape();
        s1.type = "circle";
        s1.radius = 5;

        Shape s2 = new Shape();
        s2.type = "rectangle";
        s2.width = 4;
        s2.height = 6;

        Shape[] shapes = {s1, s2};
        for (Shape s : shapes) {
            System.out.println(s.computeArea());
        }
    }
}
```

### Your Task

1. List every OOP violation in the code above.
2. Rewrite it using proper inheritance, encapsulation, abstraction, and polymorphism.
3. Make `Shape` abstract with an abstract `computeArea()` method.
4. Create `Circle`, `Rectangle`, and `Triangle` subclasses.
5. Use `private` fields with constructor initialization.
6. The `Main` class should use polymorphic references.

### Solution

**Violations found:**
- **No encapsulation:** All fields are `public` and can be set to invalid values (negative radius).
- **No abstraction:** One class does everything via `if/else` on a type string.
- **No inheritance:** No subclasses, no specialization.
- **No polymorphism:** A single `computeArea()` method with string-based branching instead of overriding.
- `type` field is used for manual dispatch -- this is what runtime polymorphism is designed to replace.
- Unused fields per shape (a circle doesn't need `width`/`height`).

```java
// ABSTRACT BASE: defines the contract
abstract class Shape {
    public abstract double computeArea();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [area=" + String.format("%.2f", computeArea()) + "]";
    }
}

// CIRCLE: encapsulates radius, validates input
class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        if (radius <= 0) throw new IllegalArgumentException("Radius must be positive");
        this.radius = radius;
    }

    public double getRadius() { return radius; }

    @Override
    public double computeArea() {
        return Math.PI * radius * radius;
    }
}

// RECTANGLE: encapsulates width and height
class Rectangle extends Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Dimensions must be positive");
        this.width = width;
        this.height = height;
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

    @Override
    public double computeArea() {
        return width * height;
    }
}

// TRIANGLE: encapsulates base and height
class Triangle extends Shape {
    private final double base;
    private final double height;

    public Triangle(double base, double height) {
        if (base <= 0 || height <= 0) throw new IllegalArgumentException("Dimensions must be positive");
        this.base = base;
        this.height = height;
    }

    @Override
    public double computeArea() {
        return 0.5 * base * height;
    }
}

class Main {
    public static void main(String[] args) {
        // Polymorphic references: parent type, child objects
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(4, 6),
            new Triangle(3, 8)
        };

        for (Shape s : shapes) {
            System.out.println(s);  // dynamic dispatch calls the right computeArea()
        }
        // Output:
        // Circle [area=78.54]
        // Rectangle [area=24.00]
        // Triangle [area=12.00]
    }
}
```

**What this demonstrates:**
- **Encapsulation:** Fields are `private final`, validated in constructor.
- **Abstraction:** `Shape` is abstract -- callers don't know what kind of shape they have.
- **Inheritance:** `Circle`, `Rectangle`, `Triangle` extend `Shape`.
- **Polymorphism:** `for (Shape s : shapes) { s.computeArea(); }` calls the correct version at runtime.

---

## Problem 2: Design a Payment System with Interfaces

**Difficulty:** Medium-Hard

### Problem Code

A checkout system needs to support multiple payment methods. The current code uses a single class with `if/else`:

```java
class PaymentService {
    public void pay(String method, double amount) {
        if (method.equals("credit")) {
            System.out.println("Charging credit card: $" + amount);
            System.out.println("Applying 2% processing fee");
            double total = amount * 1.02;
            System.out.println("Total: $" + total);
        } else if (method.equals("upi")) {
            System.out.println("UPI payment: Rs." + amount);
            System.out.println("No processing fee");
        } else if (method.equals("wallet")) {
            System.out.println("Wallet debit: $" + amount);
            System.out.println("Checking wallet balance...");
        }
    }
}
```

### Your Task

1. Identify which OOP principles and SOLID principles this code violates.
2. Refactor using:
   - A `PaymentMethod` interface with a `pay(double amount)` method and a `name()` method.
   - Concrete classes: `CreditCardPayment`, `UpiPayment`, `WalletPayment`.
   - A `CheckoutService` that depends on the `PaymentMethod` interface (constructor injection).
3. Show that adding a new payment method (e.g., `NetBankingPayment`) requires ZERO changes to `CheckoutService`.

### Solution

**Violations:**
- **SRP:** One class handles credit card logic, UPI logic, and wallet logic.
- **OCP:** Adding a new method (e.g., net banking) requires editing the `if/else` chain.
- **DIP:** The service depends on string-based concrete logic instead of an abstraction.
- **No polymorphism:** String-based dispatch instead of dynamic dispatch.
- **No encapsulation:** Payment-specific details (fee %, balance check) are not encapsulated in their own classes.

```java
// INTERFACE: contract for all payment methods
interface PaymentMethod {
    void pay(double amount);
    String name();
}

// CONCRETE: Credit Card with processing fee
class CreditCardPayment implements PaymentMethod {
    private static final double FEE_RATE = 0.02;

    @Override
    public void pay(double amount) {
        double fee = amount * FEE_RATE;
        double total = amount + fee;
        System.out.println("Charging credit card: $" + amount);
        System.out.println("Processing fee (2%): $" + String.format("%.2f", fee));
        System.out.println("Total charged: $" + String.format("%.2f", total));
    }

    @Override
    public String name() { return "Credit Card"; }
}

// CONCRETE: UPI with no fee
class UpiPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {
        System.out.println("UPI payment: Rs." + amount);
        System.out.println("No processing fee");
    }

    @Override
    public String name() { return "UPI"; }
}

// CONCRETE: Wallet with balance check
class WalletPayment implements PaymentMethod {
    private double balance;

    public WalletPayment(double initialBalance) {
        this.balance = initialBalance;
    }

    @Override
    public void pay(double amount) {
        if (amount > balance) {
            System.out.println("Wallet: Insufficient balance ($" + balance + ")");
            return;
        }
        balance -= amount;
        System.out.println("Wallet debit: $" + amount + " (remaining: $" + balance + ")");
    }

    @Override
    public String name() { return "Wallet"; }
}

// ADDING A NEW METHOD: zero changes to CheckoutService!
class NetBankingPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {
        System.out.println("Net Banking transfer: $" + amount);
        System.out.println("Redirecting to bank portal...");
    }

    @Override
    public String name() { return "Net Banking"; }
}

// SERVICE: depends on abstraction (PaymentMethod), not on concrete classes
class CheckoutService {
    private final PaymentMethod paymentMethod;

    // Constructor injection -- DIP in action
    public CheckoutService(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void checkout(double amount) {
        System.out.println("--- Checkout via " + paymentMethod.name() + " ---");
        paymentMethod.pay(amount);
        System.out.println();
    }
}

class Main {
    public static void main(String[] args) {
        // Swap payment method by passing different implementations
        new CheckoutService(new CreditCardPayment()).checkout(100.0);
        new CheckoutService(new UpiPayment()).checkout(500.0);
        new CheckoutService(new WalletPayment(200.0)).checkout(150.0);
        new CheckoutService(new NetBankingPayment()).checkout(1000.0);
    }
}
```

**Output:**
```
--- Checkout via Credit Card ---
Charging credit card: $100.0
Processing fee (2%): $2.00
Total charged: $102.00

--- Checkout via UPI ---
UPI payment: Rs.500.0
No processing fee

--- Checkout via Wallet ---
Wallet debit: $150.0 (remaining: $50.0)

--- Checkout via Net Banking ---
Net Banking transfer: $1000.0
Redirecting to bank portal...
```

**Key points for the examiner:**
- `CheckoutService` was **never modified** when `NetBankingPayment` was added (OCP).
- Each payment class handles only its own logic (SRP).
- `CheckoutService` depends on `PaymentMethod` interface, not concrete classes (DIP).
- Polymorphism drives the dispatch -- no `if/else` on strings.

---

## Problem 3: Spot and Fix All OOP Bugs

**Difficulty:** Hard

### Problem Code

This code has **6 distinct OOP-related bugs** (compile errors, runtime errors, or design violations). Find and fix all of them.

```java
abstract class Vehicle {
    private String brand;
    private int year;

    public Vehicle(String brand, int year) {
        this.brand = brand;
        this.year = year;
    }

    public abstract String fuelType();

    public String describe() {
        return brand + " (" + year + ") - " + fuelType();
    }
}

class ElectricCar extends Vehicle {
    private int batteryCapacity;

    // Bug 1 is here
    public ElectricCar(String brand, int year, int batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    // Bug 2 is here
    public String fueltype() {
        return "Electric (" + batteryCapacity + " kWh)";
    }

    // Bug 3 is here
    private String describe() {
        return super.describe() + " Battery: " + batteryCapacity;
    }
}

class GasCar extends Vehicle {
    private double engineSize;

    public GasCar(String brand, int year, double engineSize) {
        super(brand, year);
        this.engineSize = engineSize;
    }

    @Override
    public String fuelType() {
        return "Gasoline (" + engineSize + "L)";
    }
}

class Main {
    public static void main(String[] args) {
        // Bug 4 is here
        Vehicle v1 = new Vehicle("Generic", 2024) {
            public String fuelType() { return "Unknown"; }
        };

        Vehicle v2 = new ElectricCar("Tesla", 2024, 75);
        Vehicle v3 = new GasCar("Toyota", 2024, 2.0);

        Vehicle[] fleet = {v1, v2, v3};
        for (Vehicle v : fleet) {
            System.out.println(v.describe());
        }

        // Bug 5 is here
        GasCar g = (GasCar) v2;

        // Bug 6 is here
        ElectricCar e = (ElectricCar) v3;
    }
}
```

### Your Task

1. Identify all 6 bugs with their line and explanation.
2. Provide the corrected code.

### Solution

**Bug 1: Missing `super()` call**
`ElectricCar` constructor doesn't call `super(brand, year)`. Since `Vehicle` has no no-arg constructor, this is a **compile error**.
Fix: Add `super(brand, year);` as the first line.

**Bug 2: Method name typo -- `fueltype()` instead of `fuelType()`**
Java is case-sensitive. This defines a NEW method instead of overriding the abstract one. Since `fuelType()` is abstract and not overridden, `ElectricCar` would still be abstract -- **compile error**.
Fix: Rename to `fuelType()` and add `@Override`.

**Bug 3: Narrowing access on override -- `private` instead of `public`**
`describe()` is `public` in `Vehicle`. Overriding it as `private` narrows access, which is **not allowed** -- **compile error**.
Fix: Keep it `public`.

**Bug 4: Anonymous class instantiation of abstract class (design issue)**
This is technically legal Java (anonymous subclass), but it's a **design smell**. If someone reads the code, they might think `Vehicle` is being directly instantiated. Not a compile error, but worth noting.
Fix: Either leave with a comment explaining it's an anonymous subclass, or create a proper `UnknownVehicle` class.

**Bug 5: Bad downcast -- `v2` is an `ElectricCar`, not a `GasCar`**
`(GasCar) v2` will throw `ClassCastException` at runtime because `v2` is actually an `ElectricCar`.
Fix: Use `instanceof` check or cast to the correct type.

**Bug 6: Bad downcast -- `v3` is a `GasCar`, not an `ElectricCar`**
Same issue. `(ElectricCar) v3` throws `ClassCastException` at runtime.
Fix: Use `instanceof` or fix the types.

### Corrected Code

```java
abstract class Vehicle {
    private String brand;
    private int year;

    public Vehicle(String brand, int year) {
        this.brand = brand;
        this.year = year;
    }

    public abstract String fuelType();

    public String describe() {
        return brand + " (" + year + ") - " + fuelType();
    }
}

class ElectricCar extends Vehicle {
    private int batteryCapacity;

    public ElectricCar(String brand, int year, int batteryCapacity) {
        super(brand, year);  // FIX 1: call parent constructor
        this.batteryCapacity = batteryCapacity;
    }

    @Override  // FIX 2: correct method name + @Override annotation
    public String fuelType() {
        return "Electric (" + batteryCapacity + " kWh)";
    }

    @Override  // FIX 3: access must be public (same as parent)
    public String describe() {
        return super.describe() + " Battery: " + batteryCapacity;
    }
}

class GasCar extends Vehicle {
    private double engineSize;

    public GasCar(String brand, int year, double engineSize) {
        super(brand, year);
        this.engineSize = engineSize;
    }

    @Override
    public String fuelType() {
        return "Gasoline (" + engineSize + "L)";
    }
}

class Main {
    public static void main(String[] args) {
        Vehicle v2 = new ElectricCar("Tesla", 2024, 75);
        Vehicle v3 = new GasCar("Toyota", 2024, 2.0);

        Vehicle[] fleet = {v2, v3};
        for (Vehicle v : fleet) {
            System.out.println(v.describe());
        }

        // FIX 5 & 6: safe downcasting with instanceof
        for (Vehicle v : fleet) {
            if (v instanceof ElectricCar) {
                ElectricCar e = (ElectricCar) v;
                System.out.println("Found electric: " + e.describe());
            } else if (v instanceof GasCar) {
                GasCar g = (GasCar) v;
                System.out.println("Found gas: " + g.describe());
            }
        }
    }
}
```

**Output:**
```
Tesla (2024) - Electric (75 kWh) Battery: 75
Toyota (2024) - Gasoline (2.0L)
Found electric: Tesla (2024) - Electric (75 kWh) Battery: 75
Found gas: Toyota (2024) - Gasoline (2.0L)
```

**What this tests:**
- Constructor chaining with `super()` (Inheritance)
- `@Override` catching typos (Polymorphism)
- Access modifier rules in overriding (Inheritance + Encapsulation)
- Safe downcasting with `instanceof` (Polymorphism)
- Abstract classes and abstract method contracts (Abstraction)
