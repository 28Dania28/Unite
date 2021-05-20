package com.dania.unite;

public class LikesData {

    public LikesData(){

    }

    public String date, time, uid;

    public LikesData(String date, String time, String uid) {
        this.date = date;
        this.time = time;
        this.uid = uid;
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
}
