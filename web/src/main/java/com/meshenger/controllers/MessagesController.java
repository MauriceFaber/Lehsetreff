package com.meshenger.controllers;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.meshenger.models.*;

public class MessagesController {
	private Database db = Database.getInstance();

	/**
	 * Fuegt der Datenbank eine neue Nachricht hinzu.
	 * @param content
	 * Inhalt der Nachricht
	 * @param contentType
	 * Art des Inhalts
	 * @param chatroomId
	 * ID des Chatrooms
	 * @param senderId
	 * ID des Senders
	 * @throws Exception
	 * @return 
	 * Die Nachricht selbst
	 */
	public Message addMessage(String content, int contentType, int chatroomId, int senderId) throws Exception{
		Message m = null;
		try {
			PreparedStatement st = db.createStatement("insert into messages (contentType, dateAndTime, chatroomID, senderID) values(?,?,?,?)", true);
			st.setInt(1, contentType);
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
			st.setTimestamp(2,timestamp);
			st.setInt(3, chatroomId);
			st.setInt(4, senderId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();
            if(rs.next()){
				int id = rs.getInt(1);
				m = new Message();
				m.setContent(content, contentType);
				m.setId(id);
				SetContent(m, content);
				m = getMessage(id);
				db.getChatroomsController().setLatestMessage(senderId, chatroomId,timestamp);
				db.getChatroomsController().setLatestUserMessage(chatroomId, senderId);
			}
		} catch(Exception e){
				m = new Message();
				m.setContent(e.getMessage(), 0);
		}
		return m;
	}

	private void SetContent(Message m, String content) throws Exception {
		switch(m.getContentType()){
			case 0:
			m.setContent(content, m.getContentType());
			break;
			case 1:
			String imgPath = m.getImagePath(db.getImagesDirectory());
			saveImageToFile(content, imgPath);
			content = imgPath;
			break;
			default:
			throw new Exception();
		}
		PreparedStatement st = db.createStatement("update messages set content = ? where ID = ?", true);
		st.setString(1, content);
		st.setInt(2, m.getId());
		st.executeUpdate();
	}

	private void GetContent(Message m) throws Exception {
		switch(m.getContentType()){
			case 0:
			break;
			case 1:
			String imgPath = m.getContent();
			m.setContent(getImage(imgPath), m.getContentType());
			break;
			default:
			throw new Exception();
		}
	}

	/** Loeschen einer Nachricht.
	 * @param id
	 * id der Nachricht
	 * @return 
	 * die geloeschte Nachricht
	 */
	public boolean deleteMessage(int id){
		try {
			PreparedStatement st = db.createStatement("update messages contentType = ? where ID = ?", true);
			st.setInt(1, 0);
			st.setInt(2, id);
			
			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				return true;
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return false;
	}

	/**
	 * Erhalt einer bestimmten Nachricht.
	 * @param id
	 * Id der Nachricht
	 * @return
	 * Die gesuchte Nachricht
	 */
	public Message getMessage(int id) {
		Message m = null;
		try {
			PreparedStatement st = db.createStatement("select * from messages where ID = ?", false);
			st.setInt(1, id);

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				m = new Message();
				m.setId(result.getInt("ID"));
				m.setContent(result.getString("content"), result.getInt("contentType"));
				GetContent(m);
				m.setTimeStamp(result.getTimestamp("dateAndTime"));
				m.setChatroomId(result.getInt("chatroomID"));
				m.setSenderId(result.getInt("senderID"));
				User sender = db.getUserController().getUser(m.getSenderId(), false);
				m.setSenderName(sender.getName());
			}
		} catch(Exception e){
			m = null;
		}
		return m;
	}	
	
	/**
	 * Liefert alle Nachrichten in einem bestimmten Chatroom zurueck.
	 * @param chatroomId
	 * Id des Chatrooms
	 * @param userId
	 * Id des Users
	 * @return
	 * Liste der Nachrichten
	 */
	public List<Message> getMessages(int chatroomId, int userId) {
		List<Message> result = new ArrayList<Message>();
		try {
			PreparedStatement st = db.createStatement("select * from messages where chatroomID = ?", true);
			st.setInt(1, chatroomId);

			ResultSet rs = db.executeQuery(st);

			while(rs.next()){
				Message m = new Message();
				m.setId(rs.getInt("ID"));
				m.setContent(rs.getString("content"), rs.getInt("contentType"));
				GetContent(m);
				m.setTimeStamp(rs.getTimestamp("dateAndTime"));
				m.setChatroomId(rs.getInt("chatroomID"));
				m.setSenderId(rs.getInt("senderID"));
				User sender = db.getUserController().getUser(m.getSenderId(), false);
				m.setSenderName(sender.getName());
				result.add(m);
			}
			db.getChatroomsController().setLatestUserMessage(chatroomId, userId);
			} catch(Exception e){
			result.clear();
			Message t = new Message();
			t.setContent(e.getMessage(), 0);
		}
		return result;
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