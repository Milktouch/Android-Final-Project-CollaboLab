package com.collabolab.model;

public class SearchUser {
    private String email;
    private String name;
    private String userId;

    public SearchUser(String name, String email, String userId) {
        this.email = email;
        this.name = name;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }
}
