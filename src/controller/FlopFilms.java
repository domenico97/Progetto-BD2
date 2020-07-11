package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;

import com.google.gson.Gson;

import model.Movies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Servlet implementation class IncassiFIlm
 */
@WebServlet("/FlopFilms")
public class FlopFilms extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FlopFilms() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String stringMinLoss = request.getParameter("MinLoss");
		Long minLoss = Long.parseLong(stringMinLoss);
		System.out.println("MinLoss: "+ minLoss);
		
			// Richiamo il metodo del model per eseguire la query
			ArrayList<Document> result = Movies.flopFilms(minLoss);
			System.out.println("Dimensione result" + result.size());
			
			/*
			String json = null;
			
			if ((result.size()) != 0) {
				json = "[";
				for (Document x : result) {
					json += "{\"titolo\":" + "\"" + x.get("title") + "\"" + ",\"incasso\":" + "\"" + x.get("worldwide_gross_income") + "\""
							+ "},";
				}
				json = json.substring(0, json.length() - 1);
				json += "]";

			}
*/
			String json = new Gson().toJson(result);
			
			System.out.println(json);
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
