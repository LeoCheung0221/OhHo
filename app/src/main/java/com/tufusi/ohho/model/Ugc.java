package com.tufusi.ohho.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.tufusi.ohho.BR;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by 鼠夏目 on 2020/9/25.
 *
 * @author 鼠夏目
 * @description
 */
public class Ugc extends BaseObservable implements Serializable {

    /**
     * likeCount : 103
     * shareCount : 10
     * commentCount : 10
     * hasFavorite : false
     * hasLiked : false
     * hasdiss : false
     * hasDissed : false
     */

    public int likeCount;
    public int shareCount;
    public int commentCount;
    public boolean hasFavorite;
    public boolean hasLiked;
    public boolean hasdiss;
    public boolean hasDissed;

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @Bindable
    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
        notifyPropertyChanged(BR._all);
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Bindable
    public boolean isHasFavorite() {
        return hasFavorite;
    }

    public void setHasFavorite(boolean hasFavorite) {
        this.hasFavorite = hasFavorite;
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        if (this.hasLiked == hasLiked) {
            return;
        }
        if (hasLiked) {
            likeCount = likeCount + 1;
            setHasdiss(false);
        } else {
            likeCount = likeCount - 1;
        }
        this.hasLiked = hasLiked;
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public boolean isHasdiss() {
        return hasdiss;
    }

    public void setHasdiss(boolean hasdiss) {
        if (this.hasdiss == hasdiss) {
            return;
        }
        if (hasdiss) {
            setHasLiked(false);
        }
        this.hasdiss = hasdiss;
        notifyPropertyChanged(BR._all);
    }

    public boolean isHasDissed() {
        return hasDissed;
    }

    public void setHasDissed(boolean hasDissed) {
        this.hasDissed = hasDissed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ugc ugc = (Ugc) o;
        return likeCount == ugc.likeCount &&
                shareCount == ugc.shareCount &&
                commentCount == ugc.commentCount &&
                hasFavorite == ugc.hasFavorite &&
                hasLiked == ugc.hasLiked &&
                hasdiss == ugc.hasdiss &&
                hasDissed == ugc.hasDissed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(likeCount, shareCount, commentCount, hasFavorite, hasLiked, hasdiss, hasDissed);
    }
}