package com.tufusi.libcommon;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description 获取全局 Application 对象 做到尽量少侵入
 */
public class AppGlobal {

    private static Application sApplication;

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    public static Application getApplication() {
        if (sApplication == null){
            // 通过 ActivityThread 反射创建
            try {
                Method method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
                sApplication = (Application) method.invoke(null, (Object[]) null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }
}