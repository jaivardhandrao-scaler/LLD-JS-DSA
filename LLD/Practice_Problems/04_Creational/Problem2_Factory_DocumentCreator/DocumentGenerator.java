import java.util.Map;

public abstract class DocumentGenerator {

    final String generateAndSave(Map<String, String> fields){
        Document doc = createDocument(fields);
        return doc.render() + "\n --- Saved";
    }

    abstract Document createDocument(Map<String, String> fields);
}
