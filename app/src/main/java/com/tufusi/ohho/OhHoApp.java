package com.tufusi.ohho;

import android.app.Application;

import com.tufusi.libnetwork.ApiService;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public class OhHoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ApiService.initService("http://344v6u4054.zicp.vip:34141/serverdemo", null);
    }
}