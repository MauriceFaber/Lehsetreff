package com.lehsetreff.models;

import java.util.ArrayList;
import java.util.List;

public class ThreadGroup {
    
    private String caption;
    private int threadGroupId;
    private List<Thread> threads = new ArrayList<Thread>();

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

    public int getThreadGroupId() {
        return threadGroupId;
    }

    public void setThreadGroupId(int threadGroupId) {
        this.threadGroupId = threadGroupId;
    }
    
}
