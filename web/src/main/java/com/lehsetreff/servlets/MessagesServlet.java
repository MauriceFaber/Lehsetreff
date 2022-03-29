package com.lehsetreff.servlets;

import java.io.IOException;
import java.util.List;

import com.lehsetreff.Extensions;
import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.Message;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class MessagesServlet extends HttpServlet {

    Database db = Database.getInstance();
/**
 * Erstelle Nachricht, falls Person authentifiziert und Benutzer ist.
 */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		if(!Extensions.isUser(request, response)){
			return;
		}

		int userId = db.getUserController().getUserId(request);
		String contentTypeString = request.getParameter("contentType");
		if(contentTypeString == null){
			contentTypeString = Extensions.getParameterFromMap(request, "contentType");
		}
		String content = request.getParameter("content");
		if(content == null){
			content = Extensions.getParameterFromMap(request, "content");
		}
        int contentType = Integer.parseInt(contentTypeString);
		String threadIdString = request.getParameter("threadId");
		if(threadIdString == null){
			threadIdString = Extensions.getParameterFromMap(request, "threadId");
		}
        int threadId = Integer.parseInt(threadIdString);
		
		String additional = request.getParameter("additional");
       
        try {
            Message m = db.getMessagesController().addMessage(content, contentType, threadId, userId, additional);
            if(m != null){		
                Extensions.sendJsonResponse(response, m);
            } else {
                response.sendError(400);
            }
        } catch (Exception e) {
            response.sendError(400,"add Message failed");
        }
	}

	/**
	 * Lade Nachrichten des Threads.
	 */
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		String parameter = request.getParameter("threadId");
		String messageIdString = request.getParameter("messageId");
        int threadId = -1;
		if(parameter != null){
			threadId = Integer.parseInt(parameter);
		}else if(messageIdString != null){
			int messageId = Integer.parseInt(messageIdString);
			Message m = db.getMessagesController().getMessage(messageId);
			if(m != null){		
				Extensions.sendJsonResponse(response, m);
				return;
			} else {
				response.sendError(400, "get Message failed");
				return;
			}
		}else{
			String name = request.getParameter("threadName");
			String groupName = request.getParameter("groupName");
			threadId = db.getThreadController().getThread(groupName, name).getThreadId();
		}
		List<Message> messages = db.getMessagesController().getMessages(threadId);
		if(messages != null){		
            Extensions.sendJsonResponse(response, messages);
        } else {
            response.sendError(400, "get Messages failed");
        }
	}

	/**
	 * Bearbeitet die Nachricht, falls Person authentifiziert 
	 * und Benutzer ist.
	 */
    @Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

        if(!Extensions.isUser(request, response)){
			return;
		}

		String content = Extensions.getParameterFromMap(request,"content");
        int messageId = Integer.parseInt(Extensions.getParameterFromMap(request, "messageID"));
        int contentType = Integer.parseInt(Extensions.getParameterFromMap(request,"contentType"));
        
		if (!Extensions.isSender(request, response, messageId)){
			return;
		}
		Message m = db.getMessagesController().modifyMessage(content, contentType,messageId, null);
		if(m != null){		
			Extensions.sendJsonResponse(response, m);
		} else {
			response.sendError(400, "Modify Message failed");
		}
	}


	/**
	 * Loesche Nachricht, falls Person
	 * authentifiziert, Benutzer oder Sender ist.
	 */
    @Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

        if(!Extensions.isUser(request, response)){
			return;
		}

		int messageId = Integer.parseInt(Extensions.getParameterFromMap(request, "messageId"));

		if(!Extensions.isModOrSender(request, response, messageId)){
			return;
		}
		try{
			boolean result = db.getMessagesController().deleteMessage(messageId);
			if(result){
				response.setStatus(200);
				Extensions.removeHashmap(request);
				return;
			}else {
				response.sendError(404, "Delete Message failed");
				Extensions.removeHashmap(request);
				return;
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
			Extensions.removeHashmap(request);
			response.sendError(400, "Delete Message 2 failed");
	}
    
}
