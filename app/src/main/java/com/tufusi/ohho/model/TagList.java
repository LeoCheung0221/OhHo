package com.tufusi.ohho.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.tufusi.ohho.BR;

import java.util.Objects;

/**
 * Created by LeoCheung on 2020/10/28.
 *
 * @author 鼠夏目
 * @description
 */
public class TagList extends BaseObservable {

    /**
     * id : 61
     * icon : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/icon_etpack.png
     * background : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/1111.png
     * activityIcon : https://pipijoke.oss-cn-hangzhou.aliyuncs.com/51f076f662ef40c99f056a41b130c516.png
     * title : 2019高光时刻
     * intro : 2019年那些事,有哪些最让你怀念呢？
     * feedNum : 100
     * tagId : 1
     * enterNum : 1000
     * followNum : 100
     * hasFollow : false
     */

    private long id;
    private String icon;
    private String background;
    private String activityIcon;
    private String title;
    private String intro;
    private int feedNum;
    private long tagId;
    private int enterNum;
    private int followNum;
    private boolean hasFollow;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getActivityIcon() {
        return activityIcon;
    }

    public void setActivityIcon(String activityIcon) {
        this.activityIcon = activityIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getFeedNum() {
        return feedNum;
    }

    public void setFeedNum(int feedNum) {
        this.feedNum = feedNum;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public int getEnterNum() {
        return enterNum;
    }

    public void setEnterNum(int enterNum) {
        this.enterNum = enterNum;
    }

    public int getFollowNum() {
        return followNum;
    }

    public void setFollowNum(int followNum) {
        this.followNum = followNum;
    }

    @Bindable
    public boolean isHasFollow() {
        return hasFollow;
    }

    public void setHasFollow(boolean hasFollow) {
        this.hasFollow = hasFollow;
        notifyPropertyChanged(BR._all);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagList tagList = (TagList) o;
        return id == tagList.id &&
                feedNum == tagList.feedNum &&
                tagId == tagList.tagId &&
                enterNum == tagList.enterNum &&
                followNum == tagList.followNum &&
                hasFollow == tagList.hasFollow &&
                Objects.equals(icon, tagList.icon) &&
                Objects.equals(background, tagList.background) &&
                Objects.equals(activityIcon, tagList.activityIcon) &&
                Objects.equals(title, tagList.title) &&
                Objects.equals(intro, tagList.intro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, icon, background, activityIcon, title, intro, feedNum, tagId, enterNum, followNum, hasFollow);
    }
}