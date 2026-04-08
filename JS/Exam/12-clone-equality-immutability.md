# Shallow Clone, Deep Clone, Equality & Immutability (Class 14)

---

## Part A: Reference vs Value

### Start here. Run this in your head before reading anything else.

```js
// PRIMITIVES — copied by value
let a = 5;
let b = a;
b = 10;
console.log(a); // 5 — independent copy, b's change has no effect on a

// OBJECTS — copied by reference
let obj1 = { x: 1 };
let obj2 = obj1;
obj2.x = 99;
console.log(obj1.x); // 99 — BOTH variables point to the SAME object in memory
```

**Why?** Primitives (`number`, `string`, `boolean`, `null`, `undefined`, `symbol`, `bigint`) store the actual value. Objects (`{}`, `[]`, `function`) store a **memory address** (reference). Assigning an object to a new variable copies the address, not the data.

### BAD vs GOOD

```js
// BAD — thinking you made a copy
function updateUser(user) {
  const copy = user;         // NOT a copy — same reference
  copy.name = "Changed";
  return copy;
}
const original = { name: "Alice" };
updateUser(original);
console.log(original.name);  // "Changed" — original mutated!

// GOOD — actually make a copy
function updateUser(user) {
  const copy = { ...user };  // shallow clone — new object
  copy.name = "Changed";
  return copy;
}
const original = { name: "Alice" };
updateUser(original);
console.log(original.name);  // "Alice" — original untouched
```

**Exam Tip:** If a question says "what is the output?" and involves assigning one object to another, the answer almost always involves shared references.

---

## Part B: Shallow Clone

A **shallow clone** copies only the first level of properties. Nested objects are still shared.

### 1. Spread Operator `{ ...obj }`

```js
const orig = { a: 1, b: 2 };
const clone = { ...orig };
clone.a = 99;
console.log(orig.a); // 1 — first-level property is independent
```

### 2. `Object.assign({}, obj)` — same behavior

```js
const orig = { a: 1, b: 2 };
const clone = Object.assign({}, orig);
clone.b = 99;
console.log(orig.b); // 2 — independent at first level
```

### 3. Array Shallow Clone — `[...arr]` and `Array.from(arr)`

```js
const arr = [1, 2, 3];
const copy1 = [...arr];
const copy2 = Array.from(arr);

copy1[0] = 99;
console.log(arr[0]); // 1 — independent copy (primitives in array)
```

**But arrays of objects are still shallow:**

```js
const arr = [{ x: 1 }, { x: 2 }];
const copy = [...arr];
copy[0].x = 99;
console.log(arr[0].x); // 99 — nested object is shared!
```

### 4. The Shallow Clone Trap (EXAM FAVORITE)

```js
const original = { a: 1, nested: { b: 2 } };
const shallow = { ...original };

shallow.a = 99;
console.log(original.a);         // 1 — first level is independent

shallow.nested.b = 99;
console.log(original.nested.b);  // 99 — nested is SHARED!
```

**Why?** Spread copies the value of each property. For `a`, the value is the number `1` (primitive, copied by value). For `nested`, the value is a **reference** to `{ b: 2 }`. The reference is copied, so both `original.nested` and `shallow.nested` point to the same inner object.

### Summary: What Shallow Clone Copies

| Property Type | Copied Independently? |
|---|---|
| Primitive (`number`, `string`, `boolean`) | Yes |
| Nested object / array | **No** — shared reference |
| Function | Reference copied (same function) |

**Exam Tip:** If a question involves nested objects and spread/`Object.assign`, the nested data is **shared**. That is the whole point of the question.

---

## Part C: Deep Clone

A **deep clone** recursively copies everything, so no references are shared.

### 5. `JSON.parse(JSON.stringify(obj))` — Quick and Dirty

```js
const original = { a: 1, nested: { b: 2 } };
const deep = JSON.parse(JSON.stringify(original));

deep.nested.b = 99;
console.log(original.nested.b); // 2 — truly independent!
```

**Limitations (MEMORIZE THESE):**

```js
const obj = {
  fn: function() {},       // LOST — functions are dropped
  undef: undefined,        // LOST — undefined is dropped
  date: new Date(),        // BROKEN — becomes a string
  regex: /abc/g,           // BROKEN — becomes {}
  nan: NaN,                // BROKEN — becomes null
  inf: Infinity,           // BROKEN — becomes null
  sym: Symbol("x"),        // LOST — symbols are dropped
};

const clone = JSON.parse(JSON.stringify(obj));
console.log(clone.fn);    // undefined (gone)
console.log(clone.undef); // undefined (key gone entirely)
console.log(typeof clone.date); // "string" (not a Date object)
console.log(clone.regex);       // {}
console.log(clone.nan);         // null
```

**Circular references throw an error:**

```js
const obj = { a: 1 };
obj.self = obj;
JSON.stringify(obj); // TypeError: Converting circular structure to JSON
```

### 6. `structuredClone(obj)` — Modern Built-in

```js
const original = { a: 1, nested: { b: 2 }, date: new Date() };
const deep = structuredClone(original);

deep.nested.b = 99;
console.log(original.nested.b); // 2 — independent
console.log(deep.date instanceof Date); // true — Date preserved!
```

**Handles circular references:**

```js
const obj = { a: 1 };
obj.self = obj;
const clone = structuredClone(obj); // works!
console.log(clone.self === clone);  // true — circular ref preserved in clone
console.log(clone.self === obj);    // false — different object
```

**Limitations of `structuredClone`:**

- Cannot clone **functions** (throws DataCloneError)
- Cannot clone **DOM nodes**
- Cannot clone **Symbols**

### 7. Manual Deep Clone with Recursion (EXAM CRITICAL)

This is the one you need to be able to write from memory.

```js
function deepClone(value, seen = new Map()) {
  // BASE CASE 1: primitives and null — return as-is
  if (value === null || typeof value !== 'object') return value;

  // CIRCULAR REF CHECK: if we've already cloned this object, return the clone
  if (seen.has(value)) return seen.get(value);

  // SPECIAL TYPES: Date and RegExp need constructor calls
  if (value instanceof Date) return new Date(value);
  if (value instanceof RegExp) return new RegExp(value.source, value.flags);

  // CREATE SHELL: array or plain object
  const clone = Array.isArray(value) ? [] : {};

  // REGISTER in seen map BEFORE recursing (prevents infinite loop)
  seen.set(value, clone);

  // RECURSE: clone each property
  for (const key of Object.keys(value)) {
    clone[key] = deepClone(value[key], seen);
  }

  return clone;
}
```

**Line-by-line breakdown:**

| Line | Purpose |
|---|---|
| `if (value === null \|\| typeof value !== 'object') return value;` | Primitives and `null` are already values, not references. Return them directly. (`typeof null === 'object'` is a JS quirk, so we check `null` explicitly.) |
| `if (seen.has(value)) return seen.get(value);` | If we already started cloning this object, return the in-progress clone. This breaks circular reference loops. |
| `if (value instanceof Date)` | `Date` objects need `new Date(value)` to clone properly. Spread would lose the Date prototype. |
| `if (value instanceof RegExp)` | Same idea — `RegExp` needs its constructor. |
| `const clone = Array.isArray(value) ? [] : {};` | Create the right container type. |
| `seen.set(value, clone);` | Register the clone BEFORE recursing. If a child references the parent, `seen` will already have it. |
| `for (const key of Object.keys(value))` | Iterate own enumerable properties only. |
| `clone[key] = deepClone(value[key], seen);` | Recursively clone each value. |

**Test it:**

```js
const original = { a: 1, nested: { b: [2, 3] } };
const cloned = deepClone(original);

cloned.nested.b.push(4);
console.log(original.nested.b); // [2, 3] — untouched
console.log(cloned.nested.b);   // [2, 3, 4] — independent
```

### 8. Handling Circular References — Why the `seen` Map Matters

```js
const obj = { a: 1 };
obj.self = obj; // obj.self points back to obj — circular!

// WITHOUT seen Map: deepClone would recurse forever
//   clone obj → clone obj.self → clone obj.self.self → ... (infinite)

// WITH seen Map:
//   1. Start cloning obj. seen = { obj → clone }
//   2. Clone obj.self. It's obj. seen.has(obj) is true.
//   3. Return seen.get(obj) which is clone.
//   4. clone.self = clone (circular ref preserved, no infinite loop)

const cloned = deepClone(obj);
console.log(cloned.self === cloned); // true
console.log(cloned === obj);         // false
```

**Exam Tip:** If asked "how do you handle circular references in deep clone?" the answer is: use a `Map` (or `WeakMap`) to track already-visited objects. Check before recursing. Register before descending into children.

---

## Part D: Deep Equality

### 9. Why `===` Fails for Objects

```js
const a = { x: 1 };
const b = { x: 1 };
console.log(a === b);  // false — different references

const c = a;
console.log(a === c);  // true — same reference

console.log([1, 2] === [1, 2]); // false — different array objects
```

`===` compares **memory addresses** for objects, not their contents.

### 10. Deep Equality Implementation (EXAM CRITICAL)

```js
function isEqual(a, b) {
  // Same reference or same primitive value
  if (a === b) return true;

  // null checks (typeof null === 'object', so check early)
  if (a === null || b === null) return false;

  // Different types
  if (typeof a !== typeof b) return false;

  // If not objects, and a !== b already failed above, they're not equal
  if (typeof a !== 'object') return false;

  // Both are objects/arrays from here

  // Handle arrays
  if (Array.isArray(a) !== Array.isArray(b)) return false;

  const keysA = Object.keys(a);
  const keysB = Object.keys(b);

  // Different number of keys
  if (keysA.length !== keysB.length) return false;

  // Check every key in a exists in b with equal value
  for (const key of keysA) {
    if (!keysB.includes(key)) return false;
    if (!isEqual(a[key], b[key])) return false; // recursive!
  }

  return true;
}
```

**Test cases:**

```js
isEqual({ x: 1 }, { x: 1 });                    // true
isEqual({ x: 1 }, { x: 2 });                    // false
isEqual({ x: { y: 1 } }, { x: { y: 1 } });      // true  (deep)
isEqual([1, 2, 3], [1, 2, 3]);                   // true
isEqual([1, 2], [1, 2, 3]);                      // false (different length)
isEqual({ a: 1, b: 2 }, { b: 2, a: 1 });         // true  (key order irrelevant)
isEqual(null, null);                              // true  (a === b)
isEqual(null, { a: 1 });                          // false
isEqual(1, 1);                                    // true  (a === b)
isEqual(NaN, NaN);                                // false (NaN !== NaN)
```

**Note on arrays:** `Object.keys([10, 20, 30])` returns `["0", "1", "2"]`. Since arrays are objects with numeric string keys, the same function handles both.

**Exam Tip:** The tricky edge cases examiners test: `null`, `NaN`, arrays vs objects, different key counts, nested structures.

---

## Part E: Immutability

### 11. `Object.freeze()` — Shallow Freeze

```js
const obj = { a: 1, b: 2 };
Object.freeze(obj);

obj.a = 99;         // silently fails (or throws in strict mode)
console.log(obj.a); // 1

obj.c = 3;          // silently fails
console.log(obj.c); // undefined

delete obj.a;       // silently fails
console.log(obj.a); // 1
```

**But freeze is SHALLOW:**

```js
const obj = { a: 1, nested: { b: 2 } };
Object.freeze(obj);

obj.nested.b = 99;           // THIS WORKS!
console.log(obj.nested.b);   // 99 — nested object is NOT frozen
```

### 12. `Object.seal()` — Lock Structure, Allow Value Changes

```js
const obj = { a: 1, b: 2 };
Object.seal(obj);

obj.a = 99;          // ALLOWED — modifying existing property
console.log(obj.a);  // 99

obj.c = 3;           // BLOCKED — cannot add new properties
console.log(obj.c);  // undefined

delete obj.a;        // BLOCKED — cannot delete properties
console.log(obj.a);  // 99
```

### 13. `Object.preventExtensions()` — Only Block New Properties

```js
const obj = { a: 1, b: 2 };
Object.preventExtensions(obj);

obj.a = 99;          // ALLOWED
obj.c = 3;           // BLOCKED — no new properties
delete obj.a;        // ALLOWED — deletion is fine
console.log(obj);    // { b: 2 }
```

### Comparison Table: freeze vs seal vs preventExtensions

| Action | `freeze` | `seal` | `preventExtensions` |
|---|---|---|---|
| **Modify existing property values** | No | **Yes** | **Yes** |
| **Add new properties** | No | No | No |
| **Delete properties** | No | No | **Yes** |
| **Reconfigure property descriptors** | No | No | **Yes** |
| **Check method** | `Object.isFrozen(obj)` | `Object.isSealed(obj)` | `Object.isExtensible(obj)` returns `false` |
| **Depth** | Shallow | Shallow | Shallow |

**Key insight:** `freeze` is the most restrictive. `seal` is in the middle. `preventExtensions` is the least restrictive. All three are **shallow** -- they do not affect nested objects.

### 14. Deep Freeze Pattern

```js
function deepFreeze(obj) {
  Object.freeze(obj);
  for (const key of Object.keys(obj)) {
    if (typeof obj[key] === 'object' && obj[key] !== null && !Object.isFrozen(obj[key])) {
      deepFreeze(obj[key]);
    }
  }
  return obj;
}

const obj = { a: 1, nested: { b: 2 } };
deepFreeze(obj);

obj.nested.b = 99;
console.log(obj.nested.b); // 2 — nested is now frozen too
```

**Why check `!Object.isFrozen()`?** To avoid infinite loops with circular references. If an object is already frozen, skip it.

### 15. Why Immutability Matters

- **Predictable state** -- no function can accidentally mutate your data
- **Easier debugging** -- if data does not change, you know exactly where it was set
- **Safe sharing** -- pass objects to functions without fear of mutation
- **Required by frameworks** -- React state must be treated as immutable (new object = re-render)

**BAD vs GOOD (React-style thinking):**

```js
// BAD — mutating state directly
const state = { count: 0, items: [1, 2] };
state.count = 1;              // mutation
state.items.push(3);          // mutation

// GOOD — creating new state
const newState = {
  ...state,
  count: 1,
  items: [...state.items, 3], // new array
};
```

---

## Part F: Flatten & Unflatten Objects

### 16. Flatten Object

Convert nested structure to dot-notation keys:

```js
// Input:  { a: { b: 2 }, c: 3 }
// Output: { 'a.b': 2, c: 3 }

function flattenObject(obj, prefix = '', result = {}) {
  for (const key of Object.keys(obj)) {
    const newKey = prefix ? `${prefix}.${key}` : key;

    if (typeof obj[key] === 'object' && obj[key] !== null && !Array.isArray(obj[key])) {
      // Recurse into nested objects
      flattenObject(obj[key], newKey, result);
    } else {
      // Leaf value — assign with dot-path key
      result[newKey] = obj[key];
    }
  }
  return result;
}
```

**Test:**

```js
const nested = {
  user: {
    name: "Alice",
    address: {
      city: "NYC",
      zip: "10001"
    }
  },
  active: true
};

console.log(flattenObject(nested));
// {
//   'user.name': 'Alice',
//   'user.address.city': 'NYC',
//   'user.address.zip': '10001',
//   active: true
// }
```

**Key decisions:**
- Arrays are treated as leaf values (not recursed into). You could recurse into arrays using index notation like `items.0`, but the standard exam version skips arrays.
- `null` is a leaf value (`typeof null === 'object'` so we check explicitly).

### 17. Unflatten Object

Reverse operation -- dot-notation keys back to nested structure:

```js
// Input:  { 'a.b': 2, c: 3 }
// Output: { a: { b: 2 }, c: 3 }

function unflattenObject(obj) {
  const result = {};

  for (const key of Object.keys(obj)) {
    const parts = key.split('.');
    let current = result;

    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];

      if (i === parts.length - 1) {
        // Last part — assign the value
        current[part] = obj[key];
      } else {
        // Intermediate part — create nested object if needed
        if (!current[part] || typeof current[part] !== 'object') {
          current[part] = {};
        }
        current = current[part];
      }
    }
  }

  return result;
}
```

**Test:**

```js
const flat = {
  'user.name': 'Alice',
  'user.address.city': 'NYC',
  'user.address.zip': '10001',
  active: true
};

console.log(unflattenObject(flat));
// {
//   user: {
//     name: 'Alice',
//     address: { city: 'NYC', zip: '10001' }
//   },
//   active: true
// }
```

**How it works:** For key `'user.address.city'`, split into `['user', 'address', 'city']`. Walk/create the path `result.user.address`, then set `.city = 'NYC'`.

---

## Exam-Style Coding Problems

### Problem 1: What is the output?

```js
const a = { x: 1, y: { z: 2 } };
const b = { ...a };

b.x = 10;
b.y.z = 20;

console.log(a.x);   // ?
console.log(a.y.z); // ?
console.log(b.x);   // ?
console.log(b.y.z); // ?
```

**Answer:**

```
a.x   → 1    (primitive, independent copy)
a.y.z → 20   (nested object, shared reference — b.y.z change affects a.y.z)
b.x   → 10
b.y.z → 20
```

---

### Problem 2: Write `deepEqual` that handles arrays and nested objects

```js
// Must return true:
deepEqual([1, [2, 3]], [1, [2, 3]]);
deepEqual({ a: { b: [1] } }, { a: { b: [1] } });

// Must return false:
deepEqual([1, 2], [1, 2, 3]);
deepEqual({ a: 1 }, { a: 1, b: 2 });
```

**Solution:**

```js
function deepEqual(a, b) {
  if (a === b) return true;
  if (a === null || b === null) return false;
  if (typeof a !== typeof b) return false;
  if (typeof a !== 'object') return false;
  if (Array.isArray(a) !== Array.isArray(b)) return false;

  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  if (keysA.length !== keysB.length) return false;

  return keysA.every(key => keysB.includes(key) && deepEqual(a[key], b[key]));
}
```

---

### Problem 3: Implement `deepFreeze` and verify it works

```js
const config = {
  db: { host: "localhost", port: 5432 },
  debug: true
};

// Your deepFreeze function:
function deepFreeze(obj) {
  Object.freeze(obj);
  for (const key of Object.keys(obj)) {
    const val = obj[key];
    if (typeof val === 'object' && val !== null && !Object.isFrozen(val)) {
      deepFreeze(val);
    }
  }
  return obj;
}

deepFreeze(config);

config.debug = false;
config.db.port = 3306;
config.db.host = "remote";

console.log(config.debug);   // true  — frozen
console.log(config.db.port);  // 5432  — deep frozen
console.log(config.db.host);  // "localhost" — deep frozen
```

---

## Exam Tips (All in One Place)

- **Spread and `Object.assign` are shallow** -- always the trap in output questions.
- **`JSON.parse(JSON.stringify())` loses functions, undefined, Date, RegExp, NaN, Infinity, circular refs** -- memorize this list.
- **`structuredClone` is the modern answer** but still cannot clone functions or DOM nodes.
- **The `seen` Map in manual deep clone** must be registered BEFORE recursing into children, not after.
- **`===` compares references for objects** -- two objects with identical contents are not `===`.
- **`Object.freeze` is shallow** -- the exam loves testing `obj.nested.prop = X` after freezing.
- **freeze vs seal**: freeze blocks everything, seal allows modifying existing values.
- **Flatten/unflatten**: flatten recurses with a prefix string; unflatten splits on `.` and walks/creates the path.
- **NaN !== NaN** -- `isEqual(NaN, NaN)` returns `false` with the basic implementation. If the exam wants `true`, add `Number.isNaN(a) && Number.isNaN(b)` check.

---

## Quick Revision Table

| Concept | Method / Pattern | Key Gotcha |
|---|---|---|
| **Shallow clone (object)** | `{ ...obj }` or `Object.assign({}, obj)` | Nested objects are shared references |
| **Shallow clone (array)** | `[...arr]` or `Array.from(arr)` | Arrays of objects share inner refs |
| **Deep clone (quick)** | `JSON.parse(JSON.stringify(obj))` | Loses functions, undefined, Date, circular refs throw |
| **Deep clone (modern)** | `structuredClone(obj)` | No functions, no DOM nodes |
| **Deep clone (manual)** | Recursive function with `seen` Map | Register in `seen` before recursing to handle circular refs |
| **Circular ref handling** | `seen = new Map()` tracks visited objects | Without it, infinite recursion crashes |
| **Equality (`===`)** | Compares references for objects | `{x:1} === {x:1}` is `false` |
| **Deep equality** | Recursive key-by-key comparison | Check `null`, type, key count, then recurse |
| **`Object.freeze()`** | No modify, no add, no delete | **Shallow** -- nested objects still mutable |
| **`Object.seal()`** | Can modify values, no add, no delete | Also shallow |
| **`Object.preventExtensions()`** | Can modify and delete, no add | Least restrictive of the three |
| **Deep freeze** | Recursive `Object.freeze()` | Check `Object.isFrozen()` to avoid infinite loops |
| **Flatten object** | Recurse with dot-prefix accumulation | Skip arrays and `null` in recursion check |
| **Unflatten object** | Split key on `.`, walk/create path | Last segment gets the value, others get `{}` |
| **Immutability (why)** | Predictable state, safe sharing, framework patterns | React requires new objects for re-renders |
