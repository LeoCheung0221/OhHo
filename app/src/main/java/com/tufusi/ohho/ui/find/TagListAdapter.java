package com.tufusi.ohho.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tufusi.libcommon.ext.AbsPagedListAdapter;
import com.tufusi.ohho.databinding.LayoutTagListItemBinding;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.ui.InteractionPresenter;

/**
 * Created by LeoCheung on 2020/10/31.
 *
 * @author 鼠夏目
 * @description
 */
public class TagListAdapter extends AbsPagedListAdapter<TagList, TagListAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.getTagId() == newItem.getTagId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected TagListAdapter.ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding itemBinding = LayoutTagListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(itemBinding.getRoot(), itemBinding);
    }

    @Override
    protected void onBindViewHolder2(TagListAdapter.ViewHolder holder, int position) {
        TagList tagList = getItem(position);
        holder.bindData(tagList);
        holder.mItemBinding.actionFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InteractionPresenter.toggleTagLike((LifecycleOwner) mContext, tagList);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutTagListItemBinding mItemBinding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding itemBinding) {
            super(itemView);
            mItemBinding = itemBinding;
        }

        public void bindData(TagList item) {
            mItemBinding.setTagList(item);
        }
    }
}