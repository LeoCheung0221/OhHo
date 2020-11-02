package com.tufusi.ohho.ui.my;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.tufusi.ohho.R;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.Feed;
import com.tufusi.ohho.ui.InteractionPresenter;
import com.tufusi.ohho.ui.MutableItemKeyedDataSource;
import com.tufusi.ohho.ui.home.FeedAdapter;
import com.tufusi.ohho.utils.TimeUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Created by LeoCheung on 2020/11/2.
 *
 * @author 鼠夏目
 * @description
 */
public class ProfileListAdapter extends FeedAdapter {

    protected ProfileListAdapter(Context context, String pageLifeTag) {
        super(context, pageLifeTag);
    }

    @Override
    public int getItemViewType2(int position) {
        if (TextUtils.equals(mPageLifeTag, ProfileActivity.TAB_TYPE_COMMENT)) {
            return R.layout.layout_feed_type_comment;
        } else if (TextUtils.equals(mPageLifeTag, ProfileActivity.TAB_TYPE_ALL)) {
            Feed feed = getItem(position);
            if (feed.topComment != null && feed.topComment.getUserId() == UserManager.get().getUserId()) {
                return R.layout.layout_feed_type_comment;
            }
        }
        return super.getItemViewType2(position);
    }

    @Override
    public void onBindViewHolder2(@NonNull FeedViewHolder holder, int position) {
        super.onBindViewHolder2(holder, position);
        View deleteView = holder.itemView.findViewById(R.id.feed_delete);
        TextView createView = holder.itemView.findViewById(R.id.create_time);

        Feed feed = getItem(position);
        createView.setVisibility(View.VISIBLE);
        createView.setText(TimeUtils.calculate(feed.createTime));

        boolean isCommentTab = TextUtils.equals(mPageLifeTag, ProfileActivity.TAB_TYPE_COMMENT);
        deleteView.setVisibility(View.VISIBLE);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是个人主页的评论tab ，删除的时候实际是删除帖子的评论
                if (isCommentTab) {
                    InteractionPresenter.deleteFeedComment(mContext, feed.itemId, feed.topComment.getCommentId())
                            .observe((LifecycleOwner) mContext, aBoolean -> refreshList(feed));
                } else {
                    InteractionPresenter.deleteFeed(mContext, feed.itemId)
                            .observe((LifecycleOwner) mContext, aBoolean -> {
                                refreshList(feed);
                            });
                }
            }
        });
    }

    private void refreshList(Feed deleteFeed) {
        PagedList<Feed> currentList = getCurrentList();
        MutableItemKeyedDataSource<Long, Feed> dataSource =
                new MutableItemKeyedDataSource<Long, Feed>((ItemKeyedDataSource) currentList.getDataSource()) {
                    @NotNull
                    @Override
                    public Long getKey(@NonNull Feed item) {
                        return item.id;
                    }
                };

        for (Feed feed : currentList) {
            if (feed != deleteFeed) {
                dataSource.data.add(feed);
            }
        }

        PagedList<Feed> newPageList = dataSource.createNewPageList(currentList.getConfig());
        submitList(newPageList);
    }
}