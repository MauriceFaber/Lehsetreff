package com.lehsetreff.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


import com.lehsetreff.models.ThreadGroup;

public class ThreadGroupController {

    private Database db = Database.getInstance();

    public ThreadGroup addThreadGroup(String caption, int ownerId, String description){
        ThreadGroup tGroup = new ThreadGroup();
        
        tGroup.setCaption(caption);
        tGroup.setOwner(db.getUserController().getUser(ownerId, false));
        tGroup.setDescription(description);
        
        try{
            PreparedStatement st = db.createStatement("insert into threadGroups (caption, ownerID, groupDescription) values (?,?,?)", true);
            st.setString(1, tGroup.getCaption());
            st.setInt(2, tGroup.getOwner().getId());
            st.setString(3, tGroup.getDescription());
           
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

    /**
     * Loeschen einer Thread Gruppe.
     * @param threadGroupId
     * Die id der Thread Gruppe
     * @return
     * true bei Erfolg, false bei Misserfolg.
     */
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

    /**
     * Gibt die Thread Gruppe zurueck.
     * @param threadGroupId
     * Die id der Thread Gruppe.
     * @return
     * Thread Gruppen Objekt
     */
    public ThreadGroup getThreadGroup(int threadGroupId ){
        ThreadGroup tGroup = new ThreadGroup();

        try {
			PreparedStatement st = db.createStatement("select * from threadGroups where ID = ?", false);
			st.setInt(1, threadGroupId);

			ResultSet result = db.executeQuery(st);
            if(result.next()){
                tGroup = new ThreadGroup();
                tGroup.setCaption(result.getString("caption"));
                tGroup.setId(threadGroupId);
				int ownerId = result.getInt("ownerID");
				tGroup.setOwner(db.getUserController().getUser(ownerId, false));
                tGroup.setDescription(result.getString("groupDescription"));
            }
        } catch(Exception e){
			tGroup = null;
		}
        return tGroup;
    }

    /**
     * Gibt eine Lsite mit Thread Gruppen zurueck.
     * @return
     * Liste mit Thread Gruppen Objekten
     */
    public List<ThreadGroup> getThreadGroups(){
        List<ThreadGroup> result = new ArrayList<ThreadGroup>();

        try {
			PreparedStatement st = db.createStatement("select * from threadGroups", false);

			ResultSet rs = db.executeQuery(st);
            while(rs.next()){
                ThreadGroup tGroup = new ThreadGroup();
                tGroup.setCaption(rs.getString("caption"));
                tGroup.setId(rs.getInt("ID"));
				int ownerId = rs.getInt("ownerID");
				tGroup.setOwner(db.getUserController().getUser(ownerId, false));
                tGroup.setDescription(rs.getString("groupDescription"));
                result.add(tGroup);
            }
        } catch(Exception e){
			result.clear();
		}

        return result;
    }

    /**
     * Aendert den Namend er Thread Gruppe.
     * @param threadGroupId
     * Die id der Thread Gruppe.
     * @param caption
     * Der Titel der Thread Gruppe.
     * @return
     * Thread Gruppe Objekt
     */
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

    /**
     * Aendert die Beschreibung der Thread Gruppe.
     * @param threadGroupId
     * Die id der Thread Gruppe.
     * @param description
     * Die Beschreibung der Thread Gruppe
     * @return
     * Thread Gruppe Objekt
     */
    public ThreadGroup changeThreadGroupDescription(int threadGroupId, String description){
        ThreadGroup tGroup = getThreadGroup(threadGroupId);
        try {
			PreparedStatement st = db.createStatement("update threadGroups set groupDescription = ? where ID = ?", true);
			st.setString(1, description);
			st.setInt(2, threadGroupId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				tGroup.setCaption(rs.getString("groupDescription"));
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
        return tGroup;

    }

    
}
