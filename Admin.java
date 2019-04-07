import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Admin {
    private Channel channel;
    private String adminExchangeInName = "admin";
    private String adminQueueInName;
    private String adminBroadcastExchangeOutName = "broadcastAdmin";
    DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");



    public Admin(String adminQueueInName) throws IOException, TimeoutException {
        this.adminQueueInName = adminQueueInName;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(adminExchangeInName, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(adminBroadcastExchangeOutName, BuiltinExchangeType.FANOUT);
        channel.queueDeclare(this.adminQueueInName, false, false, false, null);
        channel.queueBind(this.adminQueueInName, this.adminExchangeInName, "");
    }

    Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            Date date = new Date();
            System.out.println("[" + df.format(date) + "]Received: [" + message + "]");
            channel.basicAck(envelope.getDeliveryTag(), false);
        }
    };

    private class BroadcastAdminMessageThread extends Thread{
        private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        private String adminHeader = "Admin,";
        @Override
        public void run(){
            String broadcastMsg = "";
            while(!broadcastMsg.equalsIgnoreCase("q")) {
                System.out.print("Enter msg for all members in system: >>>");
                try {
                    broadcastMsg = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String msgToSend = this.adminHeader + broadcastMsg;
                try {
                    channel.basicPublish(adminBroadcastExchangeOutName, "", null, msgToSend.getBytes("UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void runAdmin() throws IOException, InterruptedException {
        channel.basicConsume(adminQueueInName, false, consumer);
        BroadcastAdminMessageThread brMsgThread = new BroadcastAdminMessageThread();
        brMsgThread.start();
        brMsgThread.join();
    }

}
