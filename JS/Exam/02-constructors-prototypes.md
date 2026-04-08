# Constructor Functions, `new` Keyword & Prototypes

**Exam tomorrow? Start here. Read the code first, then the explanations.**

---

## Part A: Constructor Functions & `new`

### 1. What Is a Constructor Function?

There is nothing special about a constructor function. It is a regular function. The only thing that makes it a "constructor" is that you call it with `new`.

```js
// BAD — calling without new
function Person(name) {
  this.name = name;
}

const p = Person("Alice");
console.log(p);          // undefined  (no return value)
console.log(window.name); // "Alice"   (this === window in non-strict mode!)
```

```js
// GOOD — calling with new
function Person(name) {
  this.name = name;
}

const p = new Person("Alice");
console.log(p);       // Person { name: "Alice" }
console.log(p.name);  // "Alice"
```

**Convention:** capitalize the first letter (`Person`, not `person`) so other developers know to use `new`.

---

### 2. The 4 Steps of `new` Internally

**This is an exam favorite. Memorize these four steps.**

When you write `const p = new Person("Alice")`, the engine does this:

| Step | What Happens | In Code Terms |
|------|-------------|---------------|
| 1 | Create a brand-new empty object | `const obj = {}` |
| 2 | Link that object's `__proto__` to the constructor's `.prototype` | `obj.__proto__ = Person.prototype` |
| 3 | Call the constructor with `this` set to the new object | `Person.call(obj, "Alice")` |
| 4 | If the constructor returns a **non-null object**, use that instead; otherwise return `obj` | `return typeof result === 'object' && result !== null ? result : obj` |

```js
function Person(name) {
  // Step 3: `this` is the new empty object from Step 1
  this.name = name;
  // Step 4: no explicit return, so the engine returns `this`
}

const p = new Person("Alice");
// p.__proto__ === Person.prototype  (Step 2)
```

---

### 3. What Happens if the Constructor Returns Something?

**Rule:** `new` only respects a return value if it is a **non-null object**. Primitives are ignored.

```js
// Returning a primitive — IGNORED by new
function A() {
  this.x = 1;
  return 42; // primitive, ignored
}
const a = new A();
console.log(a);   // A { x: 1 }   (the 42 is thrown away)
console.log(a.x); // 1
```

```js
// Returning an object — OVERRIDES the default
function B() {
  this.x = 1;
  return { y: 2 }; // non-null object, this overrides
}
const b = new B();
console.log(b);   // { y: 2 }     (the { x: 1 } object is lost)
console.log(b.x); // undefined
console.log(b.y); // 2
```

```js
// Returning null — null is technically typeof "object" but new treats it as primitive
function C() {
  this.x = 1;
  return null;
}
const c = new C();
console.log(c);   // C { x: 1 }   (null is treated like a primitive here)
```

> **Exam Tip:** The check is `typeof result === 'object' && result !== null`. That is why `null` does not override. This is a common trap question.

---

### 4. `myNew` Polyfill Implementation

**If your exam asks "implement the `new` operator", this is the answer.**

```js
function myNew(Constructor, ...args) {
  // Step 1 + Step 2: create object with correct prototype link
  const obj = Object.create(Constructor.prototype);

  // Step 3: call constructor with this = obj
  const result = Constructor.apply(obj, args);

  // Step 4: if constructor returned a non-null object, use it; otherwise use obj
  return (typeof result === 'object' && result !== null) ? result : obj;
}
```

**Test it:**

```js
function Car(make, year) {
  this.make = make;
  this.year = year;
}
Car.prototype.drive = function () {
  return this.make + " goes vroom";
};

const c = myNew(Car, "Toyota", 2024);
console.log(c.make);          // "Toyota"
console.log(c.year);          // 2024
console.log(c.drive());       // "Toyota goes vroom"
console.log(c instanceof Car); // true  (because __proto__ chain is set up correctly)
```

**Why `Object.create` instead of `{}`?**

```js
// BAD — loses the prototype link
const obj = {};
// obj.__proto__ === Object.prototype, NOT Constructor.prototype

// GOOD — sets up the chain correctly
const obj = Object.create(Constructor.prototype);
// obj.__proto__ === Constructor.prototype
```

---

### 5. Constructor vs Factory Function

```js
// Constructor function — uses new
function PersonCtor(name) {
  this.name = name;
}
PersonCtor.prototype.greet = function () {
  return "Hi, I'm " + this.name;
};

const p1 = new PersonCtor("Alice");
console.log(p1 instanceof PersonCtor); // true
```

```js
// Factory function — no new, just returns an object
function createPerson(name) {
  return {
    name: name,
    greet: function () {
      return "Hi, I'm " + this.name;
    },
  };
}

const p2 = createPerson("Bob");
console.log(p2 instanceof createPerson); // false (no prototype link)
```

| | Constructor Function | Factory Function |
|---|---|---|
| **Called with** | `new` | Normal call `()` |
| **`this`** | Auto-bound to new object | Not relevant |
| **`instanceof`** | Works | Does not work |
| **Prototype methods** | Shared via `.prototype` (memory efficient) | Each object gets its own copy |
| **Forgetting `new`** | Bugs (pollutes global / undefined in strict) | No problem |
| **Use when** | You need `instanceof`, shared methods, inheritance | Simple object creation, no inheritance needed |

> **Exam Tip:** If the question asks "what is the disadvantage of factory functions" the answer is: no shared prototype methods (every object gets its own copy of each method, wasting memory) and `instanceof` does not work.

---

## Part B: Prototypes & Prototype Chain

### 1. `prototype` vs `__proto__` -- The #1 Most Confusing Thing

These are **two different properties** that people constantly mix up.

```js
function Dog(name) {
  this.name = name;
}
Dog.prototype.bark = function () {
  return "Woof!";
};

const d = new Dog("Rex");
```

```js
// prototype — lives on the FUNCTION
console.log(Dog.prototype);        // { bark: [Function], constructor: Dog }
console.log(d.prototype);          // undefined  (instances don't have .prototype)

// __proto__ — lives on EVERY OBJECT (including functions)
console.log(d.__proto__);                    // Dog.prototype
console.log(d.__proto__ === Dog.prototype);  // true
console.log(Dog.__proto__ === Function.prototype); // true
```

| | `.prototype` | `.__proto__` |
|---|---|---|
| **Exists on** | Functions only | Every object |
| **What it is** | An object that will become `__proto__` of instances created with `new` | A reference to the object from which this object inherits |
| **Who uses it** | The `new` operator reads it | The engine uses it for property lookup (prototype chain) |
| **Example** | `Dog.prototype` | `d.__proto__` |
| **Formal name** | The function's prototype property | `[[Prototype]]` internal slot (accessed via `__proto__` or `Object.getPrototypeOf`) |

**The key sentence to remember:**

> `d.__proto__` **IS** `Dog.prototype`. The `new` operator sets this up in Step 2.

---

### 2. Prototype Chain -- How Method Lookup Works

When you access a property on an object, the engine walks up the chain:

```js
function Animal(type) {
  this.type = type;
}
Animal.prototype.breathe = function () {
  return "breathing";
};

const cat = new Animal("cat");
cat.name = "Whiskers";
```

```
Lookup: cat.name
  1. cat own properties? YES -> "Whiskers" (FOUND, stop)

Lookup: cat.breathe
  1. cat own properties? NO
  2. cat.__proto__ (= Animal.prototype) own properties? YES -> function (FOUND, stop)

Lookup: cat.toString
  1. cat own properties? NO
  2. cat.__proto__ (= Animal.prototype) own properties? NO
  3. Animal.prototype.__proto__ (= Object.prototype) own properties? YES -> function (FOUND, stop)

Lookup: cat.nonExistent
  1. cat own properties? NO
  2. cat.__proto__ (= Animal.prototype)? NO
  3. Animal.prototype.__proto__ (= Object.prototype)? NO
  4. Object.prototype.__proto__? null -> STOP, return undefined
```

---

### 3. Setting Up Inheritance with Prototypes

This is the classic pattern before ES6 `class`:

```js
// Parent
function Shape(color) {
  this.color = color;
}
Shape.prototype.describe = function () {
  return "A " + this.color + " shape";
};

// Child
function Circle(color, radius) {
  Shape.call(this, color); // call parent constructor (like super())
  this.radius = radius;
}

// Set up the prototype chain
Circle.prototype = Object.create(Shape.prototype);
Circle.prototype.constructor = Circle; // fix the constructor reference

// Add child methods AFTER setting up the chain
Circle.prototype.area = function () {
  return Math.PI * this.radius * this.radius;
};
```

```js
const c = new Circle("red", 5);
console.log(c.describe()); // "A red shape"  (inherited from Shape)
console.log(c.area());     // 78.539...      (own method on Circle.prototype)
console.log(c instanceof Circle); // true
console.log(c instanceof Shape);  // true
console.log(c.constructor === Circle); // true (because we fixed it)
```

**BAD — Common mistakes:**

```js
// BAD: using new Shape() instead of Object.create
Circle.prototype = new Shape();
// Problem: this actually CALLS Shape(), creating an instance with properties
// on the prototype object. You get garbage properties like color: undefined
// sitting on Circle.prototype.

// BAD: forgetting to fix constructor
Circle.prototype = Object.create(Shape.prototype);
// Circle.prototype.constructor is now Shape, not Circle!
// Always add: Circle.prototype.constructor = Circle;

// BAD: adding methods BEFORE Object.create
Circle.prototype.area = function () { /* ... */ };
Circle.prototype = Object.create(Shape.prototype);
// Object.create replaces the entire prototype object — area() is gone!
```

> **Exam Tip:** The order matters. Always: (1) `Object.create`, (2) fix `constructor`, (3) add methods.

---

### 4. Property Shadowing

Setting a property on an instance does **not** change the prototype. It creates a new "own" property that **shadows** the prototype's property.

```js
function Gadget(name) {
  this.name = name;
}
Gadget.prototype.price = 100;

const g1 = new Gadget("Phone");
const g2 = new Gadget("Tablet");

console.log(g1.price); // 100 (from prototype)
console.log(g2.price); // 100 (from prototype)

g1.price = 200; // creates an OWN property on g1, does NOT change prototype

console.log(g1.price); // 200 (own property, shadows prototype)
console.log(g2.price); // 100 (still reading from prototype, unaffected)
console.log(g1.hasOwnProperty("price")); // true
console.log(g2.hasOwnProperty("price")); // false

delete g1.price; // removes the own property
console.log(g1.price); // 100 (back to reading from prototype)
```

> **Exam Tip:** Writing a property always creates/updates an **own** property. It never modifies the prototype. This is a classic "predict the output" topic.

---

### 5. `hasOwnProperty` vs `in` vs `instanceof`

```js
function Vehicle(make) {
  this.make = make;
}
Vehicle.prototype.wheels = 4;

const v = new Vehicle("Honda");
```

| Expression | Result | Why |
|---|---|---|
| `v.hasOwnProperty("make")` | `true` | `make` is directly on `v` |
| `v.hasOwnProperty("wheels")` | `false` | `wheels` is on `Vehicle.prototype`, not on `v` |
| `"make" in v` | `true` | `in` checks own + entire chain |
| `"wheels" in v` | `true` | Found on `Vehicle.prototype` in the chain |
| `"toString" in v` | `true` | Found on `Object.prototype` in the chain |
| `v instanceof Vehicle` | `true` | `Vehicle.prototype` is in `v`'s chain |
| `v instanceof Object` | `true` | `Object.prototype` is in `v`'s chain |

**Summary:**

- **`hasOwnProperty(prop)`** -- only own properties, does NOT walk the chain
- **`prop in obj`** -- own properties + the entire prototype chain
- **`obj instanceof Constructor`** -- checks if `Constructor.prototype` exists anywhere in `obj.__proto__` chain

```js
// How instanceof works internally (simplified):
function myInstanceof(obj, Constructor) {
  let proto = Object.getPrototypeOf(obj);
  while (proto !== null) {
    if (proto === Constructor.prototype) return true;
    proto = Object.getPrototypeOf(proto);
  }
  return false;
}
```

---

### 6. The Full Prototype Chain Diagram

```
instance (e.g., dog)
  |
  |__proto__
  v
Constructor.prototype (e.g., Dog.prototype)
  |
  |__proto__
  v
Object.prototype
  |
  |__proto__
  v
null  <-- end of the chain
```

**With inheritance (Circle extends Shape):**

```
circle instance
  |  .__proto__
  v
Circle.prototype          (created via Object.create(Shape.prototype))
  |  .__proto__
  v
Shape.prototype
  |  .__proto__
  v
Object.prototype
  |  .__proto__
  v
null
```

**Verify in code:**

```js
const c = new Circle("red", 5);

console.log(c.__proto__ === Circle.prototype);                    // true
console.log(c.__proto__.__proto__ === Shape.prototype);           // true
console.log(c.__proto__.__proto__.__proto__ === Object.prototype); // true
console.log(c.__proto__.__proto__.__proto__.__proto__ === null);   // true
```

---

### 7. Function.prototype and Object.prototype Relationships

This is where things get meta. Functions are objects too.

```js
// Every function's __proto__ is Function.prototype
console.log(Dog.__proto__ === Function.prototype);      // true
console.log(Array.__proto__ === Function.prototype);    // true
console.log(Object.__proto__ === Function.prototype);   // true  (Object is a function!)
console.log(Function.__proto__ === Function.prototype); // true  (Function is its own instance)

// Function.prototype is an object, so its __proto__ is Object.prototype
console.log(Function.prototype.__proto__ === Object.prototype); // true

// Object.prototype is the end of every chain
console.log(Object.prototype.__proto__ === null); // true
```

**The circular-looking part (exam question):**

```js
// Object is a function -> Object.__proto__ === Function.prototype
// Function.prototype is an object -> Function.prototype.__proto__ === Object.prototype
// Object.prototype is the root -> Object.prototype.__proto__ === null
```

This is not actually circular. The chain always terminates at `null`.

```
Dog (a function)
  |__proto__ -> Function.prototype
                  |__proto__ -> Object.prototype
                                  |__proto__ -> null

Dog.prototype (an object, NOT a function)
  |__proto__ -> Object.prototype
                  |__proto__ -> null
```

---

### 8. `Object.create()` -- What It Does & Polyfill

`Object.create(proto)` creates a **new empty object** whose `__proto__` is set to `proto`.

```js
const parent = {
  greet: function () {
    return "Hello from " + this.name;
  },
};

const child = Object.create(parent);
child.name = "Alice";

console.log(child.greet());       // "Hello from Alice"
console.log(child.__proto__ === parent); // true
console.log(child.hasOwnProperty("greet")); // false (it's on parent)
console.log(child.hasOwnProperty("name"));  // true
```

```js
// Object.create(null) creates an object with NO prototype at all
const bare = Object.create(null);
console.log(bare.__proto__);     // undefined (no prototype chain)
console.log(bare.toString);     // undefined (not even Object.prototype methods)
// Useful for dictionary/map objects with no inherited properties
```

**Polyfill (exam answer):**

```js
if (!Object.create) {
  Object.create = function (proto) {
    if (typeof proto !== 'object' && typeof proto !== 'function') {
      throw new TypeError('Argument must be an object or null');
    }
    function F() {}        // temporary empty constructor
    F.prototype = proto;   // set its prototype to the desired object
    return new F();        // new F() creates an object with __proto__ === proto
  };
}
```

**Why this works:** `new F()` does the 4 steps -- creates `{}`, links `__proto__` to `F.prototype` (which we set to `proto`), calls `F()` (does nothing), returns the object.

> **Exam Tip:** The polyfill for `Object.create` is essentially: make a throwaway constructor, set its `.prototype`, and call `new` on it. This ties together everything in this document.

---

## Predict the Output

### Question 1

```js
function Foo() {
  this.x = 1;
}
Foo.prototype.x = 2;
Foo.prototype.getX = function () {
  return this.x;
};

const f = new Foo();
console.log(f.x);          // ?
console.log(f.getX());     // ?
delete f.x;
console.log(f.x);          // ?
console.log(f.getX());     // ?
```

**Answer:**

```
1       // own property x = 1 shadows prototype's x = 2
1       // getX() returns this.x, this is f, f.x is 1 (own)
2       // after delete, own x is gone, falls back to prototype's x = 2
2       // same: this.x now resolves to prototype's x = 2
```

---

### Question 2

```js
function A() {}
function B() {}
B.prototype = Object.create(A.prototype);

const b = new B();
console.log(b instanceof B);  // ?
console.log(b instanceof A);  // ?
console.log(b.constructor === B); // ?
console.log(b.constructor === A); // ?
```

**Answer:**

```
true    // B.prototype is in b's chain
true    // A.prototype is in b's chain (B.prototype.__proto__ === A.prototype)
false   // We forgot to fix constructor! B.prototype was replaced by Object.create(A.prototype)
true    // constructor is inherited from A.prototype, where it's A
```

**Lesson:** Always do `B.prototype.constructor = B` after `Object.create`.

---

### Question 3

```js
function MyNew(Constructor, ...args) {
  const obj = Object.create(Constructor.prototype);
  const result = Constructor.apply(obj, args);
  return (typeof result === 'object' && result !== null) ? result : obj;
}

function Thing(val) {
  this.val = val;
  if (val > 10) {
    return { val: val * 2 };
  }
  return val; // primitive
}

const t1 = MyNew(Thing, 5);
const t2 = MyNew(Thing, 20);

console.log(t1.val);             // ?
console.log(t1 instanceof Thing); // ?
console.log(t2.val);             // ?
console.log(t2 instanceof Thing); // ?
```

**Answer:**

```
5       // val <= 10, returns primitive 5 (ignored), so obj with val:5 is used
true    // t1 was created with Object.create(Thing.prototype)
40      // val > 10, returns { val: 40 } (a non-null object), overrides obj
false   // the returned { val: 40 } is a plain object, not linked to Thing.prototype
```

---

### Question 4

```js
function Parent() {}
Parent.prototype.sayHi = function () {
  return "Hi";
};

function Child() {}
Child.prototype = new Parent();
Child.prototype.sayHi = function () {
  return "Hello";
};

const c = new Child();
console.log(c.sayHi());                  // ?
console.log(c.__proto__.__proto__.sayHi()); // ?
console.log(c.hasOwnProperty("sayHi"));  // ?
console.log("sayHi" in c);               // ?
```

**Answer:**

```
"Hello"   // Child.prototype.sayHi shadows Parent.prototype.sayHi
"Hi"      // c.__proto__ = Child.prototype, __proto__ again = Parent.prototype
false     // sayHi is on Child.prototype, not on c itself
true      // "in" walks the chain, finds it on Child.prototype
```

---

## Exam Tips (Collected)

- **The 4 steps of `new`** will appear on almost every exam. Know them cold. Practice writing `myNew`.
- **`prototype` vs `__proto__`**: if the exam asks "what is the prototype of X", figure out whether they mean the `.prototype` property (only on functions) or the `__proto__` link (on all objects).
- **`null` from a constructor**: even though `typeof null === "object"`, the `new` operator has a special-case check -- `null` does NOT override the default return. The check is `result !== null`.
- **Order of operations** when setting up inheritance: (1) `Object.create`, (2) fix `.constructor`, (3) add methods to prototype.
- **`delete` + prototype chain**: `delete` only removes own properties. After deleting, the prototype's property becomes visible again.
- **Factory vs Constructor**: factory returns `{}`, no `new`, no `instanceof`. Constructor uses `new`, gets `instanceof`, shares methods via prototype.
- **`Object.create(null)`**: creates a truly empty object with no prototype. Useful for dictionary objects. `toString`, `hasOwnProperty`, etc. will all be `undefined`.
- **Common trap**: `Child.prototype = Parent.prototype` is WRONG -- modifications to `Child.prototype` will also modify `Parent.prototype` because they are the same object. Use `Object.create(Parent.prototype)`.

---

## Quick Revision Table

| Concept | Key Point |
|---|---|
| Constructor function | Regular function called with `new`; capitalize name by convention |
| 4 steps of `new` | (1) Create `{}` (2) Link `__proto__` to `Constructor.prototype` (3) Call constructor with `this` = new obj (4) Return obj unless constructor returns non-null object |
| Return from constructor | Non-null object overrides; primitives and `null` are ignored |
| `myNew` polyfill | `Object.create(Ctor.prototype)` + `Ctor.apply(obj, args)` + check return |
| `.prototype` | Property on **functions**; becomes `__proto__` of instances |
| `.__proto__` | Property on **all objects**; points to parent in prototype chain |
| Prototype chain | `instance -> Constructor.prototype -> Object.prototype -> null` |
| Inheritance setup | `Child.prototype = Object.create(Parent.prototype)` then fix `.constructor` |
| Property shadowing | Setting a prop on instance hides (does not modify) prototype prop |
| `hasOwnProperty(p)` | Only own properties |
| `p in obj` | Own + prototype chain |
| `obj instanceof C` | Is `C.prototype` anywhere in obj's `__proto__` chain? |
| `Object.create(proto)` | Creates new `{}` with `__proto__` set to `proto` |
| `Object.create(null)` | Creates object with **no** prototype chain at all |
| Factory vs Constructor | Factory: no `new`, no `instanceof`, no shared prototype; Constructor: `new`, `instanceof`, shared methods |
| `Function.prototype` | `__proto__` of all functions; itself inherits from `Object.prototype` |
| `Object.prototype.__proto__` | `null` -- end of every chain |
