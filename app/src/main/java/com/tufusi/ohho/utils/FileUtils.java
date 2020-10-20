package com.tufusi.ohho.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by LeoCheung on 2020/10/18.
 *
 * @author 鼠夏目
 * @description
 */
public class FileUtils {


    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static LiveData<String> generateVideoCover(String filePath) {
        MutableLiveData<String> liveData = new MutableLiveData<>();

        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath);
                Bitmap frame = retriever.getFrameAtTime();

                if (frame != null) {
                    byte[] bytes = compressBitmap(frame, 200);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(bytes);
                        fos.flush();
                        fos.close();
                        fos = null;

                        liveData.postValue(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    liveData.postValue(null);
                }
            }
        });

        return liveData;
    }

    private static byte[] compressBitmap(Bitmap frame, int limit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        frame.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length > limit * 1024) {
            baos.reset();
            options -= 5;
            frame.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }

        byte[] bytes = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        baos = null;
        return bytes;
    }

} 