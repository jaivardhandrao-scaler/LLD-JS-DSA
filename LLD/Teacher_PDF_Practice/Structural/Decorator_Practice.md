# Decorator Pattern -- Practice (from DesignPattern_Decorator.pdf)

---

## 1. Viva Questions

**Q1. In the HTTP Client pipeline, what is the role of `HttpClientDecorator` and why is it abstract?**

`HttpClientDecorator` is the abstract decorator class. It implements `HttpClient`, holds a reference to an inner `HttpClient`, and provides a default `send()` that simply delegates to the inner client. It is abstract so that it cannot be instantiated on its own -- concrete decorators like `RetryingHttpClient` or `CachingHttpClient` must extend it and override `send()` with their additional behavior. This lets every decorator share the delegation boilerplate without repeating it.

---

**Q2. Explain the retry logic inside `RetryingHttpClient`. When does it retry, and how does backoff work?**

`RetryingHttpClient` stores `maxAttempts` and `baseBackoffMillis`. It calls the inner client's `send()`. If the response status is >= 500 (server error), it retries up to `maxAttempts` times. Between retries it sleeps for `baseBackoffMillis` (the exact backoff strategy may multiply per attempt). If all attempts are exhausted and the status is still >= 500, it returns the last failed response. It does NOT retry for client-side errors (4xx) or successes (2xx/3xx).

---

**Q3. How does `CachingHttpClient` decide whether to cache a response? What data structures does it use?**

`CachingHttpClient` uses a `ConcurrentHashMap` as its cache and a `ttlMillis` field for time-to-live. It only caches responses to GET requests. Before calling the inner client, it checks whether the cache contains a non-expired entry for the request URL. If a valid cached response exists, it returns it directly without delegating. Otherwise it delegates to the inner client, stores the response with a timestamp, and returns it. Non-GET requests (POST, PUT, DELETE) bypass the cache entirely.

---

**Q4. What header does `CompressingHttpClient` add, and what header does `AuthHttpClient` add?**

`CompressingHttpClient` adds the header `Accept-Encoding: gzip` to the outgoing `HttpRequest`. `AuthHttpClient` uses a `Supplier<String> tokenSupplier` to obtain a token at call time and adds the header `Authorization: Bearer <token>`. Both decorators modify the request headers before delegating to the inner client.

---

**Q5. The PDF shows two wiring orders. What is Order A and Order B, and why does the order matter?**

Order A: `Metrics(Retry(Cache(Auth(Compress(Base)))))` -- metrics wraps everything so it times the full pipeline including retries; caching sits inside retry so a cache miss triggers a retry-capable call; auth and compression happen closest to the base.

Order B: `Metrics(Cache(Retry(Auth(Base))))` -- caching is outside retry, so a cache hit avoids retries altogether; compression is removed.

Order matters because each decorator only sees the behavior of its inner client. Placing Cache inside Retry means retries can hit the cache repeatedly; placing Cache outside Retry means a single cache miss leads to a retry-capable call, and a cache hit skips retries entirely. Similarly, Metrics wrapping the outermost layer measures total time including retries and cache lookups, while placing Metrics deeper would only measure a subset.

---

**Q6. In the Game Damage domain, how does `ArmorPiercing` work differently from `PoisonDamage`?**

`ArmorPiercing` reduces the `baseHp` by a flat `flatReduction` BEFORE passing it to the inner `DamageSource`. This means the inner damage source operates on already-reduced HP, so percentage-based effects apply to a smaller number. `PoisonDamage` subtracts a flat `dot` (damage over time) AFTER the inner `DamageSource` has applied its damage. So ArmorPiercing modifies the INPUT to inner decorators, while PoisonDamage modifies the OUTPUT.

---

**Q7. How does `CriticalStrike` calculate its bonus damage?**

`CriticalStrike` first calls the inner `DamageSource.applyTo(baseHp)` to get the resulting HP. It computes the delta (damage dealt) as `baseHp - resultHp`. It then multiplies this delta by its `multiplier` to get the amplified damage. The final HP returned is `baseHp - (delta * multiplier)`. This means CriticalStrike amplifies whatever damage the inner source dealt, rather than applying a flat amount.

---

**Q8. The PDF discusses Proxy vs Decorator. What is the key distinction?**

Both Proxy and Decorator wrap an object and implement the same interface. The key distinction is intent: a Proxy controls ACCESS to the object (e.g., lazy loading, access control, remote proxy), while a Decorator adds BEHAVIOR to the object (e.g., adding caching, retries, compression). A Proxy typically manages the lifecycle or access policy of its subject, while a Decorator transparently layers on extra functionality. Structurally they are nearly identical, but semantically they serve different purposes.

---

**Q9. How is `MetricsHttpClient` implemented, and what thread-safety concerns does it address?**

`MetricsHttpClient` uses an `AtomicLong` for `calls` (total number of invocations) and another `AtomicLong` for `totalNanos` (cumulative time). In `send()`, it records `System.nanoTime()` before delegating to the inner client, then records the time after. It atomically increments `calls` and adds the elapsed nanos to `totalNanos`. Using `AtomicLong` ensures thread safety without explicit synchronization, so multiple threads can call `send()` concurrently and metrics remain consistent.

---

**Q10. The PDF shows a JUnit test for `RetryingHttpClient` using a `FlakyClient` stub. Describe the testing approach and why stubs are useful here.**

The test creates a `FlakyClient` stub that returns a 500 status for the first N calls and then returns 200. A `RetryingHttpClient` wraps this stub with a configured `maxAttempts`. The test asserts that after enough retries the final response is 200, verifying the retry logic. Stubs are useful because they let you control exactly when failures occur without depending on a real HTTP server. This isolates the retry logic for deterministic, fast unit tests. The PDF emphasizes this as a key benefit of the Decorator pattern -- each decorator can be tested independently by wrapping a simple stub.

---

## 2. MCQ Quiz

**Q1. Which interface does `BaseHttpClient` implement?**
- A) `HttpClientDecorator`
- B) `HttpClient`
- C) `HttpRequest`
- D) `HttpResponse`

<details><summary>Answer</summary>B) HttpClient. BaseHttpClient is the concrete component that directly implements the HttpClient interface.</details>

---

**Q2. What does `BaseHttpClient.send()` return in the PDF's example?**
- A) A 404 response with an empty body
- B) A 200 response with the echo of the request body
- C) A 500 response to simulate server errors
- D) Null

<details><summary>Answer</summary>B) A 200 response with the echo of the request body. It acts as a simple echo server for demonstration.</details>

---

**Q3. `RetryingHttpClient` retries when the response status is:**
- A) >= 400
- B) >= 300
- C) >= 500
- D) == 503 only

<details><summary>Answer</summary>C) >= 500. It retries on server errors (5xx status codes).</details>

---

**Q4. What data structure does `CachingHttpClient` use for its cache?**
- A) `HashMap`
- B) `TreeMap`
- C) `ConcurrentHashMap` (ConcurrentMap)
- D) `LinkedHashMap`

<details><summary>Answer</summary>C) ConcurrentHashMap (ConcurrentMap). This ensures thread-safe cache access.</details>

---

**Q5. Which HTTP methods does `CachingHttpClient` cache?**
- A) All methods
- B) GET and POST
- C) GET only
- D) GET and HEAD

<details><summary>Answer</summary>C) GET only. Non-GET requests bypass the cache.</details>

---

**Q6. What header does `CompressingHttpClient` add to the request?**
- A) `Content-Encoding: gzip`
- B) `Accept-Encoding: gzip`
- C) `Transfer-Encoding: chunked`
- D) `Content-Type: application/gzip`

<details><summary>Answer</summary>B) Accept-Encoding: gzip. It signals to the server that the client accepts gzip-compressed responses.</details>

---

**Q7. `AuthHttpClient` obtains its token via:**
- A) A constructor String parameter
- B) A `Supplier<String>` called at each request
- C) An environment variable
- D) A static final field

<details><summary>Answer</summary>B) A Supplier&lt;String&gt; called at each request. This allows the token to be refreshed dynamically.</details>

---

**Q8. The header added by `AuthHttpClient` is:**
- A) `X-Auth-Token: <token>`
- B) `Authorization: Basic <token>`
- C) `Authorization: Bearer <token>`
- D) `Cookie: session=<token>`

<details><summary>Answer</summary>C) Authorization: Bearer &lt;token&gt;.</details>

---

**Q9. In Order A `Metrics(Retry(Cache(Auth(Compress(Base)))))`, which decorator's `send()` is called first when a request comes in?**
- A) `BaseHttpClient`
- B) `CompressingHttpClient`
- C) `MetricsHttpClient`
- D) `RetryingHttpClient`

<details><summary>Answer</summary>C) MetricsHttpClient. The outermost decorator receives the call first, then delegates inward.</details>

---

**Q10. In Order B `Metrics(Cache(Retry(Auth(Base))))`, if the cache has a valid entry, which decorators are skipped?**
- A) Only Retry
- B) Retry, Auth, and Base
- C) Auth and Base only
- D) None, all decorators always execute

<details><summary>Answer</summary>B) Retry, Auth, and Base. A cache hit means CachingHttpClient returns the cached response without delegating to its inner client, so everything inside Cache is skipped.</details>

---

**Q11. `MetricsHttpClient` uses which type for its counters?**
- A) `int`
- B) `volatile long`
- C) `AtomicLong`
- D) `synchronized Long`

<details><summary>Answer</summary>C) AtomicLong. Both the calls counter and the totalNanos counter are AtomicLong for lock-free thread safety.</details>

---

**Q12. In the Game Damage domain, `DamageSource.applyTo(double baseHp)` returns:**
- A) The damage dealt
- B) The remaining HP after damage
- C) A boolean indicating if the target died
- D) The multiplier applied

<details><summary>Answer</summary>B) The remaining HP after damage. The method returns the new HP value.</details>

---

**Q13. `BaseHit` in the Game Damage domain applies:**
- A) Percentage-based damage
- B) Flat damage subtracted from baseHp
- C) Damage that scales with level
- D) Zero damage

<details><summary>Answer</summary>B) Flat damage subtracted from baseHp. BaseHit is the concrete component dealing a simple flat amount.</details>

---

**Q14. `ArmorPiercing` modifies:**
- A) The output HP from the inner decorator
- B) The baseHp input BEFORE passing it to the inner decorator
- C) The multiplier of CriticalStrike
- D) The dot value of PoisonDamage

<details><summary>Answer</summary>B) The baseHp input BEFORE passing it to the inner decorator. It reduces HP by flatReduction before delegation.</details>

---

**Q15. `CriticalStrike` with multiplier 2.0 wrapping an inner source that deals 30 damage to a target with 100 HP results in:**
- A) 40 HP remaining
- B) 70 HP remaining
- C) 100 HP remaining
- D) 0 HP remaining

<details><summary>Answer</summary>A) 40 HP remaining. Inner deals 30 damage (100 -> 70), delta = 30, amplified delta = 30 * 2.0 = 60, final HP = 100 - 60 = 40.</details>

---

**Q16. `PoisonDamage` applies its damage-over-time value:**
- A) Before the inner decorator
- B) After the inner decorator returns
- C) Instead of the inner decorator
- D) Only if the target HP is above 50%

<details><summary>Answer</summary>B) After the inner decorator returns. It subtracts the dot amount from the HP returned by the inner source.</details>

---

**Q17. Which SOLID principle does the Decorator pattern primarily support?**
- A) Single Responsibility Principle
- B) Open/Closed Principle
- C) Dependency Inversion Principle
- D) Interface Segregation Principle

<details><summary>Answer</summary>B) Open/Closed Principle. Classes are open for extension (adding new decorators) but closed for modification (existing code is untouched).</details>

---

**Q18. Which pitfall from the PDF describes the problem of checking `instanceof` on a decorated object?**
- A) Order confusion
- B) State leakage
- C) Type checks / breaking transparency
- D) Over-decoration

<details><summary>Answer</summary>C) Type checks / breaking transparency. Using instanceof on a decorated object can fail because the outermost type is the decorator, not the concrete component, violating the pattern's transparency.</details>

---

**Q19. In the `RetryingHttpClient` JUnit test, what does `FlakyClient` do?**
- A) Throws exceptions randomly
- B) Returns 500 for the first N calls, then returns 200
- C) Always returns 200
- D) Returns different status codes based on the URL

<details><summary>Answer</summary>B) Returns 500 for the first N calls, then returns 200. This deterministic behavior lets the test verify that RetryingHttpClient retries correctly and eventually succeeds.</details>

---

**Q20. The "state leakage" pitfall refers to:**
- A) A decorator modifying the request headers permanently across unrelated calls
- B) Forgetting to call super.send()
- C) Using too many decorators
- D) Caching POST responses

<details><summary>Answer</summary>A) A decorator modifying the request headers permanently across unrelated calls. If a decorator mutates shared state (e.g., the original request object) rather than creating copies, side effects leak between invocations.</details>

---

## 3. Self-Scoring Table

| # | Topic | Confident (2) | Partially (1) | Unsure (0) |
|---|-------|:---:|:---:|:---:|
| 1 | HttpClient interface and BaseHttpClient role | | | |
| 2 | HttpClientDecorator abstract class purpose | | | |
| 3 | RetryingHttpClient retry logic (>= 500, maxAttempts, backoff) | | | |
| 4 | CachingHttpClient (ConcurrentMap, TTL, GET-only) | | | |
| 5 | CompressingHttpClient (Accept-Encoding: gzip) | | | |
| 6 | AuthHttpClient (Supplier<String>, Bearer token) | | | |
| 7 | MetricsHttpClient (AtomicLong, nanoTime) | | | |
| 8 | Wiring order matters (Order A vs Order B) | | | |
| 9 | DamageSource interface (applyTo, describe) | | | |
| 10 | BaseHit (flat damage) | | | |
| 11 | ArmorPiercing (reduces input HP before inner) | | | |
| 12 | CriticalStrike (multiplies delta from inner) | | | |
| 13 | PoisonDamage (subtracts dot after inner) | | | |
| 14 | Order-dependent damage builds | | | |
| 15 | Proxy vs Decorator distinction | | | |
| 16 | AOP vs Decorator | | | |
| 17 | JUnit testing with FlakyClient stub | | | |
| 18 | Pitfalls: order confusion, state leakage, swallowed errors, over-decoration, type checks | | | |
| 19 | LSP contract compliance | | | |
| 20 | SOLID mapping for Decorator pattern | | | |

**Score: ___ / 40**

---

## 4. Coding Problems

### Problem 1: Implement RetryingHttpClient

Given the following interfaces and base class:

```java
public class HttpRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final byte[] body;
    // constructor, getters
}

public class HttpResponse {
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;
    // constructor, getters
}

public interface HttpClient {
    HttpResponse send(HttpRequest request);
}

public class BaseHttpClient implements HttpClient {
    @Override
    public HttpResponse send(HttpRequest request) {
        return new HttpResponse(200, Map.of(), request.getBody());
    }
}

public abstract class HttpClientDecorator implements HttpClient {
    protected final HttpClient inner;
    public HttpClientDecorator(HttpClient inner) { this.inner = inner; }
    @Override
    public HttpResponse send(HttpRequest request) { return inner.send(request); }
}
```

Implement `RetryingHttpClient` with `maxAttempts` and `baseBackoffMillis`. It should retry when the response status is >= 500, sleeping between attempts.

<details><summary>Solution</summary>

```java
public class RetryingHttpClient extends HttpClientDecorator {
    private final int maxAttempts;
    private final long baseBackoffMillis;

    public RetryingHttpClient(HttpClient inner, int maxAttempts, long baseBackoffMillis) {
        super(inner);
        this.maxAttempts = maxAttempts;
        this.baseBackoffMillis = baseBackoffMillis;
    }

    @Override
    public HttpResponse send(HttpRequest request) {
        HttpResponse response = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = inner.send(request);
            if (response.getStatus() < 500) {
                return response;
            }
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(baseBackoffMillis * attempt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return response;
                }
            }
        }
        return response; // return last failed response after all attempts exhausted
    }
}
```

</details>

---

### Problem 2: Implement CriticalStrike and Compose a Damage Pipeline

Given:

```java
public interface DamageSource {
    double applyTo(double baseHp);
    String describe();
}

public class BaseHit implements DamageSource {
    private final double flatDamage;
    public BaseHit(double flatDamage) { this.flatDamage = flatDamage; }

    @Override
    public double applyTo(double baseHp) { return baseHp - flatDamage; }

    @Override
    public String describe() { return "BaseHit(" + flatDamage + ")"; }
}

public abstract class DamageDecorator implements DamageSource {
    protected final DamageSource inner;
    public DamageDecorator(DamageSource inner) { this.inner = inner; }
}
```

(a) Implement `CriticalStrike` that takes a `multiplier` and amplifies the damage delta from the inner source.

(b) Implement `ArmorPiercing` that takes a `flatReduction` and reduces `baseHp` before delegating to the inner source.

(c) Build the pipeline: `CriticalStrike(2.0, ArmorPiercing(10, BaseHit(20)))` and manually trace `applyTo(100)`.

<details><summary>Solution</summary>

```java
public class CriticalStrike extends DamageDecorator {
    private final double multiplier;

    public CriticalStrike(DamageSource inner, double multiplier) {
        super(inner);
        this.multiplier = multiplier;
    }

    @Override
    public double applyTo(double baseHp) {
        double resultHp = inner.applyTo(baseHp);
        double delta = baseHp - resultHp;
        double amplifiedDelta = delta * multiplier;
        return baseHp - amplifiedDelta;
    }

    @Override
    public String describe() {
        return "CriticalStrike(x" + multiplier + ", " + inner.describe() + ")";
    }
}

public class ArmorPiercing extends DamageDecorator {
    private final double flatReduction;

    public ArmorPiercing(DamageSource inner, double flatReduction) {
        super(inner);
        this.flatReduction = flatReduction;
    }

    @Override
    public double applyTo(double baseHp) {
        double reducedHp = baseHp - flatReduction;
        return inner.applyTo(reducedHp);
    }

    @Override
    public String describe() {
        return "ArmorPiercing(" + flatReduction + ", " + inner.describe() + ")";
    }
}
```

**Trace of `CriticalStrike(2.0, ArmorPiercing(10, BaseHit(20))).applyTo(100)`:**

1. `CriticalStrike.applyTo(100)` calls `inner.applyTo(100)` where inner = ArmorPiercing
2. `ArmorPiercing.applyTo(100)`: reducedHp = 100 - 10 = 90, calls `inner.applyTo(90)` where inner = BaseHit
3. `BaseHit.applyTo(90)`: returns 90 - 20 = 70
4. ArmorPiercing returns 70
5. Back in CriticalStrike: resultHp = 70, delta = 100 - 70 = 30, amplifiedDelta = 30 * 2.0 = 60
6. CriticalStrike returns 100 - 60 = **40.0**

</details>

---

### Problem 3: Wire an HTTP Client Pipeline and Write a JUnit Test

(a) Wire the following pipeline matching Order A from the PDF:
`Metrics -> Retry(3 attempts, 100ms backoff) -> Cache(60000ms TTL) -> Auth(token supplier) -> Compress -> Base`

(b) Write a JUnit test for `RetryingHttpClient` using a `FlakyClient` stub that returns 500 for the first 2 calls and 200 on the third call. Assert that with `maxAttempts=3`, the final response is 200.

<details><summary>Solution</summary>

**(a) Pipeline Wiring:**

```java
public class PipelineWiring {
    public static HttpClient buildOrderA() {
        HttpClient base = new BaseHttpClient();
        HttpClient compressed = new CompressingHttpClient(base);
        HttpClient authenticated = new AuthHttpClient(compressed, () -> "my-secret-token");
        HttpClient cached = new CachingHttpClient(authenticated, 60000L);
        HttpClient retrying = new RetryingHttpClient(cached, 3, 100L);
        HttpClient metricsEnabled = new MetricsHttpClient(retrying);
        return metricsEnabled;
    }
}
```

**(b) JUnit Test:**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryingHttpClientTest {

    // Stub that fails N times then succeeds
    static class FlakyClient implements HttpClient {
        private int callCount = 0;
        private final int failuresBeforeSuccess;

        FlakyClient(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Override
        public HttpResponse send(HttpRequest request) {
            callCount++;
            if (callCount <= failuresBeforeSuccess) {
                return new HttpResponse(500, Map.of(), new byte[0]);
            }
            return new HttpResponse(200, Map.of(), "OK".getBytes());
        }

        public int getCallCount() { return callCount; }
    }

    @Test
    void testRetrySucceedsOnThirdAttempt() {
        FlakyClient flaky = new FlakyClient(2); // fail twice, succeed on 3rd
        RetryingHttpClient retrying = new RetryingHttpClient(flaky, 3, 100L);

        HttpRequest request = new HttpRequest("GET", "/test", Map.of(), new byte[0]);
        HttpResponse response = retrying.send(request);

        assertEquals(200, response.getStatus());
        assertEquals(3, flaky.getCallCount());
    }

    @Test
    void testRetryExhaustedReturnsLastFailure() {
        FlakyClient flaky = new FlakyClient(5); // always fails within 3 attempts
        RetryingHttpClient retrying = new RetryingHttpClient(flaky, 3, 100L);

        HttpRequest request = new HttpRequest("GET", "/test", Map.of(), new byte[0]);
        HttpResponse response = retrying.send(request);

        assertEquals(500, response.getStatus());
        assertEquals(3, flaky.getCallCount());
    }
}
```

</details>
