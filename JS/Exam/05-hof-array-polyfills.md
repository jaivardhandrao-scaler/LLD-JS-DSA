# Higher-Order Functions & Array Polyfills (Class 6)

---

## Start Here: What Problem Are We Solving?

Look at this code and spot the pattern:

```js
const nums = [1, 2, 3, 4, 5];

// You keep writing this same loop structure over and over:
const doubled = [];
for (let i = 0; i < nums.length; i++) {
  doubled.push(nums[i] * 2);
}

const evens = [];
for (let i = 0; i < nums.length; i++) {
  if (nums[i] % 2 === 0) evens.push(nums[i]);
}

let sum = 0;
for (let i = 0; i < nums.length; i++) {
  sum += nums[i];
}
```

Every loop has the same skeleton. The only thing that changes is the **operation inside**. Higher-order functions let you extract that operation as a callback and reuse the skeleton.

```js
const doubled = nums.map(n => n * 2);
const evens   = nums.filter(n => n % 2 === 0);
const sum     = nums.reduce((acc, n) => acc + n, 0);
```

---

## 1. What Is a Higher-Order Function (HOF)?

A function that does **at least one** of:
- **Takes a function as an argument** (callback)
- **Returns a function**

```js
// Takes a function as argument
function repeat(n, action) {
  for (let i = 0; i < n; i++) action(i);
}
repeat(3, console.log); // 0, 1, 2

// Returns a function
function multiplier(factor) {
  return function (x) {
    return x * factor;
  };
}
const double = multiplier(2);
console.log(double(5)); // 10
```

**Built-in HOFs you already know:**

| HOF | How it uses a function |
|---|---|
| `Array.prototype.map` | Takes callback, calls it per element |
| `Array.prototype.filter` | Takes callback, calls it per element |
| `Array.prototype.reduce` | Takes callback, calls it per element |
| `setTimeout` | Takes callback, calls it after delay |
| `Array.prototype.sort` | Takes comparator function |
| `addEventListener` | Takes handler function |

---

## 2. How Callbacks Work Internally

When you write `arr.map(fn)`, the engine does NOT run `fn` immediately and pass the result. It stores `fn` and **calls it once per element**, forwarding the element, index, and array.

```js
// This is roughly what map does internally:
function fakeMap(arr, callback) {
  const result = [];
  for (let i = 0; i < arr.length; i++) {
    result.push(callback(arr[i], i, arr));  // <-- YOUR function gets called here
  }
  return result;
}

fakeMap([10, 20, 30], function (elem, idx) {
  console.log(`Element: ${elem}, Index: ${idx}`);
  return elem * 2;
});
// Element: 10, Index: 0
// Element: 20, Index: 1
// Element: 30, Index: 2
// Returns: [20, 40, 60]
```

**Key insight:** Your callback receives **(element, index, array)** as arguments. You do not have to use all three.

---

## 3. `forEach` -- Iterate, Returns Nothing

`forEach` calls the callback for each element. It **always returns `undefined`**.

```js
const fruits = ["apple", "banana", "cherry"];

fruits.forEach(function (fruit, index) {
  console.log(`${index}: ${fruit}`);
});
// 0: apple
// 1: banana
// 2: cherry
```

### Common Mistake: Trying to Return or Break from `forEach`

**BAD -- return inside forEach does NOT stop iteration or produce a value:**

```js
// BAD: trying to "return" a found value
function findBanana(arr) {
  arr.forEach(item => {
    if (item === "banana") return item;  // This returns from the CALLBACK, not from findBanana
  });
  // Always reaches here. findBanana returns undefined.
}
console.log(findBanana(["apple", "banana"])); // undefined
```

**BAD -- trying to break from forEach:**

```js
// BAD: break is a syntax error inside forEach
["a", "b", "c"].forEach(item => {
  if (item === "b") break;  // SyntaxError: Illegal break statement
});
```

**GOOD -- use a regular `for` loop or `for...of` when you need to break:**

```js
for (const item of ["a", "b", "c"]) {
  if (item === "b") break;  // Works fine
  console.log(item);
}
// a
```

**GOOD -- use `find` when looking for a specific element:**

```js
const result = ["apple", "banana"].find(item => item === "banana");
console.log(result); // "banana"
```

### Exam Tip
> `forEach` returns `undefined`. If the exam asks "What does `const x = arr.forEach(...)` store?", the answer is always `undefined`.

---

## 4. `map` -- Transform Each Element

- Calls callback on every element
- **Returns a NEW array** of the same length
- Does NOT mutate the original

```js
const nums = [1, 2, 3];
const squared = nums.map(n => n ** 2);

console.log(squared); // [1, 4, 9]
console.log(nums);    // [1, 2, 3] -- unchanged
```

### BAD vs GOOD

**BAD -- using `map` but ignoring its return value (use `forEach` instead):**

```js
// BAD: map creates a new array that is immediately thrown away
const users = ["Alice", "Bob"];
users.map(user => {
  console.log(user);  // Side effect only, no transformation
});
// The returned array [undefined, undefined] is wasted.
```

**GOOD -- use `map` when you need the transformed array:**

```js
const users = ["Alice", "Bob"];
const upper = users.map(user => user.toUpperCase());
console.log(upper); // ["ALICE", "BOB"]
```

**BAD -- mutating inside map instead of returning:**

```js
// BAD: forgetting to return
const nums = [1, 2, 3];
const result = nums.map(n => {
  n * 2;  // No return! Arrow function with braces needs explicit return.
});
console.log(result); // [undefined, undefined, undefined]
```

**GOOD:**

```js
// Option A: implicit return (no braces)
const result = nums.map(n => n * 2);

// Option B: explicit return (with braces)
const result2 = nums.map(n => { return n * 2; });
```

### Exam Tip
> `map` ALWAYS returns an array of the **same length** as the original. If your callback returns nothing, you get an array of `undefined`s.

---

## 5. `filter` -- Keep Elements That Pass a Test

- Calls callback on every element
- **Keeps** element if callback returns **truthy**
- Returns a **NEW array** (possibly shorter, possibly empty, never longer)

```js
const nums = [1, 2, 3, 4, 5, 6];
const evens = nums.filter(n => n % 2 === 0);
console.log(evens); // [2, 4, 6]
```

**Truthy/falsy matters:**

```js
const mixed = [0, 1, "", "hello", null, undefined, false, 42];
const truthy = mixed.filter(Boolean);  // Shorthand: Boolean as callback
console.log(truthy); // [1, "hello", 42]
```

**BAD vs GOOD:**

**BAD -- using filter when you want to transform (use map):**

```js
// BAD: filter is for selecting, not transforming
const nums = [1, 2, 3];
const result = nums.filter(n => n * 2);
// Returns [1, 2, 3] because 2, 4, 6 are all truthy -- nothing is filtered out!
```

**GOOD:**

```js
const doubled = nums.map(n => n * 2);    // [2, 4, 6] -- transformation
const evens = nums.filter(n => n % 2 === 0); // [2] -- selection
```

---

## 6. `reduce` -- Accumulate to a Single Value

**Signature:** `arr.reduce(callback, initialValue)`

The callback receives: **(accumulator, currentElement, index, array)**

```js
const nums = [1, 2, 3, 4];
const sum = nums.reduce((acc, curr) => acc + curr, 0);
// Step by step:
// acc=0,  curr=1 -> 1
// acc=1,  curr=2 -> 3
// acc=3,  curr=3 -> 6
// acc=6,  curr=4 -> 10
console.log(sum); // 10
```

### With Initial Value vs Without

**With initial value (recommended):**

```js
[1, 2, 3].reduce((acc, curr) => acc + curr, 0);
// acc starts as 0, iteration starts at index 0
// 0+1=1, 1+2=3, 3+3=6 -> 6
```

**Without initial value:**

```js
[1, 2, 3].reduce((acc, curr) => acc + curr);
// acc starts as first element (1), iteration starts at index 1
// 1+2=3, 3+3=6 -> 6
```

### Empty Array + No Initial Value = TypeError

```js
[].reduce((acc, curr) => acc + curr);
// TypeError: Reduce of empty array with no initial value
```

```js
[].reduce((acc, curr) => acc + curr, 0);
// Returns 0 (the initial value) -- no error
```

### Common Reduce Patterns

**Sum:**

```js
const sum = [10, 20, 30].reduce((acc, n) => acc + n, 0); // 60
```

**Max:**

```js
const max = [3, 7, 2, 9, 4].reduce((a, b) => a > b ? a : b);
// 9
```

**GroupBy:**

```js
const people = [
  { name: "Alice", dept: "eng" },
  { name: "Bob", dept: "sales" },
  { name: "Charlie", dept: "eng" },
];

const grouped = people.reduce((acc, person) => {
  const key = person.dept;
  if (!acc[key]) acc[key] = [];
  acc[key].push(person.name);
  return acc;          // <-- MUST return acc
}, {});

console.log(grouped);
// { eng: ["Alice", "Charlie"], sales: ["Bob"] }
```

**Flatten:**

```js
const nested = [[1, 2], [3, 4], [5]];
const flat = nested.reduce((acc, arr) => acc.concat(arr), []);
console.log(flat); // [1, 2, 3, 4, 5]
```

### Exam Tip
> **Always return the accumulator** from the reduce callback. If you forget, `acc` becomes `undefined` on the next iteration.
>
> **Always provide an initial value** unless you have a specific reason not to. It prevents the TypeError on empty arrays.

---

## 7. `find` and `findIndex` -- First Match

**`find`** returns the **first element** where callback returns truthy, or `undefined`.

**`findIndex`** returns the **index** of the first match, or **-1**.

```js
const users = [
  { id: 1, name: "Alice" },
  { id: 2, name: "Bob" },
  { id: 3, name: "Charlie" },
];

const bob = users.find(u => u.name === "Bob");
console.log(bob); // { id: 2, name: "Bob" }

const idx = users.findIndex(u => u.name === "Bob");
console.log(idx); // 1

const nobody = users.find(u => u.name === "Zara");
console.log(nobody); // undefined

const noIdx = users.findIndex(u => u.name === "Zara");
console.log(noIdx); // -1
```

**They stop at the first match** -- they do not scan the entire array once a match is found.

---

## 8. `some` and `every` -- Boolean Checks

**`some`** returns `true` if **at least one** element passes the test.

**`every`** returns `true` if **all** elements pass the test.

```js
const nums = [1, 3, 5, 7, 8];

nums.some(n => n % 2 === 0);  // true  (8 is even)
nums.every(n => n % 2 === 0); // false (1 is odd)

[2, 4, 6].every(n => n % 2 === 0); // true
```

**Edge cases with empty arrays:**

```js
[].some(() => true);  // false  (no elements can satisfy)
[].every(() => false); // true  (vacuous truth -- no elements violate)
```

### Exam Tip
> `[].every(callback)` returns `true` regardless of the callback. This is **vacuous truth** and is a classic exam question.

---

## 9. Chaining HOFs

You can chain `filter`, `map`, `reduce` because each returns a value the next method can work on.

```js
const transactions = [
  { item: "Book", price: 15, qty: 2 },
  { item: "Pen", price: 2, qty: 10 },
  { item: "Laptop", price: 800, qty: 1 },
  { item: "Eraser", price: 1, qty: 5 },
];

// Get total cost of items that cost more than $5 each
const totalExpensive = transactions
  .filter(t => t.price > 5)              // [{Book, 15, 2}, {Laptop, 800, 1}]
  .map(t => t.price * t.qty)             // [30, 800]
  .reduce((sum, cost) => sum + cost, 0); // 830

console.log(totalExpensive); // 830
```

### Performance: Filter First

**BAD -- map first, then filter (does unnecessary work):**

```js
// Transforms ALL 1000 elements, then throws most away
bigArray
  .map(x => expensiveTransform(x))
  .filter(x => x > 100);
```

**GOOD -- filter first, then map (less work):**

```js
// Filters to a small set, then transforms only those
bigArray
  .filter(x => x > 50)
  .map(x => expensiveTransform(x));
```

### Complete Chaining Example: Objects to Summary String

```js
const students = [
  { name: "Alice", grade: 92, subject: "Math" },
  { name: "Bob", grade: 45, subject: "Math" },
  { name: "Charlie", grade: 88, subject: "Science" },
  { name: "Diana", grade: 34, subject: "Math" },
  { name: "Eve", grade: 95, subject: "Science" },
];

// Task: Get a comma-separated string of names of Math students who passed (grade >= 50)

const result = students
  .filter(s => s.subject === "Math")      // Keep only Math students
  .filter(s => s.grade >= 50)             // Keep only passing
  .map(s => s.name)                       // Extract names
  .reduce((str, name, i) => {             // Join into string
    return i === 0 ? name : str + ", " + name;
  }, "");

console.log(result); // "Alice"
// (Only Alice is a passing Math student)
```

Alternatively, the reduce for joining can be replaced with `.join(", ")`:

```js
const result = students
  .filter(s => s.subject === "Math" && s.grade >= 50)
  .map(s => s.name)
  .join(", ");
// "Alice"
```

---

## POLYFILLS (EXAM CRITICAL)

A **polyfill** is your own implementation of a built-in method. The exam tests whether you understand how `map`, `filter`, `reduce`, and `forEach` work under the hood.

Key concepts in every polyfill:
- Attach to `Array.prototype` so all arrays can use it
- Use `this` to refer to the array the method is called on
- Call the callback with `(element, index, array)`
- Handle edge cases (null/undefined `this`, `thisArg`)

---

## 10. `Array.prototype.myMap` Polyfill

```js
Array.prototype.myMap = function (callback, thisArg) {
  // 1. Guard: if called on null or undefined, throw
  if (this == null) {
    throw new TypeError("Cannot read properties of null");
  }

  // 2. Create empty result array
  const result = [];

  // 3. Loop through each element of the array (this)
  for (let i = 0; i < this.length; i++) {
    // 4. Call the callback with thisArg as context
    //    Pass: current element, current index, the whole array
    result.push(callback.call(thisArg, this[i], i, this));
  }

  // 5. Return the new array (same length as original)
  return result;
};
```

**Line-by-line explanation:**

| Line | What it does |
|---|---|
| `function (callback, thisArg)` | `callback` is the transform function. `thisArg` optionally sets `this` inside the callback. |
| `if (this == null)` | `this` refers to the array. If someone does `myMap.call(null, fn)`, we throw -- mirrors native behavior. Uses `==` to catch both `null` and `undefined`. |
| `const result = []` | The new array we will fill and return. |
| `for (let i = 0; i < this.length; i++)` | Iterate over every index. `this` is the array. |
| `callback.call(thisArg, this[i], i, this)` | `call` invokes the callback with `thisArg` as its `this`. Passes three arguments: element, index, entire array. |
| `result.push(...)` | Store each transformed value. |
| `return result` | Return the new array. Original is untouched. |

**Test case:**

```js
const nums = [1, 2, 3];
const doubled = nums.myMap(function (el) {
  return el * 2;
});
console.log(doubled); // [2, 4, 6]
console.log(nums);    // [1, 2, 3] -- original unchanged

// With thisArg
const multiplier = { factor: 10 };
const scaled = [1, 2, 3].myMap(function (el) {
  return el * this.factor;
}, multiplier);
console.log(scaled); // [10, 20, 30]
```

---

## 11. `Array.prototype.myFilter` Polyfill

```js
Array.prototype.myFilter = function (callback, thisArg) {
  if (this == null) {
    throw new TypeError("Cannot read properties of null");
  }

  const result = [];

  for (let i = 0; i < this.length; i++) {
    // Only push if callback returns truthy
    if (callback.call(thisArg, this[i], i, this)) {
      result.push(this[i]);
    }
  }

  return result;
};
```

**Line-by-line explanation:**

| Line | What it does |
|---|---|
| `if (callback.call(...))` | Call the callback exactly like `myMap`, but instead of always pushing, we check if the return value is **truthy**. |
| `result.push(this[i])` | Only push the **original element** (not the callback's return value). This is the key difference from `map`. |
| `return result` | Result may be shorter than original, or even empty. |

**Key difference from myMap:**
- `myMap` pushes **callback's return value** (the transformed value)
- `myFilter` pushes **the original element** only if callback returns truthy

**Test case:**

```js
const nums = [1, 2, 3, 4, 5, 6];
const evens = nums.myFilter(n => n % 2 === 0);
console.log(evens); // [2, 4, 6]

const words = ["hello", "", "world", "", "!"];
const nonEmpty = words.myFilter(w => w.length > 0);
console.log(nonEmpty); // ["hello", "world", "!"]
```

---

## 12. `Array.prototype.myReduce` Polyfill

```js
Array.prototype.myReduce = function (callback, initialValue) {
  // 1. Empty array with no initial value = TypeError
  if (this.length === 0 && initialValue === undefined) {
    throw new TypeError("Reduce of empty array with no initial value");
  }

  // 2. Determine starting accumulator and starting index
  let acc = initialValue !== undefined ? initialValue : this[0];
  let startIndex = initialValue !== undefined ? 0 : 1;

  // 3. Iterate from startIndex
  for (let i = startIndex; i < this.length; i++) {
    // 4. Update accumulator with callback result
    acc = callback(acc, this[i], i, this);
  }

  // 5. Return final accumulated value
  return acc;
};
```

**Line-by-line explanation:**

| Line | What it does |
|---|---|
| `if (this.length === 0 && initialValue === undefined)` | If the array is empty and no initial value was given, there is nothing to return and nothing to start with -- throw TypeError. |
| `let acc = initialValue !== undefined ? initialValue : this[0]` | If initial value exists, accumulator starts as that. Otherwise, use the first array element. |
| `let startIndex = initialValue !== undefined ? 0 : 1` | If initial value exists, start iterating at index 0 (every element goes through callback). If not, start at index 1 (first element is already the accumulator). |
| `acc = callback(acc, this[i], i, this)` | Callback receives **(accumulator, currentElement, index, array)**. Its return value becomes the new accumulator. |
| `return acc` | After all iterations, return the final accumulated value. |

**Test cases:**

```js
// Sum with initial value
console.log([1, 2, 3].myReduce((a, b) => a + b, 0)); // 6

// Sum without initial value
console.log([1, 2, 3].myReduce((a, b) => a + b)); // 6

// Empty array with initial value -- returns initial value
console.log([].myReduce((a, b) => a + b, 42)); // 42

// Empty array without initial value -- TypeError
try {
  [].myReduce((a, b) => a + b);
} catch (e) {
  console.log(e.message); // "Reduce of empty array with no initial value"
}

// Building an object
const counts = ["a", "b", "a", "c", "b", "a"].myReduce((acc, char) => {
  acc[char] = (acc[char] || 0) + 1;
  return acc;
}, {});
console.log(counts); // { a: 3, b: 2, c: 1 }
```

---

## 13. `Array.prototype.myForEach` Polyfill

```js
Array.prototype.myForEach = function (callback, thisArg) {
  if (this == null) {
    throw new TypeError("Cannot read properties of null");
  }

  for (let i = 0; i < this.length; i++) {
    callback.call(thisArg, this[i], i, this);
  }

  // No return statement -- forEach always returns undefined
};
```

**Line-by-line explanation:**

| Line | What it does |
|---|---|
| `callback.call(thisArg, this[i], i, this)` | Same call pattern as `myMap` and `myFilter`. |
| No `result` array, no `return` | `forEach` is purely for side effects. It does not collect or return anything. |

**Key difference from the others:**
- `myMap` collects return values into a new array
- `myFilter` conditionally collects original elements
- `myReduce` accumulates into a single value
- `myForEach` does **nothing** with the return value of the callback

**Test case:**

```js
const log = [];
[10, 20, 30].myForEach(function (el, idx) {
  log.push(`${idx}:${el}`);
});
console.log(log); // ["0:10", "1:20", "2:30"]

// Confirm it returns undefined
const ret = [1, 2].myForEach(x => x * 2);
console.log(ret); // undefined
```

---

## Polyfill Summary: Side-by-Side Comparison

```
myForEach:  loop + callback.call(...)                    -> returns undefined
myMap:      loop + callback.call(...) + push RETURN VALUE -> returns new array (same length)
myFilter:   loop + callback.call(...) + push ELEMENT if truthy -> returns new array (0 to same length)
myReduce:   loop + acc = callback(acc, ...)              -> returns single value
```

The skeleton is the same. The difference is **what you do with the callback's return value**.

---

## Predict the Output

### Question 1

```js
const arr = [1, 2, 3, 4, 5];
const result = arr.filter(n => n > 2).map(n => n * 10);
console.log(result);
```

<details>
<summary><strong>Answer</strong></summary>

```
[30, 40, 50]
```

**Explanation:** `filter(n => n > 2)` produces `[3, 4, 5]`. Then `map(n => n * 10)` transforms to `[30, 40, 50]`.

</details>

---

### Question 2

```js
const arr = [10, 20, 30];

const a = arr.forEach(x => x + 1);
const b = arr.map(x => { x + 1; });
const c = arr.map(x => x + 1);

console.log(a);
console.log(b);
console.log(c);
```

<details>
<summary><strong>Answer</strong></summary>

```
undefined
[undefined, undefined, undefined]
[11, 21, 31]
```

**Explanation:**
- `a`: `forEach` always returns `undefined`.
- `b`: `map` with curly braces but **no `return`** statement. The callback implicitly returns `undefined` for each element.
- `c`: `map` with implicit return (no braces). Each element is correctly transformed.

</details>

---

### Question 3

```js
const arr = [1, 2, 3];

const result = arr.reduce((acc, curr, idx) => {
  console.log(`acc=${acc}, curr=${curr}, idx=${idx}`);
  return acc + curr;
});

console.log("Result:", result);
```

<details>
<summary><strong>Answer</strong></summary>

```
acc=1, curr=2, idx=1
acc=3, curr=3, idx=2
Result: 6
```

**Explanation:** No initial value is provided, so `acc` starts as `this[0]` which is `1`, and iteration starts at **index 1** (not 0). Only 2 iterations occur for a 3-element array.

</details>

---

## Exam Tips (Consolidated)

- **`forEach` returns `undefined`**. Never assign its result to a variable expecting a value.
- **`map` returns an array of the same length** as the original. Forgetting `return` inside a braced arrow function gives you `[undefined, ...]`.
- **`filter` returns elements, not callback return values.** `[1,2,3].filter(n => n * 2)` returns `[1,2,3]` (all truthy), not `[2,4,6]`.
- **`reduce` without initial value**: accumulator = first element, iteration starts at index 1. With initial value: iteration starts at index 0.
- **`reduce` on empty array without initial value**: `TypeError`.
- **`[].every(fn)` returns `true`** (vacuous truth). `[].some(fn)` returns `false`.
- **`find` returns the element or `undefined`**. **`findIndex` returns the index or `-1`**.
- **In polyfills**, the callback signature is always `callback.call(thisArg, this[i], i, this)` for map/filter/forEach. For reduce it is `callback(acc, this[i], i, this)` (no `thisArg`).
- **Filter before map** in chains for better performance.
- **`callback.call(thisArg, ...)`** is how you respect the optional `thisArg` parameter. If `thisArg` is `undefined`, `this` inside the callback defaults to `undefined` (strict mode) or `window` (sloppy mode).
- **Polyfill pattern to memorize**: null check, create result, loop with `callback.call`, return result. Only `reduce` is different (accumulator logic, no `thisArg`, TypeError check).

---

## Quick Revision Table

| Method | Returns | Mutates Original? | Callback Receives | Key Behavior |
|---|---|---|---|---|
| `forEach` | `undefined` | No | `(elem, idx, arr)` | Side effects only. Cannot break. |
| `map` | New array (same length) | No | `(elem, idx, arr)` | Transforms each element. |
| `filter` | New array (0 to same length) | No | `(elem, idx, arr)` | Keeps elements where callback is truthy. |
| `reduce` | Single value (any type) | No | `(acc, elem, idx, arr)` | Accumulates. Needs initial value for safety. |
| `find` | First matching element or `undefined` | No | `(elem, idx, arr)` | Stops at first match. |
| `findIndex` | Index of first match or `-1` | No | `(elem, idx, arr)` | Stops at first match. |
| `some` | `boolean` | No | `(elem, idx, arr)` | `true` if any pass. Short-circuits. |
| `every` | `boolean` | No | `(elem, idx, arr)` | `true` if all pass. Short-circuits. `[].every(fn)` = `true`. |

| Polyfill | Pushes to Result | What Gets Pushed | Returns |
|---|---|---|---|
| `myForEach` | Nothing | N/A | `undefined` |
| `myMap` | Every iteration | Callback's **return value** | New array |
| `myFilter` | Only when callback is truthy | The **original element** | New array |
| `myReduce` | N/A (updates accumulator) | N/A | Final accumulator |
