import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        // Part A: Simple Factory
        Map<String, String> invoiceFields = new HashMap<>();
        invoiceFields.put("items", "Laptop, Mouse");
        invoiceFields.put("total", "Rs.55000");

        Map<String, String> resumeFields = new HashMap<>();
        resumeFields.put("name", "Jai Vardhan");
        resumeFields.put("skills", "Java, LLD, DSA");

        Map<String, String> letterFields = new HashMap<>();
        letterFields.put("recipient", "HR Team");
        letterFields.put("body", "I am writing to apply for the SDE role.");

        Document invoice = DocumentFactory.create("invoice", invoiceFields);
        Document resume  = DocumentFactory.create("resume", resumeFields);
        Document letter  = DocumentFactory.create("letter", letterFields);

        System.out.println(invoice.render());
        System.out.println();
        System.out.println(resume.render());
        System.out.println();
        System.out.println(letter.render());

        System.out.println("\n=============================\n");

        // Part B: Factory Method
        DocumentGenerator invoiceGen = new InvoiceGenerator();
        DocumentGenerator resumeGen  = new ResumeGenerator();

        System.out.println(invoiceGen.generateAndSave(invoiceFields));
        System.out.println();
        System.out.println(resumeGen.generateAndSave(resumeFields));
    }
}
