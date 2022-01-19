package com.lehsetreff.models;

import java.sql.Timestamp;

public class Message {
    
//private
	private int id;
	private int senderId;
	private int threadId;
	private String content;
	private int contentId;
	private Timestamp timeStamp;
    private boolean wasModified;
    private String senderName; //Maurice Senpai brauche ma das hier noch?



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

    public int getThreadId(){
        return threadId;
    }
    public void setThreadId(int id){
        this.threadId = id;
    }

    public String getContent(){
        return content;
    }
    public void setContent(String content, int type){
        this.content = content;
        contentId = type;
    }

    public int getContentId(){
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
