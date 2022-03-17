package com.lehsetreff.controllers;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


import com.lehsetreff.models.Thread;



public class ThreadController {
    
    private Database db = Database.getInstance();

    public Thread addThread(String caption,int userId, int ownerId, int groupId, String description){
        Thread thread = new Thread();
        thread.setCaption(caption);
        thread.setOwnerId(ownerId);
        thread.setGroupId(groupId);
		thread.setDescription(description);

        try{
            PreparedStatement st = db.createStatement("insert into threads (caption, ownerId, groupId, threadDescription) values(?,?,?,?)", true);
            st.setString(1, thread.getCaption());
            st.setInt(2, thread.getOwnerId());
            st.setInt(3, thread.getGroupId());
			st.setString(4, thread.getDescription());

            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
                thread = getThread(rs.getInt("ID"));
                OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
				Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
				thread.setLatestMessage(timestamp);
				//setLatestUserMessage(thread.getThreadId(), userId);
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
                thread.setGroupId(result.getInt("groupId"));
                thread.setOwnerId(result.getInt("ownerID"));
				thread.setDescription(result.getString("threadDescription"));
                thread.setThreadId(threadId);

				thread.setLatestMessage(result.getTimestamp("latestMessage"));
				//Timestamp latestUserMessage = getLatestUserMessage(userId, thread);
				//thread.setLatestUserMessage(latestUserMessage);
			}
		} catch(Exception e){
			thread = null;
		}

        return thread;
    }

	/**
	 * Setzt die neuste Nachricht.
	 * @param userId
	 * Die id des Benutzers.
	 * @param threadId
	 * Die id des Threads
	 * @param timestamp
	 * aktuelle Zeit
	 * @return
	 * Thread Objekt
	 */
    public Thread setLatestMessage(int userId, int threadId, java.sql.Timestamp timestamp) {
		Thread thread = getThread(threadId);
		try {
			PreparedStatement st = db.createStatement("update threads set latestMessage = ? where ID = ?", true);
			st.setTimestamp(1, timestamp);
			st.setInt(2, threadId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				thread.setLatestMessage(rs.getTimestamp("latestMessage"));
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return thread;
	}

	/**
	 * Gibt eine Array-Liste mit Threads innerhalb einer Thread Gruppe zurueck. 
	 * @param threadGroupId
	 *  Die id der Thread Gruppe.
	 * @return
	 * Array-Liste mit Threads in einer Thread Gruppe.
	 */
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
				thread.setGroupId(threadGroupId);
				thread.setOwnerId(rs.getInt("ownerID"));
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

    // public void setLatestUserMessage(int threadId, int userId) {
	// 	try {
	// 		PreparedStatement st = db.createStatement("update thread_users set latestMessage = ? where userID = ? and ID = ?", false);
	// 		OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
	// 		Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
	// 		st.setTimestamp(1, timestamp);
	// 		st.setInt(2, userId);
	// 		st.setInt(3, threadId);
	// 		st.executeUpdate();
	// 	} catch(Exception e){
	// 		System.out.println(e.getMessage());
	// 	}
	// }

    // private Timestamp getLatestUserMessage(int userId, Thread thread){
	// 	try {
	// 		PreparedStatement st = db.createStatement("select * from thread_users where ID = ? and userID = ?", false);
	// 		st.setInt(1, thread.getThreadId());
	// 		st.setInt(2, userId);

	// 		ResultSet rs = db.executeQuery(st);

	// 		if(rs.next()){
	// 			return rs.getTimestamp("latestMessage");
	// 		}
	// 	} catch(Exception e){
	// 	}
	// 	return thread.getlatestMessage();
	// }
}
