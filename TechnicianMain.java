import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TechnicianMain {
    public static void main(String[] args) throws IOException, TimeoutException {
        String firstServiceType = args[0];
        String secServiceType = args[1];
        String fromBrAdmin = args[2];
        String fromAdminBrQueue = args[3];
        String toAdminBrExchange = args[4];
        Technician technician = new Technician(firstServiceType, secServiceType, fromBrAdmin, fromAdminBrQueue, toAdminBrExchange );
        technician.runTechnician();
    }
}
