package com.dania.unite;

public class NotificationData {

    private String from,message;

    public NotificationData(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public NotificationData() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
