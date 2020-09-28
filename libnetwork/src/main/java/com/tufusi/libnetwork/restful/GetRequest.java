package com.tufusi.libnetwork.restful;

import android.util.Log;

import com.tufusi.libnetwork.OhRequest;
import com.tufusi.libnetwork.manager.UrlCreator;

import okhttp3.Request;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description
 */
public class GetRequest<T> extends OhRequest<T, GetRequest> {

    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected Request generateRequest(Request.Builder builder) {
        String url = UrlCreator.createUrlFromParams(mUrl, params);
        Request request = builder.get().url(url).build();
        return request;
    }
}