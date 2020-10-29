package com.tufusi.ohho.ui.publish;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.tufusi.libnavannotation.ActivityDestination;
import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.ActivityPublishBinding;
import com.tufusi.ohho.model.TagList;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPublishBinding mBinding;
    private TagList mTagList;
    private String filePath;
    private int width;
    private int height;
    private boolean isVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_publish);

        mBinding.actionAddFile.setOnClickListener(this);
        mBinding.actionAddTag.setOnClickListener(this);
        mBinding.actionClose.setOnClickListener(this);
        mBinding.actionDeleteFile.setOnClickListener(this);
        mBinding.actionPublish.setOnClickListener(this);
    }

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

    private void deletePublishDraft() {
        mBinding.actionAddFile.setVisibility(View.VISIBLE);
        mBinding.fileContainer.setVisibility(View.GONE);
        mBinding.cover.setImageDrawable(null);
        filePath = null;
        width = 0;
        height = 0;
        isVideo = false;
    }

    private void publish() {

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