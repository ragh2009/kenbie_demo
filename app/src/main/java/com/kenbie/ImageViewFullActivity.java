package com.kenbie;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.kenbie.listeners.SwipeListenerInterface;
import com.kenbie.model.OptionsData;
import com.kenbie.views.SwipeListener;

public class ImageViewFullActivity extends AppCompatActivity implements SwipeListenerInterface {
    private OptionsData imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);

        initData();
        SwipeListener sl = new SwipeListener(this);
        ((RelativeLayout) findViewById(R.id.gallery_view_close)).setOnTouchListener(sl);

        ImageView imgCross = (ImageView) findViewById(R.id.img_cross);
        imgCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAction(null);
            }
        });

        RequestOptions options = new RequestOptions()
                .optionalCenterInside()
                .priority(Priority.HIGH);
        ImageView touchImageView = (ImageView) findViewById(R.id.media_display);
        if (imageData.getId() == -1)
           Glide.with(this).load(imageData.getOptionCode()).apply(options).into(touchImageView);
        else
            Glide.with(this).load(imageData.getName()).apply(options).into(touchImageView);
    }

    private void initData() {
        try {
            imageData = (OptionsData) getIntent().getSerializableExtra("MediaData");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRightToLeftSwipe(View v) {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    @Override
    public void onLeftToRightSwipe(View v) {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    @Override
    public void onTopToBottomSwipe(View v) {

    }

    @Override
    public void onBottomToTopSwipe(View v) {

    }

    public void getAction(OptionsData value) {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }
}
