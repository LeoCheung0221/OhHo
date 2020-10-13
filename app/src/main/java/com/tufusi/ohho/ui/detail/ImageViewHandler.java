package com.tufusi.ohho.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.ActivityFeedDetailTypeImageBinding;
import com.tufusi.ohho.databinding.LayoutFeedDetailImageTypeHeaderBinding;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.view.OHImageView;

/**
 * Created by 鼠夏目 on 2020/10/13.
 *
 * @author 鼠夏目
 * @description
 */
public class ImageViewHandler extends ViewHandler {

    protected ActivityFeedDetailTypeImageBinding mImageBinding;
    protected LayoutFeedDetailImageTypeHeaderBinding mHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);

        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);
        mRecyclerView = mImageBinding.recyclerView;
        mInteractionBinding = mImageBinding.bottomInteractionLayout;

        mImageBinding.actionClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mImageBinding.setFeed(mFeed);

        mHeaderBinding = LayoutFeedDetailImageTypeHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        mHeaderBinding.setFeed(feed);

        OHImageView headerImage = mHeaderBinding.headerImage;
        headerImage.bindData(mFeed.getCover(), mFeed.getWidth(), mFeed.getHeight(), mFeed.getWidth() > mFeed.getHeight() ? 0 : 16);

        adapter.addHeaderView(mHeaderBinding.getRoot());

        // 上滑超出一定距离，顶部的作者用户信息显示出来
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // HeaderView 滑出屏幕的top值是否小于顶部导航栏的高度
                boolean isVisible = mHeaderBinding.getRoot().getTop() <= -mImageBinding.titleLayout.getMeasuredHeight();
                mImageBinding.authorInfoLayout.getRoot().setVisibility(isVisible ? View.VISIBLE : View.GONE);
                mImageBinding.titleLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            }
        });

        handlerEmpty(false);
    }
}