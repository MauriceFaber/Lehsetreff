package com.lehsetreff.controllers;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
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
	public Message addMessage(String content, int contentType, int threadId, int senderId, String additional) throws Exception{
		Message m = null;
		int quotedMessageId = -1;
		if(contentType == ContentType.Quote.getContentId()){
			if(additional == null || additional.trim().isEmpty()){
				return null;
			}
			additional = additional.trim();
			quotedMessageId = Integer.parseInt(additional);
			Message quotedMessage = getMessage(quotedMessageId);
			if(quotedMessage == null){
				return null;

			}
		}
		if(content == null || content.trim().length() == 0){
			return null;
		}
		content = content.trim();
		try {
			PreparedStatement st = db.createStatement("insert into messages (contentType, dateAndTime, threadID, senderID, content, additional) values(?,?,?,?,?,?)", true);
			st.setInt(1, contentType);
			OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
			Timestamp timestamp =  new Timestamp(utc.toInstant().toEpochMilli());
			st.setTimestamp(2,timestamp);
			st.setInt(3, threadId);
			st.setInt(4, senderId);
			String tmpContent = "";
				tmpContent = content;
				if(contentType == ContentType.Image.getContentId()){
					content = "";
			}
			st.setString(5, content);
			st.setString(6, additional);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();
            if(rs.next()){
				int id = rs.getInt(1);
				m = getMessage(id);
					SetContent(m, tmpContent);		
					
			}
		} catch(Exception e){
			m = null;
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
	 * Get f??r Content der Nachricht.
	 * @param m
	 * Message Objekt
	 * @throws Exception
	 */
	private void GetContent(Message m) throws Exception {
		switch(m.getContentId()){
			case Text:
            case Link:
            case Quote:
			case DELETED:
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
	 * Kennzeichnet eine Nachricht als gel??scht bzw. l??scht eine als gel??schte Nachricht endg??ltig.
	 * @param id
	 * Die id der Nachricht.
	 * @return
	 * true bei Erfolg, false bei Misserfolg
	 */
	public boolean deleteMessage(int id){
		try {
            boolean finallyDelete = getMessage(id).getContentId() == ContentType.DELETED; 
			if(finallyDelete){
				PreparedStatement st = db.createStatement("delete from messages where ID = ?", true);
				st.setInt(1, id);
				st.executeUpdate();
			}else {
				PreparedStatement st = db.createStatement("update messages set contentType = ?, content = ?, wasModified = TRUE where ID = ?", true);
				st.setInt(1, ContentType.DELETED.getContentId());
            	st.setString(2, "");
				st.setInt(3, id);
				st.executeUpdate();
			}
		    return true;
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
				m.setWasModified(result.getBoolean("wasModified"));
				m.setAdditional(result.getString("additional"));
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
	public Message modifyMessage(String content, int contentType, int messageID, String additional){
		if(content == null){
			return null;
		}
		int quotedMessageId = -1;
		if(contentType == ContentType.Quote.getContentId()){
			if(additional == null || additional.trim().isEmpty()){
				return null;
			}
			quotedMessageId = Integer.parseInt(additional);
			Message quotedMessage = getMessage(quotedMessageId);
			if(quotedMessage == null){
				return null;

			}
		}
		content = content.trim();
		Message m = getMessage(messageID);

		try {
			PreparedStatement st = db.createStatement("update messages set contentType = ?, content = ?, wasModified = TRUE where ID = ?", true);
			st.setInt(1, contentType);
            st.setString(2, content);
			st.setInt(3, messageID);
			
			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				m = getMessage(m.getId());
				return m;
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}

		return m;
	}

	/**
	 * Gibt eine Liste aus Nachrichten in einem Thread zur??ck.
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
				Message m = getMessage(rs.getInt("ID"));
				result.add(m);
			}
			} catch(Exception e){
			result.clear();
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
		base64 = base64.replace(" ", "+");
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
	 * Pruefen, ob f??r die abzuspeichernde Avatar-Datei ein Verzeichnis existiert, wenn false
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
		String result = "https://www.publicdomainpictures.net/pictures/280000/nahled/not-found-image-15383864787lu.jpg";
		try {
			result = readFile(fileName);
			result = result.replaceAll("\\s+","");

		}catch(Exception e){
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
