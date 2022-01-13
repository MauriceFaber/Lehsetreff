package com.meshenger;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.meshenger.controllers.Database;
import com.meshenger.models.Message;

public class MessagesServlet extends HttpServlet {
	private Database db = Database.getInstance();

	/**
	 * Nachricht einem Chatroom hinzufuegen.
	 * Prueft ob der Inhalt valide ist.
	 * Pruefung ob User Teil des Chatrooms ist & Inhalt dem Chatroom uebergeben.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}
		
        String content = (String) request.getParameter("content");
        String contentType = (String) request.getParameter("contentType");
        String chatroom = (String) request.getParameter("chatroomId");

        boolean isContentValid = content != null && !content.isEmpty() && contentType != null && !contentType.isEmpty();
        boolean isChatroomValid = chatroom != null && !chatroom.isEmpty();
		
		int contentTypeId = Integer.parseInt(contentType);
		int chatroomId = Integer.parseInt(chatroom);
		try {
			contentTypeId = Integer.parseInt(contentType);
			chatroomId = Integer.parseInt(chatroom);
		} catch(Exception e){
			isContentValid = false;
			isChatroomValid = false;
		}
		int senderId = -1;
		Message m = null;
		String msg = "ne";
        if (isContentValid && isChatroomValid){
			senderId = db.getUserController().getUserId(request); 

			if(db.getChatroomsController().isChatroomMember(chatroomId, senderId)){
				try {
					m = db.getMessagesController().addMessage(content, contentTypeId, chatroomId, senderId);
					if(m != null){
						Extensions.sendJsonResponse(response, m);
						return;
					}
				} catch (Exception e){
					msg = e.getMessage();
				}
			}

        }

    	response.sendError(400, "Invalid Data: content:" + content + ", contentType: " + contentType + ", chatroom:" + chatroomId
		+ "senderId: " + senderId + ", Message:" + msg);
    }

	/**
	 * Alle Nachrichten in einem Chatroom abrufen.
	 * Nachrichtenliste aus Datenbank laden und Antwort hinzufuegen.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String chatroom = (String) request.getParameter("chatroomId");
        boolean isChatroomValid = chatroom != null && !chatroom.isEmpty();

		int chatroomId = 0;
		try {
			chatroomId = Integer.parseInt(chatroom);
		} catch(Exception e){
			isChatroomValid = false;
		}

		if(isChatroomValid && db.getChatroomsController().isChatroomMember(chatroomId, request)){
        	List<Message> messages = db.getMessagesController().getMessages(chatroomId, db.getUserController().getUserId(request));
			Extensions.sendJsonResponse(response, messages);
		} else {
			response.sendError(400);
		}
	}
}