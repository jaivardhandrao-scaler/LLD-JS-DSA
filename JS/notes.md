# JavaScript Exam Notes — Full Guide

---

# MODULE 1: OOP in JavaScript

---

## OOPS-1: The `this` Keyword

### What is `this`?

`this` is a special keyword in JavaScript that refers to an object. But **which object** depends entirely on **how the function is called**, NOT where it is written.

Think of `this` like the word "I" in English — whoever is speaking, "I" refers to them. Similarly, whoever is **calling** the function, `this` refers to them.

---

### Execution Context & Binding Rules

Every time a function runs, JavaScript creates an **execution context** — a wrapper that holds:
- The variables inside the function
- The scope chain (access to outer variables)
- The value of `this`

There are **4 rules** that determine `this`, in order of priority:

#### Rule 1: `new` Binding (Highest Priority)
When a function is called with `new`, `this` = the newly created object.

```js
function Person(name) {
  // this = {} (a brand new empty object)
  this.name = name;
  // this = { name: "Alice" }
  // implicitly returns this
}
const p = new Person("Alice");
console.log(p.name); // "Alice"
```

#### Rule 2: Explicit Binding (`call`, `apply`, `bind`)
You manually tell JS what `this` should be.

```js
function greet(greeting) {
  console.log(greeting + ", " + this.name);
}

const user = { name: "Alice" };

greet.call(user, "Hello");    // "Hello, Alice" — runs immediately
greet.apply(user, ["Hello"]); // "Hello, Alice" — args as array
const bound = greet.bind(user); // returns a NEW function with this locked
bound("Hello");                 // "Hello, Alice"
```

**call vs apply vs bind:**
| Method | Runs immediately? | Args format | Returns |
|--------|------------------|-------------|---------|
| `call` | Yes | `arg1, arg2, ...` | function result |
| `apply` | Yes | `[arg1, arg2, ...]` | function result |
| `bind` | No | `arg1, arg2, ...` | new function |

#### Rule 3: Implicit Binding
When a function is called as a method of an object (object.method()), `this` = the object before the dot.

```js
const user = {
  name: "Alice",
  greet() {
    console.log(this.name); // this = user (object before the dot)
  }
};
user.greet(); // "Alice"
```

#### Rule 4: Default Binding (Lowest Priority)
When none of the above apply — just a plain function call.

```js
function show() {
  console.log(this);
}
show(); // globalThis (window in browser, global in Node)
```

**Priority order:** `new` > explicit (`call/apply/bind`) > implicit (dot) > default

---

### Global vs Function vs Object Context

```js
// GLOBAL CONTEXT
console.log(this); // globalThis (in Node module: {}, in browser: window)

// FUNCTION CONTEXT — depends on how it's called
function standalone() {
  console.log(this); // globalThis (default binding)
}
standalone();

// OBJECT CONTEXT — dot rule
const obj = {
  name: "Obj",
  method() {
    console.log(this.name); // "Obj" (implicit binding)
  }
};
obj.method();
```

---

### `this` in Strict Mode vs Non-Strict Mode

The ONLY difference is what happens with **default binding**:

```js
// NON-STRICT
function show() {
  console.log(this); // globalThis (window/global)
}
show();

// STRICT
"use strict";
function show() {
  console.log(this); // undefined
}
show();
```

Everything else (new, call/apply/bind, dot rule) works the same in both modes.

**Why this matters:** In strict mode, forgetting to use `new` with a constructor will crash instead of silently polluting the global object:
```js
"use strict";
function Person(name) { this.name = name; }
Person("Alice"); // TypeError: Cannot set property 'name' of undefined
// Without strict mode, this would silently set globalThis.name = "Alice"
```

---

### Arrow Functions vs Regular Functions

This is a **huge** exam topic. The core difference:

> **Arrow functions do NOT have their own `this`.** They capture `this` from the surrounding scope at the time they are created (lexical `this`).

```js
const obj = {
  name: "Alice",

  // Regular function: this = determined by call site
  regular() {
    console.log(this.name); // "Alice" (implicit binding — obj.regular())
  },

  // Arrow function: this = whatever this is in the SURROUNDING scope
  arrow: () => {
    console.log(this.name); // undefined! 
    // surrounding scope is the module/global, NOT obj
    // because obj = {} is not a scope, it's just an object literal
  }
};

obj.regular(); // "Alice"
obj.arrow();   // undefined
```

**CRITICAL INSIGHT:** An object literal `{}` does NOT create a scope. Only functions, blocks, and modules create scopes. So an arrow function inside an object literal captures `this` from whatever is OUTSIDE the object.

**Where arrow functions SHINE — fixing callbacks:**

```js
const obj = {
  name: "Alice",
  greet() {
    // 'this' here = obj (implicit binding)
    
    // PROBLEM: regular function callback loses this
    setTimeout(function() {
      console.log(this.name); // undefined (default binding)
    }, 100);
    
    // SOLUTION: arrow function captures this from greet()
    setTimeout(() => {
      console.log(this.name); // "Alice" (lexical this from greet)
    }, 100);
  }
};
```

**Full comparison table:**

| Feature | Regular Function | Arrow Function |
|---------|-----------------|----------------|
| `this` | Determined at **call time** | Captured at **creation time** (lexical) |
| `arguments` object | Yes | No (use `...rest` instead) |
| Can be constructor (`new`) | Yes | No — throws TypeError |
| Has `prototype` property | Yes | No |
| Can be a method | Yes (this works) | Avoid (this won't be the object) |
| Good for callbacks | Need bind/self trick | Perfect |

---

### Implicit, Explicit, and Default Binding — Practice Scenarios

**Scenario 1: Method extraction (this is LOST)**
```js
const user = {
  name: "Alice",
  greet() { console.log(this.name); }
};

user.greet();          // "Alice" — implicit binding
const fn = user.greet; // extracting the function
fn();                  // undefined — default binding (no dot!)
```

**Scenario 2: Passing method as callback (this is LOST)**
```js
const user = {
  name: "Alice",
  greet() { console.log(this.name); }
};

setTimeout(user.greet, 100); // undefined! — setTimeout calls it as a plain function

// Fix with bind:
setTimeout(user.greet.bind(user), 100); // "Alice"

// Fix with arrow wrapper:
setTimeout(() => user.greet(), 100); // "Alice"
```

**Scenario 3: Nested function (this is LOST)**
```js
const obj = {
  name: "Test",
  outer() {
    console.log(this.name); // "Test" — implicit binding
    
    function inner() {
      console.log(this.name); // undefined — default binding!
    }
    inner(); // plain call, no dot
    
    // Fix 1: Arrow function
    const innerArrow = () => console.log(this.name); // "Test"
    innerArrow();
    
    // Fix 2: Store this
    const self = this;
    function inner2() { console.log(self.name); } // "Test"
    inner2();
    
    // Fix 3: Explicit binding
    inner.call(this); // "Test"
  }
};
obj.outer();
```

**Scenario 4: Explicit always wins over implicit**
```js
const obj1 = { name: "obj1", greet() { console.log(this.name); } };
const obj2 = { name: "obj2" };

obj1.greet.call(obj2); // "obj2" — explicit beats implicit
```

**Scenario 5: `new` beats everything**
```js
function Foo(name) { this.name = name; }
const bound = Foo.bind({ name: "bound" });
const obj = new bound("new wins");
console.log(obj.name); // "new wins" — new beats bind
```

---

## OOPS-2: Constructor Functions & the `new` Keyword

### What is a Constructor Function?

A constructor function is just a regular function that is designed to be called with `new`. By convention, they start with a **capital letter**.

```js
function Person(name, age) {
  this.name = name;
  this.age = age;
}

const alice = new Person("Alice", 25);
console.log(alice.name); // "Alice"
console.log(alice.age);  // 25
console.log(alice instanceof Person); // true
```

Without `new`, it's just a regular function call — `this` would be globalThis (or undefined in strict mode), and nothing gets returned.

---

### Internal Working of `new` — What Happens Step by Step

When you write `new Person("Alice", 25)`, JavaScript does these 4 things:

```
Step 1: Create a brand new empty object
        → obj = {}

Step 2: Link that object's __proto__ to the constructor's prototype
        → obj.__proto__ = Person.prototype

Step 3: Call the constructor with this = that new object
        → Person.call(obj, "Alice", 25)
        → now obj = { name: "Alice", age: 25 }

Step 4: If the constructor returns an object, use that.
        Otherwise, return the new object (obj).
        → return obj
```

This is **extremely important** for the exam. Let me show each step visually:

```js
function Person(name) {
  // Step 1 already happened: this = {}
  // Step 2 already happened: this.__proto__ = Person.prototype
  
  this.name = name;  // Step 3: adding properties
  
  // Step 4: since we don't return an object, JS returns this automatically
}

// What if constructor explicitly returns something?
function Weird() {
  this.name = "from this";
  return { name: "from return" }; // returning an object!
}
const w = new Weird();
console.log(w.name); // "from return" — explicit object return wins!

function NotWeird() {
  this.name = "from this";
  return 42; // returning a primitive — ignored!
}
const nw = new NotWeird();
console.log(nw.name); // "from this" — primitive return is ignored
```

**Rule:** If constructor returns a **non-null object**, that's used. If it returns a **primitive** (or nothing), the `this` object is returned.

---

### Object Creation Flow (this, prototype linking)

```
Person (function)
  ├── Person.prototype (object)
  │     ├── constructor: Person  (points back)
  │     └── any methods you add here
  │
  new Person("Alice")
  │
  ├── Creates: { name: "Alice" }
  └── Links:   obj.__proto__ ──→ Person.prototype
```

So when you do:
```js
const alice = new Person("Alice");
alice.name;        // found directly on alice
alice.constructor; // not on alice → looks up __proto__ → found on Person.prototype
```

---

### Manual Implementation of `new` (Polyfill) — EXAM FAVORITE

This is one of the most commonly asked questions. You need to implement what `new` does:

```js
function myNew(Constructor, ...args) {
  // Step 1 & 2: Create object linked to Constructor.prototype
  const obj = Object.create(Constructor.prototype);
  
  // Step 3: Call constructor with this = obj
  const result = Constructor.apply(obj, args);
  
  // Step 4: If constructor returned an object, use it. Otherwise use obj.
  return (result !== null && typeof result === "object") ? result : obj;
}

// Usage:
function Person(name) {
  this.name = name;
}
Person.prototype.greet = function() {
  return "Hi, " + this.name;
};

const p = myNew(Person, "Alice");
console.log(p.name);              // "Alice"
console.log(p.greet());           // "Hi, Alice"
console.log(p instanceof Person); // true (because __proto__ chain is correct)
```

**Why `Object.create(Constructor.prototype)`?**
- `Object.create(proto)` creates a new empty object whose `__proto__` is set to `proto`
- This is cleaner than manually doing `obj.__proto__ = Constructor.prototype`

**Breaking down the return line:**
```js
return (result !== null && typeof result === "object") ? result : obj;
// If constructor returned an actual object → use that
// If constructor returned undefined/number/string → use our created obj
```

---

## OOPS-3: Prototypes & Prototype Chain

### `prototype` vs `__proto__` — The Most Confusing Part of JS

These are **two completely different things**:

| | `prototype` | `__proto__` |
|---|---|---|
| **Exists on** | Functions only | Every object |
| **What it is** | An object that will become `__proto__` of instances | A link to the parent object in the chain |
| **Purpose** | Blueprint for instances | Actual lookup chain |

```js
function Person(name) { this.name = name; }
Person.prototype.greet = function() { return "Hi " + this.name; };

const alice = new Person("Alice");

// Person.prototype = the blueprint object { greet: fn, constructor: Person }
// alice.__proto__  = Person.prototype (they point to the SAME object)

console.log(alice.__proto__ === Person.prototype); // true
console.log(Person.prototype.__proto__ === Object.prototype); // true
console.log(Object.prototype.__proto__ === null); // true — end of chain
```

**The full chain:**
```
alice → Person.prototype → Object.prototype → null
  ↑         ↑                   ↑              ↑
  object    has greet()         has toString()  end of chain
```

---

### Prototype Chaining & Inheritance

When you access a property on an object, JS looks:
1. On the object itself
2. On `object.__proto__`
3. On `object.__proto__.__proto__`
4. ... all the way up to `null`

```js
function Animal(name) { this.name = name; }
Animal.prototype.speak = function() { return this.name + " speaks"; };

function Dog(name, breed) {
  Animal.call(this, name); // call parent constructor
  this.breed = breed;
}

// Set up prototype chain: Dog instances → Dog.prototype → Animal.prototype
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog; // fix the constructor reference

Dog.prototype.bark = function() { return this.name + " barks"; };

const rex = new Dog("Rex", "Lab");
console.log(rex.bark());  // "Rex barks"  — found on Dog.prototype
console.log(rex.speak()); // "Rex speaks" — NOT on Dog.prototype, 
                           //   goes up to Animal.prototype, found!
console.log(rex.toString()); // "[object Object]" — goes all the way to Object.prototype
```

**The chain:**
```
rex → Dog.prototype → Animal.prototype → Object.prototype → null
```

---

### Method Lookup & Shadowing

**Lookup:** JS walks up the prototype chain until it finds the property or hits `null`.

**Shadowing:** If a child has the same property name as a parent, the child's version is used (it "shadows" the parent).

```js
function Animal() {}
Animal.prototype.speak = function() { return "Animal speaks"; };

function Dog() {}
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

// Shadowing — Dog defines its own speak
Dog.prototype.speak = function() { return "Dog barks"; };

const d = new Dog();
console.log(d.speak()); // "Dog barks" — found on Dog.prototype, stops looking

// To call the parent version explicitly:
console.log(Animal.prototype.speak.call(d)); // "Animal speaks"
```

**Setting a property on an instance always creates it on the instance (never modifies the prototype):**
```js
function Person() {}
Person.prototype.age = 25;

const p = new Person();
console.log(p.age);    // 25 (from prototype)

p.age = 30;            // creates OWN property, doesn't modify prototype
console.log(p.age);    // 30 (own property shadows prototype)

delete p.age;          // removes own property
console.log(p.age);    // 25 (back to prototype lookup)
```

---

### `hasOwnProperty`, `in`, `instanceof`

These three are used to check properties and types differently:

```js
function Person(name) { this.name = name; }
Person.prototype.species = "Human";

const p = new Person("Alice");

// hasOwnProperty — checks ONLY the object itself, NOT the chain
console.log(p.hasOwnProperty("name"));    // true  (own property)
console.log(p.hasOwnProperty("species")); // false (on prototype, not own)

// in — checks the object AND the entire prototype chain
console.log("name" in p);    // true  (own property)
console.log("species" in p); // true  (found on prototype)
console.log("toString" in p); // true (found on Object.prototype)

// instanceof — checks if Constructor.prototype exists in the object's __proto__ chain
console.log(p instanceof Person); // true  (Person.prototype is in p's chain)
console.log(p instanceof Object); // true  (Object.prototype is in p's chain)
console.log(p instanceof Array);  // false (Array.prototype is NOT in p's chain)
```

**How `instanceof` works internally:**
```js
// a instanceof B  is essentially:
// walk up a.__proto__ chain, check if any === B.prototype

function myInstanceOf(obj, Constructor) {
  let proto = Object.getPrototypeOf(obj);
  while (proto !== null) {
    if (proto === Constructor.prototype) return true;
    proto = Object.getPrototypeOf(proto);
  }
  return false;
}
```

---

### Function Prototypes vs Object Prototypes

Every function has a `.prototype` property (used when the function is called with `new`).
Every object has a `__proto__` property (the actual chain link).

```js
// Functions are objects too, so they have BOTH:
function Foo() {}

// Foo.prototype  → the object given to instances as __proto__
// Foo.__proto__  → Function.prototype (because Foo is an instance of Function)

console.log(Foo.__proto__ === Function.prototype);          // true
console.log(Function.prototype.__proto__ === Object.prototype); // true

// Even Function itself:
console.log(Function.__proto__ === Function.prototype); // true (weird but true)

// And Object:
console.log(Object.__proto__ === Function.prototype); // true (Object is a function)
```

**The complete picture:**
```
                         null
                          ↑
                   Object.prototype  (has: toString, hasOwnProperty, etc.)
                    ↑            ↑
          Function.prototype   Person.prototype  (has: your methods)
           ↑        ↑                ↑
        Function   Person         alice (instance)
        Object
        Array
        (all built-in constructors)
```

---

## OOPS-4: Classes & Inheritance in JS

### ES6 Classes vs Constructor Functions

Classes are **syntactic sugar** over constructor functions + prototypes. They do the exact same thing under the hood.

```js
// CONSTRUCTOR FUNCTION WAY
function Person(name) {
  this.name = name;
}
Person.prototype.greet = function() {
  return "Hi " + this.name;
};

// CLASS WAY (identical behavior)
class Person {
  constructor(name) {
    this.name = name;
  }
  greet() {
    return "Hi " + this.name;
  }
}

// Both produce the same prototype chain:
// instance.__proto__ === Person.prototype
// Person.prototype.greet === the method
```

**Key differences (classes are stricter):**
| Feature | Constructor Function | Class |
|---------|---------------------|-------|
| Must use `new` | No (fails silently without) | Yes (throws error without `new`) |
| Hoisted | Yes (function declaration) | No (TDZ like `let`) |
| Methods enumerable | Yes | No (methods are non-enumerable) |
| `"use strict"` | Manual | Automatic inside class body |

---

### `extends` and `super`

```js
class Animal {
  constructor(name) {
    this.name = name;
  }
  speak() {
    return `${this.name} makes a sound`;
  }
}

class Dog extends Animal {
  constructor(name, breed) {
    super(name);       // MUST call super() before using this
    this.breed = breed; // now you can use this
  }
  speak() {
    return `${this.name} barks`; // overrides parent's speak
  }
  parentSpeak() {
    return super.speak(); // calls Animal's speak
  }
}

const d = new Dog("Rex", "Lab");
console.log(d.speak());       // "Rex barks"
console.log(d.parentSpeak()); // "Rex makes a sound"
console.log(d instanceof Dog);    // true
console.log(d instanceof Animal); // true
```

**Rules for `super`:**
- In constructor: `super()` calls the parent constructor. **MUST** be called before any `this` access.
- In methods: `super.method()` calls the parent's version of that method.

**What `extends` does under the hood:**
```js
// class Dog extends Animal is equivalent to:
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;
Dog.__proto__ = Animal; // also links static methods
```

---

### Method Overriding

When a child class defines a method with the same name as the parent, it **shadows** (overrides) the parent's method.

```js
class Shape {
  area() { return 0; }
  describe() { return "I am a shape"; }
}

class Circle extends Shape {
  constructor(radius) {
    super();
    this.radius = radius;
  }
  // Override area — completely replaces parent version
  area() {
    return Math.PI * this.radius ** 2;
  }
  // Override describe — uses parent version + extends it
  describe() {
    return super.describe() + ` (circle, r=${this.radius})`;
  }
}

const c = new Circle(5);
console.log(c.area());     // 78.539...
console.log(c.describe()); // "I am a shape (circle, r=5)"
```

---

### Static Methods & Private Fields

**Static methods** belong to the class itself, not to instances:

```js
class MathHelper {
  static add(a, b) { return a + b; }
  static PI = 3.14159;
}

console.log(MathHelper.add(2, 3)); // 5
console.log(MathHelper.PI);        // 3.14159

const m = new MathHelper();
// m.add(2, 3);  // TypeError! Static methods aren't on instances
```

**Private fields** (prefix with `#`) — cannot be accessed outside the class:

```js
class BankAccount {
  #balance; // private field declaration

  constructor(initial) {
    this.#balance = initial;
  }

  deposit(amount) {
    this.#balance += amount;
    return this.#balance;
  }

  getBalance() {
    return this.#balance;
  }
}

const acc = new BankAccount(100);
console.log(acc.getBalance()); // 100
acc.deposit(50);
console.log(acc.getBalance()); // 150
// console.log(acc.#balance);  // SyntaxError! Private field
```

**Private methods** work the same way:
```js
class Validator {
  #isEmpty(str) {
    return str === undefined || str === null || str.length === 0;
  }
  validate(str) {
    return !this.#isEmpty(str);
  }
}
```

**Static + Private combined example (exam favorite):**
```js
class Order {
  static #count = 0;  // private static
  #id;                 // private instance field

  constructor() {
    this.#id = Order.#count;
    Order.#count++;
  }
  getId() { return this.#id; }
  static getCount() { return Order.#count; }
}

const o1 = new Order(); // id=0, count becomes 1
const o2 = new Order(); // id=1, count becomes 2
console.log(o1.getId());      // 0
console.log(o2.getId());      // 1
console.log(Order.getCount()); // 2
```

---

### Reality of OOP in JavaScript (Prototype-Based Nature)

JavaScript is **NOT** a classical OOP language like Java or C++. It's **prototype-based**.

**Classical OOP (Java):** Classes are blueprints. Objects are built from blueprints. A class is NOT an object.

**Prototypal OOP (JavaScript):** There are no real "classes". Everything is objects linked to other objects. `class` keyword is just syntax sugar — underneath, it's all prototypes.

```js
class Person {
  greet() { return "hi"; }
}

// This is just sugar for:
function Person() {}
Person.prototype.greet = function() { return "hi"; };

// Proof that class is a function:
console.log(typeof Person); // "function"
```

**Why this matters for the exam:**
- When asked "how does inheritance work in JS?" — answer: prototype chain, not classical inheritance
- `class extends` → sets up prototype chain (`Object.create`)
- `super()` → calls parent constructor with `this`
- There's no copying of methods — objects **delegate** to their prototype via the chain
- If you modify `Person.prototype.greet` after creating instances, ALL instances see the change (because they share the same prototype object via reference)

```js
function Person() {}
Person.prototype.greet = function() { return "v1"; };

const p = new Person();
console.log(p.greet()); // "v1"

Person.prototype.greet = function() { return "v2"; };
console.log(p.greet()); // "v2" — same object in the chain, sees the update!
```

---

## Quick Reference: Common Exam Patterns for Module 1

### Pattern 1: Fix `this` in nested/callback
→ Use arrow function or `.bind(this)` or `const self = this`

### Pattern 2: Implement `myNew`
```js
function myNew(Ctor, ...args) {
  const obj = Object.create(Ctor.prototype);
  const result = Ctor.apply(obj, args);
  return result !== null && typeof result === "object" ? result : obj;
}
```

### Pattern 3: Set up inheritance (pre-class)
```js
Child.prototype = Object.create(Parent.prototype);
Child.prototype.constructor = Child;
// In Child constructor: Parent.call(this, args)
```

### Pattern 4: Class with private + static
```js
class X {
  static count = 0;
  #id;
  constructor() { this.#id = X.count++; }
  getId() { return this.#id; }
  static getCount() { return X.count; }
}
```

### Pattern 5: Prototype lookup question
→ Check own property first → walk __proto__ chain → null = undefined

---
