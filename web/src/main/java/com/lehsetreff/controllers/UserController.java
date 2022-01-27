package com.lehsetreff.controllers;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import com.meshenger.models.User;
import com.lehsetreff.Extensions;
import com.lehsetreff.models.UserRole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.MessageHandler;

public class UserController {
    private Database db = Database.getInstance();
    private com.meshenger.controllers.Database meshengerDb = com.meshenger.controllers.Database.getInstance();

    Random r = new Random();

	String alphabet = "1234567890abcdefghijklmnopqrstuvwxyz";

	/**
	 * Generiert einen String aus Alphabet string anhand der Random-Zahl.
	 * @return
	 * Zufaelligen String
	 */
	private String generate(){
		String result = "";
		for (int i = 0; i < 64; i++) {
			result += alphabet.charAt(r.nextInt(alphabet.length()));
		}
		return result;
	}

	/**
	 * Ueberprueft, ob der Key unique ist.
	 * @param key
	 * Generierter apiKey
	 * @return
	 * Ergebnis ob unique
	 */
	private boolean checkKey(String key) {
		boolean result = false;
		try {
			PreparedStatement st = db.createStatement("select apiKey from users where apiKey = ?", false);
			st.setString(1, key);

			ResultSet rs = db.executeQuery(st);

            result = !rs.next();
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return result;
	}

	/**
	 * Generieren und pruefen des ApiKeys.
	 * @return
	 * ApiKey
	 */
	private String generateUniqueApiKey(){
		String result = "";
		do{
			result = generate();
		} while(!checkKey(result)); 
		return result;
	}

	/**
	 * Fuegt User zur Datenbank hinzu.
	 * @param name
	 * Name des Benutzers
	 * @param passphrase
	 * Passwort des Benutzers
	 * @return
	 * Den Benutzer selbst
	 */
	public User addUser(String name, String passphrase) {
		User u = null;
		try {
			String key = generateUniqueApiKey();

			PreparedStatement st = db.createStatement("insert into users (userName, passphrase, avatar, apiKey) values(?,?,?,?)", true);
			st.setString(1, name);
			st.setString(2, passphrase);
			st.setString(3, "");
			st.setString(4, key);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				u = getUser(name, passphrase);
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return u;
	}

	/**
	 * Ueberprueft ob der User bereits vorhanden ist.
	 * @param name
	 * Name des Users
	 * @param userId
	 * Id des users
	 * @return
	 * Ist der User vorhanden
	 */
	public boolean checkUser(String name, int userId){
		boolean result = false;
		User u = getUser(userId, false);
		if(u != null){
			result = u.getName().equals(name);
		}
		return result;
	}

	/**
	 * Liefert UserId anhand des apiKeys zurueck.
	 * @param request
	 * Http Reuqest mit Api Key
	 * @return
	 * UserId wenn gefunden, sonst -1
	 */
	public int getUserId(HttpServletRequest request){
		int result = -1;
		if(isAuthenticated(request)){
			result = getUserFromApiKey(request).getId();
		}
		return result;
	}

	/**
	 * Liefert User anhand des apiKeys zurueck.
	 * @param req
	 * Http Request mit ApiKey
	 * @return 
	 * Benutzer
	 */
	public User getUserFromApiKey(HttpServletRequest req){
		String key = req.getParameter("apiKey");
		if(key == null){
			key = Extensions.getParameterFromMap(req, "apiKey");
		}
		User u = null;
		if(key != null){
			u = getUserFromKey(key);
		}
		return u;
	}


	/**
	 * Prueft ob Benutzer authenfiziert ist.
	 * @param req
	 * Http Request mit ApiKey
	 * @return
	 * ist user authentifiziert
	 */
	public boolean isAuthenticated(HttpServletRequest req){
		boolean result = false;
		try{
			User tmp = db.getUserController().getUserFromApiKey(req);
			return tmp != null;
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		return result;
	}

	public boolean isAdmin(HttpServletRequest req) {
		return db.getRolesController().getUserRole(getUserId(req)) == UserRole.Admin;
	}

	public boolean isModerator(HttpServletRequest req) {
		UserRole role = db.getRolesController().getUserRole(getUserId(req));
		return role == UserRole.Admin || role == UserRole.Mod;
	}

	public boolean isUser(HttpServletRequest req) {
		UserRole role = db.getRolesController().getUserRole(getUserId(req));
		return role == UserRole.Admin || role == UserRole.Mod || role == UserRole.User;
	}

	public boolean isGuest(HttpServletRequest req) {
		UserRole role = db.getRolesController().getUserRole(getUserId(req));
		return role == UserRole.Guest;
	}


	/**
	 * Login des Benutzers.
	 * @param userName
	 * Benutzername
	 * @param passphrase
	 * Passwort
	 * @return 
	 * Benutzerobjekt
	 */
	public User login(String userName, String passphrase){
		User u = getUser(userName, passphrase);
		return u;
	}

	/**
	 * Login mit ApiKey.
	 * @param apiKey
	 * ApiKey des Benutzers
	 * @return
	 * Benutzerobjekt
	 */
	public User login(String apiKey){
		User u = getUserFromKey(apiKey);
		return u;
	}

	/**
	 * Logout des Benutzers.
	 * @param req
	 * http request
	 * @return
	 * Logout funktioniert/fehlgeschalgen
	 */
	public boolean logout(HttpServletRequest req){
		boolean result = false;
		HttpSession s = req.getSession(false);
		if(s != null){
			s.invalidate();
			result = true;
		}
		return result;
	}




	/**
	 * Liefert Pfad zum Bild zurueck.
	 * @param fileName
	 * Name der Datei
	 * @return
	 * Pfad zum Bild
	 */
	private String getImage(String fileName){
		String result = "";
		try {
			result = Files.readString(Paths.get(fileName));
		}catch(Exception e){
			result = null;
		}
		return result;
	}


	/**
	 * User anhand http request erhalten.
	 * @param request
	 * http request mit Id
	 * @param withAvatar
	 * Ob der Avatar abgerufen werden soll.
	 * @return
	 * gesuchtes Userobjekt
	 */
	public User getUser(HttpServletRequest request, boolean withAvatar) {
		int userId = getUserId(request);
		return getUser(userId, withAvatar);
	}

	/**
	 * Liefert User anhand eines Namens zurueck.
	 * @param userName
	 * Benutzername
	 * @return
	 * gesuchtes Userobjekt
	 */
	public User getUser(String userName){
		User u = null;
		try {
			PreparedStatement st = db.createStatement("select * from users where userName = ?", false);
			st.setString(1, userName);

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));

				u.setAvatar(getImage(result.getString("avatar")));
			}
		} catch(Exception e){
			u = null;
		}
		return u;
	}

	/**
	 * User anhand des ApiKeys aus Datenbank bekommen.
	 * @param apiKey
	 * ApiKey des Benutzers
	 * @return
	 * gesuchtes User-Objekt
	 */
	public User getUserFromKey(String apiKey){
		User u = null;
		try {
			PreparedStatement st = meshengerDb.createStatement("select * from users where apiKey = ?", false);
			st.setString(1, apiKey);

			ResultSet result = meshengerDb.executeQuery(st);

			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				u.setApiKey(result.getString("apiKey"));
				u.setAvatar(getImage(result.getString("avatar")));
			}
		} catch(Exception e){
			u = null;
		}
		return u;
	}

	/**
	 * User aus Datenbank anhand der ID bekommen.
	 * @param id
	 * Id des Benutzers
	 * @param withAvatar
	 * Ob der Avatar abgerufen werden soll.
	 * @return
	 * Gesuchter Benutzer
	 */
	public User getUser(int id, boolean withAvatar) {
		User u = null;
		try {
			PreparedStatement st = meshengerDb.createStatement("select * from users where ID = ?", false);
			st.setInt(1, id);

			ResultSet result = meshengerDb.executeQuery(st);
			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				if(withAvatar){
					u.setAvatar(getImage(result.getString("avatar")));
				}
			}
		} catch(Exception e){
			u = null;
		}
		return u;
	}

	/**
	 * Erhalte User anhand des Namens und des Passworts (Login).
	 * @param name
	 * Benutzername
	 * @param passphrase
	 * Passwort
	 * @return
	 * Gesuchter User
	 */
	public User getUser(String name, String passphrase) {
		User u = null;
		try {
			PreparedStatement st = meshengerDb.createStatement("select * from users where userName = ? && passphrase = ?", false);
			st.setString(1, name);
			st.setString(2, passphrase);

			ResultSet result = meshengerDb.executeQuery(st);

			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				u.setAvatar(getImage(result.getString("avatar")));
				u.setApiKey(result.getString("apiKey"));
			}
		} catch(Exception e){
			u = null;
		}
		return u;
	}

	public boolean isThreadOwner(int threadId, HttpServletRequest request){

		try {
			PreparedStatement st = db.createStatement("select ID = ? from threads where ownerID = ? ", false);
			st.setInt(1, threadId);
			st.setInt(2, getUserId(request));

			ResultSet result = db.executeQuery(st);
			if(result.next()){
				int tmp = result.getInt("ID");
				int temp = result.getInt("ownerID");
				if (tmp == threadId && temp == getUserId(request)){
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public boolean isThreadGroupOwner(int threadGroupId, HttpServletRequest request){

		try {
			PreparedStatement st = db.createStatement("select ID = ? from threadGroups where ownerID = ? ", false);
			st.setInt(1, threadGroupId);
			st.setInt(2, getUserId(request));

			ResultSet result = db.executeQuery(st);
			if(result.next()){
				int tmp = result.getInt("ID");
				int temp = result.getInt("ownerID");
				if (tmp == threadGroupId && temp == getUserId(request)){
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public boolean isMessageSender(int messageId, HttpServletRequest request){

		try {
			PreparedStatement st = db.createStatement("select ID = ? from messages where senderID = ? ", false);
			st.setInt(1, messageId);
			st.setInt(2, getUserId(request));

			ResultSet result = db.executeQuery(st);
			// if (result != null) { // Maurice geht die form auch? weil eigentlich müsste oben die Abfrage ja nur was zurückgeben wenn
			// 	return true;		 // wenn e Naricht mit den ID und dem sender gefunden wurde oder?
			// }
			if(result.next()){
				int tmp = result.getInt("ID");
				int temp = result.getInt("ownerID");
				if (tmp == messageId && temp == getUserId(request)){
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}
