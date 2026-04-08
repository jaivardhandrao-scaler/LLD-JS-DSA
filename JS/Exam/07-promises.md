# Promise Internals & Polyfills (Class 8)

---

## Part A: Promise Fundamentals

---

### 1. Why Promises Exist

Look at this code first. Understand the pain, then the cure.

**BAD -- Callback Hell (Pyramid of Doom)**

```js
getUser(userId, function (err, user) {
  if (err) return handleError(err);
  getOrders(user.id, function (err, orders) {
    if (err) return handleError(err);
    getOrderDetails(orders[0].id, function (err, details) {
      if (err) return handleError(err);
      getShippingStatus(details.trackingId, function (err, status) {
        if (err) return handleError(err);
        console.log(status);
      });
    });
  });
});
```

Problems:
- **Deeply nested** -- hard to read, hard to maintain
- **Error handling repeated** at every level
- **Inversion of control** -- you hand your callback to someone else and hope they call it correctly (once, with the right args)

**GOOD -- Promise Chain**

```js
getUser(userId)
  .then(user => getOrders(user.id))
  .then(orders => getOrderDetails(orders[0].id))
  .then(details => getShippingStatus(details.trackingId))
  .then(status => console.log(status))
  .catch(err => handleError(err));
```

- **Flat** -- reads top to bottom
- **Single `.catch()`** handles errors from any step
- **Composable** -- each `.then` returns a new promise

---

### 2. Promise States

A promise is always in exactly one of three states:

| State | Meaning | Can transition to? |
|---|---|---|
| **pending** | Not yet settled | fulfilled OR rejected |
| **fulfilled** | Operation succeeded, has a **value** | Nothing (final) |
| **rejected** | Operation failed, has a **reason** | Nothing (final) |

**Key rules:**
- A promise transitions **once**. `pending -> fulfilled` or `pending -> rejected`. Never both. Never reversed.
- Once settled (fulfilled or rejected), the value/reason is **immutable**.

```js
const p = new Promise((resolve, reject) => {
  resolve("first");   // p is now fulfilled with "first"
  resolve("second");  // IGNORED -- already settled
  reject("error");    // IGNORED -- already settled
});

p.then(val => console.log(val)); // "first"
```

---

### 3. Creating a Promise

```js
const p = new Promise((resolve, reject) => {
  // The executor runs IMMEDIATELY (synchronously)
  // Do async work here, then call:
  //   resolve(value)  -- to fulfill
  //   reject(error)   -- to reject
});
```

**Real example:**

```js
function delay(ms) {
  return new Promise((resolve) => {
    setTimeout(() => resolve(`Done after ${ms}ms`), ms);
  });
}

delay(1000).then(msg => console.log(msg)); // "Done after 1000ms" (after 1 second)
```

**BAD -- Throwing inside executor rejects the promise**

```js
const p = new Promise((resolve, reject) => {
  throw new Error("oops"); // same as calling reject(new Error("oops"))
});

p.catch(err => console.log(err.message)); // "oops"
```

**Exam Tip:** The executor function runs **synchronously**. Only the `.then`/`.catch` callbacks are asynchronous (microtask queue).

---

### 4. `.then(onFulfilled, onRejected)`

`.then()` **always returns a NEW promise**. This is what makes chaining work.

**Rule 1: Return value becomes the resolved value of the next promise**

```js
Promise.resolve(1)
  .then(val => val + 1)    // returns 2
  .then(val => val * 3)    // returns 6
  .then(val => console.log(val)); // 6
```

**Rule 2: Throwing in `.then` rejects the next promise**

```js
Promise.resolve(1)
  .then(val => { throw new Error("boom"); })
  .then(val => console.log("skipped"))      // SKIPPED
  .catch(err => console.log(err.message));   // "boom"
```

**Rule 3: Returning a promise from `.then` causes it to "unwrap"**

```js
Promise.resolve(1)
  .then(val => {
    return new Promise(resolve => setTimeout(() => resolve(val + 10), 100));
  })
  .then(val => console.log(val)); // 11 (after 100ms)
```

**Rule 4: `.then` with two arguments**

```js
somePromise.then(
  value => { /* onFulfilled */ },
  error => { /* onRejected */ }
);
```

**BAD -- Using second arg of `.then` for error handling in chains**

```js
// This does NOT catch errors thrown inside the onFulfilled handler:
somePromise.then(
  value => { throw new Error("oops"); },
  error => { console.log("never reached"); }
);
// The error goes UNHANDLED because the onRejected in the same .then
// does not catch errors from the onFulfilled in the same .then.
```

**GOOD -- Use `.catch` instead**

```js
somePromise
  .then(value => { throw new Error("oops"); })
  .catch(error => console.log(error.message)); // "oops"
```

---

### 5. `.catch(onRejected)`

`.catch(fn)` is syntactic sugar for `.then(null, fn)`. That is all.

```js
// These two are IDENTICAL:
p.catch(err => handleError(err));
p.then(null, err => handleError(err));
```

**Important:** `.catch()` also returns a new promise. If the catch handler returns a value, the chain **recovers**.

```js
Promise.reject("fail")
  .catch(err => "recovered")
  .then(val => console.log(val)); // "recovered"
```

---

### 6. `.finally(callback)`

- Runs **regardless** of fulfilled or rejected
- Does **NOT** receive the value or reason as an argument
- **Passes through** the original value/reason to the next handler (unless it throws or returns a rejected promise)

```js
Promise.resolve("data")
  .finally(() => console.log("cleanup"))  // "cleanup"
  .then(val => console.log(val));         // "data" (passed through!)

Promise.reject("error")
  .finally(() => console.log("cleanup"))  // "cleanup"
  .catch(err => console.log(err));        // "error" (passed through!)
```

**BAD -- Trying to use finally's return value**

```js
Promise.resolve("original")
  .finally(() => "ignored")
  .then(val => console.log(val)); // "original", NOT "ignored"
```

**Exception:** If `finally` throws, the promise rejects with that error.

```js
Promise.resolve("original")
  .finally(() => { throw new Error("oops"); })
  .catch(err => console.log(err.message)); // "oops"
```

---

### 7. Error Propagation

Errors **skip** `.then(onFulfilled)` handlers and travel down the chain until a `.catch()` (or `.then(null, onRejected)`) is found.

```js
Promise.reject("err")
  .then(val => console.log("skip 1"))  // SKIPPED
  .then(val => console.log("skip 2"))  // SKIPPED
  .then(val => console.log("skip 3"))  // SKIPPED
  .catch(err => console.log(err));     // "err"
```

**After `.catch`, the chain continues normally:**

```js
Promise.reject("err")
  .catch(err => 42)                      // recovers with 42
  .then(val => console.log(val));        // 42
```

**Exam Tip:** A `.catch()` in the middle of a chain acts like a try/catch in synchronous code -- it handles the error and the chain resumes.

---

### 8. Promise Resolution Procedure (Unwrapping)

If you `resolve` a promise with another promise (or thenable), the outer promise **adopts the state** of the inner one. This is called "unwrapping."

```js
const inner = new Promise(resolve => setTimeout(() => resolve("inner value"), 100));

const outer = new Promise(resolve => {
  resolve(inner); // outer now waits for inner
});

outer.then(val => console.log(val)); // "inner value" (after 100ms)
```

**Contrast with `reject`:** `reject` does NOT unwrap.

```js
const inner = Promise.resolve("value");

const p = new Promise((_, reject) => {
  reject(inner); // p is rejected with a Promise OBJECT, not "value"
});

p.catch(err => console.log(err)); // Promise { "value" } (the promise itself)
```

**Exam Tip:** `resolve` unwraps thenables. `reject` does not.

---

## Part B: Promise Static Methods

---

### 9. `Promise.all(iterable)`

Takes an iterable of promises. Returns a single promise that:
- **Fulfills** with an array of all results when **every** promise fulfills (order preserved)
- **Rejects** with the reason of the **first** promise that rejects

```js
const p1 = Promise.resolve(1);
const p2 = Promise.resolve(2);
const p3 = Promise.resolve(3);

Promise.all([p1, p2, p3]).then(results => console.log(results)); // [1, 2, 3]
```

```js
const p1 = Promise.resolve(1);
const p2 = Promise.reject("fail");
const p3 = Promise.resolve(3);

Promise.all([p1, p2, p3]).catch(err => console.log(err)); // "fail"
// p1 and p3 still run -- Promise.all does NOT cancel them
```

**Edge case:** Non-promise values are treated as `Promise.resolve(value)`.

```js
Promise.all([1, 2, 3]).then(r => console.log(r)); // [1, 2, 3]
```

**Edge case:** Empty iterable resolves immediately.

```js
Promise.all([]).then(r => console.log(r)); // []
```

---

### 10. `Promise.allSettled(iterable)`

Waits for **all** promises to settle (fulfill or reject). **Never rejects** (unless the iterable itself throws).

Returns an array of objects:

```js
const p1 = Promise.resolve("ok");
const p2 = Promise.reject("fail");
const p3 = Promise.resolve(42);

Promise.allSettled([p1, p2, p3]).then(results => console.log(results));
// [
//   { status: "fulfilled", value: "ok" },
//   { status: "rejected",  reason: "fail" },
//   { status: "fulfilled", value: 42 }
// ]
```

**Use case:** When you need results from all operations regardless of individual failures (e.g., sending notifications to multiple users).

---

### 11. `Promise.race(iterable)`

Returns a promise that settles as soon as the **first** promise in the iterable settles (fulfilled or rejected).

```js
const slow = new Promise(resolve => setTimeout(() => resolve("slow"), 200));
const fast = new Promise(resolve => setTimeout(() => resolve("fast"), 50));

Promise.race([slow, fast]).then(val => console.log(val)); // "fast"
```

**If the first to settle is a rejection:**

```js
const slow = new Promise(resolve => setTimeout(() => resolve("slow"), 200));
const fast = new Promise((_, reject) => setTimeout(() => reject("fast fail"), 50));

Promise.race([slow, fast]).catch(err => console.log(err)); // "fast fail"
```

**Edge case:** Empty iterable = promise that **never settles** (stays pending forever).

---

### 12. `Promise.any(iterable)`

Returns a promise that fulfills with the **first fulfilled** value. Ignores rejections unless **ALL** reject.

```js
const p1 = Promise.reject("err1");
const p2 = Promise.resolve("ok");
const p3 = Promise.reject("err3");

Promise.any([p1, p2, p3]).then(val => console.log(val)); // "ok"
```

**All reject:**

```js
const p1 = Promise.reject("err1");
const p2 = Promise.reject("err2");

Promise.any([p1, p2]).catch(err => {
  console.log(err);                    // AggregateError: All promises were rejected
  console.log(err.errors);            // ["err1", "err2"]
});
```

**Edge case:** Empty iterable rejects with `AggregateError`.

---

### 13. `Promise.resolve(value)` and `Promise.reject(reason)`

```js
// Wraps a value in a fulfilled promise
Promise.resolve(42).then(v => console.log(v)); // 42

// If value is already a promise, returns it AS-IS (no wrapping)
const p = new Promise(resolve => resolve(1));
console.log(Promise.resolve(p) === p); // true

// Wraps a reason in a rejected promise
Promise.reject("oops").catch(e => console.log(e)); // "oops"

// reject does NOT unwrap -- even if you pass a promise
Promise.reject(Promise.resolve(1)).catch(e => console.log(e)); // Promise {1}
```

---

### Comparison Table: `all` vs `allSettled` vs `race` vs `any`

| Method | Resolves when... | Rejects when... | Result shape | Empty iterable |
|---|---|---|---|---|
| **`Promise.all`** | ALL fulfill | FIRST rejection | `[val, val, ...]` | Resolves `[]` |
| **`Promise.allSettled`** | ALL settle | Never* | `[{status, value/reason}, ...]` | Resolves `[]` |
| **`Promise.race`** | First settles (any) | First settles (if rejection) | Single value | Never settles |
| **`Promise.any`** | FIRST fulfills | ALL reject | Single value | Rejects (AggregateError) |

*`allSettled` itself never rejects from the promises. It can reject if the iterable throws during iteration.

**Exam Tip:** `race` cares about the first to **settle** (fulfill or reject). `any` cares about the first to **fulfill** (ignores rejections unless all reject).

---

## Part C: Polyfills (EXAM CRITICAL)

---

### 14. `myPromiseAll` Polyfill

```js
function myPromiseAll(promises) {
  return new Promise((resolve, reject) => {
    const results = [];
    let completed = 0;
    const items = Array.from(promises);

    if (items.length === 0) return resolve([]);

    items.forEach((item, index) => {
      Promise.resolve(item).then(value => {
        results[index] = value;
        completed++;
        if (completed === items.length) resolve(results);
      }).catch(reject);
    });
  });
}
```

**Line-by-line explanation:**

| Line | What it does |
|---|---|
| `return new Promise((resolve, reject) => {` | We return a single promise that wraps all the inner ones. |
| `const results = [];` | Array to collect fulfilled values in order. |
| `let completed = 0;` | Counter -- how many promises have fulfilled so far. |
| `const items = Array.from(promises);` | Convert iterable to array (handles Sets, generators, etc.). |
| `if (items.length === 0) return resolve([]);` | Edge case: empty input resolves immediately with `[]`. |
| `items.forEach((item, index) => {` | Iterate over each promise/value. |
| `Promise.resolve(item)` | Wrap non-promise values into promises (handles raw values like `42`). |
| `.then(value => {` | When this particular promise fulfills... |
| `results[index] = value;` | Store at the correct index (NOT `push` -- order must match input). |
| `completed++;` | Increment counter. |
| `if (completed === items.length) resolve(results);` | If all done, resolve the outer promise with the full results array. |
| `}).catch(reject);` | If ANY promise rejects, reject the outer promise immediately. |

**Why `results[index]` and not `results.push(value)`?**
Promises resolve in unpredictable order. `push` would give results in completion order, not input order. Using `index` preserves input order.

**Why `completed` counter and not `results.length`?**
Sparse arrays: `results[5] = "x"` makes `results.length === 6`, not 1. The counter is reliable.

**Test case:**

```js
const p1 = new Promise(resolve => setTimeout(() => resolve("a"), 100));
const p2 = Promise.resolve("b");
const p3 = new Promise(resolve => setTimeout(() => resolve("c"), 50));

myPromiseAll([p1, p2, p3]).then(console.log);
// ["a", "b", "c"]  (order matches input, not completion time)

myPromiseAll([Promise.resolve(1), Promise.reject("fail"), Promise.resolve(3)])
  .catch(console.log);
// "fail"
```

---

### 15. `myPromiseRace` Polyfill

```js
function myPromiseRace(promises) {
  return new Promise((resolve, reject) => {
    const items = Array.from(promises);

    // Empty iterable: returned promise stays pending forever (per spec)
    items.forEach(item => {
      Promise.resolve(item).then(resolve).catch(reject);
    });
  });
}
```

**Explanation:**
- Each promise calls `resolve` or `reject` on the outer promise when it settles.
- The **first** one to call `resolve` or `reject` wins. All subsequent calls are ignored (a promise can only settle once).
- No counter needed, no results array needed -- just fire and the first one wins.

**Test case:**

```js
const slow = new Promise(resolve => setTimeout(() => resolve("slow"), 200));
const fast = new Promise(resolve => setTimeout(() => resolve("fast"), 50));

myPromiseRace([slow, fast]).then(console.log); // "fast"

const fail = new Promise((_, reject) => setTimeout(() => reject("err"), 10));
const success = new Promise(resolve => setTimeout(() => resolve("ok"), 100));

myPromiseRace([fail, success]).catch(console.log); // "err"
```

---

### 16. `myPromiseAny` Polyfill

```js
function myPromiseAny(promises) {
  return new Promise((resolve, reject) => {
    const items = Array.from(promises);
    const errors = [];
    let rejectedCount = 0;

    if (items.length === 0) {
      return reject(new AggregateError([], "All promises were rejected"));
    }

    items.forEach((item, index) => {
      Promise.resolve(item).then(resolve).catch(error => {
        errors[index] = error;
        rejectedCount++;
        if (rejectedCount === items.length) {
          reject(new AggregateError(errors, "All promises were rejected"));
        }
      });
    });
  });
}
```

**Explanation:**
- This is the **opposite logic** of `Promise.all`:
  - `all`: resolves when all fulfill, rejects on first rejection
  - `any`: resolves on first fulfillment, rejects when all reject
- `errors[index]` preserves rejection order matching input order.
- `rejectedCount` counts rejections. Only when ALL have rejected do we reject with `AggregateError`.
- First `.then(resolve)` that fires wins; subsequent calls are ignored.
- Empty iterable immediately rejects (per spec).

**Test case:**

```js
const p1 = Promise.reject("err1");
const p2 = new Promise(resolve => setTimeout(() => resolve("ok"), 50));
const p3 = Promise.reject("err3");

myPromiseAny([p1, p2, p3]).then(console.log); // "ok"

myPromiseAny([Promise.reject("a"), Promise.reject("b")]).catch(err => {
  console.log(err.errors); // ["a", "b"]
});
```

---

### 17. `myPromiseAllSettled` Polyfill

```js
function myPromiseAllSettled(promises) {
  return new Promise((resolve) => {
    const items = Array.from(promises);
    const results = [];
    let settledCount = 0;

    if (items.length === 0) return resolve([]);

    items.forEach((item, index) => {
      Promise.resolve(item)
        .then(value => {
          results[index] = { status: "fulfilled", value };
        })
        .catch(reason => {
          results[index] = { status: "rejected", reason };
        })
        .finally(() => {
          settledCount++;
          if (settledCount === items.length) resolve(results);
        });
    });
  });
}
```

**Explanation:**
- The outer promise **never rejects** -- notice there is no `reject` parameter destructured (it is not needed).
- Each inner promise settles into an object: `{ status: "fulfilled", value }` or `{ status: "rejected", reason }`.
- `.finally()` runs after either `.then` or `.catch`, so the counter increments regardless.
- When all have settled, resolve with the full results array.

**Test case:**

```js
const p1 = Promise.resolve("ok");
const p2 = Promise.reject("fail");
const p3 = Promise.resolve(42);

myPromiseAllSettled([p1, p2, p3]).then(console.log);
// [
//   { status: "fulfilled", value: "ok" },
//   { status: "rejected",  reason: "fail" },
//   { status: "fulfilled", value: 42 }
// ]
```

---

### Polyfill Pattern Summary

| Polyfill | Counter tracks | Resolves outer when | Rejects outer when |
|---|---|---|---|
| `myPromiseAll` | `completed` (fulfillments) | completed === total | Any single rejection |
| `myPromiseRace` | Nothing | First settlement | First settlement (if rejection) |
| `myPromiseAny` | `rejectedCount` (rejections) | First fulfillment | rejectedCount === total |
| `myPromiseAllSettled` | `settledCount` (all) | settledCount === total | Never |

---

## Part D: async/await

---

### 18. async Functions Return a Promise

An `async` function **always** returns a promise, even if you return a plain value.

```js
async function greet() {
  return "hello";
}

// Equivalent to:
function greet() {
  return Promise.resolve("hello");
}

greet().then(val => console.log(val)); // "hello"
```

If an `async` function throws, the returned promise is **rejected**:

```js
async function fail() {
  throw new Error("oops");
}

fail().catch(err => console.log(err.message)); // "oops"
```

---

### 19. await Pauses Until Promise Settles

`await` can only be used inside an `async` function (or at the top level of a module).

```js
async function fetchData() {
  const response = await fetch("https://api.example.com/data");
  const data = await response.json();
  return data;
}
```

**What `await` actually does:**
1. Evaluates the expression to the right (gets a promise)
2. Pauses the async function
3. When the promise fulfills, resumes with the fulfilled value
4. When the promise rejects, throws the rejection reason (can be caught with try/catch)

```js
async function demo() {
  const val = await Promise.resolve(42);
  console.log(val); // 42 (not a Promise object)
}
```

**`await` with non-promise values:**

```js
async function demo() {
  const val = await 42; // treated as await Promise.resolve(42)
  console.log(val);     // 42
}
```

---

### 20. try/catch with async/await

**BAD -- Promise chain error handling (verbose in complex flows)**

```js
function processData() {
  return fetchUser()
    .then(user => fetchOrders(user.id))
    .then(orders => processOrders(orders))
    .catch(err => console.error("Error:", err));
}
```

**GOOD -- async/await with try/catch**

```js
async function processData() {
  try {
    const user = await fetchUser();
    const orders = await fetchOrders(user.id);
    const result = await processOrders(orders);
    return result;
  } catch (err) {
    console.error("Error:", err);
  }
}
```

**Granular error handling:**

```js
async function processData() {
  let user;
  try {
    user = await fetchUser();
  } catch (err) {
    console.error("Failed to fetch user:", err);
    return;
  }

  try {
    const orders = await fetchOrders(user.id);
    return await processOrders(orders);
  } catch (err) {
    console.error("Failed to process orders:", err);
  }
}
```

---

### 21. Common Mistake: Forgetting `await`

**BAD -- Missing await**

```js
async function getUser() {
  const user = fetch("/api/user"); // MISSING await!
  console.log(user);              // Promise { <pending> }  <-- NOT the data
  console.log(user.name);         // undefined
}
```

**GOOD -- With await**

```js
async function getUser() {
  const response = await fetch("/api/user");
  const user = await response.json();
  console.log(user);        // { name: "Alice", ... }
  console.log(user.name);   // "Alice"
}
```

**Another trap -- forgetting await in conditionals:**

**BAD**

```js
async function check() {
  if (fetchStatus()) {    // This is ALWAYS truthy -- it's a Promise object!
    console.log("active");
  }
}
```

**GOOD**

```js
async function check() {
  if (await fetchStatus()) {
    console.log("active");
  }
}
```

**Sequential vs Parallel with await:**

**BAD -- Unnecessarily sequential (slow)**

```js
async function loadAll() {
  const a = await fetchA(); // waits for A to finish
  const b = await fetchB(); // THEN starts B
  const c = await fetchC(); // THEN starts C
  return [a, b, c];
}
```

**GOOD -- Parallel when tasks are independent**

```js
async function loadAll() {
  const [a, b, c] = await Promise.all([fetchA(), fetchB(), fetchC()]);
  return [a, b, c];
}
```

---

## Predict the Output (Exam Practice)

---

### Question 1

```js
console.log("A");

const p = new Promise((resolve) => {
  console.log("B");
  resolve("C");
  console.log("D");
});

p.then(val => console.log(val));

console.log("E");
```

<details>
<summary><strong>Answer</strong></summary>

```
A
B
D
E
C
```

**Explanation:**
1. `"A"` -- synchronous
2. `"B"` -- the executor runs synchronously
3. `resolve("C")` -- schedules the `.then` callback as a microtask, but does NOT stop the executor
4. `"D"` -- executor continues after resolve
5. `"E"` -- synchronous code after promise creation
6. `"C"` -- microtask queue runs after all synchronous code finishes

</details>

---

### Question 2

```js
Promise.resolve(1)
  .then(val => {
    console.log(val);
    return val + 1;
  })
  .then(val => {
    console.log(val);
    throw new Error("boom");
  })
  .then(val => {
    console.log(val);        // Does this run?
  })
  .catch(err => {
    console.log(err.message);
    return 99;
  })
  .then(val => {
    console.log(val);
  });
```

<details>
<summary><strong>Answer</strong></summary>

```
1
2
boom
99
```

**Explanation:**
1. First `.then`: `val` is 1, logs `1`, returns `2`
2. Second `.then`: `val` is 2, logs `2`, throws `Error("boom")`
3. Third `.then`: SKIPPED -- error propagates past `.then` handlers
4. `.catch`: catches the error, logs `"boom"`, returns `99` (chain recovers)
5. Final `.then`: `val` is 99, logs `99`

</details>

---

### Question 3

```js
async function foo() {
  console.log("1");
  const x = await Promise.resolve("2");
  console.log(x);
  console.log("3");
}

console.log("4");
foo();
console.log("5");
```

<details>
<summary><strong>Answer</strong></summary>

```
4
1
5
2
3
```

**Explanation:**
1. `"4"` -- synchronous, before `foo()` is called
2. `foo()` starts -- `"1"` is logged synchronously inside `foo`
3. `await` is hit -- `foo` pauses, control returns to the caller
4. `"5"` -- synchronous, after `foo()` returns (the promise, but foo is paused)
5. Microtask queue: `await` resumes, `"2"` is logged
6. `"3"` is logged

**Key insight:** Code before the first `await` runs synchronously. Code after `await` runs as a microtask.

</details>

---

## Exam Tips

- **Executor is synchronous.** `new Promise(executor)` -- the executor runs immediately, not asynchronously.
- **`.then` callbacks are microtasks.** They run after the current synchronous code finishes, before the next macrotask (setTimeout, etc.).
- **`.then` always returns a NEW promise.** Even `.catch` and `.finally` return new promises. This is why chaining works.
- **Settled = fulfilled or rejected.** "Settled" is not a state; it means "no longer pending."
- **`resolve` unwraps, `reject` does not.** If you `resolve(anotherPromise)`, the outer promise waits. If you `reject(anotherPromise)`, the rejection reason IS the promise object.
- **Order preservation.** `Promise.all` and `Promise.allSettled` preserve input order in results, regardless of completion order. This is why polyfills use `results[index]`, not `push`.
- **Empty iterable edge cases:** `all([])` and `allSettled([])` resolve with `[]`. `race([])` stays pending forever. `any([])` rejects with `AggregateError`.
- **`async` always returns a promise.** Even `async function f() { return 5; }` returns `Promise.resolve(5)`.
- **Forgetting `await` gives you a Promise object.** A Promise is truthy, so `if (fetchSomething())` is always true without `await`.
- **Polyfill patterns:** Know the counter + index pattern. `completed++` then check `completed === total`. Use `results[index]`, never `push`.
- **`finally` passes through.** It does not receive or alter the value/reason, unless it throws.

---

## Quick Revision Table

| Concept | Key Point |
|---|---|
| **Promise states** | `pending` -> `fulfilled` or `rejected` (one-way, irreversible) |
| **Executor** | Runs synchronously, receives `resolve` and `reject` |
| **`.then()`** | Returns a new promise; return value becomes next resolve value |
| **`.catch()`** | Sugar for `.then(null, onRejected)`; chain recovers after catch |
| **`.finally()`** | No args; passes through value; runs regardless of outcome |
| **Error propagation** | Errors skip `.then` handlers until `.catch` is found |
| **Resolve unwraps** | `resolve(promise)` adopts inner state; `reject(promise)` does NOT |
| **`Promise.all`** | All fulfill -> array of values; first reject -> rejects |
| **`Promise.allSettled`** | All settle -> array of `{status, value/reason}`; never rejects |
| **`Promise.race`** | First to settle wins (fulfill or reject) |
| **`Promise.any`** | First to fulfill wins; all reject -> `AggregateError` |
| **`Promise.resolve`** | Wraps value; returns same promise if already a promise |
| **`Promise.reject`** | Wraps reason; does NOT unwrap promises |
| **Polyfill: `all`** | Counter for fulfillments; `results[index]`; any reject -> reject outer |
| **Polyfill: `race`** | No counter; first `.then(resolve).catch(reject)` wins |
| **Polyfill: `any`** | Counter for rejections; first fulfill -> resolve outer; all reject -> AggregateError |
| **Polyfill: `allSettled`** | Counter for settlements; `{status, value/reason}` objects; outer never rejects |
| **`async` function** | Always returns a promise |
| **`await`** | Pauses async function; resumes with fulfilled value or throws rejection |
| **`try/catch` + `await`** | Clean error handling for async code |
| **Forgetting `await`** | You get a Promise object (truthy!), not the value |
| **Sequential vs parallel** | `await` one-by-one = sequential; `await Promise.all([...])` = parallel |
| **Microtask queue** | `.then` callbacks and `await` resumptions run as microtasks, after sync code, before setTimeout |
