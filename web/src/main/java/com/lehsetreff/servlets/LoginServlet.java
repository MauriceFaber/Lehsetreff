package com.lehsetreff.servlets;

import java.io.IOException;

import com.lehsetreff.Extensions;
import com.lehsetreff.controllers.Database;
import com.meshenger.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class LoginServlet extends HttpServlet {
	private Database db = Database.getInstance();

	/**
	 * Authentifiziert den User (Login).
	 * Extrahiert die Benutzerinfos aus dem Request.
	 * Eingegebene Daten kontrollieren.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  

		String userName = (String) request.getParameter("userName");
        String passphrase = (String) request.getParameter("passphrase");
        String apiKey = (String) request.getParameter("apiKey");

		boolean apiKeyProvided = apiKey != null && !apiKey.isEmpty();

		if(apiKeyProvided){
			User u = db.getUserController().login(apiKey);
			if(u != null){
				Extensions.sendJsonResponse(response, u);
				return;
			}else {
				response.sendError(400, "invalid key: " + apiKey);
				return;
			}
		}else{
        	boolean isNameValid = userName != null && !userName.isEmpty();
        	boolean isPassphraseValid = passphrase != null && !passphrase.isEmpty();
        	if (isPassphraseValid && isNameValid){
				if(db.getUserController().getUser(userName) == null){ 
					response.sendError(404, "user not found: " + userName);
					return;
				}
				User u = db.getUserController().login(userName, passphrase);
				if(u != null){
					Extensions.sendJsonResponse(response, u);
					return;
				}else {
					response.sendError(400, "invalid credentials: " + userName + ", " + passphrase);
					return;
				}
			}
		}
        response.sendError(400, "invalid parameters: " + userName + ", " + passphrase);
	}
}