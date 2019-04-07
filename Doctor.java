import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Doctor {
    private Channel channel;
    private List<String> techniciansExchangeNames = Arrays.asList("hip", "elbow", "knee");
    private String doctorExchangeName;
    private String doctorQueueName;
    private String doctorQueueKey;

    private String fromAdminBroadcastExchange;
    private String fromAdminBroadcastQueue;
    private String toAdminBroadcastExchange;
    DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    public Doctor(String doctorExchangeName, String doctorQueueKey, String fromAdminBroadcastExchange, String fromAdminBroadcastQueue,
                  String toAdminBroadcastExchange) throws IOException, TimeoutException {
        this.doctorExchangeName = doctorExchangeName;
        this.doctorQueueKey = doctorQueueKey;
        this.fromAdminBroadcastExchange = fromAdminBroadcastExchange;
        this.fromAdminBroadcastQueue = fromAdminBroadcastQueue;
        this.toAdminBroadcastExchange = toAdminBroadcastExchange;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(this.fromAdminBroadcastExchange, BuiltinExchangeType.FANOUT);
        channel.queueDeclare(this.fromAdminBroadcastQueue, false, false, false, null);
        channel.queueBind(this.fromAdminBroadcastQueue, this.fromAdminBroadcastExchange, "");

        channel.exchangeDeclare(this.toAdminBroadcastExchange, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(this.doctorExchangeName, BuiltinExchangeType.DIRECT);
        doctorQueueName = channel.queueDeclare().getQueue();
        channel.queueBind(doctorQueueName, this.doctorExchangeName, this.doctorQueueKey);
    }

    private class PatientService extends Thread{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String patientMsg = doctorQueueName + "," + doctorQueueKey + ",";
        public PatientService() throws IOException {
            channel.exchangeDeclare(techniciansExchangeNames.get(0), BuiltinExchangeType.TOPIC);
            channel.exchangeDeclare(techniciansExchangeNames.get(1), BuiltinExchangeType.TOPIC);
            channel.exchangeDeclare(techniciansExchangeNames.get(2), BuiltinExchangeType.TOPIC);
        }

        @Override
        public void run(){
            String injuryTypeAndName = "";
            while(!injuryTypeAndName.equalsIgnoreCase("q")){
                System.out.print("Enter examination data: [type, name] , to quit: q >>>");
                try {
                    injuryTypeAndName = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] patientData = injuryTypeAndName.split(",");
                String tmp = patientMsg;
                tmp += injuryTypeAndName;
                try {
                    channel.basicPublish(patientData[0], patientData[0] + "s", null, tmp.getBytes("UTF-8"));
                    channel.basicPublish(toAdminBroadcastExchange, "", null, tmp.getBytes("UTF-8"));
                    Date date = new Date();
                    System.out.println("[" + df.format(date) +"]Commission sended: [" + injuryTypeAndName + "]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            Date date = new Date();
            System.out.println("[" + df.format(date) +"]Received: [" + message + "]");
            channel.basicAck(envelope.getDeliveryTag(), false);
        }
    };

    public void runDoctor() throws IOException, InterruptedException {
        PatientService patientService = new PatientService();
        patientService.start();
        channel.basicConsume(doctorQueueName, false, consumer);
        channel.basicConsume(fromAdminBroadcastQueue, false, consumer);
        patientService.join();
    }

}
