package com.example.uzimaemployee.Constants;

public class NotificationsConstructor extends NotificationId {

    String from , description , myId;


    public NotificationsConstructor(){



    }

    public NotificationsConstructor(String from, String description, String myId) {
        this.from = from;
        this.description = description;
        this.myId = myId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }
}
