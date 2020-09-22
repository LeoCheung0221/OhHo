package com.tufusi.ohho.ui.publish;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.tufusi.libnavannotation.ActivityDestination;
import com.tufusi.ohho.R;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
    }
}