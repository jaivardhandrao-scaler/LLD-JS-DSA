# Problem 1: Library Management System

**Topic:** Encapsulation, Inheritance, Polymorphism
**Difficulty:** Easy

---

## Scenario

You are building a library system that manages different types of items: **Books**, **DVDs**, and **Magazines**. All items can be borrowed and returned. Each type calculates its **late fee** differently.

## Requirements

1. Create an abstract class `LibraryItem` with:
   - `private` fields: `id` (String), `title` (String), `isBorrowed` (boolean)
   - Constructor that takes `id` and `title`, validates neither is null/blank
   - `borrow()` method -- sets `isBorrowed` to true, throws if already borrowed
   - `returnItem()` method -- sets `isBorrowed` to false, throws if not borrowed
   - Abstract method: `double calculateLateFee(int daysLate)`
   - Getters (no setters for `id` and `title`)

2. Create `Book` (late fee = Rs.2 per day), `DVD` (late fee = Rs.5 per day), `Magazine` (late fee = Rs.1 per day, max Rs.10)

3. Create a `Library` class with:
   - A `List<LibraryItem>` of items
   - `addItem(LibraryItem item)` method
   - `borrowItem(String id)` -- finds item by id and borrows it
   - `calculateTotalFees(Map<String, Integer> daysLateById)` -- calculates total late fees across all items using polymorphism

4. Create a `Main` class that demonstrates:
   - Adding 2 books, 1 DVD, 1 magazine
   - Borrowing some items
   - Calculating total fees polymorphically (no `instanceof` allowed)

## What to create

```
LibraryItem.java
Book.java
DVD.java
Magazine.java
Library.java
Main.java
```

## What I'll check

- `LibraryItem` fields are `private`, constructor validates
- `calculateLateFee()` uses polymorphism (no `instanceof` or type checks in `Library`)
- Proper use of `abstract` class and `@Override`
- `borrow()`/`returnItem()` enforce state correctly
