import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class DatabaseClient {

    private String uri;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    /** Constructor that uses URI specified in environmental file **/
    public DatabaseClient(){
        this.loadURI();
    }

    /** Constructor that takes specific URI as an argument and connects to it **/
    public DatabaseClient(String uri){
        this.uri = uri;
    }

    /** Creates the MongoClient and takes a specific DB within your cluster **/
    public void connect(String db){
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(db);
    }

    /** Disconnect method  **/
    public void disconnect(){
        this.mongoClient.close();
    }

    /** Set the collection that you currently want to operate on **/
    public void setCollection(String collection){
        this.collection = database.getCollection(collection);
    }

    /** Creates a new entry in your database collection **/
    public void createItem(Document item) {
        InsertOneResult result = collection.insertOne(item);

        if (result.wasAcknowledged()) {
            System.out.println("Item inserted successfully!");
        } else {
            System.out.println("Failed to insert item.");
        }
    }

    /** Reads an item based on the item's ID and returns it as JSON**/
    public String readItem(String id) {

        String query;
        if(existsItem(id)){
            Document item = collection.find(eq("_id", new ObjectId(id))).first();
            query = item.toJson();
        }else{
            query = "No item with specified ID found";
        }

        return query;
    }

    /** Updates a single row of an item to your specified value **/
    public void updateItem(String id, String attribute, String newValue){

        if(this.existsItem(id)){
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set(attribute,newValue));
            if(result.wasAcknowledged()){
                System.out.println("Item successfully updated!");
            }else{
                System.out.println("Item was not updated.");
            }
        }else{
            System.out.println("Item not found.");
        }
    }

    /** Deletes item from specified collection based on item ID **/
    public void deleteItem(String id){
        if(this.existsItem(id)){
            DeleteResult result = this.collection.deleteOne(eq("_id", new ObjectId(id)));
            if(result.wasAcknowledged()){
                System.out.println("Item successfully deleted!");
            }else{
                System.out.println("Item was no deleted.");
            }
        }else{
            System.out.println("No matching document found.");
        }
    }

    /** Find item in DB based on ID, if item found it returns ture, else it returns false **/
    public boolean existsItem(String id) {

        FindIterable<Document> result = collection.find(eq("_id", new ObjectId(id))
        );

        // Check if any documents match the query
        return result.iterator().hasNext();
    }

    /** Gets the auto generated ID based on name **/
    public String getID(String name){
        String id;
        Document query = collection.find(eq("service_name", name)).first();
        if (query != null){
            id = query.get("_id").toString();
        } else {
            id = "No matching item was found.";
        }

        return id;
    }

    /** Helper method to load in the environmentals from the .txt file **/
    private void loadURI() {

        String path = "atlasconfig.txt";

        try (
                InputStream inputStream = BrokerClient.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.out.println("Cannot find "+path+" in classpath");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String[] configLines = reader.lines().collect(Collectors.joining("\n")).split("\n");

            // These need to be in the correct order in the txt file.
            this.uri = configLines[0].trim();
        } catch (IOException e) {
            System.out.println("Error configuring MongoDB client: " + e.getMessage());
        }
    }
}