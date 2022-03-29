package com.lehsetreff.models;

/**
 * Enum fuer Benutzer Rolle.
 */
public enum UserRole {
    Guest(0),
    User(1),
    Mod(2),
    Admin(3);

    private int roleId;
    
    /**
     * Legt die Benutzerrolle fest.
     * @param roleId
     * Die ID der Benutzer Rolle.
     */
    private UserRole(final int roleId){ this.roleId = roleId; }

    public int getRoleId(){
        return roleId;
    }
}


