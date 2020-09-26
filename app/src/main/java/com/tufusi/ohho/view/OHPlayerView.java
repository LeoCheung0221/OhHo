package com.tufusi.ohho.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tufusi.ohho.R;
import com.tufusi.ohho.utils.ScreenUtils;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public class OHPlayerView extends FrameLayout {

    private View bufferView;
    private OHImageView cover, blur;
    private ImageView playBtn;

    protected String mVideoUrl;
    protected String mPageLifeTag;

    public OHPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public OHPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OHPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        bufferView = findViewById(R.id.buffer_view);
        blur = findViewById(R.id.blur_background);
        cover = findViewById(R.id.cover);
        playBtn = findViewById(R.id.play_btn);
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
        mVideoUrl = videoUrl;
        mPageLifeTag = pageLifeTag;

        // 设置封面
        cover.setImageUrl(cover, coverUrl, false);

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
    private void setSize(int widthPx, int heightPx) {
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
            layoutHeight = coverHeight = (int) (heightPx * (maxWidth / widthPx * 1.0f));
        } else {
            coverHeight = layoutHeight = maxHeight;
            coverWidth = (int) (widthPx * (maxHeight / heightPx * 1.0f));
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
}