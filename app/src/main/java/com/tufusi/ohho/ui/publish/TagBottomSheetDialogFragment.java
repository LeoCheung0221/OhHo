package com.tufusi.ohho.ui.publish;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.libnetwork.ApiService;
import com.tufusi.libnetwork.OhResponse;
import com.tufusi.libnetwork.ResultCallback;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.model.TagList;
import com.tufusi.ohho.utils.TT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeoCheung on 2020/10/28.
 *
 * @author 鼠夏目
 * @description 底部标签选择弹窗
 */
public class TagBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private TagAdapter adapter;
    private List<TagList> mTagLists = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_bottom_sheet_dialog, null, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TagAdapter();
        recyclerView.setAdapter(adapter);

        dialog.setContentView(view);

        //设置默认展开高度，以及滑动展开的最大高度
        ViewGroup parent = (ViewGroup) view.getParent();
        BottomSheetBehavior<ViewGroup> behavior = BottomSheetBehavior.from(parent);
        behavior.setPeekHeight(ScreenUtils.getScreenHeight() / 3);
        // 不会一直收缩
        behavior.setHideable(false);

        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
        layoutParams.height = ScreenUtils.getScreenHeight() * 2 / 3;
        parent.setLayoutParams(layoutParams);

        queryTagList();

        return dialog;
    }

    private void queryTagList() {
        ApiService.get("/tag/queryTagList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", 0)
                .addParam("pageCount", 100)
                .execute(new ResultCallback<List<TagList>>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(OhResponse<List<TagList>> response) {
                        if (response.body != null) {
                            List<TagList> body = response.body;
                            mTagLists.clear();
                            mTagLists.addAll(body);
                            ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(OhResponse<List<TagList>> response) {
                        TT.showToast(response.message);
                    }
                });
    }

    class TagAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(getContext());
            textView.setTextSize(13);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.color_000));
            textView.setGravity(Gravity.CENTER_VERTICAL);

            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(45));
            textView.setLayoutParams(layoutParams);

            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(textView) {
            };
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            TagList tagList = mTagLists.get(position);
            textView.setText(tagList.getTitle());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTagItemSelectedListener!=null){
                        onTagItemSelectedListener.onTagItemSelected(tagList);
                        dismiss();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTagLists.size();
        }
    }

    public interface OnTagItemSelectedListener{
        void onTagItemSelected(TagList tagList);
    }

    private OnTagItemSelectedListener onTagItemSelectedListener;

    public void setOnTagItemSelectedListener(OnTagItemSelectedListener onTagItemSelectedListener) {
        this.onTagItemSelectedListener = onTagItemSelectedListener;
    }
}