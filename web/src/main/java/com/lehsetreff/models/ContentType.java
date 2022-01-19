package com.lehsetreff.models;

public enum ContentType {
    Text(0),
    Image(1),
    Quote(2),
    Link(3);

    private int contentId;

    private ContentType(final int contentId){ this.contentId = contentId;}

    public int getContentId(){
        return contentId;
    }
}
