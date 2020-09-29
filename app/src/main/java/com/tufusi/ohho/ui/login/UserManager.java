package com.tufusi.ohho.ui.login;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tufusi.cache.CacheManager;
import com.tufusi.ohho.model.User;

/**
 * Created by 鼠夏目 on 2020/9/29.
 *
 * @author 鼠夏目
 * @description
 */
public class UserManager {

    private static final String KEY_CACHE_USER = "cache_user";
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();

    private static volatile UserManager singleton = null;
    private User mUser;

    public static UserManager get() {
        return singleton;
    }

    private UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null && cache.getExpires_time() < System.currentTimeMillis()) {
            mUser = cache;
        }
    }

    public static UserManager getInstance() {
        if (singleton == null) {
            synchronized (UserManager.class) {
                if (singleton == null) {
                    singleton = new UserManager();
                }
            }
        }
        return singleton;
    }

    public void save(User user) {
        mUser = user;
        CacheManager.save(KEY_CACHE_USER, user);
        if (userLiveData.hasObservers()) {
            userLiveData.postValue(user);
        }
    }

    public LiveData<User> login(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        return userLiveData;
    }

    public boolean isLogin() {
        return mUser != null && mUser.getExpires_time() < System.currentTimeMillis();
    }

    public User getUser() {
        return isLogin() ? mUser : null;
    }

    public long getUserId() {
        return isLogin() ? mUser.getUserId() : 0L;
    }
} 