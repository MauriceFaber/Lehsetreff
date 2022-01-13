package com.meshenger.models;

import java.sql.Timestamp;

public class Message {

//private
	private int id;
	private int senderId;
	private int chatroomId;
	private String content;
	private int contentType;
	private Timestamp timeStamp;
	private String senderName;

//public
	//getter und setter

	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}

	public int getSenderId(){
		return senderId;
	}
	public void setSenderId(int id){
		this.senderId = id;
	}

	public int getChatroomId(){
		return chatroomId;
	}
	public void setChatroomId(int id){
		this.chatroomId = id;
	}

	public String getContent(){
		return content;
	}
	public void setContent(String content, int type){
		this.content = content;
		contentType = type;
	}

	public int getContentType(){
		return contentType;
	}

	public Timestamp getTimeStamp(){
		return timeStamp;
	}
	public void setTimeStamp(Timestamp timeStamp){
		this.timeStamp = timeStamp;
	}

	public void setSenderName(String name) {
		this.senderName = name;
	}

	public String getSenderName() {
		return senderName;
	}

	@Override
	public String toString() {
		return Integer.toString(id) + ": " + Integer.toString(chatroomId) + ": " + timeStamp.toString() + ": " + Integer.toString(contentType) + ": " + content;
	}

	public String getImagePath(String folder){
		return folder + "/img_" + id + ".txt";
	}
}