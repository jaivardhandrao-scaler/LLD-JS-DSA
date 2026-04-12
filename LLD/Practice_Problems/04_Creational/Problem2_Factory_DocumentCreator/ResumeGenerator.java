import java.util.Map;

public class ResumeGenerator extends DocumentGenerator {

    public Resume createDocument(Map<String , String> fields) {
        return new Resume(fields);
    }
}
