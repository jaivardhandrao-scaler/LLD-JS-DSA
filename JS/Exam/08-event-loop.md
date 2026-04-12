# Event Loop & JS Runtime Deep Dive (Class 9)

---

## Prediction Puzzle -- Try Before You Read

```js
console.log('A');
setTimeout(() => console.log('B'), 0);
Promise.resolve().then(() => console.log('C'));
console.log('D');
```

**Output:**

```
A
D
C
B
```

If you got that wrong, this entire document exists for you. If you got it right, this document will make sure you can handle the harder versions on the exam.

**Step-by-step walkthrough:**

| Step | What Happens | Call Stack | Microtask Queue | Macrotask Queue | Console |
|------|-------------|------------|-----------------|-----------------|---------|
| 1 | `console.log('A')` executes synchronously | `log('A')` | (empty) | (empty) | A |
| 2 | `setTimeout(cb, 0)` registers callback with Web API, which places it in the macrotask queue | `setTimeout(...)` | (empty) | `cb => log('B')` | A |
| 3 | `Promise.resolve().then(cb)` -- promise is already resolved, so `.then` callback goes straight to microtask queue | `Promise.then(...)` | `cb => log('C')` | `cb => log('B')` | A |
| 4 | `console.log('D')` executes synchronously | `log('D')` | `cb => log('C')` | `cb => log('B')` | A, D |
| 5 | **Call stack is now empty.** Event loop checks microtask queue first. Runs `log('C')`. | `log('C')` | (empty) | `cb => log('B')` | A, D, C |
| 6 | Microtask queue is empty. Event loop picks one macrotask. Runs `log('B')`. | `log('B')` | (empty) | (empty) | A, D, C, B |

**The key insight:** `setTimeout(..., 0)` does NOT mean "run immediately." It means "run as soon as possible, but only after all synchronous code AND all microtasks are done."

---

## JS Runtime Architecture

JavaScript is **single-threaded**. It can do exactly one thing at a time. So how does it handle async operations? Through a set of cooperating components:

### The Five Components

**1. Call Stack**
- LIFO (Last In, First Out) data structure
- Every function call pushes a frame onto the stack
- When a function returns, its frame is popped
- **Only one call stack** -- this is what "single-threaded" means
- If the stack is not empty, nothing else can run

**2. Web APIs / Node APIs**
- These are NOT part of the JS engine (V8) itself
- They are provided by the **browser** or **Node.js runtime**
- Examples: `setTimeout`, `setInterval`, `fetch`, DOM event listeners, `XMLHttpRequest`
- When you call `setTimeout(cb, 1000)`, the JS engine hands the timer off to the Web API. The Web API waits 1000ms, then pushes `cb` into the macrotask queue.

**3. Macrotask Queue (Callback Queue)**
- Holds callbacks from: `setTimeout`, `setInterval`, `setImmediate` (Node), I/O operations, UI rendering events
- The event loop picks **ONE** macrotask at a time

**4. Microtask Queue**
- Holds callbacks from: `Promise.then/catch/finally`, `queueMicrotask()`, `MutationObserver`, `async/await` continuations
- **ALL** microtasks drain before the event loop touches any macrotask
- Microtasks can add more microtasks, and those also run before any macrotask

**5. Event Loop**
- The orchestrator that coordinates everything
- It is an infinite loop that checks: "Is the call stack empty? If yes, what do I run next?"

### Visual Model

```
 ┌──────────────────────────────┐
 │         CALL STACK           │  <-- JS engine executes code here
 │  (one function at a time)    │
 └──────────┬───────────────────┘
            │ (when empty)
            v
 ┌──────────────────────────────┐
 │       EVENT LOOP             │  <-- checks queues in order
 │                              │
 │  1. Drain ALL microtasks     │
 │  2. Pick ONE macrotask       │
 │  3. Repeat                   │
 └──────┬───────────┬───────────┘
        │           │
        v           v
 ┌─────────────┐ ┌─────────────────┐
 │ MICROTASK Q │ │  MACROTASK Q    │
 │ (Promises)  │ │ (setTimeout...) │
 └─────────────┘ └─────────────────┘
        ^               ^
        │               │
 ┌──────────────────────────────┐
 │     WEB APIs / NODE APIs     │
 │  (timers, network, DOM...)   │
 └──────────────────────────────┘
```

---

## Event Loop Cycle -- The Exact Algorithm

Memorize this. It is the algorithm you use to solve every prediction puzzle on the exam.

**1.** Execute all synchronous code on the call stack (the script itself, or the body of a callback that was just picked up).

**2.** When the call stack is empty, **drain the entire microtask queue**:
   - Pick the oldest microtask, push it onto the call stack, execute it.
   - If that microtask added MORE microtasks, those go to the back of the microtask queue.
   - Keep going until the microtask queue is completely empty.

**3.** Pick **ONE** macrotask from the macrotask queue (the oldest one). Push it onto the call stack. Execute it.

**4.** When that macrotask finishes and the call stack is empty, go back to step 2 (drain all microtasks again).

**5.** Repeat forever.

**The critical rule:** Between any two macrotasks, ALL microtasks are drained. Microtasks always have priority.

---

## Microtasks vs Macrotasks

### Comparison Table

| | **Microtasks** | **Macrotasks** |
|---|---|---|
| **Sources** | `Promise.then/catch/finally`, `queueMicrotask()`, `MutationObserver`, `async/await` (code after `await`) | `setTimeout`, `setInterval`, `setImmediate` (Node), I/O callbacks, UI rendering |
| **Queue behavior** | ALL drain before any macrotask | ONE picked per event loop cycle |
| **Priority** | Higher -- always runs first | Lower -- waits for microtask queue to empty |
| **Can starve macrotasks?** | Yes -- if microtasks keep adding microtasks, macrotasks never run | No -- macrotasks cannot starve microtasks |
| **Added by** | JS engine (V8) | Browser / Node runtime (Web APIs) |

### BAD Mental Model vs GOOD Mental Model

**BAD:** "setTimeout(fn, 0) runs immediately after the current line."
- Wrong. It runs after ALL synchronous code AND all microtasks.

**BAD:** "Promises are asynchronous so they run later."
- Partially wrong. The `.then` callback is asynchronous, but it runs BEFORE any setTimeout callback, because it is a microtask.

**GOOD:** "When the call stack empties, drain all microtasks, then pick one macrotask. Repeat."
- This is the correct and complete model.

---

## Prediction Puzzles

### Puzzle 2: Mixed Microtasks and Macrotasks

```js
setTimeout(() => console.log(1), 0);
Promise.resolve().then(() => console.log(2));
Promise.resolve().then(() => {
  console.log(3);
  setTimeout(() => console.log(4), 0);
});
console.log(5);
```

**Output:**

```
5
2
3
1
4
```

**Walkthrough:**

| Phase | Action | Microtask Q | Macrotask Q | Console |
|-------|--------|-------------|-------------|---------|
| Sync | `setTimeout(cb1, 0)` -- registers cb1 as macrotask | (empty) | `cb1=>log(1)` | |
| Sync | `Promise.resolve().then(cb2)` -- cb2 to microtask queue | `cb2=>log(2)` | `cb1=>log(1)` | |
| Sync | `Promise.resolve().then(cb3)` -- cb3 to microtask queue | `cb2`, `cb3` | `cb1=>log(1)` | |
| Sync | `console.log(5)` -- runs immediately | `cb2`, `cb3` | `cb1=>log(1)` | 5 |
| **Stack empty** | Drain microtasks: run cb2 => `log(2)` | `cb3` | `cb1` | 5, 2 |
| Microtask | Run cb3 => `log(3)`, then `setTimeout(cb4, 0)` adds cb4 to macrotask queue | (empty) | `cb1`, `cb4` | 5, 2, 3 |
| **Microtasks drained** | Pick one macrotask: cb1 => `log(1)` | (empty) | `cb4` | 5, 2, 3, 1 |
| **Stack empty** | No microtasks. Pick one macrotask: cb4 => `log(4)` | (empty) | (empty) | 5, 2, 3, 1, 4 |

**Key takeaway:** The `setTimeout` inside the promise callback (cb3) does not run until after ALL currently queued microtasks finish AND the already-queued macrotask (cb1) runs first, because cb1 was queued before cb4.

---

### Puzzle 3: Nested Promises

```js
Promise.resolve().then(() => {
  console.log(1);
  return Promise.resolve(2);
}).then(val => console.log(val));

Promise.resolve().then(() => console.log(3));
```

**Output:**

```
1
3
2
```

**Walkthrough:**

This one is tricky. When a `.then` handler **returns a Promise**, the resolution takes extra microtask ticks.

| Phase | Action | Microtask Q | Console |
|-------|--------|-------------|---------|
| Sync | Chain A: `.then(cbA1)` queued | `cbA1` | |
| Sync | Chain B: `.then(cbB)` queued | `cbA1`, `cbB` | |
| **Stack empty** | Drain microtasks: run cbA1 => `log(1)`, returns `Promise.resolve(2)` | `cbB`, (internal resolve tick) | 1 |
| Microtask | Run cbB => `log(3)` | (internal resolve tick) | 1, 3 |
| Microtask | Internal tick resolves the returned promise, queues cbA2 | `cbA2` | 1, 3 |
| Microtask | Run cbA2 => `log(2)` | (empty) | 1, 3, 2 |

**Key takeaway:** Returning `Promise.resolve(value)` from a `.then` handler introduces extra microtask ticks before the next `.then` in the chain can run. This gives other already-queued microtasks a chance to execute first. If you had returned the plain value `2` instead, the output would be `1, 2, 3`.

---

### Puzzle 4: async/await

```js
async function foo() {
  console.log(1);
  await Promise.resolve();
  console.log(2);
}

console.log(3);
foo();
console.log(4);
```

**Output:**

```
3
1
4
2
```

**Walkthrough:**

The key is understanding that `await` splits the function into "before" and "after" parts:
- Everything **before** `await` runs **synchronously** (including the `await` expression itself).
- Everything **after** `await` becomes a microtask (equivalent to a `.then` callback).

| Phase | Action | Microtask Q | Console |
|-------|--------|-------------|---------|
| Sync | `console.log(3)` | (empty) | 3 |
| Sync | `foo()` is called. Inside foo: `console.log(1)` runs synchronously. | (empty) | 3, 1 |
| Sync | `await Promise.resolve()` -- pauses foo. The "rest of foo" (`log(2)`) goes to microtask queue. Control returns to the caller. | `rest-of-foo` | 3, 1 |
| Sync | `console.log(4)` | `rest-of-foo` | 3, 1, 4 |
| **Stack empty** | Drain microtasks: resume foo => `log(2)` | (empty) | 3, 1, 4, 2 |

**Key takeaway:** `await` does NOT make the entire function asynchronous. Code before `await` is synchronous. Code after `await` is a microtask.

**Equivalent rewrite (what the engine actually does):**

```js
function foo() {
  console.log(1);
  Promise.resolve().then(() => {
    console.log(2);
  });
}
```

---

### Puzzle 5: Complex Mix

```js
console.log('start');

setTimeout(() => console.log('timeout1'), 0);

Promise.resolve('promise1').then(console.log);

setTimeout(() => {
  console.log('timeout2');
  Promise.resolve('promise2').then(console.log);
}, 0);

console.log('end');
```

**Output:**

```
start
end
promise1
timeout1
timeout2
promise2
```

**Walkthrough:**

| Phase | Action | Microtask Q | Macrotask Q | Console |
|-------|--------|-------------|-------------|---------|
| Sync | `log('start')` | (empty) | (empty) | start |
| Sync | `setTimeout(cb1, 0)` -- cb1 to macrotask | (empty) | `cb1` | start |
| Sync | `Promise.resolve('promise1').then(console.log)` -- to microtask | `log('promise1')` | `cb1` | start |
| Sync | `setTimeout(cb2, 0)` -- cb2 to macrotask | `log('promise1')` | `cb1`, `cb2` | start |
| Sync | `log('end')` | `log('promise1')` | `cb1`, `cb2` | start, end |
| **Stack empty** | Drain microtasks: `log('promise1')` | (empty) | `cb1`, `cb2` | start, end, promise1 |
| **Microtasks drained** | Pick one macrotask: cb1 => `log('timeout1')` | (empty) | `cb2` | start, end, promise1, timeout1 |
| **Stack empty** | No microtasks. Pick one macrotask: cb2 => `log('timeout2')`, then `Promise.resolve('promise2').then(console.log)` adds to microtask queue | `log('promise2')` | (empty) | ..., timeout1, timeout2 |
| **Stack empty** | Drain microtasks: `log('promise2')` | (empty) | (empty) | ..., timeout2, promise2 |

**Key takeaway:** `promise2` prints immediately after `timeout2` (not after `timeout1`) because it is created as a microtask DURING the `timeout2` macrotask. When `timeout2` finishes, the event loop drains all microtasks before doing anything else.

---

### Puzzle 6: queueMicrotask

```js
console.log(1);

queueMicrotask(() => {
  console.log(2);
  queueMicrotask(() => console.log(3));
});

Promise.resolve().then(() => console.log(4));

console.log(5);
```

**Output:**

```
1
5
2
4
3
```

**Walkthrough:**

| Phase | Action | Microtask Q | Console |
|-------|--------|-------------|---------|
| Sync | `log(1)` | (empty) | 1 |
| Sync | `queueMicrotask(cb1)` -- cb1 to microtask queue | `cb1` | 1 |
| Sync | `Promise.resolve().then(cb2)` -- cb2 to microtask queue | `cb1`, `cb2` | 1 |
| Sync | `log(5)` | `cb1`, `cb2` | 1, 5 |
| **Stack empty** | Drain microtasks: run cb1 => `log(2)`, then `queueMicrotask(cb3)` adds cb3 | `cb2`, `cb3` | 1, 5, 2 |
| Microtask | Run cb2 => `log(4)` | `cb3` | 1, 5, 2, 4 |
| Microtask | Run cb3 => `log(3)` | (empty) | 1, 5, 2, 4, 3 |

**Key takeaway:** `queueMicrotask` and `Promise.then` both add to the same microtask queue. Microtasks added during microtask draining also run before any macrotask. The order is FIFO within the microtask queue.

---

### Puzzle 7 (Bonus -- Hard): Nested async/await

```js
async function first() {
  console.log(1);
  await second();
  console.log(2);
}

async function second() {
  console.log(3);
  await Promise.resolve();
  console.log(4);
}

console.log(5);
first();
console.log(6);
```

**Output:**

```
5
1
3
6
4
2
```

**Walkthrough:**

| Phase | Action | Microtask Q | Console |
|-------|--------|-------------|---------|
| Sync | `log(5)` | (empty) | 5 |
| Sync | Call `first()`. Inside first: `log(1)`. | (empty) | 5, 1 |
| Sync | `await second()` calls `second()`. Inside second: `log(3)`. | (empty) | 5, 1, 3 |
| Sync | `await Promise.resolve()` inside second: pauses second. "rest of second" (`log(4)`) goes to microtask queue. Control returns up through first (which is also paused at its `await`), back to the global scope. | `rest-of-second` | 5, 1, 3 |
| Sync | `log(6)` | `rest-of-second` | 5, 1, 3, 6 |
| **Stack empty** | Drain microtasks: resume second => `log(4)`. second finishes. Now the promise that first was awaiting resolves, so "rest of first" (`log(2)`) goes to microtask queue. | `rest-of-first` | 5, 1, 3, 6, 4 |
| Microtask | Resume first => `log(2)` | (empty) | 5, 1, 3, 6, 4, 2 |

**Key takeaway:** When function A awaits function B, and function B also has an await, control returns all the way back to the caller. Function B's continuation runs first (as a microtask), and only after B fully completes does A's continuation get queued.

---

## Starvation

**Starvation** occurs when microtasks continuously add more microtasks, preventing the event loop from ever reaching the macrotask queue.

```js
// BAD -- this starves the macrotask queue (and freezes the browser)
function starve() {
  Promise.resolve().then(starve);
}
starve();

// This setTimeout callback will NEVER run:
setTimeout(() => console.log('I will never print'), 0);
```

**Why it happens:** The event loop drains ALL microtasks before picking a macrotask. If microtasks keep adding more microtasks, the microtask queue never empties, so the event loop never moves on.

**Practical consequence:** The browser cannot repaint the UI (rendering is a macrotask-level operation), so the page freezes.

**How to avoid it:** If you need a recursive asynchronous loop, use `setTimeout` instead of promises to give the event loop breathing room:

```js
// GOOD -- uses macrotask so other things can run between iterations
function loop() {
  // do work...
  setTimeout(loop, 0);
}
loop();
```

---

## Blocking the Event Loop

Because JavaScript is single-threaded, any long-running synchronous operation blocks everything:

```js
// BAD -- blocks for ~5 seconds, nothing else can run
function heavyComputation() {
  const start = Date.now();
  while (Date.now() - start < 5000) {
    // spinning...
  }
}

heavyComputation();
// These are all stuck waiting:
// - setTimeout callbacks
// - Promise callbacks
// - Click handlers
// - UI rendering
```

**The event loop cannot check the queues while the call stack is occupied.** If synchronous code takes 5 seconds, nothing async can run for 5 seconds.

**Solutions:**
- Break work into chunks using `setTimeout` or `requestAnimationFrame`
- Use Web Workers for CPU-intensive work (runs on a separate thread)
- Use async I/O (never do synchronous network requests)

---

## async/await and the Event Loop

`async/await` is syntactic sugar for Promises. Understanding the transformation is critical for prediction puzzles.

### The Transformation Rule

```js
// This async function:
async function example() {
  console.log('before');
  const result = await somePromise;
  console.log('after', result);
  return result;
}

// Is equivalent to:
function example() {
  console.log('before');              // runs synchronously
  return somePromise.then(result => { // everything after await
    console.log('after', result);     //   becomes a .then callback
    return result;                    //   (i.e., a microtask)
  });
}
```

### Rules to Remember

- **Code before `await`:** runs synchronously, stays on the call stack.
- **The `await` expression itself:** evaluates synchronously (the promise is created/accessed synchronously).
- **Code after `await`:** goes to the microtask queue. It will run when the awaited promise resolves AND the call stack is empty.
- **`await` pauses only the async function, not the entire program.** Execution returns to the caller.

### Common Trap

```js
async function trap() {
  console.log('A');
  await null;          // even awaiting a non-promise value creates a microtask pause
  console.log('B');
}

trap();
console.log('C');
```

**Output: A, C, B**

`await null` is equivalent to `await Promise.resolve(null)`. It still causes the rest of the function to be deferred to the microtask queue.

---

## Exam Tips

### The Algorithm for Solving Prediction Puzzles

Follow these steps mechanically. Do not guess.

**Step 1:** Read through ALL the code and execute only the synchronous parts. Write down console output as you go.

**Step 2:** As you encounter async operations, put their callbacks in the correct queue:
- `setTimeout` / `setInterval` callbacks --> **macrotask queue**
- `.then` / `.catch` / `.finally` callbacks --> **microtask queue** (only if the promise is already resolved)
- Code after `await` --> **microtask queue** (once the awaited value resolves)

**Step 3:** When you reach the end of synchronous code (call stack is empty):
- Drain the **entire** microtask queue, in FIFO order.
- If any microtask adds more microtasks, drain those too before moving on.

**Step 4:** Pick **one** macrotask. Execute it (write down its console output).

**Step 5:** After the macrotask completes, go back to Step 3 (drain microtasks again).

**Step 6:** Repeat until all queues are empty.

### Common Exam Mistakes to Avoid

- **Forgetting that `setTimeout(fn, 0)` is still a macrotask.** It runs AFTER all microtasks, not "immediately."
- **Treating `await` as blocking.** It only pauses the async function, not the outer code. The caller continues.
- **Forgetting that ALL microtasks drain before ONE macrotask.** Not one-for-one.
- **Ignoring that returning a promise from `.then()` adds extra microtask ticks.** `return Promise.resolve(x)` is slower than `return x`.
- **Assuming `setTimeout` callbacks run in the order they were registered when they have different delays.** They run based on when their timer expires, but timers with the same delay generally maintain registration order.

### Quick Pattern Recognition

- See `console.log` outside any callback? --> **Runs first (synchronous).**
- See `.then(...)` on an already-resolved promise? --> **Microtask. Runs after sync, before timeouts.**
- See `setTimeout(..., 0)`? --> **Macrotask. Runs last (after all microtasks).**
- See `await`? --> **Split the function in two. Before = sync. After = microtask.**

---

## Quick Revision Table

| Concept | Key Point |
|---|---|
| **Call Stack** | Single-threaded, LIFO. Must be empty before event loop checks queues. |
| **Event Loop Order** | Sync code --> ALL microtasks --> ONE macrotask --> ALL microtasks --> repeat |
| **Microtask Sources** | `Promise.then/catch/finally`, `queueMicrotask()`, `MutationObserver`, code after `await` |
| **Macrotask Sources** | `setTimeout`, `setInterval`, `setImmediate` (Node), I/O, UI rendering |
| **Microtask Priority** | ALL microtasks drain before ANY macrotask runs |
| **Microtask during microtask** | Also runs before any macrotask (added to back of same queue) |
| **setTimeout(fn, 0)** | NOT immediate. Macrotask. Runs after sync + all microtasks. |
| **await** | Splits function: before = sync, after = microtask. Only pauses the async function, not the caller. |
| **await non-promise** | `await 5` is equivalent to `await Promise.resolve(5)` -- still creates a microtask pause |
| **return Promise.resolve(x) in .then** | Adds extra microtask tick(s) compared to `return x` |
| **Starvation** | Microtasks adding microtasks forever --> macrotasks never run --> UI freezes |
| **Blocking** | Long sync code blocks everything (event loop cannot check queues while stack is busy) |
| **Prediction puzzle strategy** | Execute sync first, fill queues, drain microtasks, pick one macrotask, drain microtasks, repeat |

---

## Practice These

After studying this topic, solve these coding problems:

**From practiceSheet.md:**
- P4 (Event loop output prediction)
- P5 (Implement async/await using generators)
- P6 (Sequential async task queue)
