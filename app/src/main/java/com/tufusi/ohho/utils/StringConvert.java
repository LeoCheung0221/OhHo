package com.tufusi.ohho.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

/**
 * Created by 鼠夏目 on 2020/9/26.
 *
 * @author 鼠夏目
 * @description
 */
public class StringConvert {

    public static String convertFeedUgc(int count) {
        if (count > 100000) {
            return count / 10000 + "万";
        }
        return String.valueOf(count);
    }

    public static String convertTagFeedListNum(int count) {
        if (count < 10000) {
            return count + "人观看";
        }
        return count / 10000 + "万人观看";
    }

    public static CharSequence convertSpannable(int count, String intro) {
        String countStr = String.valueOf(count);
        SpannableString ss = new SpannableString(countStr + intro);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new AbsoluteSizeSpan(16, true), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

} 