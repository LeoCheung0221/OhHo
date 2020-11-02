package com.tufusi.ohho.ui.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.tufusi.libcommon.utils.StatusBarUtils;
import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.R;
import com.tufusi.ohho.app.UserManager;
import com.tufusi.ohho.databinding.FragmentMyBinding;
import com.tufusi.ohho.model.User;

@FragmentDestination(pageUrl = "main/tabs/my", asStarter = false, needLogin = true)
public class MyFragment extends Fragment implements View.OnClickListener {

    private FragmentMyBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMyBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        mBinding.setUser(user);

        UserManager.get().refresh().observe(this, newUser -> {
            if (newUser != null) {
                mBinding.setUser(newUser);
            }
        });

        mBinding.actionLogout.setOnClickListener(v -> toastExitDialog());

        mBinding.goDetail.setOnClickListener(this);
        mBinding.userFeed.setOnClickListener(this);
        mBinding.userComment.setOnClickListener(this);
        mBinding.userFavorite.setOnClickListener(this);
        mBinding.userHistory.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_detail:
                ProfileActivity.startProfileActivity(getContext(), ProfileActivity.TAB_TYPE_ALL);
                break;
            case R.id.user_feed:
                ProfileActivity.startProfileActivity(getContext(), ProfileActivity.TAB_TYPE_FEED);
                break;
            case R.id.user_comment:
                ProfileActivity.startProfileActivity(getContext(), ProfileActivity.TAB_TYPE_COMMENT);
                break;
            case R.id.user_favorite:
                UserBehaviorListActivity.startBehaviorListActivity(getContext(), UserBehaviorListActivity.BEHAVIOR_FAVORITE);
                break;
            case R.id.user_history:
                UserBehaviorListActivity.startBehaviorListActivity(getContext(), UserBehaviorListActivity.BEHAVIOR_HISTORY);
                break;
            default:
                break;
        }
    }

    private void toastExitDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.fragment_my_logout))
                .setPositiveButton(getString(R.string.fragment_my_logout_ok), (dialog, which) -> {
                    dialog.dismiss();
                    UserManager.get().logout();
                    getActivity().onBackPressed();
                }).setNegativeButton(getString(R.string.fragment_my_logout_cancel), null)
                .create().show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtils.lightStatusBar(getActivity(), false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        StatusBarUtils.lightStatusBar(getActivity(), hidden);
    }

}