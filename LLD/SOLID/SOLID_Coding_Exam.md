# SOLID Principles - Coding Exam Questions

> 3 coding questions of increasing difficulty. These simulate end-term coding exam style.
> For each: identify violations, then write the corrected code.

---

## Question 1: Notification System (Medium)

### Problem Statement

You are building a notification system for an e-commerce platform. The current implementation is below. A developer needs to add **Push Notification** support and **WhatsApp** support in the future.

```java
class NotificationManager {
    
    void sendNotification(String type, String userId, String message) {
        if (type.equals("EMAIL")) {
            // connect to SMTP server
            SmtpClient smtp = new SmtpClient("smtp.company.com");
            String email = fetchEmailFromDB(userId);
            smtp.send(email, message);
            System.out.println("Email sent to " + email);
            
        } else if (type.equals("SMS")) {
            // connect to Twilio
            TwilioClient twilio = new TwilioClient("API_KEY_123");
            String phone = fetchPhoneFromDB(userId);
            twilio.sendSMS(phone, message);
            System.out.println("SMS sent to " + phone);
        }
        
        // Log to file
        FileWriter fw = new FileWriter("notifications.log");
        fw.write(type + " sent to " + userId + ": " + message);
        fw.close();
    }
    
    private String fetchEmailFromDB(String userId) {
        MySQLConnection conn = new MySQLConnection("db.company.com");
        return conn.query("SELECT email FROM users WHERE id = " + userId);
    }
    
    private String fetchPhoneFromDB(String userId) {
        MySQLConnection conn = new MySQLConnection("db.company.com");
        return conn.query("SELECT phone FROM users WHERE id = " + userId);
    }
}
```

### Task

**Part A:** Identify ALL SOLID violations in the code above. For each, name the principle and explain why.

**Part B:** Rewrite the code so that it follows all SOLID principles. Adding Push Notification and WhatsApp support should require NO changes to existing classes.

---

### Solution

#### Part A: Violations

| # | Principle | Violation |
|---|-----------|-----------|
| 1 | **SRP** | `NotificationManager` handles sending, logging, AND database queries — three responsibilities. |
| 2 | **OCP** | Adding Push/WhatsApp means adding more if/else branches — must modify existing code. |
| 3 | **DIP** | `new SmtpClient(...)`, `new TwilioClient(...)`, `new FileWriter(...)`, `new MySQLConnection(...)` — depends on concretions everywhere. |
| 4 | **DIP** | `fetchEmailFromDB` creates `MySQLConnection` directly — can't swap to Postgres/Mongo. |

#### Part B: Fixed Code

```java
// --- Abstractions ---

interface NotificationSender {
    void send(String contactInfo, String message);
}

interface UserRepository {
    String getEmail(String userId);
    String getPhone(String userId);
    String getContactInfo(String userId, String channel);
}

interface NotificationLogger {
    void log(String type, String userId, String message);
}

// --- Concrete Implementations ---

class EmailSender implements NotificationSender {
    private final SmtpClient smtp;
    
    EmailSender(SmtpClient smtp) {
        this.smtp = smtp;
    }
    
    @Override
    public void send(String email, String message) {
        smtp.send(email, message);
        System.out.println("Email sent to " + email);
    }
}

class SmsSender implements NotificationSender {
    private final TwilioClient twilio;
    
    SmsSender(TwilioClient twilio) {
        this.twilio = twilio;
    }
    
    @Override
    public void send(String phone, String message) {
        twilio.sendSMS(phone, message);
        System.out.println("SMS sent to " + phone);
    }
}

// Adding Push? Just a new class — no existing code changes!
class PushSender implements NotificationSender {
    private final FirebaseClient firebase;
    
    PushSender(FirebaseClient firebase) {
        this.firebase = firebase;
    }
    
    @Override
    public void send(String deviceToken, String message) {
        firebase.push(deviceToken, message);
        System.out.println("Push sent to " + deviceToken);
    }
}

class MySQLUserRepository implements UserRepository {
    private final MySQLConnection conn;
    
    MySQLUserRepository(MySQLConnection conn) {
        this.conn = conn;
    }
    
    @Override
    public String getEmail(String userId) {
        return conn.query("SELECT email FROM users WHERE id = " + userId);
    }
    
    @Override
    public String getPhone(String userId) {
        return conn.query("SELECT phone FROM users WHERE id = " + userId);
    }
    
    @Override
    public String getContactInfo(String userId, String channel) {
        if (channel.equals("EMAIL")) return getEmail(userId);
        if (channel.equals("SMS")) return getPhone(userId);
        return conn.query("SELECT " + channel.toLowerCase() + " FROM users WHERE id = " + userId);
    }
}

class FileNotificationLogger implements NotificationLogger {
    @Override
    public void log(String type, String userId, String message) {
        // write to file
    }
}

// --- Clean NotificationManager ---

class NotificationManager {
    private final Map<String, NotificationSender> senders;
    private final UserRepository userRepo;
    private final NotificationLogger logger;
    
    // All dependencies injected via constructor (DIP)
    NotificationManager(Map<String, NotificationSender> senders,
                        UserRepository userRepo,
                        NotificationLogger logger) {
        this.senders = senders;
        this.userRepo = userRepo;
        this.logger = logger;
    }
    
    void sendNotification(String type, String userId, String message) {
        NotificationSender sender = senders.get(type);
        if (sender == null) {
            throw new IllegalArgumentException("Unknown notification type: " + type);
        }
        
        String contactInfo = userRepo.getContactInfo(userId, type);
        sender.send(contactInfo, message);
        logger.log(type, userId, message);
    }
}

// --- Wiring ---

class Main {
    public static void main(String[] args) {
        Map<String, NotificationSender> senders = new HashMap<>();
        senders.put("EMAIL", new EmailSender(new SmtpClient("smtp.company.com")));
        senders.put("SMS", new SmsSender(new TwilioClient("API_KEY")));
        senders.put("PUSH", new PushSender(new FirebaseClient("FB_KEY")));
        // Adding WhatsApp? Just one more line here:
        // senders.put("WHATSAPP", new WhatsAppSender(new WhatsAppClient("WA_KEY")));
        
        UserRepository repo = new MySQLUserRepository(new MySQLConnection("db.company.com"));
        NotificationLogger logger = new FileNotificationLogger();
        
        NotificationManager manager = new NotificationManager(senders, repo, logger);
        manager.sendNotification("EMAIL", "user123", "Your order shipped!");
    }
}
```

**SOLID Checklist for the fix:**
- **SRP**: NotificationManager only orchestrates. Each sender only sends. Logger only logs. Repo only fetches data.
- **OCP**: New channel = new `NotificationSender` class + register in map. Zero existing code modified.
- **LSP**: All `NotificationSender` implementations honor the `send()` contract.
- **ISP**: `NotificationSender` has one method. `UserRepository` has only user-fetch methods. No fat interfaces.
- **DIP**: Everything injected via constructor. No `new ConcreteClass()` inside business logic.

---

## Question 2: Payment Gateway (Medium-Hard)

### Problem Statement

You're building a payment system for an online store. The system supports CreditCard and UPI payments. Each payment type has different processing and validation rules.

```java
class PaymentService {
    
    double processPayment(String method, double amount, Map<String, String> details) {
        double fee;
        
        if (method.equals("CREDIT_CARD")) {
            // Validate card
            String cardNumber = details.get("cardNumber");
            if (cardNumber == null || cardNumber.length() != 16) {
                throw new RuntimeException("Invalid card number");
            }
            
            // Calculate fee (2.5% for credit card)
            fee = amount * 0.025;
            
            // Process via Razorpay
            RazorpayClient rp = new RazorpayClient("rzp_key");
            rp.charge(cardNumber, amount + fee);
            
        } else if (method.equals("UPI")) {
            // Validate UPI ID
            String upiId = details.get("upiId");
            if (upiId == null || !upiId.contains("@")) {
                throw new RuntimeException("Invalid UPI ID");
            }
            
            // Calculate fee (0.5% for UPI)
            fee = amount * 0.005;
            
            // Process via PayTM
            PaytmClient pt = new PaytmClient("ptm_key");
            pt.transfer(upiId, amount + fee);
            
        } else {
            throw new RuntimeException("Unknown method");
        }
        
        // Save transaction
        MySQLConnection db = new MySQLConnection("db.store.com");
        db.execute("INSERT INTO transactions ...");
        
        return fee;
    }
}
```

### Task

**Part A:** List every SOLID violation.

**Part B:** Redesign using SOLID principles. The system should easily support adding NetBanking and Wallet payments in the future.

---

### Solution

#### Part A: Violations

| # | Principle | Violation |
|---|-----------|-----------|
| 1 | **SRP** | One class handles validation, fee calculation, payment processing, AND transaction storage. |
| 2 | **OCP** | Adding NetBanking/Wallet = new if/else branch = modifying existing code. |
| 3 | **DIP** | `new RazorpayClient(...)`, `new PaytmClient(...)`, `new MySQLConnection(...)` — all concrete dependencies. |
| 4 | **SRP** | Fee calculation logic is mixed with processing logic within the same method. |

#### Part B: Fixed Code

```java
// --- Payment Strategy Interface (Strategy Pattern + DIP + OCP) ---

interface PaymentMethod {
    void validate(Map<String, String> details);
    double calculateFee(double amount);
    void process(double amount, Map<String, String> details);
}

// --- Concrete Strategies ---

class CreditCardPayment implements PaymentMethod {
    private final RazorpayClient gateway;
    
    CreditCardPayment(RazorpayClient gateway) {
        this.gateway = gateway;
    }
    
    @Override
    public void validate(Map<String, String> details) {
        String card = details.get("cardNumber");
        if (card == null || card.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }
    }
    
    @Override
    public double calculateFee(double amount) {
        return amount * 0.025;  // 2.5%
    }
    
    @Override
    public void process(double amount, Map<String, String> details) {
        gateway.charge(details.get("cardNumber"), amount);
    }
}

class UpiPayment implements PaymentMethod {
    private final PaytmClient gateway;
    
    UpiPayment(PaytmClient gateway) {
        this.gateway = gateway;
    }
    
    @Override
    public void validate(Map<String, String> details) {
        String upi = details.get("upiId");
        if (upi == null || !upi.contains("@")) {
            throw new IllegalArgumentException("Invalid UPI ID");
        }
    }
    
    @Override
    public double calculateFee(double amount) {
        return amount * 0.005;  // 0.5%
    }
    
    @Override
    public void process(double amount, Map<String, String> details) {
        gateway.transfer(details.get("upiId"), amount);
    }
}

// Future: Just add this class. Nothing else changes.
class NetBankingPayment implements PaymentMethod {
    private final BankClient bank;
    
    NetBankingPayment(BankClient bank) { this.bank = bank; }
    
    @Override
    public void validate(Map<String, String> details) {
        if (details.get("bankCode") == null) {
            throw new IllegalArgumentException("Bank code required");
        }
    }
    
    @Override
    public double calculateFee(double amount) {
        return amount * 0.01;  // 1%
    }
    
    @Override
    public void process(double amount, Map<String, String> details) {
        bank.initiateTransfer(details.get("bankCode"), amount);
    }
}

// --- Transaction Repository (SRP + DIP) ---

interface TransactionRepository {
    void save(String method, double amount, double fee);
}

class MySQLTransactionRepo implements TransactionRepository {
    private final MySQLConnection conn;
    
    MySQLTransactionRepo(MySQLConnection conn) { this.conn = conn; }
    
    @Override
    public void save(String method, double amount, double fee) {
        conn.execute("INSERT INTO transactions (method, amount, fee) VALUES (...)");
    }
}

// --- Clean PaymentService (SRP - only orchestrates) ---

class PaymentService {
    private final Map<String, PaymentMethod> methods;
    private final TransactionRepository txnRepo;
    
    PaymentService(Map<String, PaymentMethod> methods, TransactionRepository txnRepo) {
        this.methods = methods;
        this.txnRepo = txnRepo;
    }
    
    double processPayment(String methodName, double amount, Map<String, String> details) {
        PaymentMethod method = methods.get(methodName);
        if (method == null) {
            throw new IllegalArgumentException("Unknown payment method: " + methodName);
        }
        
        method.validate(details);
        double fee = method.calculateFee(amount);
        method.process(amount + fee, details);
        txnRepo.save(methodName, amount, fee);
        
        return fee;
    }
}
```

**Key design decisions:**
- `PaymentMethod` interface acts as a **Strategy** — each payment type encapsulates its own validation, fee, and processing.
- `PaymentService` is now a thin orchestrator — **SRP**.
- Adding NetBanking = new class, register in map — **OCP**.
- All dependencies injected — **DIP**.
- `PaymentMethod` has 3 cohesive methods (validate, fee, process) — **ISP** (all implementors need all 3).
- All implementations honor the contract — **LSP**.

---

## Question 3: Vehicle Service Center (Hard)

### Problem Statement

A vehicle service center manages different types of vehicles. Each vehicle type has different service procedures. The current code handles Cars and Bikes.

```java
class ServiceCenter {
    
    void performService(String vehicleType, String vehicleId) {
        MySQLConnection db = new MySQLConnection("db.service.com");
        
        if (vehicleType.equals("CAR")) {
            System.out.println("Checking car engine oil...");
            System.out.println("Checking car AC system...");
            System.out.println("Checking car tire alignment...");
            System.out.println("Checking car brake pads...");
            
            double cost = 2500.0;
            db.execute("UPDATE vehicles SET last_service = NOW() WHERE id = " + vehicleId);
            
            EmailClient email = new EmailClient();
            String ownerEmail = db.query("SELECT email FROM owners WHERE vehicle_id = " + vehicleId);
            email.send(ownerEmail, "Car service done. Cost: " + cost);
            
        } else if (vehicleType.equals("BIKE")) {
            System.out.println("Checking bike chain tension...");
            System.out.println("Checking bike engine oil...");
            System.out.println("Checking bike tire pressure...");
            
            double cost = 800.0;
            db.execute("UPDATE vehicles SET last_service = NOW() WHERE id = " + vehicleId);
            
            SMSClient sms = new SMSClient();
            String ownerPhone = db.query("SELECT phone FROM owners WHERE vehicle_id = " + vehicleId);
            sms.send(ownerPhone, "Bike service done. Cost: " + cost);
        }
    }
}

// Upcoming requirements:
// - Add Truck service (different checks, higher cost)
// - Add Electric Vehicle service (battery check, no oil check)
// - Some customers prefer WhatsApp over Email/SMS
// - Need PDF invoice generation
```

### Task

**Part A:** Identify all SOLID violations. Explain how each upcoming requirement would make things worse.

**Part B:** Redesign the entire system following SOLID. Show how Truck, EV, WhatsApp, and PDF invoices can be added with ZERO changes to existing classes.

---

### Solution

#### Part A: Violations

| # | Principle | Violation | Impact of New Requirements |
|---|-----------|-----------|---------------------------|
| 1 | **SRP** | ServiceCenter does service logic, DB ops, notifications, cost calculation | Adding PDF invoices = yet another responsibility crammed in |
| 2 | **OCP** | if/else on vehicleType | Adding Truck/EV = new branches, modifying this class |
| 3 | **DIP** | `new MySQLConnection(...)`, `new EmailClient()`, `new SMSClient()` | Switching notification preference per customer = more concrete deps |
| 4 | **SRP** | Notification type (Email vs SMS) is decided inside service logic | WhatsApp support = more if/else inside already bloated method |
| 5 | **OCP** | Notification channel is hardcoded per vehicle type | Customer notification preference shouldn't depend on vehicle type |

**Each new requirement makes the if/else tree deeper and wider. The method would grow to 100+ lines with 4 vehicle types x 3 notification channels x optional invoice.**

#### Part B: Fixed Code

```java
// ============================================
// LAYER 1: Vehicle Service Procedures (OCP)
// ============================================

interface VehicleService {
    List<String> getChecks();
    double getCost();
}

class CarService implements VehicleService {
    @Override
    public List<String> getChecks() {
        return List.of(
            "Checking car engine oil",
            "Checking car AC system",
            "Checking car tire alignment",
            "Checking car brake pads"
        );
    }
    
    @Override
    public double getCost() { return 2500.0; }
}

class BikeService implements VehicleService {
    @Override
    public List<String> getChecks() {
        return List.of(
            "Checking bike chain tension",
            "Checking bike engine oil",
            "Checking bike tire pressure"
        );
    }
    
    @Override
    public double getCost() { return 800.0; }
}

// NEW — Truck: just a new class
class TruckService implements VehicleService {
    @Override
    public List<String> getChecks() {
        return List.of(
            "Checking truck engine",
            "Checking truck hydraulics",
            "Checking truck tire pressure",
            "Checking truck cargo locks",
            "Checking truck brake system"
        );
    }
    
    @Override
    public double getCost() { return 5000.0; }
}

// NEW — EV: just a new class (no oil check — LSP safe!)
class ElectricVehicleService implements VehicleService {
    @Override
    public List<String> getChecks() {
        return List.of(
            "Checking battery health",
            "Checking motor controller",
            "Checking regenerative braking",
            "Checking tire pressure"
        );
    }
    
    @Override
    public double getCost() { return 3500.0; }
}

// ============================================
// LAYER 2: Notification (ISP + OCP)
// ============================================

interface NotificationSender {
    void send(String contact, String message);
}

class EmailSender implements NotificationSender {
    public void send(String email, String message) {
        System.out.println("Email to " + email + ": " + message);
    }
}

class SmsSender implements NotificationSender {
    public void send(String phone, String message) {
        System.out.println("SMS to " + phone + ": " + message);
    }
}

// NEW — WhatsApp: just a new class
class WhatsAppSender implements NotificationSender {
    public void send(String phone, String message) {
        System.out.println("WhatsApp to " + phone + ": " + message);
    }
}

// ============================================
// LAYER 3: Invoice Generation (OCP + SRP)
// ============================================

interface InvoiceGenerator {
    void generate(String vehicleId, String vehicleType, double cost);
}

class ConsoleInvoice implements InvoiceGenerator {
    public void generate(String vehicleId, String vehicleType, double cost) {
        System.out.println("Invoice: " + vehicleType + " #" + vehicleId + " - Rs." + cost);
    }
}

// NEW — PDF Invoice: just a new class
class PdfInvoice implements InvoiceGenerator {
    public void generate(String vehicleId, String vehicleType, double cost) {
        System.out.println("Generating PDF invoice for " + vehicleId);
        // PDF generation logic here
    }
}

// ============================================
// LAYER 4: Repository (SRP + DIP)
// ============================================

interface VehicleRepository {
    void updateLastService(String vehicleId);
    String getOwnerContact(String vehicleId);
}

class MySQLVehicleRepository implements VehicleRepository {
    private final MySQLConnection conn;
    
    MySQLVehicleRepository(MySQLConnection conn) { this.conn = conn; }
    
    @Override
    public void updateLastService(String vehicleId) {
        conn.execute("UPDATE vehicles SET last_service = NOW() WHERE id = " + vehicleId);
    }
    
    @Override
    public String getOwnerContact(String vehicleId) {
        return conn.query("SELECT contact FROM owners WHERE vehicle_id = " + vehicleId);
    }
}

// ============================================
// LAYER 5: Clean ServiceCenter (SRP - orchestration only)
// ============================================

class ServiceCenter {
    private final Map<String, VehicleService> services;
    private final VehicleRepository vehicleRepo;
    private final NotificationSender notifier;
    private final InvoiceGenerator invoiceGen;
    
    // Everything injected (DIP)
    ServiceCenter(Map<String, VehicleService> services,
                  VehicleRepository vehicleRepo,
                  NotificationSender notifier,
                  InvoiceGenerator invoiceGen) {
        this.services = services;
        this.vehicleRepo = vehicleRepo;
        this.notifier = notifier;
        this.invoiceGen = invoiceGen;
    }
    
    void performService(String vehicleType, String vehicleId) {
        VehicleService service = services.get(vehicleType);
        if (service == null) {
            throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
        }
        
        // 1. Perform checks
        for (String check : service.getChecks()) {
            System.out.println(check);
        }
        
        // 2. Update DB
        vehicleRepo.updateLastService(vehicleId);
        
        // 3. Generate invoice
        double cost = service.getCost();
        invoiceGen.generate(vehicleId, vehicleType, cost);
        
        // 4. Notify owner
        String contact = vehicleRepo.getOwnerContact(vehicleId);
        notifier.send(contact, vehicleType + " service done. Cost: Rs." + cost);
    }
}

// ============================================
// WIRING
// ============================================

class Main {
    public static void main(String[] args) {
        // Register vehicle services
        Map<String, VehicleService> services = new HashMap<>();
        services.put("CAR", new CarService());
        services.put("BIKE", new BikeService());
        services.put("TRUCK", new TruckService());
        services.put("EV", new ElectricVehicleService());
        
        // Choose dependencies
        VehicleRepository repo = new MySQLVehicleRepository(new MySQLConnection("db.service.com"));
        NotificationSender notifier = new WhatsAppSender();  // easily swappable!
        InvoiceGenerator invoice = new PdfInvoice();          // easily swappable!
        
        ServiceCenter center = new ServiceCenter(services, repo, notifier, invoice);
        
        center.performService("CAR", "V-101");
        center.performService("EV", "V-202");
        center.performService("TRUCK", "V-303");
    }
}
```

**Final SOLID Checklist:**

| Principle | How It's Satisfied |
|-----------|-------------------|
| **SRP** | ServiceCenter orchestrates. VehicleService defines checks. NotificationSender sends. InvoiceGenerator generates. VehicleRepository persists. Each class, one job. |
| **OCP** | New vehicle (Truck, EV) = new VehicleService class. New notification (WhatsApp) = new NotificationSender. New invoice (PDF) = new InvoiceGenerator. Zero existing code modified. |
| **LSP** | All VehicleService implementations return valid checks and costs. All NotificationSenders send messages. No exceptions, no empty methods. Every child honors the parent contract. |
| **ISP** | Each interface is small and focused. VehicleService: checks + cost. NotificationSender: send. InvoiceGenerator: generate. No fat interfaces. |
| **DIP** | ServiceCenter depends on 4 abstractions (interfaces), all injected via constructor. No `new ConcreteClass()` inside business logic. Swap any implementation at wiring time. |

---

## Exam Strategy Tips

1. **Read the full question first.** Identify the domain before coding.
2. **Start by listing violations.** This shows the examiner you understand the problem before jumping to a solution.
3. **Design interfaces first.** Then implementations. Then the orchestrator class. Then wiring.
4. **Name the patterns you're using.** ("This uses the Strategy pattern for payment methods" — shows depth.)
5. **Always include the wiring/main method.** It proves your design works end-to-end.
6. **Check each SOLID principle.** After writing your solution, verify each one is satisfied. Write a small table like the one above.

---

*References: SOLID Principles.pdf, DesignPattern_Adapter.pdf, DesignPattern_Strategy.pdf, DesignPattern_Decorator.pdf (Kshitij, Term 7)*
