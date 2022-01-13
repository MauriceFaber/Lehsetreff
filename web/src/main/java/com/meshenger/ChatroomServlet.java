package com.meshenger;

import java.io.IOException;
import java.util.List;

import com.meshenger.controllers.Database;
import com.meshenger.models.Chatroom;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class ChatroomServlet extends HttpServlet {

	private Database db = Database.getInstance();

	/**
	 * Erstellt Chatroom.
	 * Prueft ob Benutzer authentifiziert ist.
	 * Fuegt dem User entsprechenden Chatroom hinzu.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		int userId = db.getUserController().getUserId(request);
		String name = request.getParameter("chatName");
		String avatar = request.getParameter("chatAvatar");
		int chatroomType = Integer.parseInt(request.getParameter("chatroomType"));

		Chatroom c = db.getChatroomsController().addChatroom(name, avatar, userId, chatroomType);
		if(c != null){		
            Extensions.sendJsonResponse(response, c);
        } else {
            response.sendError(400);
        }
	}

	/**
	 * Liefert dem Benutzer den Chat/Gruppenchat zurueck.
	 * Extrahiert Chatroom Id aus dem Request.
	 * Prueft ob Benutzer Mitglied im Chatroom ist.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}
        
		String chatroom = (String) request.getParameter("chatroomId");
		boolean singleRoom = false;
		int chatroomId = 0;
		try{
			chatroomId = Integer.parseInt(chatroom);
			singleRoom = true;
		}catch(Exception e){
			singleRoom = false;
		}

		if(singleRoom){
			if(db.getChatroomsController().isChatroomMember(chatroomId, request)){
				Chatroom c = db.getChatroomsController().getChatroom(db.getUserController().getUserId(request), chatroomId, false);
				Extensions.sendJsonResponse(response, c);
				return;
			}else{
				response.sendError(404);
			}
		}else {
			List<Chatroom> rooms = db.getChatroomsController().getChatrooms(request);
			Extensions.sendJsonResponse(response, rooms);
		}
    }
	/**
	 * Avatar aus Request extrahieren.
	 * Avatar im Chatroomobjekt setzen.
	 */
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

		int userId = db.getUserController().getUserId(request);
		String avatar = Extensions.getParameterFromMap(request, "avatar");
		String name = Extensions.getParameterFromMap(request, "name");
		String chatroom = Extensions.getParameterFromMap(request,"chatroomId");
		int chatroomId = 0;

		boolean isNameValid = name != null;
		
		boolean isAvatarValid = avatar != null;
		boolean isChatroomValid = false;

		try{
			chatroomId = Integer.parseInt(chatroom);
			isChatroomValid = true;
		}catch(Exception e){
		}
		
		if (isChatroomValid){
			Chatroom c = db.getChatroomsController().getChatroom(userId,chatroomId, true); 
			
			if (isAvatarValid){ 
				avatar = avatar.replace("%3A", ":");
				avatar = avatar.replace("%3B", ";");
				avatar = avatar.replace("%2C", ",");
				avatar = avatar.replace("%2F", "/");
				c.setAvatar(avatar);
				c = db.getChatroomsController().updateChatroomAvatar(userId,c.getId(), avatar);
			} 

			if (isNameValid){
				c.setName(name);
				c = db.getChatroomsController().updateChatroomName(userId,c.getId(), name);
			}

			if (c != null){
				Extensions.sendJsonResponse(response, c);
				Extensions.removeHashmap(request);
				return;
			}
		}
		Extensions.removeHashmap(request);
		response.sendError(400);
	}

	/**
	 * Verlaesst einen Chatroom.
	 * Kontrolle ob richtiger Chatroom.
	 */
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

        String chatroom = Extensions.getParameterFromMap(request, "chatroomId");
		int chatroomId = 0;
		boolean isChatroomValid = false;
		try{
			chatroomId = Integer.parseInt(chatroom);
			isChatroomValid = true;
		}catch(Exception e){
		}

        if (isChatroomValid){
			boolean result = db.getChatroomsController().deleteChatroom(chatroomId);
			if(result){
            	Extensions.sendJsonResponse(response, new String("200 ok"));
				Extensions.removeHashmap(request);
			return;
			}
		}
		Extensions.removeHashmap(request);
		response.sendError(400);
    }
}