# Problem 5: File Exporter System

**Topic:** Design from scratch following SOLID
**Difficulty:** Medium

---

## Scenario

A data analytics platform needs to export reports in multiple formats: **CSV**, **JSON**, and **PDF**. Each format has different serialization logic. The system should be easily extensible to new formats (XML, Excel) without modifying existing code.

## Requirements

Design and implement from scratch:

1. A `ReportData` class (immutable) holding:
   - `String title`
   - `List<String> headers`
   - `List<List<String>> rows`

2. An `Exporter` interface with:
   - `String export(ReportData data)` -- returns the formatted string
   - `String format()` -- returns "CSV", "JSON", etc.

3. Concrete exporters:
   - `CsvExporter` -- comma-separated values with headers as first row
   - `JsonExporter` -- JSON array of objects (each row is an object with header keys)
   - `PdfExporter` -- simulate with `"[PDF] title\nheaders\nrow1\nrow2..."` (no real PDF needed)

4. An `ExportService` that:
   - Takes a `List<Exporter>` via constructor
   - `String exportAs(ReportData data, String format)` -- finds the right exporter and exports
   - Throws if format not supported

5. `Main` demonstrating export of sample data in all 3 formats

## What to create

```
ReportData.java
Exporter.java
CsvExporter.java
JsonExporter.java
PdfExporter.java
ExportService.java
Main.java
```

## What I'll check

- `ReportData` is immutable (defensive copy of lists)
- `Exporter` interface follows ISP (minimal)
- Each exporter has one job (SRP)
- `ExportService` depends on `Exporter` interface (DIP)
- Adding `XmlExporter` requires zero changes to `ExportService` (OCP)
