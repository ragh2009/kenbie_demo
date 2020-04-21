package com.kenbie;

import android.app.Application;
import android.graphics.Typeface;

import com.android.volley.RequestQueue;
import com.twitter.sdk.android.core.Twitter;

/**
 * Created by rajaw on 5/25/2017.
 */

public class KenbieApplication extends Application {
    public static final String TAG = KenbieApplication.class.getSimpleName();
    private RequestQueue mRequestQueue;
    public static Typeface S_NORMAL = null;
    public static Typeface S_SEMI_LIGHT = null;
    public static Typeface S_SEMI_BOLD = null;
    public static Typeface S_BOLD = null;
    public static int galleryIndex = 0;

    @Override
    public void onCreate() {
        initCustomFonts();
        super.onCreate();

        Twitter.initialize(this);
    }


    // Initialization Custom Fonts
    private void initCustomFonts() {
        // TODO Auto-generated method stub
        S_NORMAL = Typeface.createFromAsset(getAssets(), "segoeui.ttf");
        S_SEMI_LIGHT = Typeface.createFromAsset(getAssets(), "segoeuisl.ttf");
        S_SEMI_BOLD = Typeface.createFromAsset(getAssets(), "seguisb.ttf");
        S_BOLD = Typeface.createFromAsset(getAssets(), "segoeuib.ttf");
    }

    public static synchronized KenbieApplication getInstance() {
//        return mInstance;
        return null;
    }

}

