package com.tufusi.libcommon.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.tufusi.libcommon.R;

/**
 * Created by 鼠夏目 on 2020/9/27.
 *
 * @author 鼠夏目
 * @description
 */
public class EmptyView extends LinearLayout {

    private ImageView icon;
    private TextView title;
    private Button action;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_empty_view, this, true);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        icon = findViewById(R.id.empty_icon);
        title = findViewById(R.id.empty_title);
        action = findViewById(R.id.empty_action);
    }

    public void setEmptyIcon(@DrawableRes int iconRes) {
        icon.setImageResource(iconRes);
    }

    public void setTitle(String text) {
        if (TextUtils.isEmpty(text)) {
            title.setVisibility(GONE);
        } else {
            title.setVisibility(VISIBLE);
            title.setText(text);
        }
    }

    public void setAction(String text, View.OnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            action.setVisibility(GONE);
        } else {
            action.setVisibility(VISIBLE);
            action.setText(text);
            action.setOnClickListener(clickListener);
        }
    }
}