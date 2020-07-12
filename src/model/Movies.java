package model;

import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Aggregates.out;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import javafx.util.Pair;

import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

public class Movies {

	private static MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017/"));
	private static MongoDatabase database = mongoClient.getDatabase("BD2");
	private static MongoCollection<Document> collection = database.getCollection("Movies");

	// Restituisce una lista di film con i relativi incassi
	public static ArrayList<Document> getWorldWideGrossIncome(String from, String to) {

		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("year", new Document().append("$gte", from).append("$lte", to))),
				new Document().append("$match",
						new Document().append("budget", new BsonRegularExpression(".*\\$.*", "i"))
								.append("worlwide_gross_income", new BsonRegularExpression(".*\\$.*", "i"))),
				new Document().append("$project",
						new Document().append("title", 1.0).append("worldwide_gross_income",
								new Document().append("$substr", Arrays.asList("$worlwide_gross_income", 2.0, -1.0)))),
				new Document().append("$addFields",
						new Document().append("worldwide_gross_income",
								new Document().append("$convert",
										new Document().append("input", "$worldwide_gross_income").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))),
				new Document().append("$sort", new Document().append("worldwide_gross_income", -1.0)));

		ArrayList<Document> result = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return result;

	}

	// Restituice il totale incassi di ciascuna compagnia cinematografica
	public static ArrayList<Document> getCompanyGrossIncome(String from, String to) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("year", new Document().append("$gte", "1900").append("$lte", "2019"))),
				new Document().append("$match",
						new Document().append("worlwide_gross_income", new BsonRegularExpression(".*\\$.*", "i"))),
				new Document().append("$project",
						new Document().append("production_company", "$production_company").append(
								"worldwide_gross_income",
								new Document().append("$substr", Arrays.asList("$worlwide_gross_income", 2.0, -1.0)))),
				new Document().append("$addFields",
						new Document().append("worldwide_gross_income",
								new Document().append("$convert",
										new Document().append("input", "$worldwide_gross_income").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))),
				new Document().append("$group",
						new Document().append("_id", "$production_company").append("total_sum",
								new Document().append("$sum", "$worldwide_gross_income"))),
				new Document()
						.append("$project",
								new Document().append("production_company", "$_id").append("_id", 0.0)
										.append("total_sum", 1.0)),
				new Document().append("$sort", new Document().append("total_sum", -1.0)));

		ArrayList<Document> result = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return result;

	}

	// Restituisce il numero di occorrenze (numero di film) per ciascuna categoria
	// in un determinato intervallo di tempo.
	public static ArrayList<Document> getFilmNumberForEachCategory(String from, String to) {
		List<? extends Bson> pipeline = Arrays
				.asList(new Document().append("$match",
						new Document().append("year", new Document().append("$gte", "2019").append("$lte", "2019"))),
						new Document().append("$addFields", new Document().append("qty",
								1.0)),
						new Document().append("$project",
								new Document()
										.append("categoria",
												new Document().append("$split", Arrays.asList("$genre", ", ")))
										.append("qty", 1.0)),
						new Document().append("$unwind", "$categoria"),
						new Document().append("$group",
								new Document().append("_id", "$categoria").append("total_qty",
										new Document().append("$sum", "$qty"))),
						new Document().append("$project",
								new Document().append("genre", "$_id").append("_id", 0.0).append("total_qty", 1.0)),
						new Document().append("$sort", new Document().append("total_qty", -1.0)));
		ArrayList<Document> result = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return result;
	}

	public static ArrayList<Document> genreAverageForActor(String actor) throws UnknownHostException {

		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("actors", new BsonRegularExpression(".*Russell Crowe.*", "i"))),
				new Document().append("$project",
						new Document().append("avg_vote", 1.0).append("title", 1.0).append("votes", 1.0)
								.append("genre", 1.0)
								.append("genres", new Document().append("$split", Arrays.asList("$genre", ", ")))),
				new Document().append("$unwind", "$genres"),
				new Document().append("$group",
						new Document().append("_id", "$genres").append("average",
								new Document().append("$avg", "$avg_vote"))),
				new Document().append("$project",
						new Document().append("genre", "$_id").append("_id", 0.0).append("average", 1.0)));

		ArrayList<Document> list = collection.aggregate(pipeline).allowDiskUse(true).into(new ArrayList<>());

		return list;
	}

	public static ArrayList<Document> flopFilms(long threshold) throws UnknownHostException {

		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("budget", new BsonRegularExpression(".*\\$.*", "i"))
								.append("worlwide_gross_income", new BsonRegularExpression(".*\\$.*", "i"))),
				new Document().append("$project", new Document().append("title", 1.0)
						.append("sBudget", new Document().append("$substr", Arrays.asList("$budget", 2.0, -1.0)))
						.append("sIncome",
								new Document().append("$substr", Arrays.asList("$worlwide_gross_income", 2.0, -1.0)))),
				new Document().append("$addFields", new Document()
						.append("convBudget",
								new Document().append("$convert",
										new Document().append("input", "$sBudget").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))
						.append("convIncome",
								new Document().append("$convert",
										new Document().append("input", "$sIncome").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))),
				new Document().append("$project",
						new Document().append("title", 1.0).append("loss",
								new Document().append("$subtract", Arrays.asList("$convBudget", "$convIncome")))),
				new Document().append("$match", new Document().append("loss", new Document().append("$gt", threshold))),
				new Document().append("$sort", new Document().append("loss", -1.0)));

		ArrayList<Document> list = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return list;
	}

	public static ArrayList<Document> getFilm(String film) throws UnknownHostException {

		Document query = new Document();
		query.append("title", new BsonRegularExpression(".*" + film + ".*", "i"));

		Document projection = new Document();
		projection.append("_id", 0.0);

		ArrayList<Document> list = collection.find(query).projection(projection).into(new ArrayList<>());

		return list;
	}

	public static ArrayList<Document> getIncomeByDirector(String director) {
		Bson filter = eq("director", director);
		Bson project = and(eq("title", 1L), eq("worlwide_gross_income", 1L));

		return collection.find(filter).projection(project).into(new ArrayList<>());
	}

	// Query 5
	public static ArrayList<Document> getMedianIncomeByGenre(String dateFrom, String dateTo) {
		List<? extends Bson> pipeline = Arrays
				.asList(new Document().append("$match",
						new Document().append("year", new Document().append("$gte", dateFrom).append("$lte", dateTo))),
						new Document().append("$match",
								new Document().append("worlwide_gross_income",
										new BsonRegularExpression(".*\\$.*", "i"))),
						new Document().append("$project",
								new Document()
										.append("categoria",
												new Document().append("$split", Arrays.asList("$genre", ", ")))
										.append("worldwide_gross_income",
												new Document().append("$substr",
														Arrays.asList("$worlwide_gross_income", 2.0, -1.0)))),
						new Document().append("$unwind", "$categoria"),
						new Document().append("$addFields",
								new Document().append("worldwide_gross_income", new Document().append("$convert",
										new Document().append("input", "$worldwide_gross_income").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))),
						new Document().append("$group",
								new Document().append("_id", new Document().append("genre", "$categoria"))
										.append("avgIncome", new Document().append("$avg", "$worldwide_gross_income"))),
						new Document().append("$sort", new Document().append("_id", 1.0)));

		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
	}

	// Query 7
	public static ArrayList<Document> getFilmsForAllCountry() {
		List<? extends Bson> pipeline = Arrays
				.asList(new Document().append("$match", new Document()),
						new Document().append("$addFields", new Document().append("qty",
								1.0)),
						new Document().append("$project",
								new Document()
										.append("categoria",
												new Document().append("$split", Arrays.asList("$country", ", ")))
										.append("qty", 1.0)),
						new Document().append("$unwind", "$categoria"),
						new Document().append("$group",
								new Document().append("_id", new Document().append("Nazione", "$categoria"))
										.append("total_films", new Document().append("$sum", "$qty"))),
						new Document().append("$sort", new Document().append("_id", 1.0)));

		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
	}

}