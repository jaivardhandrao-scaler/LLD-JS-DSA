# SOLID Principles - MCQ Quiz

> 25 questions. Try to answer without looking at the notes first, then check.
> Mark yourself: 20+ = Exam ready | 15-19 = Revise weak areas | <15 = Re-read notes

---

### Q1: What does SRP stand for?
- A) Single Return Principle
- B) Single Responsibility Principle
- C) Simple Responsibility Principle
- D) Structured Responsibility Principle

<details><summary>Answer</summary>B) Single Responsibility Principle — A class should have only one reason to change.</details>

---

### Q2: Which is a violation of SRP?
```java
class Employee {
    String name;
    void save() { /* writes to file */ }
    void calculateTax() { /* computes tax */ }
}
```
- A) Employee should not have a name field
- B) save() and calculateTax() are two unrelated responsibilities
- C) Employee class is too small
- D) No violation here

<details><summary>Answer</summary>B) The class has three reasons to change: employee data, persistence logic, and tax rules. save() and calculateTax() should be in separate classes.</details>

---

### Q3: Which of these is NOT a quick check for SRP violation (from your curriculum)?
- A) Multiple if/else statements
- B) Unspecified Util/Helper class
- C) Monster methods
- D) Using inheritance

<details><summary>Answer</summary>D) Using inheritance is not an SRP red flag. The other three are directly from your curriculum as SRP violation indicators.</details>

---

### Q4: OCP says software entities should be:
- A) Open for modification, closed for extension
- B) Open for extension, closed for modification
- C) Open for both extension and modification
- D) Closed for both extension and modification

<details><summary>Answer</summary>B) Open for extension (add new behavior), closed for modification (don't change existing code).</details>

---

### Q5: Which code follows OCP?
- A) `if (type == "FTE") ... else if (type == "Intern") ...`
- B) Abstract class with subclasses for each type
- C) A single class with all logic
- D) A switch statement on employee type

<details><summary>Answer</summary>B) Using abstract classes/interfaces with subclasses means adding a new type = adding a new class. No existing code is modified.</details>

---

### Q6: In the curriculum's Tax Calculation example, what was wrong with having if/else in calculate()?
- A) It was too slow
- B) Adding a new employee type requires modifying existing code
- C) It used too much memory
- D) The tax rates were wrong

<details><summary>Answer</summary>B) Every new employee type means adding another if/else branch — modifying existing code. This violates OCP.</details>

---

### Q7: LSP states that:
- A) Lists should be substitutable for arrays
- B) Objects of a superclass should be replaceable with objects of subclasses without breaking the program
- C) Low-level modules should substitute high-level modules
- D) Subclasses must override all parent methods

<details><summary>Answer</summary>B) Liskov Substitution Principle — if B extends A, you should be able to use B wherever A is expected without issues.</details>

---

### Q8: Why does Kiwi extending Bird (with abstract fly()) violate LSP?
- A) Kiwi is not a real bird
- B) Kiwi cannot fly, so it throws an exception in fly(), breaking code that expects all Birds to fly
- C) Kiwi should use composition instead
- D) The Bird class has too many methods

<details><summary>Answer</summary>B) Code like `makeBirdFly(Bird b)` will crash for Kiwi. The subclass cannot substitute for the parent — LSP violated.</details>

---

### Q9: In the Rectangle-Square problem, what happens when you call setWidth(5) then setHeight(4) on a Square?
- A) Width = 5, Height = 4, Area = 20
- B) Width = 4, Height = 4, Area = 16
- C) Width = 5, Height = 5, Area = 25
- D) It throws an exception

<details><summary>Answer</summary>B) Square's setHeight(4) sets BOTH width and height to 4. Caller expects area = 20 (5*4) but gets 16 (4*4). LSP violated.</details>

---

### Q10: Which is a red flag for LSP violation?
- A) Using interfaces
- B) Constructor injection
- C) `throw new UnsupportedOperationException()` in a subclass method
- D) Having abstract methods

<details><summary>Answer</summary>C) If a subclass throws UnsupportedOperationException for an inherited method, it can't properly substitute for the parent.</details>

---

### Q11: ISP stands for:
- A) Interface Simplification Principle
- B) Interface Segregation Principle
- C) Interface Standardization Principle
- D) Internal Segregation Principle

<details><summary>Answer</summary>B) Interface Segregation Principle — no client should be forced to depend on methods it does not use.</details>

---

### Q12: From the curriculum — why is putting fly(), flapWings(), takeOff() in one Flyable interface problematic?
- A) Too many methods total
- B) Superman and MiG-21 can fly but don't flap wings — they're forced to implement flapWings()
- C) The interface is too simple
- D) Java doesn't support multiple methods in interfaces

<details><summary>Answer</summary>B) Not all flying things flap wings. Superman and MiG-21 are forced to implement a method they don't need — ISP violation.</details>

---

### Q13: How should the Employee interface be fixed for UnpaidIntern?
- A) Make UnpaidIntern return 0 for getSalary()
- B) Remove UnpaidIntern from the system
- C) Split into Employee (getEmail, getName) and Payable (processPayment, getSalary)
- D) Make processPayment() throw an exception

<details><summary>Answer</summary>C) Thin interfaces. UnpaidIntern implements only Employee. FTE implements both Employee and Payable. No forced methods.</details>

---

### Q14: Which interface design follows ISP?
- A) `interface Machine { print(); scan(); fax(); }`
- B) `interface Printer { print(); } interface Scanner { scan(); } interface Fax { fax(); }`
- C) `interface Everything { print(); scan(); fax(); email(); call(); }`
- D) All of the above

<details><summary>Answer</summary>B) Small, focused interfaces. OldPrinter implements only Printer. MultiFunctionPrinter implements all three. No forced methods.</details>

---

### Q15: DIP stands for:
- A) Dependency Injection Principle
- B) Dependency Inversion Principle
- C) Design Inversion Principle
- D) Data Inversion Principle

<details><summary>Answer</summary>B) Dependency Inversion Principle. Note: Dependency Injection is the TECHNIQUE; DIP is the PRINCIPLE. Common trick question!</details>

---

### Q16: What is the DIP violation in this code?
```java
class PaymentProcessor {
    void pay(String productId) {
        SqlProductRepo repo = new SqlProductRepo();
        Product p = repo.getProductById(productId);
    }
}
```
- A) PaymentProcessor is too simple
- B) High-level module (PaymentProcessor) directly depends on low-level module (SqlProductRepo)
- C) The method name is wrong
- D) productId should be an int

<details><summary>Answer</summary>B) PaymentProcessor creates a concrete SqlProductRepo. If we switch to Mongo, we must modify PaymentProcessor. Should depend on a ProductRepo interface instead.</details>

---

### Q17: Which is the correct way to fix the DIP violation above?
- A) Make SqlProductRepo static
- B) Create a ProductRepo interface and inject it via PaymentProcessor's constructor
- C) Put both classes in the same file
- D) Make PaymentProcessor extend SqlProductRepo

<details><summary>Answer</summary>B) Interface + constructor injection. PaymentProcessor takes ProductRepo (abstraction) in constructor. Swap implementations without changing PaymentProcessor.</details>

---

### Q18: In the Adapter PDF, SellerRankingService follows DIP because:
- A) It uses if/else to choose providers
- B) It creates SDSellerSearchService internally
- C) It depends on SellerSearch interface, not concrete provider classes
- D) It doesn't use any dependencies

<details><summary>Answer</summary>C) It takes SellerSearch (abstraction) via constructor injection. It never knows about SD or Exclusively concretions.</details>

---

### Q19: Which principle does this code violate?
```java
class OldPrinter implements Machine {
    void print() { /* works */ }
    void scan() { throw new UnsupportedOperationException(); }
    void fax() { throw new UnsupportedOperationException(); }
}
```
- A) SRP only
- B) ISP and LSP
- C) OCP only
- D) DIP only

<details><summary>Answer</summary>B) ISP — OldPrinter is forced to implement scan() and fax() it doesn't need. LSP — OldPrinter can't substitute for Machine because scan/fax throw exceptions.</details>

---

### Q20: Which design pattern best demonstrates OCP + DIP together?
- A) Singleton
- B) Builder
- C) Strategy
- D) Prototype

<details><summary>Answer</summary>C) Strategy — algorithms are behind an interface (DIP), and new algorithms can be added without modifying the context class (OCP).</details>

---

### Q21: A class creates 3 concrete objects (FileLogger, EmailService, MySQLDatabase) inside its methods. Which principles are violated?
- A) Only DIP
- B) DIP and SRP
- C) Only SRP
- D) Only OCP

<details><summary>Answer</summary>B) DIP — depends on concretions, not abstractions. SRP — logging, emailing, and database access are three different responsibilities handled in one class.</details>

---

### Q22: Which is NOT a valid way to achieve dependency injection?
- A) Constructor injection
- B) Setter injection
- C) Interface injection
- D) Inheritance injection

<details><summary>Answer</summary>D) There is no "inheritance injection." The three valid DI techniques are constructor, setter, and interface injection.</details>

---

### Q23: Which principle is violated when adding a Parrot requires modifying Bird's fly() method?
- A) SRP
- B) OCP
- C) LSP
- D) ISP

<details><summary>Answer</summary>B) OCP — adding a new type (Parrot) should mean adding a new class, not modifying existing code.</details>

---

### Q24: The SellerSearch interface has only 2 methods: getSellersBySku() and getSellerWithMaxDiscount(). This is an example of:
- A) SRP
- B) OCP
- C) LSP
- D) ISP

<details><summary>Answer</summary>D) ISP — the interface is minimal and client-specific. It only has what the ranking service needs, not a bloated interface with 20 provider methods.</details>

---

### Q25: Which combination correctly maps patterns to principles?
- A) Adapter → OCP + DIP + ISP + SRP, Strategy → OCP + DIP, Factory → OCP + DIP
- B) Adapter → SRP only, Strategy → LSP only, Factory → ISP only
- C) All patterns follow only OCP
- D) Patterns have nothing to do with SOLID

<details><summary>Answer</summary>A) Design patterns are solutions that naturally enforce SOLID principles. Adapter enforces OCP, DIP, ISP, SRP. Strategy enforces OCP, DIP. Factory enforces OCP, DIP.</details>

---

## Score Yourself

| Score | Verdict |
|-------|---------|
| 22-25 | Exam ready. Focus on speed and articulation. |
| 18-21 | Good. Revise the principles you got wrong. |
| 13-17 | Needs work. Re-read the notes for weak areas. |
| <13 | Start from the SOLID_Principles.md notes again. |

---

*Questions reference: SOLID Principles.pdf, DesignPattern_Adapter.pdf, DesignPattern_Strategy.pdf (Kshitij, Term 7)*
