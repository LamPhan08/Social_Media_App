package com.example.social_media_app.Models;

public class ModelComments {
    String uid, comment, commentId, postId, imageComment, commentTime, commentUserAvatar, commentUserName, commentUserEmail;

    public ModelComments() {

    }

    public ModelComments(String uid, String comment, String commentId, String postId, String imageComment, String commentTime, String commentUserAvatar, String commentUserName, String commentUserEmail) {
        this.uid = uid;
        this.comment = comment;
        this.commentId = commentId;
        this.postId = postId;
        this.imageComment = imageComment;
        this.commentTime = commentTime;
        this.commentUserAvatar = commentUserAvatar;
        this.commentUserName = commentUserName;
        this.commentUserEmail = commentUserEmail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getImageComment() {
        return imageComment;
    }

    public void setImageComment(String imageComment) {
        this.imageComment = imageComment;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public String getCommentUserAvatar() {
        return commentUserAvatar;
    }

    public void setCommentUserAvatar(String commentUserAvatar) {
        this.commentUserAvatar = commentUserAvatar;
    }

    public String getCommentUserName() {
        return commentUserName;
    }

    public void setCommentUserName(String commentUserName) {
        this.commentUserName = commentUserName;
    }

    public String getCommentUserEmail() {
        return commentUserEmail;
    }

    public void setCommentUserEmail(String commentUserEmail) {
        this.commentUserEmail = commentUserEmail;
    }
}
