package com.tufusi.ohho.utils;

import com.tufusi.ohho.app.AppGlobal;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description
 */
public class ScreenUtils {

    public static int dip2px(int size) {
        float value = (AppGlobal.getsApplication().getResources().getDisplayMetrics().density * size) + 0.5f;
        return (int) value;
    }

}