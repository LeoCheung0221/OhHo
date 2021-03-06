package com.tufusi.ohho.ui.my;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tufusi.ohho.exoplayer.PageListPlayerDetector;
import com.tufusi.ohho.exoplayer.PageListPlayerManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsListFragment;
import com.tufusi.ohho.ui.home.FeedAdapter;

/**
 * Created by LeoCheung on 2020/11/2.
 *
 * @author 鼠夏目
 * @description
 */
public class UserBehaviorListFragment extends AbsListFragment<Feed, UserBehaviorViewModel> {

    private static final String CATEGORY = "user_behavior_list";
    private PageListPlayerDetector playDetector;
    private boolean shouldPause = true;

    public static UserBehaviorListFragment newInstance(int behavior) {
        Bundle args = new Bundle();
        args.putInt(UserBehaviorListActivity.KEY_BEHAVIOR, behavior);
        UserBehaviorListFragment fragment = new UserBehaviorListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playDetector = new PageListPlayerDetector(this, mRecyclerView);
        assert getArguments() != null;
        int behavior = getArguments().getInt(UserBehaviorListActivity.KEY_BEHAVIOR);
        mViewModel.setBehavior(behavior);
    }

    @Override
    public PagedListAdapter getAdapter() {
        return new FeedAdapter(getContext(), CATEGORY) {
            @Override
            public void onViewAttachedToWindow2(FeedViewHolder holder) {
                if (holder.isVideoType()) {
                    playDetector.addTarget(holder.playerView);
                }
            }

            @Override
            public void onViewDetachedFromWindow2(FeedViewHolder holder) {
                if (holder.isVideoType()) {
                    playDetector.removeTarget(holder.playerView);
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                shouldPause = false;
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = mAdapter.getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPause) {
            playDetector.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        playDetector.onResume();
    }

    @Override
    public void onDestroyView() {
        PageListPlayerManager.release(CATEGORY);
        super.onDestroyView();
    }

}