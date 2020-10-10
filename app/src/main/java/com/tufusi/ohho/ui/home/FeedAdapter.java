package com.tufusi.ohho.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.ohho.databinding.LayoutFeedTypeImageBinding;
import com.tufusi.ohho.databinding.LayoutFeedTypeVideoBinding;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.view.OHPlayerView;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public class FeedAdapter extends PagedListAdapter<Feed, FeedAdapter.FeedViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private String mPageLifeTag;

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
    public int getItemViewType(int position) {
        Feed feed = getItem(position);
        return feed != null ? feed.getItemType() : 0;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        if (viewType == Feed.IMAGE_TYPE) {
            binding = LayoutFeedTypeImageBinding.inflate(mInflater);
        } else {
            binding = LayoutFeedTypeVideoBinding.inflate(mInflater);
        }
        return new FeedViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bindData(getItem(position));
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mBinding;
        private OHPlayerView playerView;

        public FeedViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;

                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.getCover(), item.getWidth(), item.getHeight(), 16);
                imageBinding.setLifecycleOwner((LifecycleOwner) mContext);
            } else {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;

                videoBinding.setFeed(item);
                videoBinding.playView.bindData(item.getUrl(), item.getCover(), item.getWidth(), item.getHeight(), mPageLifeTag);
                videoBinding.setLifecycleOwner((LifecycleOwner) mContext);

                playerView = videoBinding.playView;
            }
        }

        public boolean isVideoType(){
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public OHPlayerView getPlayView(){
            return playerView;
        }
    }
}