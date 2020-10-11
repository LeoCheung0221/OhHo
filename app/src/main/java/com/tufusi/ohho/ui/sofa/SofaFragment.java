package com.tufusi.ohho.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.AppRouteConfig;
import com.tufusi.ohho.databinding.FragmentSofaBinding;
import com.tufusi.ohho.model.SofaTab;
import com.tufusi.ohho.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false, needLogin = false)
public class SofaFragment extends Fragment {

    private FragmentSofaBinding binding;
    private TabLayout sofaTab;
    private ViewPager2 viewPager2;
    private SofaTab tabConfig;
    private ArrayList<SofaTab.Tabs> tabs;

    private HashMap<Integer, Fragment> mFragmentMap = new HashMap<>();
    private TabLayoutMediator mediator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSofaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sofaTab = binding.sofaTab;
        viewPager2 = binding.viewPager;

        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for (SofaTab.Tabs tab : tabConfig.getTabs()) {
            if (tab.isEnable()) {
                tabs.add(tab);
            }
        }

        // 解除预加载机制
        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager2.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = mFragmentMap.get(position);
                if (fragment == null) {
                    fragment = getTabFragment(position);
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });
        sofaTab.setTabGravity(tabConfig.getTabGravity());

        mediator = new TabLayoutMediator(sofaTab, viewPager2, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(createTabView(position));
            }
        });
        mediator.attach();

        viewPager2.registerOnPageChangeCallback(mPageChangeCallback);

        viewPager2.post(() -> viewPager2.setCurrentItem(tabConfig.getSelect()));
    }

    ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = sofaTab.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = sofaTab.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {
                    customView.setTextSize(tabConfig.getActiveSize());
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.getNormalSize());
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };

    private View createTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(tabConfig.getActiveColor()), Color.parseColor(tabConfig.getNormalColor())};
        ColorStateList stateList = new ColorStateList(states, colors);

        tabView.setText(tabs.get(position).getTitle());
        tabView.setTextColor(stateList);
        tabView.setTextSize(tabConfig.getNormalSize());

        return tabView;
    }

    private Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).getTag());
    }

    private SofaTab getTabConfig() {
        return AppRouteConfig.getSofaTabConfig();
    }

    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager2.unregisterOnPageChangeCallback(mPageChangeCallback);
        super.onDestroy();
    }
}