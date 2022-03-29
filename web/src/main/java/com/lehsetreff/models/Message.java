package com.lehsetreff.models;

import java.sql.Timestamp;

import com.meshenger.models.User;

public class Message {
    
//private
	private int id;
	private String content;
	private ContentType contentId;
	private Timestamp timeStamp;
    private boolean wasModified;
    private String senderName; //Maurice Senpai brauche ma das hier noch?

	private Thread thread;
	private User sender;

//public

//getter und setter

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public User getSender(){
        return sender;
    }
    public void setSender(User sender){
        this.sender = sender;
    }

    public Thread getThread(){
        return thread;
    }
    public void setThread(Thread thread){
        this.thread = thread;
    }

    public String getContent(){
        return content;
    }
    public void setContent(String content, ContentType type){
		if(content == null || content.isEmpty()){
			content = "empty content";
			type = ContentType.Empty;
		}
        this.content = content;
        contentId = type;
    }

    public ContentType getContentId(){
        return contentId;
    }

    public Timestamp getTimeStamp(){
        return timeStamp;
    }
    public void setTimeStamp(Timestamp timeStamp){
        this.timeStamp = timeStamp;
    }

    public boolean isModified() {
        return wasModified;
    }
    public void setWasModified(boolean wasModified) {
        this.wasModified = wasModified;
    }

    public void setSenderName(String name) {
        this.senderName = name;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getImagePath(String folder){
        return folder + "/img_" + id + ".txt";
    }
}
