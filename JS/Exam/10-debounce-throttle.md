# Debouncing and Throttling — Exam Study Guide (Class 11/12)

---

## Start Here: The Problem

```js
// BAD — fires an API call on EVERY keystroke
searchInput.addEventListener("input", (e) => {
  fetchResults(e.target.value); // user types "hello" → 5 API calls!
});

// GOOD — fires ONE API call after user stops typing for 300ms
searchInput.addEventListener("input", debounce((e) => {
  fetchResults(e.target.value); // user types "hello" → 1 API call
}, 300));
```

```js
// BAD — fires handler on EVERY scroll tick (hundreds per second)
window.addEventListener("scroll", () => {
  updatePosition(); // browser chokes, page lags
});

// GOOD — fires handler at most once every 200ms
window.addEventListener("scroll", throttle(() => {
  updatePosition(); // smooth, controlled updates
}, 200));
```

**Core idea:**
- **Debounce** = "Wait until the user STOPS doing something, then fire once."
- **Throttle** = "While the user keeps doing something, fire at a steady rate (once per interval)."

---

# Part A: Debounce

---

## 1. How Debounce Works

Imagine a door buzzer that only rings if nobody presses it again for 2 seconds:

```
User types:  H   e   l   l   o
Time (ms):   0  100 200 300 400
Timer:      set reset reset reset reset
Fires at:                          400 + 300ms = 700ms → ONE call with "Hello"
```

**Each new call resets the timer.** The function only executes when the calls stop coming for `delay` milliseconds.

---

## 2. Basic Debounce Implementation

```js
function debounce(fn, delay) {
  let timerId;                          // 1. Closure variable — persists across calls
  return function (...args) {           // 2. Return a wrapper function (closure)
    clearTimeout(timerId);              // 3. Cancel any previously scheduled call
    timerId = setTimeout(() => {        // 4. Schedule a new call after `delay` ms
      fn.apply(this, args);             // 5. Call original fn with correct `this` and args
    }, delay);
  };
}
```

**Line-by-line breakdown:**

| Line | What it does | Why it matters |
|------|-------------|----------------|
| `let timerId;` | Declares a variable in the closure scope | Shared across every call to the returned function. Persists because of closure. |
| `return function(...args)` | Returns a new function that wraps `fn` | This is the function the event listener actually calls. `...args` captures all arguments (like the event object). |
| `clearTimeout(timerId)` | Cancels the previous pending timer | This is the KEY line. Every new call cancels the previous scheduled execution. |
| `timerId = setTimeout(...)` | Starts a new timer for `delay` ms | If no new call comes in within `delay` ms, the callback fires. |
| `fn.apply(this, args)` | Calls the original function | `apply` preserves the `this` context and passes the captured arguments. |

**Usage:**

```js
const debouncedSearch = debounce((query) => {
  console.log("Searching for:", query);
  // fetch(`/api/search?q=${query}`);
}, 300);

// Simulating rapid typing
debouncedSearch("h");       // timer set → 300ms
debouncedSearch("he");      // timer RESET → 300ms
debouncedSearch("hel");     // timer RESET → 300ms
debouncedSearch("hell");    // timer RESET → 300ms
debouncedSearch("hello");   // timer RESET → 300ms
// 300ms of silence...
// Output: "Searching for: hello"  (fired ONCE)
```

---

## 3. Leading vs Trailing Debounce

### Trailing Debounce (Default — shown above)

Fires **AFTER** the delay, once calls stop coming.

```
Calls:    X  X  X  X  X  . . . [fires here]
Time:     0  1  2  3  4  5  6  7
```

### Leading Debounce

Fires on the **FIRST** call immediately, then ignores all subsequent calls until silence for `delay` ms.

```
Calls:    X  X  X  X  X  . . . 
Time:     0  1  2  3  4  5  6  7
Fires:    ^                        (fires immediately on first call)
```

**Leading debounce implementation:**

```js
function debounceLeading(fn, delay) {
  let timerId;
  return function (...args) {
    if (!timerId) {                     // First call — no timer running
      fn.apply(this, args);            // Execute immediately
    }
    clearTimeout(timerId);             // Reset the "cooldown" timer
    timerId = setTimeout(() => {
      timerId = null;                  // After silence, allow next leading call
    }, delay);
  };
}
```

**How it works:**
- First call: `timerId` is `undefined` (falsy) → function fires immediately
- Subsequent calls within `delay` ms: `timerId` exists (truthy) → function is skipped, timer is reset
- After `delay` ms of silence: timer callback sets `timerId = null` → next call will fire immediately again

### Leading + Trailing Debounce

Fires on the **FIRST** call AND **AFTER** the delay.

```
Calls:    X  X  X  X  X  . . . [fires]
Time:     0  1  2  3  4  5  6  7
Fires:    ^                      ^
```

```js
function debounceBoth(fn, delay) {
  let timerId;
  return function (...args) {
    if (!timerId) {
      fn.apply(this, args);             // Leading: fire on first call
    }
    clearTimeout(timerId);
    timerId = setTimeout(() => {
      timerId = null;
      fn.apply(this, args);             // Trailing: fire after silence
    }, delay);
  };
}
```

### Comparison Table

| Type | When it fires | Best for |
|------|--------------|----------|
| **Trailing** (default) | After `delay` ms of silence | Search input, resize |
| **Leading** | Immediately on first call | Button click protection |
| **Leading + Trailing** | First call AND after silence | Getting both immediate feedback and final value |

---

## 4. Cancel Support

Sometimes you need to cancel a pending debounced call (e.g., component unmounts).

```js
function debounce(fn, delay) {
  let timerId;

  function debounced(...args) {
    clearTimeout(timerId);
    timerId = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  }

  debounced.cancel = function () {
    clearTimeout(timerId);             // Kill the pending timer
    timerId = undefined;               // Reset state
  };

  return debounced;
}
```

**Usage:**

```js
const debouncedSave = debounce(saveData, 1000);

inputField.addEventListener("input", debouncedSave);

// User navigates away — cancel the pending save
cancelButton.addEventListener("click", () => {
  debouncedSave.cancel();
  console.log("Pending save cancelled");
});
```

---

## 5. Real-World Use Cases for Debounce

| Use Case | Why Debounce? | Typical Delay |
|----------|--------------|---------------|
| **Search-as-you-type** | Wait until user stops typing before hitting API | 300-500ms |
| **Window resize** | Wait until user finishes resizing to recalculate layout | 200-500ms |
| **Form validation** | Wait until user stops editing a field before validating | 300ms |
| **Auto-save** | Wait until user pauses editing to save draft | 1000-2000ms |
| **Scroll-to-search** | Wait until user stops scrolling to load section | 200ms |

---

# Part B: Throttle

---

## 6. How Throttle Works

Imagine a news ticker that updates at most once per second, no matter how fast news arrives:

```
Events:   X X X X X X X X X X X X
Time:     0 1 2 3 4 5 6 7 8 9 10 11
Fires:    ^       ^       ^        ^
          (every 4 units — throttled to once per interval)
```

**Key difference from debounce:** Throttle guarantees execution at a regular interval. Debounce keeps delaying until silence.

---

## 7. Basic Throttle Implementation (Timestamp-Based)

```js
function throttle(fn, interval) {
  let lastTime = 0;                      // 1. Track when fn last fired
  return function (...args) {            // 2. Return wrapper function
    const now = Date.now();              // 3. Current timestamp in ms
    if (now - lastTime >= interval) {    // 4. Has enough time passed?
      lastTime = now;                    // 5. Update last fire time
      fn.apply(this, args);             // 6. Call original function
    }
  };
}
```

**Line-by-line breakdown:**

| Line | What it does | Why it matters |
|------|-------------|----------------|
| `let lastTime = 0` | Initializes to 0 (epoch) | First call always passes the check since `Date.now() - 0` is huge. |
| `const now = Date.now()` | Gets current time in milliseconds | Used to compare against last execution time. |
| `if (now - lastTime >= interval)` | Checks if `interval` ms have passed since last call | This is the GATE — blocks calls that come too soon. |
| `lastTime = now` | Records when function last fired | Updates the reference point for the next comparison. |
| `fn.apply(this, args)` | Calls original with correct context | Same pattern as debounce — preserves `this` and arguments. |

**Usage:**

```js
const throttledScroll = throttle(() => {
  console.log("Scroll position:", window.scrollY);
}, 200);

window.addEventListener("scroll", throttledScroll);
// Scrolling rapidly → logs at most once every 200ms, not on every pixel
```

**Behavior with rapid calls:**

```js
const throttled = throttle((x) => console.log(x), 1000);

// Simulating rapid calls every 200ms
// t=0:    throttled("a") → "a" fires (1000ms have passed since epoch)
// t=200:  throttled("b") → BLOCKED (only 200ms since last fire)
// t=400:  throttled("c") → BLOCKED (only 400ms since last fire)
// t=600:  throttled("d") → BLOCKED (only 600ms since last fire)
// t=800:  throttled("e") → BLOCKED (only 800ms since last fire)
// t=1000: throttled("f") → "f" fires (1000ms have passed)
// t=1200: throttled("g") → BLOCKED
```

---

## 8. Alternative Throttle with setTimeout (Trailing Call)

The timestamp version above has a problem: it **drops the last call** if it falls between intervals. This version guarantees a trailing call:

```js
function throttle(fn, interval) {
  let timerId = null;                    // 1. Track if a cooldown timer is active
  return function (...args) {
    if (timerId) return;                 // 2. If timer running, skip this call
    fn.apply(this, args);               // 3. Execute immediately (leading call)
    timerId = setTimeout(() => {         // 4. Start cooldown timer
      timerId = null;                   // 5. After interval, allow next call
    }, interval);
  };
}
```

**How this differs from the timestamp version:**

| | Timestamp Version | setTimeout Version |
|---|---|---|
| **Leading call** | Yes (first call fires) | Yes (first call fires) |
| **Mechanism** | Compares `Date.now()` | Uses a boolean-like lock via `timerId` |
| **Trailing call** | No — last event may be lost | No in this version, but can be added (see below) |

### Throttle with Trailing Call (captures the last event)

```js
function throttle(fn, interval) {
  let timerId = null;
  let lastArgs = null;                   // Store the most recent arguments

  return function (...args) {
    if (timerId) {
      lastArgs = args;                   // Save latest args, don't execute
      return;
    }
    fn.apply(this, args);               // Execute leading call
    timerId = setTimeout(() => {
      timerId = null;
      if (lastArgs) {                    // If there were calls during cooldown
        fn.apply(this, lastArgs);       // Fire with the LAST set of arguments
        lastArgs = null;
      }
    }, interval);
  };
}
```

---

## 9. Leading vs Trailing Throttle

### Leading Throttle (default implementations above)

Fires **immediately** on first call, then blocks for `interval` ms.

```
Events:   X  X  X  X  |  X  X  X  X
Time:     0  1  2  3  4  5  6  7  8
Fires:    ^              ^
          (fires at start of each interval)
```

### Trailing Throttle

Fires at the **END** of each interval.

```
Events:   X  X  X  X  |  X  X  X  X
Time:     0  1  2  3  4  5  6  7  8
Fires:                 ^              ^
          (fires at end of each interval)
```

```js
function throttleTrailing(fn, interval) {
  let timerId = null;
  return function (...args) {
    if (timerId) return;
    timerId = setTimeout(() => {
      fn.apply(this, args);              // Fire at the END of the interval
      timerId = null;
    }, interval);
  };
}
```

**Notice the difference:** `fn.apply` is inside `setTimeout` (trailing) instead of before it (leading).

---

## 10. Real-World Use Cases for Throttle

| Use Case | Why Throttle? | Typical Interval |
|----------|--------------|-----------------|
| **Scroll handler** | Scroll fires hundreds of times/sec; limit to steady rate | 100-200ms |
| **Mouse move tracking** | `mousemove` fires extremely fast | 50-100ms |
| **API rate limiting** | Server allows max N requests per second | 1000ms |
| **Game loop input** | Process player input at fixed rate | 16ms (60fps) |
| **Button click** | Prevent double-submit of forms | 1000-2000ms |
| **Analytics events** | Don't flood analytics with every interaction | 500-1000ms |

---

# Part C: Debounce vs Throttle Comparison

---

## 11. Side-by-Side Comparison

| | **Debounce** | **Throttle** |
|---|---|---|
| **When it fires** | After `delay` ms of **silence** | At most once per `interval` ms |
| **Resets on new call?** | **Yes** — timer resets every time | **No** — timer is independent of calls |
| **During rapid calls** | Keeps delaying, fires **0 times** until calls stop | Fires at a **steady rate** throughout |
| **After rapid calls stop** | Fires **once** (trailing) | May or may not fire (depends on implementation) |
| **Guarantees execution during activity?** | **No** — can be starved indefinitely | **Yes** — fires at regular intervals |
| **Total fires for 10s of rapid input** | **1** (after input stops) | **~10s / interval** (e.g., 50 times at 200ms) |

### Timeline Visualization

**Scenario:** User clicks rapidly for 1 second, then stops. `delay/interval = 300ms`.

```
Clicks:         X  X  X  X  X  X  X  X  X  X  . . . . .
Time (ms):      0  100 200 300 400 500 600 700 800 900 1000 1100 1200 1300

DEBOUNCE (trailing, 300ms):
Fires:          .  .   .   .   .   .   .   .   .   .    .    .    .   FIRE
                                                                      ^ 1200ms
                (timer keeps resetting — fires 300ms AFTER last click)
                Total fires: 1

THROTTLE (leading, 300ms):
Fires:          F  .   .   F   .   .   F   .   .   F    .   .    .    .
                ^          ^          ^          ^
                0ms       300ms     600ms     900ms
                (fires immediately, then once every 300ms)
                Total fires: 4
```

---

## 12. When to Use Which

| Scenario | Use | Why |
|----------|-----|-----|
| **Search input / autocomplete** | Debounce | Wait for user to finish typing |
| **Window resize** | Debounce | Only care about the FINAL size |
| **Form field validation** | Debounce | Validate after user stops editing |
| **Auto-save** | Debounce | Save after user pauses |
| **Scroll event handler** | Throttle | Need steady updates while scrolling |
| **Mouse move / drag** | Throttle | Need regular position updates |
| **Button click / form submit** | Throttle | Prevent double-click, allow first click through |
| **API rate limiting** | Throttle | Must respect rate limits during continuous usage |
| **Infinite scroll (load more)** | Throttle | Check position regularly while scrolling |
| **Game input** | Throttle | Process at fixed frame rate |

**Quick rule of thumb:**
- **"Wait for it to stop"** → Debounce
- **"Do it regularly"** → Throttle

---

## 13. Async Debounce

When debouncing an `async` function (e.g., API call), you often want to return a Promise so the caller can `await` the result.

```js
function debounceAsync(fn, delay) {
  let timerId;
  let pendingResolve = null;

  return function (...args) {
    return new Promise((resolve, reject) => {
      clearTimeout(timerId);

      // If there's a previous pending promise, reject it (call was superseded)
      if (pendingResolve) {
        pendingResolve.reject(new Error("Debounced — call superseded"));
      }

      pendingResolve = { resolve, reject };

      timerId = setTimeout(async () => {
        try {
          const result = await fn.apply(this, args);
          pendingResolve.resolve(result);
        } catch (err) {
          pendingResolve.reject(err);
        } finally {
          pendingResolve = null;
        }
      }, delay);
    });
  };
}
```

**Usage:**

```js
const debouncedFetch = debounceAsync(async (query) => {
  const res = await fetch(`/api/search?q=${query}`);
  return res.json();
}, 300);

// In an async handler
try {
  const results = await debouncedFetch("hello");
  console.log(results);
} catch (err) {
  if (err.message.includes("superseded")) {
    // This call was replaced by a newer one — safe to ignore
  }
}
```

**Simpler version** (just cancels, no promise return):

```js
function debounceAsync(fn, delay) {
  let timerId;
  let abortController;

  return function (...args) {
    clearTimeout(timerId);
    if (abortController) abortController.abort();   // Cancel previous fetch

    abortController = new AbortController();
    const signal = abortController.signal;

    timerId = setTimeout(() => {
      fn.apply(this, [...args, signal]);             // Pass signal to fn
    }, delay);
  };
}

// Usage — fn receives the signal as last arg
const debouncedSearch = debounceAsync(async (query, signal) => {
  const res = await fetch(`/api/search?q=${query}`, { signal });
  const data = await res.json();
  renderResults(data);
}, 300);
```

---

# Exam-Style Coding Problems

---

## Problem 1: Implement Debounce from Scratch

**Question:** Write a `debounce(fn, delay)` function that:
- Returns a new function
- Only calls `fn` after `delay` ms have passed since the last invocation
- Preserves `this` context and arguments
- Has a `.cancel()` method

**Solution:**

```js
function debounce(fn, delay) {
  let timerId;

  function debounced(...args) {
    clearTimeout(timerId);
    timerId = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  }

  debounced.cancel = function () {
    clearTimeout(timerId);
    timerId = undefined;
  };

  return debounced;
}

// Test
let callCount = 0;
const increment = debounce(() => { callCount++; }, 100);

increment(); // starts timer
increment(); // resets timer
increment(); // resets timer
// After 100ms of silence → callCount === 1

// Test cancel
increment();
increment.cancel();
// After 100ms → callCount still === 1 (cancelled)
```

**Grading notes:**
- `clearTimeout` before `setTimeout` is essential (resets the timer)
- `fn.apply(this, args)` preserves context (not just `fn(...args)`)
- Closure over `timerId` is required

---

## Problem 2: Implement Throttle from Scratch

**Question:** Write a `throttle(fn, interval)` function that:
- Calls `fn` immediately on the first invocation
- Ensures `fn` is not called again until `interval` ms have passed
- Preserves `this` context and arguments

**Solution (Timestamp approach):**

```js
function throttle(fn, interval) {
  let lastTime = 0;

  return function (...args) {
    const now = Date.now();
    if (now - lastTime >= interval) {
      lastTime = now;
      fn.apply(this, args);
    }
  };
}

// Test
let log = [];
const throttled = throttle((val) => log.push(val), 1000);

// t=0ms
throttled("a");  // fires → log = ["a"]
// t=200ms
throttled("b");  // blocked → log = ["a"]
// t=500ms
throttled("c");  // blocked → log = ["a"]
// t=1000ms
throttled("d");  // fires → log = ["a", "d"]
// t=1100ms
throttled("e");  // blocked → log = ["a", "d"]
```

**Solution (setTimeout approach):**

```js
function throttle(fn, interval) {
  let isThrottled = false;

  return function (...args) {
    if (isThrottled) return;
    fn.apply(this, args);
    isThrottled = true;
    setTimeout(() => {
      isThrottled = false;
    }, interval);
  };
}
```

**Grading notes:**
- Either approach is valid
- `lastTime = 0` ensures first call always fires
- Must use `fn.apply(this, args)`, not just `fn()`

---

## Problem 3: Implement Debounce with Leading Option

**Question:** Write `debounce(fn, delay, options)` where `options.leading` (boolean) controls whether the function fires immediately on the first call.
- `{ leading: false }` (default) = trailing debounce (fires after silence)
- `{ leading: true }` = fires immediately, then waits for silence before allowing again

**Solution:**

```js
function debounce(fn, delay, options = {}) {
  let timerId;
  const leading = options.leading || false;

  return function (...args) {
    const isFirstCall = !timerId;       // No timer means this is a fresh burst

    clearTimeout(timerId);

    if (leading && isFirstCall) {
      fn.apply(this, args);            // Fire immediately on first call
    }

    timerId = setTimeout(() => {
      if (!leading) {
        fn.apply(this, args);          // Trailing: fire after silence
      }
      timerId = null;                  // Reset — next call is a "first call" again
    }, delay);
  };
}

// Test trailing (default)
const trailingFn = debounce((x) => console.log("trailing:", x), 300);
trailingFn("a"); // ...waits...
trailingFn("b"); // ...waits...
trailingFn("c"); // ...300ms silence... → "trailing: c"

// Test leading
const leadingFn = debounce((x) => console.log("leading:", x), 300, { leading: true });
leadingFn("a"); // → "leading: a" (fires immediately)
leadingFn("b"); // blocked
leadingFn("c"); // blocked
// ...300ms silence... → timerId reset, ready for next burst
leadingFn("d"); // → "leading: d" (fires immediately again)
```

**Grading notes:**
- `isFirstCall` check is the key insight — determined by whether `timerId` is falsy
- Must set `timerId = null` inside the setTimeout callback to properly reset the state
- Both branches (`leading` and `!leading`) must use `fn.apply(this, args)`

---

# Exam Tips

- **Closure is the backbone of both debounce and throttle.** The returned function "remembers" `timerId` / `lastTime` from the outer scope. If asked "what concept makes debounce work?" the answer is **closure**.

- **`clearTimeout` on `undefined` is safe.** It does nothing. So the first call to a debounced function works fine even though `timerId` is `undefined`.

- **`fn.apply(this, args)` vs `fn(...args)`**: Use `apply` when you need to preserve the caller's `this`. If the debounced function is used as an object method, `fn(...args)` would lose `this`. Exam questions often test this.

- **`Date.now()` returns milliseconds since Jan 1, 1970 (Unix epoch).** That is why `lastTime = 0` means the first throttled call always passes the `now - lastTime >= interval` check.

- **Common exam trap:** "What happens if delay is 0?" Answer: `setTimeout(fn, 0)` still defers to the next event loop tick. So `debounce(fn, 0)` still batches synchronous calls — only the last one fires.

- **`setTimeout` returns a number (timer ID).** `clearTimeout` takes that number. These are browser/Node APIs, not language features.

- **Memory leak risk:** If you create debounced functions in a loop or inside components without cleanup, the closures (and their references) persist. That is why `.cancel()` and cleanup on unmount matter.

- **Lodash's `_.debounce` and `_.throttle`** are the production-standard implementations. They support `leading`, `trailing`, `maxWait`, and `cancel`. Knowing the basic versions above is enough for exams, but mention Lodash if asked about real-world usage.

- **Throttle guarantees execution during activity; debounce does not.** A debounce with a 1-second delay on a continuously firing event will NEVER execute until the events stop. A throttle will execute once every interval.

---

# Quick Revision Table

| Concept | Debounce | Throttle |
|---------|----------|----------|
| **One-liner** | Fire after silence of X ms | Fire at most once per X ms |
| **Timer resets on new call?** | Yes | No |
| **Fires during rapid events?** | No (keeps delaying) | Yes (at steady intervals) |
| **Core mechanism** | `clearTimeout` + `setTimeout` | `Date.now()` comparison OR `setTimeout` lock |
| **Key variable** | `timerId` (stores pending timer) | `lastTime` or `timerId` (tracks cooldown) |
| **Leading variant** | Fire on first call, ignore rest until silence | Fire on first call, block for interval (default) |
| **Trailing variant** | Fire after silence (default) | Fire at end of interval |
| **Preserves context with** | `fn.apply(this, args)` | `fn.apply(this, args)` |
| **Cancel support** | `clearTimeout(timerId)` | `clearTimeout(timerId)` or reset `lastTime` |
| **Search input** | Yes | No |
| **Scroll handler** | No | Yes |
| **Window resize** | Yes | No |
| **Button double-click** | No | Yes |
| **API rate limit** | No | Yes |
| **Auto-save** | Yes | No |
| **Uses closure?** | Yes | Yes |
| **`clearTimeout(undefined)` safe?** | Yes (no-op) | Yes (no-op) |
| **`setTimeout(fn, 0)` behavior** | Still defers to next tick | Still defers to next tick |
| **Production library** | `_.debounce` (Lodash) | `_.throttle` (Lodash) |
