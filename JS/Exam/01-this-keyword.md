# The `this` Keyword in JavaScript -- Exam Study Guide

---

## 0. Start Here: A Broken Example

Read this code and predict the output BEFORE reading the explanation.

```js
const user = {
  name: "Alice",
  greet: function () {
    console.log("Hello, " + this.name);
  },
};

user.greet();           // ?
const greetFn = user.greet;
greetFn();              // ?
```

**Output:**
```
Hello, Alice
Hello, undefined       // (or "" in browser non-strict, or TypeError in strict)
```

**Why?** `this` is NOT determined by where a function is written. It is determined by **how the function is called**. The second call has no object in front of it -- so `this` falls to the default binding (globalThis or undefined).

This single idea -- **runtime binding** -- is the foundation of every `this` question on your exam.

---

## 1. What is `this`?

- `this` is a **runtime binding**, not a compile-time one.
- Its value is set when a function is **called**, not when it is **defined**.
- Think of it as: "Who called me?"

**BAD mental model:**
> "`this` refers to the object the function is inside."

**GOOD mental model:**
> "`this` refers to the object that is calling the function at the moment of invocation."

```js
function showThis() {
  console.log(this);
}

const objA = { name: "A", show: showThis };
const objB = { name: "B", show: showThis };

objA.show(); // { name: "A", show: [Function] }  -- this = objA
objB.show(); // { name: "B", show: [Function] }  -- this = objB
```

Same function, different `this` -- because the **call site** changed.

---

## 2. The 4 Binding Rules (Priority Order)

This is the core framework. Memorize the priority -- exams LOVE asking which rule wins.

| Priority | Rule               | How it looks                       | `this` =                  |
| -------- | ------------------ | ---------------------------------- | ------------------------- |
| 1 (highest) | **new binding**    | `new Foo()`                        | the newly created object  |
| 2        | **Explicit binding**| `fn.call(obj)` / `fn.apply(obj)` / `fn.bind(obj)` | `obj`         |
| 3        | **Implicit binding**| `obj.fn()`                         | `obj` (left of the dot)   |
| 4 (lowest)  | **Default binding** | `fn()`                            | `globalThis` (non-strict) or `undefined` (strict) |

When multiple rules could apply, **higher priority wins**.

---

### 2.1 Default Binding (Priority 4 -- Lowest)

A plain function call with nothing in front of it.

```js
function whoAmI() {
  console.log(this);
}

whoAmI(); // non-strict: globalThis (window in browser, global in Node)
          // strict mode: undefined
```

**BAD -- assuming `this` is always the global:**
```js
"use strict";
function whoAmI() {
  console.log(this);   // undefined, NOT globalThis
}
whoAmI();
```

**GOOD -- checking strict mode:**
```js
"use strict";
function whoAmI() {
  console.log(this);   // undefined
}
whoAmI();
// If you need globalThis, reference it directly: console.log(globalThis);
```

---

### 2.2 Implicit Binding (Priority 3 -- The Dot Rule)

When a function is called as a method of an object, `this` = the object **left of the dot**.

```js
const car = {
  brand: "Toyota",
  getBrand: function () {
    return this.brand;
  },
};

console.log(car.getBrand()); // "Toyota" -- car is left of the dot
```

**The trap: only the LAST dot matters.**

```js
const outer = {
  inner: {
    value: 42,
    getValue: function () {
      return this.value;
    },
  },
};

console.log(outer.inner.getValue()); // 42 -- this = outer.inner, NOT outer
```

---

### 2.3 Explicit Binding (Priority 2 -- `call`, `apply`, `bind`)

You manually specify what `this` should be.

#### `call` -- invoke immediately, args listed out
```js
function greet(greeting, punctuation) {
  console.log(greeting + ", " + this.name + punctuation);
}

const person = { name: "Bob" };
greet.call(person, "Hello", "!"); // "Hello, Bob!"
```

#### `apply` -- invoke immediately, args as array
```js
greet.apply(person, ["Hi", "?"]); // "Hi, Bob?"
```

#### `bind` -- returns a NEW function with `this` permanently set
```js
const boundGreet = greet.bind(person, "Hey");
boundGreet(".");   // "Hey, Bob."
boundGreet("!!!");  // "Hey, Bob!!!"
// boundGreet's `this` is ALWAYS person, no matter how you call it
```

**Exam Tip:** `call` and `apply` invoke the function immediately. `bind` does NOT invoke it -- it returns a new function.

| Method  | Invokes immediately? | Arguments           |
| ------- | -------------------- | ------------------- |
| `call`  | Yes                  | Listed: `a, b, c`   |
| `apply` | Yes                  | Array: `[a, b, c]`  |
| `bind`  | No (returns new fn)  | Listed: `a, b, c`   |

**Mnemonic:** **A**pply takes an **A**rray.

---

### 2.4 `new` Binding (Priority 1 -- Highest)

When a function is called with `new`, JavaScript:
1. Creates a brand-new empty object `{}`
2. Sets `this` = that new object
3. Executes the function body
4. Returns `this` (unless the function explicitly returns another object)

```js
function Person(name) {
  // this = {} (new empty object created by `new`)
  this.name = name;
  // implicitly: return this;
}

const p = new Person("Carol");
console.log(p.name); // "Carol"
```

**BAD -- forgetting `new`:**
```js
const p = Person("Carol"); // no `new`!
console.log(p);            // undefined (function returns nothing)
console.log(name);         // "Carol" leaked onto globalThis (non-strict)
```

**GOOD -- always use `new` with constructor functions:**
```js
const p = new Person("Carol");
console.log(p.name); // "Carol"
```

---

## 3. Global Context

At the top level (outside any function), `this` depends on the environment.

| Environment            | Top-level `this`         |
| ---------------------- | ------------------------ |
| Browser (script tag)   | `window` (the global object) |
| Node.js (REPL)         | `global`                 |
| Node.js (module file)  | `module.exports` (NOT global) |
| ES Module              | `undefined`              |

```js
// Browser <script> tag:
console.log(this === window); // true

// Node.js REPL:
console.log(this === global); // true

// Node.js file (CommonJS):
console.log(this === module.exports); // true
console.log(this === global);        // false
```

**Exam Tip:** If the question says "in Node.js" without specifying REPL vs file, ask yourself whether it is module-level code. In a module file, top-level `this` is `module.exports`, NOT `global`.

---

## 4. Function Context -- Plain Function Calls

A plain function call (no dot, no `new`, no `call/apply/bind`) uses **default binding**.

```js
function show() {
  console.log(this);
}

show(); // non-strict: globalThis
        // strict: undefined
```

This is the single most common source of bugs with `this`.

---

## 5. Object Method Context -- Implicit Binding

```js
const counter = {
  count: 0,
  increment: function () {
    this.count++;
  },
};

counter.increment();
console.log(counter.count); // 1 -- this = counter
```

**But implicit binding is FRAGILE.** It is lost the moment the function is separated from the object.

```js
const inc = counter.increment;  // extracted the method
inc();                          // this != counter anymore!
console.log(counter.count);     // still 1 -- the increment went to globalThis.count
```

---

## 6. Strict Mode vs Non-Strict Mode

Strict mode **only changes the default binding rule.** The other three rules are unaffected.

| Scenario                        | Non-strict         | Strict             |
| ------------------------------- | ------------------ | ------------------ |
| Plain function call `fn()`      | `this` = globalThis | `this` = `undefined` |
| Object method `obj.fn()`        | `this` = `obj`     | `this` = `obj`     |
| `fn.call(obj)`                  | `this` = `obj`     | `this` = `obj`     |
| `new Fn()`                      | `this` = new obj   | `this` = new obj   |

```js
// Non-strict
function a() { console.log(this); }
a(); // globalThis (window/global)

// Strict
"use strict";
function b() { console.log(this); }
b(); // undefined
```

**Exam Tip:** `"use strict"` makes default binding return `undefined`, which causes `TypeError` if you try `this.someProperty`. This is actually helpful -- it surfaces bugs instead of silently polluting the global object.

---

## 7. Arrow Functions vs Regular Functions

This is the **highest-value exam topic** for `this`. Expect at least one question.

### The Core Rule

- **Regular function:** `this` is set at **call time** (runtime binding).
- **Arrow function:** `this` is captured from the **enclosing lexical scope** at **definition time**. An arrow function has **no own `this`**.

```js
const obj = {
  value: 10,
  // Regular function -- this = obj when called as obj.regular()
  regular: function () {
    console.log(this.value);
  },
  // Arrow function -- this = whatever `this` is in the surrounding SCOPE
  arrow: () => {
    console.log(this.value);
  },
};

obj.regular(); // 10
obj.arrow();   // undefined -- this is NOT obj!
```

### Why `obj.arrow()` gives `undefined`

An arrow function captures `this` from the surrounding **scope**. The surrounding scope of the arrow function defined inside the object literal `{}` is the **module/global scope** -- NOT the object itself.

**An object literal `{}` is NOT a scope.** Only functions, blocks (for `let`/`const`), and modules create scope.

So `this` inside the arrow = `this` at the point where the object literal was evaluated = globalThis (or undefined in strict/module).

### BAD vs GOOD Patterns

**BAD -- arrow function as an object method:**
```js
const timer = {
  seconds: 0,
  start: () => {
    // `this` is NOT timer -- it is the outer scope's `this`
    setInterval(() => {
      this.seconds++;  // wrong `this`!
    }, 1000);
  },
};
```

**GOOD -- regular function as method, arrow function for callback:**
```js
const timer = {
  seconds: 0,
  start: function () {
    // `this` = timer (implicit binding)
    setInterval(() => {
      this.seconds++;  // arrow captures `this` from start(), which is timer
    }, 1000);
  },
};
```

### Full Comparison Table

| Feature                     | Regular Function               | Arrow Function                  |
| --------------------------- | ------------------------------ | ------------------------------- |
| Own `this`                  | Yes -- set at call time        | No -- inherits from lexical scope |
| `arguments` object          | Yes                            | No (use rest params `...args`)  |
| Can be used with `new`      | Yes                            | No -- throws TypeError          |
| Has `prototype` property    | Yes                            | No                              |
| Can be a generator          | Yes (`function*`)              | No                              |
| `call`/`apply`/`bind` on `this` | Works -- overrides `this`  | Ignored -- `this` stays lexical |

**Exam Tip:** `bind`, `call`, and `apply` have **no effect** on the `this` of an arrow function. The arrow function always uses the lexically captured `this`.

```js
const arrow = () => console.log(this);
const obj = { name: "test" };
arrow.call(obj);  // still globalThis/undefined -- NOT obj
```

---

## 8. `this` in Callbacks -- Why It Gets Lost

Whenever you pass a method as a callback, you are **extracting** it from the object. The callback caller invokes it as a plain function -- so implicit binding is lost.

### setTimeout

**BAD:**
```js
const user = {
  name: "Alice",
  greet: function () {
    console.log("Hello, " + this.name);
  },
};

setTimeout(user.greet, 1000); // "Hello, undefined"
// setTimeout receives the function reference and calls it as a plain function
```

**GOOD (Fix 1 -- arrow wrapper):**
```js
setTimeout(() => user.greet(), 1000); // "Hello, Alice"
// The arrow function calls user.greet() with the dot -- implicit binding preserved
```

**GOOD (Fix 2 -- bind):**
```js
setTimeout(user.greet.bind(user), 1000); // "Hello, Alice"
```

### Event Handlers

In DOM event handlers, `this` = the element that the listener is attached to (unless you use an arrow function).

```js
// Regular function -- this = the button element
button.addEventListener("click", function () {
  console.log(this); // <button> element
});

// Arrow function -- this = surrounding scope's this (probably window/undefined)
button.addEventListener("click", () => {
  console.log(this); // NOT the button
});
```

### Array Methods (forEach, map, etc.)

**BAD:**
```js
const processor = {
  multiplier: 2,
  process: function (items) {
    return items.map(function (item) {
      return item * this.multiplier; // `this` is NOT processor
    });
  },
};
processor.process([1, 2, 3]); // [NaN, NaN, NaN]
```

**GOOD (Fix 1 -- arrow function):**
```js
const processor = {
  multiplier: 2,
  process: function (items) {
    return items.map((item) => {
      return item * this.multiplier; // arrow captures `this` from process()
    });
  },
};
processor.process([1, 2, 3]); // [2, 4, 6]
```

**GOOD (Fix 2 -- thisArg parameter):**
```js
const processor = {
  multiplier: 2,
  process: function (items) {
    return items.map(function (item) {
      return item * this.multiplier;
    }, this); // <-- second argument to map sets `this` inside the callback
  },
};
processor.process([1, 2, 3]); // [2, 4, 6]
```

---

## 9. Common `this` Traps (With Fixes)

### Trap 1: Method Extraction

```js
const obj = {
  x: 42,
  getX: function () {
    return this.x;
  },
};

console.log(obj.getX());          // 42 -- implicit binding

const extractedGetX = obj.getX;   // just a function reference now
console.log(extractedGetX());     // undefined -- default binding
```

**Fix:**
```js
const extractedGetX = obj.getX.bind(obj);
console.log(extractedGetX()); // 42
```

### Trap 2: Passing Method as Callback

```js
function runCallback(cb) {
  cb(); // plain function call -- default binding
}

const obj = {
  name: "Test",
  getName: function () {
    return this.name;
  },
};

runCallback(obj.getName); // undefined (or "" in browser non-strict)
```

**Fix:**
```js
runCallback(obj.getName.bind(obj)); // "Test"
// or
runCallback(() => obj.getName());   // "Test"
```

### Trap 3: Nested Function Inside a Method

```js
const obj = {
  value: 100,
  outer: function () {
    console.log(this.value); // 100 -- implicit binding

    function inner() {
      console.log(this.value); // undefined -- inner() is a plain call!
    }
    inner();
  },
};
obj.outer();
```

The nested `inner()` is a plain function call inside `outer`. It does NOT inherit `this` from `outer`.

**Fix 1 -- Arrow function (best):**
```js
const obj = {
  value: 100,
  outer: function () {
    const inner = () => {
      console.log(this.value); // 100 -- arrow captures outer's `this`
    };
    inner();
  },
};
```

**Fix 2 -- `const self = this` (older pattern, still shows up on exams):**
```js
const obj = {
  value: 100,
  outer: function () {
    const self = this;
    function inner() {
      console.log(self.value); // 100 -- using closure, not `this`
    }
    inner();
  },
};
```

**Fix 3 -- `call`:**
```js
const obj = {
  value: 100,
  outer: function () {
    function inner() {
      console.log(this.value);
    }
    inner.call(this); // explicitly pass `this`
  },
};
```

### Trap 4: Arrow Function in Object Literal (Repeat for Emphasis)

```js
const obj = {
  name: "Gotcha",
  getName: () => this.name,  // `this` = enclosing scope, NOT obj
};

console.log(obj.getName()); // undefined
```

**There is no fix that keeps it as an arrow function.** Use a regular function for object methods:
```js
const obj = {
  name: "Gotcha",
  getName() { return this.name; }, // shorthand method -- regular function
};

console.log(obj.getName()); // "Gotcha"
```

---

## 10. Binding Precedence -- Proof With Code

### new > explicit

```js
function Foo(val) {
  this.a = val;
}

const obj = {};
const boundFoo = Foo.bind(obj);

boundFoo(2);
console.log(obj.a); // 2 -- explicit binding worked

const baz = new boundFoo(3);
console.log(obj.a); // 2 -- unchanged!
console.log(baz.a); // 3 -- new binding overrode the bind!
```

`new` wins over `bind`.

### explicit > implicit

```js
function greet() {
  console.log(this.name);
}

const alice = { name: "Alice", greet: greet };
const bob = { name: "Bob" };

alice.greet();           // "Alice" -- implicit binding
alice.greet.call(bob);   // "Bob" -- explicit wins over implicit
```

### implicit > default

```js
function show() {
  console.log(this.x);
}

const obj = { x: 99, show: show };

show();      // undefined -- default binding
obj.show();  // 99 -- implicit wins over default
```

### Full priority chain

```
new binding  >  explicit (call/apply/bind)  >  implicit (dot)  >  default (plain call)
```

---

## Exam Tips -- Memorize These

1. **"this" = "who called me?"** Look at the call site, not the definition site.
2. **Dot rule:** `obj.fn()` means `this = obj`. Only the **last** dot counts.
3. **Extraction kills implicit binding:** `const f = obj.fn; f()` -- `this` is gone.
4. **Arrow functions have no `this`.** They borrow from their enclosing **scope**.
5. **Object literals are NOT scopes.** An arrow in `{}` sees the outer scope's `this`.
6. **`bind` returns a new function.** `call`/`apply` invoke immediately.
7. **`apply` takes an Array** (A for A). `call` takes comma-separated args.
8. **Strict mode only changes default binding** (globalThis becomes undefined). Other rules are unchanged.
9. **`new` beats everything.** Even `bind` cannot override `new`.
10. **Arrow functions ignore `call`/`apply`/`bind`** for `this`. They always use lexical `this`.

---

## Predict the Output -- Exam Practice

### Question 1

```js
"use strict";

const obj = {
  a: 10,
  foo: function () {
    console.log(this.a);
  },
  bar: () => {
    console.log(this.a);
  },
};

obj.foo();
obj.bar();

const fn = obj.foo;
fn();
```

<details>
<summary><strong>Answer</strong></summary>

```
10        // obj.foo() -- implicit binding, this = obj
undefined // obj.bar() -- arrow function, this = enclosing scope (module/global), not obj
TypeError // fn() -- strict mode default binding, this = undefined, cannot read property 'a' of undefined
```

**Why `obj.bar()` is undefined:** The arrow function captures `this` from the scope where the object literal is evaluated. The object literal is NOT a scope. In strict mode / module scope, the enclosing `this` is `undefined`, so `this.a` is `undefined` (or throws if `this` itself is undefined at the top level -- behavior varies by environment). In a browser non-strict `<script>`, `this` at the top level is `window`, so `this.a` would be `undefined` (since `window.a` is not defined).

**Why `fn()` throws:** In strict mode, a plain function call sets `this` to `undefined`. Accessing `undefined.a` throws `TypeError`.

</details>

---

### Question 2

```js
function Person(name) {
  this.name = name;
  this.sayName = () => {
    console.log(this.name);
  };
}

const alice = new Person("Alice");
const bob = { name: "Bob" };

alice.sayName();
alice.sayName.call(bob);
```

<details>
<summary><strong>Answer</strong></summary>

```
Alice
Alice
```

**Why both are "Alice":** `sayName` is an arrow function defined inside the `Person` constructor. When `new Person("Alice")` runs, `this` inside the constructor is the new object (alice). The arrow function captures that `this` permanently. Even `call(bob)` cannot override an arrow function's `this`. So both calls print "Alice".

</details>

---

### Question 3

```js
const obj = {
  x: 1,
  getX: function () {
    return this.x;
  },
};

const obj2 = {
  x: 2,
  getX: obj.getX,
};

console.log(obj.getX());
console.log(obj2.getX());
console.log(obj.getX.call(obj2));
console.log(obj.getX.bind(obj)());
```

<details>
<summary><strong>Answer</strong></summary>

```
1  // implicit binding: this = obj
2  // implicit binding: this = obj2 (obj2 is left of the dot)
2  // explicit binding via call: this = obj2
1  // explicit binding via bind: this = obj (bind is permanent)
```

</details>

---

### Question 4

```js
const obj = {
  count: 0,
  increment: function () {
    [1, 2, 3].forEach(function (num) {
      this.count += num;
    });
  },
};

obj.increment();
console.log(obj.count);
```

<details>
<summary><strong>Answer</strong></summary>

```
0
```

**Why:** The `forEach` callback is a **regular function** invoked as a plain function call (by `forEach` internally). So `this` inside it is `globalThis` (non-strict) or `undefined` (strict). Either way, `obj.count` is never touched. It stays `0`.

**Fix (any of these):**

```js
// Fix 1: arrow function
[1, 2, 3].forEach((num) => { this.count += num; });

// Fix 2: thisArg
[1, 2, 3].forEach(function (num) { this.count += num; }, this);

// Fix 3: bind
[1, 2, 3].forEach(function (num) { this.count += num; }.bind(this));
```

After any fix, `obj.count` would be `6`.

</details>

---

## Quick Revision Table

| Call Style                     | `this` =                              | Rule Name        | Priority |
| ------------------------------ | ------------------------------------- | ---------------- | -------- |
| `fn()`                         | `globalThis` (non-strict) or `undefined` (strict) | Default          | 4 (lowest) |
| `obj.fn()`                     | `obj`                                 | Implicit (dot)   | 3        |
| `fn.call(ctx)` / `fn.apply(ctx)` | `ctx`                              | Explicit         | 2        |
| `fn.bind(ctx)()`               | `ctx`                                 | Explicit (bind)  | 2        |
| `new Fn()`                     | newly created object                  | new              | 1 (highest) |
| `() => {}`                     | `this` of enclosing **scope** (lexical) | Lexical (arrow)| N/A -- cannot be overridden |

| Scenario                       | `this` lost? | Fix                                    |
| ------------------------------ | ------------ | -------------------------------------- |
| `const f = obj.method; f()`    | Yes          | `f.bind(obj)` or `() => obj.method()`  |
| `setTimeout(obj.method, 100)`  | Yes          | `.bind(obj)` or arrow wrapper           |
| Nested function inside method  | Yes          | Arrow function, `.call(this)`, or `const self = this` |
| Arrow function in object literal | Never had it | Use a regular function for methods    |
| `call`/`apply` on arrow fn     | Cannot set   | Use a regular function if you need dynamic `this` |

| Strict mode effect             | Changes?                               |
| ------------------------------ | -------------------------------------- |
| Default binding                | Yes: `globalThis` becomes `undefined`  |
| Implicit binding               | No change                              |
| Explicit binding               | No change                              |
| `new` binding                  | No change                              |
| Arrow function `this`          | No change (still lexical)              |

---

**One final rule to carry into the exam:**

Look at the **call site**. Ask: Is there `new`? Is there `call`/`apply`/`bind`? Is there a dot? If none, it is default binding. Apply the highest-priority rule that matches. If it is an arrow function, ignore all of the above -- `this` was decided when the arrow was created, not when it was called.
