package com.tufusi.ohho.ui.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.ohho.R;

/**
 * Created by 鼠夏目 on 2020/10/14.
 *
 * @author 鼠夏目
 * @description 用来自定义摆放各组件间的相对位置
 * <p>
 * View 指代 Behavior被应用的 View
 * 同时也要得知当前 View 依赖的 anchorView_id
 */
public class ViewAnchorBehavior extends CoordinatorLayout.Behavior<View> {

    private int anchorId;
    // 底部互动区高度
    private int extraUsed;

    public ViewAnchorBehavior() {
    }

    public ViewAnchorBehavior(int anchorId) {
        this.anchorId = anchorId;
        extraUsed = ScreenUtils.dip2px(48);
    }

    /**
     * 这个构造函数必须要有，否则布局文件中应用将无作用
     */
    @SuppressLint("Recycle")
    public ViewAnchorBehavior(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.view_anchor_behavior, 0, 0);
        anchorId = typedArray.getResourceId(R.styleable.view_anchor_behavior_anchorId, 0);
        typedArray.recycle();
        extraUsed = ScreenUtils.dip2px(48);
    }

    /**
     * @param parent     根布局 CoordinatorLayout
     * @param child      behavior 应用的View
     * @param dependency 当前 View 所依赖的 View
     * @return
     */
    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return anchorId == dependency.getId();
    }

    /**
     * CoordinatorLayout 遍历每一个子View，在测量每一个子View的时候 回调此方法
     *
     * @param parent                  根布局
     * @param child                   Behavior 被应用的 View
     * @param parentWidthMeasureSpec  parent的宽度测量模式
     * @param widthUsed               CoordinatorLayout在水平方向上有多少空间被占用 （这个给0即可）
     * @param parentHeightMeasureSpec parent的高度测量模式
     * @param heightUsed              CoordinatorLayout在垂直方向上有多少空间被占用
     * @return 返回true  CoordinatorLayout 就不会在测量此 View，会根据测量的值，摆放 View 的位置
     */
    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent,
                                  @NonNull View child,
                                  int parentWidthMeasureSpec,
                                  int widthUsed,
                                  int parentHeightMeasureSpec,
                                  int heightUsed) {
        // 首先找出该 View 是否是 anchorView
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            return false;
        }

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();

        // 测量子 View 时，需要告诉 CoordinatorLayout。垂直方向上，已经有多少空间被占用了
        // 如果 heightUsed = 0，那么评论列表这个View被测出来的高度将会大于其实际的高度。造成的后果就是会被底部互动区域给遮挡
        // 通过返回true 标记，将我们计算好的值进行位置摆放
        heightUsed = bottom + topMargin + extraUsed;
        parent.onMeasureChild(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, heightUsed);
        return true;
    }

    /**
     * 当 CoordinatorLayout 在摆放每一个子 View 时调用
     *
     * @param parent
     * @param child
     * @param layoutDirection
     * @return true 不会再次摆放这个 View 的位置
     */
    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            // 继续让 CoordinatorLayout 去摆放布局
            return false;
        }

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();

        parent.onLayoutChild(child, layoutDirection);
        child.offsetTopAndBottom(topMargin + bottom);

        return true;
    }
}