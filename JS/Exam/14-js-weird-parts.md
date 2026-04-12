# JS Weird Parts -- Advanced Edge Cases (Exam Study Guide)

---

## Part A: Hoisting

---

### 1. What Is Hoisting?

Hoisting is JavaScript's behavior of **moving declarations to the top of their scope** during the compilation phase, before any code executes. Only the **declaration** is hoisted, not the initialization/assignment.

**BAD mental model:** "JavaScript physically moves my code to the top."

**GOOD mental model:** "During compilation, JS registers all declarations in the current scope. At runtime, variables declared with `var` start as `undefined`, while `let`/`const`/`class` exist but are inaccessible until their declaration line (TDZ)."

---

### 2. `var` Hoisting

`var` declarations are hoisted and initialized to `undefined`.

```js
console.log(x); // undefined (NOT ReferenceError)
var x = 5;
console.log(x); // 5
```

**What the engine sees (conceptually):**

```js
var x;           // declaration hoisted, initialized to undefined
console.log(x);  // undefined
x = 5;           // assignment stays in place
console.log(x);  // 5
```

**`var` is function-scoped, NOT block-scoped:**

```js
function example() {
  console.log(a); // undefined (hoisted to top of function)
  if (true) {
    var a = 10;
  }
  console.log(a); // 10 (var ignores the if-block)
}
example();
```

---

### 3. `let` and `const` Hoisting (TDZ)

`let` and `const` ARE hoisted, but they are **not initialized**. Accessing them before the declaration line throws a `ReferenceError`. The period between the start of the scope and the declaration is called the **Temporal Dead Zone (TDZ)**.

```js
// console.log(x); // ReferenceError: Cannot access 'x' before initialization
let x = 5;
console.log(x);    // 5
```

**Proof that `let` is hoisted (not just "undeclared"):**

```js
let x = "outer";

function test() {
  // If let were NOT hoisted, this would print "outer" (from the outer scope).
  // But it throws ReferenceError, proving the inner `let x` IS hoisted
  // and creates a TDZ from the start of the function to the let line.
  console.log(x); // ReferenceError: Cannot access 'x' before initialization
  let x = "inner";
}
test();
```

**`const` behaves the same way for hoisting but must be initialized at declaration:**

```js
const y;          // SyntaxError: Missing initializer in const declaration
```

---

### 4. Function Declaration Hoisting

Function declarations are **fully hoisted** -- both the name AND the function body are available at the top of the scope.

```js
sayHello(); // "Hello!" -- works BEFORE the declaration

function sayHello() {
  console.log("Hello!");
}
```

**This is unique to function declarations.** No other construct is fully hoisted like this.

---

### 5. Function Expression Hoisting

Function expressions follow the hoisting rules of their variable keyword (`var`, `let`, `const`).

```js
// var function expression
console.log(greet);   // undefined (var is hoisted, but not the assignment)
// greet();            // TypeError: greet is not a function
var greet = function () {
  return "Hi";
};

// let function expression
// console.log(hello); // ReferenceError (TDZ)
let hello = function () {
  return "Hey";
};
```

**BAD -- assuming function expressions work like function declarations:**

```js
add(2, 3); // TypeError: add is not a function

var add = function (a, b) {
  return a + b;
};
```

**GOOD -- declare before use, or use a function declaration:**

```js
function add(a, b) {
  return a + b;
}
add(2, 3); // 5
```

---

### 6. Arrow Function Hoisting

Arrow functions are always expressions, never declarations. They follow the same rules as function expressions:

```js
// console.log(square); // ReferenceError (const + TDZ)
const square = (n) => n * n;
console.log(square(4)); // 16
```

---

### 7. Class Hoisting (TDZ)

Classes are hoisted but in the TDZ, just like `let`/`const`.

```js
// const a = new Animal("Cat"); // ReferenceError: Cannot access 'Animal' before initialization

class Animal {
  constructor(type) {
    this.type = type;
  }
}

const a = new Animal("Cat"); // works
```

This is different from function declarations, which can be called before their declaration.

---

### 8. Hoisting Priority -- When Declarations Collide

When a `var` and a function declaration have the same name, **function declarations take priority over `var`**.

```js
console.log(foo); // [Function: foo] -- NOT undefined

var foo = "bar";
function foo() {
  return "baz";
}

console.log(foo); // "bar" -- assignment overwrites during execution
```

**What happens internally:**

1. Compilation: `function foo` is hoisted (full function). `var foo` is also hoisted, but since `foo` already exists (from the function), the `var` declaration is ignored (does not reset it).
2. Execution: `console.log(foo)` → the function. Then `foo = "bar"` overwrites it with the string.

**Two function declarations with the same name -- last one wins:**

```js
function foo() { return 1; }
function foo() { return 2; }

console.log(foo()); // 2 -- the second declaration overwrites the first
```

---

### 9. Hoisting Summary Table

| Declaration Type | Hoisted? | Initialized? | Usable before declaration? | Scope |
|---|---|---|---|---|
| `var` | Yes | `undefined` | Yes (value is `undefined`) | Function |
| `let` | Yes | No (TDZ) | No (`ReferenceError`) | Block |
| `const` | Yes | No (TDZ) | No (`ReferenceError`) | Block |
| `function` declaration | Yes | Full function body | **Yes** (fully usable) | Function* |
| `function` expression (`var`) | Var is hoisted | `undefined` | No (`TypeError`: not a function) | Function |
| `function` expression (`let`/`const`) | Yes (TDZ) | No | No (`ReferenceError`) | Block |
| Arrow function | Same as its variable keyword | Same | Same | Same |
| `class` | Yes | No (TDZ) | No (`ReferenceError`) | Block |

*Function declarations inside blocks have inconsistent behavior across engines. Avoid it.

---

### 10. Hoisting Prediction Questions

#### Question 1

```js
var x = 1;
function x() {}
console.log(typeof x);
```

<details>
<summary><strong>Answer</strong></summary>

```
"number"
```

**Why:** During hoisting, `function x` is hoisted first (fully), then `var x` sees `x` already exists and does nothing. At runtime, `x = 1` overwrites the function with the number `1`. So `typeof x` is `"number"`.

</details>

---

#### Question 2

```js
console.log(a);
console.log(b);
var a = 1;
let b = 2;
```

<details>
<summary><strong>Answer</strong></summary>

```
undefined
ReferenceError: Cannot access 'b' before initialization
```

**Why:** `var a` is hoisted and initialized to `undefined`. `let b` is hoisted but in TDZ -- accessing it throws `ReferenceError`. The second `console.log` never runs because the error halts execution.

</details>

---

#### Question 3

```js
foo();
bar();

function foo() {
  console.log("foo");
}

var bar = function () {
  console.log("bar");
};
```

<details>
<summary><strong>Answer</strong></summary>

```
"foo"
TypeError: bar is not a function
```

**Why:** `function foo` is fully hoisted -- calling it works. `var bar` is hoisted as `undefined` -- calling `undefined()` throws `TypeError`.

</details>

---

#### Question 4

```js
let a = 1;

function outer() {
  console.log(a);
  let a = 2;
}

outer();
```

<details>
<summary><strong>Answer</strong></summary>

```
ReferenceError: Cannot access 'a' before initialization
```

**Why:** The inner `let a` is hoisted to the top of `outer()`, creating a TDZ. Even though there is an outer `a = 1`, the inner `let a` shadows it. Accessing `a` before the `let` line hits the TDZ.

</details>

---

## Part B: Type Coercion

---

### 11. Implicit Type Coercion

JavaScript automatically converts types when operators expect a different type. This is "implicit coercion" and it is the source of most "weird" behavior.

#### The `+` Operator -- String Wins

If **either** operand is a string, `+` does **string concatenation**:

```js
"5" + 3        // "53"    (3 becomes "3")
"5" + true     // "5true"
"5" + null     // "5null"
"5" + undefined // "5undefined"
1 + "2" + 3    // "123"   (left to right: 1+"2"="12", "12"+3="123")
```

If **neither** operand is a string, `+` does **numeric addition**:

```js
5 + 3          // 8
true + true    // 2       (true→1, 1+1=2)
true + false   // 1       (true→1, false→0)
null + 1       // 1       (null→0)
undefined + 1  // NaN     (undefined→NaN)
```

#### The `-`, `*`, `/`, `%` Operators -- Always Numeric

These operators always convert both operands to numbers:

```js
"5" - 3        // 2     ("5"→5)
"5" * "2"      // 10
"10" / "2"     // 5
"5" - true     // 4     (true→1)
"abc" - 1      // NaN   ("abc"→NaN)
```

**Exam Tip:** `+` is ambiguous (string or number). All other arithmetic operators always coerce to number.

---

### 12. Object-to-Primitive Coercion

When objects appear in arithmetic or string contexts, JS calls:
1. `valueOf()` -- expected to return a primitive
2. If `valueOf()` doesn't return a primitive, try `toString()`

```js
[].toString()       // ""
[1, 2].toString()   // "1,2"
({}).toString()      // "[object Object]"
```

**The famous coercion results:**

```js
[] + []           // ""
// Both arrays → toString() → "" + "" = ""

[] + {}           // "[object Object]"
// [] → "", {} → "[object Object]", "" + "[object Object]"

{} + []           // 0
// {} is parsed as empty block (NOT an object), then +[] is unary plus
// +[] → +"" → 0

[] + 0            // "0"
// [] → "", "" + 0 = "0"

"" + {}           // "[object Object]"
```

---

### 13. Truthy and Falsy Values

**Falsy values (MEMORIZE -- there are exactly 8):**

| Value | Type |
|---|---|
| `false` | boolean |
| `0` | number |
| `-0` | number |
| `0n` | bigint |
| `""` (empty string) | string |
| `null` | object (typeof bug) |
| `undefined` | undefined |
| `NaN` | number |

**Everything else is truthy**, including:

```js
Boolean([])           // true  (empty array is truthy!)
Boolean({})           // true  (empty object is truthy!)
Boolean("0")          // true  (non-empty string)
Boolean("false")      // true  (non-empty string)
Boolean(new Number(0)) // true  (object wrapper)
Boolean(-1)           // true
Boolean(Infinity)     // true
```

**Exam Tip:** `[]` and `{}` are **truthy**. This trips people up because empty feels falsy, but they are objects, and all objects are truthy.

---

## Part C: Equality -- `==` vs `===`

---

### 14. Strict Equality (`===`)

**No type coercion.** If types differ, return `false` immediately.

```js
5 === 5          // true  (same type, same value)
5 === "5"        // false (number vs string)
0 === false      // false (number vs boolean)
null === undefined // false (different types)
NaN === NaN      // false (NaN is NEVER equal to anything, including itself)
```

**Rule:** Always use `===` unless you have a specific reason for `==`.

---

### 15. Loose Equality (`==`) -- The Coercion Rules

`==` converts types before comparing. The algorithm (simplified):

| Comparison | What happens |
|---|---|
| Same type | Same as `===` (except NaN) |
| `null == undefined` | **`true`** (special rule) |
| `null == anything else` | **`false`** |
| `undefined == anything else` | **`false`** |
| Number vs String | String → Number, then compare |
| Boolean vs anything | Boolean → Number first, then compare |
| Object vs Primitive | Object → `valueOf()`/`toString()`, then compare |

```js
// Number vs String
5 == "5"         // true  ("5" → 5, 5 == 5)
0 == ""          // true  ("" → 0, 0 == 0)

// Boolean vs Number
true == 1        // true  (true → 1, 1 == 1)
false == 0       // true  (false → 0, 0 == 0)

// Boolean vs String
true == "1"      // true  (true → 1, "1" → 1, 1 == 1)
false == ""      // true  (false → 0, "" → 0, 0 == 0)
false == "0"     // true  (false → 0, "0" → 0, 0 == 0)

// null / undefined
null == undefined  // true  (special rule)
null == 0          // false (null only equals undefined)
null == ""         // false
null == false      // false
undefined == 0     // false
undefined == false // false

// Object vs Primitive
[1] == 1         // true  ([1].toString() = "1", "1" → 1, 1 == 1)
[""] == ""       // true  ([""].toString() = "", "" == "")
```

---

### 16. The Infamous `[] == ![]`

```js
[] == ![]        // true
```

**Step by step:**

1. `![]` evaluates first. `[]` is truthy, so `![]` is `false`.
2. Now we have `[] == false`.
3. Boolean vs Object: `false` → `0`. Now `[] == 0`.
4. Object vs Number: `[].toString()` → `""`. Now `"" == 0`.
5. String vs Number: `""` → `0`. Now `0 == 0`.
6. `true`.

---

### 17. Equality Quick Reference

| Expression | `==` | `===` | Why |
|---|---|---|---|
| `1 == "1"` | `true` | `false` | `==` coerces string to number |
| `0 == false` | `true` | `false` | `==` coerces boolean to number |
| `0 == ""` | `true` | `false` | `==` coerces empty string to 0 |
| `null == undefined` | `true` | `false` | Special `==` rule |
| `null == 0` | `false` | `false` | null only `==` undefined |
| `NaN == NaN` | `false` | `false` | NaN never equals anything |
| `[] == []` | `false` | `false` | Different references |
| `"" == false` | `true` | `false` | Both coerce to 0 |
| `"0" == false` | `true` | `false` | Both coerce to 0 |
| `"" == "0"` | `false` | `false` | Same type, different values |
| `[] == ![]` | `true` | N/A | See walkthrough above |

---

## Part D: NaN, undefined, null -- The Trinity of Confusion

---

### 18. `undefined`

**Meaning:** "A variable has been declared but has not been assigned a value."

```js
let x;
console.log(x);             // undefined
console.log(typeof x);      // "undefined"

function foo(a, b) {
  console.log(b);            // undefined (no argument passed)
}
foo(1);

const obj = {};
console.log(obj.missing);   // undefined (property does not exist)

function bar() {}
console.log(bar());          // undefined (no return statement)
```

**Key facts:**
- `typeof undefined` is `"undefined"`
- `undefined` is falsy
- `undefined == null` is `true`
- `undefined === null` is `false`

---

### 19. `null`

**Meaning:** "Intentional absence of any value." You explicitly assign `null` to indicate "nothing here."

```js
let user = null;             // deliberately empty
console.log(user);           // null
console.log(typeof user);    // "object" (HISTORIC BUG -- memorize this)
```

**Key facts:**
- `typeof null` is `"object"` (this is a bug from 1995, never fixed)
- `null` is falsy
- `null == undefined` is `true`
- `null === undefined` is `false`
- `null == 0` is `false` (null only `==` to undefined)
- `null == ""` is `false`

**How to check for null:**

```js
// BAD -- typeof null is "object"
if (typeof x === "null") { }  // never true!

// GOOD -- strict equality
if (x === null) { }

// GOOD -- check both null and undefined at once
if (x == null) { }  // true for both null and undefined
```

---

### 20. `NaN`

**Meaning:** "Not a Number" -- the result of an invalid numeric operation.

```js
console.log(0 / 0);          // NaN
console.log(parseInt("abc")); // NaN
console.log(Math.sqrt(-1));   // NaN
console.log(undefined + 1);   // NaN
console.log("hello" - 1);     // NaN
```

**Key facts:**
- `typeof NaN` is `"number"` (yes, "Not a Number" is a number)
- **`NaN !== NaN`** -- NaN is the only value in JavaScript not equal to itself
- `NaN` is falsy

**How to check for NaN:**

```js
// BAD -- NaN === NaN is false, so this never works
if (x === NaN) { }     // always false!

// BAD -- isNaN() coerces first, giving surprising results
isNaN("hello");          // true  (coerces "hello" to NaN)
isNaN("");               // false (coerces "" to 0)
isNaN(undefined);        // true  (coerces undefined to NaN)
isNaN(null);             // false (coerces null to 0)

// GOOD -- Number.isNaN() does NOT coerce
Number.isNaN(NaN);       // true
Number.isNaN("hello");   // false (it's a string, not NaN)
Number.isNaN(undefined); // false
Number.isNaN(123);       // false
```

**Exam Tip:** `isNaN()` (global) coerces its argument to a number first, then checks. `Number.isNaN()` does NOT coerce -- it only returns `true` for the actual `NaN` value. Always use `Number.isNaN()`.

---

### 21. Comparison Table: undefined vs null vs NaN

| | `undefined` | `null` | `NaN` |
|---|---|---|---|
| **Meaning** | Not assigned | Intentionally empty | Invalid number |
| **typeof** | `"undefined"` | `"object"` (bug) | `"number"` |
| **Falsy?** | Yes | Yes | Yes |
| **`== null`** | `true` | `true` | `false` |
| **`=== null`** | `false` | `true` | `false` |
| **`== undefined`** | `true` | `true` | `false` |
| **`== NaN`** | `false` | `false` | `false` |
| **`=== NaN`** | `false` | `false` | `false` |
| **`=== self`** | `true` | `true` | **`false`** |
| **How to check** | `x === undefined` or `typeof x === "undefined"` | `x === null` | `Number.isNaN(x)` |
| **Default value of** | Uninitialized variables, missing args, missing props | Nothing (must assign explicitly) | `0/0`, `parseInt("abc")`, math errors |

---

## Part E: Classic Weird Interview Edge Cases

---

### 22. `typeof` Quirks

```js
typeof undefined       // "undefined"
typeof null            // "object"       ← BUG
typeof NaN             // "number"       ← ironic
typeof []              // "object"       ← arrays are objects
typeof {}              // "object"
typeof function(){}    // "function"     ← special case
typeof 42              // "number"
typeof "hello"         // "string"
typeof true            // "boolean"
typeof Symbol("x")     // "symbol"
typeof 42n             // "bigint"
typeof undeclaredVar   // "undefined"    ← does NOT throw ReferenceError!
```

**Exam Tip:** `typeof` on an undeclared variable returns `"undefined"` instead of throwing. This is the only operator that can safely reference an undeclared variable.

---

### 23. Floating Point Precision

```js
0.1 + 0.2 === 0.3     // false
0.1 + 0.2              // 0.30000000000000004
```

**Why?** JavaScript uses IEEE 754 double-precision floating point. `0.1` and `0.2` cannot be represented exactly in binary, so their sum has a tiny rounding error.

**Fix -- compare with epsilon:**

```js
function floatEqual(a, b) {
  return Math.abs(a - b) < Number.EPSILON;
}

floatEqual(0.1 + 0.2, 0.3); // true
```

---

### 24. Comma Operator

The comma operator evaluates each expression left to right and returns the **last** one.

```js
const x = (1, 2, 3);
console.log(x); // 3

let a = 0;
const b = (a++, a++, a++);
console.log(a); // 3
console.log(b); // 2 (value of the last a++, which is post-increment: returns 2, then a becomes 3)
```

---

### 25. More Coercion Gotchas

```js
true + true + true     // 3  (1+1+1)
[] + 0                 // "0"  ([]→"", ""+0="0")
+[]                    // 0   (+""→0)
+{}                    // NaN (+("[object Object]")→NaN)
!!""                   // false (empty string is falsy)
!!"0"                  // true  (non-empty string)
!!0                    // false
!![]                   // true  (objects are truthy)
!!null                 // false
undefined + undefined  // NaN  (NaN + NaN)
null + null            // 0    (0 + 0)
"" - - ""              // 0    (0 - (-0) = 0)
"b" + "a" + +"a" + "a" // "baNaNa" (+"a" is NaN, NaN→"NaN" in string concat)
```

---

### 26. String to Number Coercion Rules

| Input | `Number(x)` | Notes |
|---|---|---|
| `""` | `0` | Empty string → 0 |
| `"  "` | `0` | Whitespace-only string → 0 |
| `"123"` | `123` | Numeric string → number |
| `"12.5"` | `12.5` | Works with decimals |
| `"0x1A"` | `26` | Hex strings work |
| `"abc"` | `NaN` | Non-numeric → NaN |
| `"12abc"` | `NaN` | Mixed → NaN (unlike parseInt!) |
| `true` | `1` | |
| `false` | `0` | |
| `null` | `0` | |
| `undefined` | `NaN` | |
| `[]` | `0` | [] → "" → 0 |
| `[5]` | `5` | [5] → "5" → 5 |
| `[1,2]` | `NaN` | [1,2] → "1,2" → NaN |
| `{}` | `NaN` | {} → "[object Object]" → NaN |

**`parseInt` vs `Number`:**

```js
parseInt("123abc")   // 123  (parses until non-digit)
Number("123abc")     // NaN  (entire string must be numeric)

parseInt("")         // NaN  (no digits found)
Number("")           // 0    (empty string is 0)

parseInt("0x1A")     // 26   (detects hex prefix)
parseInt("08")       // 8    (modern engines parse as decimal)
```

---

## Predict the Output (Exam Practice)

---

### Question 1

```js
console.log(1 + "2" + "2");
console.log(1 + +"2" + "2");
console.log(1 + -"1" + "2");
console.log(+"1" + "1" + "2");
console.log("A" - "B" + "2");
console.log("A" - "B" + 2);
```

<details>
<summary><strong>Answer</strong></summary>

```
"122"    // 1+"2"="12", "12"+"2"="122"
"32"     // +"2"=2, 1+2=3, 3+"2"="32"
"02"     // -"1"=-1, 1+(-1)=0, 0+"2"="02"
"112"    // +"1"=1, 1+"1"="11", "11"+"2"="112"
"NaN2"   // "A"-"B"=NaN, NaN+"2"="NaN2"
NaN      // "A"-"B"=NaN, NaN+2=NaN
```

</details>

---

### Question 2

```js
console.log(typeof typeof 1);
```

<details>
<summary><strong>Answer</strong></summary>

```
"string"
```

**Why:** `typeof 1` returns `"number"` (a string). `typeof "number"` returns `"string"`.

</details>

---

### Question 3

```js
var a = 1;
function foo() {
  console.log(a);
  var a = 2;
  console.log(a);
}
foo();
```

<details>
<summary><strong>Answer</strong></summary>

```
undefined
2
```

**Why:** Inside `foo`, `var a` is hoisted to the top of the function. The local `a` shadows the global `a`. At the first `console.log`, local `a` is `undefined` (hoisted but not yet assigned). At the second, it is `2`.

</details>

---

### Question 4

```js
console.log(false == "0");
console.log(false == "");
console.log("0" == "");
console.log(false === "0");
```

<details>
<summary><strong>Answer</strong></summary>

```
true     // false→0, "0"→0, 0==0
true     // false→0, ""→0, 0==0
false    // both strings, "0" !== "" (no coercion, same type)
false    // different types, strict equality
```

</details>

---

### Question 5

```js
console.log([] == false);
console.log([] == 0);
console.log("" == false);
console.log("" == 0);
console.log(0 == false);
```

<details>
<summary><strong>Answer</strong></summary>

```
true     // [] → "" → 0, false → 0, 0 == 0
true     // [] → "" → 0, 0 == 0
true     // "" → 0, false → 0, 0 == 0
true     // "" → 0, 0 == 0
true     // false → 0, 0 == 0
```

All roads lead to `0 == 0`.

</details>

---

### Question 6

```js
let x = 10;
if (true) {
  console.log(x);
  let x = 20;
}
```

<details>
<summary><strong>Answer</strong></summary>

```
ReferenceError: Cannot access 'x' before initialization
```

**Why:** The `let x = 20` inside the block creates a TDZ for `x` from the start of the block to the `let` line. The outer `x = 10` is shadowed. Accessing `x` before the `let` hits the TDZ.

</details>

---

## Exam Tips

- **Hoisting order:** function declarations > var declarations. If both exist with the same name, the function takes priority during hoisting, but the `var` assignment can overwrite at runtime.
- **TDZ is real.** `let` and `const` ARE hoisted, but accessing them before declaration is a `ReferenceError`, NOT `undefined`. This is a common "predict the output" trap.
- **`+` with a string always concatenates.** `-` always converts to numbers. This asymmetry is the root of most coercion surprises.
- **Falsy values:** exactly 8 (`false`, `0`, `-0`, `0n`, `""`, `null`, `undefined`, `NaN`). Everything else (including `[]`, `{}`, `"0"`, `"false"`) is truthy.
- **`null == undefined` is `true`.** `null` does not `==` anything else (not `0`, not `""`, not `false`). This is a special rule.
- **`NaN !== NaN`.** Always use `Number.isNaN()` to check for NaN, never `===`.
- **`typeof null === "object"`** is a historic bug. Know it cold.
- **`typeof NaN === "number"`** -- "Not a Number" is technically a number type.
- **`[] == ![]` is `true`.** Walk through the coercion steps on the exam.
- **`typeof` on an undeclared variable** returns `"undefined"` without throwing. This is unique to `typeof`.

---

## Quick Revision Table

| Concept | Key Fact | Common Trap |
|---|---|---|
| **var hoisting** | Hoisted + initialized to `undefined` | Accessing before assignment gives `undefined`, not error |
| **let/const hoisting** | Hoisted but in TDZ | `ReferenceError`, not `undefined` |
| **Function declaration** | Fully hoisted (name + body) | Can call before declaration line |
| **Function expression** | Only variable hoisted, not the function | `TypeError: not a function` if called early with `var` |
| **Class hoisting** | TDZ, like `let` | `ReferenceError` if used before declaration |
| **Function vs var priority** | Function wins during hoisting | `var` assignment can overwrite at runtime |
| **`+` operator** | String concat if either operand is string | `1 + "2"` is `"12"`, not `3` |
| **`-` operator** | Always numeric | `"5" - 3` is `2` |
| **Truthy/Falsy** | 8 falsy values; everything else truthy | `[]`, `{}`, `"0"` are all truthy |
| **`==` coercion** | Converts types, then compares | `null == undefined` is `true`, `null == 0` is `false` |
| **`===` strict** | No coercion; type must match | Always prefer this |
| **NaN** | Not equal to itself | Use `Number.isNaN()`, not `=== NaN` |
| **typeof null** | `"object"` | Bug from 1995, never fixed |
| **typeof NaN** | `"number"` | "Not a Number" is type number |
| **typeof undeclared** | `"undefined"` | Does NOT throw ReferenceError |
| **0.1 + 0.2** | Not exactly 0.3 | IEEE 754 floating point; use epsilon comparison |
| **`parseInt` vs `Number`** | parseInt stops at non-digit; Number requires whole string | `parseInt("123abc")` = 123, `Number("123abc")` = NaN |
| **`[] + []`** | `""` | Both toString to "", string concat |
| **`[] == ![]`** | `true` | Step through: ![] → false → 0, [] → "" → 0, 0 == 0 |

---

## Practice These

After studying this topic, solve these coding problems:

**From practiceSheet.md:**
- P25 (Fix var + setTimeout bug -- tests hoisting + closure understanding)

**Note:** The prediction questions within this file are your best practice. Focus on getting them right without peeking.
