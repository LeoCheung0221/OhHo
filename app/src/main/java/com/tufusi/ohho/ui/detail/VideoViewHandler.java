package com.tufusi.ohho.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.tufusi.ohho.databinding.LayoutFeedDetailImageTypeHeaderBinding;
import com.tufusi.ohho.databinding.LayoutFeedDetailTypeVideoBinding;
import com.tufusi.ohho.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.view.FullScreenPlayerView;

/**
 * Created by 鼠夏目 on 2020/10/13.
 *
 * @author 鼠夏目
 * @description
 */
public class VideoViewHandler extends ViewHandler {

    private LayoutFeedDetailTypeVideoBinding mVideoBinding;
    private String lifeTag;
    private boolean backPressed;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mVideoBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_video);

        mInteractionBinding = mVideoBinding.bottomInteraction;
        mRecyclerView = mVideoBinding.recyclerView;

        View authInfoView = mVideoBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) authInfoView.getLayoutParams();
        layoutParams.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        mVideoBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mVideoBinding.playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) params.getBehavior();
        if (behavior != null) {
            behavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
                @Override
                public void onDragZoom(int height) {
                    int bottom = mVideoBinding.playerView.getBottom();
                    boolean isMoveUp = height < bottom;
                    // 向上滑动，超过底部互动区域的高度就不能显示全屏界面的样式了
                    boolean isFullScreen = isMoveUp ?
                            height >= mVideoBinding.coordinator.getBottom() - mInteractionBinding.getRoot().getHeight()
                            :
                            height >= mVideoBinding.coordinator.getBottom();
                    setViewAppearance(isFullScreen);
                }
            });
        }
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);

        lifeTag = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_LIFE_TAG);
        mVideoBinding.playerView.bindData(mFeed.url, mFeed.cover, mFeed.width, mFeed.height, lifeTag);

        mVideoBinding.playerView.post(new Runnable() {
            @Override
            public void run() {
                boolean isFullScreen = mVideoBinding.playerView.getBottom() >= mVideoBinding.coordinator.getBottom();
                setViewAppearance(isFullScreen);
            }
        });

        LayoutFeedDetailTypeVideoHeaderBinding headerBinding =
                LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        headerBinding.setFeed(feed);
        adapter.addHeaderView(headerBinding.getRoot());
    }

    private void setViewAppearance(boolean isFullScreen) {
        mVideoBinding.setFullscreen(isFullScreen);
        mVideoBinding.fullScreenAuthorInfo.getRoot().setVisibility(isFullScreen ? View.VISIBLE : View.GONE);

        int measuredHeight = mInteractionBinding.getRoot().getMeasuredHeight();
        int ctrlMeasuredHeight = mVideoBinding.playerView.getPlayController().getMeasuredHeight();
        int bottom = mVideoBinding.playerView.getPlayController().getBottom();

        mVideoBinding.playerView.getPlayController().setY(isFullScreen ? bottom - measuredHeight - ctrlMeasuredHeight : bottom - ctrlMeasuredHeight);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            mVideoBinding.playerView.onInActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        mVideoBinding.playerView.onActive();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        mVideoBinding.playerView.getPlayController().setTranslationY(0);
    }
}