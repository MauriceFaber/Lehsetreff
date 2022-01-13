package com.meshenger;

import java.io.IOException;
import java.util.List;
import com.meshenger.controllers.Database;
import com.meshenger.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class ContactsServlet extends HttpServlet {
	private Database db = Database.getInstance();

	/**
	 * Fuegt Benutzer einen Kontakt hinzu.
	 * Prueft ob Benutzer authentifiert ist.
	 * Prueft ob Benutzerinfos legitim sind und fuegt Kontakt hinzu.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String name = (String) request.getParameter("contactName");
		String contact = (String) request.getParameter("contactId");

		boolean valid = name != null && !name.isEmpty();
		if(valid){
			try{
				int contactId = Integer.parseInt(contact);
				User u = db.getContactsController().addContact(name, contactId, request);
				if(u != null){
					Extensions.sendJsonResponse(response, u);
					return;
				}else {
					response.sendError(404);
					return;
				}
			}catch (Exception e) {
			}
		}
		response.sendError(400);
	}

	/**
	 * Laedt die Kontakte eines Benutzers.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		List<User> users = db.getContactsController().getContacts(request);
        Extensions.sendJsonResponse(response, users);
	}

	/**
	 * Loescht Kontakt eines Benutzers.
	 * Wenn Kontakt vorhanden -> loeschen.
	 */
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

        String contact = Extensions.getParameterFromMap(request, "contactId");

		try{
			int contactId = Integer.parseInt(contact);
			boolean result = db.getContactsController().deleteContact(contactId, request);
			if(result){
				response.setStatus(200);
				Extensions.removeHashmap(request);
				return;
			}else {
				response.sendError(404);
				Extensions.removeHashmap(request);
				return;
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
			Extensions.removeHashmap(request);
			response.sendError(400);
    }
}