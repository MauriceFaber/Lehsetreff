package com.lehsetreff.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


import com.lehsetreff.models.Thread;
import com.lehsetreff.models.ThreadGroup;

/**
 * Stellt den ThreadController dar.
 */
public class ThreadController {
    
    private Database db = Database.getInstance();

	/**
	 * Fuegt einen Thread hinzu.
	 * @param caption
	 * Die Ueberschrift des Threads
	 * @param ownerId
	 * Die Besitzer ID des Threads.
	 * @param groupId
	 * Die Gruppen ID des Threads.
	 * @param description
	 * Die Beschreibung des Threads.
	 * @return
	 * Thread Objekt
	 */
    public Thread addThread(String caption, int ownerId, int groupId, String description){
        Thread thread = new Thread();
		caption = caption.trim();
		description = description.trim();

		if(caption.length() == 0 || description.length() == 0){
			return null;
		}
		
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

	/**
	 * Gibt ein Thread Objekt zurück.
	 * @param threadName
	 * Die id des Threads.
	 * @return der thread
	 * Thread Objekt
	 */
    public Thread getThread(String groupName, String threadName){
		groupName = groupName.toLowerCase().trim();
		threadName = threadName.toLowerCase().trim();
        Thread thread = null;
        try {

			ThreadGroup group = db.getThreadGroupController().getThreadGroup(groupName);

			PreparedStatement st = db.createStatement("select * from threads where LOWER(caption) = ? and groupID = ?", false);
			st.setString(1, threadName);
			st.setInt(2, group.getId());

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				int id = result.getInt("ID");
				thread = getThread(id);
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
				int groupId = rs.getInt("groupID");
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
	 * @param caption
	 * Der Titel des Threads
	 * @return
	 * Thread Objekt
	 */
	public Thread renameThread(int threadId, String caption){
		caption = caption.trim();
		if(caption.length() == 0){
			return null;
		}
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
	 * @param description
	 * Die Beschreibung des Threads.
	 * @return
	 * Thread Objekt
	 */
	public Thread changeThreadDescription(int threadId, String description){
		description = description.trim();

		if(description.length() == 0){
			return null;
		}

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
