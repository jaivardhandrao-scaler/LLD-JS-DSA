import java.util.Map;

public class InvoiceGenerator extends DocumentGenerator {

    public Invoice createDocument(Map<String , String> fields) {
        return new Invoice(fields);
    }
}
