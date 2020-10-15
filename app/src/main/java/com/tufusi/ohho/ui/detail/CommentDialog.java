package com.tufusi.ohho.ui.detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.libcommon.view.ViewHelper;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.databinding.LayoutCommentDialogBinding;
import com.tufusi.ohho.model.Comment;
import com.tufusi.ohho.utils.TT;

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
//        filePath = null;
//        fileUrl = null;
//        coverUrl = null;
//        isVideo = false;
//        width = 0;
//        height = 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_send:
                publishComment();
                break;
            case R.id.comment_delete:

                break;
            case R.id.comment_video:

                break;
            default:
                break;
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputView.getText())) {
            return;
        }
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("itemId", itemId)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("commentText", commentText)
                .addParam("image_url", null)
                .addParam("video_url", null)
                .addParam("width", 0)
                .addParam("height", 0)
                .execute(new ResultCallback<Comment>() {
                    @Override
                    public void onSuccess(OhResponse<Comment> response) {
                        onCommentSuccess(response.body);
                    }

                    @Override
                    public void onError(OhResponse<Comment> respone) {
                        TT.showToast(respone.message);
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

    private void onCommentSuccess(Comment body) {
        TT.showToast("评论发布成功");
        if (onCommentAddListener != null) {
            onCommentAddListener.onCommentAdd(body);
        }
        dismiss();
    }

    public interface onCommentAddListener {
        void onCommentAdd(Comment comment);
    }

    private onCommentAddListener onCommentAddListener;

    public void setOnCommentAddListener(CommentDialog.onCommentAddListener onCommentAddListener) {
        this.onCommentAddListener = onCommentAddListener;
    }
}