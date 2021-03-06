package com.lehsetreff.controllers;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static final String CONNECTION_STRING = "jdbc:mysql://raspberrypi:3306/lehsetreff";
    private static final String USER_NAME = "lehsetreffuser22";
	private static final String PASSWORD = "Lehserkalation22";
	private static final String IMAGE_DIRECTORY = "/lehsetreff/images";
	private static final String AVATAR_DIRECTORY = "/meshenger/images";

	/**
	 * Bild-Speicherort einer Nachricht zurueckgeben.
	 * @return 
	 * IMAGE_DIRECTORY
	 */
	public String getImagesDirectory(){
		return IMAGE_DIRECTORY;
	}

	/**
	 * Bild-Speicherort eines Avatars zurueckgeben.
	 * @return 
	 * AVATAR_DIRECTORY
	 */
	public String getAvatarsDirectory(){
		return AVATAR_DIRECTORY;
	}
	
	private Connection con;
	private static Database db;

	/**
	 * Neue Datenbankinstanz erstellen.
	 * @return 
	 * neue Datenbankinstanz
	 */
	public static Database getInstance(){
		 if(db == null){
			db = new Database();
			db.connect();
		}
		return db;
	}

	private UserController userController;
    private ThreadController threadController;
	private MessagesController messagesController;
	private ThreadGroupController threadGroupController;
	private UserRolesController rolesController;

	/**
	 * Usercontroller zurueckgeben, falls keiner besteht.
	 * @return 
	 * userController
	 */
	public UserController getUserController(){
		if(userController == null){
			userController = new UserController();
		}
		return userController;
	}

	/**
	 * ThreadGroupController zurueckgeben, falls keiner besteht.
	 * @return
	 * threadGroupController
	 */
	public ThreadGroupController getThreadGroupController(){
		if(threadGroupController == null){
			threadGroupController = new ThreadGroupController();
		}
		return threadGroupController;
	}

	/**
	 * ThreadController zurueckgeben, falls keiner besteht.
	 * @return
	 * threadController
	 */

    public ThreadController getThreadController(){
		if(threadController == null){
			threadController = new ThreadController();
		}
		return threadController;
	}

	/**
	 * MessagesController zurueckgeben, falls keiner besteht.
	 * @return
	 * messagesController
	 */
	public MessagesController getMessagesController(){
		if(messagesController == null){
			messagesController = new MessagesController();
		}
		return messagesController;
	}

	/**
	 * UserRolesController zurueckgeben, falls keiner besteht.
	 * @return
	 * rolesController
	 */
	public UserRolesController getRolesController(){
		if (rolesController == null) {
			rolesController = new UserRolesController();
		}
		return rolesController;
	}


	/**
	 * Pruefung, ob eine Verbindung zur Datenbank besteht.
	 * @return 
	 * true bei Erfolg, false bei Misserfolg
	 */
	public boolean connect(){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(CONNECTION_STRING, USER_NAME, PASSWORD);
			return true;
		} catch (Exception e){
			con = null;
			System.out.println(e.getStackTrace());
			return false;
		}
	}
	
	/**
	 * Erstellen der SQL Statements.
	 * @param query
	 * @param returnGeneratedKeys
	 * @return 
	 * Rueckgabe des Statements
	 */
	public PreparedStatement createStatement(String query, boolean returnGeneratedKeys){
		PreparedStatement st = null;
		try{
			if(returnGeneratedKeys){
				st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			}else{
				st = con.prepareStatement(query);
			}
		} catch (SQLException e){

		}
		return st;
	}

	/**
	 * Ausfuehren der Query.
	 * @param statement
	 * SQL Statement
	 * @return result
	 * Rueckgabe Ergebnisses der Query
	 */
	ResultSet executeQuery(PreparedStatement statement){
		ResultSet result = null;
		try{
			result = statement.executeQuery();
		} catch(SQLException e){

		}
		return result;
	}

	/**
	 * Schlie??en der Datenbankverbindung.
	 */
	public void disconnect(){
		try{
			con.close();
		} catch (SQLException e){

		}
	}
}