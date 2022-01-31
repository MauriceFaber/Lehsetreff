package com.lehsetreff.servlets;

import java.io.IOException;
import java.util.List;

import com.lehsetreff.controllers.Database;
import com.lehsetreff.models.Thread;
import com.lehsetreff.models.UserRole;

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

		int userId = db.getUserController().getUserId(request);
        int roleId = Integer.parseInt(request.getParameter("roleID"));

		boolean result = db.getRolesController().setUserRole(roleId, userId);
		if(result){		
            response.sendError(200);
        } else {
            response.sendError(400, "Set Role failed");
        }
	}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = db.getUserController().getUserId(request);

		UserRole role = db.getRolesController().getUserRole(userId);
        if(role != null){		
            Extensions.sendJsonResponse(response, role);
        } else {
            response.sendError(400, "Get Role failed");
        }
    }
    
}
