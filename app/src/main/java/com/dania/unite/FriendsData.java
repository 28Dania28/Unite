package com.dania.unite;

public class FriendsData {

    private String id;
    private String username;
    private String imageurl;

    public FriendsData(String id, String username, String imageurl) {
        this.id = id;
        this.username = username;
        this.imageurl = imageurl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public FriendsData(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
