# Memory Management & Garbage Collection (Class 13)

---

## Start Here: What Goes Wrong?

Look at this code and figure out the problem **before** reading the explanation:

```js
function createButtons() {
  const bigData = new Array(1000000).fill("x");

  for (let i = 0; i < 5; i++) {
    const btn = document.createElement("button");
    btn.textContent = `Button ${i}`;
    btn.addEventListener("click", () => {
      console.log(bigData.length); // closure holds reference to bigData
    });
    document.body.appendChild(btn);
  }
}
createButtons();
// bigData (millions of entries) is NEVER freed from memory
// because every button's click handler still references it via closure
```

**The problem:** `bigData` stays in memory forever because 5 closures reference it. This is a **memory leak** -- memory that is allocated, never used again, but never released.

This entire topic is about understanding **why** that happens and **how** to prevent it.

---

## 1. How JavaScript Manages Memory

Every value you create occupies memory. JS handles memory in **three phases**:

| Phase | What Happens | Who Does It |
|-------|-------------|-------------|
| **Allocate** | Memory reserved when you create variables, objects, arrays, functions | JS engine (automatic) |
| **Use** | Reading/writing to that memory | Your code |
| **Release** | Memory freed when no longer needed | Garbage Collector (automatic) |

```js
// ALLOCATE — engine reserves memory for each
let name = "Alice";                    // string on heap
let scores = [90, 85, 92];            // array on heap
let student = { name: "Bob", gpa: 3.8 }; // object on heap

// USE — your code reads/writes
console.log(student.name);
scores.push(88);

// RELEASE — happens automatically when nothing references the value
name = null;    // old string "Alice" is now unreachable -> eligible for GC
scores = null;  // array [90, 85, 92, 88] is now unreachable -> eligible for GC
student = null; // object is now unreachable -> eligible for GC
```

**Key point:** You never call `free()` or `delete` to release memory. The **Garbage Collector (GC)** does it for you. But it can only free memory that is truly **unreachable**.

---

## 2. Garbage Collection: Mark-and-Sweep

### The Algorithm JS Uses

Modern JS engines (V8, SpiderMonkey) use **mark-and-sweep**:

1. **Start from roots** -- global object (`window`/`globalThis`), currently executing function scopes, the call stack
2. **Mark** -- traverse all references from roots; everything reachable gets "marked" as alive
3. **Sweep** -- anything NOT marked is garbage; free its memory

```
ROOTS (global, stack, closures)
  |
  +--> obj A (MARKED - reachable)
  |      |
  |      +--> obj B (MARKED - reachable through A)
  |
  +--> obj C (MARKED - reachable)

  obj D (NOT MARKED - nothing points to it) --> GARBAGE COLLECTED
  obj E --> obj F (neither marked, both collected even though E references F)
```

### When Does GC Run?

- **You do not control it.** There is no `gc()` you can call.
- The engine decides based on memory pressure, heuristics, idle time.
- It runs automatically in the background.

### What Makes Something "Garbage"?

An object is garbage when **no reference chain** connects it to any root.

```js
let a = { x: 1 };
let b = a;       // two references to the same object
a = null;        // object still reachable through b
b = null;        // NOW the object is unreachable -> garbage collected
```

```js
// Circular references are STILL collected by mark-and-sweep
let objA = {};
let objB = {};
objA.ref = objB;
objB.ref = objA;
objA = null;
objB = null;
// Both objects reference each other, but neither is reachable from any root
// Mark-and-sweep handles this correctly -> both are collected
```

**Exam Tip:** Older reference-counting GC algorithms could NOT handle circular references. Mark-and-sweep can, and that is what modern JS uses.

---

## 3. Common Memory Leaks (BAD vs GOOD)

A **memory leak** occurs when memory that is no longer needed remains reachable, so GC cannot free it.

---

### a. Accidental Globals

```js
// BAD -- creates a global variable (attached to window)
function leak() {
  data = "huge string that never goes away";  // missing var/let/const!
}
leak();
// window.data now exists and persists for the lifetime of the page
```

```js
// GOOD -- always declare variables; use strict mode to catch mistakes
"use strict";
function noLeak() {
  const data = "huge string";
  // data is local, freed when function returns
}
noLeak();
```

**Why it leaks:** Global variables are roots. The GC will **never** collect them because they are always reachable from `window`/`globalThis`.

**Exam Tip:** `"use strict"` turns accidental global assignment into a `ReferenceError`, catching the bug immediately.

---

### b. Forgotten Timers

```js
// BAD -- interval runs forever, keeps largeObj alive
const largeObj = { data: new Array(1000000) };

setInterval(() => {
  console.log(largeObj.data.length);
}, 1000);
// largeObj can NEVER be garbage collected because the interval
// callback holds a reference, and the interval never stops
```

```js
// GOOD -- clear the interval when done
const largeObj = { data: new Array(1000000) };

const id = setInterval(() => {
  console.log(largeObj.data.length);
}, 1000);

// When you no longer need it:
clearInterval(id);
// Now the callback is removed, and if nothing else references largeObj,
// it becomes eligible for GC
```

**Why it leaks:** `setInterval` registers a callback with the browser runtime. That callback is a **root**. Anything the callback closes over stays alive.

---

### c. Closures Holding References

```js
// BAD -- inner function holds reference to hugeData even though it never uses it
function outer() {
  const hugeData = new Array(1000000);
  return function inner() {
    console.log("I exist");
    // hugeData is still in scope and in memory because of the closure!
  };
}
const fn = outer();
// fn keeps inner alive, inner's closure keeps hugeData alive
// 1 million elements stuck in memory for nothing
```

```js
// GOOD -- null out what you don't need before returning the closure
function outer() {
  let hugeData = new Array(1000000);
  const result = hugeData.reduce((sum, val) => sum + val, 0);
  hugeData = null;  // break the reference, allow GC to collect the array
  return function inner() {
    console.log(result); // only holds the small computed result
  };
}
const fn = outer();
// hugeData is collected; only result (a single number) remains
```

**Why it leaks:** A closure captures the **entire scope** of the outer function. Even variables the inner function does not explicitly use may be retained (depending on engine optimization). Safest practice: null out large data you no longer need.

---

### d. Detached DOM References

```js
// BAD -- element removed from DOM but JS variable still holds a reference
const btn = document.getElementById("myBtn");
document.body.removeChild(btn);
// btn is gone from the page, but the JS variable still points to the node
// The DOM node stays in memory -- this is a "detached DOM node"
```

```js
// GOOD -- remove the JS reference too
let btn = document.getElementById("myBtn");
document.body.removeChild(btn);
btn = null;  // now the DOM node can be garbage collected
```

**Why it leaks:** Removing a node from the DOM does **not** remove it from memory. If any JS variable still references it, GC considers it reachable.

---

### e. Event Listeners Not Removed

```js
// BAD -- listeners pile up, each holding references to outer scope
function setup() {
  const heavyData = new Array(1000000);

  window.addEventListener("resize", function handler() {
    console.log(heavyData.length);
  });
  // If setup() is called multiple times, each call adds a NEW listener
  // Each listener holds its own copy of heavyData in its closure
}
setup();
setup(); // two listeners, two arrays of 1 million elements
setup(); // three listeners, three arrays
```

```js
// GOOD -- store reference, remove when no longer needed
function setup() {
  const heavyData = new Array(1000000);

  function handler() {
    console.log(heavyData.length);
  }
  window.addEventListener("resize", handler);

  // Return a cleanup function
  return function cleanup() {
    window.removeEventListener("resize", handler);
    // Now handler is eligible for GC, and so is heavyData
  };
}

const cleanup = setup();
// Later, when you're done:
cleanup();
```

**Why it leaks:** Event listeners are roots (registered with the browser). Their closures keep captured variables alive. Failing to remove them means the memory never gets freed.

---

## 4. WeakMap

A `WeakMap` holds **weak references** to its keys. If nothing else references a key, the key (and its associated value) can be garbage collected.

```js
const cache = new WeakMap();

let user = { name: "Alice" };
cache.set(user, { lastLogin: Date.now() });

console.log(cache.get(user)); // { lastLogin: 1712567... }

user = null;
// No strong reference to the object remains
// The WeakMap entry { name: "Alice" } -> { lastLogin: ... } is automatically removed
// Both key and value become eligible for GC
```

### WeakMap Rules

- **Keys must be objects** (not strings, not numbers)
- **Not iterable** -- no `for...of`, no `.forEach()`, no `.keys()`
- **No `.size` property** -- you cannot check how many entries exist
- **Methods available:** `.get(key)`, `.set(key, value)`, `.has(key)`, `.delete(key)`

```js
const wm = new WeakMap();

wm.set("hello", 1);      // TypeError! Keys must be objects
wm.set(42, "value");      // TypeError! Keys must be objects
wm.set({ id: 1 }, "ok");  // Works
wm.set([1, 2, 3], "ok");  // Works (arrays are objects)

console.log(wm.size);     // undefined (not available)
```

**Why is it not iterable?** Because entries can disappear at any time when GC runs. Allowing iteration would produce unpredictable results.

---

## 5. WeakSet

Same concept as `WeakMap`, but stores **values** instead of key-value pairs.

```js
const visited = new WeakSet();

let page1 = { url: "/home" };
let page2 = { url: "/about" };

visited.add(page1);
visited.add(page2);

console.log(visited.has(page1)); // true
console.log(visited.has(page2)); // true

page1 = null;
// { url: "/home" } is now eligible for GC
// visited automatically loses that entry
```

### WeakSet Rules

- **Values must be objects**
- **Not iterable**, no `.size`
- **Methods:** `.add(value)`, `.has(value)`, `.delete(value)`

**Use case:** Tracking which objects have been "seen" or "processed" without preventing their cleanup.

```js
// Use case: prevent processing the same object twice
const processed = new WeakSet();

function processOrder(order) {
  if (processed.has(order)) {
    console.log("Already processed");
    return;
  }
  // ... do work ...
  processed.add(order);
}
```

---

## 6. WeakRef (Brief)

`WeakRef` lets you hold a **weak reference** to a single object. The object can still be garbage collected.

```js
let bigObject = { data: new Array(1000000) };
const ref = new WeakRef(bigObject);

console.log(ref.deref());       // { data: [...] } -- object still alive
console.log(ref.deref().data.length); // 1000000

bigObject = null;
// At some point after GC runs:
console.log(ref.deref());       // undefined -- object was collected
```

- **`.deref()`** returns the object if it is still alive, or `undefined` if GC has collected it.
- **Always check** the return value of `.deref()` before using it.
- Use sparingly. Most code should use `WeakMap` or `WeakSet` instead.

---

## 7. Map vs WeakMap Comparison

| Feature | Map | WeakMap |
|---------|-----|---------|
| **Key types** | Any (string, number, object, etc.) | Objects only |
| **Iterable** | Yes (`for...of`, `.forEach()`, `.keys()`, `.values()`, `.entries()`) | No |
| **`.size` property** | Yes | No |
| **Keys garbage collected** | No -- Map holds strong references | Yes -- keys are weakly held |
| **Use case** | General-purpose key-value storage | Caching, metadata, private data |
| **Memory behavior** | Keys stay in memory as long as Map exists | Keys can be collected when unreferenced elsewhere |

```js
// Map -- keeps the object alive
const map = new Map();
let obj = { id: 1 };
map.set(obj, "data");
obj = null;
// The object { id: 1 } is still in the Map, NOT collected

// WeakMap -- lets the object go
const weakMap = new WeakMap();
let obj2 = { id: 2 };
weakMap.set(obj2, "data");
obj2 = null;
// The object { id: 2 } CAN be garbage collected, entry disappears
```

**Set vs WeakSet follows the same pattern:** Set holds strong references, WeakSet holds weak references.

---

## 8. Practical Patterns

### Pattern A: Private Data with WeakMap

```js
const _privateData = new WeakMap();

class User {
  constructor(name, password) {
    this.name = name;
    _privateData.set(this, { password }); // private, tied to instance
  }

  checkPassword(input) {
    return _privateData.get(this).password === input;
  }
}

const u = new User("Alice", "secret123");
console.log(u.name);               // "Alice"
console.log(u.password);           // undefined -- not a public property
console.log(u.checkPassword("secret123")); // true

// When `u` is garbage collected, the WeakMap entry is automatically cleaned up
```

**Why WeakMap here?** If you used a regular `Map`, the entries would prevent instances from being garbage collected even after all other references are gone.

---

### Pattern B: Caching Expensive Computations

```js
const computeCache = new WeakMap();

function expensiveCompute(obj) {
  if (computeCache.has(obj)) {
    return computeCache.get(obj); // return cached result
  }
  // Simulate expensive work
  const result = Object.keys(obj).length * 42;
  computeCache.set(obj, result);
  return result;
}

let data = { a: 1, b: 2, c: 3 };
console.log(expensiveCompute(data)); // computes: 126
console.log(expensiveCompute(data)); // cached: 126

data = null;
// Cache entry is automatically cleaned up by GC -- no manual cleanup needed
```

---

### Pattern C: Cleanup Patterns for Timers and Listeners

```js
class Poller {
  constructor(url, intervalMs) {
    this.url = url;
    this.timerId = setInterval(() => this.poll(), intervalMs);
  }

  poll() {
    console.log(`Polling ${this.url}...`);
  }

  destroy() {
    clearInterval(this.timerId); // stop the timer
    this.timerId = null;         // clear the reference
    // Now the Poller instance can be garbage collected
  }
}

const poller = new Poller("/api/status", 5000);
// Later:
poller.destroy(); // always clean up
```

```js
// AbortController pattern for event listeners (modern approach)
class Widget {
  constructor(el) {
    this.el = el;
    this.controller = new AbortController();

    this.el.addEventListener("click", () => this.onClick(), {
      signal: this.controller.signal,
    });
    window.addEventListener("resize", () => this.onResize(), {
      signal: this.controller.signal,
    });
  }

  onClick() { console.log("clicked"); }
  onResize() { console.log("resized"); }

  destroy() {
    this.controller.abort(); // removes ALL listeners at once
    this.el = null;
  }
}
```

---

## Exam-Style Questions

### Question 1

**What is the output, and is there a memory issue?**

```js
function createFunctions() {
  const bigArray = new Array(1000000).fill(0);
  const funcs = [];

  for (let i = 0; i < 3; i++) {
    funcs.push(function () {
      return bigArray.length;
    });
  }

  return funcs;
}

const fns = createFunctions();
console.log(fns[0]());
```

**Answer:**
- Output: `1000000`
- **Yes, there is a memory issue.** All three functions in `funcs` close over `bigArray`. As long as `fns` holds the array of functions, `bigArray` (1 million elements) stays in memory. Even calling just `fns[0]()` keeps the entire array alive because the closure captures it.
- **Fix:** Compute what you need from `bigArray` before creating the closures, null out `bigArray`, and return closures that use only the computed value.

---

### Question 2

**What happens to the WeakMap entry after the last line?**

```js
const metadata = new WeakMap();
let element = document.createElement("div");
metadata.set(element, { clicks: 0, created: Date.now() });

console.log(metadata.has(element)); // ?

element = null;

console.log(metadata.has(element)); // ?
```

**Answer:**
- First `console.log`: `true` -- the element is still referenced by `element`, so the WeakMap entry exists.
- Second `console.log`: `false` -- after `element = null`, we are calling `metadata.has(null)`, which returns `false`. The WeakMap entry for the original DOM element is now eligible for garbage collection since no strong reference to that object remains.

---

### Question 3

**Identify ALL memory leaks in this code:**

```js
let buttons = [];

function addButton(label) {
  cache = [];  // line A

  const btn = document.createElement("button");
  btn.textContent = label;
  document.body.appendChild(btn);

  btn.addEventListener("click", function handler() {
    cache.push(label);
    console.log(cache.length);
  });

  buttons.push(btn);
}

function removeAllButtons() {
  buttons.forEach((btn) => document.body.removeChild(btn));
  buttons = [];
}
```

**Answer -- three leaks:**

1. **Accidental global (line A):** `cache = []` has no `let`/`const`/`var`. It creates a global variable `window.cache` that persists forever.

2. **Event listeners not removed:** `removeAllButtons` removes buttons from the DOM and clears the `buttons` array, but **never calls `removeEventListener`**. The click handlers still exist, holding references to `cache` and `label` via closure.

3. **Detached DOM nodes (partial):** After `removeAllButtons`, the button elements are removed from the DOM and removed from the `buttons` array. However, because the event listeners (registered with the browser) still reference each button's handler closure, the DOM nodes may not be collectible until the listeners are also removed.

---

## Exam Tips

- **Mark-and-sweep** is the GC algorithm to name on the exam. It starts from roots, marks everything reachable, and sweeps (frees) everything else.
- **Circular references** are NOT a problem for mark-and-sweep. They were only a problem for the older reference-counting approach.
- The **five common memory leaks** to memorize: accidental globals, forgotten timers, closures holding references, detached DOM nodes, event listeners not removed.
- **WeakMap keys must be objects.** If an exam question asks whether you can use a string as a WeakMap key, the answer is **no** (TypeError).
- **WeakMap and WeakSet are not iterable** -- no `.size`, no `for...of`, no `.forEach()`. This is because entries can vanish unpredictably when GC runs.
- **"Weak"** in WeakMap/WeakSet/WeakRef means: the structure does NOT prevent its entries from being garbage collected.
- **You cannot force GC to run.** There is no standard `gc()` call. The engine decides when.
- The **fix** for most leaks is the same idea: **remove the reference** (`= null`, `clearInterval`, `removeEventListener`).

---

## Quick Revision Table

| Concept | Key Point |
|---------|-----------|
| Memory lifecycle | Allocate -> Use -> Release (release is automatic via GC) |
| Mark-and-sweep | Start from roots, mark reachable, sweep unreachable |
| Roots | Global object, call stack, active closures |
| Circular references | Handled correctly by mark-and-sweep |
| Accidental globals | Missing `let`/`const`/`var` creates global; use `"use strict"` |
| Forgotten timers | `setInterval` callback is a root; always `clearInterval` |
| Closure leaks | Closures capture outer scope; null out large data you do not need |
| Detached DOM nodes | Removing from DOM is not enough; null the JS variable too |
| Event listener leaks | Always `removeEventListener` or use `AbortController` |
| WeakMap | Weak keys (objects only), not iterable, no `.size`, entries auto-removed by GC |
| WeakSet | Weak values (objects only), not iterable, no `.size`, entries auto-removed by GC |
| WeakRef | `.deref()` returns object or `undefined`; always check before use |
| Map vs WeakMap | Map holds strong refs (prevents GC); WeakMap holds weak refs (allows GC) |
| WeakMap use cases | Private class data, caching metadata, associating data with DOM nodes |
| Forcing GC | You cannot; the engine decides when to run it |
