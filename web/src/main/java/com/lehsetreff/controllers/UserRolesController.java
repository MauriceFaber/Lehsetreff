package com.lehsetreff.controllers;

import com.lehsetreff.models.*;
import com.meshenger.models.User;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRolesController {
    private Database db = Database.getInstance();


    public UserRole setUserRole(int roleId, int userId){
        UserRole role = null;
        try {
            PreparedStatement st = db.createStatement("update userRoles set roleID = ? where userID =?", false);
            st.setInt(1, roleId);
            st.setInt(2, userId);
            

            ResultSet rs = db.executeQuery(st);
            if (rs.next()){
                switch (roleId) {
                    case 0:
                        role = UserRole.Guest;
                        break;
                    case 1:
                        role = UserRole.User;
                        break;
                    case 2:
                        role = UserRole.Mod;
                        break;
                    case 3:
                        role = UserRole.Admin;
                        break;                
                    default:
                        role = null;
                        break;
                }
            }
        } catch (Exception e) {
            role = null;
            System.out.println(e);
        }
        

    
        return role;
    }

    public UserRole getUserRole(int userId){
        UserRole role = null;

        try {
            PreparedStatement st = db.createStatement("select * from userRoles where userID = ?", false);
            st.setInt(1, userId);

            ResultSet rs = db.executeQuery(st);

            if(rs.next()){
                int roleId = rs.getInt("roleID");
                switch (roleId) {
                    case 0:
                        role = UserRole.Guest;
                        break;
                    case 1:
                        role = UserRole.User;
                        break;
                    case 2:
                        role = UserRole.Mod;
                        break;
                    case 3:
                        role = UserRole.Admin;
                        break;                
                    default:
                        role = null;
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return role;
    }
    
}
