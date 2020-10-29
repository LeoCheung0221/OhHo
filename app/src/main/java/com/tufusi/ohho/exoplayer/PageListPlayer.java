package com.tufusi.ohho.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.tufusi.libcommon.AppGlobal;
import com.tufusi.ohho.R;

/**
 * Created by 鼠夏目 on 2020/10/10.
 *
 * @author 鼠夏目
 * @description
 */
public class PageListPlayer {

    public PlayerControlView mControlView;
    public SimpleExoPlayer mExoPlayer;
    public PlayerView mPlayView;
    public String mPlayUrl;

    public PageListPlayer() {
        Application application = AppGlobal.getApplication();
        // 创建 exoplayer 播放器实例
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(application,
                // 视频每一帧的画面渲染器的实现类（默认）
                new DefaultRenderersFactory(application),
                // 视频的音视频轨道加载器（默认）
                new DefaultTrackSelector(),
                // 视频缓存控制器（默认）
                new DefaultLoadControl());

        // 展示视频播放
        mPlayView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exoplayer_view, null, false);
        // 控制视频播放
        mControlView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exoplayer_control_view, null, false);

        // 关联 PlayView 和 ControlView
        mPlayView.setPlayer(mExoPlayer);
        mControlView.setPlayer(mExoPlayer);
    }

    public void release() {
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.stop(true);
            mExoPlayer.release();
            mExoPlayer = null;
        }
        if (mPlayView != null) {
            mPlayView.setPlayer(null);
            mPlayView = null;
        }
        if (mControlView != null) {
            mControlView.setPlayer(null);
            mControlView.setVisibilityListener(null);
            mControlView = null;
        }
    }

    /**
     * 切换与播放器 exoplayer 绑定的 exoplayerView 用于页面切换视频无缝续播的场景
     * 新旧播放器互斥
     */
    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        mPlayView.setPlayer(attach ? null : mExoPlayer);
        newPlayerView.setPlayer(attach ? mExoPlayer : null);
    }
}