import java.util.Map;

public class Letter implements Document {

    private Map<String, String> fields;

    Letter(Map<String, String> fields){
        this.fields = fields;
    }

    public String type() {
        return "letter";
    }

    public String render(){
        return "LETTER\n---\nDear " + fields.getOrDefault("recipient", "N/A") +
               ", " + fields.getOrDefault("body", "N/A");
    }
}

