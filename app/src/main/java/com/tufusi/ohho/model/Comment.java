package com.tufusi.ohho.model;

import java.io.Serializable;

/**
 * Created by 鼠夏目 on 2020/9/24.
 *
 * @author 鼠夏目
 * @description
 */
public class Comment implements Serializable {

    /**
     * id : 1126
     * itemId : 1578976510452
     * commentId : 1579007787804000
     * userId : 1578919786
     * commentType : 1
     * createTime : 1579007787804
     * commentCount : 0
     * likeCount : 1001
     * commentText : 2020他来了，就在眼前了~Happy New Year
     * imageUrl :
     * videoUrl :
     * width : 0
     * height : 0
     * hasLiked : false
     * author : {"id":1250,"userId":1578919786,"name":"、蓅哖╰伊人为谁笑","avatar":"http://qzapp.qlogo.cn/qzapp/101794421/FE41683AD4ECF91B7736CA9DB8104A5C/100","description":"这是一只神秘的jetpack","likeCount":3,"topCommentCount":0,"followCount":0,"followerCount":2,"qqOpenId":"FE41683AD4ECF91B7736CA9DB8104A5C","expires_time":1586695789903,"score":0,"historyCount":222,"commentCount":9,"favoriteCount":0,"feedCount":0,"hasFollow":false}
     * ugc : {"likeCount":103,"shareCount":10,"commentCount":10,"hasFavorite":false,"hasLiked":false,"hasdiss":false,"hasDissed":false}
     */

    private String id;
    private long itemId;
    private long commentId;
    private int userId;
    private int commentType;
    private long createTime;
    private int commentCount;
    private int likeCount;
    private String commentText;
    private String imageUrl;
    private String videoUrl;
    private int width;
    private int height;
    private boolean hasLiked;
    private User author;
    private Ugc ugc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Ugc getUgc() {
        return ugc;
    }

    public void setUgc(Ugc ugc) {
        this.ugc = ugc;
    }
}