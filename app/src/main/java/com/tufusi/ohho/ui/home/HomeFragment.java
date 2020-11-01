package com.tufusi.ohho.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.exoplayer.PageListPlayerDetector;
import com.tufusi.ohho.exoplayer.PageListPlayerManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsListFragment;
import com.tufusi.ohho.ui.MutablePageKeyedDataSource;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true, needLogin = false)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private PageListPlayerDetector playerDetector;
    // tab 类型
    private String feedType;
    // 是否需要暂停视频播放
    private boolean shouldPause = true;

    public static HomeFragment newInstance(String feedType) {

        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        mViewModel.getCacheLiveData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                // 更新缓存数据
                mAdapter.submitList(feeds);
            }
        });

        playerDetector = new PageListPlayerDetector(this, mRecyclerView);
        mViewModel.setFeedType(feedType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType) {
            @Override
            public void onViewAttachedToWindow2(@NonNull FeedViewHolder holder) {
                // 判断是否是视频类型的item
                if (holder.isVideoType()) {
                    playerDetector.addTarget(holder.getPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(@NonNull FeedViewHolder holder) {
                playerDetector.removeTarget(holder.getPlayerView());
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                // 此处方法触发是在每提交一次 pageList 对象到 adapter就会回调
                // 即每调用一次 adapter.submitList()
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                // 点击如果是视频类型，则记录下来
                boolean isVideo = feed.itemType == Feed.VIDEO_TYPE;
                shouldPause = !isVideo;
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = mAdapter.getCurrentList();
        if (currentList == null || currentList.size() <= 0) {
            finishRefresh(false);
            return;
        }
        Feed feed = currentList.get(mAdapter.getItemCount() - 1);
        if (feed == null) {
            finishRefresh(false);
            return;
        }
        mViewModel.loadAfter(feed.getId(), new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<Feed> data) {
                PagedList.Config config = mAdapter.getCurrentList().getConfig();
                if (data.size() > 0) {
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource();
                    dataSource.data.addAll(currentList);
                    dataSource.data.addAll(data);

                    PagedList newPageList = dataSource.createNewPageList(config);
                    submitList(newPageList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //invalidate 之后Paging会重新创建一个DataSource 重新调用它的loadInitial方法加载初始化数据
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            playerDetector.onPause();
        } else {
            playerDetector.onResume();
        }
    }

    @Override
    public void onPause() {
        if (shouldPause){
            playerDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getParentFragment() != null) {
            if (getParentFragment().isVisible() && isVisible()) {
                playerDetector.onResume();
            }
        } else {
            if (isVisible()) {
                playerDetector.onResume();
            }
        }
    }

    @Override
    public void onDestroy() {
        PageListPlayerManager.release(feedType);
        super.onDestroy();
    }
}