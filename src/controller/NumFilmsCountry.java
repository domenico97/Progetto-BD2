package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import model.Movies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Servlet implementation class IncassiDirector
 */
@WebServlet("/NumFilmsCountry")
public class NumFilmsCountry extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Movies model;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NumFilmsCountry() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		
			// Richiamo il metodo del model per eseguire la query
			ArrayList<Document> result = model.getFilmsForAllCountry();
			System.out.println("Dimensione result" + result.size());
			for(Document x : result)
				System.out.println(x);

			String json = null;
			String nation = "";
			if ((result.size()) != 0) {
				json = "[";
				for (Document x : result) {
					if(x.get("Nazione").toString().length() > 1)
						nation = x.get("Nazione").toString();
					else
						nation = "Altro";
					json += "{\"nazione\":" + "\"" + nation + "\"" + ",\"film_totali\":" + "\"" + x.get("total_films") + "\""
							+ "},";
				}
				json = json.substring(0, json.length() - 1);
				json += "]";

			}

			response.getWriter().print(json);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
