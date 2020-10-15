package com.tufusi.ohho.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 鼠夏目 on 2020/9/29.
 *
 * @author 鼠夏目
 * @description 自定义继承 PageKeyedDataSource 的数据源对象 提供缓存使用
 */
public class MutablePageKeyedDataSource<Key, Value> extends PageKeyedDataSource<Key, Value> {

    public List<Value> data = new ArrayList<>();

    @SuppressLint("RestrictedApi")
    public PagedList<Value> createNewPageList(PagedList.Config config) {
        return new PagedList.Builder(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> params, @NonNull LoadInitialCallback<Key, Value> callback) {
        // 数据源，前后页码编号
        callback.onResult(data, null, null);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Key, Value> callback) {
        // 传入空数组和页码编号
        callback.onResult(Collections.emptyList(), null);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Key, Value> callback) {
        callback.onResult(Collections.emptyList(), null);
    }
}