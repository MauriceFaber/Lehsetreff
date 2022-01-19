package com.lehsetreff.controllers;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import com.lehsetreff.models.*;
import com.lehsetreff.models.Thread;


import jakarta.servlet.http.HttpServletRequest;

public class ThreadController {
    
    private Database db = Database.getInstance();

    public Thread addThread(String caption,int userId, int ownerId, int groupId){
        Thread thread = new Thread();
        thread.setCaption(caption);
        thread.setOwnerId(ownerId);
        thread.setGroupId(groupId);

        try{
            PreparedStatement st = db.createStatement("insert into threads (caption, ownerId, groupId) values(?,?,?)", true);
            st.setString(1, thread.getCaption());
            st.setInt(2, thread.getOwnerId());
            st.setInt(3, thread.getGroupId());

            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
                thread = getThread(userId, rs.getInt("ID"));
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

    public Thread getThread(int userId, int threadId){
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

    public Thread setLatestMessage(int userId, int threadId, java.sql.Timestamp timestamp) {
		Thread thread = getThread(userId, threadId);
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
                result.add(thread);
            }
        } catch(Exception e){
			result.clear();
		}

		return result;
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
