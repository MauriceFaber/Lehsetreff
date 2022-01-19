package com.lehsetreff.models;

public enum UserRole {
    Guest(0),
    User(1),
    Mod(2),
    Admin(3);

    private int roleId;
    
    private UserRole(final int roleId){ this.roleId = roleId; }

    public int getRoleId(){
        return roleId;
    }
}


