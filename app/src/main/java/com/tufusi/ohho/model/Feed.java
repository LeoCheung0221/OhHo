package com.tufusi.ohho.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by 鼠夏目 on 2020/9/24.
 *
 * @author 鼠夏目
 * @description
 */
public class Feed extends BaseObservable implements Serializable {

    public static final int IMAGE_TEXT_TYPE = 1;
    public static final int VIDEO_TYPE = 2;

    /**
     * id : 428
     * itemId : 1578976510452
     * itemType : 2
     * createTime : 1578977844500
     * duration : 8
     * feeds_text : 2020他来了，就在眼前了
     * authorId : 1578919786
     * activityIcon : null
     * activityText : 2020新年快乐
     * width : 960
     * height : 540
     * url : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/New%20Year%20-%2029212-video.mp4
     * cover : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/2020%E5%B0%81%E9%9D%A2%E5%9B%BE.png
     */

    public long id;
    public long itemId;
    public int itemType;
    public long createTime;
    public int duration;
    public String feeds_text;
    public String authorId;
    public String activityIcon;
    public String activityText;
    public int width;
    public int height;
    public String url;
    public String cover;
    public User author;
    public Ugc ugc;
    public Comment topComment;

    @Bindable
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Bindable
    public Ugc getUgc() {
        if (ugc == null){
            ugc = new Ugc();
        }
        return ugc;
    }

    public void setUgc(Ugc ugc) {
        this.ugc = ugc;
    }

    public Comment getTopComment() {
        return topComment;
    }

    public void setTopComment(Comment topComment) {
        this.topComment = topComment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFeeds_text() {
        return feeds_text;
    }

    public void setFeeds_text(String feeds_text) {
        this.feeds_text = feeds_text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getActivityIcon() {
        return activityIcon;
    }

    public void setActivityIcon(String activityIcon) {
        this.activityIcon = activityIcon;
    }

    public String getActivityText() {
        return activityText;
    }

    public void setActivityText(String activityText) {
        this.activityText = activityText;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feed feed = (Feed) o;
        return id == feed.id &&
                itemId == feed.itemId &&
                itemType == feed.itemType &&
                createTime == feed.createTime &&
                duration == feed.duration &&
                width == feed.width &&
                height == feed.height &&
                Objects.equals(feeds_text, feed.feeds_text) &&
                Objects.equals(authorId, feed.authorId) &&
                Objects.equals(activityIcon, feed.activityIcon) &&
                Objects.equals(activityText, feed.activityText) &&
                Objects.equals(url, feed.url) &&
                Objects.equals(cover, feed.cover) &&
                Objects.equals(author, feed.author) &&
                Objects.equals(ugc, feed.ugc) &&
                Objects.equals(topComment, feed.topComment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, itemType, createTime, duration, feeds_text, authorId, activityIcon, activityText, width, height, url, cover, author, ugc, topComment);
    }
}