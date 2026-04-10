# Problem 11: Stackable Logger Decorators

**Topic:** Decorator Pattern
**Difficulty:** Hard

---

## Scenario

A logging framework starts with a simple console logger. Clients want to **optionally** add behaviors:
- Timestamp prefix
- Log level filtering (only log WARNING and above)
- Encryption (simulate by reversing the string)
- File output (simulate by printing to `[FILE] ...`)

These must be **stackable in any combination and order**.

## Requirements

1. Create `Logger` interface:
   - `void log(String level, String message)` (levels: "DEBUG", "INFO", "WARNING", "ERROR")

2. Create `ConsoleLogger` (concrete component):
   - Prints: `"[level] message"`

3. Create abstract `LoggerDecorator` implements `Logger`:
   - Holds `Logger inner`
   - Default `log()` delegates to `inner.log()`

4. Create decorators:
   - `TimestampDecorator` -- prepends `"[2024-01-01T12:00:00]"` (use `Instant.now()`) to message, then delegates
   - `LevelFilterDecorator` -- only passes through if level is WARNING or ERROR, silently drops DEBUG/INFO
   - `EncryptionDecorator` -- reverses the message string before delegating (simulating encryption)
   - `FileDecorator` -- prints `"[FILE] [level] message"` AND delegates to inner (logs to both file and inner)

5. `Main` demonstrating:
   - **Config A:** Timestamp + Console (all messages get timestamp)
   - **Config B:** LevelFilter + Timestamp + Console (only warnings/errors, with timestamp)
   - **Config C:** FileDecorator + EncryptionDecorator + Console (logs encrypted to file AND console)

## What to create

```
Logger.java
ConsoleLogger.java
LoggerDecorator.java
TimestampDecorator.java
LevelFilterDecorator.java
EncryptionDecorator.java
FileDecorator.java
Main.java
```

## What I'll check

- All decorators implement `Logger` and wrap another `Logger`
- Decorators are composable in any order
- `LoggerDecorator` abstract class reduces boilerplate
- Each decorator does ONE thing (SRP)
- Main shows at least 3 different compositions
- Adding a new decorator requires ZERO changes to existing decorators (OCP)
