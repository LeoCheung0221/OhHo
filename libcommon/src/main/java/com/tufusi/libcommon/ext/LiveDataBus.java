package com.tufusi.libcommon.ext;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.tufusi.libcommon.utils.StatusBarUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 鼠夏目 on 2020/10/14.
 *
 * @author 鼠夏目
 * @description 事件总线
 */
public class LiveDataBus {

    private ConcurrentHashMap<String, StickyLiveData> mHashMap = new ConcurrentHashMap<>();

    private static class Lazy {
        static LiveDataBus sLiveDataBus = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return Lazy.sLiveDataBus;
    }

    /**
     * 获取粘性事件的 StickyLiveData
     */
    public StickyLiveData with(String eventName) {
        StickyLiveData liveData = mHashMap.get(eventName);
        if (liveData == null) {
            liveData = new StickyLiveData(eventName);
            mHashMap.put(eventName, liveData);
        }
        return liveData;
    }

    /**
     * 粘性事件
     */
    public class StickyLiveData<T> extends LiveData<T> {

        private String mEventName;
        private T mStickyData; // 粘性数据发送
        private int mDataVersion = 0; // 标记发送次数

        public StickyLiveData(String eventName) {
            mEventName = eventName;
        }

        @Override
        public void setValue(T value) {
            mDataVersion++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            mDataVersion++;
            super.postValue(value);
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            observerSticky(owner, observer, false);
        }

        public void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean isStick) {
            super.observe(owner, new WrapperObserver(this, observer, isStick));
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        mHashMap.remove(mEventName);
                    }
                }
            });
        }

        /**
         * 同步发送粘性事件
         */
        public void setStickyData(T stickyData) {
            mStickyData = stickyData;
            setValue(stickyData);
        }

        /**
         * 允许异步发送
         */
        public void postStickyData(T stickyData) {
            mStickyData = stickyData;
            postValue(stickyData);
        }

        private class WrapperObserver<T> implements Observer<T> {

            private StickyLiveData<T> mLiveData;
            private Observer<T> mObserver;
            private boolean isStick;

            // 标记LiveData发送数据次数，用来过滤老数据的重复接收
            private int mLastVersion = 0;

            public WrapperObserver(StickyLiveData<T> liveData, Observer<T> observer, boolean isStick) {
                this.mLiveData = liveData;
                this.mObserver = observer;
                this.isStick = isStick;

                mLastVersion = liveData.mDataVersion;
            }

            @Override
            public void onChanged(T t) {
                // 只要lastVersion不小于标记位，就需要考虑是否是粘性事件（先发送，后注册）
                if (mLastVersion >= mLiveData.mDataVersion) {
                    if (isStick && mLiveData.mStickyData != null) {
                        mObserver.onChanged(mLiveData.mStickyData);
                    }
                    return;
                }

                mLastVersion = mLiveData.mDataVersion;
                mObserver.onChanged(t);
            }
        }
    }
}