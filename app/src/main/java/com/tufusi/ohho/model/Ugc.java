package com.tufusi.ohho.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by 鼠夏目 on 2020/9/25.
 *
 * @author 鼠夏目
 * @description
 */
public class Ugc implements Serializable {

    /**
     * likeCount : 103
     * shareCount : 10
     * commentCount : 10
     * hasFavorite : false
     * hasLiked : false
     * hasdiss : false
     * hasDissed : false
     */

    private int likeCount;
    private int shareCount;
    private int commentCount;
    private boolean hasFavorite;
    private boolean hasLiked;
    private boolean hasdiss;
    private boolean hasDissed;

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isHasFavorite() {
        return hasFavorite;
    }

    public void setHasFavorite(boolean hasFavorite) {
        this.hasFavorite = hasFavorite;
    }

    public boolean isHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public boolean isHasdiss() {
        return hasdiss;
    }

    public void setHasdiss(boolean hasdiss) {
        this.hasdiss = hasdiss;
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