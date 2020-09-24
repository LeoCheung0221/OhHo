package com.tufusi.libnetwork.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by 鼠夏目 on 2020/9/24.
 *
 * @author 鼠夏目
 * @description
 */
public class JsonConvert implements IConvert {

    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject object = jsonObject.getJSONObject("data");
        if (object != null) {
            Object data = object.get("data");
            return JSON.parseObject(String.valueOf(data), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject object = jsonObject.getJSONObject("data");
        if (object != null) {
            Object data = object.get("data");
            return JSON.parseObject(String.valueOf(data), clz);
        }
        return null;
    }
}