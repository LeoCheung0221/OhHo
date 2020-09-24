package com.tufusi.libnetwork;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description 返回结果回调
 */
public abstract class ResultCallback<T> {

    /**
     * 网络请求响应成功回调
     *
     * @param response 响应结果对象
     */
    public void onSuccess(OhResponse<T> response) {

    }

    /**
     * 网络请求失败回调
     *
     * @param response 响应对象
     */
    public void onError(OhResponse<T> response) {

    }

    /**
     * 缓存成功回调
     *
     * @param response 响应对象
     */
    public void onCacheSuccess(OhResponse<T> response) {

    }

} 