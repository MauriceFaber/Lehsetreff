package com.lehsetreff.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.meshenger.models.User;

public class Thread implements Serializable {
    
    private static final long serialVersionUID = 2L;

    private String caption;
    private int id;
    private String description;
	private ThreadGroup threadGroup;
	private User owner;
	private List<Message> messages = new ArrayList<Message>();

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getThread() {
        return id;
    }

    public void setThreadId(int threadId) {
        this.id = threadId;
    }

    public ThreadGroup getGroup() {
        return threadGroup;
    }

    public void setGroup(ThreadGroup group) {
        this.threadGroup = group;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

	public List<Message> getMessages(){
		return messages;
	}
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
