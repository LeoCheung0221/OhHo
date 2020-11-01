package com.tufusi.ohho.exoplayer;

import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 鼠夏目 on 2020/10/10.
 *
 * @author 鼠夏目
 * @description 列表视频自动播放 检测逻辑
 */
public class PageListPlayerDetector {

    private LifecycleOwner mOwner;
    private RecyclerView mRecyclerView;

    // 收集一个个的能够进行视频播放的 对象，面向接口
    private List<IPlayerTarget> mTargets = new ArrayList<>();
    // 正在播放的那个
    private IPlayerTarget mPlayingTarget;

    private Pair<Integer, Integer> rvLocation = null;

    public void addTarget(IPlayerTarget target) {
        mTargets.add(target);
    }

    public void removeTarget(IPlayerTarget target) {
        mTargets.remove(target);
    }

    /**
     * 构造器传入生命周期管理对象以及列表对象
     */
    public PageListPlayerDetector(LifecycleOwner owner, RecyclerView recyclerView) {
        mOwner = owner;
        mRecyclerView = recyclerView;

        /**
         * 监听数组的生命周期
         */
        LifecycleEventObserver mLifecycleObserver = new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mPlayingTarget = null;
                    mTargets.clear();
                    mRecyclerView.removeCallbacks(delayAutoPlay);
                    mRecyclerView.removeOnScrollListener(mScrollListener);
                    mOwner.getLifecycle().removeObserver(this);
                }
            }
        };
        mOwner.getLifecycle().addObserver(mLifecycleObserver);
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(mScrollListener);
    }

    /**
     * 监听是否有数据添加到了 RecyclerView 里
     */
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            postAutoPlay();
        }
    };

    /**
     * 滑动监听何时自动播放/暂停播放
     */
    RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            // 当列表滚动停止之后
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx == 0 && dy == 0) {
                postAutoPlay();
            } else {
                // 如果有正在播放的，且滑动时被划出了屏幕 则停止其他播放
                if (mPlayingTarget != null && mPlayingTarget.isPlaying() && !isTargetInBounds(mPlayingTarget)) {
                    mPlayingTarget.onInActive();
                }
            }
        }
    };

    /**
     * 检测 IPlayerTarget 所在的 ViewGroup 是否至少还有一半的大小在屏幕内
     */
    private boolean isTargetInBounds(IPlayerTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();

        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }

        int[] location = new int[2];
        owner.getLocationOnScreen(location);

        int center = location[1] + owner.getHeight() / 2;

        // 承载视频播放画面的 ViewGroup，至少需要一半的大小 在 RecyclerView 上下范围内
        return center >= rvLocation.first && center <= rvLocation.second;
    }

    /**
     * 确定 RecyclerView 的位置
     *
     * @return 返回 RV 的位置信息
     */
    private Pair<Integer, Integer> ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);

            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();

            rvLocation = new Pair<>(top, bottom);
        }

        return rvLocation;
    }

    Runnable delayAutoPlay = new Runnable() {
        @Override
        public void run() {
            autoPlay();
        }
    };

    private void postAutoPlay() {
        mRecyclerView.post(delayAutoPlay);
    }

    /**
     * 自动播放条件：只要 PlayView 有一半在页面内就实现自动播放功能
     */
    private void autoPlay() {
        if (mTargets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }

        if (mPlayingTarget != null && mPlayingTarget.isPlaying() && isTargetInBounds(mPlayingTarget)) {
            return;
        }

        IPlayerTarget activeTarget = null;
        for (IPlayerTarget target : mTargets) {
            boolean inBounds = isTargetInBounds(target);

            if (inBounds) {
                activeTarget = target;
                break;
            }
        }

        if (activeTarget != null) {
            if (mPlayingTarget != null) {
                mPlayingTarget.onInActive();
            }
            mPlayingTarget = activeTarget;
            activeTarget.onActive();
        }
    }

    public void onPause() {
        if (mPlayingTarget != null) {
            mPlayingTarget.onInActive();
        }
    }

    public void onResume() {
        if (mPlayingTarget != null) {
            mPlayingTarget.onActive();
        }
    }
}