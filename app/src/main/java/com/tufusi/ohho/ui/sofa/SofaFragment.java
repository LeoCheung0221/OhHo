package com.tufusi.ohho.ui.sofa;

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

import com.tufusi.libnavannotation.FragmentDestination;
import com.tufusi.ohho.R;

@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false, needLogin = false)
public class SofaFragment extends Fragment {

    private SofaViewModel sofaViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sofaViewModel =
                ViewModelProviders.of(this).get(SofaViewModel.class);
        View root = inflater.inflate(R.layout.fragment_sofa, container, false);
        final TextView textView = root.findViewById(R.id.text_sofa);
        sofaViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}