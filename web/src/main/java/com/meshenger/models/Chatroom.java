package com.meshenger.models;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stellt einen Chatraum dar.
 */
public class Chatroom implements Serializable {
//private
	private static final long serialVersionUID = 2L;

	private int id;
	private int chatroomType;
	private String name;
	private String avatar;
	private List<User> members = new ArrayList<User>();

//Public

	public static final String DEFAULT_AVATAR = "default";
	public static final int GROUP_CHAT = 0;
	public static final int SINGLE_CHAT = 1;
	
	public java.sql.Timestamp latestMessage;
	public java.sql.Timestamp latestUserMessage;
	
	//Getter und Setter
	public int getChatroomType(){
		return chatroomType;
	}
	public void setChatroomType(int type){
		chatroomType = type;
	}

	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}

	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}

	public String getAvatar(){
		return (avatar == null) ? DEFAULT_AVATAR : avatar;
	}
	public void setAvatar(String avatar){
		this.avatar = avatar;
	}

	public List<User> getMembers(){
		return members;
	}
	public void setMembers(List<User> members){
		this.members = members;
	}

	public java.sql.Timestamp getlatestMessage(){
		return latestMessage;
	}
	public void setLatestMessage(java.sql.Timestamp timestamp){
		this.latestMessage = timestamp;
	}

	public java.sql.Timestamp getLatestUserMessage(){
		return latestUserMessage;
	}

	public void setLatestUserMessage(java.sql.Timestamp timestamp){
		this.latestUserMessage = timestamp;
	}

	public String getAvatarPath(String folder){
		return folder + "/avatar_" + id + ".txt";
	}

}