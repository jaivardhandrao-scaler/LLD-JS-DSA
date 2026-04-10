# Adapter Pattern -- Practice Sheet
**Based on: DesignPattern_Adapter.pdf (LLD Course)**

---

## 1. Viva Questions

### Q1. What is the concrete business problem that motivates the Adapter pattern in the PDF?

**Answer:** SellerRankingService needs to rank sellers for a given SKU. It originally consumed data from SDSellerSearchService (Snapdeal's internal service). After Snapdeal acquires "Exclusively," a second service -- ExclusivelySellerSearchService -- must also be integrated. The two services expose completely different APIs, return different data types (SDVendor vs ExMerchant), and use different units (e.g., price in rupees vs pricePaise in paise, rating 0-5 vs score100 0-100). The Adapter pattern lets SellerRankingService work with both through a single SellerSearch interface without modifying either legacy service.

---

### Q2. List all five naive alternatives discussed in the PDF and state which SOLID principles each one violates.

**Answer:**
1. **Change the legacy services** (make SDSellerSearchService and ExclusivelySellerSearchService conform to a common API) -- Violates OCP (modifying closed/stable code), SRP (services now carry conversion responsibility), and DIP (client still depends on concrete classes).
2. **Big if/else block inside SellerRankingService** (check type, call the right service, map inline) -- Violates SRP (ranking service now does mapping), OCP (every new source means editing the block), DIP (depends on concretes), and causes shotgun surgery.
3. **Global "God" Mapper class** (centralized switch/map that converts any source to canonical) -- Still a switchyard that violates OCP (add a case per new source) and is weak on SRP.
4. **Shared canonical domain model across teams** (force both teams to use the same Seller class directly) -- Crosses team boundaries, and if a service only partially implements the shared model it violates LSP. Coordination overhead is high.
5. **Static convenience/utility methods** (e.g., SellerUtils.toSeller(SDVendor)) -- Same switchyard problem as #3, no polymorphism, so OCP is still violated and DIP is not satisfied because the client must know which utility to call.

---

### Q3. What are the exact fields of SDVendor and how does each map to the canonical Seller class?

**Answer:**
| SDVendor field | Type     | Seller field   | Conversion              |
|----------------|----------|----------------|-------------------------|
| vendorId       | String   | id             | Direct copy             |
| shopName       | String   | name           | Direct copy             |
| listPrice      | double   | price          | Direct copy (already in major units) |
| discountPct    | double   | discountPct    | Direct copy (already 0-100) |
| starRating     | double   | rating         | Direct copy (already 0-5) |

No unit conversion is required for SDVendor because its units already match the canonical Seller model.

---

### Q4. What are the exact fields of ExMerchant and what unit conversions does ExSearchAdapter perform?

**Answer:**
| ExMerchant field | Type   | Seller field   | Conversion                          |
|------------------|--------|----------------|-------------------------------------|
| id               | String | id             | Direct copy                         |
| display          | String | name           | Direct copy                         |
| pricePaise       | long   | price (double) | pricePaise / 100.0 (paise to rupees)|
| off              | int    | discountPct    | Direct copy (already 0-100)         |
| score100         | int    | rating (0-5)   | score100 / 20.0 (0-100 scale to 0-5)|

The two critical conversions are: dividing pricePaise by 100.0 to get rupees, and dividing score100 by 20.0 to map a 0-100 score onto a 0-5 rating.

---

### Q5. Describe the SellerSearch target interface -- its method signatures and the canonical Seller class.

**Answer:** The SellerSearch interface declares two methods:
- `List<Seller> getSellersBySku(String sku)` -- returns all sellers for a given SKU.
- `Seller getSellerWithMaxDiscount(String sku)` -- returns the single seller offering the highest discount for that SKU.

The canonical `Seller` class has five fields:
- `String id`
- `String name`
- `double price` (in major currency units, e.g., rupees)
- `double discountPct` (percentage, 0-100)
- `double rating` (0-5 scale)

---

### Q6. How does ExclusivelySellerSearchService's API differ structurally from SDSellerSearchService's API?

**Answer:** ExclusivelySellerSearchService returns results wrapped in a `Page<T>` wrapper and uses pagination parameters. Its method signature is something like `Page<ExMerchant> merchantsFor(String articleCode, int page, int perPage)`. This means the ExSearchAdapter must handle pagination (potentially fetching multiple pages) and unwrap the `Page<T>` to extract the list of ExMerchant objects. In contrast, SDSellerSearchService returns a simple list directly with no pagination wrapper, making its adapter straightforward.

---

### Q7. Explain how SellerRankingService sorts the sellers and why this logic is independent of the adapter.

**Answer:** SellerRankingService sorts sellers using a multi-key comparator: first by discountPct descending (highest discount first), then by rating descending (highest rating first as a tiebreaker), then by price ascending (lowest price first as a second tiebreaker). This sorting logic operates entirely on the canonical Seller class, so it is completely independent of whether the data came from SDSellerSearchService or ExclusivelySellerSearchService. The adapter has already converted everything into uniform Seller objects before the ranking service sees them.

---

### Q8. What is the difference between an Object Adapter and a Class Adapter, and which does the PDF recommend for Java?

**Answer:** An Object Adapter uses composition -- it holds a reference to the adaptee as a field and delegates calls to it. A Class Adapter uses inheritance -- it extends the adaptee class and overrides or supplements behavior. The PDF recommends the Object Adapter (composition) for Java because Java does not support multiple class inheritance. Since the adapter already needs to implement the Target interface, it cannot also extend the adaptee class without hitting Java's single-inheritance limit. Composition is also more flexible: you can swap the adaptee instance at runtime, and it results in looser coupling.

---

### Q9. How does the Adapter pattern satisfy all five SOLID principles in this scenario?

**Answer:**
- **SRP:** Each adapter has a single responsibility -- translating one specific service's data into the canonical model. SellerRankingService only ranks; it does not map.
- **OCP:** Adding a new seller source (e.g., a third acquisition) requires writing a new adapter class. No existing code is modified.
- **LSP:** Every adapter implements SellerSearch and returns properly constructed Seller objects, so any adapter can substitute for any other without breaking SellerRankingService.
- **ISP:** SellerRankingService depends on the slim SellerSearch interface, not on the fat APIs of SDSellerSearchService or ExclusivelySellerSearchService.
- **DIP:** SellerRankingService depends on the SellerSearch abstraction (injected via constructor), not on any concrete service or adapter. High-level policy is decoupled from low-level data-fetching details.

---

### Q10. What practical concerns beyond field mapping does the PDF mention for production-quality adapters?

**Answer:** The PDF highlights several practical concerns:
1. **Unit conversions** -- e.g., paise to rupees, score100 to 0-5 rating; getting these wrong silently corrupts data.
2. **Null handling** -- the adaptee may return null fields or null results; the adapter should handle these gracefully rather than propagating NullPointerExceptions.
3. **Error translation** -- the adaptee may throw service-specific exceptions; the adapter should catch these and translate them into exceptions or error types that the target interface's callers expect.
4. **Caching** -- the adapter is a natural place to add caching since it sits between the client and the external service.
5. **Observability** -- logging, metrics, and tracing can be added in the adapter layer to monitor the health and latency of the underlying service without polluting the client or the adaptee.

---

## 2. MCQ Quiz

**Instructions:** Select the single best answer for each question. Click the answer toggle to check.

---

**Q1.** In the PDF scenario, what does SellerRankingService originally depend on before the acquisition?

A) ExclusivelySellerSearchService  
B) SellerSearch interface  
C) SDSellerSearchService  
D) A global Mapper class  

<details><summary>Answer</summary>

**C) SDSellerSearchService** -- Before the acquisition of Exclusively, SellerRankingService consumed seller data directly from Snapdeal's own SDSellerSearchService.

</details>

---

**Q2.** Which data class does SDSellerSearchService return?

A) Seller  
B) ExMerchant  
C) SDVendor  
D) SDMerchant  

<details><summary>Answer</summary>

**C) SDVendor** -- SDSellerSearchService returns SDVendor objects with fields vendorId, shopName, listPrice, discountPct, and starRating.

</details>

---

**Q3.** What is the type of `pricePaise` in ExMerchant?

A) double  
B) int  
C) long  
D) BigDecimal  

<details><summary>Answer</summary>

**C) long** -- pricePaise is stored as a long in ExMerchant, representing the price in paise (smallest currency unit).

</details>

---

**Q4.** To convert ExMerchant's `score100` to the canonical Seller's rating (0-5), you divide by:

A) 5.0  
B) 10.0  
C) 20.0  
D) 100.0  

<details><summary>Answer</summary>

**C) 20.0** -- score100 is on a 0-100 scale. Dividing by 20.0 maps it to 0-5 (e.g., 80/20.0 = 4.0).

</details>

---

**Q5.** To convert ExMerchant's `pricePaise` to the canonical Seller's price in rupees, you:

A) Multiply by 100.0  
B) Divide by 10.0  
C) Divide by 100.0  
D) Cast to double directly  

<details><summary>Answer</summary>

**C) Divide by 100.0** -- pricePaise is in paise; dividing by 100.0 converts to rupees (e.g., 49999L / 100.0 = 499.99).

</details>

---

**Q6.** ExclusivelySellerSearchService wraps its results in which generic type?

A) List\<T>  
B) Optional\<T>  
C) Page\<T>  
D) Collection\<T>  

<details><summary>Answer</summary>

**C) Page\<T>** -- The service returns Page\<ExMerchant>, and the adapter must unwrap this pagination wrapper to extract the list.

</details>

---

**Q7.** What is the method signature on ExclusivelySellerSearchService for fetching merchants?

A) getSellersBySku(String sku)  
B) merchantsFor(String articleCode, int page, int perPage)  
C) findMerchants(String sku)  
D) searchExclusively(String articleCode)  

<details><summary>Answer</summary>

**B) merchantsFor(String articleCode, int page, int perPage)** -- It takes an article code (not SKU), a page number, and a per-page count, returning Page\<ExMerchant>.

</details>

---

**Q8.** Naive Alternative #2 (big if/else inside SellerRankingService) causes which specific maintenance problem?

A) Deadlock  
B) Shotgun surgery  
C) Circular dependency  
D) Memory leak  

<details><summary>Answer</summary>

**B) Shotgun surgery** -- Every time a new seller source is added, you must find and modify the if/else block (and potentially multiple such blocks), scattering changes across the codebase.

</details>

---

**Q9.** Naive Alternative #4 (shared canonical domain model across teams) primarily risks violating:

A) OCP  
B) SRP  
C) LSP  
D) DIP  

<details><summary>Answer</summary>

**C) LSP** -- If a service only partially implements the shared model (e.g., leaves certain fields null or uses them differently), it violates the Liskov Substitution Principle because callers cannot safely treat all implementations interchangeably.

</details>

---

**Q10.** In the Adapter solution, SellerRankingService receives its SellerSearch dependency via:

A) A static factory method  
B) Service locator  
C) Constructor injection  
D) Field annotation  

<details><summary>Answer</summary>

**C) Constructor injection** -- SellerRankingService takes a SellerSearch instance through its constructor, satisfying DIP.

</details>

---

**Q11.** The canonical Seller class has how many fields?

A) 3  
B) 4  
C) 5  
D) 6  

<details><summary>Answer</summary>

**C) 5** -- id, name, price (major units), discountPct (0-100), and rating (0-5).

</details>

---

**Q12.** Which of the following is NOT a field in SDVendor?

A) vendorId  
B) shopName  
C) pricePaise  
D) starRating  

<details><summary>Answer</summary>

**C) pricePaise** -- pricePaise belongs to ExMerchant. SDVendor uses listPrice (already in major currency units).

</details>

---

**Q13.** The SDVendor field for the seller's name is called:

A) name  
B) display  
C) shopName  
D) sellerName  

<details><summary>Answer</summary>

**C) shopName** -- SDVendor uses shopName, while ExMerchant uses display, and the canonical Seller uses name.

</details>

---

**Q14.** The ExMerchant field for the seller's name is called:

A) name  
B) shopName  
C) merchantName  
D) display  

<details><summary>Answer</summary>

**D) display** -- ExMerchant stores the seller's display name in a field called display.

</details>

---

**Q15.** SellerRankingService sorts sellers by discount (desc), then rating (desc), then:

A) name (asc)  
B) id (asc)  
C) price (asc)  
D) price (desc)  

<details><summary>Answer</summary>

**C) price (asc)** -- The tertiary sort key is price in ascending order, so the cheapest option is preferred when discount and rating are tied.

</details>

---

**Q16.** Why does the PDF recommend Object Adapter over Class Adapter in Java?

A) Object Adapter is faster at runtime  
B) Java lacks multiple class inheritance  
C) Class Adapter cannot access private fields  
D) Object Adapter uses less memory  

<details><summary>Answer</summary>

**B) Java lacks multiple class inheritance** -- The adapter must implement the Target interface, so it cannot also extend the Adaptee class in Java. Composition (Object Adapter) solves this.

</details>

---

**Q17.** Naive Alternative #1 (change the legacy services to match a common API) violates OCP because:

A) It adds new classes  
B) It modifies stable, already-deployed code  
C) It uses too many interfaces  
D) It creates circular dependencies  

<details><summary>Answer</summary>

**B) It modifies stable, already-deployed code** -- OCP says classes should be open for extension, closed for modification. Rewriting a working service to match a new API modifies closed code.

</details>

---

**Q18.** Which adapter class wraps ExclusivelySellerSearchService?

A) SDSearchAdapter  
B) ExSearchAdapter  
C) ExclusivelyAdapter  
D) MerchantSearchAdapter  

<details><summary>Answer</summary>

**B) ExSearchAdapter** -- ExSearchAdapter implements SellerSearch and internally wraps ExclusivelySellerSearchService, converting ExMerchant objects to Seller.

</details>

---

**Q19.** The `off` field in ExMerchant represents:

A) A boolean indicating the seller is offline  
B) The discount percentage (0-100)  
C) The price offset in paise  
D) The number of items out of stock  

<details><summary>Answer</summary>

**B) The discount percentage (0-100)** -- off is an int representing the discount percentage, mapping directly to Seller's discountPct without unit conversion.

</details>

---

**Q20.** Which practical concern does the PDF mention as a natural fit for the adapter layer?

A) Database sharding  
B) Caching  
C) Load balancing  
D) Thread pooling  

<details><summary>Answer</summary>

**B) Caching** -- The PDF mentions that the adapter is a natural place to introduce caching since it mediates between the client and the external service.

</details>

---

## 3. Self-Scoring Table

| Section         | Max Score | Your Score |
|-----------------|-----------|------------|
| Viva (10 x 2)  | 20        |            |
| MCQ  (20 x 1)  | 20        |            |
| Coding (3 x 10)| 30        |            |
| **Total**       | **70**    |            |

**Grading Guide:**
- 60-70: Excellent -- you can confidently implement and explain the Adapter pattern
- 45-59: Good -- review the unit conversion formulas and SOLID mapping
- 30-44: Needs work -- re-read the PDF focusing on the five naive alternatives and adapter wiring
- Below 30: Start over -- study the PDF carefully, then reattempt

---

## 4. Coding Problems

### Problem 1: Implement the Core Adapter Classes

Write the following in Java:
1. The `Seller` canonical class with fields: `id` (String), `name` (String), `price` (double), `discountPct` (double), `rating` (double).
2. The `SellerSearch` interface with methods `getSellersBySku(String sku)` and `getSellerWithMaxDiscount(String sku)`.
3. `SDSearchAdapter` that implements `SellerSearch`, wraps `SDSellerSearchService`, and maps `SDVendor` to `Seller`.
4. `ExSearchAdapter` that implements `SellerSearch`, wraps `ExclusivelySellerSearchService`, and maps `ExMerchant` to `Seller` with the correct unit conversions (`pricePaise / 100.0`, `score100 / 20.0`). It must handle the `Page<T>` wrapper and the `merchantsFor(articleCode, page, perPage)` API.

Assume the following are given:

```java
// Given - do NOT modify
class SDVendor {
    String vendorId, shopName;
    double listPrice, discountPct, starRating;
}

class SDSellerSearchService {
    List<SDVendor> searchByProductId(String productId) { /*...*/ }
}

class ExMerchant {
    String id, display;
    long pricePaise;
    int off;      // discount 0-100
    int score100; // rating 0-100
}

class Page<T> {
    List<T> content;
    int totalPages;
}

class ExclusivelySellerSearchService {
    Page<ExMerchant> merchantsFor(String articleCode, int page, int perPage) { /*...*/ }
}
```

<details><summary>Solution</summary>

```java
// --- Canonical model ---
class Seller {
    private String id;
    private String name;
    private double price;
    private double discountPct;
    private double rating;

    public Seller(String id, String name, double price, double discountPct, double rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.discountPct = discountPct;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getDiscountPct() { return discountPct; }
    public double getRating() { return rating; }
}

// --- Target interface ---
interface SellerSearch {
    List<Seller> getSellersBySku(String sku);
    Seller getSellerWithMaxDiscount(String sku);
}

// --- SDSearchAdapter (Object Adapter) ---
class SDSearchAdapter implements SellerSearch {

    private final SDSellerSearchService service;

    public SDSearchAdapter(SDSellerSearchService service) {
        this.service = service;
    }

    @Override
    public List<Seller> getSellersBySku(String sku) {
        List<SDVendor> vendors = service.searchByProductId(sku);
        if (vendors == null) return Collections.emptyList();

        return vendors.stream()
                .map(this::toSeller)
                .collect(Collectors.toList());
    }

    @Override
    public Seller getSellerWithMaxDiscount(String sku) {
        return getSellersBySku(sku).stream()
                .max(Comparator.comparingDouble(Seller::getDiscountPct))
                .orElse(null);
    }

    private Seller toSeller(SDVendor v) {
        return new Seller(
                v.vendorId,
                v.shopName,
                v.listPrice,       // already in major units
                v.discountPct,     // already 0-100
                v.starRating       // already 0-5
        );
    }
}

// --- ExSearchAdapter (Object Adapter) ---
class ExSearchAdapter implements SellerSearch {

    private static final int PAGE_SIZE = 50;
    private final ExclusivelySellerSearchService service;

    public ExSearchAdapter(ExclusivelySellerSearchService service) {
        this.service = service;
    }

    @Override
    public List<Seller> getSellersBySku(String sku) {
        List<Seller> results = new ArrayList<>();
        int currentPage = 0;
        Page<ExMerchant> page;

        do {
            page = service.merchantsFor(sku, currentPage, PAGE_SIZE);
            if (page == null || page.content == null) break;

            for (ExMerchant m : page.content) {
                results.add(toSeller(m));
            }
            currentPage++;
        } while (currentPage < page.totalPages);

        return results;
    }

    @Override
    public Seller getSellerWithMaxDiscount(String sku) {
        return getSellersBySku(sku).stream()
                .max(Comparator.comparingDouble(Seller::getDiscountPct))
                .orElse(null);
    }

    private Seller toSeller(ExMerchant m) {
        return new Seller(
                m.id,
                m.display,
                m.pricePaise / 100.0,   // paise -> rupees
                (double) m.off,          // already 0-100
                m.score100 / 20.0        // 0-100 -> 0-5
        );
    }
}
```

Key points:
- Both adapters use composition (Object Adapter), holding the adaptee as a private final field.
- SDSearchAdapter requires no unit conversion; ExSearchAdapter performs `pricePaise / 100.0` and `score100 / 20.0`.
- ExSearchAdapter handles the `Page<T>` wrapper by looping through pages until `currentPage >= totalPages`.
- Both handle null defensively.

</details>

---

### Problem 2: Implement SellerRankingService with the PDF's Sort Order

Write `SellerRankingService` that:
- Takes a `SellerSearch` via constructor injection (DIP).
- Has a method `List<Seller> rankSellers(String sku)` that fetches sellers and sorts them by: discount descending, then rating descending, then price ascending.
- Has a method `Seller topSeller(String sku)` that returns the #1 ranked seller.

Write a `main` method that wires SDSearchAdapter into SellerRankingService and then wires ExSearchAdapter into a second instance, demonstrating that the ranking service is agnostic to the data source.

<details><summary>Solution</summary>

```java
class SellerRankingService {

    private final SellerSearch sellerSearch;

    // Constructor injection -- depends on abstraction, not concrete adapter
    public SellerRankingService(SellerSearch sellerSearch) {
        this.sellerSearch = sellerSearch;
    }

    public List<Seller> rankSellers(String sku) {
        List<Seller> sellers = new ArrayList<>(sellerSearch.getSellersBySku(sku));

        sellers.sort(
            Comparator.comparingDouble(Seller::getDiscountPct).reversed()  // discount DESC
                .thenComparing(
                    Comparator.comparingDouble(Seller::getRating).reversed() // rating DESC
                )
                .thenComparingDouble(Seller::getPrice)                      // price ASC
        );

        return sellers;
    }

    public Seller topSeller(String sku) {
        List<Seller> ranked = rankSellers(sku);
        return ranked.isEmpty() ? null : ranked.get(0);
    }
}

// --- Wiring demonstration ---
class Main {
    public static void main(String[] args) {

        // Wire with Snapdeal adapter
        SDSellerSearchService sdService = new SDSellerSearchService();
        SellerSearch sdAdapter = new SDSearchAdapter(sdService);
        SellerRankingService sdRanking = new SellerRankingService(sdAdapter);

        List<Seller> sdResults = sdRanking.rankSellers("SKU-12345");
        System.out.println("Top SD seller: " + sdRanking.topSeller("SKU-12345").getName());

        // Wire with Exclusively adapter -- same SellerRankingService class, different source
        ExclusivelySellerSearchService exService = new ExclusivelySellerSearchService();
        SellerSearch exAdapter = new ExSearchAdapter(exService);
        SellerRankingService exRanking = new SellerRankingService(exAdapter);

        List<Seller> exResults = exRanking.rankSellers("ART-67890");
        System.out.println("Top Ex seller: " + exRanking.topSeller("ART-67890").getName());
    }
}
```

Key points:
- SellerRankingService has zero knowledge of SDVendor, ExMerchant, or any concrete service. It only knows SellerSearch and Seller.
- The three-level comparator matches the PDF: discount DESC, rating DESC, price ASC.
- Two separate instances show that the exact same ranking logic works with completely different data sources -- that is the Adapter pattern's payoff.

</details>

---

### Problem 3: Add a Third Adapter and Demonstrate OCP

A third company "ShopEasy" is acquired. Its service:

```java
class SEProduct {
    int productCode;
    String label;
    float mrp;            // price in major units
    float discountFraction; // 0.0 to 1.0 (NOT 0-100)
    int reviewStars;       // 1 to 10 (NOT 0-5)
}

class ShopEasyService {
    SEProduct[] findProducts(String category) { /*...*/ }
}
```

Tasks:
1. Write `ShopEasyAdapter` implementing `SellerSearch` with the correct unit conversions.
2. Explain (in comments) which existing classes you modified and which you did not. Verify OCP holds.
3. Wire it into SellerRankingService.

<details><summary>Solution</summary>

```java
/*
 * OCP verification:
 *   MODIFIED:  Nothing. Zero changes to Seller, SellerSearch, SDSearchAdapter,
 *              ExSearchAdapter, or SellerRankingService.
 *   ADDED:     ShopEasyAdapter (new class).
 *
 * This is the Open/Closed Principle in action -- the system is extended by
 * adding a new adapter, not by modifying any existing code.
 */

class ShopEasyAdapter implements SellerSearch {

    private final ShopEasyService service;

    public ShopEasyAdapter(ShopEasyService service) {
        this.service = service;
    }

    @Override
    public List<Seller> getSellersBySku(String sku) {
        SEProduct[] products = service.findProducts(sku);
        if (products == null) return Collections.emptyList();

        return Arrays.stream(products)
                .map(this::toSeller)
                .collect(Collectors.toList());
    }

    @Override
    public Seller getSellerWithMaxDiscount(String sku) {
        return getSellersBySku(sku).stream()
                .max(Comparator.comparingDouble(Seller::getDiscountPct))
                .orElse(null);
    }

    private Seller toSeller(SEProduct p) {
        return new Seller(
                String.valueOf(p.productCode),   // int -> String for id
                p.label,                          // direct map to name
                (double) p.mrp,                   // float -> double, already major units
                p.discountFraction * 100.0,       // 0.0-1.0 -> 0-100
                p.reviewStars / 2.0               // 1-10 -> 0.5-5.0
        );
    }
}

// --- Wiring ---
class Main {
    public static void main(String[] args) {

        // Third source -- no changes to any existing class
        ShopEasyService seService = new ShopEasyService();
        SellerSearch seAdapter = new ShopEasyAdapter(seService);
        SellerRankingService seRanking = new SellerRankingService(seAdapter);

        Seller top = seRanking.topSeller("CAT-99999");
        System.out.println("Top ShopEasy seller: " + top.getName());
    }
}
```

Key conversions:
- `discountFraction * 100.0`: converts 0.0-1.0 fraction to 0-100 percentage matching canonical Seller.
- `reviewStars / 2.0`: converts 1-10 scale to 0.5-5.0 scale matching canonical Seller's 0-5 rating.
- `String.valueOf(p.productCode)`: converts int ID to String to match canonical Seller.

OCP is proven: we added one new file (ShopEasyAdapter) and touched nothing else.

</details>
