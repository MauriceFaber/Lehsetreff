package com.lehsetreff.servlets;

import java.io.IOException;
import java.util.List;

import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.UserRole;
import com.meshenger.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.lehsetreff.Extensions;

public class UserRoleServlet extends HttpServlet {

    private Database db = Database.getInstance();

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

        if(!Extensions.isAdmin(request, response)){
			return;
		}

		int userId = Integer.parseInt(request.getParameter("userId"));
        int roleId = Integer.parseInt(request.getParameter("roleId"));

		boolean result = db.getRolesController().setUserRole(roleId, userId);
		if(result){		
            response.sendError(200);
        } else {
            response.sendError(400, "Set Role failed");
        }
	}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!Extensions.isAuthenticated(request, response)){
			return;
		}

		String userMode = (String)request.getParameter("userMode");
		String withAvatars = (String)request.getParameter("withAvatars");
		Boolean avatars = withAvatars.compareTo("true") == 0;
		
		if(userMode.compareTo("allUsers") == 0){
			if(!Extensions.isAdmin(request, response)){
				return;
			}

			List<User> users = db.getUserController().getUsers(avatars);
			if(users == null){
				response.sendError(400);
			} else {
				Extensions.sendJsonResponse(response, users);
			}
    	} else {
			String userIdString = (String)request.getParameter("userMode");
			int userId = Integer.parseInt(userIdString);

			User u = db.getUserController().getUser(userId, avatars);

			if(u == null){
				response.sendError(400);
			} else {
				Extensions.sendJsonResponse(response, u);
			}
		}
	}
}
