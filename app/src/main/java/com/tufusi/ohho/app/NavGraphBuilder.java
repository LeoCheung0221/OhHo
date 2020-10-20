package com.tufusi.ohho.app;

import android.content.ComponentName;
import android.content.Context;

import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.tufusi.libcommon.AppGlobal;
import com.tufusi.ohho.app.navigator.FixFragmentNavigator;
import com.tufusi.ohho.model.Destination;

import java.util.HashMap;

/**
 * Created by 鼠夏目 on 2020/9/22.
 *
 * @author 鼠夏目
 * @description
 */
public class NavGraphBuilder {

    public static void builder(Context context, FragmentManager manager, int containerId, NavController controller) {
        // 首先拿到 NavigatorProvider 对象
        NavigatorProvider nProvider = controller.getNavigatorProvider();
        ActivityNavigator activityNavigator = nProvider.getNavigator(ActivityNavigator.class);

        // 自定义内置导航器
        FixFragmentNavigator fragmentNavigator = new FixFragmentNavigator(context, manager, containerId);
        nProvider.addNavigator(fragmentNavigator);

        NavGraph navGraph = new NavGraph(new NavGraphNavigator(nProvider));

        HashMap<String, Destination> destHashMap = AppRouteConfig.getRouteConfig();
        for (Destination value : destHashMap.values()) {
            if (value.isFragment) {
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setId(value.id);
                destination.setClassName(value.className);
                destination.addDeepLink(value.pageUrl);

                navGraph.addDestination(destination);
            } else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(value.id);
                destination.setComponentName(new ComponentName(AppGlobal.getApplication().getPackageName(), value.className));
                destination.addDeepLink(value.pageUrl);

                navGraph.addDestination(destination);
            }

            if (value.asStarter) {
                navGraph.setStartDestination(value.id);
            }
        }

        controller.setGraph(navGraph);
    }

} 