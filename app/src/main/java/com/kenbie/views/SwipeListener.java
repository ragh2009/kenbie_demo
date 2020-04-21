package com.kenbie.views;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.kenbie.listeners.SwipeListenerInterface;

public class SwipeListener implements View.OnTouchListener {

    private SwipeListenerInterface activity;
    private float downX, downY, upX, upY;
    private String logTag = "GallerySwipe";
    private int swipeRestrictionX, swipeRestrictionY;

    public SwipeListener(SwipeListenerInterface activity) {
        this.activity = activity;
    }

    public void onRightToLeftSwipe(View v) {
        Log.i(logTag, "RightToLeftSwipe!");
        activity.onRightToLeftSwipe(v);
    }

    public void onLeftToRightSwipe(View v) {
        Log.i(logTag, "LeftToRightSwipe!");
        activity.onLeftToRightSwipe(v);
    }

    public void onTopToBottomSwipe(View v) {
        Log.i(logTag, "TopToBottomSwipe!");
        activity.onTopToBottomSwipe(v);
    }

    public void onBottomToTopSwipe(View v) {
        Log.i(logTag, "BottomToTopSwipe!");
        activity.onBottomToTopSwipe(v);
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if (deltaX < 0) {
                    this.onLeftToRightSwipe(v);
                    return true;
                }
                if (deltaX > 0) {
                    this.onRightToLeftSwipe(v);
                    return true;
                }

                if (deltaY < 0) {
                    this.onTopToBottomSwipe(v);
                    return true;
                }
                if (deltaY > 0) {
                    this.onBottomToTopSwipe(v);
                    return true;
                }

            }
        }
        return false;
    }

    public void setSwipeRestrictions(int swipeRestrictionX, int swipeRestrictionY) {
        this.swipeRestrictionX = swipeRestrictionX;
        this.swipeRestrictionY = swipeRestrictionY;
    }
}