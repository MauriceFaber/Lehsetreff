package com.lehsetreff.models;

public class Link {
    
    private int linkedMessageId;
    private int messageId;


    public int getLinkedMessageId(){
        return linkedMessageId;
    }

    public void setLinkedMessageId(int linkedMessageId) {
        this.linkedMessageId = linkedMessageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

}
