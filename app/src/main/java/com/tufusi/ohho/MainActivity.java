package com.tufusi.ohho;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AppConfig;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tufusi.libcommon.utils.StatusBarUtils;
import com.tufusi.ohho.app.AppRouteConfig;
import com.tufusi.ohho.app.NavGraphBuilder;
import com.tufusi.ohho.model.Destination;
import com.tufusi.ohho.model.User;
import com.tufusi.ohho.app.UserManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        //启用沉浸式状态栏 白底黑字
        StatusBarUtils.fitSystemBar(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
        NavGraphBuilder.builder(this, getSupportFragmentManager(), fragment.getId(), navController);

        navView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        HashMap<String, Destination> config = AppRouteConfig.getRouteConfig();
        Iterator<Map.Entry<String, Destination>> iterator = config.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> entry = iterator.next();
            Destination value = entry.getValue();
            if (value != null && !UserManager.get().isLogin() && value.needLogin && value.id == menuItem.getItemId()) {
                UserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        Log.e("当前nav: ", menuItem.getTitle().toString());
                        navView.setSelectedItemId(menuItem.getItemId());
                    }
                });

                return false;
            }
        }

        navController.navigate(menuItem.getItemId());
        return !TextUtils.isEmpty(menuItem.getTitle());
    }

    @Override
    public void onBackPressed() {
//        boolean shouldIntercept = false;
//        int homeDestinationId = 0;
//
//        Fragment fragment = getSupportFragmentManager().getPrimaryNavigationFragment();
//        assert fragment != null;
//        String tag = fragment.getTag();
//        assert tag != null;
//        int currentPageDestId = Integer.parseInt(tag);
//        HashMap<String, Destination> routeConfig = AppRouteConfig.getRouteConfig();
//        Iterator<Map.Entry<String, Destination>> iterator = routeConfig.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Destination> next = iterator.next();
//            Destination destination = next.getValue();
//            if (!destination.asStarter && destination.id == currentPageDestId) {
//                shouldIntercept = true;
//            }
//
//            if (destination.asStarter) {
//                homeDestinationId = destination.id;
//            }
//        }
//
//        if (shouldIntercept && homeDestinationId > 0) {
//            navView.setSelectedItemId(homeDestinationId);
//            return;
//        }
//
//        super.onBackPressed();

        // 当前正在显示的页面destinationId
        int currentPageId = navController.getCurrentDestination().getId();
        // APP页面路导航结构图  首页的destinationId
        int homeDestId = navController.getGraph().getStartDestination();
        //如果当前正在显示的页面不是首页，而我们点击了返回键，则拦截。
        if (currentPageId != homeDestId) {
            navView.setSelectedItemId(homeDestId);
            return;
        }

        //否则 finish，此处不宜调用onBackPressed。因为navigation会操作回退栈,切换到之前显示的页面。
        finish();
    }
}