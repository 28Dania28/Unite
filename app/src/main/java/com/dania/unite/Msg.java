package com.dania.unite;

public class Msg {

    public Msg() {

    }

    private String seen;
    private String msg;
    private String time;
    private String date;
    private String status;
    private String type;
    private String s_or_r;
    private String push_key;

    public Msg(String seen, String msg, String time, String date, String status, String type, String s_or_r, String push_key) {
        this.seen = seen;
        this.msg = msg;
        this.time = time;
        this.date = date;
        this.status = status;
        this.type = type;
        this.s_or_r = s_or_r;
        this.push_key = push_key;
    }

    public String getPush_key() {
        return push_key;
    }

    public void setPush_key(String push_key) {
        this.push_key = push_key;
    }

    public String getS_or_r() {
        return s_or_r;
    }

    public void setS_or_r(String s_or_r) {
        this.s_or_r = s_or_r;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
}
