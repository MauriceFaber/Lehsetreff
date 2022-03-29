package com.meshenger.models;

/**
 * Stellt einen Kontakt eines Benutzers dar.
 */
public class Contact {
//private
	private int contactOwnerId;
	private int contactId;

//public

	//getter und setter
	public int getContactOwnerId(){
		return contactOwnerId;
	}

	public void setContactOwnerId(int id){
		contactOwnerId = id;
	}


	public int getContactId(){
		return contactId;
	}

	public void setContactId(int id){
		contactId = id;
	}
}