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
import model.Movies2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Servlet implementation class IncassiFIlm
 */
@WebServlet("/FilmPerGenereControl")
public class FilmPerGenereControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Movies prova;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FilmPerGenereControl() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String from = request.getParameter("From");
		String to = request.getParameter("To");
		System.out.println(from + " " + to);
		
			// Richiamo il metodo del model per eseguire la query
			ArrayList<Document> result = prova.getFilmNumberForEachCategory(from, to);
			System.out.println("Dimensione result " + result.size());
			

//			String json = null;
//			if ((result.size()) != 0) {
//				json = "[";
//				for (Document x : result) {
//					json += "{\"genere\":" + "\"" + x.get("genre") + "\"" + ",\"occorrenze\":" + "\"" + x.get("total_qty") + "\""
//							+ "},";
//				}
//				json = json.substring(0, json.length() - 1);
//				json += "]";
//
//			}

			String json = new Gson().toJson(result);
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
