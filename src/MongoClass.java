
import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Aggregates.out;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;



public class MongoClass {
	
	public static void main(String[] args) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(
			    new MongoClientURI(
			        "mongodb://localhost:27017/"
			    )
			);
		System.out.println("Connessione stabilita");
		MongoDatabase database = mongoClient.getDatabase("BD2");
		MongoCollection<Document> collection = database.getCollection("Movies");
		Document query = new Document();
        query.append("avg_vote", new Document()
                .append("$gt", "6")
        );
       
        Consumer<Document> processBlock = new Consumer<Document>() {
            @Override
            public void accept(Document document) {
                System.out.println(document);
            }
        };
       
        collection.find(query).forEach(processBlock);
	}
}

