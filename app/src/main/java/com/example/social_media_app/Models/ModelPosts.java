package com.example.social_media_app.Models;

public class ModelPosts {
    String uid, description, title, userName, userEmail, userAvatar, postLikes, postComments, postTime, postImage, likeRemoveValue;

    public ModelPosts() {}

    public ModelPosts(String uid
            , String description
            , String userName
            , String userEmail
            , String userAvatar
            , String postLikes
            , String postComments
            , String postTime
            , String postImage
            , String likeRemoveValue) {
        this.uid = uid;
        this.description = description;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userAvatar = userAvatar;
        this.postLikes = postLikes;
        this.postComments = postComments;
        this.postTime = postTime;
        this.postImage = postImage;
        this.likeRemoveValue = likeRemoveValue;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getPostLikes() {
        return postLikes;
    }

    public void setPostLikes(String postLikes) {
        this.postLikes = postLikes;
    }

    public String getPostComments() {
        return postComments;
    }

    public void setPostComments(String postComments) {
        this.postComments = postComments;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getLikeRemoveValue() {
        return likeRemoveValue;
    }

    public void setLikeRemoveValue(String likeRemoveValue) {
        this.likeRemoveValue = likeRemoveValue;
    }
}
