package com.tufusi.ohho.ui.find;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.app.AppRouteConfig;
import com.tufusi.ohho.model.SofaTab;
import com.tufusi.ohho.ui.sofa.SofaFragment;

@FragmentDestination(pageUrl = "main/tabs/find", asStarter = false, needLogin = false)
public class FindFragment extends SofaFragment {

    @Override
    public Fragment getTabFragment(int position) {
        SofaTab.Tabs tab = getTabConfig().getTabs().get(position);
        TagListFragment fragment = TagListFragment.newInstance(tab.getTag());
        return fragment;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        assert childFragment.getArguments() != null;
        String tagType = childFragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            ViewModelProviders.of(childFragment).get(TagListViewModel.class)
                    .getSwitchLiveData().observe(this, new Observer() {
                @Override
                public void onChanged(Object o) {
                    viewPager2.setCurrentItem(1);
                }
            });
        }
    }

    @Override
    public SofaTab getTabConfig() {
        return AppRouteConfig.getFindTabConfig();
    }
}