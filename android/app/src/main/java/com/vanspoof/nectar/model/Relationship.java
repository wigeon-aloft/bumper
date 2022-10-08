package com.vanspoof.nectar.model;

public class Relationship {
    private User requester;
    private User recipient;
    private String status;

    public Relationship(User requester, User recipient, String status) {
        this.requester = requester;
        this.recipient = recipient;
        this.status = status;
    }

    public User getNonCurrentUser(int userId) {
        if (requester.getUserId() == userId) {
            return recipient;
        } else {
            return requester;
        }
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
