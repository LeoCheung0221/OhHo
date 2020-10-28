package com.tufusi.libcommon.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.tufusi.libcommon.R;
import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.libcommon.view.ViewHelper;

/**
 * Created by LeoCheung on 2020/10/19.
 *
 * @author 鼠夏目
 * @description
 */
public class LoadingDialog extends AlertDialog {

    private TextView loadingText;

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    protected LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.layout_loading_view);
        loadingText = findViewById(R.id.loading_text);
        loadingText.setTextColor(ContextCompat.getColor(getContext(), R.color.color_white));
        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        // 设置对话框显示时界面变暗程度
        attributes.dimAmount = 0.5f;
        // 解决外边框问题
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ViewHelper.setViewOutline(findViewById(R.id.loading_layout), ScreenUtils.dip2px(10), ViewHelper.RADIUS_ALL);
        window.setAttributes(attributes);
    }

    public void setLoadingText(String loadingText) {
        if (this.loadingText != null) {
            this.loadingText.setVisibility(View.VISIBLE);
            this.loadingText.setText(loadingText);
        }
    }
}