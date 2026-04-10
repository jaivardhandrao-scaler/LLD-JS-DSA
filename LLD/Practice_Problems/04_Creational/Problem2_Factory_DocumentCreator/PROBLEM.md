# Problem 7: Document Creator Factory

**Topic:** Simple Factory + Factory Method
**Difficulty:** Medium

---

## Scenario

A content management system creates different types of documents: **Invoice**, **Resume**, **Letter**. Each document type has different structure and rendering.

## Part A: Simple Factory

1. Create `Document` interface with:
   - `String type()`
   - `String render()`

2. Create concrete documents:
   - `Invoice` -- renders `"INVOICE\n---\nItems: ... Total: ..."`
   - `Resume` -- renders `"RESUME\n---\nName: ... Skills: ..."`
   - `Letter` -- renders `"LETTER\n---\nDear ..., ..."`
   (Each takes relevant fields in constructor)

3. Create `DocumentFactory` (Simple Factory):
   - `static Document create(String type, Map<String, String> fields)`
   - Switches on type to create the right document

## Part B: Factory Method

4. Create `DocumentGenerator` abstract class:
   - `final String generateAndSave(Map<String, String> fields)` -- calls `createDocument()`, renders it, adds a "Saved!" footer
   - `abstract Document createDocument(Map<String, String> fields)`

5. Create `InvoiceGenerator` and `ResumeGenerator` that override `createDocument()`

6. `Main` showing both approaches

## What to create

```
Document.java
Invoice.java
Resume.java
Letter.java
DocumentFactory.java
DocumentGenerator.java
InvoiceGenerator.java
ResumeGenerator.java
Main.java
```

## What I'll check

- Simple Factory: static method, switch-based, creates from type string
- Factory Method: abstract class with `final` template + abstract creation
- Client code doesn't use `new Invoice()` directly (uses factory)
- Adding a new doc type to Simple Factory = edit the switch (note the OCP limitation)
- Adding a new generator to Factory Method = new subclass, no edit to base
