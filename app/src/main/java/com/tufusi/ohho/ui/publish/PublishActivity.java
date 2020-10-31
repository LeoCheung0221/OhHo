package com.tufusi.ohho.ui.publish;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.tufusi.libcommon.dialog.LoadingDialog;
import com.tufusi.libcommon.utils.StatusBarUtils;
import com.tufusi.libnavannotation.ActivityDestination;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.databinding.ActivityPublishBinding;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.utils.FileUploadWorker;
import com.tufusi.ohho.utils.FileUtils;
import com.tufusi.ohho.utils.TT;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPublishBinding mBinding;
    private TagList mTagList;
    private String filePath;
    private int width;
    private int height;
    private boolean isVideo;
    private LoadingDialog mLoadingDialog;
    private String coverFilePath;
    private UUID coverUploadUUID;
    private UUID fileUploadUUID;
    private String coverUploadUrl, fileUploadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_publish);

        mBinding.actionAddFile.setOnClickListener(this);
        mBinding.actionAddTag.setOnClickListener(this);
        mBinding.actionClose.setOnClickListener(this);
        mBinding.actionDeleteFile.setOnClickListener(this);
        mBinding.actionPublish.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_add_tag:
                showTagBottomDialog();
                break;
            case R.id.action_add_file:
                CaptureActivity.startActivityForResult(this);
                break;
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.action_delete_file:
                deletePublishDraft();
                break;
            case R.id.action_publish:
                publish();
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void publish() {
        showLoading();

        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (!TextUtils.isEmpty(filePath)) {
            if (isVideo) {
                // 生成视频封面文件
                FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String coverPath) {
                        coverFilePath = coverPath;

                        OneTimeWorkRequest coverRequest = getOneTimeWorkRequest(coverPath);
                        coverUploadUUID = coverRequest.getId();
                        workRequests.add(coverRequest);

                        enqueue(workRequests);
                    }
                });
            }
            OneTimeWorkRequest request = getOneTimeWorkRequest(filePath);
            fileUploadUUID = request.getId();
            workRequests.add(request);

            // 如果是视频文件则需要等待封面文件生成之后再统一提交任务队列
            // 否则如果是图片类型的，则直接提交
            if (!isVideo) {
                enqueue(workRequests);
            }
        } else {
            //  纯文本类型的帖子提交
            publishFeed();
        }
    }

    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation = WorkManager.getInstance(this).beginWith(workRequests);
        workContinuation.enqueue();

        workContinuation.getWorkInfosLiveData().observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // 成功任务数
                int completedCount = 0;
                // 失败任务数
                int failedCount = 0;

                for (WorkInfo workInfo : workInfos) {
                    WorkInfo.State state = workInfo.getState();
                    Data outputData = workInfo.getOutputData();
                    UUID uuid = workInfo.getId();

                    if (state == WorkInfo.State.SUCCEEDED) {
                        String fileUrl = outputData.getString("fileUrl");
                        if (uuid.equals(coverUploadUUID)) {
                            coverUploadUrl = fileUrl;
                        } else if (uuid.equals(fileUploadUUID)) {
                            fileUploadUrl = fileUrl;
                        }
                        completedCount++;
                    } else if (state == WorkInfo.State.FAILED) {
                        if (uuid.equals(coverUploadUUID)) {
                            TT.showToast(getString(R.string.file_upload_cover_message));
                        } else if (uuid.equals(fileUploadUUID)) {
                            TT.showToast(getString(R.string.file_upload_original_message));
                        }
                        failedCount++;
                    }
                }

                if (completedCount >= workInfos.size()) {
                    publishFeed();
                } else if (failedCount > 0) {
                    dismissLoading();
                }
            }
        });
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl", coverUploadUrl)
                .addParam("fileUrl", fileUploadUrl)
                .addParam("fileWidth", width)
                .addParam("fileHeight", height)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", mTagList == null ? 0 : mTagList.getTagId())
                .addParam("tagTitle", mTagList == null ? "" : mTagList.getTitle())
                .addParam("feedText", mBinding.inputView.getText().toString())
                .addParam("feedType", isVideo ? Feed.VIDEO_TYPE : Feed.IMAGE_TEXT_TYPE)
                .execute(new ResultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(OhResponse<JSONObject> response) {
                        TT.showToast(getString(R.string.feed_publish_success));
                        PublishActivity.this.finish();
                        dismissLoading();
                    }

                    @Override
                    public void onError(OhResponse<JSONObject> response) {
                        TT.showToast(response.message);
                        dismissLoading();
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    private OneTimeWorkRequest getOneTimeWorkRequest(String filePath) {
        Data inputData = new Data.Builder()
                .putString("file", filePath)
                .build();

//        Constraints constraints = new Constraints();
//        //设备存储空间充足的时候 才能执行 ,>15%
//        constraints.setRequiresStorageNotLow(true);
//        //必须在指定的网络条件下才能够执行,不计流量 ,wifi
//        constraints.setRequiredNetworkType(NetworkType.UNMETERED);
//        //设备的充电量充足的才能执行 >15%
//        constraints.setRequiresBatteryNotLow(true);
//        //只有设备在充电的情况下 才能允许执行
//        constraints.setRequiresCharging(true);
//        //只有设备在空闲的情况下才能被执行 比如息屏，cpu利用率不高
//        constraints.setRequiresDeviceIdle(true);
//        //WorkManager利用contentObserver监控传递进来的这个uri对应的内容是否发生变化,当且仅当它发生变化了
//        //我们的任务才会被触发执行，以下三个api是关联的
//        constraints.setContentUriTriggers(null);
//        //设置从content变化到被执行中间的延迟时间，如果在这期间。content发生了变化，延迟时间会被重新计算
//        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerContentUpdateDelay(0);
//        //设置从content变化到被执行中间的最大延迟时间
//        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerMaxContentDelay(0);

        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(FileUploadWorker.class)
                .setInputData(inputData)
//                .setConstraints(constraints)
//                //设置一个拦截器，在任务执行之前 可以做一次拦截，去修改入参的数据然后返回新的数据交由worker使用
//                .setInputMerger(null)
//                //当一个任务被调度失败后，所要采取的重试策略，可以通过BackoffPolicy来执行具体的策略
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
//                //任务被调度执行的延迟时间
//                .setInitialDelay(10, TimeUnit.SECONDS)
//                //设置该任务尝试执行的最大次数
//                .setInitialRunAttemptCount(2)
//                //设置这个任务开始执行的时间
//                //System.currentTimeMillis()
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
//                //指定该任务被调度的时间
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
//                //当一个任务执行状态编程finish时，又没有后续的观察者来消费这个结果，难么WorkManager会在
//                //内存中保留一段时间的该任务的结果。超过这个时间，这个结果就会被存储到数据库中
//                //下次想要查询该任务的结果时，会触发WorkManager的数据库查询操作，可以通过uuid来查询任务的状态
//                .keepResultsForAtLeast(10, TimeUnit.SECONDS)
                .build();
        return request;
    }

    private void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setLoadingText(getString(R.string.feed_publish_ing));
        }
        mLoadingDialog.show();
    }

    private void dismissLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } else {
            runOnUiThread(() -> {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            });
        }
    }

    private void deletePublishDraft() {
        mBinding.actionAddFile.setVisibility(View.VISIBLE);
        mBinding.fileContainer.setVisibility(View.GONE);
        mBinding.cover.setImageDrawable(null);
        filePath = null;
        width = 0;
        height = 0;
        isVideo = false;
    }

    private void showTagBottomDialog() {
        TagBottomSheetDialogFragment sheetDialogFragment = new TagBottomSheetDialogFragment();
        sheetDialogFragment.setOnTagItemSelectedListener(new TagBottomSheetDialogFragment.OnTagItemSelectedListener() {
            @Override
            public void onTagItemSelected(TagList tagList) {
                mTagList = tagList;
                mBinding.actionAddTag.setText(tagList.getTitle());
            }
        });
        sheetDialogFragment.show(getSupportFragmentManager(), "tag_dialog");
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.publish_exit_message))
                .setNegativeButton(getString(R.string.publish_exit_action_cancel), null)
                .setPositiveButton(getString(R.string.publish_exit_action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CaptureActivity.REQUEST_CAPTURE_CODE && data != null) {
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        mBinding.actionAddFile.setVisibility(View.GONE);
        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        mBinding.cover.setImageUrl(filePath);
        mBinding.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.startActivityForResult(PublishActivity.this, filePath, isVideo, null);
            }
        });
    }
}