package com.tufusi.libnetwork.restful;

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
        Request request = builder.get().url(UrlCreator.createUrlFromParams(mUrl, params)).build();
        return request;
    }
}