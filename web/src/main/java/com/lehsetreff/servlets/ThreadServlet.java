package com.lehsetreff.servlets;

import java.io.IOException;
import java.util.List;

import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.Thread;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.lehsetreff.Extensions;

public class ThreadServlet extends HttpServlet {
    
    private Database db = Database.getInstance();

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		int userId = db.getUserController().getUserId(request);
		String threadCaption = (String) request.getParameter("threadCaption");
        int ownerId = Integer.parseInt(request.getParameter("ownerId"));
        int groupId = Integer.parseInt(request.getParameter("groupId"));
		String threadDescription = (String) request.getParameter("threadDescription");


		Thread thread = db.getThreadController().addThread(threadCaption, userId, ownerId, groupId, threadDescription);
		if(thread != null){		
            Extensions.sendJsonResponse(response, thread);
        } else {
            response.sendError(400, "Add Thread failed");
        }
	}


	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		String threadIdString = request.getParameter("threadGroupID");
			String threadName = request.getParameter("threadName");
			int threadId = -1;
		if(threadIdString != null){
			threadId = Integer.parseInt(threadIdString);
		}else if(threadName != null){
			threadId = db.getThreadController().getThread(threadName).getThreadId();
		}

		if(threadId != -1){
			Thread thread = db.getThreadController().getThread(threadId);
			if(thread != null){		
        	    Extensions.sendJsonResponse(response, thread);
				return;
        	} else {
        	    response.sendError(400, "get Thread failed");
				return;
        	}
		}


		String threadGroupIdString = request.getParameter("threadGroupID");

		int threadGroupId = -1;
		if(threadGroupIdString != null){
			threadGroupId = Integer.parseInt(threadGroupIdString);
		}else {
			String name = request.getParameter("threadGroupName");
			threadGroupId = db.getThreadGroupController().getThreadGroup(name).getId();
		}

		List<Thread> threads = db.getThreadController().getThreadsFromThreadGroup(threadGroupId);
		if(threads != null){		
            Extensions.sendJsonResponse(response, threads);
        } else {
            response.sendError(400, "get Threads from ThreadGroup failed");
        }
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		int threadId =Integer.parseInt(request.getParameter("threadId"));
		int userId =Integer.parseInt(request.getParameter("userId"));
		String caption = (String) request.getParameter("caption");
		String description = (String) request.getParameter("threadDescription");

		if(!Extensions.isModerator(request, response) || !Extensions.isThreadOwner(request, response, threadId)){
			return;
		}
		
		if (caption != null) {
			Thread thread = db.getThreadController().renameThread(threadId, userId, caption);
			
			if(thread != null){		
				Extensions.sendJsonResponse(response, thread);
			} else {
				response.sendError(400, "rename Thread failed");
			}
		}

		if (description != null) {
			Thread thread = db.getThreadController().changeThreadDescription(threadId, userId, description);

			if(thread != null){		
				Extensions.sendJsonResponse(response, thread);
			} else {
				response.sendError(400, "change ThreadDescription failed");
			}
		}
		

	}



	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

		int threadId = Integer.parseInt(Extensions.getParameterFromMap(request, "threadId"));
		if(!Extensions.isThreadOwner(request, response, threadId)){
			Extensions.removeHashmap(request);
			return;
		}
		try{
			boolean result = db.getThreadController().deleteThread(threadId);
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
			response.sendError(400, "Delete Thread failed");
	}
}
