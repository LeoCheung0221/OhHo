package com.tufusi.ohho.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description
 */
public class BottomBar {

    /**
     * activeColor : #333333
     * inActiveColor : #666666
     * selectTab : 0
     * tabs : [{"size":24,"enable":true,"index":0,"pageUrl":"main/tabs/home","title":"首页","needLogin":false,"permissionLv":0},{"size":24,"enable":true,"index":1,"pageUrl":"main/tabs/sofa","title":"沙发","needLogin":false,"permissionLv":0},{"size":40,"enable":true,"index":2,"tintColor":"#ff678f","pageUrl":"main/tabs/publish","title":"","needLogin":true,"permissionLv":0},{"size":24,"enable":true,"index":3,"pageUrl":"main/tabs/find","title":"发现","needLogin":false,"permissionLv":0},{"size":24,"enable":true,"index":4,"pageUrl":"main/tabs/my","title":"我的","needLogin":true,"permissionLv":0}]
     */

    public String activeColor;
    public String inActiveColor;
    public int selectTab;
    public List<TabsBean> tabs;

    public static class TabsBean {
        /**
         * size : 24
         * enable : true
         * index : 0
         * pageUrl : main/tabs/home
         * title : 首页
         * permissionLv : 0
         * tintColor : #ff678f
         */

        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;
        public int permissionLv;
        public String tintColor;
    }
}