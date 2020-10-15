package com.tufusi.ohho.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.ohho.R;
import com.tufusi.ohho.exoplayer.PageListPlayer;
import com.tufusi.ohho.exoplayer.PageListPlayerManager;

/**
 * Created by 鼠夏目 on 2020/10/14.
 *
 * @author 鼠夏目
 * @description 视频详情页全屏播放专用
 */
public class FullScreenPlayerView extends OHPlayerView {

    private PlayerView exoPlayerView;

    public FullScreenPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FullScreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        exoPlayerView = (PlayerView) LayoutInflater.from(context).inflate(R.layout.layout_exoplayer_view, null, false);
    }

    @Override
    protected void setSize(int widthPx, int heightPx) {
        if (widthPx > heightPx) {
            super.setSize(widthPx, heightPx);
            return;
        }

        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenHeight;
        setLayoutParams(layoutParams);

        FrameLayout.LayoutParams coverLayoutParams = (LayoutParams) cover.getLayoutParams();
        coverLayoutParams.width = (int) (widthPx / (heightPx * 1.0f / screenHeight));
        coverLayoutParams.height = screenHeight;
        coverLayoutParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverLayoutParams);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (mHeightPx > mWidthPx) {
            int layoutWidth = params.width;
            int layoutHeight = params.height;

            ViewGroup.LayoutParams coverLayoutParams = cover.getLayoutParams();
            coverLayoutParams.width = (int) (mWidthPx / (mHeightPx * 1.0f / layoutHeight));
            coverLayoutParams.height = layoutHeight;

            cover.setLayoutParams(coverLayoutParams);
            if (exoPlayerView != null) {
                ViewGroup.LayoutParams layoutParams = exoPlayerView.getLayoutParams();
                if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
                    float scaleX = coverLayoutParams.width * 1.0f / layoutParams.width;
                    float scaleY = coverLayoutParams.height * 1.0f / layoutParams.height;

                    exoPlayerView.setScaleX(scaleX);
                    exoPlayerView.setScaleY(scaleY);
                }
            }
        }
        super.setLayoutParams(params);
    }

    @Override
    public void onActive() {
        PageListPlayer pageListPlayer = PageListPlayerManager.get(mPageLifeTag);
        PlayerView playerView = this.exoPlayerView;
        PlayerControlView controlView = pageListPlayer.mControlView;
        SimpleExoPlayer exoPlayer = pageListPlayer.mExoPlayer;

        if (playerView == null) {
            return;
        }

        // 主动关联播放器和exoplayerView，直接替换布局位置达到无缝续播
        pageListPlayer.switchPlayerView(playerView, true);
        ViewParent parent = playerView.getParent();

        if (parent != this) {
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
            }

            ViewGroup.LayoutParams coverLayoutParams = cover.getLayoutParams();
            addView(playerView, 1, coverLayoutParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }

            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM;
            addView(controlView, layoutParams);
        }

        // 如果是同一个视频资源，则不需要重新创建 mediaSource
        // 但是需要调 onPlayStateChanged，否则不会触发 onPlayStateChanged()
        if (TextUtils.equals(pageListPlayer.mPlayUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayerManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlayer.mPlayUrl = mVideoUrl;
        }

        controlView.show();
        controlView.setVisibilityListener(this);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onInActive() {
        super.onInActive();
        PageListPlayer pageListPlayer = PageListPlayerManager.get(mPageLifeTag);
        // 切断播放器与exoplayer关联
        pageListPlayer.switchPlayerView(exoPlayerView, false);
    }
}