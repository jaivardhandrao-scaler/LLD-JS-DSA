# Call, Apply & Bind -- Internals & Polyfills (Class 5)

---

## 1. Start With the Problem: Why Do These Exist?

```js
const student = {
  name: "Jai",
  greet: function () {
    console.log("Hi, I am " + this.name);
  },
};

student.greet(); // "Hi, I am Jai"  -- works fine

const greetFn = student.greet; // extract the method
greetFn(); // "Hi, I am undefined"  -- 'this' is LOST
```

**What happened?** When you pull a method off an object and call it standalone, `this` no longer points to that object. It defaults to `globalThis` (browser: `window`, Node: `global`) in non-strict mode, or `undefined` in strict mode.

**`call`, `apply`, and `bind` exist to explicitly set `this` when calling a function.**

```js
const student = {
  name: "Jai",
  greet: function () {
    console.log("Hi, I am " + this.name);
  },
};

const greetFn = student.greet;

// FIX with call
greetFn.call(student);   // "Hi, I am Jai"

// FIX with apply
greetFn.apply(student);  // "Hi, I am Jai"

// FIX with bind
const boundGreet = greetFn.bind(student);
boundGreet();            // "Hi, I am Jai"
```

---

## 2. `Function.prototype.call`

**Syntax:** `fn.call(thisArg, arg1, arg2, ...)`

- Invokes the function **immediately**.
- First argument becomes `this` inside the function.
- Remaining arguments are passed **individually** (comma-separated).

```js
function introduce(city, age) {
  console.log(this.name + " from " + city + ", age " + age);
}

const person = { name: "Jai" };

introduce.call(person, "Bangalore", 21);
// "Jai from Bangalore, age 21"
```

**BAD vs GOOD**

```js
// BAD -- losing context
const obj = {
  val: 10,
  getVal: function () { return this.val; }
};
const fn = obj.getVal;
console.log(fn()); // undefined  (this = globalThis)

// GOOD -- explicit context with call
console.log(fn.call(obj)); // 10
```

---

## 3. `Function.prototype.apply`

**Syntax:** `fn.apply(thisArg, [arg1, arg2, ...])`

- Invokes the function **immediately**.
- First argument becomes `this`.
- Second argument is an **array** (or array-like) of arguments.

```js
function introduce(city, age) {
  console.log(this.name + " from " + city + ", age " + age);
}

const person = { name: "Jai" };

introduce.apply(person, ["Bangalore", 21]);
// "Jai from Bangalore, age 21"
```

**When to pick `apply` over `call`?** When your arguments are already in an array.

```js
const nums = [5, 1, 9, 3];
Math.max.apply(null, nums); // 9
// Equivalent modern approach: Math.max(...nums)
```

---

## 4. `Function.prototype.bind`

**Syntax:** `const boundFn = fn.bind(thisArg, arg1, arg2, ...)`

- Does **NOT** invoke the function.
- Returns a **new function** with `this` permanently locked to `thisArg`.
- Pre-set arguments are applied first (partial application).

```js
function introduce(city, age) {
  console.log(this.name + " from " + city + ", age " + age);
}

const person = { name: "Jai" };

const boundFn = introduce.bind(person, "Bangalore");
boundFn(21); // "Jai from Bangalore, age 21"
// "Bangalore" was pre-set, 21 was supplied later
```

### Partial Application with `bind`

```js
function multiply(a, b) {
  return a * b;
}

const double = multiply.bind(null, 2); // lock first arg to 2
console.log(double(5));  // 10
console.log(double(10)); // 20

const triple = multiply.bind(null, 3);
console.log(triple(5));  // 15
```

**BAD vs GOOD -- setTimeout context**

```js
const user = {
  name: "Jai",
  sayHi: function () {
    console.log("Hi " + this.name);
  },
};

// BAD -- this is lost inside setTimeout callback
setTimeout(user.sayHi, 1000); // "Hi undefined"

// GOOD -- bind locks the context
setTimeout(user.sayHi.bind(user), 1000); // "Hi Jai"
```

---

## 5. Comparison Table: call vs apply vs bind

| Feature | `call` | `apply` | `bind` |
|---|---|---|---|
| **Invokes immediately?** | Yes | Yes | No (returns new function) |
| **Arguments format** | Comma-separated | Single array | Comma-separated (pre-set) |
| **Return value** | Result of function call | Result of function call | A new bound function |
| **Use case** | Invoke with specific `this` | Invoke when args are in array | Store for later / callbacks |

**Memory trick:** **C**all = **C**ommas, **A**pply = **A**rray, **B**ind = **B**ound (later).

---

## 6. Function Borrowing

Use a method from one object on a completely different object.

### Example 1: Borrowing `Array.prototype` methods on array-likes

```js
const arrayLike = { 0: "a", 1: "b", 2: "c", length: 3 };

// arrayLike has no .slice(), .map(), etc. -- borrow from Array
const realArray = Array.prototype.slice.call(arrayLike);
console.log(realArray); // ["a", "b", "c"]

// Borrow .join()
const joined = Array.prototype.join.call(arrayLike, "-");
console.log(joined); // "a-b-c"
```

### Example 2: Borrowing between plain objects

```js
const person1 = {
  name: "Jai",
  greet: function (greeting) {
    console.log(greeting + ", " + this.name);
  },
};

const person2 = { name: "Rahul" };

// person2 has no greet -- borrow from person1
person1.greet.call(person2, "Hello"); // "Hello, Rahul"
```

### Example 3: Classic `arguments` object trick

```js
function demo() {
  // 'arguments' is array-like, not a real array
  const args = Array.prototype.slice.call(arguments);
  console.log(args); // now a real array
}
demo(1, 2, 3); // [1, 2, 3]
```

---

## 7. Context Loss Scenarios & Fixes

### Scenario A: Extracting a method

```js
const obj = {
  x: 42,
  getX: function () { return this.x; }
};

const extracted = obj.getX;
console.log(extracted());          // undefined (this lost)
console.log(extracted.call(obj));  // 42 (fixed with call)
```

### Scenario B: Passing method as callback

```js
const logger = {
  prefix: "LOG:",
  log: function (msg) {
    console.log(this.prefix + " " + msg);
  },
};

// BAD
[1, 2, 3].forEach(logger.log);
// "undefined 1", "undefined 2", "undefined 3"

// GOOD -- bind
[1, 2, 3].forEach(logger.log.bind(logger));
// "LOG: 1", "LOG: 2", "LOG: 3"
```

### Scenario C: Nested function inside method

```js
const obj = {
  name: "Jai",
  outer: function () {
    function inner() {
      console.log(this.name); // 'this' is NOT obj here
    }
    inner(); // this = globalThis (non-strict) or undefined (strict)
  },
};

obj.outer(); // undefined

// FIX 1: call
// inner.call(this);

// FIX 2: bind
// const inner = (function () { ... }).bind(this);

// FIX 3: arrow function (arrow inherits 'this' from outer)
```

---

## 8. Edge Cases

### Passing `null` or `undefined` as context

```js
// NON-STRICT MODE
function showThis() { console.log(this); }

showThis.call(null);      // globalThis (window / global)
showThis.call(undefined); // globalThis
showThis.call(0);         // Number {0}  (primitives get boxed)
showThis.call("hello");   // String {"hello"}
```

```js
// STRICT MODE
"use strict";
function showThis() { console.log(this); }

showThis.call(null);      // null  (stays null)
showThis.call(undefined); // undefined
showThis.call(0);         // 0  (no boxing)
```

### `bind` cannot be overridden by `call` or `apply`

```js
function greet() {
  console.log(this.name);
}

const obj1 = { name: "Jai" };
const obj2 = { name: "Rahul" };

const bound = greet.bind(obj1);
bound();              // "Jai"
bound.call(obj2);     // "Jai"  -- still obj1, NOT obj2
bound.apply(obj2);    // "Jai"  -- still obj1
```

**Once bound, always bound. `call`/`apply` cannot override `bind`.**

### Binding an already-bound function

```js
const fn = function () { console.log(this.name); };

const b1 = fn.bind({ name: "A" });
const b2 = b1.bind({ name: "B" }); // second bind has NO effect on 'this'

b2(); // "A"  -- first bind wins
```

---

## 9. Polyfill for `Function.prototype.myCall` (EXAM CRITICAL)

```js
Function.prototype.myCall = function (context, ...args) {
  // Step 1: If context is null/undefined, default to globalThis
  context = context || globalThis;

  // Step 2: Create a unique property to avoid overwriting existing keys
  const sym = Symbol();

  // Step 3: Attach the function (this) as a method of context
  //         'this' here is the function myCall was invoked on
  context[sym] = this;

  // Step 4: Invoke the function as a method of context
  //         This makes 'this' inside the function point to context
  const result = context[sym](...args);

  // Step 5: Clean up -- remove the temporary property
  delete context[sym];

  // Step 6: Return whatever the original function returned
  return result;
};
```

### Line-by-Line Explanation

| Line | What it does |
|---|---|
| `context = context \|\| globalThis` | Mirrors native behavior: if `null`/`undefined`, use global object |
| `const sym = Symbol()` | Creates a unique key so we never collide with existing properties |
| `context[sym] = this` | `this` is the function that `.myCall` was called on. Attaching it to `context` as a method means when we call it, its `this` will be `context` |
| `context[sym](...args)` | Invoke the function with spread args. Because it is called as `context.method()`, `this` inside it becomes `context` |
| `delete context[sym]` | Remove the temporary property to leave the object clean |
| `return result` | Forward the return value |

### Test Case

```js
function add(a, b) {
  return this.base + a + b;
}

const obj = { base: 100 };

console.log(add.myCall(obj, 5, 10)); // 115
console.log(add.call(obj, 5, 10));   // 115 (matches native)
```

---

## 10. Polyfill for `Function.prototype.myApply` (EXAM CRITICAL)

```js
Function.prototype.myApply = function (context, args) {
  // Step 1: Default context
  context = context || globalThis;

  // Step 2: Default args to empty array if not provided
  args = args || [];

  // Step 3: Unique key
  const sym = Symbol();

  // Step 4: Attach function to context
  context[sym] = this;

  // Step 5: Invoke with array spread
  const result = context[sym](...args);

  // Step 6: Clean up
  delete context[sym];

  return result;
};
```

### Key Difference from `myCall`

| `myCall` | `myApply` |
|---|---|
| `...args` (rest parameter -- collects individual args) | `args` (single parameter -- expects an array) |

Everything else is identical. The only difference is **how arguments are received**: `myCall` uses rest params, `myApply` takes a single array.

### Test Case

```js
function introduce(city, age) {
  return this.name + " from " + city + ", age " + age;
}

const person = { name: "Jai" };

console.log(introduce.myApply(person, ["Bangalore", 21]));
// "Jai from Bangalore, age 21"

console.log(introduce.apply(person, ["Bangalore", 21]));
// "Jai from Bangalore, age 21"  (matches native)
```

---

## 11. Polyfill for `Function.prototype.myBind` (EXAM CRITICAL)

```js
Function.prototype.myBind = function (context, ...presetArgs) {
  // Step 1: Save reference to the original function
  const fn = this;

  // Step 2: Return a NEW function (bind never invokes immediately)
  return function (...laterArgs) {
    // Step 3: When the returned function is called,
    //         invoke the original function with:
    //         - 'context' as this
    //         - presetArgs first, then laterArgs (partial application)
    return fn.apply(context, [...presetArgs, ...laterArgs]);
  };
};
```

### Line-by-Line Explanation

| Line | What it does |
|---|---|
| `const fn = this` | Captures the original function. Inside the returned closure, `this` will change, so we store it now |
| `...presetArgs` | Captures arguments given at bind-time (for partial application) |
| `return function (...laterArgs)` | Returns a new function (this is what makes `bind` different from `call`/`apply`) |
| `fn.apply(context, [...presetArgs, ...laterArgs])` | Calls the original function with locked `this` and merges preset + later arguments |

### Test Case

```js
function greet(greeting, punctuation) {
  return greeting + ", " + this.name + punctuation;
}

const person = { name: "Jai" };

const boundGreet = greet.myBind(person, "Hello");
console.log(boundGreet("!"));  // "Hello, Jai!"
console.log(boundGreet(".")); // "Hello, Jai."

// Matches native
const nativeBound = greet.bind(person, "Hello");
console.log(nativeBound("!")); // "Hello, Jai!"
```

### Partial Application Demo

```js
function sum(a, b, c) {
  return a + b + c;
}

const addFive = sum.myBind(null, 5);
console.log(addFive(3, 2));  // 10  (5 + 3 + 2)

const addFiveAndThree = sum.myBind(null, 5, 3);
console.log(addFiveAndThree(2)); // 10  (5 + 3 + 2)
```

---

## Predict the Output (Exam Practice)

### Question 1

```js
const obj = {
  name: "Jai",
  getName: function () {
    return this.name;
  },
};

const getName = obj.getName;
console.log(getName());                   // ?
console.log(getName.call(obj));           // ?
console.log(getName.bind(obj).call({ name: "Rahul" })); // ?
```

**Answer:**
- `getName()` --> `undefined` (context lost, `this` is `globalThis`, no `.name`)
- `getName.call(obj)` --> `"Jai"` (explicitly set `this` to `obj`)
- `getName.bind(obj).call({ name: "Rahul" })` --> `"Jai"` (bind locks `this` to `obj`; subsequent `.call()` cannot override it)

---

### Question 2

```js
function foo() {
  console.log(this);
}

foo.call(0);
foo.call("");
foo.call(null);
```

**Answer (non-strict mode):**
- `foo.call(0)` --> `Number {0}` (primitive gets boxed into a Number wrapper object)
- `foo.call("")` --> `String {""}` (primitive gets boxed)
- `foo.call(null)` --> `globalThis` / `window` (null/undefined defaults to global object)

**Answer (strict mode):**
- `foo.call(0)` --> `0` (no boxing)
- `foo.call("")` --> `""` (no boxing)
- `foo.call(null)` --> `null` (stays null)

---

### Question 3

```js
const module = {
  x: 42,
  getX: function () { return this.x; },
};

const unboundGetX = module.getX;
const boundGetX = unboundGetX.bind(module);

const newModule = { x: 99, getX: boundGetX };

console.log(newModule.getX()); // ?
```

**Answer:** `42`

Even though `boundGetX` is called as `newModule.getX()` (which would normally set `this` to `newModule`), the function was already bound to `module`. Bound functions ignore the calling context. So `this.x` is `module.x` which is `42`, not `99`.

---

## Exam Tips

- **Polyfills are the most likely exam question.** Memorize the `myCall`, `myApply`, and `myBind` implementations. Understand why `Symbol()` is used (to avoid property name collisions).
- **The trick in `myCall`/`myApply`:** We attach the function as a temporary method of `context`, then call it. Since JavaScript determines `this` by what is **left of the dot**, calling `context[sym]()` makes `this` equal to `context`.
- **`bind` returns a function.** If you write `fn.bind(obj)` and forget to call the result, nothing happens. This is a common trap in MCQs.
- **`bind` is permanent.** Once a function is bound, `call`/`apply` cannot change its `this`. A second `bind` also cannot change it.
- **Primitives as `thisArg`:** In non-strict mode, primitives (numbers, strings, booleans) passed as `thisArg` get **boxed** into their wrapper objects. In strict mode, they stay as-is.
- **`null`/`undefined` as `thisArg`:** In non-strict mode, this defaults to `globalThis`. In strict mode, it stays `null`/`undefined`.
- **Arrow functions ignore `call`/`apply`/`bind`.** Arrow functions have no own `this`; they inherit it lexically. You cannot override it.
- **Common wrong answer trap:** confusing `bind` (returns function) with `call`/`apply` (invoke immediately).

---

## Quick Revision Table

| Concept | Key Point |
|---|---|
| **`call(ctx, a, b)`** | Invoke now, args as comma list |
| **`apply(ctx, [a, b])`** | Invoke now, args as array |
| **`bind(ctx, a)`** | Returns new function, does NOT invoke |
| **call vs apply** | Only difference is args format: commas vs array |
| **bind + call/apply** | bind wins; call/apply cannot override bound `this` |
| **null/undefined context (non-strict)** | Defaults to `globalThis` |
| **null/undefined context (strict)** | Stays `null` / `undefined` |
| **Primitive context (non-strict)** | Gets boxed (Number, String, Boolean wrapper) |
| **Primitive context (strict)** | Stays as-is, no boxing |
| **Arrow functions** | Ignore call/apply/bind entirely; `this` is lexical |
| **Function borrowing** | `Array.prototype.slice.call(arrayLike)` |
| **Polyfill core trick** | Attach fn as temp method on context -> call -> delete |
| **Why Symbol() in polyfill** | Unique key prevents collision with existing properties |
| **myCall vs myApply** | Only difference: rest params `...args` vs single array param `args` |
| **myBind** | Returns closure that calls `fn.apply(context, [...preset, ...later])` |
| **Partial application** | `bind(ctx, arg1)` pre-fills arg1; rest supplied when calling |
| **Second bind** | Has NO effect; first bind's `this` wins |
