package com.tufusi.ohho.ui.find;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.ui.AbsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by LeoCheung on 2020/10/31.
 *
 * @author 鼠夏目
 * @description
 */
public class TagListViewModel extends AbsViewModel<TagList> {

    /**
     * 锁住操作，防止自动上拉加载和手动上拉记载冲突导致列表计数有问题
     */
    private AtomicBoolean loadAfter = new AtomicBoolean();
    private int offset;
    private String tagType;
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    @SuppressLint("RestrictedApi")
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback<TagList> callback) {
        // 如果上拉加载无数据
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }

        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ((TagListViewModel.DataSource)getDataSource()).loadData(tagId, callback);
            }
        });
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    private class DataSource extends ItemKeyedDataSource<Long, TagList> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(0L, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            return item.getTagId();
        }

        private void loadData(Long tagId, ItemKeyedDataSource.LoadCallback<TagList> callback) {
            // 上拉加载
            if (tagId > 0) {
                loadAfter.set(true);
            }

            OhResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .addParam("offset", offset)
                    .addParam("pageCount", 20)
                    .addParam("tagId", tagId)
                    .addParam("tagType", tagType)
                    .addParam("userId", UserManager.get().getUserId())
                    .responseRawType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .execute();
            List<TagList> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
            if (tagId > 0) {
                loadAfter.set(false);
                offset += result.size();
                // 通知外部是否可以继续上拉加载
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            } else {
                offset = result.size();
            }
        }
    }


}