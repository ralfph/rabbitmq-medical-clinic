import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ExchangeBindings {
    private Channel channel;
    private List<String> techniciansExchangeNames = Arrays.asList("hip", "elbow", "knee");

    public ExchangeBindings() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(techniciansExchangeNames.get(0), BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(techniciansExchangeNames.get(1), BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(techniciansExchangeNames.get(2), BuiltinExchangeType.TOPIC);

        channel.queueDeclare(techniciansExchangeNames.get(0), false, false, false, null);
        channel.queueDeclare(techniciansExchangeNames.get(1), false, false, false, null);
        channel.queueDeclare(techniciansExchangeNames.get(2), false, false, false, null);

        channel.queueBind(techniciansExchangeNames.get(0), techniciansExchangeNames.get(0), "hips");
        channel.queueBind(techniciansExchangeNames.get(1), techniciansExchangeNames.get(1), "elbows");
        channel.queueBind(techniciansExchangeNames.get(2), techniciansExchangeNames.get(2), "knees");
    }



}
