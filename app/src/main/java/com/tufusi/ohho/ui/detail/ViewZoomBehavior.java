package com.tufusi.ohho.ui.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.ohho.R;
import com.tufusi.ohho.view.FullScreenPlayerView;

/**
 * Created by 鼠夏目 on 2020/10/14.
 *
 * @author 鼠夏目
 * @description 用来处理手势滑动效果，可以通过 ViewDragHelper 协调拦截手势
 */
public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullScreenPlayerView> {

    private OverScroller overScroller;
    private int scrollingId;
    private int minHeight;
    private ViewDragHelper viewDragHelper;
    private View scrollingView;
    private FullScreenPlayerView refChild;
    private int childOriginalHeight;
    private boolean canFullScreen;
    private FlingRunnable runnable;

    public ViewZoomBehavior() {
    }

    @SuppressLint("Recycle")
    public ViewZoomBehavior(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.view_zoom_behavior, 0, 0);
        minHeight = typedArray.getDimensionPixelOffset(R.styleable.view_zoom_behavior_minHeight, ScreenUtils.dip2px(200));
        scrollingId = typedArray.getResourceId(R.styleable.view_zoom_behavior_scrolling_id, 0);
        typedArray.recycle();

        overScroller = new OverScroller(context);
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, int layoutDirection) {
        // 获取 scrollingView
        // 全局保存 childView(FullScreenPlayerView)
        // 计算初始时 child 的底部值，即高度值。后续拖动时，这就是最大高度的限制
        // 计算当前页面是否可以进行全局视频展示，即 h>w
        if (viewDragHelper == null) {
            viewDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback);
            scrollingView = parent.findViewById(scrollingId);
            refChild = child;
            childOriginalHeight = child.getMeasuredHeight();
            canFullScreen = childOriginalHeight > parent.getMeasuredWidth();
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullScreen || viewDragHelper == null) {
            return super.onTouchEvent(parent, child, ev);
        }

        viewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child, @NonNull MotionEvent ev) {
        if (!canFullScreen || viewDragHelper == null) {
            return super.onInterceptTouchEvent(parent, child, ev);
        }

        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        /**
         * 告诉 ViewDragHelper 何时可以拦截   手指触摸的这个View的手势分发
         */
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            // 当全屏 && 上滑 交给我们自己分发手势操作
            if (canFullScreen &&
                    refChild.getBottom() >= minHeight
                    && refChild.getBottom() <= childOriginalHeight) {
                return true;
            }
            return false;
        }

        /**
         * 告诉 ViewDragHelper 在屏幕上滑动多少距离才算是拖拽
         */
        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return viewDragHelper.getTouchSlop();
        }

        /**
         * 告诉 ViewDragHelper 手指拖拽的这个 View 本次滑动最终能够移动的距离
         */
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            // dy > 0 : 代表手指从屏幕上方往屏幕下方滑动
            // dy < 0 : 代表手指从屏幕下方往屏幕上方滑动
            if (refChild == null || dy == 0) {
                return 0;
            }

            // 手指从下方往上滑动，dy<0 意味着refChild的底部会被向上移动。所以它的底部的最小值不能小于minHeight
            if (dy < 0 && refChild.getBottom() < minHeight
                    // 手指从上往下移动，dy>0 意味着refChild的底部会被向下移动。所以它的底部的最大值 不能超过父容器的最原始高度，也即 childOriginHeight
                    || dy > 0 && refChild.getBottom() > childOriginalHeight
                    // 手指从上往下滑动，如果scrollingView还没有滑动到列表的最顶部，也意味着列表还可以向下滑动，此时应该让列表自行滑动，不做拦截
                    || dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1))) {
                return 0;
            }

            int maxConsumed = 0;
            if (dy > 0) {
                // 如果本次滑动的dy值 + refChild 的bottom值 > 父容器的最大高度值
                if (refChild.getBottom() + dy > childOriginalHeight) {
                    maxConsumed = childOriginalHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            } else {
                // 这里的dy值是负值
                // 如果本次滑动的dy值 + refChild的 bottom值 < minHeight，计算本地能够滑动的最大距离
                // 即一直往上滑动，直到缩到minHeight为止
                if (refChild.getBottom() + dy < minHeight) {
                    maxConsumed = minHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }

            ViewGroup.LayoutParams layoutParams = refChild.getLayoutParams();
            layoutParams.height = layoutParams.height + maxConsumed;
            refChild.setLayoutParams(layoutParams);

            // 控制视频缩放
            if (mViewZoomCallback != null) {
                mViewZoomCallback.onDragZoom(layoutParams.height);
            }
            return maxConsumed;
        }

        /**
         * 手指从屏幕上离开时会被触发
         */
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (refChild.getBottom() > minHeight && refChild.getBottom() < childOriginalHeight && yvel != 0) {
                refChild.removeCallbacks(runnable);
                runnable = new FlingRunnable(refChild);
                // 惯性滑动
                runnable.fling((int) xvel, (int) yvel);
            }
        }
    };

    /**
     * 手指拖动或者滑动的值传递出去
     */
    public interface ViewZoomCallback {
        void onDragZoom(int height);
    }

    private ViewZoomCallback mViewZoomCallback;

    public void setViewZoomCallback(ViewZoomCallback viewZoomCallback) {
        this.mViewZoomCallback = viewZoomCallback;
    }

    private class FlingRunnable implements Runnable {

        private View mFlingView;

        public FlingRunnable(View flingView) {
            this.mFlingView = flingView;
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams params = mFlingView.getLayoutParams();
            int height = params.height;
            // 判断本次滑动是否已经滚动到最终点，如果没有则驱动它再次进行滚动
            if (overScroller.computeScrollOffset() && height >= minHeight && height <= childOriginalHeight) {
                int newHeight = Math.min(overScroller.getCurrY(), childOriginalHeight);
                if (newHeight != height) {
                    params.height = newHeight;
                    mFlingView.setLayoutParams(params);

                    if (mViewZoomCallback != null) {
                        mViewZoomCallback.onDragZoom(newHeight);
                    }
                }
                // 驱动惯性滑动
                ViewCompat.postOnAnimation(mFlingView, this);
            } else {
                mFlingView.removeCallbacks(this);
            }
        }

        /**
         * 惯性滑动操作
         *
         * @param xvel 水平上面的惯性滑动速度
         * @param yvel 垂直上面的惯性滑动速度
         */
        public void fling(int xvel, int yvel) {
            /**
             * startX:开始的X值，由于我们不需要再水平方向滑动 所以为0
             * startY:开始滑动时Y的起始值，那就是flingview的bottom值
             * xvel:水平方向上的速度，实际上为0的
             * yvel:垂直方向上的速度。即松手时的速度
             * minX:水平方向上 滚动回弹的越界最小值，给0即可
             * maxX:水平方向上 滚动回弹越界的最大值，实际上给0也是一样的
             * minY：垂直方向上 滚动回弹的越界最小值，给0即可
             * maxY:垂直方向上，滚动回弹越界的最大值，实际上给0 也一样
             */
            overScroller.fling(0, mFlingView.getBottom(), xvel, yvel, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            // 实现连续滑动，上面的方法只能一段距离滑行一次
            run();
        }
    }
}