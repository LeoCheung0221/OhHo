package com.tufusi.ohho.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tufusi.libcommon.view.EmptyView;
import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.LayoutRefreshViewBinding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public abstract class AbsListFragment<T, M extends AbsViewModel> extends Fragment
        implements OnRefreshListener, OnLoadMoreListener {

    protected LayoutRefreshViewBinding binding;
    protected SmartRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected EmptyView mEmptyView;
    protected PagedListAdapter<T, RecyclerView.ViewHolder> mAdapter;
    protected M mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        mRefreshLayout = binding.refreshLayout;
        mRecyclerView = binding.recyclerView;
        mEmptyView = binding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);
        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);

        genericViewModel();
        afterCreateView();

        return binding.getRoot();
    }

    private void genericViewModel() {
        //利用 子类传递的 泛型参数实例化出 AbsViewModel 对象
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        assert type != null;
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClz = ((Class) argument).asSubclass(AbsViewModel.class);
            mViewModel = (M) ViewModelProviders.of(this).get(modelClz);

            // 触发页面初始化数据加载的逻辑
            mViewModel.getPageData().observe(this, new Observer<PagedList<T>>() {
                @Override
                public void onChanged(PagedList<T> pagedList) {
                    mAdapter.submitList(pagedList);
                }
            });

            // 监听数据加载的边界回调 以此控制空布局显示隐藏
            mViewModel.getBoundaryPageData().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean hasData) {
                    finishRefresh(hasData);
                }
            });
        }
    }

    protected abstract void afterCreateView();

    /**
     * 提交数据进行刷新
     *
     * @param pagedList 每次下拉刷新获取的列表对象
     */
    public void submitList(PagedList<T> pagedList) {
        if (pagedList.size() > 0) {
            mAdapter.submitList(pagedList);
        }
        finishRefresh(pagedList.size() > 0);
    }

    public void finishRefresh(boolean hasData) {
        PagedList<T> currentList = mAdapter.getCurrentList();
        hasData = hasData || currentList != null && currentList.size() > 0;
        RefreshState state = mRefreshLayout.getState();

        if (state.isHeader && state.isOpening) {
            Log.e("finishRefresh: ", "下拉刷新完成");
            mRefreshLayout.finishRefresh();
        } else if (state.isFooter && state.isOpening) {
            Log.e("finishRefresh: ", "上拉加载完成");
            mRefreshLayout.finishLoadMore();
        } else {
            Log.e("finishRefresh: ", "上下完成");
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMore();
        }

        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 抽象出适配器 继承 PagedListAdapter
     *
     * @return 适配器
     */
    public abstract PagedListAdapter getAdapter();

}