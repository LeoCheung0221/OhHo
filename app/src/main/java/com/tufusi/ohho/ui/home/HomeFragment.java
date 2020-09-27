package com.tufusi.ohho.ui.home;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsListFragment;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true, needLogin = false)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    @Override
    protected void afterCreateView() {

    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }
}