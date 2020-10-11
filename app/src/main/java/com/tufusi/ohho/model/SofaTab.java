package com.tufusi.ohho.model;

import java.util.List;

/**
 * Created by 鼠夏目 on 2020/10/11.
 *
 * @author 鼠夏目
 * @description
 */
public class SofaTab {


    /**
     * activeSize : 16
     * normalSize : 14
     * activeColor : #ED7282
     * normalColor : #666666
     * select : 0
     * tabGravity : 0
     * tabs : [{"title":"图片","index":0,"tag":"pics","enable":true},{"title":"视频","index":1,"tag":"video","enable":true},{"title":"文本","index":1,"tag":"text","enable":true}]
     */

    private int activeSize;
    private int normalSize;
    private String activeColor;
    private String normalColor;
    private int select;
    private int tabGravity;
    private List<Tabs> tabs;

    public int getActiveSize() {
        return activeSize;
    }

    public void setActiveSize(int activeSize) {
        this.activeSize = activeSize;
    }

    public int getNormalSize() {
        return normalSize;
    }

    public void setNormalSize(int normalSize) {
        this.normalSize = normalSize;
    }

    public String getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(String activeColor) {
        this.activeColor = activeColor;
    }

    public String getNormalColor() {
        return normalColor;
    }

    public void setNormalColor(String normalColor) {
        this.normalColor = normalColor;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public int getTabGravity() {
        return tabGravity;
    }

    public void setTabGravity(int tabGravity) {
        this.tabGravity = tabGravity;
    }

    public List<Tabs> getTabs() {
        return tabs;
    }

    public void setTabs(List<Tabs> tabs) {
        this.tabs = tabs;
    }

    public static class Tabs {
        /**
         * title : 图片
         * index : 0
         * tag : pics
         * enable : true
         */

        private String title;
        private int index;
        private String tag;
        private boolean enable;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }
}