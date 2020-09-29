package com.example.uzimaemployee.Constants;

import java.sql.Timestamp;
import java.util.Date;

public class NotificationsConstructor extends NotificationId {

    String from , description , myId;
    public Date timestamp;


    public NotificationsConstructor(){



    }

    public NotificationsConstructor(String from, String description, String myId , Timestamp timestamp) {
        this.from = from;
        this.description = description;
        this.myId = myId;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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
