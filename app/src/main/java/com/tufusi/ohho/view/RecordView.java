package com.tufusi.ohho.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tufusi.libcommon.utils.ScreenUtils;
import com.tufusi.ohho.R;

/**
 * Created by 鼠夏目 on 2020/10/15.
 *
 * @author 鼠夏目
 * @description
 */
public class RecordView extends View implements View.OnClickListener, View.OnLongClickListener {

    // 每隔多少毫秒更新进度值
    private static final int PROGRESS_INTERVAL = 100;
    private final int radius;
    private final int progressWidth;
    private final int progressColor;
    private final int fillColor;
    private final int maxDuration;

    // 背景圆画笔
    private Paint fillPaint;
    // 圆环进度画笔
    private Paint progressPaint;
    // 录制进度最大值
    private int progressMaxValue;
    // 是否正在录制
    private boolean isRecording;
    // 当前录制进度值
    private int progressValue;
    // 开始录制时间戳
    private long startRecordTime;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("Recycle")
    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes);
        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, ScreenUtils.dip2px(3));
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        maxDuration = typedArray.getInteger(R.styleable.RecordView_duration, 10);

        //根据传递进来的时间值，计算进度最大值
        setMaxDuration(maxDuration);
        typedArray.recycle();

        // 设置画笔
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                progressValue++;
                postInvalidate();
                if (progressValue <= progressMaxValue) {
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                } else {
                    finishRecord();
                }
            }
        };

        // Action.DOWN -> 录制开始  Action.UP -> 录制结束
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isRecording = true;
                    startRecordTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(0);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    long now = System.currentTimeMillis();
                    // 判断是否是长按录制
                    // 如果是长按录制 则主动finish掉
                    if (now - startRecordTime > ViewConfiguration.getLongPressTimeout()) {
                        finishRecord();
                    }
                    handler.removeCallbacksAndMessages(null);
                    isRecording = false;
                    startRecordTime = 0;
                    progressValue = 0;
                    postInvalidate();
                }
                return false;
            }
        });

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    private void finishRecord() {
        if (onRecordListener != null) {
            onRecordListener.onFinish();
        }
    }

    public void setMaxDuration(int maxDuration) {
        this.progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    private OnRecordListener onRecordListener;

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    @Override
    public void onClick(View v) {
        if (onRecordListener != null) {
            onRecordListener.onClick();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onRecordListener != null) {
            onRecordListener.onLongClick();
        }
        return true;
    }

    public interface OnRecordListener {
        void onClick();

        void onLongClick();

        void onFinish();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 得到组件宽高
        int width = getWidth();
        int height = getHeight();

        // 判断视频是否正在录制
        // 如果已经开始，需要按照组件的宽高绘制圆圈，否则需要根据传递进来的radius的大小进行圆绘制
        if (isRecording) {

            canvas.drawCircle(width / 2f, height / 2f, width / 2f, fillPaint);
            // 绘制圆环进度条
            int left = progressWidth / 2;
            int top = progressWidth / 2;
            int right = width - progressWidth / 2;
            int bottom = height - progressWidth / 2;
            float sweepAngle = (progressValue * 1.0f / progressMaxValue) * 360;
            canvas.drawArc(left, top, right, bottom, -90, sweepAngle, false, progressPaint);
        } else {
            canvas.drawCircle(width / 2f, height / 2f, radius, fillPaint);
        }

    }
}