package com.tufusi.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.alibaba.fastjson.JSON;
import com.tufusi.cache.CacheManager;
import com.tufusi.libnetwork.manager.UrlCreator;
import com.tufusi.libnetwork.parse.IConvert;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description 实现 Cloneable 可以在同步请求中 做到先读取缓存再请求网络
 */
public abstract class OhRequest<T, R extends OhRequest> implements Cloneable {

    protected String mUrl;

    protected HashMap<String, String> headers = new HashMap<>();
    protected HashMap<String, Object> params = new HashMap<>();

    /**
     * 仅仅访问缓存 即使本地没有也不会访问网络
     */
    public static final int CACHE_ONLY = 1;
    /**
     * 优先访问缓存 再访问网络 成功后缓存到本地
     */
    public static final int CACHE_FIRST = 2;

    /**
     * 仅仅访问网络 不做任何存储
     */
    public static final int NET_ONLY = 3;

    /**
     * 优先访问网络，成功后缓存到本地
     */
    public static final int NET_CACHE = 4;

    private String mCacheKey;
    private Type mType;
    private Class mClass;
    private int mCacheStrategy;

    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE})
    public @interface CacheStrategy {
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    public OhRequest(String url) {
        this.mUrl = url;
    }

    public R addHeader(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }

        try {
            if (value.getClass() == String.class) {
                params.put(key, value);
            } else {
                // 八大基本类型利用反射 判断
                Field field = value.getClass().getField("TYPE");
                Class clazz = (Class) field.get(null);
                if (clazz.isPrimitive()) {
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
//        Log.i( "ohho_log_request: ", JSON.toJSONString(params));
        return (R) this;
    }

    public R cacheKey(String key) {
        this.mCacheKey = key;
        return (R) this;
    }

    /**
     * 有关泛型类型获取
     * 从 Java 5 开始，规定泛型信息要写到 java 文件中，没有显示明确声明泛型类型的，会被编译器编译时擦除掉。
     * 解决方案：
     * new interface 出传递的泛型类型运行时是可以获取的，
     * 因为编译时会生成 interface 的匿名内部类，内部类是明确显示声明的泛型的类型。也就不会被擦除
     */
    public OhResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("响应对象类型必须设置");
        }
        if (mCacheStrategy == CACHE_ONLY) {
            return readCache();
        }

        OhResponse<T> result;
        try {
            Response response = getCall().execute();
            result = parseResponse(response, null);
        } catch (IOException e) {
            e.printStackTrace();
            result = new OhResponse<>();
            result.message = e.getMessage();
        }
        Log.e("execute result: ", JSON.toJSONString(result));
        return result;
    }

    @SuppressLint("RestrictedApi")
    public void execute(final ResultCallback<T> callback) {
        if (mCacheStrategy != NET_ONLY) {
            //  子线程异步执行
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    OhResponse<T> response = readCache();
                    if (callback != null && response.body != null) {
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }

        if (mCacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    OhResponse<T> response = new OhResponse<>();
                    response.message = e.getMessage();
                    if (callback != null) {
                        callback.onError(response);
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    OhResponse<T> ohResponse = parseResponse(response, callback);
                    if (ohResponse.success) {
                        if (callback != null) {
                            callback.onSuccess(ohResponse);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError(ohResponse);
                        }
                    }
                }
            });
        }
    }

    private OhResponse<T> readCache() {
        String key = TextUtils.isEmpty(mCacheKey) ? generateCacheKey() : mCacheKey;
        Object cache = CacheManager.getCache(key);

        OhResponse<T> response = new OhResponse<>();
        response.success = true;
        //缓存码
        response.status = 304;
        response.message = "缓存获取成功";
        response.body = (T) cache;
        return response;
    }

    public R responseRawType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseRawType(Class claz) {
        mClass = claz;
        return (R) this;
    }

    /**
     * 解析响应实体对象
     *
     * @param response 响应结果
     * @param callback 结果回调
     * @return
     */
    private OhResponse<T> parseResponse(Response response, ResultCallback<T> callback) {
        String message = null;
        boolean success = response.isSuccessful();
        int status = response.code();
        OhResponse<T> result = new OhResponse<>();
        IConvert convert = ApiService.sConvert;
        try {
            ResponseBody body = response.body();
            String content = body.string();
            if (success) {
                if (callback != null) {
                    // 找出泛型原始类型
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) convert.convert(content, mType);
                } else if (mClass != null) {
                    result.body = (T) convert.convert(content, mClass);
                } else {
                    Log.e("OhRequest", "parseResponse: 无法解析");
                }
            } else {
                message = content;
            }
        } catch (Exception e) {
            message = e.getMessage();
            success = false;
            status = 0;
        }
        result.success = success;
        result.status = status;
        result.message = message;

        if (mCacheStrategy != NET_ONLY && result.success && result.body instanceof Serializable) {
            saveCache(result.body);
        }

        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(mCacheKey) ? generateCacheKey() : mCacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        mCacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return mCacheKey;
    }

    private Call getCall() {
        Request.Builder reqBuilder = new Request.Builder();
        addHeaders(reqBuilder);
        Request request = generateRequest(reqBuilder);

        return ApiService.mOkHttpClient.newCall(request);
    }

    protected abstract Request generateRequest(Request.Builder builder);

    private void addHeaders(Request.Builder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    @NonNull
    @Override
    public OhRequest clone() throws CloneNotSupportedException {
        return (OhRequest<T, R>) super.clone();
    }
}