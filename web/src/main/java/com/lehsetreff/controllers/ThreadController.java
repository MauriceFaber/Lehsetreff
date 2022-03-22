package com.lehsetreff.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


import com.lehsetreff.models.Thread;

public class ThreadController {
    
    private Database db = Database.getInstance();

    public Thread addThread(String caption,int userId, int ownerId, int groupId, String description){
        Thread thread = new Thread();
        thread.setCaption(caption);
		
		thread.setOwner(db.getUserController().getUser(ownerId, false));
		thread.setGroup(db.getThreadGroupController().getThreadGroup(groupId));
		thread.setDescription(description);

        try{
            PreparedStatement st = db.createStatement("insert into threads (caption, ownerId, groupId, threadDescription) values(?,?,?,?)", true);
            st.setString(1, thread.getCaption());
            st.setInt(2, thread.getOwner().getId());
            st.setInt(3, thread.getGroup().getId());
			st.setString(4, thread.getDescription());

            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
                thread = getThread(rs.getInt("ID"));
            }
        }catch(Exception e){
                System.out.println(e.getMessage());
        }

        return thread;
    }

	/**
	 * Thread löschen
	 * @param threadId
	 * Die id des Threads.
	 * @return
	 * true bei Erfolg, false bei Misserfolg
	 */
    public boolean deleteThread(int threadId){
		try {
			PreparedStatement st = db.createStatement("delete from threads where ID = ?", true);
			st.setInt(1, threadId);
			st.executeUpdate();

			return true;

		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return false;
	}

	/**
	 * Gibt ein Thread Objekt zurück.
	 * @param threadId
	 * Die id des Threads.
	 * @return
	 * Thread Objekt
	 */
    public Thread getThread(int threadId){
        Thread thread = null;
        try {
			PreparedStatement st = db.createStatement("select * from threads where ID = ?", false);
			st.setInt(1, threadId);

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				thread = new Thread();
				thread.setCaption(result.getString("caption"));
				int groupId = result.getInt("groupId");
				int ownerId = result.getInt("ownerID");
				thread.setOwner(db.getUserController().getUser(ownerId, false));
				thread.setGroup(db.getThreadGroupController().getThreadGroup(groupId));
				thread.setDescription(result.getString("threadDescription"));
                thread.setThreadId(threadId);
			}
		} catch(Exception e){
			thread = null;
		}

        return thread;
    }

	public List<Thread> getThreadsFromThreadGroup(int threadGroupId){
		List<Thread> result = new ArrayList<Thread>();
		
		try {
			PreparedStatement st = db.createStatement("select * from threads where groupID = ?", false);
			st.setInt(1, threadGroupId);

			ResultSet rs = db.executeQuery(st);
            while(rs.next()){
                Thread thread = new Thread();
                thread.setCaption(rs.getString("caption"));
                thread.setThreadId(rs.getInt("ID"));
				int groupId = rs.getInt("threadID");
				int ownerId = rs.getInt("ownerID");
				thread.setGroup(db.getThreadGroupController().getThreadGroup(groupId));
				thread.setOwner(db.getUserController().getUser(ownerId, false));
				thread.setDescription(rs.getString("threadDescription"));
                result.add(thread);
            }
        } catch(Exception e){
			result.clear();
		}

		return result;
	}

	/**
	 * Aendert den Titel eines Threads.
	 * @param threadId
	 * Die id des Threads.
	 * @param userId
	 * Die id des Benutzers
	 * @param caption
	 * Der Titel des Threads
	 * @return
	 * Thread Objekt
	 */
	public Thread renameThread(int threadId, int userId, String caption){
        Thread thread = getThread(threadId);
        try {
			PreparedStatement st = db.createStatement("update threads set caption = ? where ID = ?", true);
			st.setString(1, caption);
			st.setInt(2, threadId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				thread.setCaption(rs.getString("caption"));
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
        return thread;

    }

	/**
	 * Aendert die Beschreibung eines Threads.
	 * @param threadId
	 * Die id des Threads.
	 * @param userId
	 * Die id des Benutzers.
	 * @param description
	 * Die Beschreibung des Threads.
	 * @return
	 * Thread Objekt
	 */
	public Thread changeThreadDescription(int threadId, int userId, String description){
        Thread thread = getThread(threadId);
        try {
			PreparedStatement st = db.createStatement("update threads set threadDescription = ? where ID = ?", true);
			st.setString(1, description);
			st.setInt(2, threadId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				thread.setCaption(rs.getString("threadDescription"));
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
        return thread;

    }
}
