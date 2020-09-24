package com.tufusi.libnetwork.parse;

import java.lang.reflect.Type;

/**
 * Created by 鼠夏目 on 2020/9/24.
 *
 * @author 鼠夏目
 * @description
 */
public interface IConvert<T> {

    /**
     * 按 类型来解析
     *
     * @param response 返回json串
     * @param type     对象类型
     * @return 实体对象
     */
    T convert(String response, Type type);

    /**
     * 按类来解析
     *
     * @param response 返回json串
     * @param clz      对象类型
     * @return 实体对象
     */
    T convert(String response, Class clz);

} 