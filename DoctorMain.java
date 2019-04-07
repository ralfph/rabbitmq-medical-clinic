import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DoctorMain {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        String doctorExchangeName = args[0];
        String doctorQueueKey = args[1];
        String fromBrAdmin = args[2];
        String fromAdminBrQueue = args[3];
        String toAdminBrExchange = args[4];
        Doctor doctor = new Doctor(doctorExchangeName, doctorQueueKey, fromBrAdmin, fromAdminBrQueue, toAdminBrExchange);
        doctor.runDoctor();
    }
}
