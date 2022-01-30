package com.lehsetreff.controllers;

import com.lehsetreff.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRolesController {
    private Database db = Database.getInstance();

	private boolean isRoleSet(int userId){
		boolean result = false;
        try {
            PreparedStatement st = db.createStatement("select * from userRoles where userID = ?", false);
            st.setInt(1, userId);
            
            ResultSet rs = db.executeQuery(st);
            if (rs.next()){
                result = true;
            }
        } catch (Exception e) {
            result = false;
            System.out.println(e);
        }
        return result;
    }

    public boolean setUserRole(int roleId, int userId){
		boolean result = false;
        try {
			boolean isRoleSet = isRoleSet(userId);
			if(!isRoleSet){
            	PreparedStatement st = db.createStatement("insert into userRoles (roleID, userID) values (?, ?)", false);
            	st.setInt(1, roleId);
            	st.setInt(2, userId);
	
            	ResultSet rs = db.executeQuery(st);
            	if (rs.next()){
            	    result = true;
            	}
			}else{
            	PreparedStatement st = db.createStatement("update userRoles set roleID = ? where userID =?", false);
            	st.setInt(1, roleId);
            	st.setInt(2, userId);
	
            	ResultSet rs = db.executeQuery(st);
            	if (rs.next()){
            	    result = true;
            	}
			}
        } catch (Exception e) {
            result = false;
            System.out.println(e);
        }
        return result;
    }

    public UserRole getUserRole(int userId){
        UserRole role = UserRole.Guest;
        try {
            PreparedStatement st = db.createStatement("select * from userRoles where userID = ?", false);
            st.setInt(1, userId);
            ResultSet rs = db.executeQuery(st);
            if(rs.next()){
                int roleId = rs.getInt("roleID");
				role = UserRole.values()[roleId]; 
            }else if(userId > -1){
				role = UserRole.User;
			}
        } catch (Exception e) {
            System.out.println(e);
        }
        return role;
    }
}
