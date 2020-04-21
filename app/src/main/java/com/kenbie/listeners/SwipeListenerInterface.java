package com.kenbie.listeners;

import android.view.View;

public interface SwipeListenerInterface {

    void onRightToLeftSwipe(View v);

    void onLeftToRightSwipe(View v);

    void onTopToBottomSwipe(View v);

    void onBottomToTopSwipe(View v);
}