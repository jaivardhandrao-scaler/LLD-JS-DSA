# Closures, Scope & Currying (Class 7) -- Exam Study Guide

---

## Part A: Scope

---

### 1. Lexical Scoping

Scope is determined **where you write** the code, not where you call it.

```js
const greeting = "Hello";

function outer() {
  const name = "Alice";

  function inner() {
    // inner can see 'name' (outer) and 'greeting' (global)
    // because it was WRITTEN inside outer, which is inside global
    console.log(greeting + ", " + name);
  }

  return inner;
}

const fn = outer();
fn(); // "Hello, Alice"
// Even though fn() is called in global scope,
// it still accesses 'name' from outer -- that's lexical scoping.
```

**Key idea:** JavaScript looks at where the function was **defined** in the source code to decide what variables it can access. The call site does not matter.

---

### 2. Scope Chain

When JavaScript encounters a variable, it searches **outward** through nested scopes until it finds it (or hits global and throws a ReferenceError).

```js
const a = 1;

function first() {
  const b = 2;

  function second() {
    const c = 3;

    function third() {
      console.log(a, b, c); // 1 2 3 -- walks up the chain
    }

    third();
  }

  second();
}

first();
```

**The chain goes inward-to-outward only.** An outer function **cannot** reach into an inner function's variables:

```js
function outer() {
  function inner() {
    const secret = 42;
  }
  inner();
  console.log(secret); // ReferenceError: secret is not defined
}
```

---

### 3. var vs let vs const in Scope

| Feature | `var` | `let` | `const` |
|---|---|---|---|
| **Scope** | Function-scoped | Block-scoped | Block-scoped |
| **Hoisted?** | Yes (initialized to `undefined`) | Yes (but in TDZ until declaration) | Yes (but in TDZ until declaration) |
| **Re-assignable?** | Yes | Yes | No |
| **Re-declarable in same scope?** | Yes | No | No |

**TDZ = Temporal Dead Zone** -- the variable exists but you cannot access it before its declaration line.

```js
function example() {
  console.log(x); // undefined (var is hoisted)
  // console.log(y); // ReferenceError (let is in TDZ)
  // console.log(z); // ReferenceError (const is in TDZ)

  var x = 1;
  let y = 2;
  const z = 3;
}
```

**Block scope difference:**

```js
if (true) {
  var a = 10;
  let b = 20;
  const c = 30;
}

console.log(a); // 10 -- var leaks out of blocks
// console.log(b); // ReferenceError -- let is block-scoped
// console.log(c); // ReferenceError -- const is block-scoped
```

---

#### The Classic `var` + `for` Loop + `setTimeout` Bug

**BAD -- using var (the bug):**

```js
for (var i = 0; i < 3; i++) {
  setTimeout(function () {
    console.log(i);
  }, 1000);
}
// Output: 3, 3, 3
// NOT 0, 1, 2
```

**Why?** `var i` is function-scoped (or global-scoped here). There is only ONE `i`. By the time the setTimeout callbacks run, the loop has finished and `i` is 3. All three callbacks share that same `i`.

**GOOD -- Fix 1: Use `let`:**

```js
for (let i = 0; i < 3; i++) {
  setTimeout(function () {
    console.log(i);
  }, 1000);
}
// Output: 0, 1, 2
// let creates a new 'i' for each iteration (block-scoped)
```

**GOOD -- Fix 2: Use an IIFE:**

```js
for (var i = 0; i < 3; i++) {
  (function (j) {
    setTimeout(function () {
      console.log(j);
    }, 1000);
  })(i);
}
// Output: 0, 1, 2
// The IIFE captures the current value of i into parameter j
```

**GOOD -- Fix 3: Use a helper function:**

```js
function createCallback(val) {
  return function () {
    console.log(val);
  };
}

for (var i = 0; i < 3; i++) {
  setTimeout(createCallback(i), 1000);
}
// Output: 0, 1, 2
// Each call to createCallback captures the current i via the parameter 'val'
```

---

## Part B: Closures

---

### 4. What Is a Closure?

A **closure** is a function that **remembers and can access variables** from its outer (enclosing) function's scope, even after that outer function has finished executing.

**Start with a problem, then the definition clicks:**

```js
function makeGreeter(greeting) {
  // greeting lives here
  return function (name) {
    // This inner function is a CLOSURE.
    // It "closes over" the variable 'greeting'.
    return greeting + ", " + name + "!";
  };
}

const sayHello = makeGreeter("Hello");
const sayHi = makeGreeter("Hi");

// makeGreeter has already returned, but the inner function
// still has access to 'greeting':
console.log(sayHello("Alice")); // "Hello, Alice!"
console.log(sayHi("Bob"));     // "Hi, Bob!"
```

**Counter example (classic exam question):**

```js
function createCounter() {
  let count = 0; // private -- only accessible through the closure

  return function () {
    count++;
    return count;
  };
}

const counter = createCounter();

console.log(counter()); // 1
console.log(counter()); // 2
console.log(counter()); // 3

// count is NOT accessible from outside:
// console.log(count); // ReferenceError
```

Each call to `createCounter()` creates a **separate** closure with its own `count`:

```js
const counterA = createCounter();
const counterB = createCounter();

console.log(counterA()); // 1
console.log(counterA()); // 2
console.log(counterB()); // 1  <-- separate count
```

---

### 5. Closure Lifecycle

The outer function's variables stay alive in memory **as long as the inner function (the closure) holds a reference to them**. Once no code references the closure, the garbage collector can free those variables.

```js
function heavySetup() {
  const bigArray = new Array(1000000).fill("data");

  return function search(index) {
    return bigArray[index];
  };
}

let searcher = heavySetup();
// bigArray is alive because 'searcher' references the closure

searcher = null;
// Now nothing references the closure.
// bigArray can be garbage collected.
```

---

### 6. Data Encapsulation with Closures -- Private Variables

Closures let you create **truly private** state that cannot be accessed from outside.

```js
function createCounter(initial, step) {
  let count = initial;

  return {
    increment() {
      count += step;
    },
    decrement() {
      count -= step;
    },
    getCount() {
      return count;
    },
  };
}

const c = createCounter(10, 5);

c.increment();
console.log(c.getCount()); // 15

c.increment();
console.log(c.getCount()); // 20

c.decrement();
console.log(c.getCount()); // 15

// There is NO way to directly access or modify 'count':
console.log(c.count); // undefined
```

**Why this matters:** Unlike object properties, `count` here is **truly private**. No external code can read or write it except through the methods the closure exposes.

---

### 7. Private Bank Account Pattern

```js
function createBankAccount(ownerName, initialBalance) {
  let balance = initialBalance;
  const owner = ownerName;

  return {
    deposit(amount) {
      if (amount <= 0) {
        console.log("Deposit amount must be positive.");
        return;
      }
      balance += amount;
      console.log(`${owner} deposited ${amount}. Balance: ${balance}`);
    },

    withdraw(amount) {
      if (amount <= 0) {
        console.log("Withdrawal amount must be positive.");
        return;
      }
      if (amount > balance) {
        console.log("Insufficient funds.");
        return;
      }
      balance -= amount;
      console.log(`${owner} withdrew ${amount}. Balance: ${balance}`);
    },

    getBalance() {
      return balance;
    },

    getOwner() {
      return owner;
    },
  };
}

const account = createBankAccount("Alice", 1000);

account.deposit(500);       // Alice deposited 500. Balance: 1500
account.withdraw(200);      // Alice withdrew 200. Balance: 1300
console.log(account.getBalance()); // 1300

// Cannot cheat:
account.balance = 999999;          // This sets a NEW property on the object,
console.log(account.getBalance()); // 1300  <-- still uses the closure variable
```

---

### 8. Function Factory Pattern

A function that **creates and returns specialized functions** based on a parameter.

```js
function createMultiplier(factor) {
  return function (n) {
    return n * factor;
  };
}

const double = createMultiplier(2);
const triple = createMultiplier(3);
const toPercent = createMultiplier(100);

console.log(double(5));     // 10
console.log(triple(5));     // 15
console.log(toPercent(0.8)); // 80
```

Another common factory pattern -- **greeting factory:**

```js
function createGreeting(prefix) {
  return function (name) {
    return `${prefix}, ${name}!`;
  };
}

const formal = createGreeting("Good evening");
const casual = createGreeting("Hey");

console.log(formal("Dr. Smith")); // "Good evening, Dr. Smith!"
console.log(casual("Jake"));      // "Hey, Jake!"
```

---

### 9. Memoization with Closures

Memoization = caching the results of expensive function calls so repeated calls with the same arguments return instantly.

```js
function memoize(fn) {
  const cache = {}; // private cache inside the closure

  return function (...args) {
    const key = JSON.stringify(args);

    if (key in cache) {
      console.log("Cache hit for:", key);
      return cache[key];
    }

    console.log("Computing for:", key);
    const result = fn(...args);
    cache[key] = result;
    return result;
  };
}

// Usage:
function slowSquare(n) {
  // Imagine this is expensive
  return n * n;
}

const fastSquare = memoize(slowSquare);

console.log(fastSquare(4)); // Computing for: [4] --> 16
console.log(fastSquare(4)); // Cache hit for: [4] --> 16
console.log(fastSquare(5)); // Computing for: [5] --> 25
```

**How it works:**
- `cache` is enclosed in the closure -- persists across calls, private from outside
- Each call checks if the result for those arguments already exists
- If yes, return from cache; if no, compute, store, and return

---

### 10. Common Closure Pitfalls

#### Pitfall 1: `var` in a loop (all callbacks share the same variable)

```js
// BAD
function attachHandlers() {
  var buttons = ["Save", "Delete", "Cancel"];

  for (var i = 0; i < buttons.length; i++) {
    setTimeout(function () {
      console.log("Clicked: " + buttons[i]);
    }, 100);
  }
}

attachHandlers();
// Output (three times): "Clicked: undefined"
// Because i is 3 after the loop, and buttons[3] is undefined
```

**Fix with `let`:**

```js
for (let i = 0; i < buttons.length; i++) {
  setTimeout(function () {
    console.log("Clicked: " + buttons[i]);
  }, 100);
}
// Output: "Clicked: Save", "Clicked: Delete", "Clicked: Cancel"
```

**Fix with IIFE:**

```js
for (var i = 0; i < buttons.length; i++) {
  (function (index) {
    setTimeout(function () {
      console.log("Clicked: " + buttons[index]);
    }, 100);
  })(i);
}
```

**Fix with helper function:**

```js
function makeLogger(label) {
  return function () {
    console.log("Clicked: " + label);
  };
}

for (var i = 0; i < buttons.length; i++) {
  setTimeout(makeLogger(buttons[i]), 100);
}
```

#### Pitfall 2: Unintentional memory leaks

Closures keep their entire enclosing scope alive. If that scope contains large objects you no longer need, they cannot be garbage collected.

```js
// BAD -- large data stays in memory as long as callback exists
function setup() {
  const hugeData = new Array(10000000).fill("x");

  return function () {
    // Even if this function never uses hugeData,
    // some engines may keep hugeData alive because
    // it's in the closure's scope.
    console.log("done");
  };
}

const leak = setup(); // hugeData cannot be GC'd
```

**GOOD -- null out what you do not need:**

```js
function setup() {
  let hugeData = new Array(10000000).fill("x");
  const summary = hugeData.length; // extract what you need
  hugeData = null; // allow GC

  return function () {
    console.log("Items processed:", summary);
  };
}
```

---

## Part C: Currying & Partial Application

---

### 11. What Is Currying?

Currying transforms a function that takes multiple arguments into a chain of functions that each take a single argument.

```
f(a, b, c)  -->  f(a)(b)(c)
```

**Simple manual example:**

```js
// Non-curried:
function add(a, b) {
  return a + b;
}
console.log(add(2, 3)); // 5

// Curried:
function curriedAdd(a) {
  return function (b) {
    return a + b;
  };
}
console.log(curriedAdd(2)(3)); // 5

// You can also store intermediate functions:
const addFive = curriedAdd(5);
console.log(addFive(10)); // 15
console.log(addFive(20)); // 25
```

**Why curry?**
- Create specialized functions from general ones
- Avoid repeating arguments
- Compose functions more easily

---

### 12. Generic Curry Implementation

```js
function curry(fn) {
  return function curried(...args) {
    if (args.length >= fn.length) {
      return fn.apply(this, args);
    }
    return function (...moreArgs) {
      return curried.apply(this, [...args, ...moreArgs]);
    };
  };
}
```

**Line-by-line explanation:**

| Line | What it does |
|---|---|
| `function curry(fn)` | Takes any function `fn` that we want to curry. |
| `return function curried(...args)` | Returns a named function `curried` that collects arguments using rest syntax. Named so it can call itself recursively. |
| `if (args.length >= fn.length)` | `fn.length` is the number of parameters `fn` was defined with. If we have collected enough arguments, we can call the original function. |
| `return fn.apply(this, args)` | Call the original function with all collected arguments. `apply` preserves `this` context. |
| `return function(...moreArgs)` | If we do NOT have enough arguments yet, return a new function that waits for more. |
| `return curried.apply(this, [...args, ...moreArgs])` | When called, merge the previously collected `args` with the new `moreArgs` and feed them back into `curried` to check again. |

**Usage:**

```js
function volume(l, w, h) {
  return l * w * h;
}

const curriedVolume = curry(volume);

// All of these work:
console.log(curriedVolume(2)(3)(4));    // 24
console.log(curriedVolume(2, 3)(4));    // 24
console.log(curriedVolume(2)(3, 4));    // 24
console.log(curriedVolume(2, 3, 4));    // 24

// Store intermediate results:
const boxWithWidth2 = curriedVolume(2);
const boxWithWidth2Height3 = boxWithWidth2(3);
console.log(boxWithWidth2Height3(4)); // 24
console.log(boxWithWidth2Height3(5)); // 30
```

---

### 13. Infinite Currying Sum

A pattern where you keep calling with arguments until you call with **no arguments** to get the result.

```js
function sum(...args) {
  const total = args.reduce((a, b) => a + b, 0);

  function inner(...moreArgs) {
    if (moreArgs.length === 0) return total;
    return sum(total, ...moreArgs);
  }

  return inner;
}

console.log(sum(1)(2)(3)());          // 6
console.log(sum(1, 2)(3, 4)(5)());    // 15
console.log(sum(10)());               // 10
console.log(sum(1)(2)(3)(4)(5)());    // 15
```

**How it works:**
- `sum(...args)` computes a running total from all arguments passed so far
- It returns `inner`, which is a function waiting for more arguments
- If `inner` is called with **no arguments** (`moreArgs.length === 0`), it returns the total
- If called with arguments, it recursively calls `sum` with the running total plus the new arguments
- This creates an infinite chain: `sum(1)(2)(3)...(n)()`

**Alternative version using `toString` / `valueOf` (no empty-call terminator):**

```js
function sum(...args) {
  const total = args.reduce((a, b) => a + b, 0);

  function inner(...moreArgs) {
    return sum(total, ...moreArgs);
  }

  inner.valueOf = function () {
    return total;
  };

  inner.toString = function () {
    return String(total);
  };

  return inner;
}

// These work when JS coerces to a primitive:
console.log(+sum(1)(2)(3));     // 6  (unary + triggers valueOf)
console.log(`${sum(1)(2)(3)}`); // "6" (template literal triggers toString)
```

---

### 14. Partial Application with `bind`

**Partial application** = fix some arguments of a function, producing a new function that takes the remaining ones. `bind` is the simplest way to do this in JavaScript.

```js
function greet(greeting, punctuation, name) {
  return `${greeting}, ${name}${punctuation}`;
}

// Partially apply the first two arguments:
const casualGreet = greet.bind(null, "Hey", "!");
// null = we don't care about 'this'

console.log(casualGreet("Alice")); // "Hey, Alice!"
console.log(casualGreet("Bob"));   // "Hey, Bob!"

const formalGreet = greet.bind(null, "Good evening", ".");
console.log(formalGreet("Dr. Smith")); // "Good evening, Dr. Smith."
```

**Currying vs Partial Application:**

| | Currying | Partial Application |
|---|---|---|
| **What it does** | Transforms `f(a,b,c)` into `f(a)(b)(c)` | Fixes some args, returns function for the rest |
| **Arguments** | Always one at a time (pure currying) | Can fix any number at once |
| **Implementation** | Custom `curry()` function | `bind()`, closures, or libraries |
| **Returns** | Chain of single-arg functions | One function taking remaining args |

**Partial application using a closure (manual):**

```js
function partial(fn, ...fixedArgs) {
  return function (...remainingArgs) {
    return fn(...fixedArgs, ...remainingArgs);
  };
}

function add(a, b, c) {
  return a + b + c;
}

const add10 = partial(add, 10);
console.log(add10(20, 30)); // 60

const add10and20 = partial(add, 10, 20);
console.log(add10and20(30)); // 60
```

---

## Predict the Output Questions

---

### Question 1

```js
function outer() {
  let x = 10;

  function inner() {
    console.log(x);
  }

  x = 20;
  return inner;
}

const fn = outer();
fn();
```

<details>
<summary><b>Answer</b></summary>

**Output:** `20`

**Why:** The closure captures the **variable** `x`, not its value at the time `inner` was created. By the time `inner` is called, `x` has been reassigned to 20.

</details>

---

### Question 2

```js
function createFunctions() {
  var result = [];

  for (var i = 0; i < 3; i++) {
    result.push(function () {
      return i;
    });
  }

  return result;
}

const fns = createFunctions();
console.log(fns[0]());
console.log(fns[1]());
console.log(fns[2]());
```

<details>
<summary><b>Answer</b></summary>

**Output:**
```
3
3
3
```

**Why:** `var i` is function-scoped. All three closures share the same `i`. After the loop, `i` is 3, so every function returns 3.

**Fix:** Change `var` to `let` to get `0, 1, 2`.

</details>

---

### Question 3

```js
function makeAdder(x) {
  return function (y) {
    return x + y;
  };
}

const add5 = makeAdder(5);
const add10 = makeAdder(10);

console.log(add5(3));
console.log(add10(3));
console.log(add5(add10(1)));
```

<details>
<summary><b>Answer</b></summary>

**Output:**
```
8
13
16
```

**Why:**
- `add5(3)` = 5 + 3 = 8
- `add10(3)` = 10 + 3 = 13
- `add10(1)` = 11, then `add5(11)` = 5 + 11 = 16

Each call to `makeAdder` creates a separate closure with its own `x`.

</details>

---

### Question 4

```js
let count = 0;

function increment() {
  count++;
}

function getCount() {
  return count;
}

increment();
increment();
increment();

let count2 = count;

increment();

console.log(getCount());
console.log(count2);
```

<details>
<summary><b>Answer</b></summary>

**Output:**
```
4
3
```

**Why:**
- After three `increment()` calls, `count` is 3.
- `count2 = count` copies the **value** 3 into `count2` (primitives are copied by value).
- After the fourth `increment()`, `count` is 4 but `count2` is still 3.
- `getCount()` returns the current value of `count`, which is 4.

</details>

---

## Exam Tips

- **Closures capture variables, not values.** If the variable changes after the closure is created, the closure sees the updated value. This is the key to almost every "predict the output" trap.
- **`var` is function-scoped, `let`/`const` are block-scoped.** Whenever you see `var` inside a `for` loop with asynchronous callbacks (`setTimeout`, event listeners), expect the "all same value" bug.
- **Three fixes for the var-loop bug:** (1) use `let`, (2) wrap in an IIFE, (3) use a helper function. Know all three -- exams love asking for "fix this code."
- **Private variables via closures** are a classic exam pattern. The returned object's methods can access the outer function's variables, but nothing else can.
- **Each call to an outer function creates a new closure.** `createCounter()` called twice produces two independent counters with separate `count` variables.
- **`fn.length`** gives the number of declared parameters. This is how the generic `curry` function knows when it has enough arguments.
- **Currying vs partial application:** currying always breaks into single-argument functions; partial application fixes some arguments and returns one function for the rest. `bind` does partial application, not currying.
- **Infinite currying** uses an empty call `()` as the terminator signal. Watch for `moreArgs.length === 0` as the base case.
- **Memoization** stores results in a cache object inside the closure. The cache persists across calls because the closure keeps it alive.
- **Memory leaks:** closures keep their enclosing scope alive. If a closure captures a large object it does not need, that object cannot be garbage collected. Null out large references when you are done with them.

---

## Quick Revision Table

| Concept | One-Line Summary | Key Code Pattern |
|---|---|---|
| **Lexical Scope** | Scope decided by where code is written, not where it runs | Inner function reads outer variable |
| **Scope Chain** | Variable lookup goes outward through nesting levels | `third()` can access vars from `first()` |
| **var** | Function-scoped, hoisted as `undefined` | `var x` inside `if` leaks out of block |
| **let / const** | Block-scoped, TDZ before declaration | `let x` inside `if` stays inside block |
| **var + loop bug** | All async callbacks share one `var` variable | `for(var i...)` + `setTimeout` prints same value |
| **Loop fix (3 ways)** | Use `let`, IIFE, or helper function | `for(let i...)` / `(function(j){...})(i)` / `makeCallback(i)` |
| **Closure** | Function remembers outer scope after outer returns | `return function() { return outerVar; }` |
| **Closure lifecycle** | Outer vars alive while inner function exists | Set closure to `null` to allow GC |
| **Private variables** | Closure hides state, exposes methods only | `return { getCount() { return count; } }` |
| **Bank account** | Closure protects balance from direct access | `deposit()`, `withdraw()`, `getBalance()` via closure |
| **Function factory** | Outer function returns specialized inner function | `createMultiplier(2)` returns `(n) => n * 2` |
| **Memoization** | Cache results in a closure-held object | `const cache = {}` inside outer, check before computing |
| **Currying** | `f(a,b,c)` becomes `f(a)(b)(c)` | `curry(fn)` checks `args.length >= fn.length` |
| **fn.length** | Number of declared parameters of a function | `function f(a,b,c){}` has `f.length === 3` |
| **Infinite sum** | Chain calls, empty call returns total | `sum(1)(2)(3)()` returns 6 |
| **Partial application** | Fix some args now, pass rest later | `fn.bind(null, fixedArg1, fixedArg2)` |
| **Currying vs Partial** | Currying = one arg at a time; Partial = fix N args at once | `curry` vs `bind` |

---

*Good luck on the exam tomorrow.*

---

## Practice These

After studying this topic, solve these coding problems:

**From Contest_Practice-1.md:**
- Q13: Closure counter
- Q14: Memoized add with cache
- Q15: Private bank account
- Q16: Function factory -- multiplier

**From practiceSheet.md:**
- P21: once(fn)
- P23: Private variables using closures
- P24: Rate limiter using closure
- P25: Fix var + setTimeout bug
- P26: Implement currying
- P27: Infinite currying sum
- P28: Function logger wrapper
