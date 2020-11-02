package com.tufusi.ohho.ui.my;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by LeoCheung on 2020/11/2.
 *
 * @author 鼠夏目
 * @description
 */
public class UserBehaviorViewModel extends AbsViewModel<Feed> {

    private int mBehavior;

    public void setBehavior(int behavior) {
        this.mBehavior = behavior;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    private class DataSource extends ItemKeyedDataSource<Long, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(0L, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull Feed item) {
            return item.getId();
        }
    }

    private void loadData(Long feedId, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        OhResponse<List<Feed>> response = ApiService.get("/feeds/queryUserBehaviorList")
                .addParam("behavior", mBehavior)
                .addParam("feedId", feedId)
                .addParam("pageCount", 10)
                .addParam("userId", UserManager.get().getUserId())
                .responseRawType(new TypeReference<ArrayList<Feed>>() {
                }.getType())
                .execute();

        List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
        callback.onResult(result);

        if (feedId > 0) {
            getBoundaryPageData().postValue(result.size() > 0);
        }
    }
}