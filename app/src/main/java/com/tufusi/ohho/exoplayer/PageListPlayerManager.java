package com.tufusi.ohho.exoplayer;

import android.app.Application;
import android.net.Uri;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.tufusi.libcommon.AppGlobal;

import java.util.HashMap;

/**
 * Created by 鼠夏目 on 2020/10/10.
 *
 * @author 鼠夏目
 * @description 播放器管理类
 * 管理每个页面的视频播放/暂停操作
 */
public class PageListPlayerManager {

    private static HashMap<String, PageListPlayer> sPlayerHashMap = new HashMap<>();

    public static PageListPlayer get(String pageName) {
        PageListPlayer pageListPlayer = sPlayerHashMap.get(pageName);
        if (pageListPlayer == null) {
            pageListPlayer = new PageListPlayer();
            sPlayerHashMap.put(pageName, pageListPlayer);
        }

        return pageListPlayer;
    }

    private static final ProgressiveMediaSource.Factory mediaSourceFactory;

    static {
        Application application = AppGlobal.getsApplication();
        // 创建http视频资源如何加载的工厂对象：视频 url 下载视频
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(application, application.getPackageName()));
        // 创建缓存，指定缓存位置，和缓存策略 - 最近最少使用策略，最大缓存为200Mb
        Cache cache = new SimpleCache(application.getCacheDir(), new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200));
        // 将缓存对象 cache 和负责缓存数据读取、写入的工厂类 CacheDataSinkFactory 相关联
        CacheDataSinkFactory cacheDataSinkFactory = new CacheDataSinkFactory(cache, Long.MAX_VALUE);

        /**
         * 创建能够 边播放边缓存 的本地资源加载 和 http 网络数据写入的工厂类
         *          * public CacheDataSourceFactory(
         *          *       Cache cache, 缓存写入策略和缓存写入位置的对象
         *          *       DataSource.Factory upstreamFactory,http视频资源如何加载的工厂对象
         *          *       DataSource.Factory cacheReadDataSourceFactory,本地缓存数据如何读取的工厂对象
         *          *       @Nullable DataSink.Factory cacheWriteDataSinkFactory,http网络数据如何写入本地缓存的工厂对象
         *          *       @CacheDataSource.Flags int flags,加载本地缓存数据进行播放时的策略,如果遇到该文件正在被写入数据,或读取缓存数据发生错误时的策略
         *          *       @Nullable CacheDataSource.EventListener eventListener  缓存数据读取的回调
         */
        CacheDataSourceFactory cacheDataSourceFactory =
                new CacheDataSourceFactory(cache,
                        dataSourceFactory,
                        new FileDataSourceFactory(),
                        cacheDataSinkFactory,
                        CacheDataSource.FLAG_BLOCK_ON_CACHE,
                        null);

        // 创建 MediaSource 媒体资源 加载工厂类
        // 创建该类能够实现 边缓冲变播放的 效果
        // 如果需要播放 hls、m3u8，则需要创建 DashMediaSource.Factory()
        mediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    /**
     * 创建 MediaSource 视频播放
     */
    public static MediaSource createMediaSource(String url) {
        return mediaSourceFactory.createMediaSource(Uri.parse(url));
    }

    public static void release(String pageName) {
        PageListPlayer pageListPlayer = sPlayerHashMap.get(pageName);
        if (pageListPlayer != null) {
            pageListPlayer.release();
        }
    }
}