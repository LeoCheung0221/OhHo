package com.tufusi.libcommon.utils;

import android.util.DisplayMetrics;

import com.tufusi.libcommon.AppGlobal;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description
 */
public class ScreenUtils {

    public static int dip2px(int size) {
        float value = (AppGlobal.getApplication().getResources().getDisplayMetrics().density * size) + 0.5f;
        return (int) value;
    }

    public static int getScreenWidth() {
        DisplayMetrics metrics = AppGlobal.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics metrics = AppGlobal.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

}