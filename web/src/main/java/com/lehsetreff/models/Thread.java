package com.lehsetreff.models;

import java.io.Serializable;

public class Thread implements Serializable {
    
    private static final long serialVersionUID = 2L;

    private int ownerId;
    private int groupId;
    private String caption;
    
    

    public java.sql.Timestamp latestMessage;
	public java.sql.Timestamp latestUserMessage;

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
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
    
}
