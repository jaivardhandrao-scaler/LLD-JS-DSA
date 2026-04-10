# Problem 9: UI Theme Prototype Registry

**Topic:** Prototype Pattern + Registry
**Difficulty:** Medium

---

## Scenario

A UI framework lets users create custom themes. Each theme has many properties (colors, fonts, borders, shadows). Creating themes from scratch is tedious. Users should be able to pick a base theme from a catalog and get a **copy** they can customize without affecting the original.

## Requirements

1. Create a `Theme` interface with:
   - `Theme copy()` -- returns a deep-enough clone
   - `String name()`
   - `void setName(String name)` -- to rename the copy
   - `String describe()` -- returns a summary of the theme

2. Create `ColorScheme` (mutable, has `primary`, `secondary`, `background` String fields, with a `copy()`)

3. Create `DarkTheme` implementing `Theme`:
   - Fields: `String name`, `ColorScheme colors`, `String fontFamily`, `int borderRadius`
   - `copy()` must deep-copy `ColorScheme` (it's mutable!) but can share `fontFamily` (String is immutable)

4. Create `LightTheme` implementing `Theme` (same fields, different defaults)

5. Create `ThemeRegistry`:
   - `Map<String, Theme> store`
   - `register(String key, Theme prototype)`
   - `Theme create(String key)` -- returns `prototype.copy()`, throws if key unknown
   - `List<String> available()` -- returns all registered keys

6. `Main`:
   - Register "dark" and "light" prototypes
   - Create a theme from "dark", customize it (change name, change primary color)
   - Print both the original "dark" prototype (unchanged) and the customized copy

## What to create

```
Theme.java
ColorScheme.java
DarkTheme.java
LightTheme.java
ThemeRegistry.java
Main.java
```

## What I'll check

- `copy()` creates a genuinely independent clone
- `ColorScheme` is deep-copied (mutable object)
- Modifying the copy does NOT affect the original prototype in registry
- Registry returns clones, not the original
- Main demonstrates independence
