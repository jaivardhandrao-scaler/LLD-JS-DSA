# Problem 12: Compression Strategy

**Topic:** Strategy Pattern with runtime swap
**Difficulty:** Medium

---

## Scenario

A file archiver tool supports multiple compression algorithms. The user picks the algorithm at startup, but can **switch at runtime** (e.g., start with fast compression, switch to high-ratio for large files).

## Requirements

1. Create `CompressionStrategy` interface:
   - `String compress(String data)` -- returns "compressed" data (simulate it)
   - `String name()`

2. Create concrete strategies:
   - `NoCompression` -- returns data as-is, name "NONE"
   - `RunLengthEncoding` -- simulates RLE: for each group of repeated chars, output `char + count`. E.g., "aaabbc" -> "a3b2c1". Name "RLE"
   - `ZipCompression` -- simulates by returning `"ZIP[" + data.length() + " bytes]"`. Name "ZIP"

3. Create `FileArchiver` (context):
   - Holds a `CompressionStrategy` (set via constructor)
   - `void setStrategy(CompressionStrategy s)` -- hot-swap at runtime
   - `String archiveFile(String filename, String content)` -- compresses content, returns `"Archived filename with STRATEGY_NAME: compressed_result"`

4. `Main` demonstrating:
   - Archive a small file with NoCompression
   - Archive a medium file with RLE (use `"aaabbbccccdd"` as content)
   - Hot-swap to ZIP at runtime, archive a large file
   - Print all results

## What to create

```
CompressionStrategy.java
NoCompression.java
RunLengthEncoding.java
ZipCompression.java
FileArchiver.java
Main.java
```

## What I'll check

- Strategy interface is minimal (one method for compression)
- Each strategy is its own class with no knowledge of others
- `FileArchiver` depends on `CompressionStrategy` interface (DIP)
- Runtime swap works via `setStrategy()`
- Adding a new algorithm (e.g., `HuffmanCompression`) requires ZERO changes to `FileArchiver` (OCP)
- RLE actually works correctly for the given input
