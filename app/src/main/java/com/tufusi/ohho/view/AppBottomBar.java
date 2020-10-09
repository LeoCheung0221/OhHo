package com.tufusi.ohho.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.tufusi.ohho.R;
import com.tufusi.ohho.model.BottomBar;
import com.tufusi.ohho.model.Destination;
import com.tufusi.ohho.app.AppRouteConfig;
import com.tufusi.libcommon.utils.ScreenUtils;

import java.util.List;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description 继承 BottomNavigationView，可以动态配置
 */
public class AppBottomBar extends BottomNavigationView {

    private static int[] sIcons = new int[]{
            R.drawable.icon_tab_home,
            R.drawable.icon_tab_sofa,
            R.drawable.icon_tab_publish,
            R.drawable.icon_tab_find,
            R.drawable.icon_tab_mine
    };

    public AppBottomBar(@NonNull Context context) {
        this(context, null);
    }

    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        BottomBar bottomBar = AppRouteConfig.getBottomMenuConfig();
        List<BottomBar.TabsBean> tabs = bottomBar.tabs;

        // 定义二维数组用来承载底部按钮选中与不被选中的两种颜色状态
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        // 初始化两种状态颜色值
        int[] colors = new int[]{Color.parseColor(bottomBar.activeColor), Color.parseColor(bottomBar.inActiveColor)};

        // 存入ColorStateList
        ColorStateList stateList = new ColorStateList(states, colors);
        setItemIconTintList(stateList);
        setItemTextColor(stateList);
        // 所有按钮无论何种状态都会显示按钮
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        setSelectedItemId(bottomBar.selectTab);

        // 遍历集合，将按钮添加到bottomBar上
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.TabsBean tabBean = tabs.get(i);
            // 如果不可用，则不可见
            if (!tabBean.enable) {
                return;
            }

            int id = getId(tabBean.pageUrl);
            if (id < 0) {
                return;
            }
            MenuItem menuItem = getMenu().add(0, id, tabBean.index, tabBean.title);
            menuItem.setIcon(sIcons[tabBean.index]);
        }

        // 不在上面的循环中设置导航栏icon大小，是因为
        // getMenu().add - MenuBuilder#add - addInternal - onItemsChanged(true) - BottomNavigationPresenter#updateMenuView - BottomNavigationMenuView#buildMenuView
        // 如果不等布局绘制好，直接设置会被remove掉，从而设置无效
        for (BottomBar.TabsBean tab : tabs) {
            int iconSize = ScreenUtils.dip2px(tab.size);

            // BottomNavigationView 直接子View : BottomNavigationMenuView
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            // 继续寻找 BottomNavigationItemView
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.index);

            itemView.setIconSize(iconSize);

            if (TextUtils.isEmpty(tab.title)) {
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)));
                itemView.setShifting(false);
            }
        }
    }

    private int getId(String pageUrl) {
        Destination destination = AppRouteConfig.getRouteConfig().get(pageUrl);
        if (destination == null) {
            return -1;
        }
        return destination.id;
    }
}