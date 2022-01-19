package com.lehsetreff.models;

import java.util.ArrayList;
import java.util.List;

public class ThreadGroup {
    
    private String Caption;
    private List<Thread> threads = new ArrayList<Thread>();

    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public List<Thread> getThreads(){
		return threads;
	}
	public void setMembers(List<Thread> threads){
		this.threads = threads;
	}
}
