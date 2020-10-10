package com.tufusi.ohho.exoplayer;

import android.view.ViewGroup;

/**
 * Created by 鼠夏目 on 2020/10/10.
 *
 * @author 鼠夏目
 * @description 通过实现此接口的对象找到 PlayView
 */
public interface IPlayerTarget {

    /**
     * 得到 PlayView 容器
     */
    ViewGroup getOwner();

    /**
     * 是否正在播放
     */
    boolean isPlaying();

    /**
     * 活跃状态：视频可播放
     */
    void onActive();

    /**
     * 非活跃装填：视频暂停
     */
    void onInActive();

}