package com.lehsetreff.models;

/**
 * Enum fuer Kontent Typen.
 */
public enum ContentType {
    Text(0),
    Image(1),
    Quote(2),
    Link(3),
    DELETED(4);

    private int contentId;

    /**
     * Legt den Kontent Typ fest
     * @param contentId
     * Die ID des Kontents.
     */
    private ContentType(final int contentId){ this.contentId = contentId;}

    //getter und setter
    
    public int getContentId(){
        return contentId;
    }
}
