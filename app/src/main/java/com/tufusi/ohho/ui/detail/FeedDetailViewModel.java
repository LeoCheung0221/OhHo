package com.tufusi.ohho.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.Comment;
import com.tufusi.ohho.ui.AbsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 鼠夏目 on 2020/10/13.
 *
 * @author 鼠夏目
 * @description
 */
public class FeedDetailViewModel extends AbsViewModel<Comment> {

    private Long itemId;

    @Override
    public DataSource createDataSource() {
        return new DetailDataSource();
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    class DetailDataSource extends ItemKeyedDataSource<Long, Comment>{

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Comment> callback) {
            loadData(0L, params.requestedLoadSize, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Comment> callback) {
            loadData(params.key, params.requestedLoadSize, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull Comment item) {
            return item.getItemId();
        }
    }

    private void loadData(Long key, int size, ItemKeyedDataSource.LoadCallback<Comment> callback) {
        OhResponse<List<Comment>> response = ApiService.get("/comment/queryFeedComments")
                .addParam("id", key)
                .addParam("itemId", itemId)
                .addParam("pageCount", size)
                .addParam("userId", UserManager.get().getUserId())
                .responseRawType(new TypeReference<ArrayList<Comment>>() {
                }.getType())
                .execute();
        List<Comment> list = response.body == null ? Collections.emptyList() : response.body;
        callback.onResult(list);
    }
}