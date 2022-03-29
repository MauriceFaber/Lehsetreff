package com.lehsetreff.models;

import java.util.ArrayList;
import java.util.List;

import com.meshenger.models.User;

/**
 * Stellt eine ThreadGruppe dar.
 */

public class ThreadGroup {
    
    private String caption;
    private int id;
    private User owner;
    private List<Thread> threads = new ArrayList<Thread>();
    private String description;

    //getter und setter

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<Thread> getThreads(){
		return threads;
	}
	public void setMembers(List<Thread> threads){
		this.threads = threads;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
