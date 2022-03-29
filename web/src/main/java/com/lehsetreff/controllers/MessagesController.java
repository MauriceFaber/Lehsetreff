package com.lehsetreff.controllers;

import java.io.FileWriter;
import java.io.IOException;
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

import com.lehsetreff.models.*;
import com.meshenger.models.User;

/**
 * Stellt den MessageController dar.
 */
public class MessagesController {
    private Database db = Database.getInstance();

	/**
	 * Fuegt eine Nachricht in einen Thread hinzu.
	 * @param content
	 * Der Inhalt der Nachricht.
	 * @param contentType
	 * Der Inhalttyp der Nachricht.
	 * @param threadId
	 * Die ID des Threads.
	 * @param senderId
	 * Die ID des Senders.
	 * @return
	 * Message Objekt
	 * @throws Exception
	 */
	public Message addMessage(String content, int contentType, int threadId, int senderId) throws Exception{
		Message m = null;
		if(content == null || content.trim().length() == 0){
			return m;
		}
		content = content.trim();
		try {
			PreparedStatement st = db.createStatement("insert into messages (contentType, dateAndTime, threadID, senderID) values(?,?,?,?)", true);
			st.setInt(1, contentType);
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
			st.setTimestamp(2,timestamp);
			st.setInt(3, threadId);
			st.setInt(4, senderId);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();
            if(rs.next()){
				int id = rs.getInt(1);
				m = new Message();
				m.setContent(content, ContentType.values()[contentType]);
				m.setId(id);
				SetContent(m, content);
				m = getMessage(id);
			}
		} catch(Exception e){
				m = new Message();
				m.setContent(e.getMessage(), ContentType.Text);
		}
		return m;
	}

/**
 * Setzt Content der Nachricht.
 * @param m
 * Message Objekt
 * @param content
 * string mit content als Inhalt
 * @throws Exception
 */
	private void SetContent(Message m, String content) throws Exception {
		switch(m.getContentId()){
			case Text:
            case Link:
            case Quote:
			m.setContent(content, m.getContentId());
			break;
			case Image:
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

	/**
	 * Get für Content der Nachricht.
	 * @param m
	 * Message Objekt
	 * @throws Exception
	 */
	private void GetContent(Message m) throws Exception {
		switch(m.getContentId()){
			case Text:
            case Link:
            case Quote:
			break;
			case Image:
			String imgPath = m.getContent();
			m.setContent(getImage(imgPath), m.getContentId());
			break;
			default:
			throw new Exception();
		}
	}

	/**
	 * Kennzeichnet eine Nachricht als gelöscht.
	 * @param id
	 * Die id der Nachricht.
	 * @return
	 * true bei Erfolg, false bei Misserfolg
	 */
	public boolean deleteMessage(int id){
		try {
			PreparedStatement st = db.createStatement("update messages contentType = ? and content = ? where ID = ?", true);
			st.setInt(1, ContentType.DELETED.getContentId());
            st.setString(2, "");
			st.setInt(3, id);
			
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
	 * Auswahl der Nachricht.
	 * @param id
	 * Die id der Nachricht.
	 * @return 
	 * Nachricht Objekt
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
				m.setContent(result.getString("content"), ContentType.values()[result.getInt("contentType")]);
				GetContent(m);
				m.setTimeStamp(result.getTimestamp("dateAndTime"));
				int threadId = result.getInt("threadID");
				int senderId = result.getInt("senderID");
				m.setThread(db.getThreadController().getThread(threadId));
				m.setSender(db.getUserController().getUser(senderId, false));
				User sender = db.getUserController().getUser(m.getSender().getId(), false);
				m.setSenderName(sender.getName());
			}
		} catch(Exception e){
			m = null;
		}
		return m;
	}	
	
/**
 * Bearbeiten der Nachricht.
 * @param content
 * Der Inhalt der Nachricht.
 * @param contentType
 * Der Inhalts-Typ der Nachricht.
 * @param messageID
 * Die ID der Nachricht.
 * @return
 * Nachricht Objekt
 */
	public Message modifyMessage(String content , int contentType, int messageID){
		if(content == null || content.trim().length() == 0){
			return null;
		}
		content = content.trim();
		Message m = getMessage(messageID);

		try {
			PreparedStatement st = db.createStatement("update messages contentType = ? and content = ? where ID = ?", true);
			st.setInt(1, contentType);
            st.setString(2, content);
			st.setInt(3, messageID);
			
			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				return m;
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}

		return m;
	}

	/**
	 * Gibt eine Liste aus Nachrichten in einem Thread zurück.
	 * @param threadId
	 * Die id des Threads.
	 * @return
	 * Liste aus Nachrichten des Threads.
	 */
	public List<Message> getMessages(int threadId) {
		List<Message> result = new ArrayList<Message>();
		try {
			PreparedStatement st = db.createStatement("select * from messages where threadID = ?", true);
			st.setInt(1, threadId);

			ResultSet rs = db.executeQuery(st);

			while(rs.next()){
				Message m = new Message();
				m.setId(rs.getInt("ID"));
				m.setContent(rs.getString("content"), ContentType.values()[rs.getInt("contentType")]);
				GetContent(m);
				m.setTimeStamp(rs.getTimestamp("dateAndTime"));
				int senderId = rs.getInt("senderID");
				m.setThread(db.getThreadController().getThread(threadId));
				m.setSender(db.getUserController().getUser(senderId, false));
				User sender = db.getUserController().getUser(m.getSender().getId(), false);
				m.setSenderName(sender.getName());
				result.add(m);
			}

			} catch(Exception e){
			result.clear();
			Message t = new Message();
			t.setContent(e.getMessage(), ContentType.Text);
		}
		return result;
	}

	/**
	 * Speichert das Bild in die Datei.
	 * @param base64
	 * Bilddarstellung als base 64 string
	 * @param fileName
	 * Name der Datei.
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
		Path p = Paths.get(db.getImagesDirectory());
		if(!Files.exists(p)){
			Files.createDirectories(p);
		}
	}

	/**
	 * Liefert Pfad zum Bild zurueck.
	 * @param fileName
	 * Name der Datei
	 * @return
	 * Der Inhalt der Datei.
	 */
	private String getImage(String fileName){
		String result = "";
		try {
			result = readFile(fileName);
		}catch(Exception e){
			result = null;
		}
		return result;
	}

	/**
	 * Einlesen der Datei
	 * @param path
	 * Der Pfad der Datei.
	 * @return
	 * dekodierten String der Datei
	 * @throws IOException
	 */
	private String readFile(String path) throws IOException {
  		byte[] encoded = Files.readAllBytes(Paths.get(path));
  		return new String(encoded);
	}
}
