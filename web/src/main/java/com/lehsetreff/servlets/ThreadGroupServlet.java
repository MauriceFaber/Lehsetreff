package com.lehsetreff.servlets;


import java.io.IOException;
import java.util.List;

import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.Thread;
import com.lehsetreff.models.ThreadGroup;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.meshenger.Extensions;

public class ThreadGroupServlet extends HttpServlet{

    private Database db = Database.getInstance();



    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		int userId = db.getUserController().getUserId(request);
		String caption = request.getParameter("threadGroupCaption");
        
		ThreadGroup tGroup = db.getThreadGroupController().addThreadGroup(caption, userId);
		if(tGroup != null){		
            Extensions.sendJsonResponse(response, tGroup);
        } else {
            response.sendError(400, "ThreadGroup anlegen failed");
        }
	}


    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		List<ThreadGroup> threadGroups = db.getThreadGroupController().getThreadGroups();
		if(threadGroups != null){		
            Extensions.sendJsonResponse(response, threadGroups);
        } else {
            response.sendError(400, "ThreadGroups erhalten failed");
        }
	}


    @Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String caption = request.getParameter("threadGroupCaption");
        int threadGroupId = Integer.parseInt(request.getParameter("threadGroupID"));
        
		if(!Extensions.hasRights(request, response) || !Extensions.isThreadGroupOwner(request, response, threadGroupId)){
			return;
		}

		ThreadGroup tGroup = db.getThreadGroupController().renameThreadGroup(threadGroupId, caption);
		if(tGroup != null){		
            Extensions.sendJsonResponse(response, tGroup);
        } else {
            response.sendError(400, "ThreadGroup Rename failed");
        }
	}


    @Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

		int threadGroupId = Integer.parseInt(Extensions.getParameterFromMap(request, "threadGroupId"));
		if(!Extensions.hasRights(request, response) || !Extensions.isThreadGroupOwner(request, response, threadGroupId)){
			return;
		}
		try{
			boolean result = db.getThreadGroupController().deleteThreadGroup(threadGroupId);
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
			response.sendError(400, "Delete ThreadGroup failed");
	}
    
}
