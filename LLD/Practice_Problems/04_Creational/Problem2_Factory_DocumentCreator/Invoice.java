import java.util.Map;

public class Invoice implements Document {

    private Map<String, String> fields;

    Invoice(Map<String, String> fields){
        this.fields = fields;
    }

    public String type() {
        return "invoice";
    }

    public String render(){
        return "INVOICE\n---\nItems: " + fields.getOrDefault("items", "N/A") +
               "\nTotal: " + fields.getOrDefault("total", "N/A");
    }
}
