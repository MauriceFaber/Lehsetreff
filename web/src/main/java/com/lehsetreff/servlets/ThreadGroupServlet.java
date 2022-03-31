package com.lehsetreff.servlets;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.ThreadGroup;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.lehsetreff.Extensions;

public class ThreadGroupServlet extends HttpServlet{

    private Database db = Database.getInstance();


	/**
	 * Erstelle Thread Gruppe, falls Person
	 * authentifiziert und Moderator ist.
	 */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		if(!Extensions.isModerator(request, response)){
			return;
		}

		int userId = db.getUserController().getUserId(request);
		String caption = request.getParameter("caption");
		caption = URLDecoder.decode(caption, "UTF-8");
		String description = request.getParameter("description");
		description = URLDecoder.decode(description, "UTF-8");

		if(caption == null || caption.length() == 0){
			response.sendError(401, "caption null or empty");
			return;
		}

		if(description == null || description.length() == 0){
			response.sendError(401, "caption null or empty");
			return;
		}
        
		ThreadGroup tGroup = db.getThreadGroupController().addThreadGroup(caption, userId, description);
		if(tGroup != null){		
            Extensions.sendJsonResponse(response, tGroup);
        } else {
            response.sendError(400, "ThreadGroup anlegen failed");
        }
	}

	/**
	 * Lade Thread Gruppen.
	 */

    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		String idString = request.getParameter("id");
		String groupName = request.getParameter("groupName");
		groupName = URLDecoder.decode(groupName, "UTF-8");
		int id = -1;
		if(idString != null){
			id = Integer.parseInt(idString);
		}else if(groupName != null){
			id = db.getThreadGroupController().getThreadGroup(groupName).getId();
		}
		if(id > -1){
			ThreadGroup group = db.getThreadGroupController().getThreadGroup(id);
			if(group!=null){
				Extensions.sendJsonResponse(response, group);
			}else{
				response.sendError(404);
			}
			return;
		}
		List<ThreadGroup> threadGroups = db.getThreadGroupController().getThreadGroups();
		if(threadGroups != null){		
            Extensions.sendJsonResponse(response, threadGroups);
        } else {
            response.sendError(400, "ThreadGroups erhalten failed");
        }
	}

	/**
	 * Bearbeitet die Thread Gruppe, falls Person
	 * authentifiziert und Moderator ist.
	 */
    @Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String caption = Extensions.getParameterFromMap(request, "caption");
		caption = URLDecoder.decode(caption, "UTF-8");
		String idString = Extensions.getParameterFromMap(request, "id");
        int threadGroupId = Integer.parseInt(idString);
		String description = Extensions.getParameterFromMap(request, "description");
		description = URLDecoder.decode(description, "UTF-8");
        
		if(!Extensions.isModerator(request, response)){
			return;
		}

		if (caption != null) {
			ThreadGroup tGroup = db.getThreadGroupController().renameThreadGroup(threadGroupId, caption);
			
			if(description == null || tGroup == null){
				if(tGroup != null){		
					Extensions.sendJsonResponse(response, tGroup);
				} else {
					response.sendError(400, "ThreadGroup Rename failed");
					return;
				}
			}
		}

		if (description != null) {
			ThreadGroup tGroup = db.getThreadGroupController().changeThreadGroupDescription(threadGroupId, description);
		
			if(tGroup != null){		
				Extensions.sendJsonResponse(response, tGroup);
			} else {
				response.sendError(400, "Change ThreadGroup Description failed");
			}
		}
		
	}

	/**
	 * Loesche Thread Gruppe, falls Person 
	 * authentifiziert und Moderator ist.
	 */
    @Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
		if(!Extensions.isAuthenticated(request, response)){
			Extensions.removeHashmap(request);
			return;
		}

		int threadGroupId = Integer.parseInt(Extensions.getParameterFromMap(request, "id"));
		if(!Extensions.isModerator(request, response)){
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
