package com.lehsetreff.models;

import java.sql.Timestamp;

import com.meshenger.models.User;

/**
 * Stellt eine Nachricht dar.
 */
public class Message {
    
	private int id;
	private String content;
	private ContentType contentId;
	private Timestamp timeStamp;
    private boolean wasModified;

	private String additional;
	private Thread thread;
	private User sender;

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

    public String getImagePath(String folder){
        return folder + "/img_" + id + ".txt";
    }

	public String getAdditional(){
		return additional;
	}

	public void setAdditional(String additional){
		this.additional = additional;
	}
}
