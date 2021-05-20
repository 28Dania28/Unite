package com.dania.unite;

public class MsgServer {
    private String userid;
    private String msg;
    private String type;
    private String time;
    private String date;
    private String push_key;

    public String getPush_key() {
        return push_key;
    }

    public void setPush_key(String push_key) {
        this.push_key = push_key;
    }

    private String status;

    public MsgServer() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public MsgServer(String userid,String msg,String type, String time, String date, String status, String push_key) {
        this.userid = userid;
        this.time = time;
        this.date = date;
        this.msg = msg;
        this.type = type;
        this.status = status;
        this.push_key = push_key;
    }
}
