import java.util.Map;

public class Resume implements Document {

    private Map<String, String> fields;

    Resume(Map<String, String> fields){
        this.fields = fields;
    }

    public String type() {
        return "resume";
    }

    public String render(){
        return "RESUME\n---\nName: " + fields.getOrDefault("name", "N/A") +
               "\nSkills: " + fields.getOrDefault("skills", "N/A");
    }
}
