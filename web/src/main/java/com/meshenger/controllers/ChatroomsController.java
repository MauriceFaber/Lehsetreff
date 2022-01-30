package com.meshenger.controllers;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import com.meshenger.models.*;

import jakarta.servlet.http.HttpServletRequest;

public class ChatroomsController {
	
	private Database db = Database.getInstance();

	/**
	 * Erstellen der Chatraeume.
	 * @param name
	 * Chatraumname
	 * @param avatar
	 * Chatraumavatar
	 * @param userId
	 * Ersteller des Chatraumes als Mitglied
	 * @param type
	 * Chatraumtyp
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom addChatroom(String name, String avatar, int userId, int type) {
		Chatroom c = new Chatroom();
		c.setName(name);
		c.setAvatar(avatar);
		c.setChatroomType(type);
		try {
			PreparedStatement st = db.createStatement("insert into chatrooms (chatName, chatAvatar, chatroomType) values(?,?,?)", true);
			st.setString(1, c.getName());
			st.setString(2, c.getAvatar());
			st.setInt(3, c.getChatroomType());

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();
		
            if(rs.next()){
				c = getChatroom(userId, rs.getInt(1), false);
				addMember(c, userId);
				OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
				Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
				c.setLatestMessage(timestamp);
				setLatestUserMessage(c.getId(), userId);
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return c;
	}

	/**
	 * Liefert für den aktuellen User die Chatraumliste.
	 * @param request
	 * Http Request
	 * @return 
	 * Chatroomliste
	 */
	public List<Chatroom> getChatrooms(HttpServletRequest request){
		int userId = db.getUserController().getUserId(request);
		return getChatrooms(userId);
	}

	/**
	 * Fuegt einen angegebenen User in den entsprechenden Chatraum hinzu und liefert diesen zurueck.
	 * @param chatroomId
	 * Chatraumnummer
	 * @param userId
	 * Benutzernummer
	 * @param request
	 * Http Request
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom addMember(int chatroomId, int userId, HttpServletRequest request){
		int user = db.getUserController().getUserId(request);
		if(isChatroomMember(chatroomId, user)){
			Chatroom c = getChatroom(userId, chatroomId, true);
			return addMember(c, userId);
		};
		return null;
	}

	/**
	 * Hinzufuegen neuer Mitglieder zu einem Chatraum.
	 * @param c
	 * Chatraum
	 * @param userId
	 * Benutzernummer
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom addMember(Chatroom c, int userId){
		if(c.getChatroomType() == Chatroom.SINGLE_CHAT && c.getMembers().size()==2){
			return null;
		}
		try {
			PreparedStatement st = db.createStatement("insert into chatrooms_users (chatroomID, userID) values(?,?)", true);
			st.setInt(1, c.getId());
			st.setInt(2, userId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				c.setMembers(getChatroomMembers(c.getId(), true)); 
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return c;
	}

	/**
	 * Entfernen eines Chatraumes.
	 * @param chatroomId
	 * Chatraumnummer
	 * @return 
	 * true, wenn erfolgreich geloescht, ansonsten false
	 */
	public boolean deleteChatroom(int chatroomId){
		try {
			PreparedStatement st = db.createStatement("delete from chatrooms where ID = ?", true);
			st.setInt(1, chatroomId);
			st.executeUpdate();

			return true;

		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return false;
	}

	/**
	 * Rueckgabe aller Chatraume eines Benutzers.
	 * @param userId
	 * Benutzernummer
	 * @return
	 * mit dem Benutzer verknuepfte Chatraeume
	 */
	public List<Chatroom> getChatrooms(int userId){
		List<Chatroom> result = new ArrayList<Chatroom>();
		try {
			PreparedStatement st = db.createStatement("select * from chatrooms_users where userID = ?", false);
			st.setInt(1, userId);

			ResultSet rs = db.executeQuery(st);

			while(rs.next()){
				Chatroom c = getChatroom(userId, rs.getInt("chatroomID"), true); 
				result.add(c);
			}
		} catch(Exception e){
			result.clear();
		}
		return result;
	}

	/**
	 * Ueberpruefung, ob ein Benutzer in einem Chatraum ist.
	 * @param chatroomId
	 * Chatraumnummer
	 * @param request
	 * Http Request
	 * @return
	 * true wenn Benutzer Mitglied ist, ansonsten false
	 */
	public boolean isChatroomMember(int chatroomId, HttpServletRequest request){
		int userId = db.getUserController().getUserId(request);
		return isChatroomMember(chatroomId, userId);
	}

	/**
	 * Rueckgabe, ob ein Benutezr Mitglied eines Chatraumes ist. 
	 * @param chatroomId
	 * Chatraumnummer
	 * @param userId
	 * Benutzernummer
	 * @return
	 * true wenn Benutzer Mitglied ist, ansonsten false
	 */
	public boolean isChatroomMember(int chatroomId, int userId){
		boolean result = false;
		try {
			PreparedStatement st = db.createStatement("select * from chatrooms_users where chatroomID = ? and userID = ?", false);
			st.setInt(1, chatroomId);
			st.setInt(2, userId);

			ResultSet rs = db.executeQuery(st);

			if(rs.next()){
				result = true;
			}
		} catch(Exception e){
			result = false;
		}
		return result;
	}

	/**
	 * Ermittlung der neusten Benutzernachricht.
	 * @param userId
	 * Benutzernummer
	 * @param c
	 * Chatraum
	 * @return 
	 * neuste Benutzernachricht zurueckgeben
	 */
	private Timestamp getLatestUserMessage(int userId, Chatroom c){
		try {
			PreparedStatement st = db.createStatement("select * from chatrooms_users where chatroomID = ? and userID = ?", false);
			st.setInt(1, c.getId());
			st.setInt(2, userId);

			ResultSet rs = db.executeQuery(st);

			if(rs.next()){
				return rs.getTimestamp("latestMessage");
			}
		} catch(Exception e){
		}
		return c.getlatestMessage();
	}

	/**
	 * Rueckgabe des Chatraumes anhand der ID.
	 * @param userId
	 * Benutzernummer
	 * @param id
	 * Chatraumnummer
	 * @param withoutAvatarsAndMembers
	 * @return 
	 * Rueckgabe Chatraum
	 */
	public Chatroom getChatroom(int userId, int id, boolean withoutAvatarsAndMembers) {
		Chatroom c = null;
		try {
			PreparedStatement st = db.createStatement("select * from chatrooms where ID = ?", false);
			st.setInt(1, id);

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				c = new Chatroom();
				c.setId(result.getInt("ID"));
				c.setName(result.getString("chatName"));
				c.setChatroomType(result.getInt("chatroomType"));
				if(!withoutAvatarsAndMembers){
					c.setAvatar(getImage(result.getString("chatAvatar")));
					c.setMembers(getChatroomMembers(c.getId(), true));
				}
				c.setLatestMessage(result.getTimestamp("latestMessage"));
				Timestamp latestUserMessage = getLatestUserMessage(userId, c);
				c.setLatestUserMessage(latestUserMessage);
			}
		} catch(Exception e){
			c = null;
		}
		return c;
	}	
	
	/**
	 * Rueckgabe aller Mitglieder eines Chatraumes als Liste.
	 * @param id
	 * Chatraumnummer
	 * @param withAvatars
	 * @return 
	 * Liste aller Chatraummitglieder
	 */
	public List<User> getChatroomMembers(int id, boolean withAvatars) {
		List<User> result = new ArrayList<User>();
		try {
			PreparedStatement st = db.createStatement("select * from chatrooms_users where chatroomID = ?", false);
			st.setInt(1, id);
			ResultSet rs = db.executeQuery(st);

			while(rs.next()){
				User u = db.getUserController().getUser(rs.getInt("userID"), withAvatars);
				result.add(u);
			}
		} catch(Exception e){
			result.clear();
		}
		return result;
	}

	/**
	 * Die neusten Nachrichten in einen Chatraum schreiben.
	 * @param userId
	 * Benutzernummer
	 * @param chatroomId
	 * Chatraumnummer
	 * @param timestamp
	 * Zeitstämpel der aktuellen zeit
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom setLatestMessage(int userId, int chatroomId, java.sql.Timestamp timestamp) {
		Chatroom c = getChatroom(userId, chatroomId, true);
		try {
			PreparedStatement st = db.createStatement("update chatrooms set latestMessage = ? where ID = ?", true);
			st.setTimestamp(1, timestamp);
			st.setInt(2, chatroomId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				c.setLatestMessage(rs.getTimestamp("latestMessage"));
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return c;
	}

	/**
	 * Gibt nach erfolgreicher Entfernung des aktuellen Members den Chatraum zurueck.
	 * @param chatroomId
	 * Chatraumnummer
	 * @param userId
	 * Benutzernummer
	 * @param timestamp
	 * Zeitstaempel der aktuellen zeit
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom removeMember(int chatroomId, int userId, HttpServletRequest request){
		Chatroom c = getChatroom(userId, chatroomId, true);
		
		if (removeMember(chatroomId, userId, db.getUserController().getUserId(request))){
			return c;
		}
		return null;
	}
	
	/**
	 * Mitglied aus einem Chatraum entfernen.
	 * @param chatroomId
	 * Chatraumnummer
	 * @param userId
	 * Benutzernummer
	 * @param currentUser
	 * aktueller Benutzer
	 * @return 
	 * true, wenn erfolgreich entfernt, ansonsten false
	 */
	private boolean removeMember(int chatroomId, int userId, int currentUser){
		
		if(!isChatroomMember(chatroomId, currentUser)){
			return false;
		}
		
		try {
			PreparedStatement st = db.createStatement("delete from chatrooms_users where chatroomID = ? and userID = ?", true);
			st.setInt(1, chatroomId);
			st.setInt(2, userId);

			st.executeUpdate();

			if(getChatroom(userId, chatroomId, false).getMembers().size() == 0) {
				return deleteChatroom(chatroomId);
			}
			return true;

		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return false;
	}

	/**
	 * Neuste Nachricht eines Benutzers in den Chatraum schreiben.
	 * @param chatroomId
	 * Chatraumnummer
	 * @param userId
	 * Benutzernummer
	 */
	public void setLatestUserMessage(int chatroomId, int userId) {
		Chatroom c = db.getChatroomsController().getChatroom(userId, chatroomId, true);
		try {
			PreparedStatement st = db.createStatement("update chatrooms_users set latestMessage = ? where userID = ? and chatroomID = ?", false);
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
			st.setTimestamp(1, timestamp);
			st.setInt(2, userId);
			st.setInt(3, chatroomId);
			st.executeUpdate();
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Chatraum-Avatar updaten.
	 * @param userId
	 * Benutzernummer
	 * @param id
	 * Chatraumnummer
	 * @param newAvatar
	 * neuer Avatar als String
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom updateChatroomAvatar(int userId, int id, String newAvatar){
		
		Chatroom c= getChatroom(userId,id,true);
		try {
			String path = c.getAvatarPath(db.getChatroomAvatarDirectory());
			saveImageToFile(newAvatar, path);

			PreparedStatement st = db.createStatement("update chatrooms set chatAvatar = ? where ID = ?", true);
			st.setString(1, path);
			st.setInt(2, id);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				c = getChatroom(userId,id,true);
				setLatestUserMessage(c.getId(), userId);
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return c;
	}

	/**
	 * Chatraum-Namen updaten.
	 * @param userId
	 * Benutzernummer
	 * @param id
	 * Chatraumnummer
	 * @param name
	 * Name des Chatraumes
	 * @return 
	 * aktualisierter Chatraum
	 */
	public Chatroom updateChatroomName(int userId, int id, String name){
		
		Chatroom c= getChatroom(userId,id,true);
		try {
			PreparedStatement st = db.createStatement("update chatrooms set chatName = ? where ID = ?", true);
			st.setString(1, name);
			st.setInt(2, id);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				c = getChatroom(userId,id,true);
				setLatestUserMessage(c.getId(), userId);
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return c;
	}

	/**
	 * Avatar als Datei abspeichern.
	 * @param base64
	 * Avatar als String
	 * @param fileName
	 * Dateiname
	 * @throws Exception
	 */
	private void saveImageToFile(String base64, String fileName) throws Exception{
		checkDirectory();

		Path p = Paths.get(fileName);
		if(!Files.exists(p)){
			Files.createFile(p);
		}
		FileWriter w = new FileWriter(fileName);
		w.write(base64);
		w.close();
	}

	/**
	 * Pruefen, ob für die abzuspeichernde Avatar-Datei ein Verzeichnis existiert, wenn false
	 * wird das Verzeichnis angelegt.
	 * @throws Exception
	 */
	private void checkDirectory() throws Exception{
		Path p = Paths.get(db.getChatroomAvatarDirectory());
		if(!Files.exists(p)){
			Files.createDirectories(p);
		}
	}

	/**
	 * Pfad zum Bild zurueckliefern.
	 * @param fileName
	 * Name der Datei
	 * @return
	 * Pfad zum Bild
	 */
	private String getImage(String fileName){
		String result = "";
		try {
			result = Paths.get(fileName).toFile().getAbsolutePath();
		}catch(Exception e){
			result = null;
		}
		return result;
	}
}