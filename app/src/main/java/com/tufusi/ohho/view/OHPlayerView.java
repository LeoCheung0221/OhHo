package com.tufusi.ohho.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.tufusi.ohho.R;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.ohho.exoplayer.IPlayerTarget;
import com.tufusi.ohho.exoplayer.PageListPlayer;
import com.tufusi.ohho.exoplayer.PageListPlayerManager;

import static android.view.Gravity.BOTTOM;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public class OHPlayerView extends FrameLayout implements IPlayerTarget, PlayerControlView.VisibilityListener, Player.EventListener {

    public View bufferView;
    public OHImageView cover, blur;
    protected ImageView playBtn;

    protected String mVideoUrl;
    protected String mPageLifeTag;
    protected boolean isPlaying;
    protected int mWidthPx;
    protected int mHeightPx;

    public OHPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public OHPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OHPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        // 缓冲加载圈圈 View
        bufferView = findViewById(R.id.buffer_view);
        // 高斯模糊背景图 方式出现两边留黑
        blur = findViewById(R.id.blur_background);
        // 视频封面
        cover = findViewById(R.id.cover);
        // 视频播放暂停按钮
        playBtn = findViewById(R.id.play_btn);

        playBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()) {
                    onInActive();
                } else {
                    onActive();
                }
            }
        });

        setTransitionName("oHPlayerView");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击改区域时，展示视频进度控制器
        PageListPlayer player = PageListPlayerManager.get(mPageLifeTag);
        return true;
    }

    /**
     * 绑定页面数据
     *
     * @param videoUrl    传入的视频链接
     * @param coverUrl    传入的视频封面
     * @param widthPx     传入的视频宽
     * @param heightPx    传入的视频高
     * @param pageLifeTag 页面的声明标识
     */
    public void bindData(String videoUrl, String coverUrl, int widthPx, int heightPx, String pageLifeTag) {
        mWidthPx = widthPx;
        mHeightPx = heightPx;
        mVideoUrl = videoUrl;
        mPageLifeTag = pageLifeTag;
        // 设置封面
        cover.setImageUrl(coverUrl);

        // 设置高斯模糊背景
        // 只有当高>宽，才需要展示高斯模糊
        if (widthPx < heightPx) {
            blur.setBlurImageUrl(coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }

        setSize(widthPx, heightPx);
    }

    /**
     * 设置控件的宽高
     */
    protected void setSize(int widthPx, int heightPx) {
        int maxWidth = ScreenUtils.getScreenWidth();
        int maxHeight = maxWidth;

        // 控件根据传入进来的宽高计算出的宽高
        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        // 封面的宽高，根据视频宽高比较设置是否铺满横向
        int coverWidth = 0;
        int coverHeight = 0;

        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) ((heightPx * maxWidth) / widthPx * 1.0f);
        } else {
            coverHeight = layoutHeight = maxHeight;
            coverWidth = (int) ((widthPx * maxHeight) / heightPx * 1.0f);
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        // 设置组件
        setLayoutParams(params);

        // 设置高斯模糊 跟控件的宽高一致
        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeight;
        blur.setLayoutParams(blurParams);

        // 设置封面的宽高
        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        // 设置播放按钮
        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);
    }

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void onActive() {
        // 视频播放、或恢复播放
        // 通过该View所在页面的 mPageLifeTag(比如 首页列表：tab_all，沙发：tab_video，标签帖子聚合：tag_feed 等字段)
        // 取出管理该页面的 Exoplayer 播放器，ExoplayerView 播放 View，控制器对象 PageListPlayer
        PageListPlayer pageListPlayer = PageListPlayerManager.get(mPageLifeTag);
        PlayerView playerView = pageListPlayer.mPlayView;
        PlayerControlView controlView = pageListPlayer.mControlView;
        SimpleExoPlayer exoPlayer = pageListPlayer.mExoPlayer;

        if (playerView == null) {
            return;
        }

        // 这里需要主动调用 switchPlayerView，将播放器 Exoplayer 和展示视频画面的View ExoplayerView 相关联
        // 因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的 View ExoplayerView相关联。达到视频无缝对接效果
        // 当再次返回列表页，则需要再次把播放器和 ExoplayerView 相关联
        pageListPlayer.switchPlayerView(playerView, true);

        ViewParent parent = playerView.getParent();
        if (parent != this) {
            // 将展示视频画面的 View 添加到 ItemView 的容器上
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                // 仍需暂停列表上正在播放的视频Item
                ((OHPlayerView) parent).onInActive();
            }

            ViewGroup.LayoutParams coverLayoutParams = cover.getLayoutParams();
            addView(playerView, 1, coverLayoutParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            // 将视频控制器添加到 ItemView 的容器上
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }

            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = BOTTOM;
            addView(controlView, layoutParams);
        }

        // 如果是同一个视频资源，则不需要重新创建 MediaSource
        // 但是需要 onPlayerStateChanged，否则不会触发onPlayerStateChanged()
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
        // 暂停视频播放，并让封面图和开始按钮显示出来
        PageListPlayer pageListPlayer = PageListPlayerManager.get(mPageLifeTag);
        if (pageListPlayer.mExoPlayer == null || pageListPlayer.mControlView == null || pageListPlayer.mPlayView == null) {
            return;
        }

        pageListPlayer.mExoPlayer.setPlayWhenReady(false);
        pageListPlayer.mControlView.setVisibilityListener(null);
        pageListPlayer.mExoPlayer.removeListener(this);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public void onVisibilityChange(int visibility) {
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // 监听视频播放的状态
        PageListPlayer pageListPlayer = PageListPlayerManager.get(mPageLifeTag);
        SimpleExoPlayer exoPlayer = pageListPlayer.mExoPlayer;

        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            cover.setVisibility(GONE);
            bufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            bufferView.setVisibility(VISIBLE);
        }

        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public View getPlayController() {
        PageListPlayer pageListPlayer = PageListPlayerManager.get(mPageLifeTag);
        return pageListPlayer.mControlView;
    }
}