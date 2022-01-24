package com.lehsetreff.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import com.lehsetreff.models.*;
import com.lehsetreff.models.Thread;
import com.lehsetreff.models.ThreadGroup;

public class ThreadGroupController {

    private Database db = Database.getInstance();

    public ThreadGroup addThreadGroup(String caption){
        ThreadGroup tGroup = new ThreadGroup();
        
        tGroup.setCaption(caption);
        
        try{
            PreparedStatement st = db.createStatement("insert into threadGroups (caption) values(?)", true);
            st.setString(1, tGroup.getCaption());
           

            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();

            if(!rs.next()){
              tGroup = null;
            }

        }catch(Exception e){
                System.out.println(e.getMessage());
        }
        
        return tGroup;
    }

    public boolean deleteThreadGroup(int threadGroupId){
        try {
			PreparedStatement st = db.createStatement("delete from threadGroups where ID = ?", true);
			st.setInt(1, threadGroupId);
			st.executeUpdate();

			return true;

		} catch(Exception e){
				System.out.println(e.getMessage());
		}
        return false;
    }

    public ThreadGroup getThreadGroup(int threadGroupId ){
        ThreadGroup tGroup = new ThreadGroup();

        try {
			PreparedStatement st = db.createStatement("select * from threadGroups where ID = ?", false);
			st.setInt(1, threadGroupId);

			ResultSet result = db.executeQuery(st);
            if(result.next()){
                tGroup = new ThreadGroup();
                tGroup.setCaption(result.getString("caption"));
                tGroup.setThreadGroupId(threadGroupId);
            }
        } catch(Exception e){
			tGroup = null;
		}
        return tGroup;
    }

    public List<ThreadGroup> getThreadGroups(){
        List<ThreadGroup> result = new ArrayList<ThreadGroup>();

        try {
			PreparedStatement st = db.createStatement("select * from threadGroups", false);

			ResultSet rs = db.executeQuery(st);
            while(rs.next()){
                ThreadGroup tGroup = new ThreadGroup();
                tGroup.setCaption(rs.getString("caption"));
                tGroup.setThreadGroupId(rs.getInt("ID"));
                result.add(tGroup);
            }
        } catch(Exception e){
			result.clear();
		}

        return result;
    }

    public ThreadGroup renameThreadGroup(int threadGroupId, String caption){
        ThreadGroup tGroup = getThreadGroup(threadGroupId);
        try {
			PreparedStatement st = db.createStatement("update threadGroups set caption = ? where ID = ?", true);
			st.setString(1, caption);
			st.setInt(2, threadGroupId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				tGroup.setCaption(rs.getString("caption"));
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
        return tGroup;

    }

    
}
