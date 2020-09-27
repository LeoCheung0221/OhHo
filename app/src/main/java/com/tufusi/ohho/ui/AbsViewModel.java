package com.tufusi.ohho.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public abstract class AbsViewModel<T> extends ViewModel {

    private DataSource dataSource;
    private LiveData<PagedList<T>> pageData;
    private MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {
        // 设置分页信息配置
        PagedList.Config config = new PagedList.Config.Builder()
                .setPageSize(10)
                .setInitialLoadSizeHint(12)
//                .setMaxSize(100) // 共加载多少数据，一般都不会知道
//                .setEnablePlaceholders(false) // 如果设置为true，则在知道总共条数的情况下，在加载完第一页之后的其他页面都会默认有占位符项代替预加载出的项
//                .setPrefetchDistance() // 在距离底部还有多少条数据的时候开始预取数据
                .build();

        pageData = new LivePagedListBuilder<>(factory, config)
                // 在加载初始化区的时候需要传递的参数
                .setInitialLoadKey(0)
                // 异步执行的线程池
//                .setFetchExecutor()
                // 用于侦听PagedList加载状态的边界回调
                .setBoundaryCallback(callback)
                .build();
    }

    public MutableLiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    public LiveData<PagedList<T>> getPageData() {
        return pageData;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    PagedList.BoundaryCallback<T> callback = new PagedList.BoundaryCallback<T>() {

        // 当从PagedList的数据源的初始加载返回零项时调用
        // 是否要展示空布局在此处回调
        @Override
        public void onZeroItemsLoaded() {
            boundaryPageData.postValue(false);
        }

        // 当列表的第一条数据被加载后才会调用此方法
        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            boundaryPageData.postValue(true);
        }

        // 当最后一条数据被加载后才会回调此方法
        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            super.onItemAtEndLoaded(itemAtEnd);
        }
    };

    DataSource.Factory factory = new DataSource.Factory() {
        @NonNull
        @Override
        public DataSource create() {
            dataSource = createDataSource();
            return dataSource;
        }
    };

    public abstract DataSource createDataSource();
}