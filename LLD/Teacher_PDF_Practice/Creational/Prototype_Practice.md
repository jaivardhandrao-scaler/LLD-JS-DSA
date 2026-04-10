# Prototype -- Teacher PDF Practice

> All questions are based on the **DesignPattern_Prototype.pdf** examples (Vector graphics editor, ShapeRegistry, Rectangle/Ellipse/TextBox, StrokeStyle/FillStyle, Tool/Canvas).

---

## Viva Questions

**Q1: What is the problem the PDF sets up before introducing Prototype? Why are shape constructors painful?**

In the vector graphics editor, the palette offers tools like Rectangle, Ellipse, TextBox, and Arrow. Each shape constructor does heavy, repetitive configuration work (setting up stroke styles, fill styles, dimensions). When a user clicks a tool, calling `new Rectangle(...)` every time means:
1. The client must know the exact class name and all constructor parameters.
2. The set of available shapes can change at runtime (e.g., plugins adding new shapes), so hardcoding `new ClassName(...)` everywhere violates OCP.
3. Creation should be "by example" (clone a pre-configured prototype) rather than "by class name."

---

**Q2: What three methods does the `Shape` interface define in the PDF?**

1. `Shape copy()` -- returns a deep/shallow clone of the shape (the Prototype method).
2. `void draw(Graphics2D g)` -- renders the shape onto a graphics context.
3. `void setPosition(int x, int y)` -- places the shape at a canvas coordinate.

The key insight: `copy()` is what makes `Shape` a Prototype. Every concrete shape must know how to duplicate itself.

---

**Q3: Explain the `ShapeRegistry` from the PDF. What data structure does it use and what are its three operations?**

`ShapeRegistry` is the **Prototype Registry**. It uses a `Map<String, Shape>` internally (the `store`). Its operations:
1. `register(String key, Shape proto)` -- puts a prototype into the map under the given key (e.g., `"rect"`, `"ellipse"`, `"text"`).
2. `unregister(String key)` -- removes a prototype from the registry.
3. `Shape create(String key)` -- looks up the prototype by key, calls `p.copy()`, and returns the fresh clone.

The registry decouples client code from concrete shape classes entirely. Clients only know string keys.

---

**Q4: How does the `Tool` class work in the PDF? Trace what happens when `onClick(x, y)` is called.**

`Tool` holds a reference to a `ShapeRegistry` and a `String key` (e.g., `"rect"`). When the user clicks:
1. `onClick(x, y)` calls `registry.create(key)` -- this looks up the `"rect"` prototype and calls `copy()` on it.
2. A fresh `Rectangle` clone is returned with its own independent `StrokeStyle` and `FillStyle`.
3. `setPosition(x, y)` is called on the clone to place it at the click coordinates.
4. The clone is added to the canvas for drawing.

No `new Rectangle(...)` appears in `Tool` -- it only knows the registry and a key string.

---

**Q5: The PDF defines `StrokeStyle` and `FillStyle` as value objects. What role do they play in `copy()`?**

`StrokeStyle` and `FillStyle` encapsulate visual properties (line width, color, dash pattern, fill color, opacity, etc.). Each has its own `copy()` method. When `Rectangle.copy()` is called, it creates a new `Rectangle` and passes in `strokeStyle.copy()` and `fillStyle.copy()` so the clone gets independent style objects. This is **deep copy** -- modifying the clone's stroke does not affect the prototype's stroke.

`TextBox` only has a `FillStyle` (no stroke), so its `copy()` only deep-copies the fill.

---

**Q6: Walk through the Demo from the PDF. What prototypes are registered and what happens when tools are clicked?**

The Demo:
1. Creates configured prototypes: a `Rectangle` with specific width/height/stroke/fill, an `Ellipse` with rx/ry/stroke/fill, and a `TextBox` with default text and fill.
2. Registers them: `registry.register("rect", rectProto)`, `registry.register("ellipse", ellipseProto)`, `registry.register("text", textProto)`.
3. Creates `Tool` objects: `new Tool(registry, "rect")`, `new Tool(registry, "ellipse")`, etc.
4. Each `tool.onClick(x, y)` produces a fresh clone of the registered prototype, positioned at (x, y). Multiple clicks produce multiple independent shapes, all cloned from the same prototype.

---

**Q7: The PDF has a section on Shallow vs Deep Copy. When can styles be shared, and when must they be deep-copied?**

- **Immutable styles can be shared (shallow copy):** If `StrokeStyle` and `FillStyle` are immutable (all fields are `final`, no setters), then multiple clones can safely point to the same style object. No clone can modify it, so sharing is safe.
- **Mutable styles must be deep-copied:** If styles have setters (e.g., `setColor()`, `setWidth()`), then clones must get their own copies. Otherwise, changing the stroke color on one clone would change it on every clone that shares that style.

The decision is per-field: you analyze each field in the prototype and decide share vs copy based on mutability.

---

**Q8: The PDF compares Prototype to Factory patterns. What are the key differences?**

| Aspect | Prototype | Factory Method / Abstract Factory |
|--------|-----------|-----------------------------------|
| **Creation mechanism** | Clone an existing instance | Call a method that invokes a constructor |
| **Class knowledge** | Client doesn't need to know concrete class -- just calls `copy()` | Factory subclass knows the concrete class |
| **Runtime flexibility** | New "kinds" added by registering a differently-configured prototype at runtime -- no new classes needed | New kinds require new factory subclasses or new branches in a switch |
| **When to prefer** | Objects are expensive to construct from scratch but cheap to clone; the set of products varies at runtime | The set of products is fixed at compile time; construction logic is complex but varies by family |

The PDF emphasizes that Prototype shines when "creation by example" is more natural than "creation by class name."

---

## MCQ Quiz (15 Questions)

**Q1.** In the PDF's vector editor, the `Shape` interface declares which methods?

a) `clone()`, `render()`, `moveTo()`
b) `copy()`, `draw(Graphics2D g)`, `setPosition(int x, int y)`
c) `duplicate()`, `paint()`, `place()`
d) `copy()`, `draw()`, `translate()`

<details><summary>Answer</summary>b) The PDF defines exactly three methods: `copy()`, `draw(Graphics2D g)`, and `setPosition(int x, int y)`.</details>

---

**Q2.** The `Rectangle` constructor in the PDF takes which parameters?

a) `(int x, int y, int w, int h)`
b) `(int w, int h, StrokeStyle, FillStyle)`
c) `(double w, double h, Color c)`
d) `(StrokeStyle, FillStyle)`

<details><summary>Answer</summary>b) `Rectangle(int w, int h, StrokeStyle, FillStyle)` -- width, height, and both style value objects.</details>

---

**Q3.** What does `ShapeRegistry.create(String key)` return?

a) The original prototype stored in the map
b) `null` if the key is not found
c) `p.copy()` -- a clone of the prototype registered under that key
d) A new instance created via reflection

<details><summary>Answer</summary>c) It looks up the prototype `p` by key and returns `p.copy()` -- a fresh clone, not the original.</details>

---

**Q4.** In the PDF, `TextBox` differs from `Rectangle` and `Ellipse` because:

a) It has no `copy()` method
b) It has only a `FillStyle`, no `StrokeStyle`
c) It cannot be registered in the registry
d) It uses shallow copy exclusively

<details><summary>Answer</summary>b) `TextBox(String text, FillStyle)` has no `StrokeStyle`. Its `copy()` only deep-copies the `FillStyle`.</details>

---

**Q5.** The `ShapeRegistry` stores prototypes in:

a) A `List<Shape>`
b) A `Map<String, Shape>`
c) A `Set<Shape>`
d) An array of `Shape`

<details><summary>Answer</summary>b) A `Map<String, Shape>` called `store`, keyed by string identifiers like `"rect"`, `"ellipse"`, `"text"`.</details>

---

**Q6.** In the PDF's Demo, which three keys are registered in the `ShapeRegistry`?

a) `"rectangle"`, `"circle"`, `"textbox"`
b) `"rect"`, `"ellipse"`, `"text"`
c) `"r"`, `"e"`, `"t"`
d) `"shape1"`, `"shape2"`, `"shape3"`

<details><summary>Answer</summary>b) `"rect"`, `"ellipse"`, and `"text"` are the keys used in the Demo.</details>

---

**Q7.** The `Tool` class holds:

a) A `Shape` and a `Canvas`
b) A `ShapeRegistry` and a `String key`
c) A `Graphics2D` context only
d) A `List<Shape>` of all created shapes

<details><summary>Answer</summary>b) `Tool` holds a reference to the `ShapeRegistry` and a `String key` identifying which prototype to clone.</details>

---

**Q8.** When `Tool.onClick(x, y)` is called, the order of operations is:

a) `new Shape()` then `draw()`
b) `registry.create(key)` then `setPosition(x, y)`
c) `setPosition(x, y)` then `copy()`
d) `draw()` then `setPosition(x, y)`

<details><summary>Answer</summary>b) First `registry.create(key)` clones the prototype, then `setPosition(x, y)` places the clone at the click coordinates.</details>

---

**Q9.** If `StrokeStyle` is immutable, then `Rectangle.copy()`:

a) Must still deep-copy the `StrokeStyle`
b) Can share the same `StrokeStyle` reference (shallow copy for that field)
c) Should set `StrokeStyle` to `null`
d) Should create a new `StrokeStyle` with default values

<details><summary>Answer</summary>b) Immutable objects are safe to share. Multiple clones can reference the same `StrokeStyle` because no clone can modify it.</details>

---

**Q10.** If `FillStyle` is mutable and you only do a shallow copy, what bug occurs?

a) The clone throws `NullPointerException`
b) Changing the fill color on the clone also changes it on the prototype and all other clones sharing that reference
c) The prototype is garbage collected
d) No bug -- shallow copy is always safe

<details><summary>Answer</summary>b) All shapes sharing the same mutable `FillStyle` reference will see each other's modifications. This is the core shallow-copy pitfall the PDF warns about.</details>

---

**Q11.** The `Ellipse` constructor in the PDF takes:

a) `(int cx, int cy, int r)`
b) `(int rx, int ry, StrokeStyle, FillStyle)`
c) `(double radius, Color c)`
d) `(int rx, int ry)`

<details><summary>Answer</summary>b) `Ellipse(int rx, int ry, StrokeStyle, FillStyle)` -- horizontal radius, vertical radius, and both style objects.</details>

---

**Q12.** Which `ShapeRegistry` method removes a prototype from the registry?

a) `delete(key)`
b) `remove(key)`
c) `unregister(key)`
d) `clear(key)`

<details><summary>Answer</summary>c) `unregister(String key)` removes the prototype associated with that key from the map.</details>

---

**Q13.** According to the PDF, Prototype is preferred over Factory when:

a) You have a fixed, compile-time set of products
b) The set of shapes can change at runtime and creation should be "by example"
c) You need complex multi-step construction
d) Only one instance should exist

<details><summary>Answer</summary>b) The PDF motivates Prototype by saying the shape palette can change at runtime, and creation should be by cloning a configured example rather than by class name.</details>

---

**Q14.** In the PDF, both `StrokeStyle` and `FillStyle` have:

a) A `draw()` method
b) Their own `copy()` methods
c) A `setPosition()` method
d) A `register()` method

<details><summary>Answer</summary>b) Both are value objects with their own `copy()` methods, enabling deep copy when needed by the shapes that contain them.</details>

---

**Q15.** Adding a new shape type (e.g., `Arrow`) to the PDF's system requires:

a) Modifying `ShapeRegistry`, `Tool`, and the Demo
b) Creating an `Arrow` class implementing `Shape`, then calling `registry.register("arrow", arrowProto)` -- no changes to existing classes
c) Modifying the `Shape` interface to add arrow-specific methods
d) Creating a new `ArrowFactory` subclass

<details><summary>Answer</summary>b) You create the `Arrow` class with `copy()`, `draw()`, and `setPosition()`, then register a prototype. `ShapeRegistry`, `Tool`, and all existing shapes remain untouched. This is OCP.</details>

---

## Self-Scoring

| Score | Level |
|-------|-------|
| 14-15 | Exam ready on Prototype |
| 11-13 | Good, review shallow vs deep copy and the registry |
| 8-10 | Re-read the PDF sections on ShapeRegistry and Tool |
| Below 8 | Go back to the PDF |

---

## Coding Problems

### Problem 1: Implement the Shape Prototype System from Memory

Write the complete system as described in the PDF:
- `Shape` interface with `copy()`, `draw(Graphics2D g)`, `setPosition(int x, int y)`
- `StrokeStyle` and `FillStyle` value objects with `copy()` methods
- `Rectangle(int w, int h, StrokeStyle, FillStyle)` with `copy()` that deep-copies styles
- `Ellipse(int rx, int ry, StrokeStyle, FillStyle)` with `copy()`
- `TextBox(String text, FillStyle)` with `copy()`
- `ShapeRegistry` with `register()`, `unregister()`, `create()`

Then write a Demo that registers three prototypes, creates them via the registry, and proves clones are independent.

<details><summary>Solution</summary>

```java
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

// --- Value Objects ---

class StrokeStyle {
    private String color;
    private int width;

    public StrokeStyle(String color, int width) {
        this.color = color;
        this.width = width;
    }

    public StrokeStyle copy() {
        return new StrokeStyle(color, width);
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getWidth() { return width; }

    @Override
    public String toString() {
        return "StrokeStyle{color='" + color + "', width=" + width + "}";
    }
}

class FillStyle {
    private String color;
    private double opacity;

    public FillStyle(String color, double opacity) {
        this.color = color;
        this.opacity = opacity;
    }

    public FillStyle copy() {
        return new FillStyle(color, opacity);
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public double getOpacity() { return opacity; }

    @Override
    public String toString() {
        return "FillStyle{color='" + color + "', opacity=" + opacity + "}";
    }
}

// --- Shape Interface ---

interface Shape {
    Shape copy();
    void draw(Graphics2D g);
    void setPosition(int x, int y);
}

// --- Concrete Shapes ---

class Rectangle implements Shape {
    private int w, h, x, y;
    private StrokeStyle stroke;
    private FillStyle fill;

    public Rectangle(int w, int h, StrokeStyle stroke, FillStyle fill) {
        this.w = w;
        this.h = h;
        this.stroke = stroke;
        this.fill = fill;
    }

    @Override
    public Shape copy() {
        return new Rectangle(w, h, stroke.copy(), fill.copy());
    }

    @Override
    public void draw(Graphics2D g) {
        System.out.println("Drawing Rectangle at (" + x + "," + y + ") "
            + w + "x" + h + " " + stroke + " " + fill);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public StrokeStyle getStroke() { return stroke; }
    public FillStyle getFill() { return fill; }
}

class Ellipse implements Shape {
    private int rx, ry, x, y;
    private StrokeStyle stroke;
    private FillStyle fill;

    public Ellipse(int rx, int ry, StrokeStyle stroke, FillStyle fill) {
        this.rx = rx;
        this.ry = ry;
        this.stroke = stroke;
        this.fill = fill;
    }

    @Override
    public Shape copy() {
        return new Ellipse(rx, ry, stroke.copy(), fill.copy());
    }

    @Override
    public void draw(Graphics2D g) {
        System.out.println("Drawing Ellipse at (" + x + "," + y + ") "
            + "rx=" + rx + " ry=" + ry + " " + stroke + " " + fill);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class TextBox implements Shape {
    private String text;
    private int x, y;
    private FillStyle fill;

    public TextBox(String text, FillStyle fill) {
        this.text = text;
        this.fill = fill;
    }

    @Override
    public Shape copy() {
        return new TextBox(text, fill.copy());
    }

    @Override
    public void draw(Graphics2D g) {
        System.out.println("Drawing TextBox at (" + x + "," + y + ") "
            + "text='" + text + "' " + fill);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public FillStyle getFill() { return fill; }
}

// --- Registry ---

class ShapeRegistry {
    private final Map<String, Shape> store = new HashMap<>();

    public void register(String key, Shape proto) {
        store.put(key, proto);
    }

    public void unregister(String key) {
        store.remove(key);
    }

    public Shape create(String key) {
        Shape p = store.get(key);
        if (p == null) throw new IllegalArgumentException("No prototype: " + key);
        return p.copy();
    }
}

// --- Demo ---

class Demo {
    public static void main(String[] args) {
        ShapeRegistry registry = new ShapeRegistry();

        // Register prototypes
        registry.register("rect",
            new Rectangle(100, 50,
                new StrokeStyle("black", 2),
                new FillStyle("blue", 1.0)));

        registry.register("ellipse",
            new Ellipse(40, 25,
                new StrokeStyle("red", 1),
                new FillStyle("yellow", 0.8)));

        registry.register("text",
            new TextBox("Hello",
                new FillStyle("white", 1.0)));

        // Clone via registry
        Shape r1 = registry.create("rect");
        Shape r2 = registry.create("rect");
        r1.setPosition(10, 20);
        r2.setPosition(200, 300);

        // Prove independence: modify clone's style
        ((Rectangle) r1).getStroke().setColor("green");

        r1.draw(null);  // stroke is green
        r2.draw(null);  // stroke is still black -- independent clone

        Shape t1 = registry.create("text");
        t1.setPosition(50, 50);
        t1.draw(null);
    }
}
```

Output:
```
Drawing Rectangle at (10,20) 100x50 StrokeStyle{color='green', width=2} FillStyle{color='blue', opacity=1.0}
Drawing Rectangle at (200,300) 100x50 StrokeStyle{color='black', width=2} FillStyle{color='blue', opacity=1.0}
Drawing TextBox at (50,50) text='Hello' FillStyle{color='white', opacity=1.0}
```

Key: `r1` and `r2` have independent `StrokeStyle` objects because `copy()` deep-copies them.
</details>

---

### Problem 2: Implement the Tool Class and Simulate Canvas Clicks

Using the `ShapeRegistry` from Problem 1, implement the `Tool` class exactly as described in the PDF. Then simulate a user selecting the rectangle tool and clicking three different positions on the canvas. Print each shape to prove they are separate clones at different positions.

<details><summary>Solution</summary>

```java
import java.util.ArrayList;
import java.util.List;

class Tool {
    private final ShapeRegistry registry;
    private final String key;

    public Tool(ShapeRegistry registry, String key) {
        this.registry = registry;
        this.key = key;
    }

    public Shape onClick(int x, int y) {
        Shape clone = registry.create(key);
        clone.setPosition(x, y);
        return clone;
    }
}

class Canvas {
    private final List<Shape> shapes = new ArrayList<>();

    public void add(Shape s) {
        shapes.add(s);
    }

    public void drawAll() {
        for (Shape s : shapes) {
            s.draw(null);
        }
    }
}

class ToolDemo {
    public static void main(String[] args) {
        // Setup registry with prototypes
        ShapeRegistry registry = new ShapeRegistry();
        registry.register("rect",
            new Rectangle(80, 40,
                new StrokeStyle("black", 2),
                new FillStyle("blue", 1.0)));
        registry.register("ellipse",
            new Ellipse(30, 20,
                new StrokeStyle("red", 1),
                new FillStyle("yellow", 0.8)));
        registry.register("text",
            new TextBox("Label",
                new FillStyle("white", 1.0)));

        // Create tools
        Tool rectTool = new Tool(registry, "rect");
        Tool ellipseTool = new Tool(registry, "ellipse");
        Tool textTool = new Tool(registry, "text");

        // Simulate clicks on canvas
        Canvas canvas = new Canvas();
        canvas.add(rectTool.onClick(10, 10));
        canvas.add(rectTool.onClick(150, 80));
        canvas.add(rectTool.onClick(300, 200));
        canvas.add(ellipseTool.onClick(50, 50));
        canvas.add(textTool.onClick(100, 100));

        // Draw all -- each shape is an independent clone at its own position
        canvas.drawAll();

        // Prove independence
        System.out.println("\n--- Independence check ---");
        Shape a = rectTool.onClick(0, 0);
        Shape b = rectTool.onClick(999, 999);
        System.out.println("Same object? " + (a == b));  // false
    }
}
```

Output:
```
Drawing Rectangle at (10,10) 80x40 StrokeStyle{color='black', width=2} FillStyle{color='blue', opacity=1.0}
Drawing Rectangle at (150,80) 80x40 StrokeStyle{color='black', width=2} FillStyle{color='blue', opacity=1.0}
Drawing Rectangle at (300,200) 80x40 StrokeStyle{color='black', width=2} FillStyle{color='blue', opacity=1.0}
Drawing Ellipse at (50,50) rx=30 ry=20 StrokeStyle{color='red', width=1} FillStyle{color='yellow', opacity=0.8}
Drawing TextBox at (100,100) text='Label' FillStyle{color='white', opacity=1.0}

--- Independence check ---
Same object? false
```

Key: `Tool` never uses `new Rectangle(...)`. It only knows the registry and a key string. Adding a new shape type requires zero changes to `Tool`.
</details>

---

### Problem 3: Shallow Copy Bug -- Find and Fix

The following `Rectangle.copy()` uses shallow copy for styles. Write a test that demonstrates the bug, then fix it.

```java
class Rectangle implements Shape {
    private int w, h, x, y;
    private StrokeStyle stroke;
    private FillStyle fill;

    public Rectangle(int w, int h, StrokeStyle stroke, FillStyle fill) {
        this.w = w; this.h = h; this.stroke = stroke; this.fill = fill;
    }

    @Override
    public Shape copy() {
        // BUG: shallow copy -- shares stroke and fill references
        return new Rectangle(w, h, stroke, fill);
    }

    @Override
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    @Override
    public void draw(Graphics2D g) {
        System.out.println("Rect at (" + x + "," + y + ") stroke=" + stroke + " fill=" + fill);
    }

    public StrokeStyle getStroke() { return stroke; }
    public FillStyle getFill() { return fill; }
}
```

**Task A:** Write code that creates a prototype, clones it, modifies the clone's stroke color, and prints both to show the bug.

**Task B:** Fix `copy()` so the bug no longer occurs.

**Task C:** If `StrokeStyle` were made immutable (all fields `final`, no setters), would the shallow copy still be a bug? Explain.

<details><summary>Solution</summary>

**Task A -- Demonstrating the bug:**

```java
class ShallowCopyBugDemo {
    public static void main(String[] args) {
        Rectangle proto = new Rectangle(100, 50,
            new StrokeStyle("black", 2),
            new FillStyle("blue", 1.0));

        Rectangle clone = (Rectangle) proto.copy();
        clone.setPosition(50, 50);

        // Modify clone's stroke
        clone.getStroke().setColor("red");

        proto.draw(null);
        clone.draw(null);
    }
}
```

Output (BUG):
```
Rect at (0,0) stroke=StrokeStyle{color='red', width=2} fill=FillStyle{color='blue', opacity=1.0}
Rect at (50,50) stroke=StrokeStyle{color='red', width=2} fill=FillStyle{color='blue', opacity=1.0}
```

Both show `red` because `proto` and `clone` share the same `StrokeStyle` object.

**Task B -- Fix:**

```java
@Override
public Shape copy() {
    return new Rectangle(w, h, stroke.copy(), fill.copy());  // deep copy
}
```

Output after fix:
```
Rect at (0,0) stroke=StrokeStyle{color='black', width=2} fill=FillStyle{color='blue', opacity=1.0}
Rect at (50,50) stroke=StrokeStyle{color='red', width=2} fill=FillStyle{color='blue', opacity=1.0}
```

Now each clone has its own style objects.

**Task C -- Immutable StrokeStyle:**

```java
final class StrokeStyle {
    private final String color;
    private final int width;

    public StrokeStyle(String color, int width) {
        this.color = color;
        this.width = width;
    }

    // No setters -- immutable
    public String getColor() { return color; }
    public int getWidth() { return width; }
}
```

If `StrokeStyle` is immutable, shallow copy is **not a bug**. Since there are no setters, no code can modify the shared object. The line `clone.getStroke().setColor("red")` would not even compile. Immutable value objects are safe to share -- this is exactly what the PDF means by "decide what is shared vs copied based on mutability."
</details>
