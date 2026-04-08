# Memoization, Caching & Optimization + JS Weird Parts (Class 15)

**Exam tomorrow. Every section here is fair game. Read the code, understand the pattern, then test yourself with the problems at the end.**

---

## Part A: Memoization

### 1. The Problem -- Expensive Repeated Calls

**BAD -- no caching, recomputes every time:**

```js
function slowSquare(n) {
  console.log("Computing...");
  // Imagine this takes 3 seconds
  let result = 0;
  for (let i = 0; i < 1000000000; i++) {
    result = n * n;
  }
  return result;
}

slowSquare(5); // Computing... (3 sec)
slowSquare(5); // Computing... (3 sec again -- WASTED)
slowSquare(5); // Computing... (3 sec again -- WASTED)
```

**GOOD -- cache the result, return instantly on repeat calls:**

```js
function slowSquare(n) {
  console.log("Computing...");
  let result = 0;
  for (let i = 0; i < 1000000000; i++) {
    result = n * n;
  }
  return result;
}

const cache = {};

function memoizedSquare(n) {
  if (n in cache) {
    console.log("From cache!");
    return cache[n];
  }
  const result = slowSquare(n);
  cache[n] = result;
  return result;
}

memoizedSquare(5); // Computing... (3 sec)
memoizedSquare(5); // From cache! (instant)
memoizedSquare(5); // From cache! (instant)
```

**Core idea**: Store the result of a function call indexed by its arguments. If the same arguments appear again, return the stored result instead of recomputing.

---

### 2. Generic memoize() Implementation

The above example hardcodes caching for one function. A **generic memoize** wraps ANY function:

```js
function memoize(fn) {
  const cache = {};                         // 1. Private cache object (closure)
  return function(...args) {                // 2. Return a new function (wrapper)
    const key = JSON.stringify(args);       // 3. Convert args array to a string key
    if (key in cache) return cache[key];    // 4. Cache hit -- return stored result
    const result = fn.apply(this, args);    // 5. Cache miss -- call original function
    cache[key] = result;                    // 6. Store result in cache
    return result;                          // 7. Return result
  };
}
```

**Line-by-line breakdown:**

| Line | What it does |
|------|-------------|
| `const cache = {}` | Creates a private cache via **closure** -- persists across calls but invisible outside |
| `return function(...args)` | Returns a wrapper function. `...args` collects all arguments into an array |
| `JSON.stringify(args)` | Converts `[5, 10]` to the string `"[5,10]"` -- objects need string keys |
| `key in cache` | Checks if we have seen these exact arguments before |
| `fn.apply(this, args)` | Calls the original function, preserving `this` context and passing args |
| `cache[key] = result` | Stores the computed result for future lookups |

**Usage:**

```js
const fastSquare = memoize(function(n) {
  console.log("Computing...");
  return n * n;
});

fastSquare(5);  // Computing... => 25
fastSquare(5);  // => 25 (from cache, no "Computing..." logged)
fastSquare(10); // Computing... => 100
```

**Keying strategy -- JSON.stringify limitations:**

| Issue | Example | Problem |
|-------|---------|---------|
| **Object key order** | `JSON.stringify({a:1, b:2})` vs `JSON.stringify({b:2, a:1})` | Different strings, same logical object (in modern JS engines order is preserved, but semantically they are equal) |
| **Functions as args** | `JSON.stringify([() => {}])` | Functions serialize to `null` -- different functions produce the same key |
| **undefined** | `JSON.stringify([undefined])` | Becomes `"[null]"` -- collides with actual `null` |
| **Circular refs** | `JSON.stringify(circularObj)` | Throws an error |

---

### 3. Memoize with Map (Better)

`Map` can use **any value** as a key (not just strings). For single-argument functions with primitive args, skip serialization entirely:

```js
function memoize(fn) {
  const cache = new Map();
  return function(...args) {
    // Optimization: single primitive arg can be the key directly
    const key = args.length === 1 ? args[0] : JSON.stringify(args);
    if (cache.has(key)) return cache.get(key);
    const result = fn.apply(this, args);
    cache.set(key, result);
    return result;
  };
}
```

**Why Map over plain object?**

| Feature | `{}` (Object) | `Map` |
|---------|--------------|-------|
| Key types | Strings/Symbols only | **Any type** (numbers, objects, etc.) |
| Key `0` vs `"0"` | Same key (coerced) | **Different keys** |
| Size | `Object.keys(obj).length` | `map.size` |
| Iteration order | Insertion order (mostly) | **Guaranteed** insertion order |
| Performance | Good | **Better for frequent add/delete** |

---

### 4. Referential Equality -- Why It Matters for Caching

```js
const obj1 = { a: 1 };
const obj2 = { a: 1 };

console.log(obj1 === obj2); // false -- different references in memory!
console.log(obj1 === obj1); // true  -- same reference
```

**This means:**

```js
const memoized = memoize(someFunction);

memoized({ a: 1 }); // Cache MISS -- stores result
memoized({ a: 1 }); // With JSON.stringify key: Cache HIT (same string)
                     // With object reference key: Cache MISS (different object!)
```

**Bottom line**: JSON.stringify solves the equality problem for plain data but breaks for functions, circular objects, and `undefined`. There is no perfect universal solution -- choose the keying strategy based on your use case.

---

### 5. When TO Memoize

- **Pure functions** -- same input always gives same output, no side effects
- **Expensive computations** -- heavy math, parsing, sorting large data
- **Recursive functions** -- fibonacci, tree traversals (dramatic speedup)
- **API call deduplication** -- same request with same params

### 6. When NOT to Memoize

- **Side effects** -- function writes to DB, modifies DOM, logs analytics
- **Non-deterministic** -- `Math.random()`, `Date.now()`, network calls
- **Cheap computations** -- cache overhead exceeds computation cost
- **Unique inputs** -- if every call has different args, cache just wastes memory

---

### 7. Memoized Fibonacci (Classic Exam Question)

**BAD -- naive recursion, O(2^n) time:**

```js
function fib(n) {
  if (n <= 1) return n;
  return fib(n - 1) + fib(n - 2);
}

fib(40); // Takes several seconds
fib(50); // Takes MINUTES -- exponential blowup
```

Why it is slow: `fib(5)` computes `fib(3)` twice, `fib(2)` three times, etc. The call tree explodes.

**GOOD -- memoized, O(n) time:**

```js
const fib = memoize(function(n) {
  if (n <= 1) return n;
  return fib(n - 1) + fib(n - 2);
});

fib(40);  // Instant
fib(100); // Instant -- each value computed only once
```

**How it works**: `fib(5)` calls `fib(4)` and `fib(3)`. When `fib(4)` internally computes `fib(3)`, that result is already cached. Every value from `fib(0)` to `fib(n)` is computed exactly once.

**Manual memoization (no generic wrapper):**

```js
function fib(n, memo = {}) {
  if (n in memo) return memo[n];
  if (n <= 1) return n;
  memo[n] = fib(n - 1, memo) + fib(n - 2, memo);
  return memo[n];
}
```

---

## Part B: once(fn)

### 8. The once() Function

**Problem**: You have a function that should only execute **one time**, ever. Subsequent calls return the first result.

**Use cases**: Initialize a DB connection, set up event listeners, load config.

```js
function once(fn) {
  let called = false;         // Flag: has the function been called?
  let result;                 // Store the first call's return value
  return function(...args) {
    if (called) return result; // Already called -- return cached result
    called = true;             // Mark as called
    result = fn.apply(this, args); // Execute and store result
    return result;
  };
}
```

**Usage:**

```js
const initialize = once(function() {
  console.log("Initializing...");
  return { db: "connected" };
});

initialize(); // "Initializing..." => { db: "connected" }
initialize(); // => { db: "connected" } (no log, returns cached)
initialize(); // => { db: "connected" } (no log, returns cached)
```

**BAD -- without once, you risk double initialization:**

```js
let config = null;

function init() {
  console.log("Connecting to DB...");
  config = { db: "connected" };
  return config;
}

init(); // Connecting to DB...
init(); // Connecting to DB... -- OOPS, connected twice!
```

**GOOD -- with once:**

```js
const init = once(function() {
  console.log("Connecting to DB...");
  return { db: "connected" };
});

init(); // Connecting to DB...
init(); // silent, returns previous result
```

**Exam Tip**: `once()` is essentially `memoize()` but simpler -- it ignores arguments entirely and just remembers whether it has been called.

---

## Part C: LRU Cache (EXAM CRITICAL)

### 9. What is LRU?

**LRU = Least Recently Used**

A cache with a **fixed capacity**. When the cache is full and a new item needs to be added, **evict the item that was used least recently**.

**Real-world analogy**: Your browser keeps the last N tabs in memory. When memory is full, it unloads the tab you have not visited the longest.

**Key operations:**
- **get(key)**: Return the value and mark it as "recently used"
- **put(key, value)**: Add/update the item. If full, evict the LRU item first.

**Both operations must be O(1) time.**

---

### 10. LRU Cache Implementation Using Map

**Why Map works**: JavaScript `Map` **preserves insertion order**. The first key inserted is the first key returned by `map.keys()`. By deleting and re-inserting a key, we "move" it to the end (most recently used).

```js
class LRUCache {
  constructor(capacity) {
    this.capacity = capacity;   // Maximum number of items
    this.cache = new Map();     // Map preserves insertion order
  }

  get(key) {
    if (!this.cache.has(key)) return -1;  // Key not found

    const value = this.cache.get(key);    // Get the value

    // Move to end (most recently used):
    this.cache.delete(key);               // Remove from current position
    this.cache.set(key, value);           // Re-insert at end

    return value;
  }

  put(key, value) {
    if (this.cache.has(key)) {
      // Key exists -- delete it so we can re-insert at end
      this.cache.delete(key);
    } else if (this.cache.size >= this.capacity) {
      // Cache is full -- evict the LEAST recently used (first item in Map)
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
    }

    // Insert at end (most recently used position)
    this.cache.set(key, value);
  }
}
```

**Line-by-line for get(key):**

| Step | Code | Why |
|------|------|-----|
| 1 | `if (!this.cache.has(key)) return -1` | Standard "not found" return value |
| 2 | `const value = this.cache.get(key)` | Save the value before deleting |
| 3 | `this.cache.delete(key)` | Remove from its current position in order |
| 4 | `this.cache.set(key, value)` | Re-insert at the END (now most recent) |
| 5 | `return value` | Return to caller |

**Line-by-line for put(key, value):**

| Step | Code | Why |
|------|------|-----|
| 1 | `if (this.cache.has(key))` | If key already exists... |
| 2 | `this.cache.delete(key)` | ...delete it (will re-insert at end) |
| 3 | `else if (this.cache.size >= this.capacity)` | If cache is full... |
| 4 | `this.cache.keys().next().value` | Get the FIRST key (oldest/LRU) |
| 5 | `this.cache.delete(firstKey)` | Evict it |
| 6 | `this.cache.set(key, value)` | Insert new key at the END |

**The delete-then-set trick**: Map has no "move to end" method. So we delete the entry (removing it from its position in the order) and re-set it (which inserts it at the end). This is how we track recency.

---

### 11. Full Walkthrough

```js
const cache = new LRUCache(2);   // capacity = 2

cache.put(1, 'a');
// Map order: { 1:'a' }
// Size: 1

cache.put(2, 'b');
// Map order: { 1:'a', 2:'b' }
// Size: 2 (full)

cache.get(1);
// Key 1 exists. Delete and re-insert.
// Map order: { 2:'b', 1:'a' }
// Returns: 'a'
// Note: 1 is now at the END (most recent). 2 is at the START (least recent).

cache.put(3, 'c');
// Cache is full (size 2). Need to evict LRU.
// LRU = first key = 2. Evict it.
// Map order after eviction: { 1:'a' }
// Insert 3.
// Map order: { 1:'a', 3:'c' }

cache.get(2);
// Key 2 does not exist (was evicted).
// Returns: -1

cache.put(4, 'd');
// Cache is full. LRU = first key = 1. Evict it.
// Map order: { 3:'c', 4:'d' }

cache.get(1);   // -1 (evicted)
cache.get(3);   // 'c' (still here, moved to end)
cache.get(4);   // 'd' (still here, moved to end)
```

---

### 12. Time & Space Complexity

| Operation | Time | Why |
|-----------|------|-----|
| `get(key)` | **O(1)** | `Map.has`, `Map.get`, `Map.delete`, `Map.set` are all O(1) |
| `put(key, value)` | **O(1)** | Same Map operations + `keys().next()` is O(1) for first element |
| **Space** | **O(capacity)** | At most `capacity` items stored |

**Exam Tip**: If asked "how to implement LRU with O(1) for both get and put", the answer is **Map** (JS) or **HashMap + Doubly Linked List** (general CS). In JavaScript, Map alone is sufficient because it maintains insertion order.

---

> **Note:** JS Weird Parts (type coercion, hoisting, == vs ===, NaN/undefined/null) is covered in detail in **[14-js-weird-parts.md](14-js-weird-parts.md)**.

---

## Part D: Design Patterns (Bonus)

### 15. Singleton Pattern

**Purpose**: Ensure a class has only **one instance**. Every `new` call returns the same object.

```js
class ConfigManager {
  static #instance = null;   // Private static -- shared across all instances
  #config = {};              // Private instance field

  constructor() {
    if (ConfigManager.#instance) return ConfigManager.#instance; // Return existing
    ConfigManager.#instance = this;                              // Store first instance
  }

  set(key, value) { this.#config[key] = value; }
  get(key) { return this.#config[key]; }
}
```

**Usage:**

```js
const a = new ConfigManager();
const b = new ConfigManager();

a.set("theme", "dark");
console.log(b.get("theme")); // "dark" -- same instance!
console.log(a === b);         // true
```

**BAD -- without Singleton:**

```js
class Config {
  constructor() { this.config = {}; }
  set(k, v) { this.config[k] = v; }
  get(k) { return this.config[k]; }
}

const a = new Config();
const b = new Config();
a.set("theme", "dark");
console.log(b.get("theme")); // undefined -- different instances!
```

**Exam Tip**: The key line is `if (ConfigManager.#instance) return ConfigManager.#instance;` in the constructor. The `#` makes the field truly private (not accessible outside the class).

---

### 16. Observer Pattern (EventEmitter)

**Purpose**: One object (emitter) broadcasts events. Multiple objects (listeners) subscribe and react. **Decouples** producers from consumers.

```js
class EventEmitter {
  constructor() {
    this.events = {};       // { eventName: [listener1, listener2, ...] }
  }

  on(event, listener) {
    // Add listener to event. Create array if first listener.
    (this.events[event] ||= []).push(listener);
  }

  off(event, listener) {
    // Remove a specific listener
    this.events[event] = (this.events[event] || []).filter(l => l !== listener);
  }

  emit(event, ...args) {
    // Call all listeners for this event with provided args
    (this.events[event] || []).forEach(l => l(...args));
  }

  once(event, listener) {
    // Listener that auto-removes after first call
    const wrapper = (...args) => {
      listener(...args);
      this.off(event, wrapper);  // Remove wrapper after execution
    };
    this.on(event, wrapper);
  }
}
```

**Usage:**

```js
const emitter = new EventEmitter();

function onData(data) {
  console.log("Received:", data);
}

emitter.on("data", onData);
emitter.emit("data", { id: 1 });  // "Received: { id: 1 }"
emitter.emit("data", { id: 2 });  // "Received: { id: 2 }"
emitter.off("data", onData);
emitter.emit("data", { id: 3 });  // (nothing -- listener removed)

emitter.once("connect", () => console.log("Connected!"));
emitter.emit("connect"); // "Connected!"
emitter.emit("connect"); // (nothing -- once listener auto-removed)
```

**Key detail**: `||=` is the **logical OR assignment**. `this.events[event] ||= []` means "if `this.events[event]` is falsy (undefined), assign `[]` to it". Then `.push(listener)` adds to that array.

---

### 17. Factory Pattern

**Purpose**: A function that creates and returns objects without using `new` directly. Lets you choose WHICH object to create at runtime.

```js
function createUser(type, name) {
  switch (type) {
    case "admin":
      return {
        name,
        role: "admin",
        permissions: ["read", "write", "delete"],
        canDelete: true,
      };
    case "editor":
      return {
        name,
        role: "editor",
        permissions: ["read", "write"],
        canDelete: false,
      };
    case "viewer":
      return {
        name,
        role: "viewer",
        permissions: ["read"],
        canDelete: false,
      };
    default:
      throw new Error(`Unknown user type: ${type}`);
  }
}

const admin = createUser("admin", "Alice");
const viewer = createUser("viewer", "Bob");

console.log(admin.permissions); // ["read", "write", "delete"]
console.log(viewer.permissions); // ["read"]
```

**Class-based factory:**

```js
class UserFactory {
  static create(type, name) {
    const roles = {
      admin:  { permissions: ["read", "write", "delete"], canDelete: true },
      editor: { permissions: ["read", "write"],           canDelete: false },
      viewer: { permissions: ["read"],                    canDelete: false },
    };

    if (!roles[type]) throw new Error(`Unknown type: ${type}`);

    return { name, role: type, ...roles[type] };
  }
}

const user = UserFactory.create("admin", "Alice");
```

**When to use**: When object creation logic is complex, when the type of object depends on runtime data, when you want to centralize creation logic.

---

## Exam-Style Coding Problems

### Problem 1: Implement memoize with a max cache size

**Question**: Write a `memoize` function that stores at most `maxSize` results. When the cache is full, remove the oldest entry (FIFO).

**Solution:**

```js
function memoize(fn, maxSize = 100) {
  const cache = new Map();

  return function(...args) {
    const key = JSON.stringify(args);

    if (cache.has(key)) {
      return cache.get(key);
    }

    const result = fn.apply(this, args);

    if (cache.size >= maxSize) {
      // Delete the oldest entry (first key in Map)
      const oldestKey = cache.keys().next().value;
      cache.delete(oldestKey);
    }

    cache.set(key, result);
    return result;
  };
}

// Test
const add = memoize((a, b) => {
  console.log("Computing...");
  return a + b;
}, 2); // max 2 entries

add(1, 2); // Computing... => 3
add(3, 4); // Computing... => 7
add(1, 2); // From cache => 3
add(5, 6); // Computing... => 11 (cache full, evicts (3,4))
add(3, 4); // Computing... => 7 (was evicted, recomputes)
```

---

### Problem 2: Implement an LRU Cache with a getOrCompute method

**Question**: Extend the LRU Cache so it has a `getOrCompute(key, computeFn)` method. If the key exists, return it. If not, call `computeFn()`, store the result, and return it.

**Solution:**

```js
class LRUCache {
  constructor(capacity) {
    this.capacity = capacity;
    this.cache = new Map();
  }

  get(key) {
    if (!this.cache.has(key)) return -1;
    const value = this.cache.get(key);
    this.cache.delete(key);
    this.cache.set(key, value);
    return value;
  }

  put(key, value) {
    if (this.cache.has(key)) {
      this.cache.delete(key);
    } else if (this.cache.size >= this.capacity) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
    }
    this.cache.set(key, value);
  }

  getOrCompute(key, computeFn) {
    const existing = this.get(key);
    if (existing !== -1) return existing;

    const value = computeFn();
    this.put(key, value);
    return value;
  }
}

// Test
const cache = new LRUCache(3);

const result = cache.getOrCompute("user:1", () => {
  console.log("Fetching user 1...");
  return { name: "Alice", id: 1 };
});
// "Fetching user 1..." => { name: "Alice", id: 1 }

const cached = cache.getOrCompute("user:1", () => {
  console.log("This should NOT run");
  return { name: "Alice", id: 1 };
});
// No log => { name: "Alice", id: 1 } (from cache)
```

---

---

## Exam Tips

- **Memoize = closure + cache object.** If they ask you to "optimize a recursive function," memoization is almost always the answer.
- **LRU Cache is a top interview/exam question.** Memorize the Map-based implementation. The key trick is **delete-then-set to move to end**.
- **Map preserves insertion order.** `map.keys().next().value` gives you the FIRST (oldest) key. This is what makes O(1) LRU possible in JS.
- **once() is a simplified memoize** -- it caches the first result and ignores all subsequent calls.
- **JSON.stringify for cache keys** works for primitives and plain objects but fails for functions, `undefined`, and circular references.
- **Singleton**: Constructor returns existing instance. **Observer**: `on/off/emit` pattern. **Factory**: Function returns different objects based on input.
- **JS Weird Parts** (type coercion, hoisting, == vs ===, NaN/null/undefined) are covered in **14-js-weird-parts.md**.
- **Always use `fn.apply(this, args)`** in wrappers to preserve the `this` context.
- **Time complexity of LRU**: O(1) for both get and put. Space is O(capacity). Know this cold.

---

## Quick Revision Table

| Concept | Key Idea | Implementation Detail | Complexity |
|---------|----------|----------------------|------------|
| **Memoize** | Cache function results by args | Closure + object/Map, `JSON.stringify` key | O(1) lookup |
| **once()** | Execute function only once | Boolean flag + cached result | O(1) |
| **LRU Cache** | Fixed-size cache, evict least recent | Map + delete-then-set trick | O(1) get/put |
| **Fibonacci (memo)** | Reduce O(2^n) to O(n) | Memoize recursive calls | O(n) time, O(n) space |
| **Singleton** | One instance per class | Return existing instance from constructor | N/A |
| **Observer** | Pub/sub event system | `events` object with arrays of listeners | O(1) emit per listener |
| **Factory** | Create objects dynamically | Switch/map on type, return object | O(1) |
| **Map vs Object** | Map: any key type, ordered | `map.keys().next().value` = first key | O(1) ops |
| **JSON.stringify limits** | Fails on functions, undefined, circular | Use Map with reference keys when needed | N/A |
