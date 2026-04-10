# Problem 4: Refactor the Notification Service

**Topic:** Identify and fix SOLID violations
**Difficulty:** Medium

---

## Scenario

You inherited this code. It works, but it's a SOLID disaster. Your job: identify every violation and refactor.

## The Bad Code

```java
class NotificationService {
    private String dbUrl;

    public NotificationService(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void sendEmail(String to, String subject, String body) {
        // validate email format
        if (!to.contains("@")) throw new RuntimeException("Bad email");
        System.out.println("EMAIL to " + to + ": " + subject);
        saveToDatabase(to, "email", subject);
    }

    public void sendSMS(String phone, String message) {
        if (phone.length() != 10) throw new RuntimeException("Bad phone");
        System.out.println("SMS to " + phone + ": " + message);
        saveToDatabase(phone, "sms", message);
    }

    public void sendPush(String deviceId, String title, String body) {
        System.out.println("PUSH to " + deviceId + ": " + title);
        saveToDatabase(deviceId, "push", title);
    }

    private void saveToDatabase(String recipient, String channel, String content) {
        System.out.println("Saving to DB at " + dbUrl + ": [" + channel + "] " + recipient);
    }

    public String generateReport() {
        return "Notification stats: ...";
    }
}
```

## Your Task

1. Create a file `VIOLATIONS.md` listing every SOLID violation you find (which principle, which line, why it's bad)

2. Refactor into clean classes:
   - A `Notifier` interface with a single method
   - Concrete: `EmailNotifier`, `SmsNotifier`, `PushNotifier`
   - A separate `NotificationLogger` for database saving
   - A separate `ReportGenerator` for reporting
   - A `NotificationService` that takes a `List<Notifier>` and a `NotificationLogger`

3. Create `Main` showing how to wire and use the refactored code

## What to create

```
VIOLATIONS.md
Notifier.java
EmailNotifier.java
SmsNotifier.java
PushNotifier.java
NotificationLogger.java
ReportGenerator.java
NotificationService.java
Main.java
```

## What I'll check

- `VIOLATIONS.md` correctly identifies SRP, OCP, DIP, ISP violations
- Each refactored class has exactly one responsibility
- `NotificationService` depends on abstractions, not concrete notifiers
- Adding a new channel (e.g., Slack) requires ZERO changes to existing code
- Validation lives in the right place (each notifier validates its own input)
