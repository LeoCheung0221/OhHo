package com.tufusi.ohho.ui.find;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tufusi.libcommon.ext.AbsPagedListAdapter;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.libcommon.utils.StatusBarUtils;
import com.tufusi.libcommon.view.EmptyView;
import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.ActivityTagFeedListBinding;
import com.tufusi.ohho.databinding.LayoutTagFeedListHeaderBinding;
import com.tufusi.ohho.exoplayer.PageListPlayerDetector;
import com.tufusi.ohho.exoplayer.PageListPlayerManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.ui.home.FeedAdapter;

public class TagFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnRefreshListener, OnLoadMoreListener {

    public static final String KEY_TAG_LIST = "tag_list";
    public static final String KEY_FEED_TYPE = "tag_feed_list";
    private ActivityTagFeedListBinding mBinding;
    private RecyclerView recyclerView;
    private EmptyView emptyView;
    private SmartRefreshLayout refreshLayout;
    private AbsPagedListAdapter adapter;
    private PageListPlayerDetector playDetector;
    private boolean shouldPause = true;
    private TagList tagList;
    private TagFeedListViewModel tagFeedListViewModel;
    private int totalScrollY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.fitSystemBar(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tag_feed_list);
        recyclerView = mBinding.refreshLayout.recyclerView;
        emptyView = mBinding.refreshLayout.emptyView;
        refreshLayout = mBinding.refreshLayout.refreshLayout;
        mBinding.actionBack.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = (AbsPagedListAdapter) getAdapter();
        recyclerView.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_divider));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(null);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadMoreListener(this);

        tagList = (TagList) getIntent().getSerializableExtra(KEY_TAG_LIST);
        mBinding.setTagList(tagList);
        mBinding.setOwner(this);

        tagFeedListViewModel = ViewModelProviders.of(this).get(TagFeedListViewModel.class);
        tagFeedListViewModel.setFeedType(tagList.getTitle());
        tagFeedListViewModel.getPageData().observe(this, feeds -> submitList(feeds));
        tagFeedListViewModel.getBoundaryPageData().observe(this, hasData -> finishRefresh(hasData));

        playDetector = new PageListPlayerDetector(this, recyclerView);

        addHeaderView();
    }

    private void addHeaderView() {
        LayoutTagFeedListHeaderBinding headerBinding = LayoutTagFeedListHeaderBinding.inflate(LayoutInflater.from(this), recyclerView, false);
        headerBinding.setTagList(tagList);
        headerBinding.setOwner(this);
        adapter.addHeaderView(headerBinding.getRoot());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScrollY += dy;
                boolean overHeight = totalScrollY > ScreenUtils.dip2px(48);
                mBinding.tagLogo.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                mBinding.tagTitle.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                mBinding.topBarFollow.setVisibility(overHeight ? View.VISIBLE : View.GONE);
                mBinding.actionBack.setImageResource(overHeight ? R.drawable.icon_back_black : R.drawable.icon_back_white);
                mBinding.topBar.setBackgroundColor(overHeight ? Color.WHITE : Color.TRANSPARENT);
                mBinding.topLine.setVisibility(overHeight ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void finishRefresh(Boolean hasData) {
        PagedList currentList = adapter.getCurrentList();
        hasData = currentList != null && currentList.size() > 0 || hasData;

        if (hasData) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }

        RefreshState state = refreshLayout.getState();
        if (state.isOpening && state.isHeader) {
            refreshLayout.finishRefresh();
        } else if (state.isOpening && state.isFooter) {
            refreshLayout.finishLoadMore();
        }
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            adapter.submitList(feeds);
        }
        finishRefresh(feeds.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        tagFeedListViewModel.getDataSource().invalidate();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList currentList = getAdapter().getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    public PagedListAdapter getAdapter() {
        return new FeedAdapter(this, KEY_FEED_TYPE) {
            @Override
            public void onViewAttachedToWindow2(FeedViewHolder holder) {
                if (holder.isVideoType()) {
                    playDetector.addTarget(holder.getPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(FeedViewHolder holder) {
                playDetector.removeTarget(holder.getPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType == Feed.VIDEO_TYPE;
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                // 每提交一次 pageList对象到adapter 就会触发一次
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        recyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        if (shouldPause) {
            playDetector.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playDetector.onResume();
    }

    @Override
    protected void onDestroy() {
        PageListPlayerManager.release(KEY_FEED_TYPE);
        super.onDestroy();
    }

    public static void startActivity(Context context, TagList tagList) {
        Intent intent = new Intent(context, TagFeedListActivity.class);
        intent.putExtra(KEY_TAG_LIST, tagList);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}