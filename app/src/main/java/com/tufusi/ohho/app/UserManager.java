package com.tufusi.ohho.app;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tufusi.libcommon.AppGlobal;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.libnetwork.cache.CacheManager;
import com.tufusi.ohho.model.User;
import com.tufusi.ohho.ui.login.LoginActivity;
import com.tufusi.ohho.utils.TT;

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

    private UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null && cache.getExpires_time() > System.currentTimeMillis()) {
            mUser = cache;
        }
    }

    public static UserManager get() {
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return userLiveData;
    }

    public boolean isLogin() {
        return mUser != null && mUser.getExpires_time() > System.currentTimeMillis();
    }

    public User getUser() {
        return isLogin() ? mUser : null;
    }

    public long getUserId() {
        return isLogin() ? mUser.getUserId() : 0L;
    }

    public LiveData<User> refresh() {
        if (!isLogin()) {
            return login(AppGlobal.getApplication());
        }
        MutableLiveData<User> liveData = new MutableLiveData<>();
        ApiService.get("/user/query")
                .addParam("userId", getUserId())
                .execute(new ResultCallback<User>() {
                    @Override
                    public void onSuccess(OhResponse<User> response) {
                        save(response.body);
                        liveData.postValue(getUser());
                    }

                    @Override
                    public void onError(OhResponse<User> response) {
                        TT.showToast(response.message);
                        liveData.postValue(null);
                    }
                });
        return liveData;
    }

    public void logout() {
        CacheManager.delete(KEY_CACHE_USER, mUser);
        mUser = null;
    }
}