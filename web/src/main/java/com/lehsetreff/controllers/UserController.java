package com.lehsetreff.controllers;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.meshenger.models.User;
import com.lehsetreff.Extensions;
import com.lehsetreff.PasswordAuthentication;
import com.lehsetreff.models.UserRole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Stellt den UserController dar.
 */
public class UserController {
    private Database db = Database.getInstance();


	PasswordAuthentication auth = new PasswordAuthentication();

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
		name = name.trim();
		passphrase = passphrase.trim();
		if(name.length() == 0 || passphrase.length() == 0){
			return null;
		}
		User u = null;
		try {
			String key = generateUniqueApiKey();

			String hashedPassword = auth.hash(passphrase.toCharArray());

			PreparedStatement st = db.createStatement("insert into users (userName, passphrase, avatar, apiKey) values(?,?,?,?)", true);
			st.setString(1, name);
			st.setString(2, hashedPassword);
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

	// public void hashPassword(){
	// 	try {
	// 		PreparedStatement st = db.createStatement("select * from users", false);

	// 		ResultSet result = db.executeQuery(st);

	// 		while(result.next()){
	// 			User u = new User();
	// 			u.setId(result.getInt("ID"));
	// 			u.setName(result.getString("userName"));
	// 			String passphrase = result.getString("passphrase");
	// 			u.setAvatar(getImage(result.getString("avatar")));
	// 			u.setApiKey(result.getString("apiKey"));

	// 			if(passphrase.charAt(0) == '$'){
	// 				continue;
	// 			}

	// 		PasswordAuthentication auth = new PasswordAuthentication();
	// 		String hashed = auth.hash(passphrase.toCharArray());

	// 		PreparedStatement sta = db.createStatement("update meshenger.users set passphrase = ? where ID = ?", true);
	// 		sta.setString(1, hashed);
	// 		sta.setInt(2, u.getId());

	// 		int intResult = sta.executeUpdate();
	// 		}


	// 		} catch (Exception e) {
	// 			String message = e.getMessage();
	// 			System.out.println(message);
	// 		}
	// }

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
		userName = userName.trim();
		passphrase = passphrase.trim();
		if(userName.isEmpty() || passphrase.isEmpty()){
			return null;
		}
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
	 * Der Inhalt der Datei
	 */
	private String getImage(String fileName){
		String result = "https://png.pngtree.com/png-vector/20190114/ourlarge/pngtree-vector-avatar-icon-png-image_313572.jpg";
		try {
			result = readFile(fileName);
		}catch(Exception e){
		}
		return result;
	}


	private String readFile(String path) throws IOException {
  		byte[] encoded = Files.readAllBytes(Paths.get(path));
  		return new String(encoded);
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
				u.setUserRole(db.getRolesController().getUserRole(u.getId()));
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
			PreparedStatement st = db.createStatement("select * from users where apiKey = ?", false);
			st.setString(1, apiKey);

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				u.setApiKey(result.getString("apiKey"));
				u.setAvatar(getImage(result.getString("avatar")));
				u.setUserRole(db.getRolesController().getUserRole(u.getId()));
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
			PreparedStatement st = db.createStatement("select * from users where ID = ?", false);
			st.setInt(1, id);

			ResultSet result = db.executeQuery(st);
			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				u.setUserRole(db.getRolesController().getUserRole(u.getId()));
				if(withAvatar){
					u.setAvatar(getImage(result.getString("avatar")));
				}
			}
		} catch(Exception e){
			u = null;
		}
		return u;
	}

	public List<User> getUsers(Boolean withAvatar){
		List<User> users = new ArrayList<User>();
		try {
			PreparedStatement st = db.createStatement("select * from users", false);

			ResultSet result = db.executeQuery(st);
			while(result.next()){
				User u  = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				u.setUserRole(db.getRolesController().getUserRole(u.getId()));
				if(withAvatar){
					u.setAvatar(getImage(result.getString("avatar")));
				}
				users.add(u);
			}
		} catch(Exception e){
			users = null;
		}
		return users;
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
			PreparedStatement st = db.createStatement("select * from users where userName = ?", false);
			st.setString(1, name);
			// st.setString(2, hashedPassword);

			ResultSet result = db.executeQuery(st);

			if(result.next()){
				u = new User();
				u.setId(result.getInt("ID"));
				u.setName(result.getString("userName"));
				u.setAvatar(getImage(result.getString("avatar")));
				u.setApiKey(result.getString("apiKey"));
				String tmpPassphrase = result.getString("passphrase");
				boolean isAuth = auth.authenticate(passphrase.toCharArray(), tmpPassphrase);

				u.setUserRole(db.getRolesController().getUserRole(u.getId()));

				if(!isAuth){
					u = null;
				}
			}
		} catch(Exception e){
			u = null;
		}
		return u;
	}

	/**
	 * Ueberpruefe, ob Benutzer den Thread besitzt.
	 * @param threadId
	 * Die id des Threads.
	 * @param request
	 * Servlet Anfrage
	 * @return
	 * true bei Erfolg, sonst false.
	 */
	public boolean isThreadOwner(int threadId, HttpServletRequest request){

		try {
			PreparedStatement st = db.createStatement("select * from threads where ID = ? and ownerID = ? ", false);
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

	/**
	 * Gibt zur??ck, ob der Benutzer der Thread-Gruppen-Besitzer ist.
	 * @param threadGroupId
	 * Die Id der ThreadGruppe.
	 * @param request
	 * Das Reqeust.
	 * @return
	 * Wahr, wenn der Benutzer der Besitzer ist, ansonsten False.
	 */
	public boolean isThreadGroupOwner(int threadGroupId, HttpServletRequest request){
		try {
			PreparedStatement st = db.createStatement("select * from threadGroups where ID = ? and ownerID = ? ", false);
			st.setInt(1, threadGroupId);
			st.setInt(2, getUserId(request));

			ResultSet result = db.executeQuery(st);
			if(result.next()){
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Aktualisiert den Avatar eines Benutzers.
	 * @param id
	 * Id des Benutzers
	 * @param newAvatar
	 * Avatar als base64
	 * @return
	 * Benutzerobjekt
	 */
	public User updateUser(int id, String newAvatar){
		User u = getUser(id, false);
		try {
			String path = u.getAvatarPath(db.getAvatarsDirectory());
			saveImageToFile(newAvatar, path);

			PreparedStatement st = db.createStatement("update users set avatar = ? where ID = ?", true);
			st.setString(1, path);
			st.setInt(2, id);

			st.executeUpdate();
			ResultSet rs = st.getGeneratedKeys();

            if(rs.next()){
				u = getUser(id, true);
			}
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return u;
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
	 * Gibt zurueck, ob der Benutzer die Nachricht gesendet hat.
	 * @param messageId
	 * Die id der Nachricht.
	 * @param request
	 * Servlet Anfrage
	 * @return
	 * true bei Erfolg, sonst false
	 */
	public boolean isMessageSender(int messageId, HttpServletRequest request){
		try {
			PreparedStatement st = db.createStatement("select * from messages where ID = ? and senderID = ? ", false);
			st.setInt(1, messageId);
			st.setInt(2, getUserId(request));

			ResultSet result = db.executeQuery(st);
			if(result.next()){
				int tmp = result.getInt("ID");
				int temp = result.getInt("senderID");
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
