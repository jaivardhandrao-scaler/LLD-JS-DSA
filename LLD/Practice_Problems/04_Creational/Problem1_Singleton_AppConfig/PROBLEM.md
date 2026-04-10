# Problem 6: Application Configuration Singleton

**Topic:** Singleton Pattern (Holder Idiom)
**Difficulty:** Easy

---

## Scenario

Your application has a `AppConfig` that loads settings from a properties map. There must be exactly ONE config instance across the entire app. Multiple instances would cause inconsistency.

## Requirements

1. Create `AppConfig` using the **Holder Idiom**:
   - `private` fields: `Map<String, String> properties`
   - Private constructor that initializes default properties: `{"app.name": "MyApp", "app.version": "1.0", "db.host": "localhost", "db.port": "5432"}`
   - `String get(String key)` -- returns value or null
   - `String getOrDefault(String key, String defaultValue)`
   - `static AppConfig getInstance()` using Holder

2. Create `Main` that:
   - Gets the instance twice and proves they're the same (`==`)
   - Reads some config values
   - Prints `"Same instance? true"`

## What to create

```
AppConfig.java
Main.java
```

## What I'll check

- Private constructor
- Static inner `Holder` class with `private static final` instance
- `getInstance()` returns `Holder.INSTANCE`
- Properties map is not exposed directly (encapsulation)
- Main proves single instance
