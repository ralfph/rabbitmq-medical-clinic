import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class Technician {
    private Channel channel;
    private String doctorsExchangeName = "doctors";
    private String firstServiceType;
    private String secServiceType;

    private String fromAdminBroadcastExchange;
    private String fromAdminBroadcastQueue;
    private String toAdminBroadcastExchange;
    DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    public Technician(String firstServiceType, String secServiceType, String fromAdminBroadcastExchange, String fromAdminBroadcastQueue, String toAdminBroadcastExchange)
            throws IOException, TimeoutException {
        this.firstServiceType = firstServiceType;
        this.secServiceType = secServiceType;
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
        channel.basicQos(1);
        channel.exchangeDeclare(firstServiceType, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(secServiceType, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(doctorsExchangeName, BuiltinExchangeType.DIRECT);
    }

    Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            String[] msgInfo = message.split(",");
            Date date = new Date();
            if(!msgInfo[0].equals("Admin")){
                System.out.println("[" + df.format(date) +"]Received: [" + msgInfo[2] + ", " + msgInfo[3] + "]");
                String responseMsg = msgInfo[3] + " " + msgInfo[2] + " done";
                channel.basicAck(envelope.getDeliveryTag(), false);
                channel.basicPublish(doctorsExchangeName, msgInfo[1], null, responseMsg.getBytes("UTF-8"));
                channel.basicPublish(toAdminBroadcastExchange, "", null, responseMsg.getBytes("UTF-8"));
            }
            else{
                channel.basicAck(envelope.getDeliveryTag(), false);
                System.out.println("[" + df.format(date) +"]Received: [" + message + "]");
            }

        }
    };

    public void runTechnician() throws IOException {
        System.out.println("Starting Technician with specializations in: " + firstServiceType + ", " + secServiceType);
        channel.basicConsume(firstServiceType, false, consumer);
        channel.basicConsume(secServiceType, false, consumer);
        channel.basicConsume(fromAdminBroadcastQueue, false, consumer);
    }


}
