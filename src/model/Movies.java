package model;

import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Aggregates.out;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
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
	private static String x = collection.createIndex(Indexes.hashed("director"));
	private static String y = collection.createIndex(Indexes.ascending("year"));

	// Restituisce una lista di film con i relativi incassi
	public static ArrayList<Document> getWorldWideGrossIncome(String from, String to) {

		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("year", new Document().append("$gte", from).append("$lte", to))),
				new Document().append("$match",
						new Document().append("budget", new BsonRegularExpression(".*\\$.*", "i"))
								.append("worlwide_gross_income", new BsonRegularExpression(".*\\$.*", "i"))),
				new Document().append("$project", new Document().append("title", 1.0)
						.append("worldwide_gross_income",
								new Document().append("$substr", Arrays.asList("$worlwide_gross_income", 2.0, -1.0)))
						.append("budget", new Document().append("$substr", Arrays.asList("$budget", 2.0, -1.0)))
						.append("director", 1.0).append("genre", 1.0).append("year", 1.0)),
				new Document().append("$addFields",
						new Document().append("worldwide_gross_income",
								new Document().append("$convert",
										new Document().append("input", "$worldwide_gross_income").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))),
				new Document().append("$addFields",
						new Document().append("budget",
								new Document().append("$convert",
										new Document().append("input", "$budget").append("to", "double")
												.append("onError", "Error").append("onNull", Decimal128.parse("0"))))),
				new Document().append("$sort", new Document().append("worldwide_gross_income", -1.0)),
				new Document().append("$limit", 500.0));
		ArrayList<Document> result = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return result;
	}

	// Restituice il totale incassi di ciascuna compagnia cinematografica
	public static ArrayList<Document> getCompanyGrossIncome(String from, String to) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("year", new Document().append("$gte", from).append("$lte", to))),
				new Document().append("$match",
						new Document().append("production_company", new Document().append("$ne", ""))),
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
				new Document().append("$project",
						new Document().append("production_company", "$_id").append("_id", 0.0).append("total_sum",
								1.0)),
				new Document().append("$sort", new Document().append("total_sum", -1.0)),
				new Document().append("$limit", 500.0));
		ArrayList<Document> result = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return result;

	}

	// Restituisce il numero di occorrenze (numero di film) per ciascuna categoria
	// in un determinato intervallo di tempo.
	public static ArrayList<Document> getFilmNumberForEachCategory(String from, String to) {
		List<? extends Bson> pipeline = Arrays
				.asList(new Document().append("$match",
						new Document().append("year", new Document().append("$gte", from).append("$lte", to))),
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
						new Document().append("actors", new BsonRegularExpression(".*" + actor + ".*", "i"))),
				new Document().append("$project",
						new Document().append("avg_vote", 1.0).append("title", 1.0).append("votes", 1.0)
								.append("genre", 1.0)
								.append("genres", new Document().append("$split", Arrays.asList("$genre", ", ")))),
				new Document().append("$unwind", "$genres"),
				new Document().append("$group",
						new Document().append("_id", "$genres")
								.append("average", new Document().append("$avg", "$avg_vote"))
								.append("films", new Document().append("$push", "$title"))),
				new Document().append("$project",
						new Document().append("genre", "$_id").append("_id", 0.0).append("average", 1.0).append("films",
								1.0)),

				new Document().append("$sort", new Document().append("genre", 1.0)));
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
				new Document().append("$sort", new Document().append("loss", -1.0)),
				new Document().append("$limit", 31.0));

		ArrayList<Document> list = collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());

		return list;
	}

	public static ArrayList<Document> getFilm(String film) throws UnknownHostException {

		Document query = new Document();
		query.append("title", new BsonRegularExpression(".*" + film + ".*", "i"));

		Document sort = new Document();
		sort.append("avg_vote", -1.0);

		int limit = 31;

		Document projection = new Document();
		projection.append("_id", 0.0);
		projection.append("imdb_title_id", 1.0);
		projection.append("title", 1.0);
		projection.append("year", 1.0);
		projection.append("genre", 1.0);
		projection.append("duration", 1.0);
		projection.append("country", 1.0);
		projection.append("director", 1.0);
		projection.append("avg_vote", 1.0);
		projection.append("description", 1.0);
		projection.append("budget", 1.0);
		projection.append("worlwide_gross_income", 1.0);

		ArrayList<Document> list = collection.find(query).projection(projection).sort(sort).limit(limit)
				.into(new ArrayList<>());

		return list;
	}

	public static ArrayList<Document> getIncomeByDirector(String director) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match",
						new Document().append("director", director).append("worlwide_gross_income",
								new Document().append("$ne", ""))),
				new Document().append("$project",
						new Document().append("title", 1.0).append("worlwide_gross_income",
								new Document().append("$substr", Arrays.asList("$worlwide_gross_income", 2.0, -1.0)))),
				new Document().append("$addFields",
						new Document().append("worlwide_gross_income",
								new Document().append("$toDouble", "$worlwide_gross_income"))),
				new Document().append("$sort", new Document().append("worlwide_gross_income", -1.0)));

		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
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
						new Document().append("$sort", new Document().append("_id", 1.0)),
						new Document().append("$project", new Document().append("genre", "$_id.genre")
								.append("_id", 0.0).append("avgIncome", 1.0)));

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
						new Document().append("$sort", new Document().append("total_films", -1.0)),
						new Document().append("$project", new Document().append("Nazione", "$_id.Nazione")
								.append("_id", 0.0).append("total_films", 1.0)));

		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
	}

	//Distribuzione di tutti i generi
	public static ArrayList<Document> distributionGenresForYear(String from, String to) {
		List<? extends Bson> pipeline = Arrays.asList(new Document().append("$match", new Document().append("$and",
				Arrays.asList(new Document()
						.append("year", new Document().append("$gte", from)),
						new Document().append("year", new Document().append("$lte", to))))),
				new Document().append("$addFields", new Document().append("qty", 1.0)),
				new Document().append("$project",
						new Document()
								.append("categoria", new Document().append("$split", Arrays.asList("$genre", ", ")))
								.append("year", 1.0).append("qty", 1.0)),
				new Document().append("$unwind", "$categoria"),
				new Document().append("$group",
						new Document()
								.append("_id", new Document().append("anno", "$year").append("genere", "$categoria"))
								.append("total_qty", new Document().append("$sum", "$qty"))),
				new Document().append("$project",
						new Document().append("anno", "$_id.anno").append("genere", "$_id.genere").append("_id", 0.0)
								.append("total_qty", 1.0)),
				new Document().append("$group",
						new Document().append("_id", "$anno").append("film",
								new Document().append("$push",
										new Document().append("genere", "$genere").append("quantita", "$total_qty")))),
				new Document().append("$sort", new Document().append("_id", 1.0)));
		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
	}

	// Distribuzione di un solo genere selezionato dall'utente
	public static ArrayList<Document> genreDistributionForYear(String from, String to, String genre) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document()
						.append("$match",
								new Document().append("$and",
										Arrays.asList(
												new Document().append("year", new Document().append("$gte", from)),
												new Document().append("year", new Document().append("$lte", to)),
												new Document().append("genre", new BsonRegularExpression(".*"+genre+".*",
														"i"))))),
				new Document().append("$addFields", new Document().append("qty", 1.0)),
				new Document().append("$project",
						new Document()
								.append("categoria", new Document().append("$split", Arrays.asList("$genre", ", ")))
								.append("year", 1.0).append("qty", 1.0)),
				new Document().append("$unwind", "$categoria"),
				new Document().append("$group",
						new Document()
								.append("_id", new Document().append("anno", "$year").append("genere", "$categoria"))
								.append("total_qty", new Document().append("$sum", "$qty"))),
				new Document().append("$project",
						new Document().append("anno", "$_id.anno").append("genere", "$_id.genere").append("_id", 0.0)
								.append("total_qty", 1.0)),
				new Document().append("$group", new Document().append("_id", "$anno").append("film",
						new Document().append("$push",
								new Document().append("genere", "$genere").append("quantita", "$total_qty")))),
				new Document().append("$sort", new Document().append("_id", 1.0)));
		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
	}

}
