
# 20 Coding Questions – All 5 Classes (Advanced JavaScript)



---

## Question 1: Fix `this` in Object Method and Nested Function

**Problem Statement:**  
You are given an object `user` with `name` and a method `greet` that logs `this.name` and then calls an inner function `innerGreet` that should also log `this.name`. When you call `user.greet()`, the inner function logs `undefined` because `this` is lost in the nested call. Fix the code so that both logs print the user's name (e.g. `"Alice"`) without changing the way `greet` is invoked (`user.greet()`).

**Hints:**  
- In a regular nested function, `this` is not the same as in the outer method; it follows the "how the function is called" rule.  
- You can store `this` in a variable (e.g. `self` or `that`) in the outer method and use that variable inside the inner function.  
- Alternatively, use an arrow function for the inner function so that it uses the surrounding (lexical) `this`.

**Expected Output:**
```
Alice
Alice
```

**Code Stub:**
```javascript
const user = {
  name: "Alice",
  greet: function () {
    console.log(this.name);
    function innerGreet() {
      console.log(this.name);
    }
    innerGreet();
  }
};

user.greet();
```

---

## Question 2: Predict and Fix `this` with setTimeout

**Problem Statement:**  
An object `timer` has `name: "Timer"` and a method `start` that uses `setTimeout` with a callback. The callback tries to log `this.name`, but when it runs, `this` is not the `timer` object, so the output is wrong. Fix it so that when `timer.start()` is called, after 100 ms the callback logs `"Timer"` (the object's name).

**Hints:**  
- Callbacks passed to `setTimeout` are invoked with a default binding (e.g. global or `undefined` in strict mode), not as a method of `timer`.  
- Use an arrow function in the callback so that `this` is taken from the surrounding scope (the `start` method), or bind the callback to `this`, or store `this` in a variable and use it inside the callback.

**Expected Output:**
```
Timer
```

**Code Stub:**
```javascript
const timer = {
  name: "Timer",
  start: function () {
    setTimeout(function () {
      console.log(this.name);
    }, 100);
  }
};

timer.start();
```

---

## Question 3: Method Called Without Object (Lost `this`)

**Problem Statement:**  
You have an object `calc` with a method `add(a, b)` that returns `this.base + a + b`. When you call `calc.add(1, 2)` it works. When you do `const fn = calc.add; fn(1, 2)` the result is wrong because `this` is lost. Implement a function `bindBase(base)` that returns a new function which, when called with `(a, b)`, returns `base + a + b`, so that `fn(1, 2)` gives the correct result for a given `base` (e.g. `base = 10` → `13`).

**Hints:**  
- `this` is determined by how the function is called; when `fn = calc.add`, calling `fn(1, 2)` does not have an object before the dot.  
- You need a function that "remembers" `base` and returns a function that uses that value. Closures can hold `base`; no need to rely on `this` for the final addition.

**Expected Output:**
```
13
```

**Code Stub:**
```javascript
const calc = {
  base: 10,
  add: function (a, b) {
    return this.base + a + b;
  }
};

function bindBase(base) {
  // Return a function that takes (a, b) and returns base + a + b
}

const fn = bindBase(10);
console.log(fn(1, 2));
```

---

## Question 4: Global vs Method `this` (Strict Mode)

**Problem Statement:**  
Write a small script that demonstrates the difference between `this` in the global scope and `this` inside a function called without an object in **strict mode**. Log `this` in both places. Your output should show that at the top level (e.g. in Node or browser) `this` is the global/module object, but inside the function `this` is `undefined`.

**Hints:**  
- Start the file or function with `"use strict";`.  
- In strict mode, a function called without a caller (e.g. `fn()`) gets `this === undefined`.  
- At the top level, `this` is still the environment's global object (e.g. `globalThis` or `window` or module exports in Node).

**Expected Output (conceptually):**
```
[Object: global] or window or {}
undefined
```

**Code Stub:**
```javascript
"use strict";

console.log(this);

function showThis() {
  console.log(this);
}

showThis();
```

---

## Question 5: Constructor Function for Book with Prototype Method

**Problem Statement:**  
Implement a constructor function `Book` that takes `title` and `author` and assigns them to the instance. Add a method `getInfo` on `Book.prototype` that returns a string: `"<title> by <author>"`. Create two instances and call `getInfo` on each. Do not put `getInfo` inside the constructor.

**Hints:**  
- Use `this.title = title` and `this.author = author` in the constructor.  
- Attach `getInfo` to `Book.prototype` so all instances share the same function.  
- Call the constructor with `new Book(...)`.

**Expected Output:**
```
Clean Code by Robert Martin
JS Guide by MDN
```

**Code Stub:**
```javascript
function Book(title, author) {
}

Book.prototype.getInfo = function () {
};

const b1 = new Book("Clean Code", "Robert Martin");
const b2 = new Book("JS Guide", "MDN");
console.log(b1.getInfo());
console.log(b2.getInfo());
```

---

## Question 6: Implement `myNew` (Polyfill for `new`)

**Problem Statement:**  
Implement a function `myNew(Constructor, ...args)` that mimics what the `new` keyword does: create a new object whose prototype is `Constructor.prototype`, run the constructor with `this` bound to that object and pass `args`, and return the object (unless the constructor returns a non-primitive, in which case return that value). Use it to create a `Person` with `name` and verify `person.name` and `person instanceof Person`.

**Hints:**  
- Create an object linked to `Constructor.prototype` (e.g. `Object.create(Constructor.prototype)`).  
- Call `Constructor.apply(obj, args)` so `this` inside the constructor is the new object.  
- If the result of that call is an object (or function) and not null, return it; otherwise return the new object.

**Expected Output:**
```
John
true
```

**Code Stub:**
```javascript
function myNew(Constructor, ...args) {
}

function Person(name) {
  this.name = name;
}

const person = myNew(Person, "John");
console.log(person.name);
console.log(person instanceof Person);
```

---

## Question 7: Factory Function vs Constructor

**Problem Statement:**  
Implement both a constructor `Car(make, model)` and a factory function `createCar(make, model)`. Both should produce objects with `make`, `model`, and a method `describe()` that returns `"<make> - <model>"`. Create one instance from each and call `describe()`. Also log whether each is `instanceof Car` (only the one created with `new Car` should be).

**Hints:**  
- Constructor: use `this.make`, `this.model`, and add `describe` on `Car.prototype`.  
- Factory: create a plain object, set properties, add `describe` on that object (or a shared prototype if you prefer), return the object. No `new`; so `instanceof Car` will be false for the factory-created object.

**Expected Output:**
```
Toyota - Camry
Honda - Civic
true
false
```

**Code Stub:**
```javascript
function Car(make, model) {
}

Car.prototype.describe = function () {
};

function createCar(make, model) {
}

const c1 = new Car("Toyota", "Camry");
const c2 = createCar("Honda", "Civic");
console.log(c1.describe());
console.log(c2.describe());
console.log(c1 instanceof Car);
console.log(c2 instanceof Car);
```

---

## Question 8: Add Method on Prototype and Avoid Prototype Pollution

**Problem Statement:**  
Implement a constructor `Student(id, name)` and add a method `getId` on `Student.prototype` that returns `this.id`. Do **not** add any method to `Array.prototype` or `Object.prototype`. Create an array of two students and use only the native `Array` methods (e.g. `map`) to get an array of their IDs. Your code should not modify built-in prototypes.

**Hints:**  
- Keep all custom methods on `Student.prototype` only.  
- Use `students.map(s => s.getId())` or equivalent; do not define `Array.prototype.myMap` or similar for this task.

**Expected Output:**
```
[ 101, 102 ]
```

**Code Stub:**
```javascript
function Student(id, name) {
}

Student.prototype.getId = function () {
};

const students = [
  new Student(101, "Ali"),
  new Student(102, "Bina")
];

const ids = [];
console.log(ids);
```

---

## Question 9: ES6 Class with Private Field and Static Counter

**Problem Statement:**  
Create a class `Order` with a private field `#id` and a static property `count` (number) that increments every time a new `Order` is created. The constructor should set `#id` to the current `Order.count` (before or after incrementing, as long as each order gets a unique id). Add a method `getId()` that returns `this.#id`. Add a static method `getCount()` that returns `Order.count`. Create two orders and log their IDs and `Order.getCount()`.

**Hints:**  
- Declare `#id` at the top of the class.  
- In the constructor, assign `this.#id = Order.count` (or similar) and then `Order.count++`.  
- Static method: `static getCount() { return Order.count; }`.

**Expected Output:**
```
0
1
2
```

**Code Stub:**
```javascript
class Order {
  static count = 0;

  constructor() {
  }

  getId() {
  }

  static getCount() {
  }
}

const o1 = new Order();
const o2 = new Order();
console.log(o1.getId());
console.log(o2.getId());
console.log(Order.getCount());
```

---

## Question 10: Inheritance with `extends` and `super`, Method Override

**Problem Statement:**  
Define a class `Shape` with a constructor that takes `name` and sets `this.name = name`, and a method `describe()` that returns `"Shape: " + this.name`. Define a class `Circle` that extends `Shape`, has a constructor taking `name` and `radius`, calls `super(name)`, sets `this.radius = radius`, and overrides `describe()` to return `"Circle: " + this.name + ", radius " + this.radius`. Create a `Circle` instance and call `describe()`.

**Hints:**  
- In the child constructor, the first line must be `super(name)` before using `this`.  
- Override by defining `describe()` again in `Circle`; you can use `super.describe()` if you want to extend the parent string.

**Expected Output:**
```
Circle: Small, radius 5
```

**Code Stub:**
```javascript
class Shape {
  constructor(name) {
  }

  describe() {
  }
}

class Circle extends Shape {
  constructor(name, radius) {
  }

  describe() {
  }
}

const c = new Circle("Small", 5);
console.log(c.describe());
```

---

## Question 11: Private Method and Public API

**Problem Statement:**  
Create a class `Validator` with a private method `#isEmpty(str)` that returns `true` if `str` is undefined, null, or has length 0. Add a public method `validate(str)` that returns `false` if `#isEmpty(str)` is true, and `true` otherwise. Do not expose `#isEmpty` outside the class. Test with an empty string and a non-empty string.

**Hints:**  
- Declare private method with `#isEmpty(str) { ... }`.  
- Call it from `validate` as `this.#isEmpty(str)`.

**Expected Output:**
```
false
true
```

**Code Stub:**
```javascript
class Validator {
  #isEmpty(str) {
  }

  validate(str) {
  }
}

const v = new Validator();
console.log(v.validate(""));
console.log(v.validate("hello"));
```

---

## Question 12: Class with Getter and Private Field

**Problem Statement:**  
Create a class `Temperature` with a private field `#celsius`. The constructor takes a number and sets `#celsius`. Add a getter `celsius` that returns `this.#celsius` and a getter `fahrenheit` that returns `(this.#celsius * 9) / 5 + 32`. Create an instance with 25 and log both getters.

**Hints:**  
- Use `get celsius() { return this.#celsius; }` and similarly for `fahrenheit`.  
- Formula: F = (C * 9/5) + 32.

**Expected Output:**
```
25
77
```

**Code Stub:**
```javascript
class Temperature {
  #celsius;

  constructor(celsius) {
  }

  get celsius() {
  }

  get fahrenheit() {
  }
}

const t = new Temperature(25);
console.log(t.celsius);
console.log(t.fahrenheit);
```

---

## Question 13: Closure Counter with Initial and Step

**Problem Statement:**  
Implement `createCounter(initial, step)`. It should return an object with methods `increment()`, `decrement()`, and `getCount()`. Each call to `increment()` adds `step` to the current count; each call to `decrement()` subtracts `step`. `getCount()` returns the current count. The count must be private (use a closure). Two counters created from `createCounter` must have independent state.

**Hints:**  
- Store a variable (e.g. `count`) in the outer function and return an object whose methods close over that variable.  
- `increment`: count += step; return or use count. Same idea for decrement.

**Expected Output:**
```
2
4
9
```

**Code Stub:**
```javascript
function createCounter(initial, step) {
  return {
    increment() {
    },
    decrement() {
    },
    getCount() {
    }
  };
}

const c1 = createCounter(0, 2);
c1.increment();
console.log(c1.getCount());
c1.increment();
console.log(c1.getCount());
const c2 = createCounter(10, 1);
c2.decrement();
console.log(c2.getCount());
```

---

## Question 14: Memoized Add (Closure Cache)

**Problem Statement:**  
Implement `createMemoizedAdd()`. It returns a function that takes one number `n` and returns `n + n`. The returned function must cache results: if it has already been called with a given `n`, return the cached value instead of recomputing. Use a closure to keep the cache private. Verify with two calls with the same argument; the second should use the cache (you can check by side effect or by return value).

**Hints:**  
- In the outer function, create an object (e.g. `cache = {}`).  
- In the returned function, check `if (cache[n] !== undefined)` (or use `n in cache`); if so return `cache[n]`. Otherwise compute `n + n`, store in `cache[n]`, and return it.

**Expected Output:**
```
10
10
```

**Code Stub:**
```javascript
function createMemoizedAdd() {
  return function (n) {
  };
}

const add = createMemoizedAdd();
console.log(add(5));
console.log(add(5));
```

---

## Question 15: Private Bank Account with Closures

**Problem Statement:**  
Implement `createBankAccount(initialBalance)`. It must return an object with methods `deposit(amount)`, `withdraw(amount)`, and `getBalance()`. The balance must be private (only accessible via these methods). `deposit` adds to the balance and returns the new balance; `withdraw` subtracts and returns the new balance (assume no overdraft check for simplicity). `getBalance()` returns the current balance.

**Hints:**  
- Keep a variable `balance` in the outer function.  
- Return an object with three methods that read/update that variable. Do not attach balance to the returned object.

**Expected Output:**
```
1500
1300
1300
```

**Code Stub:**
```javascript
function createBankAccount(initialBalance) {
  return {
    deposit(amount) {
    },
    withdraw(amount) {
    },
    getBalance() {
    }
  };
}

const acc = createBankAccount(1000);
console.log(acc.deposit(500));
console.log(acc.withdraw(200));
console.log(acc.getBalance());
```

---

## Question 16: Function Factory (Multiplier)

**Problem Statement:**  
Implement `createMultiplier(factor)`. It should return a function that takes a number `n` and returns `n * factor`. Create `double` and `triple` using `createMultiplier(2)` and `createMultiplier(3)`, and call them with 7.

**Hints:**  
- Return a function that closes over `factor` and computes `n * factor`.

**Expected Output:**
```
14
21
```

**Code Stub:**
```javascript
function createMultiplier(factor) {
  return function (n) {
  };
}

const double = createMultiplier(2);
const triple = createMultiplier(3);
console.log(double(7));
console.log(triple(7));
```

---

## Question 17: Polyfill for `Array.prototype.myMap`

**Problem Statement:**  
Implement `Array.prototype.myMap` so that it works like the native `Array.prototype.map`. It takes a callback and optionally a `thisArg`. For each element of the array (only iterate over existing indices, 0 to length-1), call the callback with (element, index, array) and push the return value into a new array, then return that array. Do not use the built-in `map`. Handle the case when `this` is null/undefined (throw). Callback should be called with the correct `thisArg` if provided.

**Hints:**  
- Use a for loop from 0 to this.length - 1.  
- results.push(callback.call(thisArg, this[i], i, this)).  
- Return the results array.

**Expected Output:**
```
[ 2, 4, 6 ]
```

**Code Stub:**
```javascript
Array.prototype.myMap = function (callback, thisArg) {
};

const arr = [1, 2, 3];
const result = arr.myMap(function (x) {
  return x * 2;
});
console.log(result);
```

---

## Question 18: Polyfill for `Array.prototype.myFilter`

**Problem Statement:**  
Implement `Array.prototype.myFilter` that behaves like the native `filter`. It takes a callback (and optionally `thisArg`). For each element, if the callback returns a truthy value, push that element into a new array. Return the new array. Do not use the built-in `filter`.

**Hints:**  
- Loop over the array; call callback(element, index, array).  
- If the result is truthy, push the element. Return the new array.

**Expected Output:**
```
[ 2, 4, 6 ]
```

**Code Stub:**
```javascript
Array.prototype.myFilter = function (callback, thisArg) {
};

const arr = [1, 2, 3, 4, 5, 6];
const evens = arr.myFilter(function (x) {
  return x % 2 === 0;
});
console.log(evens);
```

---

## Question 19: Polyfill for `Array.prototype.myReduce`

**Problem Statement:**  
Implement `Array.prototype.myReduce(callback, initialValue)`. The callback receives (accumulator, currentValue, index, array). If `initialValue` is provided, use it as the initial accumulator and start from index 0; otherwise use the first element as accumulator and start from index 1. If the array is empty and no initialValue is given, throw a TypeError. Return the final accumulator.

**Hints:**  
- If length is 0 and initialValue is undefined, throw new TypeError("Reduce of empty array with no initial value").  
- Set acc and start index based on whether initialValue is provided.  
- Loop and do acc = callback(acc, this[i], i, this). Return acc.

**Expected Output:**
```
15
15
```

**Code Stub:**
```javascript
Array.prototype.myReduce = function (callback, initialValue) {
};

const arr = [1, 2, 3, 4, 5];
const sum1 = arr.myReduce((acc, curr) => acc + curr, 0);
const sum2 = arr.myReduce((acc, curr) => acc + curr);
console.log(sum1);
console.log(sum2);
```

---

## Question 20: Chaining HOFs (map, filter, reduce) Without Polyfills

**Problem Statement:**  
You are given an array of objects: `[ { name: "Alice", age: 25 }, { name: "Bob", age: 17 }, { name: "Charlie", age: 30 } ]`. Using only the native array methods `map`, `filter`, and `reduce`, (1) filter to keep only persons with `age >= 18`, (2) map each to a string `"<name> is adult"`, (3) reduce to a single string where each sentence is separated by `"; "` (e.g. `"Alice is adult; Charlie is adult"`). Do not implement polyfills; use the built-in methods.

**Hints:**  
- filter( p => p.age >= 18 ), then map( p => `${p.name} is adult` ), then reduce to concatenate with `"; "` as separator.  
- In reduce, initial value can be `""` and accumulator can be `acc + (acc ? "; " : "") + current`.

**Expected Output:**
```
Alice is adult; Charlie is adult
```

**Code Stub:**
```javascript
const people = [
  { name: "Alice", age: 25 },
  { name: "Bob", age: 17 },
  { name: "Charlie", age: 30 }
];

let result;
console.log(result);
```

---

## Summary: Topic Coverage

| Q# | Class 1 (this) | Class 2 (Constructor/Prototype) | Class 3 (Classes) | Class 4 (Closures) | Class 5 (HOF/Polyfills) |
|----|----------------|----------------------------------|-------------------|--------------------|--------------------------|
| 1  | ✓              |                                  |                   |                    |                          |
| 2  | ✓              |                                  |                   |                    |                          |
| 3  | ✓              |                                  |                   | ✓                  |                          |
| 4  | ✓              |                                  |                   |                    |                          |
| 5  |                | ✓                                |                   |                    |                          |
| 6  |                | ✓                                |                   |                    |                          |
| 7  |                | ✓                                |                   |                    |                          |
| 8  |                | ✓                                |                   |                    |                          |
| 9  |                |                                  | ✓                 |                    |                          |
| 10 |                |                                  | ✓                 |                    |                          |
| 11 |                |                                  | ✓                 |                    |                          |
| 12 |                |                                  | ✓                 |                    |                          |
| 13 |                |                                  |                   | ✓                  |                          |
| 14 |                |                                  |                   | ✓                  |                          |
| 15 |                |                                  |                   | ✓                  |                          |
| 16 |                |                                  |                   | ✓                  |                          |
| 17 |                |                                  |                   |                    | ✓                        |
| 18 |                |                                  |                   |                    | ✓                        |
| 19 |                |                                  |                   |                    | ✓                        |
| 20 |                |                                  |                   |                    | ✓                        |

---

*End of 20 Coding Questions*
