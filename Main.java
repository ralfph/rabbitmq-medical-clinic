
public class Main {
    public static void main(String[] args) {
        try {
            ExchangeBindings exBind = new ExchangeBindings();
            System.out.println("Exchanges and Queues for Technicians created");
            Admin admin = new Admin("adminQueue");
            admin.runAdmin();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
