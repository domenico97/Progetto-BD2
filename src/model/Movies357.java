package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

public class Movies357 {
	public static ArrayList<Document> getIncomeByDirector(String director) {
		List<? extends Bson> pipeline = Arrays.asList(
                new Document()
                        .append("$match", new Document()
                                .append("director", director)
                                .append("worlwide_gross_income", new Document()
                                        .append("$ne", "")
                                )
                        ), 
                new Document()
                        .append("$project", new Document()
                                .append("title", 1.0)
                                .append("worlwide_gross_income", new Document()
                                        .append("$substr", Arrays.asList(
                                                "$worlwide_gross_income",
                                                2.0,
                                                -1.0
                                            )
                                        )
                                )
                        ), 
                new Document()
                        .append("$addFields", new Document()
                                .append("worlwide_gross_income", new Document()
                                        .append("$toInt", "$worlwide_gross_income")
                                )
                        ), 
                new Document()
                        .append("$sort", new Document()
                                .append("worlwide_gross_income", -1.0)
                        )
        );
        
       return collection.aggregate(pipeline)
                .allowDiskUse(false).into(new ArrayList<>());
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
						new Document().append("$project",
						new Document().append("genre", "$_id.genre").append("_id", 0.0).append("avgIncome", 1.0)));

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
						new Document().append("$project",
								new Document().append("Nazione", "$_id.Nazione").append("_id", 0.0).append("total_films", 1.0)));

		return collection.aggregate(pipeline).allowDiskUse(false).into(new ArrayList<>());
	}
}
