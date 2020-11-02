package com.tufusi.ohho.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tufusi.libcommon.utils.ScreenUtils;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by 鼠夏目 on 2020/9/25.
 *
 * @author 鼠夏目
 * @description DataBinding 绑定数据不是立刻执行，而是延迟一帧执行的，
 * 因此针对部分组件，尤其是列表里的宽高不确定，需要根据运行时去动态的计算，这种组件就不推荐使用 DataBinding
 */
public class OHImageView extends AppCompatImageView {

    public OHImageView(@NonNull Context context) {
        super(context);
    }

    public OHImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OHImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageUrl(String imageUrl) {
        setImageUrl(this, imageUrl, false);
    }

    @BindingAdapter(value = {"image_url", "isCircle"})
    public static void setImageUrl(OHImageView view, String imageUrl, boolean isCircle) {
        setImageUrl(view, imageUrl, isCircle, 0);
    }

    /**
     * requireAll 默认为true，代表所设定的参数需要全部传递
     */
    @SuppressLint("CheckResult")
    @BindingAdapter(value = {"image_url", "isCircle", "radius"}, requireAll = false)
    public static void setImageUrl(OHImageView view, String imageUrl, boolean isCircle, int radius) {
        RequestBuilder<Drawable> builder = Glide.with(view).load(imageUrl);
        if (isCircle) {
            builder.transform(new CircleCrop());
        } else if (radius > 0) {
            builder.transform(new RoundedCornersTransformation(ScreenUtils.dip2px(radius), 0));
        }
        // 为了防止图片过大，传入图片大小做限制
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null && layoutParams.width > 0 && layoutParams.height > 0) {
            builder.override(layoutParams.width, layoutParams.height);
        }
        builder.into(view);
    }

    public void bindData(String imageUrl, int widthPx, int heightPx, int marginLeft) {
        bindData(imageUrl, widthPx, heightPx, marginLeft, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenWidth());
    }

    /**
     * 需要图片的宽高，这里网络返回
     */
    public void bindData(String imageUrl, int widthPx, int heightPx, int marginLeft, int maxWidth, int maxHeight) {
        if (TextUtils.isEmpty(imageUrl)) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }
        if (widthPx <= 0 || heightPx <= 0) {
            Glide.with(this).load(imageUrl).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    int width = resource.getIntrinsicWidth();
                    int height = resource.getIntrinsicHeight();
                    setSize(width, height, marginLeft, maxWidth, maxHeight);

                    setImageDrawable(resource);
                }
            });
            return;
        }

        setSize(widthPx, heightPx, marginLeft, maxWidth, maxHeight);
        setImageUrl(this, imageUrl, false);
    }

    /**
     * 根据图片大小设置宽高
     */
    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth, finalHeight;
        // 如果宽大于高，则宽度全填满，高度等比例自适应
        if (width > height) {
            finalWidth = maxWidth;
            finalHeight = (int) (height / (width * 1.0f / finalWidth));
        } else {
            finalHeight = maxHeight;
            finalWidth = (int) (width / (height * 1.0f / finalHeight));
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = finalWidth;
        params.height = finalHeight;

        // 如果高度大于宽度的图片，则适当设置 marginLeft
        if (params instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) params).leftMargin = height > width ? ScreenUtils.dip2px(marginLeft) : 0;
        } else if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).leftMargin = height > width ? ScreenUtils.dip2px(marginLeft) : 0;
        }

        setLayoutParams(params);
    }

    /**
     * 设置高斯模糊
     *
     * @param blurUrl 封面图
     * @param radius  半径
     */
    @BindingAdapter(value = {"blur_url", "radius"})
    public static void setBlurImageUrl(ImageView imageView, String blurUrl, int radius) {
        Glide.with(imageView).load(blurUrl).override(radius)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // 必须是 setBackground，如果 setImageDrawable 会撑不满组件的宽高
                        imageView.setBackground(resource);
                    }
                });
    }
}