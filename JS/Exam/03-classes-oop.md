# ES6 Classes, Inheritance & OOP Reality in JS (Class 4)

---

## 1. Classes Are Syntactic Sugar -- See It Yourself

Before memorizing definitions, look at these two blocks. They do **the exact same thing**.

### Old Way: Constructor Function + Prototype

```js
function Person(name, age) {
  this.name = name;
  this.age = age;
}

Person.prototype.greet = function () {
  return `Hi, I'm ${this.name}`;
};

const p = new Person("Alice", 25);
console.log(p.greet()); // "Hi, I'm Alice"
```

### New Way: ES6 Class

```js
class Person {
  constructor(name, age) {
    this.name = name;
    this.age = age;
  }

  greet() {
    return `Hi, I'm ${this.name}`;
  }
}

const p = new Person("Alice", 25);
console.log(p.greet()); // "Hi, I'm Alice"
```

### Proof that a class IS a function

```js
class Dog {}
console.log(typeof Dog); // "function"
console.log(Dog === Dog.prototype.constructor); // true
```

The `class` keyword creates a constructor function and attaches methods to its `.prototype`. That's it. There is **no new object model** -- it is the same prototype chain underneath.

---

## 2. Key Differences: Class vs Constructor Function

Even though classes are sugar, they come with **stricter rules**. Exams love testing these.

| Behavior | Constructor Function | ES6 Class |
|---|---|---|
| Call without `new` | Returns `undefined` (no error) | **Throws TypeError** |
| Hoisting | Function is hoisted | **Not hoisted** (TDZ like `let`) |
| Method enumerability | Enumerable (shows in `for...in`) | **Non-enumerable** |
| Strict mode | Only if you write `"use strict"` | **Automatic strict mode** inside class body |
| `typeof` | `"function"` | `"function"` |

### BAD: Calling a class without `new`

```js
class Car {
  constructor(make) {
    this.make = make;
  }
}

const c = Car("Toyota"); // TypeError: Class constructor Car cannot be invoked without 'new'
```

### GOOD: Always use `new`

```js
const c = new Car("Toyota"); // works
```

### BAD: Relying on class being hoisted

```js
const a = new Animal("Cat"); // ReferenceError -- TDZ!

class Animal {
  constructor(type) {
    this.type = type;
  }
}
```

### GOOD: Declare the class before you use it

```js
class Animal {
  constructor(type) {
    this.type = type;
  }
}

const a = new Animal("Cat"); // works
```

### Methods are non-enumerable

```js
class Box {
  open() {}
}

const b = new Box();
for (let key in b) {
  console.log(key); // nothing prints -- open is non-enumerable
}

// Compare with constructor function:
function Box2() {}
Box2.prototype.open = function () {};
const b2 = new Box2();
for (let key in b2) {
  console.log(key); // "open" -- enumerable by default
}
```

> **Exam Tip:** If a question asks "name 4 differences between class and constructor function," these four are the answer: `new` requirement, hoisting/TDZ, non-enumerable methods, automatic strict mode.

---

## 3. `extends` and `super`

### Full Example: Shape -> Circle

```js
class Shape {
  constructor(color) {
    this.color = color;
  }

  describe() {
    return `A ${this.color} shape`;
  }
}

class Circle extends Shape {
  constructor(color, radius) {
    super(color);        // MUST call super() before using `this`
    this.radius = radius;
  }

  area() {
    return Math.PI * this.radius ** 2;
  }

  describe() {
    // Method override -- calls parent version via super
    return `${super.describe()} (circle, r=${this.radius})`;
  }
}

const c = new Circle("red", 5);
console.log(c.describe()); // "A red shape (circle, r=5)"
console.log(c.area());     // 78.5398...
console.log(c instanceof Circle); // true
console.log(c instanceof Shape);  // true
```

### What `extends` does under the hood

Two things happen:

1. **Prototype chain:** `Circle.prototype.__proto__` is set to `Shape.prototype` (so instances can access parent methods).
2. **Static chain:** `Circle.__proto__` is set to `Shape` (so static methods are inherited too).

```js
console.log(Object.getPrototypeOf(Circle.prototype) === Shape.prototype); // true
console.log(Object.getPrototypeOf(Circle) === Shape);                     // true
```

### BAD: Forgetting `super()` before `this`

```js
class Square extends Shape {
  constructor(color, side) {
    this.side = side;   // ReferenceError: Must call super constructor before accessing 'this'
    super(color);
  }
}
```

### GOOD: Always call `super()` first

```js
class Square extends Shape {
  constructor(color, side) {
    super(color);       // first
    this.side = side;   // then use this
  }
}
```

### Rule: If you extend a class and write a constructor, you MUST call `super()`. If you don't write a constructor at all, JS generates one that calls `super(...args)` automatically.

---

## 4. Method Overriding

When a child class defines a method with the same name as the parent, it **shadows** the parent method. The parent method is not deleted -- it still exists on the parent prototype. The child's version is found first in the prototype chain.

```js
class Animal {
  speak() {
    return "...";
  }
}

class Dog extends Animal {
  speak() {
    return "Woof!";
  }
}

class Cat extends Animal {
  speak() {
    return `Cat says: ${super.speak()}`; // calls parent version
  }
}

const d = new Dog();
const c = new Cat();

console.log(d.speak()); // "Woof!"
console.log(c.speak()); // "Cat says: ..."
```

**Key point:** `super.method()` inside a method calls the parent class's version of that method. This is how you **extend** behavior instead of fully replacing it.

---

## 5. Static Methods and Static Properties

Static members belong to the **class itself**, not to instances.

```js
class MathHelper {
  static PI = 3.14159;

  static square(x) {
    return x * x;
  }
}

console.log(MathHelper.PI);         // 3.14159
console.log(MathHelper.square(4));   // 16

const m = new MathHelper();
console.log(m.square);    // undefined -- not on instances
console.log(m.PI);        // undefined -- not on instances
```

### Common Pattern: Instance Counter

```js
class User {
  static count = 0;

  constructor(name) {
    this.name = name;
    User.count++;         // reference the class name, not `this`
  }
}

new User("A");
new User("B");
new User("C");
console.log(User.count); // 3
```

### Common Pattern: Factory Method

```js
class Point {
  constructor(x, y) {
    this.x = x;
    this.y = y;
  }

  static origin() {
    return new Point(0, 0);   // returns a new instance
  }

  static fromArray([x, y]) {
    return new Point(x, y);
  }
}

const p = Point.origin();
console.log(p.x, p.y); // 0 0
```

### Static methods are inherited via `extends`

```js
class Shape {
  static create() {
    return new this(); // `this` refers to the calling class
  }
}

class Circle extends Shape {}

const c = Circle.create();
console.log(c instanceof Circle); // true
```

---

## 6. Private Fields (`#`) and Private Methods

### Declaration and Access

Private fields **must be declared** in the class body (not just assigned in constructor).

```js
class BankAccount {
  #balance;   // declaration

  constructor(initial) {
    this.#balance = initial;
  }

  deposit(amount) {
    this.#balance += amount;
  }

  getBalance() {
    return this.#balance;
  }
}

const acc = new BankAccount(100);
acc.deposit(50);
console.log(acc.getBalance()); // 150
console.log(acc.#balance);     // SyntaxError: Private field '#balance' must be declared in an enclosing class
```

### BAD: Accessing private fields from outside

```js
const acc = new BankAccount(100);
console.log(acc.#balance);     // SyntaxError
console.log(acc["#balance"]);  // undefined -- this is a regular string property, not the private field
```

### Private Methods

```js
class Validator {
  #rules;

  constructor(rules) {
    this.#rules = rules;
  }

  // Private method
  #checkRule(rule, value) {
    if (rule === "nonEmpty") return value.length > 0;
    if (rule === "isNumber") return typeof value === "number";
    return true;
  }

  validate(value) {
    return this.#rules.every(rule => this.#checkRule(rule, value));
  }
}

const v = new Validator(["nonEmpty"]);
console.log(v.validate("hello"));  // true
console.log(v.validate(""));       // false
// v.#checkRule("nonEmpty", "hi");  // SyntaxError
```

### Private + Static Combined

```js
class Config {
  static #instance = null;

  static getInstance() {
    if (!Config.#instance) {
      Config.#instance = new Config();
    }
    return Config.#instance;
  }
}

const a = Config.getInstance();
const b = Config.getInstance();
console.log(a === b);          // true (singleton)
// console.log(Config.#instance); // SyntaxError if accessed outside class body
```

> **Exam Tip:** The `#` is part of the name. `#balance` and `balance` are two completely different fields. A class can have both.

---

## 7. Getters and Setters

Use `get` and `set` keywords to make methods behave like properties.

### Full Example: Temperature (Celsius/Fahrenheit)

```js
class Temperature {
  #celsius;

  constructor(celsius) {
    this.#celsius = celsius;
  }

  get fahrenheit() {
    return this.#celsius * 9 / 5 + 32;
  }

  set fahrenheit(f) {
    this.#celsius = (f - 32) * 5 / 9;
  }

  get celsius() {
    return this.#celsius;
  }

  set celsius(c) {
    if (c < -273.15) throw new RangeError("Below absolute zero");
    this.#celsius = c;
  }
}

const t = new Temperature(0);
console.log(t.fahrenheit);   // 32  -- accessed like a property, no ()
console.log(t.celsius);      // 0

t.fahrenheit = 212;           // calls the setter
console.log(t.celsius);      // 100

t.celsius = -300;             // RangeError: Below absolute zero
```

**Key points:**
- `get` defines a method called with no arguments via property access syntax (no parentheses).
- `set` defines a method called with one argument via assignment syntax.
- Getters/setters are placed on the **prototype**, just like regular methods.
- They are commonly combined with **private fields** to add validation.

### BAD: Using getter with parentheses

```js
console.log(t.fahrenheit()); // TypeError: t.fahrenheit is not a function
```

### GOOD: Access like a property

```js
console.log(t.fahrenheit);   // 32
```

---

## 8. Full Coded Example: Order Class (Private Field + Static Counter)

```js
class Order {
  static #totalOrders = 0;
  #items;

  constructor(customerName) {
    Order.#totalOrders++;
    this.id = Order.#totalOrders;   // auto-incrementing ID
    this.customerName = customerName;
    this.#items = [];
  }

  addItem(name, price) {
    this.#items.push({ name, price });
  }

  get total() {
    return this.#items.reduce((sum, item) => sum + item.price, 0);
  }

  get itemCount() {
    return this.#items.length;
  }

  static get orderCount() {
    return Order.#totalOrders;
  }

  toString() {
    return `Order #${this.id} for ${this.customerName}: ${this.itemCount} items, $${this.total}`;
  }
}

const o1 = new Order("Alice");
o1.addItem("Book", 15);
o1.addItem("Pen", 3);

const o2 = new Order("Bob");
o2.addItem("Laptop", 999);

console.log(o1.toString());      // "Order #1 for Alice: 2 items, $18"
console.log(o2.toString());      // "Order #2 for Bob: 1 items, $999"
console.log(Order.orderCount);   // 2
console.log(o1.total);           // 18
// console.log(o1.#items);       // SyntaxError
// console.log(Order.#totalOrders); // SyntaxError
```

---

## 9. Reality of OOP in JS

This is a **conceptual** section. Exams sometimes ask essay-style or true/false questions here.

### JS is prototype-based, NOT classical

- In Java/C++, a class is a **blueprint** and creating an instance **copies** the structure.
- In JS, there is **no copying**. Objects are linked via the prototype chain. Method lookup walks the chain at runtime.

```js
class Vehicle {
  drive() {
    return "driving";
  }
}

const v = new Vehicle();

// The method lives on Vehicle.prototype, not on v itself
console.log(v.hasOwnProperty("drive")); // false
console.log("drive" in v);              // true -- found via chain
```

### Delegation, not duplication

```js
class A {
  greet() {
    return "hello from A";
  }
}

const obj1 = new A();
const obj2 = new A();

// Both share the SAME function object
console.log(obj1.greet === obj2.greet); // true
```

### Modifying the prototype after instance creation affects ALL existing instances

```js
class Robot {
  speak() {
    return "beep";
  }
}

const r1 = new Robot();
const r2 = new Robot();

Robot.prototype.speak = function () {
  return "boop";
};

console.log(r1.speak()); // "boop" -- r1 was created BEFORE the change
console.log(r2.speak()); // "boop"
```

This proves there is no copy. Both `r1` and `r2` delegate to `Robot.prototype`, and when you mutate that shared prototype, every instance sees the change immediately.

> **Exam Tip:** If a question says "explain why JS is not truly classical OOP," your answer centers on: no copying, prototype delegation, runtime chain lookup, shared prototype mutations affect all instances.

---

## 10. Predict the Output

### Question 1

```js
class Counter {
  static count = 0;
  constructor() {
    Counter.count++;
  }
}

const a = new Counter();
const b = new Counter();
const c = new Counter();

console.log(Counter.count);
console.log(a.count);
```

<details>
<summary><strong>Answer</strong></summary>

```
3
undefined
```

`Counter.count` is a **static** property -- it lives on the class, not on instances. `a.count` looks for `count` on the instance (not found) and then on the prototype (not found), so it returns `undefined`.

</details>

---

### Question 2

```js
class Parent {
  constructor() {
    this.name = "parent";
  }

  greet() {
    return `Hello from ${this.name}`;
  }
}

class Child extends Parent {
  constructor() {
    super();
    this.name = "child";
  }
}

const c = new Child();
console.log(c.greet());
```

<details>
<summary><strong>Answer</strong></summary>

```
Hello from child
```

Execution order: `super()` runs `Parent`'s constructor, setting `this.name = "parent"`. Then `Child`'s constructor continues, overwriting `this.name = "child"`. When `greet()` runs, `this.name` is `"child"`. There is only **one object** (`c`), and both constructors operate on it.

</details>

---

### Question 3

```js
class Animal {
  speak() {
    return "...";
  }
}

class Dog extends Animal {
  speak() {
    return super.speak() + " Woof!";
  }
}

class LoudDog extends Dog {
  speak() {
    return super.speak().toUpperCase();
  }
}

const d = new LoudDog();
console.log(d.speak());
```

<details>
<summary><strong>Answer</strong></summary>

```
... WOOF!
```

Chain of calls:
1. `LoudDog.speak()` calls `super.speak()` which is `Dog.speak()`.
2. `Dog.speak()` calls `super.speak()` which is `Animal.speak()`, returning `"..."`.
3. `Dog.speak()` returns `"..." + " Woof!"` = `"... Woof!"`.
4. `LoudDog.speak()` calls `.toUpperCase()` on that, producing `"... WOOF!"`.

</details>

---

## Exam Tips -- All in One Place

- **`typeof ClassName`** is always `"function"`. If an exam asks what type a class is, the answer is function.
- **Classes are not hoisted.** If code uses a class before its declaration, it is a `ReferenceError` (temporal dead zone, same as `let`/`const`).
- **Forgetting `super()` in a derived constructor** gives a `ReferenceError` when you try to use `this`. This is the single most common trap in inheritance questions.
- **Static members are not on instances.** `instance.staticMethod` is `undefined`.
- **Private fields (`#`) cause a `SyntaxError`** at parse time when accessed outside the class body. Not a `TypeError`, not a `ReferenceError` -- a **SyntaxError**.
- **Getters are accessed without parentheses.** Calling `obj.getter()` is a `TypeError`.
- **Prototype mutation affects existing instances.** If a question modifies the prototype after creating instances and asks what the instances see, the answer is the new version.
- **`super` in a method** refers to the parent class's prototype. `super.foo()` calls the parent's `foo`, not the current class's `foo`.
- **One object, two constructors:** When `extends` is used, `super()` and the child constructor both modify the **same `this` object**. Properties set by `super()` can be overwritten by the child constructor.

---

## Quick Revision Table

| Concept | Syntax / Key Fact | Common Mistake |
|---|---|---|
| Class declaration | `class Foo { constructor() {} }` | Trying to call without `new` |
| typeof a class | `"function"` | Saying `"class"` or `"object"` |
| Hoisting | **Not hoisted** (TDZ) | Using class before declaration |
| Method enumerability | Non-enumerable (won't appear in `for...in`) | Assuming they appear like prototype methods on constructor functions |
| Strict mode | Automatic inside class body | Forgetting this causes subtle `this` bugs |
| `extends` | `class Child extends Parent {}` | Forgetting `super()` in child constructor |
| `super()` in constructor | Must be called **before** `this` | Accessing `this` first = `ReferenceError` |
| `super.method()` | Calls parent version of the method | Confusing with `this.method()` (which calls child's own version) |
| Static method | `static foo() {}` | Trying to call on instance (`obj.foo()` = `undefined`) |
| Static property | `static count = 0;` | Incrementing with `this.count++` instead of `ClassName.count++` |
| Private field | `#field` declared in class body | Accessing outside class = **SyntaxError** |
| Private method | `#method() {}` | Same -- **SyntaxError** if called outside |
| Getter | `get prop() { return ... }` | Calling with `()` = TypeError |
| Setter | `set prop(val) { ... }` | Forgetting validation logic |
| Prototype-based OOP | No copying, delegation via chain | Thinking JS copies like Java |
| Prototype mutation | Affects **all** existing instances | Thinking instances have their own copy |
| `instanceof` | Checks prototype chain | Works across inheritance (`child instanceof Parent` = `true`) |

---

## Practice These

After studying this topic, solve these coding problems:

**From Contest_Practice-1.md:**
- Q9: ES6 class with private field + static counter
- Q10: Inheritance with extends/super
- Q11: Private method + public API
- Q12: Class with getter + private field

**From practiceSheet.md:**
- P40: Class-based inheritance with mixins
- P41: Method overriding + super simulation
- P42: Singleton pattern
- P43: Factory pattern
