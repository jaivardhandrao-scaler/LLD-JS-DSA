public class Main {
    public static void main(String[] args) {
        
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        if(instance1 == instance2){
            System.out.println("Same instances");
        }else{
            System.out.println("different instances");
        }
    }
}
