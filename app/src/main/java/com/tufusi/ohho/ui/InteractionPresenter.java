package com.tufusi.ohho.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.tufusi.libcommon.AppGlobal;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.model.User;
import com.tufusi.ohho.ui.share.ShareDialog;


/**
 * Created by 鼠夏目 on 2020/10/9.
 *
 * @author 鼠夏目
 * @description
 */
public class InteractionPresenter {

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_FEED_SHARE = "/ugc/increaseShareCount";

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
                        }
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppGlobal.getsApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()) {
            return true;
        } else {
            LiveData<User> liveData = UserManager.get().login(AppGlobal.getsApplication());
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

} 