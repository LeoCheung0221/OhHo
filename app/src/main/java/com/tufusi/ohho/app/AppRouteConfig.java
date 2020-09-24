package com.tufusi.ohho.app;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tufusi.libcommon.AppGlobal;
import com.tufusi.ohho.model.BottomBar;
import com.tufusi.ohho.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description 页面路径配置管理类
 */
public class AppRouteConfig {

    private static HashMap<String, Destination> sDestConfig;
    private static BottomBar sBottomBarConfig;

    /**
     * 获取页面路径路由配置
     */
    public static HashMap<String, Destination> getRouteConfig() {
        if (sDestConfig == null) {
            String fileContent = parseFile("dest.json");
            sDestConfig = JSON.parseObject(fileContent, new TypeReference<HashMap<String, Destination>>() {
            }.getType());
        }
        return sDestConfig;
    }

    /**
     * 获取底部导航栏相关配置
     */
    public static BottomBar getBottomMenuConfig() {
        if (sBottomBarConfig == null) {
            String content = parseFile("main_tabs_config.json");
            sBottomBarConfig = JSON.parseObject(content, BottomBar.class);
        }
        return sBottomBarConfig;
    }

    /**
     * 获取 assets 下的 fileName/dest.json 并解析实例
     *
     * @param fileName 文件路径
     */
    private static String parseFile(String fileName) {
        AssetManager assetManager = AppGlobal.getsApplication().getAssets();
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            inputStream = assetManager.open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

} 