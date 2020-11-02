package com.tufusi.ohho.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.Observer;

import com.tufusi.libcommon.dialog.LoadingDialog;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.libcommon.view.ViewHelper;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.databinding.LayoutCommentDialogBinding;
import com.tufusi.ohho.model.Comment;
import com.tufusi.ohho.ui.publish.CaptureActivity;
import com.tufusi.ohho.utils.FileUploadManager;
import com.tufusi.ohho.utils.FileUtils;
import com.tufusi.ohho.utils.TT;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 鼠夏目 on 2020/10/14.
 *
 * @author 鼠夏目
 * @description
 */
public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private static final String KEY_ITEM_ID = "key_item_id";
    private LayoutCommentDialogBinding mBinding;
    private long itemId;
    private String filePath;
    private int width;
    private int height;
    private boolean isVideo;
    private LoadingDialog loadingDialog;
    private String coverUrl;
    private String fileUrl;

    public static CommentDialog newInstance(long itemId) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ITEM_ID, itemId);

        CommentDialog commentDialog = new CommentDialog();
        commentDialog.setArguments(bundle);
        return commentDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() == null) {
            return null;
        }
        Window window = getDialog().getWindow();
        assert window != null;
        window.setWindowAnimations(0);

        mBinding = LayoutCommentDialogBinding.inflate(inflater, window.findViewById(android.R.id.content), false);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        assert getArguments() != null;
        itemId = getArguments().getLong(KEY_ITEM_ID);

        ViewHelper.setViewOutline(mBinding.getRoot(), ScreenUtils.dip2px(10), ViewHelper.RADIUS_TOP);
        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                showSoftInputMethod();
            }
        });

        dismissWhenPressBack();
        return mBinding.getRoot();
    }

    @Override
    public void dismiss() {
        super.dismiss();
//        dismissLoadingDialog();
        filePath = null;
//        fileUrl = null;
//        coverUrl = null;
        isVideo = false;
        width = 0;
        height = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_send:
                publishComment();
                break;
            case R.id.comment_video:
                CaptureActivity.startActivityForResult(getActivity());
                break;
            case R.id.comment_delete:
                filePath = null;
                isVideo = false;
                width = 0;
                height = 0;
                mBinding.commentCover.setImageDrawable(null);
                mBinding.commentExtLayout.setVisibility(View.GONE);

                mBinding.commentVideo.setEnabled(true);
                mBinding.commentVideo.setAlpha(255);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQUEST_CAPTURE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            if (!TextUtils.isEmpty(filePath)) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUrl(filePath);
                if (isVideo) {
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }

            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setAlpha(80);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputView.getText())) {
            return;
        }

        if (isVideo && !TextUtils.isEmpty(filePath)) {
            FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFile(coverPath, filePath);
                }
            });
        } else if (!TextUtils.isEmpty(filePath)) {
            uploadFile(null, filePath);
        } else {
            publish();
        }
    }

    @SuppressLint("RestrictedApi")
    private void uploadFile(String coverPath, String filePath) {
        showLoadingDialog();
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    int remain = count.decrementAndGet();
                    coverUrl = FileUploadManager.upload(coverPath);
                    if (remain <= 0) {
                        if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                            publish();
                        } else {
                            dismissLoadingDialog();
                            TT.showToast(getString(R.string.file_upload_failed));
                        }
                    }
                }
            });
        }

        // 上传视频 必须等有封面url才可发布
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int remain = count.decrementAndGet();
                fileUrl = FileUploadManager.upload(filePath);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        TT.showToast(getString(R.string.file_upload_failed));
                    }
                }
            }
        });
    }

    private void publish() {
        Log.e("publishComment: ", "封面URL:" + (isVideo ? coverUrl : fileUrl) + "    视频URL:\n" + (isVideo ? fileUrl : null));
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("itemId", itemId)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("commentText", commentText)
                .addParam("image_url", isVideo ? coverUrl : fileUrl)
                .addParam("video_url", isVideo ? fileUrl : null)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new ResultCallback<Comment>() {
                    @Override
                    public void onSuccess(OhResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(OhResponse<Comment> response) {
                        TT.showToast("评论失败:" + response.message);
                        dismissLoadingDialog();
                    }
                });
    }

    private void showSoftInputMethod() {
        mBinding.inputView.setFocusable(true);
        mBinding.inputView.setFocusableInTouchMode(true);
        //请求获得焦点
        mBinding.inputView.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(mBinding.inputView, 0);
    }

    private void dismissWhenPressBack() {
        mBinding.inputView.setKeyBackEventListener(() -> {
            mBinding.inputView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 200);
            return true;
        });
    }

    @SuppressLint("RestrictedApi")
    private void onCommentSuccess(Comment body) {
        TT.showToast("评论发布成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (onCommentAddListener != null) {
                    onCommentAddListener.onCommentAdd(body);
                }
                dismiss();
            }
        });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        loadingDialog.setLoadingText(getString(R.string.upload_text));
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                });
            } else if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

    public interface onCommentAddListener {
        void onCommentAdd(Comment comment);
    }

    private onCommentAddListener onCommentAddListener;

    public void setOnCommentAddListener(CommentDialog.onCommentAddListener onCommentAddListener) {
        this.onCommentAddListener = onCommentAddListener;
    }
}