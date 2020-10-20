package com.tufusi.ohho.utils;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;

import com.tufusi.libcommon.AppGlobal;

/**
 * Created by 鼠夏目 on 2020/10/15.
 *
 * @author 鼠夏目
 * @description
 */
public class TT {

    @SuppressLint("RestrictedApi")
    public static void showToast(String content) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobal.getApplication(), content, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppGlobal.getApplication(), content, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
} 