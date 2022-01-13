package com.meshenger.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.meshenger.models.*;

import jakarta.servlet.http.HttpServletRequest;

public class ContactsController {
	
	private Database db = Database.getInstance();

	/** 
	 * Aktuellen Kontakt zurueckgeben.
	 * @param contactName
	 * Kontaktname
	 * @param contactId
	 * Kontaktnummer
	 * @param request
	 * Http Request
	 * @return 
	 * aktuelelr Kontakt als User
	*/
	public User addContact(String contactName, int contactId, HttpServletRequest request) {
		int userId = db.getUserController().getUserId(request);
		return addContact(contactName, contactId, userId);
	}

	/** 
	 * Neuer Kontakt wird hinzugefuegt.
	 * @param contactName
	 * Kontaktname
	 * @param contactId
	 * Kontaktnummer
	 * @param contactOwnerId
	 * Nummer des Kontakt-Eigentümers
	 * @return User
	 * Liefer den entsprechenden User zurueck
	*/
	public User addContact(String contactName, int contactId, int contactOwnerId) {
		User c = null;
		if(db.getUserController().checkUser(contactName, contactId)){
			try {
				PreparedStatement st = db.createStatement("insert into contacts (contactID, contactOwnerID) values(?,?)", true);
				st.setInt(1, contactId);
				st.setInt(2, contactOwnerId);

				st.executeUpdate();

				c = getContact(contactId, contactOwnerId);

			} catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		return c;
	}

	/**
	 * Den Benutzer anhand der ID bekommen.
	 * @param contactId
	 * Kontaktnummer
	 * @param request
	 * Http Request
	 * @return 
	 * User-Objekt
	 */
	public User getContact(int contactId, HttpServletRequest request){
		int userId = db.getUserController().getUserId(request);
		return getContact(contactId, userId);
	}

	/**
	 * Liefert den User entsprechend der Datenbank zurueck.
	 * @param contactId
	 * Id des Kontaktes
	 * @param contactOwnerId
	 * ID des Kontakt Besitzers
	 * @return
	 * User-Objekt
	 */
	public User getContact(int contactId, int contactOwnerId){
		User c = null;
		try {
			PreparedStatement st = db.createStatement("select * from contacts where contactID = ? and contactOwnerID = ?", false);
			st.setInt(1, contactId);
			st.setInt(2, contactOwnerId);

			ResultSet rs = db.executeQuery(st);

			if(rs.next()){
				c = db.getUserController().getUser(rs.getInt("contactID"), true);
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
		return c;
	}

	/**
	 * Geloeschten Kontakt zurueckgeben.
	 * @param contactId
	 * Id des Kontaktes
	 * @param request
	 * Http Request
	 * @return
	 * Ergebnis der Operation
	 */
	public boolean deleteContact(int contactId, HttpServletRequest request){
		int userId = db.getUserController().getUserId(request);
		return deleteContact(contactId, userId);
	}

	/**
	 * Loescht Kontakt eines Users aus Datenbank.
	 * @param contactId
	 * Id des Kontaktes
	 * @param contactOwnerId
	 * Id des Kontaktbesitzers
	 * @return Ergebnis der Loeschung
	 */
	private boolean deleteContact(int contactId, int contactOwnerId){
		try {
			PreparedStatement st = db.createStatement("delete from contacts where contactID = ? and contactOwnerID = ?", true);
			st.setInt(1, contactId);
			st.setInt(2, contactOwnerId);

			st.executeUpdate();
			return true;
		} catch(Exception e){
				System.out.println(e.getMessage());
		}
		return false;
	}

	/**
	 * Liefert alle Kontakte des aktuellen Users zurueck.
	 * @param request
	 * Http Request
	 * @return 
	 * Kontaktliste
	 */
	public List<User> getContacts(HttpServletRequest request){
		int userId = db.getUserController().getUserId(request);
		return getContacts(userId);
	}

	/**
	 * Liefert alle Kontakte eines Benutzers zurueck.
	 * @param userId
	 * Id des Users
	 * @return 
	 * Kontaktliste von Usern
	 */
	public List<User> getContacts(int userId){
		List<User> result = new ArrayList<User>();
		try {
			PreparedStatement st = db.createStatement("select * from contacts where contactOwnerID = ?", false);
			st.setInt(1, userId);

			ResultSet rs = db.executeQuery(st);

			while(rs.next()){
				User c = db.getUserController().getUser(rs.getInt("contactID"), true);
				result.add(c);
			}
		} catch(Exception e){
			result.clear();
		}
		return result;
	}
}