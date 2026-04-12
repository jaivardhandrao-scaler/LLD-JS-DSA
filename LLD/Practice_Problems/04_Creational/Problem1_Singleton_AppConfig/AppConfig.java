import java.util.*;

public class AppConfig{

    private final Map<String , String> properties;

    // private static volatile AppConfig INSTANCE;

    private AppConfig() {
        this.properties = new HashMap<>();
        this.properties.put("app.name" , "MyApp");
        this.properties.put("app.version" , "1.0");
        this.properties.put("db.host" , "localhost");
        this.properties.put("db.port" , "5432");
    }

    public String get(String key){
        return properties.get(key);
    }

    private static class Holder{
        private static final AppConfig INSTANCE = new AppConfig();
    }

    // static AppConfig getInstance(){
    //     if(INSTANCE == null){
    //         synchronized (AppConfig.class) {
    //             if(INSTANCE == null){
    //                 INSTANCE = new AppConfig();
    //             }
    //         }
    //     }
    //     return INSTANCE;
    // }
    static AppConfig getInstance(){
        return Holder.INSTANCE;
    }

    

}
