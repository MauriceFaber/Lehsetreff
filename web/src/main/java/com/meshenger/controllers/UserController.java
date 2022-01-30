package com.meshenger.controllers;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import com.lehsetreff.Extensions;
import com.meshenger.models.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class UserController {
	private Database db = Database.getInstance();

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
	 * Ueberprueft/legt Verzeichnis zum Ablegen der Avatare an.
	 * @throws Exception
	 */
	private void checkDirectory() throws Exception{
		Path p = Paths.get(db.getAvatarDirectory());
		if(!Files.exists(p)){
			Files.createDirectories(p);
		}
	}

	/**
	 * Konvertiert Avatar aus base64 zu einer Datei.
	 * @param base64
	 * Code des Avatars
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
	 * Liefert Pfad zum Bild zurueck.
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
			String path = u.getAvatarPath(db.getAvatarDirectory());
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
			PreparedStatement st = db.createStatement("select * from users where apiKey = ?", false);
			st.setString(1, apiKey);

			ResultSet result = db.executeQuery(st);

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
			PreparedStatement st = db.createStatement("select * from users where ID = ?", false);
			st.setInt(1, id);

			ResultSet result = db.executeQuery(st);
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
			PreparedStatement st = db.createStatement("select * from meshenger.users where userName = ? && passphrase = ?", false);
			st.setString(1, name);
			st.setString(2, passphrase);

			ResultSet result = db.executeQuery(st);

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
}