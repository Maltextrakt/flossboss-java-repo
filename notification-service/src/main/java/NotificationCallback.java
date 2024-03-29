import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.ExecutorService;

public class NotificationCallback implements MqttCallback {

    private final ExecutorService threadPool;
    private final BrokerClient brokerClient;
    private final NotificationHandler handler;
    private final HealthHandler healthHandler;

    public NotificationCallback(ExecutorService threadPool){
        this.threadPool = threadPool;
        this.brokerClient = BrokerClient.getInstance();
        this.handler = new NotificationHandler();
        this.healthHandler = new HealthHandler();
    }
    @Override
    public void connectionLost(Throwable cause) {
        reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message){
        String payload = message.toString();

        if (topic.startsWith(Topic.CONFIRM.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.confirmation(payload));
            }
        } else if (topic.startsWith(Topic.CANCEL_DENTIST.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.dentistCancellation(payload));
            }
        } else if (topic.startsWith(Topic.CANCEL_USER.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.userCancellation(payload));
            }
        } else if (topic.startsWith(Topic.SUBSCRIPTION.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.subscriptionUpdate(payload));
            }
        } else if (topic.equals(Topic.PING.getStringValue())) {
            // Create separate thread to handle pings
            Thread healthThread = new Thread(healthHandler::echo);
            // Set the thread as a daemon so that it won't prevent the application from exiting
            healthThread.setDaemon(true);

            healthThread.start();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (!token.isComplete()) {
            System.out.println("Message delivery failed.");
            if (token.getException() != null) {
                token.getException().printStackTrace();
            }
        }
    }

    /** Checks if payload is in the correct format. **/
    protected boolean isValidPayload(String payload){
        try {
            JsonObject jsonPayload = JsonParser.parseString(payload).getAsJsonObject();

            // Check that every attribute of the appointment is present.
            if (jsonPayload.has("_id")
                    && jsonPayload.has("_clinicId")
                    && jsonPayload.has("_dentistId")
                    && jsonPayload.has("_userId")
                    && jsonPayload.has("date")
                    && jsonPayload.has("timeFrom")
                    && jsonPayload.has("timeTo")
                    && jsonPayload.has("isAvailable")
                    && jsonPayload.has("isPending")
                    && jsonPayload.has("isBooked")) {
                return true;
            } else if(jsonPayload.has("_id")
                    && jsonPayload.has("date")
                    && jsonPayload.has("clinicName")
                    && jsonPayload.has("userEmails")) {
                return true;
            } else {
                System.out.println("Invalid Payload");
                return false;
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Invalid JSON Syntax");
            return false;
        }
    }

    /** Reconnection logic **/
    private void reconnect(){
        brokerClient.reconnect();
        brokerClient.setCallback(this);
    }

    /**{"_id": {"$oid": "658808724d4ae76f7ccad9eb"}, "_clinicId": "657844d2fb84354ce31a0a77", "date": {"$date": "2024-01-01T00:00:00Z"}, "clinicName": "WeSmile Visby", "userEmails": ["joel_celen@hotmail.com", "celen.joel@gmail.com", "j.celen@protonmail.com"], "__v": 0} **/
}
