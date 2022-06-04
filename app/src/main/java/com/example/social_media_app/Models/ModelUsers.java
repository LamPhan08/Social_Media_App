package com.example.social_media_app.Models;

public class    ModelUsers {
    private String name, email, bio, avatar, cover, uid, status;

    public ModelUsers() {
    }

    public ModelUsers(String name, String email, String bio, String avatar, String cover, String uid, String status) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.avatar = avatar;
        this.cover = cover;
        this.uid = uid;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}