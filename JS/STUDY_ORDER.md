# JavaScript — Study Order & Practice Map

**Exam:** Functional Programming using JS
**Date:** 13th April 2026, 5:30 PM - 7:00 PM
**Format:** 6 Coding Questions | 90 minutes | Proctored
**No MCQs. No viva. Pure coding.**

---

## Recommended Study Order

Study the **Exam Prep files** in this order. Each module builds on the previous one.

---

### Module 1: OOP in JavaScript

These topics form the foundation. `this`, `new`, prototypes, and classes show up in almost every coding question — even async ones.

| # | Topic | Study File | Key Concepts |
|---|-------|-----------|-------------|
| 1 | `this` Keyword | `Exam/01-this-keyword.md` | 4 binding rules (priority order), strict mode, arrow vs regular |
| 2 | Constructors & `new` | `Exam/02-constructors-prototypes.md` | How `new` works internally, prototype linking, `instanceof` |
| 3 | Classes & Inheritance | `Exam/03-classes-oop.md` | `extends`, `super`, method overriding, private fields `#`, static |

**Practice for Module 1:**

| Source | Questions | What they test |
|--------|-----------|---------------|
| Contest_Practice-1.md | Q1-Q4 | `this` in nested functions, setTimeout, lost `this`, strict mode |
| Contest_Practice-1.md | Q5-Q8 | Constructor functions, `myNew` polyfill, factory vs constructor, prototype methods |
| Contest_Practice-1.md | Q9-Q12 | ES6 classes, private fields, `extends`/`super`, getters |
| practiceSheet.md | P37 | Implement `new` operator (polyfill) |
| practiceSheet.md | P38 | Manual prototypal inheritance |
| practiceSheet.md | P39 | Prototype lookup after `delete` |
| practiceSheet.md | P40 | Class-based inheritance with mixins |
| practiceSheet.md | P41 | Method overriding + `super` simulation without `class` |
| practiceSheet.md | P42 | Singleton pattern in JS |
| practiceSheet.md | P43 | Factory pattern |
| practiceSheet.md | P20 | Polyfill `Object.create` |

---

### Module 2: Function Mastery & Functional JS

This module is the **highest-value** for the exam. Polyfills, closures, and currying are classic 90-minute contest questions.

| # | Topic | Study File | Key Concepts |
|---|-------|-----------|-------------|
| 4 | `call`, `apply`, `bind` | `Exam/04-call-apply-bind.md` | Explicit binding, polyfills for all three, edge cases |
| 5 | HOFs & Array Polyfills | `Exam/05-hof-array-polyfills.md` | `map`, `filter`, `reduce`, `forEach` polyfills, functional patterns |
| 6 | Closures, Scope & Currying | `Exam/06-closures-scope-currying.md` | Lexical scope, var-loop bug (3 fixes), data encapsulation, currying |

**Practice for Module 2:**

| Source | Questions | What they test |
|--------|-----------|---------------|
| Contest_Practice-1.md | Q13-Q16 | Closure counter, memoized add, private bank account, function factory |
| Contest_Practice-1.md | Q17-Q20 | `myMap`, `myFilter`, `myReduce` polyfills, chaining HOFs |
| practiceSheet.md | P15 | Polyfill `Function.prototype.call` |
| practiceSheet.md | P16 | Polyfill `Function.prototype.apply` |
| practiceSheet.md | P17 | Polyfill `Function.prototype.bind` (+ `new` handling) |
| practiceSheet.md | P18 | Polyfill `Array.prototype.reduce` |
| practiceSheet.md | P19 | Polyfill `Array.prototype.filter` |
| practiceSheet.md | P21 | `once(fn)` — closure + memoization |
| practiceSheet.md | P23 | Private variables using closures |
| practiceSheet.md | P24 | Rate limiter using closure |
| practiceSheet.md | P25 | Fix `var` + `setTimeout` bug (3 ways) |
| practiceSheet.md | P26 | Implement currying |
| practiceSheet.md | P27 | Infinite currying `sum(1)(2)(3)()` |
| practiceSheet.md | P28 | Function logger wrapper |
| practiceSheet.md | P49 | Flatten nested array (recursive + iterative) |
| practiceSheet.md | P50 | `groupBy` implementation |

---

### Module 3: Asynchronous JavaScript

Expect 2-3 questions from async topics. Promise combinators and event loop ordering are the most likely coding questions.

| # | Topic | Study File | Key Concepts |
|---|-------|-----------|-------------|
| 7 | Promises | `Exam/07-promises.md` | States, chaining, `.all`/`.race`/`.any`/`.allSettled` polyfills, microtasks |
| 8 | Event Loop | `Exam/08-event-loop.md` | Call stack, task queue vs microtask queue, execution order prediction |
| 9 | Async Patterns & Error Handling | `Exam/09-async-patterns.md` | Sequential vs parallel, concurrency control, `try/catch` with `async/await`, retry |

**Practice for Module 3:**

| Source | Questions | What they test |
|--------|-----------|---------------|
| practiceSheet.md | P1 | Promise pool (concurrency limiter) |
| practiceSheet.md | P2 | Retry with exponential backoff |
| practiceSheet.md | P3 | `Promise.all` with concurrency control |
| practiceSheet.md | P4 | Event loop output prediction |
| practiceSheet.md | P5 | Implement `async/await` using generators |
| practiceSheet.md | P6 | Sequential async task queue |
| practiceSheet.md | P7 | Manual promise chaining (`MyPromise`) |
| practiceSheet.md | P8 | Promise timeout wrapper |
| practiceSheet.md | P11 | Polyfill `Promise.all` |
| practiceSheet.md | P12 | Polyfill `Promise.any` |
| practiceSheet.md | P13 | Polyfill `Promise.race` |
| practiceSheet.md | P14 | Polyfill `Promise.allSettled` |
| practiceSheet.md | P44 | Observer pattern |
| practiceSheet.md | P45 | EventEmitter (`on`, `off`, `emit`, `once`) |
| practiceSheet.md | P46 | Pub-Sub system |
| practiceSheet.md | P48 | Priority-based task scheduler |

---

### Module 4: Performance & Optimization

These topics round out the exam. Debounce/throttle, deep clone, and memoization are common standalone coding questions.

| # | Topic | Study File | Key Concepts |
|---|-------|-----------|-------------|
| 10 | Debouncing & Throttling | `Exam/10-debounce-throttle.md` | Implementation from scratch, leading/trailing edge, real-world use |
| 11 | Memory Management | `Exam/11-memory-management.md` | Stack vs heap, GC, memory leaks (closures, DOM refs, timers) |
| 12 | Cloning & Immutability | `Exam/12-clone-equality-immutability.md` | Shallow vs deep copy, circular refs, `===` vs `==`, immutable patterns |
| 13 | Memoization & Caching | `Exam/13-memoization-caching-lru.md` | Memoize function, LRU cache, cache invalidation |
| 14 | JS Weird Parts | `Exam/14-js-weird-parts.md` | Hoisting, type coercion, TDZ, `typeof`, `NaN`, trick questions |

**Practice for Module 4:**

| Source | Questions | What they test |
|--------|-----------|---------------|
| practiceSheet.md | P9 | Debounced async function |
| practiceSheet.md | P10 | Throttled async function |
| practiceSheet.md | P22 | `memoize(fn)` implementation |
| practiceSheet.md | P29 | Deep clone with circular reference handling |
| practiceSheet.md | P30 | Deep equality check |
| practiceSheet.md | P31 | Flatten an object (dot-path keys) |
| practiceSheet.md | P32 | Unflatten an object |
| practiceSheet.md | P33 | Object diff |
| practiceSheet.md | P34 | Deep merge objects |
| practiceSheet.md | P35 | `pick(obj, keys)` |
| practiceSheet.md | P36 | `omit(obj, keys)` |
| practiceSheet.md | P47 | LRU Cache (`O(1)` get/put) |

---

## Exam Day Cheat Sheet: Most Likely Question Types

Based on the 6-question, 90-minute coding format:

| Priority | Question Type | Likely Count | Key Topics |
|----------|--------------|-------------|------------|
| HIGH | Polyfills (call/apply/bind, map/filter/reduce, Promise.all) | 1-2 | Module 2 + 3 |
| HIGH | Closure/Scope problems (counter, encapsulation, currying) | 1 | Module 2 |
| HIGH | Async control flow (promise pool, sequential queue, retry) | 1-2 | Module 3 |
| MEDIUM | `this` keyword prediction + fix | 1 | Module 1 |
| MEDIUM | Debounce/Throttle implementation | 1 | Module 4 |
| MEDIUM | Deep clone / memoize / LRU cache | 1 | Module 4 |
| LOW | Hoisting/coercion prediction (more likely MCQ, but you have coding) | 0-1 | Module 4 |

---

## Last-Minute Priority (If Short on Time)

If you only have a few hours, study these files in this exact order:

1. `Exam/06-closures-scope-currying.md` — closures are tested in almost everything
2. `Exam/04-call-apply-bind.md` — polyfills are guaranteed questions
3. `Exam/05-hof-array-polyfills.md` — `map`/`filter`/`reduce` polyfills
4. `Exam/07-promises.md` — promise combinators + polyfills
5. `Exam/01-this-keyword.md` — binding rules you need for all of the above
6. `Exam/10-debounce-throttle.md` — standalone coding question likely

Then practice with `Contest_Practice-1.md` Q13-Q20 (closures + polyfills).

---

## Quick Reference

For a condensed overview of all OOP concepts, see `notes.md`.

---

*Exam format: 6 coding questions, 90 minutes, no external resources, proctored.*
