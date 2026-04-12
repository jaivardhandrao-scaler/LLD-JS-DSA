import java.util.Map;

public class DocumentFactory {

    public static Document create(String type, Map<String, String> fields) {
        switch (type) {
            case "invoice": return new Invoice(fields);
            case "resume":  return new Resume(fields);
            case "letter":  return new Letter(fields);
            default: throw new IllegalArgumentException("Unknown document type: " + type);
        }
    }
}
