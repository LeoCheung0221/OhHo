package com.tufusi.ohho.ui.detail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.tufusi.ohho.R;
import com.tufusi.ohho.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {

    private static final String KEY_FEED = "key_feed";
    private static final String KEY_LIFE_TAG = "key_life_tag";

    private Feed feed;

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

        ViewHandler viewHandler = null;
        if (feed.getItemType() == Feed.IMAGE_TYPE) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }

        viewHandler.bindInitData(feed);
    }
}