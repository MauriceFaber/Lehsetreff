package com.meshenger.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static final String CONNECTION_STRING = "jdbc:mysql://meshenger.de:3306/meshenger";
	private static final String USER_NAME = "meshengeruser";
	private static final String PASSWORD = "Meshkalation21";
	private static final String AVATAR_DIRECTORY = "/meshenger/avatars";
	private static final String CHATROOM_AVATAR_DIRECTORY = "/meshenger/chatroomAvatars";
	private static final String IMAGE_DIRECTORY = "/meshenger/images";

	/**
	 * Avatar-Speicherort zurueckgeben.
	 * @return
	 * AVATAR_DIRECTORY
	 */
	public String getAvatarDirectory(){
		return AVATAR_DIRECTORY;
	}

	/**
	 * Avatar-Speicherort eines Chatraumes zurueckgeben.
	 * @return 
	 * CHATROOM_AVATAR_DIRECTORY
	 */
	public String getChatroomAvatarDirectory(){
		return CHATROOM_AVATAR_DIRECTORY;
	}

	/**
	 * Bild-Speicherort einer Nachricht zurueckgeben.
	 * @return 
	 * IMAGE_DIRECTORY
	 */
	public String getImagesDirectory(){
		return IMAGE_DIRECTORY;
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
	private MessagesController messagesController;
	private ChatroomsController chatroomsController;
	private ContactsController contactsController;

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
	 * Messagecontroller zurueckgeben, falls keiner besteht.
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
	 * Chatroomcontroller zurueckgeben falls keiner besteht.
	 * @return 
	 * chatroomsController
	 */
	public ChatroomsController getChatroomsController(){
		if(chatroomsController == null){
			chatroomsController = new ChatroomsController();
		}
		return chatroomsController;
	}

	/**
	 * Contactscontroller zurueckgeben, falls keiner besteht
	 * @return 
	 * contactsController
	 */
	public ContactsController getContactsController(){
		if(contactsController == null){
			contactsController = new ContactsController();
		}
		return contactsController;
	}

	/**
	 * Pruefung, ob eine Verbindung zur Datenbank besteht.
	 * @return 
	 * Erfolg oder Misserfolg der Verbindung
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
	 * Schließen der Datenbankverbindung.
	 */
	public void disconnect(){
		try{
			con.close();
		} catch (SQLException e){

		}
	}
}