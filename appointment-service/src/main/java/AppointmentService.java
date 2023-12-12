import org.bson.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppointmentService {
    public static void main(String[] args) throws InterruptedException {

        // Create new thread pool with 8 threads
        ExecutorService threadPool = Executors.newFixedThreadPool(8);

        // Create the delay queue and assign one thread to it
        PendingQueue pendingQueue = PendingQueue.getInstance();
        threadPool.submit(pendingQueue);

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client
        DatabaseClient databaseClient = DatabaseClient.getInstance();
        // Connect to the specific DB within the cluster
        databaseClient.connect("flossboss");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("timeslots");

        // Creates an instance of the appointment handler and binds it to the callback
        brokerClient.setCallback(new AppointmentHandler(threadPool));
    }
}
