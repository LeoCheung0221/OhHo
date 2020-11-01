package com.tufusi.ohho.utils;

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

} 