# OOP MCQ Quiz

> 25 questions. Try to answer before expanding. Score yourself at the end.

---

### Q1. What is the output?

```java
class A {
    public void show() { System.out.println("A"); }
}
class B extends A {
    public void show() { System.out.println("B"); }
}
class Main {
    public static void main(String[] args) {
        A obj = new B();
        obj.show();
    }
}
```

a) A  
b) B  
c) Compile error  
d) Runtime error  

<details><summary>Answer</summary>

**b) B**

`obj` is declared as `A` but the actual object is `B`. Method overriding + dynamic dispatch means the JVM calls `B.show()` at runtime.

</details>

---

### Q2. Which of these is NOT a valid way to achieve polymorphism in Java?

a) Method overloading  
b) Method overriding  
c) Operator overloading  
d) Interface implementation  

<details><summary>Answer</summary>

**c) Operator overloading**

Java does not support operator overloading (unlike C++). The `+` for String concatenation is built into the language, not user-definable.

</details>

---

### Q3. What happens here?

```java
class Parent {
    public int getValue() { return 1; }
}
class Child extends Parent {
    public double getValue() { return 2.0; }
}
```

a) Compiles fine -- overloading  
b) Compiles fine -- overriding with covariant return  
c) Compile error  
d) Runtime error  

<details><summary>Answer</summary>

**c) Compile error**

`double` is NOT a subtype of `int` (they're primitives, not objects). Covariant returns only work with reference types (e.g., `Animal` -> `Dog`). This is neither valid overriding nor overloading (same parameters).

</details>

---

### Q4. What is encapsulation primarily about?

a) Hiding implementation details  
b) Controlling access to an object's internal state  
c) Making all fields static  
d) Using inheritance to reuse code  

<details><summary>Answer</summary>

**b) Controlling access to an object's internal state**

Encapsulation bundles data and methods together and restricts access via access modifiers. Option (a) describes abstraction. Encapsulation is the mechanism that supports abstraction.

</details>

---

### Q5. Which access modifier allows access from a subclass in a different package?

a) `private`  
b) default (no modifier)  
c) `protected`  
d) Both b and c  

<details><summary>Answer</summary>

**c) `protected`**

Default (package-private) does NOT allow access from subclasses in other packages. Only `protected` and `public` do.

</details>

---

### Q6. What is the output?

```java
class Animal {
    public static void speak() { System.out.println("Animal"); }
}
class Dog extends Animal {
    public static void speak() { System.out.println("Dog"); }
}
class Main {
    public static void main(String[] args) {
        Animal a = new Dog();
        a.speak();
    }
}
```

a) Animal  
b) Dog  
c) Compile error  
d) Runtime error  

<details><summary>Answer</summary>

**a) Animal**

Static methods are not overridden -- they are **hidden**. The call is resolved based on the **reference type** (`Animal`), not the actual object type. This is the opposite of dynamic dispatch.

</details>

---

### Q7. Can you instantiate an abstract class?

a) Yes, always  
b) Yes, but only if it has no abstract methods  
c) No, never directly  
d) No, unless you use `new` with the class name  

<details><summary>Answer</summary>

**c) No, never directly**

An abstract class cannot be instantiated even if all its methods are concrete. You can create an anonymous subclass: `new AbstractClass() { ... }`, but that's instantiating a subclass, not the abstract class itself.

</details>

---

### Q8. What is the output?

```java
class X {
    int a = 10;
    X() {
        show();
    }
    void show() { System.out.println("X: " + a); }
}
class Y extends X {
    int b = 20;
    void show() { System.out.println("Y: " + b); }
}
class Main {
    public static void main(String[] args) {
        new Y();
    }
}
```

a) X: 10  
b) Y: 20  
c) Y: 0  
d) Compile error  

<details><summary>Answer</summary>

**c) Y: 0**

When `new Y()` is called, `X`'s constructor runs first and calls `show()`. Due to dynamic dispatch, `Y.show()` executes. But `Y`'s field `b` hasn't been initialized yet (still default `0`). This is why calling overridable methods from constructors is dangerous.

</details>

---

### Q9. Which statement about method overriding is FALSE?

a) The return type must be the same or covariant  
b) The access modifier can be more restrictive  
c) `@Override` annotation is optional but recommended  
d) The method signature must be exactly the same  

<details><summary>Answer</summary>

**b) The access modifier can be more restrictive**

This is FALSE. When overriding, access must be the same or **wider**, not more restrictive. You can go from `protected` to `public`, but not from `public` to `private`.

</details>

---

### Q10. What is the output?

```java
class A {
    public void foo(int x) { System.out.println("int"); }
    public void foo(double x) { System.out.println("double"); }
}
class Main {
    public static void main(String[] args) {
        A a = new A();
        a.foo(5);
    }
}
```

a) int  
b) double  
c) Compile error (ambiguous)  
d) Runtime error  

<details><summary>Answer</summary>

**a) int**

`5` is an `int` literal. The compiler picks the most specific matching overload: `foo(int)`. If `foo(int)` didn't exist, it would widen to `foo(double)`.

</details>

---

### Q11. Java supports multiple inheritance through:

a) Classes  
b) Interfaces  
c) Both classes and interfaces  
d) Neither  

<details><summary>Answer</summary>

**b) Interfaces**

Java does not allow a class to extend multiple classes (diamond problem). But a class can implement multiple interfaces, which is Java's form of multiple inheritance of type/contract.

</details>

---

### Q12. What does `super()` do when not explicitly written in a constructor?

a) Nothing -- no parent constructor is called  
b) The compiler automatically inserts a call to the parent's no-arg constructor  
c) It calls the parent's constructor with the same parameters  
d) Compile error  

<details><summary>Answer</summary>

**b) The compiler automatically inserts a call to the parent's no-arg constructor**

If you don't write `super(...)`, the compiler inserts `super()` (no-arg). If the parent doesn't have a no-arg constructor, you get a compile error.

</details>

---

### Q13. What is the result?

```java
Animal a = new Cat();
Dog d = (Dog) a;
```

a) Compile error  
b) Runs fine  
c) `ClassCastException` at runtime  
d) `NullPointerException`  

<details><summary>Answer</summary>

**c) `ClassCastException` at runtime**

The compiler allows the cast (since `Dog` could theoretically be a subclass of `Animal`), but at runtime the actual object is a `Cat`, not a `Dog`. Always use `instanceof` before downcasting.

</details>

---

### Q14. Which of these is method overloading?

a) Same name, same parameters, different return type  
b) Same name, different parameters  
c) Different name, same parameters  
d) Same name, same parameters, in parent and child class  

<details><summary>Answer</summary>

**b) Same name, different parameters**

(a) is a compile error -- return type alone doesn't differentiate overloads. (c) is just two different methods. (d) is method overriding.

</details>

---

### Q15. A `final` class:

a) Cannot have methods  
b) Cannot be instantiated  
c) Cannot be inherited (extended)  
d) Cannot have final methods  

<details><summary>Answer</summary>

**c) Cannot be inherited (extended)**

A `final` class can be instantiated, have methods, have final methods, etc. It simply cannot be subclassed. Example: `String` is a final class in Java.

</details>

---

### Q16. What is the output?

```java
interface I {
    default void greet() { System.out.println("Hello from I"); }
}
class C implements I {
    public void greet() { System.out.println("Hello from C"); }
}
class Main {
    public static void main(String[] args) {
        I obj = new C();
        obj.greet();
    }
}
```

a) Hello from I  
b) Hello from C  
c) Compile error  
d) Runtime error  

<details><summary>Answer</summary>

**b) Hello from C**

`C` overrides the default method from `I`. Dynamic dispatch picks `C`'s version at runtime. Default methods in interfaces can be overridden just like regular methods.

</details>

---

### Q17. Which is true about constructors?

a) Constructors are inherited by child classes  
b) Constructors can be abstract  
c) Constructors can call other constructors using `this()`  
d) Constructors can be called multiple times on the same object  

<details><summary>Answer</summary>

**c) Constructors can call other constructors using `this()`**

(a) is false -- constructors are never inherited. (b) is false -- constructors cannot be abstract. (d) is false -- constructors run once during object creation.

</details>

---

### Q18. What type of polymorphism is this?

```java
void print(String s) { ... }
void print(int n) { ... }
void print(String s, int n) { ... }
```

a) Runtime polymorphism  
b) Compile-time polymorphism  
c) Ad-hoc polymorphism  
d) Both b and c  

<details><summary>Answer</summary>

**d) Both b and c**

Method overloading is compile-time polymorphism (resolved by the compiler based on parameter types). It's also called ad-hoc polymorphism in academic terminology.

</details>

---

### Q19. Which keyword prevents method overriding?

a) `abstract`  
b) `static`  
c) `final`  
d) Both b and c  

<details><summary>Answer</summary>

**c) `final`**

A `final` method cannot be overridden. `static` methods aren't overridden either (they're hidden), but that's a different mechanism. `abstract` actually *requires* overriding. The most precise answer is `final`.

</details>

---

### Q20. What is the output?

```java
class A { }
class B extends A { }
class Main {
    public static void main(String[] args) {
        B b = new B();
        System.out.println(b instanceof A);
        System.out.println(b instanceof B);
    }
}
```

a) true, true  
b) true, false  
c) false, true  
d) Compile error  

<details><summary>Answer</summary>

**a) true, true**

`b` is an instance of `B` and also an instance of `A` (because `B extends A`). `instanceof` checks the entire inheritance chain.

</details>

---

### Q21. An interface in Java can have:

a) Instance fields  
b) Constructors  
c) `static final` constants  
d) `private` instance methods  

<details><summary>Answer</summary>

**c) `static final` constants**

Interfaces cannot have instance fields (a) or constructors (b). They can have `private` methods (since Java 9) but these are helper methods, not instance methods in the traditional sense. All fields in an interface are implicitly `public static final`.

</details>

---

### Q22. What concept does this demonstrate?

```java
Shape s = new Circle();
s.draw();  // calls Circle's draw
```

a) Encapsulation  
b) Abstraction  
c) Runtime polymorphism  
d) Method overloading  

<details><summary>Answer</summary>

**c) Runtime polymorphism**

A parent reference (`Shape`) holds a child object (`Circle`). The method called is determined at runtime based on the actual object type. This is the textbook example of runtime polymorphism (dynamic dispatch).

</details>

---

### Q23. Which is TRUE about abstract methods?

a) They can have a body  
b) They can be `private`  
c) They can be `static`  
d) They must be overridden by the first concrete subclass  

<details><summary>Answer</summary>

**d) They must be overridden by the first concrete subclass**

Abstract methods have no body (a is false). They cannot be `private` (b) because subclasses need to see them to override them. They cannot be `static` (c) because static methods belong to the class and can't be overridden.

</details>

---

### Q24. What is the output?

```java
class P {
    protected void show() { System.out.println("P"); }
}
class C extends P {
    public void show() { System.out.println("C"); }
}
```

a) Compile error -- can't widen access  
b) Compiles fine -- widening access is allowed  
c) Compile error -- can't override protected  
d) Runtime error  

<details><summary>Answer</summary>

**b) Compiles fine -- widening access is allowed**

When overriding, you can make the access modifier the same or wider. `protected` -> `public` is widening, which is legal. Going the other way (`public` -> `protected`) would be a compile error.

</details>

---

### Q25. "Favor composition over inheritance" is important because:

a) Inheritance is slower at runtime  
b) Composition avoids the diamond problem and provides looser coupling  
c) Java doesn't support inheritance  
d) Composition doesn't use polymorphism  

<details><summary>Answer</summary>

**b) Composition avoids the diamond problem and provides looser coupling**

Composition (HAS-A) lets you combine behaviors from multiple sources, swap implementations at runtime, and avoids tight coupling between parent and child. Design patterns like Strategy, Decorator, and Adapter all demonstrate this principle.

</details>

---

## Self-Scoring

| Score | Level |
|-------|-------|
| 22-25 | Exam ready |
| 18-21 | Strong, review the ones you missed |
| 14-17 | Revisit the notes, focus on polymorphism and overriding rules |
| Below 14 | Re-read OOP_NOTES.md thoroughly before retaking |
