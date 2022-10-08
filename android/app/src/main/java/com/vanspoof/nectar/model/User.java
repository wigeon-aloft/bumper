package com.vanspoof.nectar.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String email;
    private int userId;
    private String token;
    private String username;
    private String displayname;

    public User(JSONObject userJSON, String token) {
        try {
            this.userId = userJSON.getInt("user_id");
            this.token = token; // fixme shouldn't token be part of the JSON response? (i.e. part of the login/create user response)
            this.email = userJSON.getString("email");
            this.username = userJSON.getString("username");
            this.displayname = ""; // need to add display name column to db
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
