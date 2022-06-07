package com.example.social_media_app.Models;

public class ModelNotifications {
    private String postId, time, myUid, notification, name, type, avatar, hisUid;

    public ModelNotifications() {}

    public ModelNotifications(String postId, String time, String myUid, String notification, String name, String type, String avatar, String hisUid) {
        this.postId = postId;
        this.time = time;
        this.myUid = myUid;
        this.notification = notification;
        this.name = name;
        this.type = type;
        this.avatar = avatar;
        this.hisUid = hisUid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }

    public String getHisUid() {
        return hisUid;
    }

    public void setHisUid(String hisUid) {
        this.hisUid = hisUid;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
