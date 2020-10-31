package com.tufusi.ohho.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Created by LeoCheung on 2020/10/30.
 *
 * @author 鼠夏目
 * @description
 */
public class FileUploadWorker extends Worker {

    public FileUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inoutData = getInputData();
        String filePath = inoutData.getString("file");
        String fileUrl = FileUploadManager.upload(filePath);
        if (TextUtils.isEmpty(fileUrl)) {
            return Result.failure();
        } else {
            Data data = new Data.Builder().putString("fileUrl", fileUrl).build();
            return Result.success(data);
        }
    }
}