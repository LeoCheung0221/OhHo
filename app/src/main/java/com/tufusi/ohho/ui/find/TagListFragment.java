package com.tufusi.ohho.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tufusi.ohho.R;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.ui.AbsListFragment;
import com.tufusi.ohho.ui.MutableItemKeyedDataSource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by LeoCheung on 2020/10/31.
 *
 * @author 鼠夏目
 * @description
 */
public class TagListFragment extends AbsListFragment<TagList, TagListViewModel> {

    public static final String KEY_TAG_TYPE = "tag_type";
    private String tagType;

    public static TagListFragment newInstance(String tagType) {
        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        TagListFragment fragment = new TagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (TextUtils.equals(tagType,"onlyFollow")){
            mEmptyView.setTitle(getString(R.string.tag_list_no_follow));
            mEmptyView.setAction(getString(R.string.tag_list_no_follow_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewModel.getSwitchLiveData().setValue(new Object());
                }
            });
        }
        mRecyclerView.removeItemDecorationAt(0);
        mViewModel.setTagType(tagType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        assert getArguments() != null;
        tagType = getArguments().getString(KEY_TAG_TYPE);
        TagListAdapter adapter = new TagListAdapter(getContext());
        return adapter;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<TagList> currentList = getAdapter().getCurrentList();
        long lastTagId = currentList == null ? 0L : currentList.get(currentList.size() - 1).getTagId();
        mViewModel.loadData(lastTagId, new ItemKeyedDataSource.LoadCallback<TagList>() {
            @Override
            public void onResult(@NonNull List<TagList> data) {
                if (data.size() > 0) {
                    // 重组数据发送
                    MutableItemKeyedDataSource<Long, TagList> mutableItemKeyedDataSource =
                            new MutableItemKeyedDataSource<Long, TagList>((ItemKeyedDataSource) mViewModel.getDataSource()) {
                                @NotNull
                                @Override
                                public Long getKey(@NonNull TagList item) {
                                    return item.getTagId();
                                }
                            };
                    mutableItemKeyedDataSource.data.addAll(currentList);
                    mutableItemKeyedDataSource.data.addAll(data);
                    PagedList<TagList> newPageList = mutableItemKeyedDataSource.createNewPageList(currentList.getConfig());
                    submitList(newPageList);
                }else{
                    finishRefresh(false);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }
}