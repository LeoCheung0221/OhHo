package com.tufusi.ohho.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by 鼠夏目 on 2020/10/14.
 *
 * @author 鼠夏目
 * @description 覆写dispatchKeyEventPreIme：可以在对话框弹窗中，监听backPress事件，以销毁对话框
 */
public class OHEditTextView extends AppCompatEditText {

    private onKeyBackEvent keyBackEvent;

    public OHEditTextView(@NonNull Context context) {
        super(context);
    }

    public OHEditTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OHEditTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (keyBackEvent != null) {
                keyBackEvent.onKeyEvent();
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public interface onKeyBackEvent {
        boolean onKeyEvent();
    }

    public void setKeyBackEventListener(onKeyBackEvent keyBackEvent) {
        this.keyBackEvent = keyBackEvent;
    }
}