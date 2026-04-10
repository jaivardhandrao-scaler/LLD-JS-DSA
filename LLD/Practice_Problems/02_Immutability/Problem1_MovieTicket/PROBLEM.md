# Problem 3: Immutable Movie Ticket

**Topic:** Immutable classes, defensive copying
**Difficulty:** Easy

---

## Scenario

A cinema booking system issues tickets. Once a ticket is issued, it must **never be modified** -- not the movie name, not the seat, not the add-ons. This is critical because tickets are shared across threads (display boards, receipt printers, analytics).

## Requirements

1. Create an immutable `MovieTicket` class with:
   - `String movieName`
   - `String seatNumber`
   - `double price`
   - `List<String> addOns` (e.g., "Popcorn", "3D Glasses")
   - `Instant bookedAt`

2. Apply ALL 6 immutability rules:
   - Class is `final`
   - All fields are `private final`
   - No setters
   - Defensive copy of `addOns` in constructor
   - Return unmodifiable view of `addOns` in getter
   - Validate in constructor (movieName and seatNumber must not be null/blank, price >= 0)

3. Create a `Main` class that **proves** immutability by:
   - Creating a ticket with a mutable `ArrayList` of add-ons
   - Modifying the original list after construction (should NOT affect ticket)
   - Trying to modify the list returned by `getAddOns()` (should throw `UnsupportedOperationException`)
   - Printing the ticket to show it's unchanged

## What to create

```
MovieTicket.java
Main.java
```

## What I'll check

- Class is `final`
- All fields `private final`
- `List.copyOf()` or `new ArrayList<>(addOns)` in constructor
- `Collections.unmodifiableList()` or `List.copyOf()` in getter
- Constructor validates inputs
- Main class actually demonstrates the two attack scenarios
