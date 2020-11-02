package com.tufusi.ohho.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.tufusi.libcommon.AppGlobal;
import com.tufusi.libcommon.ext.LiveDataBus;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.Comment;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.model.User;
import com.tufusi.ohho.ui.share.ShareDialog;
import com.tufusi.ohho.utils.TT;


/**
 * Created by 鼠夏目 on 2020/10/9.
 *
 * @author 鼠夏目
 * @description
 */
public class InteractionPresenter {

    public static final String DATA_FROM_INTERACTION = "data_from_interaction";

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_TOGGLE_FEED_FAVORITE = "/ugc/toggleFavorite";

    private static final String URL_TOGGLE_FEED_FOLLOW_USER = "/ugc/toggleUserFollow";

    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    private static final String URL_FEED_SHARE = "/ugc/increaseShareCount";

    private static final String URL_COMMENT_ADD = "/comment/addComment";

    private static final String URL_COMMENT_DELETE = "/comment/deleteComment";

    /**
     * 给帖子点赞/取消点赞  和踩是互斥的操作
     */
    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedLikeInternal(feed);
            }
        })) {
        } else {
            toggleFeedLikeInternal(feed);
        }
    }

    /**
     * 踩/取消踩
     */
    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedDissInternal(feed);
            }
        })) {
        } else {
            toggleFeedDissInternal(feed);
        }
    }

    /**
     * 收藏/取消收藏
     */
    public static void toggleFeedFavorite(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedFavoriteInternal(feed);
            }
        })) {
        } else {
            toggleFeedFavoriteInternal(feed);
        }
    }

    /**
     * 给评论点赞/取消点赞
     */
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleCommentLikeInternal(comment);
            }
        })) {
        } else {
            toggleCommentLikeInternal(comment);
        }
    }

    /**
     * 关注/取消关注
     */
    public static void toggleFollowUser(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFollowUserInternal(feed);
            }
        })) {
        } else {
            toggleFollowUserInternal(feed);
        }
    }

    /**
     * 关注/取消关注 标签
     */
    public static void toggleTagLike(LifecycleOwner owner, TagList tagList) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleTagLikeInternal(tagList);
            }
        })) {

        } else {
            toggleTagLikeInternal(tagList);
        }
    }

    /**
     * 删除帖子
     */
    public static LiveData<Boolean> deleteFeedComment(Context context, long itemId, long commentId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteFeedComment(liveData, itemId, commentId);
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage("确定要删除这条评论吗？")
                .create().show();
        return liveData;
    }

    private static void deleteFeedComment(LiveData liveData, long itemId, long commentId) {
        ApiService.get(URL_COMMENT_DELETE)
                .addParam("commentId", commentId)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean result = response.body.getBooleanValue("result");
                            ((MutableLiveData) liveData).postValue(result);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> respone) {
                        TT.showToast(respone.message);
                    }
                });
    }

    private static void toggleCommentLikeInternal(Comment comment) {
        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.getId())
                .addParam("userId", UserManager.get().getUserId())
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    private static void toggleFollowUserInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_FOLLOW_USER)
                .addParam("followUserId", UserManager.get().getUserId())
                .addParam("userId", feed.getAuthor().getUserId())
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFollow = response.body.getBooleanValue("hasLiked");
                            feed.getAuthor().setHasFollow(hasFollow);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    private static void toggleTagLikeInternal(TagList tagList) {
        ApiService.get("/tag/toggleTagFollow")
                .addParam("tagId", tagList.getTagId())
                .addParam("userId", UserManager.get().getUserId())
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean follow = response.body.getBoolean("hasFollow");
                            tagList.setHasFollow(follow);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        TT.showToast(response.message);
                    }
                });
    }

    private static void toggleFeedFavoriteInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_FAVORITE)
                .addParam("itemId", feed.getItemId())
                .addParam("userId", UserManager.get().getUserId())
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("itemId", feed.getItemId())
                .addParam("userId", UserManager.get().getUserId())
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasdiss(hasLiked);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKE)
                .addParam("itemId", feed.getItemId())
                .addParam("userId", UserManager.get().getUserId())
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasLiked(hasLiked);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    public static void openShare(Context context, Feed feed) {
        String shareContent = feed.getFeeds_text();
        if (!TextUtils.isEmpty(feed.getUrl())) {
            shareContent = feed.getUrl();
        } else if (!TextUtils.isEmpty(feed.getCover())) {
            shareContent = feed.getCover();
        }
        ShareDialog shareDialog = new ShareDialog(context);
        shareDialog.setShareContent(shareContent);
        shareDialog.setShareItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApiService.get(URL_FEED_SHARE)
                        .addParam("itemId", feed.getItemId())
                        .execute(new ResultCallback<JSONObject>() {
                            @Override
                            public void onSuccess(OhResponse<JSONObject> response) {
                                if (response.body != null) {
                                    int count = response.body.getIntValue("count");
                                    feed.getUgc().setShareCount(count);
                                }
                            }

                            @Override
                            public void onError(OhResponse<JSONObject> response) {
                                showToast(response.message);
                            }
                        });
            }
        });

        shareDialog.show();
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppGlobal.getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()) {
            return true;
        } else {
            LiveData<User> liveData = UserManager.get().login(AppGlobal.getApplication());
            if (owner == null) {
                liveData.observeForever(loginObserver(observer, liveData));
            } else {
                liveData.observe(owner, loginObserver(observer, liveData));
            }
            return false;
        }
    }

    private static Observer<User> loginObserver(Observer<User> observer, LiveData<User> liveData) {
        return new Observer<User>() {
            @Override
            public void onChanged(User user) {
                liveData.removeObserver(this);
                if (observer != null && user != null) {
                    observer.onChanged(user);
                }
            }
        };
    }

    public static LiveData<Boolean> deleteFeed(Context context, long itemId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", (dialog, which) -> {
                    dialog.dismiss();
                    deleteFeedInternal(liveData, itemId);
                }).setPositiveButton("取消", (dialog, which) -> dialog.dismiss())
                .setMessage("确定要删除这条评论吗？").create().show();
        return liveData;
    }

    private static void deleteFeedInternal(MutableLiveData<Boolean> liveData, long itemId) {
        ApiService.get("/feeds/deleteFeed")
                .addParam("itemId", itemId)
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean success = response.body.getBoolean("result");
                            liveData.postValue(success);
                            showToast("删除成功");
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }
}