package com.meshenger;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meshenger.controllers.Database;
import com.meshenger.models.Chatroom;
import com.meshenger.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class MembersServlet extends HttpServlet {
	private Database db = Database.getInstance();

	/**
	 * Extrahiert Chatroom Id aus dem Request.
	 * Prueft ob Benutzer Mitglied im Chatroom ist.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}
        
		int chatroomId= Integer.parseInt((String)request.getParameter("chatroomId"));
		boolean withAvatars= Boolean.parseBoolean((String)request.getParameter("withAvatars"));

		if(db.getChatroomsController().isChatroomMember(chatroomId, request)){
			List<User> members = db.getChatroomsController().getChatroomMembers(chatroomId, withAvatars);
			Extensions.sendJsonResponse(response, members);
			return;
		}else{
			response.sendError(404);
		}
    }

	/**
	 * Erstellt einen Chatroom.
	 * Entnimmt dem Request die UserDetails.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String action = request.getParameter("action");
		int chatroomId = Integer.parseInt(request.getParameter("chatroomId"));
		Chatroom c = null;
		if (action.equals("add")){
			ObjectMapper mapper = new ObjectMapper();

			String json = request.getParameter("ids");
			String[] contacts = mapper.readValue(json, String[].class);
			
			for (String id : contacts) {
				c = db.getChatroomsController().addMember(chatroomId, Integer.parseInt(id), request);
			}
		}else if(action.equals("remove")){
			String contact = request.getParameter("contactId");
			int contactId = Integer.parseInt(contact);

			c = db.getChatroomsController().removeMember(chatroomId, contactId, request);
		}

		if(c != null){		
            Extensions.sendJsonResponse(response, c);
        } else {
            response.sendError(400);
        }
	}
}