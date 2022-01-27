package com.lehsetreff.servlets;

import java.io.IOException;
import java.util.List;

import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.Message;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.meshenger.Extensions;

public class MessagesServlet extends HttpServlet {

    Database db = Database.getInstance();

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		

		int userId = db.getUserController().getUserId(request);
        int contentType = Integer.parseInt(request.getParameter("contentType"));
        int threadId = Integer.parseInt(request.getParameter("threadId"));
		String content = request.getParameter("content");
       
       
        try {
            Message m = db.getMessagesController().addMessage(content, contentType, threadId, userId);
            if(m != null){		
                Extensions.sendJsonResponse(response, m);
            } else {
                response.sendError(400);
            }
        } catch (Exception e) {
            response.sendError(400,"add Message failed");
        }
		
	
	}

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}


        int userId = db.getUserController().getUserId(request);
        int threadId = Integer.parseInt(request.getParameter("threadId"));
		List<Message> messages = db.getMessagesController().getMessages(threadId, userId);
		if(messages != null){		
            Extensions.sendJsonResponse(response, messages);
        } else {
            response.sendError(400, "get Messages failed");
        }
	}


    @Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String content = request.getParameter("content");
        int messageId = Integer.parseInt(request.getParameter("messageID"));
        int contentType = Integer.parseInt(request.getParameter("contentType"));
        
		if (Extensions.isSender(request, response, messageId)){
			Message m = db.getMessagesController().modifyMessage(content, contentType,messageId);
			if(m != null){		
				Extensions.sendJsonResponse(response, m);
			} else {
				response.sendError(400, "Modify Message failed");
			}
		}
	}


    @Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

		int messageId = Integer.parseInt(Extensions.getParameterFromMap(request, "messageId"));

		if(!Extensions.isSender(request, response, messageId)){
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
