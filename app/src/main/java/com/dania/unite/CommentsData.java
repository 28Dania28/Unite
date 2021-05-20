package com.dania.unite;

public class CommentsData {

    public CommentsData(){

    }



    public String comment, date, time, uid, username;
    public long timestamp;

    public CommentsData(String comment, String date, String time, String uid, String username, long timestamp) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.username = username;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
