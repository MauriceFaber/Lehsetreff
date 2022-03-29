package com.lehsetreff.controllers;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;
import com.lehsetreff.models.DbConfig;

public class Database {
	DbConfig config;

	/**
	 * Bild-Speicherort einer Nachricht zurueckgeben.
	 * @return 
	 * IMAGE_DIRECTORY
	 */
	public String getImagesDirectory(){
		return config.imageDirectory;
	}
	
	public Database(){
		//String configFilePath = "dbConfig.json";
		//config = new Gson().fromJson(configFilePath, DbConfig.class); 
		config = new DbConfig();
		config.username = "lehsetreffuser22";
		config.password = "Lehserkalation22";
		config.connectionString = "jdbc:mysql://raspberrypi:3306/lehsetreff";
		config.imageDirectory = "/lehsetreff/images";
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
			con = DriverManager.getConnection(config.connectionString, config.username, config.password);
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