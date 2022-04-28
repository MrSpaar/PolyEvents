package ci.polyevent;

import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.lang.Nullable;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.Collections;

public class MongoDB {
    private static final MongoClient client = MongoClients.create("MongoDB link ;)");
    private static final MongoCollection<Document> collection = client.getDatabase("polyevent").getCollection("events");

    public static FindIterable<Document> getEvents() {
        return collection.find();
    }

    public static  FindIterable<Document> filterEvents(String displayName) {
        Document query = new Document("players.name", displayName);
        return collection.find(query);
    }

    public static Document findEvent(String name) {
        return collection.find(new Document("_id", name)).first();
    }

    public static void createEvent(String name, String description, String world, double... coords) {
        Document event = new Document("_id", name);
        event.append("description", description);
        event.append("world", world);
        event.append("location", Arrays.asList(coords[0], coords[1], coords[2]));
        event.append("players", Collections.emptyList());

        collection.insertOne(event);
    }

    public static void deleteEvent(String name) {
        collection.deleteOne(new Document("_id", name));
    }

    @Nullable
    public static Document findPlayer(String name, String displayName) {
        Document query = new Document("_id", name);
        query.append("players.name", displayName);

        Document event = collection.find(query).first();
        if (event == null) return null;

        for (Document player: event.getList("players", Document.class)) {
            if (player.get("name").equals(displayName)) return player;
        }

        return null;
    }

    public static void updatePlayer(String name, String displayName, String world, double... coords) {
        Document query = new Document("_id", name);

        Document player = new Document("name", displayName);
        player.append("world", world);
        player.append("location", Arrays.asList(coords[0], coords[1], coords[2]));

        Bson update = Updates.addToSet("players", player);
        UpdateOptions options = new UpdateOptions().upsert(true);

        collection.updateOne(query, update, options);
    }

    public static void removePlayer(String name, String displayName) {
        Document query = new Document();
        Document update = new Document("$pull", new Document("players", new Document("name", displayName)));

        collection.updateOne(query, update);
    }
}
