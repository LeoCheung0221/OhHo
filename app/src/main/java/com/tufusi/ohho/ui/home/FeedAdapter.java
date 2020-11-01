package com.tufusi.ohho.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.libcommon.ext.AbsPagedListAdapter;
import com.tufusi.libcommon.ext.LiveDataBus;
import com.tufusi.ohho.BR;
import com.tufusi.ohho.R;
import com.tufusi.ohho.databinding.LayoutFeedTypeImageBinding;
import com.tufusi.ohho.databinding.LayoutFeedTypeVideoBinding;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.InteractionPresenter;
import com.tufusi.ohho.ui.detail.FeedDetailActivity;
import com.tufusi.ohho.view.OHImageView;
import com.tufusi.ohho.view.OHPlayerView;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.FeedViewHolder> {

    private LayoutInflater mInflater;
    protected Context mContext;
    protected String mPageLifeTag;

    private FeedObserver mFeedObserver;

    protected FeedAdapter(Context context, String pageLifeTag) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPageLifeTag = pageLifeTag;
    }

    @Override
    public int getItemViewType2(int position) {
        Feed feed = getItem(position);
        if (feed == null) {
            return 0;
        }
        if (feed.getItemType() == Feed.IMAGE_TEXT_TYPE) {
            return R.layout.layout_feed_type_image;
        } else if (feed.getItemType() == Feed.VIDEO_TYPE) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder2(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(mInflater, viewType, parent, false);
        return new FeedViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder2(@NonNull FeedViewHolder holder, int position) {
        Feed feed = getItem(position);
        holder.bindData(feed);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 无缝续播需要
                FeedDetailActivity.startFeedDetailActivity(mContext, getItem(position), mPageLifeTag);
                onStartFeedDetailActivity(feed);
                if (mFeedObserver == null) {
                    mFeedObserver = new FeedObserver();
                    LiveDataBus.get().with(InteractionPresenter.DATA_FROM_INTERACTION)
                            .observe((LifecycleOwner) mContext, mFeedObserver);
                }
                mFeedObserver.setFeed(feed);
            }
        });
    }

    public void onStartFeedDetailActivity(Feed feed) {

    }

    private static class FeedObserver implements Observer<Feed> {

        private Feed mFeed;

        @Override
        public void onChanged(Feed newOne) {
            if (mFeed.getId() != newOne.getId()) {
                return;
            }

            mFeed.author = newOne.author;
            mFeed.ugc = newOne.ugc;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) {
            mFeed = feed;
        }
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding mBinding;
        public OHPlayerView playerView;
        public OHImageView feedImage;

        public FeedViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            mBinding.setVariable(BR.feed, item);
            mBinding.setVariable(BR.owner, mContext);

            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                feedImage = imageBinding.feedImage;
                imageBinding.feedImage.bindData(item.getCover(), item.getWidth(), item.getHeight(), 16);
            } else {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.playView.bindData(item.getUrl(), item.getCover(), item.getWidth(), item.getHeight(), mPageLifeTag);
                playerView = videoBinding.playView;
            }
        }

        public boolean isVideoType() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public OHPlayerView getPlayerView() {
            return playerView;
        }
    }
}