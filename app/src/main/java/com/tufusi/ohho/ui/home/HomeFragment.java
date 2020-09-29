package com.tufusi.ohho.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsListFragment;
import com.tufusi.ohho.ui.MutableDataSource;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true, needLogin = false)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    @Override
    protected void afterCreateView() {
        mViewModel.getCacheLiveData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                // 更新缓存数据
                mAdapter.submitList(feeds);
            }
        });
    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType);
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
                    MutableDataSource dataSource = new MutableDataSource();
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
}