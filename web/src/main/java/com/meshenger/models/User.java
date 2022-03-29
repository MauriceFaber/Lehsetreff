package com.meshenger.models;
import java.io.Serializable;

import com.lehsetreff.models.UserRole;

/**
 * Stellt einen Benutzer dar.
 */
public class User implements Serializable {

//private
	private static final long serialVersionUID = 1L;
	private int id;
	private String userName;
	private String apiKey;
	private String passphrase;
	private String avatar;
	private UserRole role;

//public

	public static final String DEFAULT_AVATAR = "default";

	//getter und setter
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}

	public String getName(){
		return userName;
	}
	public void setName(String name){
		this.userName = name;
	}

	public String getApiKey(){
		return apiKey;
	}
	public void setApiKey(String apiKey){
		this.apiKey = apiKey;
	}

	public String getPassphrase(){
		return passphrase;
	}
	public void setPassphrase(String passphrase){
		this.passphrase = passphrase;
	}

	public String getAvatar(){
		return (avatar == null) ? DEFAULT_AVATAR : avatar;
	}
	public void setAvatar(String avatar){
		this.avatar = avatar;
	}

	public String getAvatarPath(String folder){
		return folder + "/avatar_" + id + ".txt";
	}

	public void setUserRole(UserRole role){
		this.role = role;
	}

	public UserRole getUserRole(){
		return role;
	}
}