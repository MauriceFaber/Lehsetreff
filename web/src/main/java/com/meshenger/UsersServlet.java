package com.meshenger;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.meshenger.controllers.Database;
import com.meshenger.models.User;

public class UsersServlet extends HttpServlet {
	private Database db = Database.getInstance();

	/**
	 * User zur Datenbank hinzufuegen.
	 * Daten aus Request entnehmen und auf Validitaet pruefen.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        String userName = (String) request.getParameter("userName");
        String passphrase = (String) request.getParameter("passphrase");

        boolean isNameValid = userName != null && !userName.isEmpty();
        boolean isPassphraseValid = passphrase != null && !passphrase.isEmpty();

		boolean success = false;
        if (isPassphraseValid && isNameValid){
            User u = db.getUserController().addUser(userName, passphrase);
			if(u != null){
				success = true;
				Extensions.sendJsonResponse(response, u);
			}
        }
        if(!success) {
            response.sendError(400);
        }
	}
     
	/**
	 * Userdaten aus Datenbank entnehmen.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		User u = db.getUserController().getUser(request, true);
		if(u == null){
			response.sendError(400);
		} else {
			Extensions.sendJsonResponse(response, u);
		}
	}

	/**
	 * Avatar ueberpruefen, User aus Datenbank entnehmen und Avatar in Userobjekt und Datenbank setzen.
	 */
    @Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

        String avatar = Extensions.getParameterFromMap(request, "avatar");
		avatar = avatar.replace("%3A", ":");
		avatar = avatar.replace("%3B", ";");
		avatar = avatar.replace("%2C", ",");
		avatar = avatar.replace("%2F", "/");

        boolean isAvatarValid = avatar != null;

        User u = db.getUserController().getUser(request, false); 

        if (isAvatarValid){ 
			u.setAvatar(avatar);
        } 

		u = db.getUserController().updateUser(u.getId(), avatar);
		if (u != null){
        	Extensions.sendJsonResponse(response, u);
			Extensions.removeHashmap(request);
			return;
		}
		Extensions.removeHashmap(request);
		response.sendError(400);
    }   
}