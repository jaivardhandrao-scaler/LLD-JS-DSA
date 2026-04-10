# Problem 8: HTTP Request Builder

**Topic:** Builder Pattern
**Difficulty:** Medium

---

## Scenario

You're building an HTTP client library. An `HttpRequest` has many fields, some required, some optional. The object must be **immutable** after construction.

## Requirements

1. Create an immutable `HttpRequest` class with:
   - **Required:** `String method` (GET/POST/PUT/DELETE), `String url`
   - **Optional (with defaults):** `Map<String, String> headers` (default: empty), `byte[] body` (default: null), `int timeoutMs` (default: 30000), `boolean followRedirects` (default: true)

2. Create a `static inner class Builder`:
   - Required fields passed to Builder constructor: `Builder(String method, String url)`
   - Fluent setters for optional fields, each returns `this`
   - `addHeader(String key, String value)` -- adds to headers map
   - `body(byte[] body)`
   - `timeoutMs(int ms)`
   - `followRedirects(boolean follow)`
   - `HttpRequest build()` -- validates (method not blank, url not blank, timeout > 0), returns immutable object

3. `HttpRequest` constructor is `private`, takes `Builder`
4. Headers map must be defensively copied (immutable after build)
5. Body byte array must be defensively copied

6. `Main` demonstrating:
   - A simple GET request (only required fields)
   - A POST request with headers, body, and custom timeout

## What to create

```
HttpRequest.java
Main.java
```

## What I'll check

- `HttpRequest` is immutable (`final` class, `private final` fields)
- Builder is a `public static` inner class
- Required fields enforced in Builder constructor
- Optional fields have sensible defaults
- Defensive copies of `Map` and `byte[]`
- Fluent API: `new HttpRequest.Builder("POST", "/api").addHeader("Content-Type", "application/json").body(data).build()`
