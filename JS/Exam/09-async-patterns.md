# Async Patterns, Concurrency Control & Error Handling

> Classes 10 & 12 -- Exam Study Guide

---

## Part A: Async Execution Patterns

---

### 1. Sequential Execution

**The problem:** You have an array of async tasks. You need each one to finish before the next one starts.

**BAD -- forEach with await (does NOT wait)**

```js
async function sequential(tasks) {
  const results = [];
  // BAD: forEach ignores the returned promise from the async callback.
  // All tasks fire at roughly the same time — this is NOT sequential.
  tasks.forEach(async (task) => {
    const result = await task();
    results.push(result);
  });
  return results; // returns [] immediately — results are still pending
}
```

Why it fails: `Array.prototype.forEach` does not await the promise returned by the callback. It calls every callback synchronously in one tick, so all tasks launch in parallel, and the function returns an empty array before any of them resolve.

**GOOD -- for...of with await**

```js
async function sequential(tasks) {
  const results = [];
  for (const task of tasks) {
    results.push(await task());   // waits for each task before moving on
  }
  return results;
}

// --- runnable demo ---
const delay = (ms, val) => () =>
  new Promise(r => setTimeout(() => { console.log(val); r(val); }, ms));

const tasks = [
  delay(300, 'A'),
  delay(100, 'B'),
  delay(200, 'C'),
];

sequential(tasks).then(console.log);
// Console output order: A, B, C  (300ms → 100ms → 200ms, total ~600ms)
// Final result: ['A', 'B', 'C']
```

**When to use:** Order matters, or each task depends on the previous result (e.g., paginated API calls).

---

### 2. Parallel Execution

**The problem:** You have independent tasks and want to run them all at the same time for maximum speed.

```js
async function parallel(tasks) {
  return Promise.all(tasks.map(t => t()));
}

// --- runnable demo ---
const delay = (ms, val) => () =>
  new Promise(r => setTimeout(() => { console.log(val); r(val); }, ms));

const tasks = [
  delay(300, 'A'),
  delay(100, 'B'),
  delay(200, 'C'),
];

parallel(tasks).then(console.log);
// Console output order: B, C, A  (whichever finishes first)
// Final result: ['A', 'B', 'C']  (order preserved by Promise.all)
// Total time: ~300ms (limited by the slowest task)
```

**Key detail:** `Promise.all` preserves the **input order** in the results array, even though tasks complete in any order.

**BAD -- launching tasks too early (common mistake)**

```js
// BAD: tasks are already running before Promise.all sees them
const tasks = [fetchA(), fetchB(), fetchC()]; // all started NOW
const results = await Promise.all(tasks);     // just waiting, not controlling start
```

**GOOD -- pass functions, call them inside map**

```js
// GOOD: tasks are functions; we control when they start
const taskFns = [() => fetchA(), () => fetchB(), () => fetchC()];
const results = await Promise.all(taskFns.map(fn => fn()));
```

**When to use:** Tasks are independent and you want maximum throughput. Be cautious: if you launch 10,000 requests at once, you may overwhelm the server.

---

### 3. Promise Pool / Concurrency Limiter

**The problem:** You have 100 tasks but can only run 5 at a time (e.g., rate-limited API, database connection limit).

**Full implementation:**

```js
function promisePool(tasks, limit) {
  return new Promise((resolve, reject) => {
    let nextIndex = 0;    // index of the next task to start
    let active = 0;       // how many are currently running
    let completed = 0;    // how many have finished
    const results = [];   // stores results in original order

    function runNext() {
      if (nextIndex >= tasks.length && active === 0) {
        resolve(results);   // all done
        return;
      }
      while (active < limit && nextIndex < tasks.length) {
        const i = nextIndex++;
        active++;
        tasks[i]()
          .then(val => {
            results[i] = val;   // store at original index
          })
          .catch(err => {
            results[i] = err;   // or: reject(err) to fail fast
          })
          .finally(() => {
            active--;
            completed++;
            runNext();          // pull the next task off the queue
          });
      }
    }

    if (tasks.length === 0) {
      resolve([]);
      return;
    }

    runNext();
  });
}

// --- runnable demo ---
const delay = (ms, val) => () =>
  new Promise(r => setTimeout(() => {
    console.log(`done: ${val}`);
    r(val);
  }, ms));

const tasks = [
  delay(300, 'A'),
  delay(100, 'B'),
  delay(200, 'C'),
  delay(150, 'D'),
  delay(250, 'E'),
];

promisePool(tasks, 2).then(console.log);
// At most 2 tasks run at any time.
// Result: ['A', 'B', 'C', 'D', 'E'] (original order)
```

**How it works, step by step:**

1. Start up to `limit` tasks immediately.
2. When any task finishes, decrement `active`, store the result, and call `runNext()`.
3. `runNext()` starts new tasks as long as `active < limit` and tasks remain.
4. When `active === 0` and no tasks are left, resolve.

**Alternative: async/await style (cleaner, also exam-worthy)**

```js
async function promisePoolAsync(tasks, limit) {
  const results = [];
  const executing = new Set();

  for (const [i, task] of tasks.entries()) {
    const p = task().then(val => {
      results[i] = val;
      executing.delete(p);
    });
    executing.add(p);

    if (executing.size >= limit) {
      await Promise.race(executing);  // wait for ANY one to finish
    }
  }

  await Promise.all(executing);  // wait for remaining
  return results;
}
```

---

### 4. Sequential Queue (Dynamic Task Addition)

**The problem:** Tasks arrive at unpredictable times. Each must run one at a time, in the order it was added.

```js
function createSequentialQueue() {
  let tail = Promise.resolve();

  return {
    add(taskFn) {
      // Chain this task onto the end of the current queue
      const result = tail.then(() => taskFn());
      // Swallow errors so the chain doesn't break for future tasks
      tail = result.catch(() => {});
      return result;  // caller can still catch their own errors
    }
  };
}

// --- runnable demo ---
const queue = createSequentialQueue();

function asyncWork(label, ms) {
  return () => new Promise(resolve => {
    console.log(`start: ${label}`);
    setTimeout(() => {
      console.log(`done:  ${label}`);
      resolve(label);
    }, ms);
  });
}

// Tasks added at different times — all run sequentially
queue.add(asyncWork('A', 300));  // starts immediately
queue.add(asyncWork('B', 100));  // waits for A
queue.add(asyncWork('C', 200));  // waits for B

// Output:
// start: A → done: A → start: B → done: B → start: C → done: C
```

**Why `tail = result.catch(() => {})` matters:**

Without it, if task A rejects, the chain breaks and tasks B, C, etc. never run. The `.catch(() => {})` swallows the error at the chain level, but `result` (returned to the caller) still carries the real rejection -- so callers can handle their own errors normally.

---

## Part B: Error Handling in Async Code

---

### 5. try/catch with async/await

**The standard pattern -- works exactly like synchronous try/catch.**

```js
async function fetchUserData(userId) {
  try {
    const response = await fetch(`/api/users/${userId}`);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    return data;
  } catch (err) {
    console.error('Failed to fetch user:', err.message);
    throw err;   // re-throw if you want the caller to handle it too
  } finally {
    console.log('fetch attempt completed');  // runs whether it succeeded or not
  }
}
```

**BAD -- catch swallows error silently**

```js
async function getUser(id) {
  try {
    return await fetch(`/api/users/${id}`);
  } catch (err) {
    // BAD: returns undefined, caller has no idea something went wrong
  }
}
```

**GOOD -- either re-throw or return a meaningful fallback**

```js
async function getUser(id) {
  try {
    return await fetch(`/api/users/${id}`);
  } catch (err) {
    console.error(err);
    return { error: true, message: err.message };  // explicit fallback
  }
}
```

---

### 6. Promise.catch -- Error Propagation in Chains

```js
fetch('/api/data')
  .then(res => res.json())
  .then(data => processData(data))
  .then(result => saveResult(result))
  .catch(err => {
    // catches errors from ANY step above
    console.error('Pipeline failed:', err.message);
  });
```

**Key rules of .catch propagation:**

- An error skips all `.then` handlers until it hits a `.catch`.
- A `.catch` that returns a value **recovers** the chain (next `.then` runs).
- A `.catch` that throws (or returns a rejected promise) **continues** the error path.

```js
Promise.reject('boom')
  .then(v => console.log('A:', v))   // skipped
  .then(v => console.log('B:', v))   // skipped
  .catch(err => {
    console.log('caught:', err);     // "caught: boom"
    return 'recovered';              // chain recovers here
  })
  .then(v => console.log('C:', v))   // "C: recovered"
  .catch(err => console.log('D:', err)); // NOT reached — chain recovered
```

**BAD -- .catch in the middle swallows error**

```js
fetchData()
  .catch(err => console.log(err))   // swallows error, returns undefined
  .then(data => data.name)          // TypeError: cannot read property of undefined
```

**GOOD -- re-throw after logging**

```js
fetchData()
  .catch(err => { console.log(err); throw err; })  // re-throws
  .then(data => data.name)  // skipped because error re-thrown
  .catch(err => handleFinalError(err));
```

---

### 7. Retry with Exponential Backoff

**The problem:** A network call fails. Retry it, but wait longer each time (to avoid hammering the server).

```js
async function retryWithBackoff(fn, retries = 3, delay = 1000) {
  for (let i = 0; i <= retries; i++) {
    try {
      return await fn();
    } catch (err) {
      if (i === retries) throw err;  // final attempt failed, give up
      const waitTime = delay * Math.pow(2, i);
      console.log(`Attempt ${i + 1} failed. Retrying in ${waitTime}ms...`);
      await new Promise(r => setTimeout(r, waitTime));
    }
  }
}

// --- runnable demo ---
let callCount = 0;

async function flakyApi() {
  callCount++;
  if (callCount < 3) {
    throw new Error(`Fail #${callCount}`);
  }
  return 'success';
}

retryWithBackoff(flakyApi, 4, 500)
  .then(console.log)   // "success" (on 3rd attempt)
  .catch(console.error);

// Timeline:
// Attempt 1 fails  → wait 500ms   (500 * 2^0)
// Attempt 2 fails  → wait 1000ms  (500 * 2^1)
// Attempt 3 succeeds → returns "success"
```

**Backoff schedule (delay = 1000):**

| Attempt | Wait before retry |
|---------|-------------------|
| 1       | 1000ms (1s)       |
| 2       | 2000ms (2s)       |
| 3       | 4000ms (4s)       |
| 4       | 8000ms (8s)       |

The formula is `delay * 2^i` where `i` is the zero-based attempt index.

---

### 8. Promise Timeout Wrapper

**The problem:** A promise may hang forever. You want to reject it after a deadline.

```js
function withTimeout(promise, ms) {
  const timeout = new Promise((_, reject) =>
    setTimeout(() => reject(new Error(`Timeout after ${ms}ms`)), ms)
  );
  return Promise.race([promise, timeout]);
}

// --- runnable demo ---
function slowOperation() {
  return new Promise(resolve => setTimeout(() => resolve('done'), 5000));
}

withTimeout(slowOperation(), 2000)
  .then(console.log)
  .catch(console.error);  // Error: Timeout after 2000ms
```

**How `Promise.race` works:** It resolves/rejects with whichever promise settles first. If the timeout fires before the real promise, we get a rejection.

**Improved version with cleanup (prevents timer leak):**

```js
function withTimeout(promise, ms) {
  let timerId;
  const timeout = new Promise((_, reject) => {
    timerId = setTimeout(() => reject(new Error(`Timeout after ${ms}ms`)), ms);
  });

  return Promise.race([promise, timeout]).finally(() => {
    clearTimeout(timerId);  // clean up timer if promise wins the race
  });
}
```

---

### 9. Handling Partial Failures -- Promise.allSettled

**The problem:** You run multiple tasks in parallel. Some may fail, but you still want the results of the ones that succeeded. `Promise.all` rejects on the **first** failure. `Promise.allSettled` **never rejects** -- it waits for all to settle.

**BAD -- Promise.all loses successful results on any failure**

```js
const urls = ['/api/a', '/api/b', '/api/c'];

try {
  // If /api/b fails, we lose results from /api/a and /api/c
  const results = await Promise.all(urls.map(url => fetch(url)));
} catch (err) {
  console.log('One failed, all lost:', err.message);
}
```

**GOOD -- Promise.allSettled keeps everything**

```js
const urls = ['/api/a', '/api/b', '/api/c'];

const results = await Promise.allSettled(urls.map(url => fetch(url)));

// results is an array of objects:
// { status: 'fulfilled', value: Response }
// { status: 'rejected',  reason: Error   }

const successes = results.filter(r => r.status === 'fulfilled').map(r => r.value);
const failures  = results.filter(r => r.status === 'rejected').map(r => r.reason);

console.log(`${successes.length} succeeded, ${failures.length} failed`);
```

**Side-by-side comparison:**

| Method                | On first rejection       | Return value                          |
|-----------------------|--------------------------|---------------------------------------|
| `Promise.all`         | Rejects immediately      | Single rejected reason                |
| `Promise.allSettled`  | Keeps going              | Array of `{status, value/reason}`     |
| `Promise.any`         | Keeps going              | First fulfilled value (or AggregateError if all fail) |
| `Promise.race`        | Settles with first result| Value/reason of whichever settles first|

---

## Part C: Common Async Mistakes

---

### 10. Forgetting to await -- Getting a Promise Object Instead of Its Value

**BAD:**

```js
async function getUsername() {
  const user = fetch('/api/user').then(r => r.json());
  console.log(user.name);  // undefined — user is a Promise, not the data
}
```

**GOOD:**

```js
async function getUsername() {
  const user = await fetch('/api/user').then(r => r.json());
  console.log(user.name);  // "Alice" — properly awaited
}
```

**How to spot it:** If you `console.log` a value and see `Promise { <pending> }`, you forgot `await`.

---

### 11. await Inside forEach -- Does Not Wait

**BAD:**

```js
async function processAll(items) {
  items.forEach(async (item) => {
    await processItem(item);  // each callback awaits internally...
  });
  console.log('All done!');   // ...but this runs BEFORE any item is processed
}
```

`forEach` calls every callback but does NOT await the promises they return. All items start processing at once, and "All done!" prints immediately.

**GOOD (sequential):**

```js
async function processAll(items) {
  for (const item of items) {
    await processItem(item);
  }
  console.log('All done!');  // truly all done
}
```

**GOOD (parallel):**

```js
async function processAll(items) {
  await Promise.all(items.map(item => processItem(item)));
  console.log('All done!');  // truly all done
}
```

---

### 12. Unhandled Promise Rejection -- Silent Failures

**BAD:**

```js
async function riskyOperation() {
  throw new Error('something broke');
}

riskyOperation();  // no .catch, no try/catch — error is silently swallowed
// In Node.js, this triggers UnhandledPromiseRejectionWarning
// In modern Node.js (v15+), this CRASHES the process
```

**GOOD:**

```js
// Option A: .catch
riskyOperation().catch(err => console.error('Handled:', err.message));

// Option B: try/catch in an async wrapper
(async () => {
  try {
    await riskyOperation();
  } catch (err) {
    console.error('Handled:', err.message);
  }
})();

// Option C: global safety net (last resort, not a substitute)
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled rejection:', reason);
});
```

---

### 13. Creating Promises but Not Returning Them in a Chain

**BAD:**

```js
function fetchAndSave(url) {
  return fetch(url)
    .then(res => res.json())
    .then(data => {
      saveToDatabase(data);  // returns a promise, but we don't return it!
    });
}

// The caller thinks it's done when .then resolves,
// but the database save may still be in progress (or may fail silently).
```

**GOOD:**

```js
function fetchAndSave(url) {
  return fetch(url)
    .then(res => res.json())
    .then(data => {
      return saveToDatabase(data);  // return the promise!
    });
}

// Now the caller's .then/.catch properly waits for the save to complete.
```

**Rule:** Inside `.then()`, if you call an async function, always `return` it. Otherwise the chain cannot track it.

---

## Exam-Style Problems

---

### Problem 1: Implement a Task Runner with Concurrency Limit

**Question:** Write a function `runTasks(tasks, concurrency)` that takes an array of task functions (each returns a promise) and a concurrency limit. It should return a promise that resolves to an array of results in the same order as the input tasks. At most `concurrency` tasks should run at any time.

**Solution:**

```js
async function runTasks(tasks, concurrency) {
  const results = [];
  const executing = new Set();

  for (let i = 0; i < tasks.length; i++) {
    const p = tasks[i]().then(val => {
      results[i] = val;
      executing.delete(p);
    });
    executing.add(p);

    if (executing.size >= concurrency) {
      await Promise.race(executing);
    }
  }

  await Promise.all(executing);
  return results;
}

// --- test ---
const makeTasks = () => [
  () => new Promise(r => setTimeout(() => r('A'), 300)),
  () => new Promise(r => setTimeout(() => r('B'), 100)),
  () => new Promise(r => setTimeout(() => r('C'), 200)),
  () => new Promise(r => setTimeout(() => r('D'), 150)),
  () => new Promise(r => setTimeout(() => r('E'), 50)),
];

runTasks(makeTasks(), 2).then(console.log);
// ['A', 'B', 'C', 'D', 'E']
```

---

### Problem 2: Fetch with Retry and Timeout

**Question:** Write a function `robustFetch(url, options)` where `options = { retries, timeout, backoff }`. It should retry on failure with exponential backoff, and each individual attempt should be aborted if it exceeds the timeout.

**Solution:**

```js
async function robustFetch(url, { retries = 3, timeout = 5000, backoff = 1000 } = {}) {
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      const result = await withTimeout(fetch(url), timeout);
      if (!result.ok) throw new Error(`HTTP ${result.status}`);
      return await result.json();
    } catch (err) {
      console.log(`Attempt ${attempt + 1} failed: ${err.message}`);
      if (attempt === retries) throw err;
      await new Promise(r => setTimeout(r, backoff * Math.pow(2, attempt)));
    }
  }
}

function withTimeout(promise, ms) {
  let timerId;
  const timeout = new Promise((_, reject) => {
    timerId = setTimeout(() => reject(new Error(`Timeout after ${ms}ms`)), ms);
  });
  return Promise.race([promise, timeout]).finally(() => clearTimeout(timerId));
}

// Usage:
// robustFetch('/api/data', { retries: 3, timeout: 2000, backoff: 500 })
//   .then(data => console.log(data))
//   .catch(err => console.error('All attempts failed:', err));
```

---

### Problem 3: Process Items with Partial Failure Reporting

**Question:** Write a function `processItems(items, processFn)` that processes all items in parallel. It should never reject. Instead, it returns `{ successes: [...], failures: [...] }` where each failure includes the item and the error message.

**Solution:**

```js
async function processItems(items, processFn) {
  const results = await Promise.allSettled(
    items.map(item =>
      processFn(item).then(value => ({ item, value }))
    )
  );

  const successes = [];
  const failures = [];

  for (const result of results) {
    if (result.status === 'fulfilled') {
      successes.push(result.value);
    } else {
      // We need the item info in the rejection path too.
      // Since the rejection doesn't carry the item by default,
      // we handle it differently:
      failures.push({ reason: result.reason.message });
    }
  }

  return { successes, failures };
}

// Better version that preserves item info even on failure:
async function processItemsV2(items, processFn) {
  const results = await Promise.allSettled(
    items.map(async (item) => {
      try {
        const value = await processFn(item);
        return { item, value };
      } catch (err) {
        throw { item, error: err.message };  // carry item in rejection
      }
    })
  );

  const successes = results
    .filter(r => r.status === 'fulfilled')
    .map(r => r.value);

  const failures = results
    .filter(r => r.status === 'rejected')
    .map(r => r.reason);

  return { successes, failures };
}

// --- test ---
async function unreliableProcess(n) {
  if (n % 2 === 0) throw new Error(`${n} is even`);
  return n * 10;
}

processItemsV2([1, 2, 3, 4, 5], unreliableProcess).then(console.log);
// {
//   successes: [ {item:1, value:10}, {item:3, value:30}, {item:5, value:50} ],
//   failures:  [ {item:2, error:'2 is even'}, {item:4, error:'4 is even'} ]
// }
```

---

## Exam Tips

- **"Run tasks sequentially"** means `for...of` with `await`, never `forEach`.
- **"Run tasks in parallel"** means `Promise.all` with `.map`.
- **"Run at most N at a time"** means a promise pool -- use the `Set` + `Promise.race` pattern.
- **`Promise.all` vs `Promise.allSettled`:** If the question says "some tasks may fail" or "partial results," use `allSettled`.
- **`Promise.race`** is the building block for timeouts and concurrency limiters.
- **Exponential backoff formula:** `delay * 2^attempt` -- memorize this.
- **The `.catch(() => {})` trick** in the sequential queue prevents a single failure from breaking the chain.
- **Always return promises** inside `.then()` callbacks -- the most common chain bug.
- **`finally` runs regardless of success or failure** -- use it for cleanup (clearing timers, closing connections).
- **If an exam question says "implement without using Promise.all"**, use the callback-based pool pattern with `active` counter and `runNext()`.

---

## Quick Revision Table

| Pattern | When to Use | Key API / Technique | Gotcha |
|---|---|---|---|
| **Sequential** | Tasks depend on each other or order matters | `for...of` + `await` | `forEach` with `await` does NOT work sequentially |
| **Parallel** | Independent tasks, maximize speed | `Promise.all` + `.map` | One rejection rejects everything |
| **Promise Pool** | Too many tasks to run at once (rate limits) | `Set` + `Promise.race` or `active` counter | Must track indices to preserve result order |
| **Sequential Queue** | Tasks arrive dynamically, must run one at a time | Chain with `tail.then(...)` | Must `.catch(() => {})` on tail to prevent chain breakage |
| **try/catch** | Standard async error handling | `try { await ... } catch (e) {}` | Forgetting `await` means catch won't fire |
| **Promise.catch** | Error handling in `.then()` chains | `.catch(err => ...)` | Returning a value from `.catch` recovers the chain |
| **Retry + Backoff** | Transient failures (network, rate limits) | `delay * 2^i` inside a for loop | Must re-throw on final attempt |
| **Timeout Wrapper** | Prevent hanging promises | `Promise.race([promise, timeout])` | Clear the timer in `.finally()` to avoid leaks |
| **Partial Failures** | Some tasks can fail and that is acceptable | `Promise.allSettled` | Returns `{status, value/reason}` objects, never rejects |
| **Unhandled Rejection** | Every async call in your codebase | Always `.catch()` or `try/catch` | Node.js v15+ crashes on unhandled rejections |

---

| Promise Static Method | Resolves When | Rejects When |
|---|---|---|
| `Promise.all(arr)` | All fulfill | Any one rejects |
| `Promise.allSettled(arr)` | All settle (fulfill or reject) | Never |
| `Promise.race(arr)` | First settles (fulfill or reject) | First settles with rejection |
| `Promise.any(arr)` | First fulfills | All reject (`AggregateError`) |
