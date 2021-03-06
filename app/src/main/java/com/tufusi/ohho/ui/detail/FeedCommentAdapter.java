package com.tufusi.ohho.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.libcommon.ext.AbsPagedListAdapter;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.databinding.LayoutFeedCommentListItemBinding;
import com.tufusi.ohho.model.Comment;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.InteractionPresenter;
import com.tufusi.ohho.ui.MutableItemKeyedDataSource;
import com.tufusi.ohho.ui.publish.PreviewActivity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by 鼠夏目 on 2020/10/13.
 *
 * @author 鼠夏目
 * @description
 */
public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected FeedCommentAdapter.ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(FeedCommentAdapter.ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        holder.mBinding.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item == null) {
                    return;
                }
                deleteComment(item);
            }
        });
        holder.mBinding.commentCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item == null) {
                    return;
                }
                boolean isVideo = item.getCommentType() == Comment.COMMENT_TYPE_VIDEO;
                PreviewActivity.startActivityForResult((Activity) mContext, isVideo ? item.getVideoUrl() : item.getImageUrl(), isVideo, null);
            }
        });
    }

    private void deleteComment(Comment item) {
        InteractionPresenter.deleteFeedComment(mContext, item.getItemId(), item.getCommentId())
                .observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        if (success) {
                            MutableItemKeyedDataSource<Long, Comment> dataSource =
                                    new MutableItemKeyedDataSource<Long, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
                                        @NotNull
                                        @Override
                                        public Long getKey(@NonNull Comment item) {
                                            return item.getId();
                                        }
                                    };

                            PagedList<Comment> currentList = getCurrentList();
                            for (Comment comment : currentList) {
                                if (comment != item) {
                                    dataSource.data.add(comment);
                                }
                            }

                            PagedList<Comment> newPageList = dataSource.createNewPageList(getCurrentList().getConfig());
                            submitList(newPageList);
                        }
                    }
                });
    }

    public void addAndRefreshList(Comment comment) {
        MutableItemKeyedDataSource<Long, Comment>
                mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Long, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
            @NotNull
            @Override
            public Long getKey(@NonNull Comment item) {
                return item.getId();
            }
        };

        mutableItemKeyedDataSource.data.add(comment);
        mutableItemKeyedDataSource.data.addAll(getCurrentList());
        PagedList<Comment> pageList = mutableItemKeyedDataSource.createNewPageList(getCurrentList().getConfig());
        submitList(pageList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            boolean isAuthor = item.getAuthor() != null && UserManager.get().getUserId() == item.getAuthor().getUserId();
            mBinding.labelAuthor.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.getImageUrl())) {
                mBinding.commentExt.setVisibility(View.VISIBLE);
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(item.getImageUrl(), item.getWidth(), item.getHeight(), 0, ScreenUtils.dip2px(200), ScreenUtils.dip2px(200));
                if (!TextUtils.isEmpty(item.getVideoUrl())) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentExt.setVisibility(View.GONE);
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
            }
        }
    }
}