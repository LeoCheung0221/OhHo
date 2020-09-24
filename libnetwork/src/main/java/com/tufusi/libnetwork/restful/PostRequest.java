package com.tufusi.libnetwork.restful;

import com.tufusi.libnetwork.OhRequest;
import com.tufusi.libnetwork.manager.UrlCreator;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description
 */
public class PostRequest<T> extends OhRequest<T, PostRequest> {

    public PostRequest(String url) {
        super(url);
    }

    @Override
    protected Request generateRequest(Request.Builder builder) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            bodyBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        Request request = builder.url(mUrl).post(bodyBuilder.build()).build();
        return request;
    }
}