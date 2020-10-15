package com.tufusi.ohho.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 鼠夏目 on 2020/10/15.
 *
 * @author 鼠夏目
 * @description
 */
public abstract class MutableItemKeyedDataSource<Key, Value> extends ItemKeyedDataSource<Key, Value> {

    private ItemKeyedDataSource mDataSource;

    /**
     * 必须传入原始类型的 DataSource
     */
    public MutableItemKeyedDataSource(ItemKeyedDataSource dataSource) {
        mDataSource = dataSource;
    }

    public List<Value> data = new ArrayList<>();

    @SuppressLint("RestrictedApi")
    public PagedList<Value> createNewPageList(PagedList.Config config) {
        return new PagedList.Builder(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> params, @NonNull LoadInitialCallback<Value> callback) {
        callback.onResult(data);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        if (mDataSource != null) {
            mDataSource.loadAfter(params, callback);
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        callback.onResult(Collections.emptyList());
    }

    @NotNull
    @Override
    public abstract Key getKey(@NonNull Value item);
}