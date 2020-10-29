package com.tufusi.ohho.ui.detail;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.libcommon.view.EmptyView;
import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.tufusi.ohho.model.Comment;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.MutableItemKeyedDataSource;

import org.jetbrains.annotations.NotNull;

/**
 * Created by 鼠夏目 on 2020/10/13.
 *
 * @author 鼠夏目
 * @description
 */
public abstract class ViewHandler {

    private FeedDetailViewModel viewModel;
    protected FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailBottomInteractionBinding mInteractionBinding;
    protected FeedCommentAdapter adapter;

    private EmptyView mEmptyView;
    private CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity) {
        mActivity = activity;

        // 得到FeedDetailViewModel实例对象
        viewModel = ViewModelProviders.of(activity).get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
        mInteractionBinding.setOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        adapter = new FeedCommentAdapter(mActivity);
        mRecyclerView.setAdapter(adapter);

        viewModel.setItemId(mFeed.getItemId());
        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                adapter.submitList(comments);
                handlerEmpty(comments.size() > 0);
            }
        });

        mInteractionBinding.inputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog();
            }
        });
    }

    protected void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = CommentDialog.newInstance(mFeed.getItemId());
        }
        commentDialog.setOnCommentAddListener(new CommentDialog.onCommentAddListener() {
            @Override
            public void onCommentAdd(Comment comment) {
                handlerEmpty(true);
                adapter.addAndRefreshList(comment);
            }
        });
        commentDialog.show(mActivity.getSupportFragmentManager(), "comment_dialog");
    }

    protected void handlerEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                adapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = ScreenUtils.dip2px(40);
                mEmptyView.setLayoutParams(layoutParams);
                mEmptyView.setTitle(mActivity.getString(R.string.string_comment_empty_hint));
            }
            adapter.addHeaderView(mEmptyView);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (commentDialog != null && commentDialog.isAdded()) {
            commentDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onBackPressed() {

    }
}