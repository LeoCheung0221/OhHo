package com.tufusi.ohho.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhRequest;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean withCache = true;

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }


    /**
     * 首页加载 先加载缓存，再加载网络，随后更新本地缓存
     */
    ItemKeyedDataSource<Long, Feed> mDataSource = new ItemKeyedDataSource<Long, Feed>() {

        // 加载初始化数据时调用
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Feed> callback) {
            Log.e("homeviewmodel", "loadInitial: ");
            loadData(0L, callback);
            withCache = false;
        }

        // 加载分页数据时调用
        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            Log.e("homeviewmodel", "loadAfter: ");
            loadData(params.key, callback);
        }

        // 加载之前的数据时调用
        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull Feed item) {
            return item.getId();
        }
    };

    private void loadData(Long key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
//        if (key > 0) {
//        }
        OhRequest request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", 0)
                .addParam("feedId", key)
                .addParam("pageCount", 10)
                .responseRawType(new TypeReference<ArrayList<Feed>>() {
                }.getType());

        Log.i("loadData: ", JSONObject.toJSONString(request.getParams()));

        // 需要加载缓存
        if (withCache) {
            request.cacheStrategy(OhRequest.CACHE_ONLY);
            request.execute(new ResultCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(OhResponse<List<Feed>> response) {
                    Log.e("onCacheSuccess", "onCacheSuccess: " + response.body);
                }
            });
        }

        try {
            OhRequest netRequest = withCache ? request.clone() : request;
            // 如果是下拉刷新 则缓存策略选择 OhRequest.NET_CACHE， 如果是上拉加载 取 OhRequest.NET_ONLY
            netRequest.cacheStrategy(key == 0L ? OhRequest.NET_CACHE : OhRequest.NET_ONLY);
            OhResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;

            callback.onResult(data);

            if (key > 0L) {
                // 告诉UI层关闭上拉加载动画，如果上拉无数据，通过 liveData发送数据
                getBoundaryPageData().postValue(data.size() > 0);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}