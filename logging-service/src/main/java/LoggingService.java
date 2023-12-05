import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class LoggingService {
    public static void main(String[] args) {

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance("mongodb+srv://flossboss-test:vaSEAvtHSumlixAv@test-cluster.wlvtb6y.mongodb.net/?retryWrites=true&w=majority");

        // Connect to the specific DB within the cluster
        databaseClient.connect("services-db");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("logger");

        // Publish payload to topic, placeholder
        brokerClient.publish("flossboss/test/publish", "I'm the LoggingService", 0);

        // Subscribe to topic, placeholder
        brokerClient.subscribe("flossboss/#", 0);

        // Placeholder callback functionality, replace with real logic once decided
        brokerClient.setCallback(new Logger());
    }
}
