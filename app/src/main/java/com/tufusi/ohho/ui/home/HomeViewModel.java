package com.tufusi.ohho.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhRequest;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.AbsViewModel;
import com.tufusi.ohho.ui.MutablePageKeyedDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean withCache = true;
    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();
    private AtomicBoolean loadAfter = new AtomicBoolean(false);
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return new FeedDataSource();
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    /**
     * 首页加载 先加载缓存，再加载网络，随后更新本地缓存
     */
    class FeedDataSource extends ItemKeyedDataSource<Long, Feed> {

        // 加载初始化数据时调用
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Feed> callback) {
            Log.e("FeedDataSource", "loadInitial: ");
            loadData(0L, callback);
            withCache = false;
        }

        // 加载分页数据时调用
        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            Log.e("FeedDataSource", "loadAfter: ");
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
    }

    private void loadData(Long key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (key > 0) {
            // 如果此次加载属于分页的，则设置为 true
            loadAfter.set(true);
        }
        OhRequest request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", mFeedType)
                .addParam("userId", UserManager.get().getUserId())
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
                    Log.e("loadData：", "onCacheSuccess: " + (response.body != null ? response.body.size() : 0));
                    // 创建 pageList 对象需要绑定一个 DataSource 将缓存数据和 DataSource关联起来
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource<Integer, Feed>();
                    dataSource.data.addAll(response.body);

                    // 关联 pageList 和 DataSource
                    PagedList newPageList = dataSource.createNewPageList(config);
                    cacheLiveData.postValue(newPageList);

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
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Log.e("loadData", "loadData key: " + key);
    }

    /**
     * 这里需要做一个同步位， 防止 paging 和 我们主动操作加载 ，造成数据重复
     */
    @SuppressLint("RestrictedApi")
    public void loadAfter(long id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        // 如果加载过，直接 return
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id, callback);
            }
        });
    }
}