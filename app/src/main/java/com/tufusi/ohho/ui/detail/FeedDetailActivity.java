package com.tufusi.ohho.ui.detail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.tufusi.ohho.R;
import com.tufusi.ohho.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {

    public static final String KEY_FEED = "key_feed";
    public static final String KEY_LIFE_TAG = "key_life_tag";

    private Feed feed;
    private ViewHandler viewHandler = null;

    public static void startFeedDetailActivity(Context context, Feed item, String lifeTag) {
        Intent intent = new Intent(context, FeedDetailActivity.class);
        intent.putExtra(KEY_FEED, item);
        intent.putExtra(KEY_LIFE_TAG, lifeTag);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feed = (Feed) getIntent().getSerializableExtra(KEY_FEED);
        if (feed == null) {
            finish();
            return;
        }

        if (feed.getItemType() == Feed.IMAGE_TYPE) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }

        viewHandler.bindInitData(feed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewHandler != null) {
            viewHandler.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewHandler != null) {
            viewHandler.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (viewHandler != null) {
            viewHandler.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (viewHandler != null) {
            viewHandler.onActivityResult(requestCode, resultCode, data);
        }
    }
}