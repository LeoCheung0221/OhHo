package com.tufusi.ohho.ui.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.AppConstants;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private View actionClose, actionLogin;
    private Tencent mTencent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        actionClose = findViewById(R.id.action_close);
        actionLogin = findViewById(R.id.action_login);

        actionClose.setOnClickListener(this);
        actionLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_close) {
            finish();
        } else if (v.getId() == R.id.action_login) {
            login();
        }
    }

    private void login() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(AppConstants.APP_KEY, getApplicationContext());
        }

        mTencent.login(this, "all", mLoginUiListener);
    }

    IUiListener mLoginUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            JSONObject response = (JSONObject) o;

            try {
                String openid = response.getString("openid");
                String access_token = response.getString("access_token");
                String expires_in = response.getString("expires_in");
                long expires_time = response.getLong("expires_time");

                mTencent.setAccessToken(access_token, expires_in);
                mTencent.setOpenId(openid);

                QQToken qqToken = mTencent.getQQToken();
                getUserInfo(qqToken, openid, expires_time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(LoginActivity.this, "登录失败：" + uiError.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
        }
    };

    private void getUserInfo(QQToken qqToken, String openid, long expires_time) {
        UserInfo userInfo = new UserInfo(getApplicationContext(), qqToken);
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                JSONObject response = (JSONObject) o;

                try {
                    String nickname = response.getString("nickname");
                    String avatar = response.getString("figureurl_2");

                    saveInfo(nickname, avatar, openid, expires_time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(LoginActivity.this, "登录失败：" + uiError.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 保存QQ登录信息到服务器
     */
    private void saveInfo(String nickname, String avatar, String openid, long expires_time) {
        ApiService.get("/user/insert")
                .addParam("name", nickname)
                .addParam("avatar", avatar)
                .addParam("expires_time", expires_time)
                .addParam("qqOpenId", openid)
                .execute(new ResultCallback<User>() {
                    @Override
                    public void onSuccess(OhResponse<User> response) {
                        if (response.body != null) {
                            Log.d("QQ授权信息: ", JSON.toJSONString(response.body));

                            UserManager.get().save(response.body);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "登录失败：" + response.message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(OhResponse response) {
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mLoginUiListener);
        }
    }
}