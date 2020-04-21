package com.kenbie;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kenbie.fragments.MessageUserListFragment;
import com.kenbie.model.UserItem;
import com.kenbie.util.Constants;

import java.util.ArrayList;
import java.util.Map;

public class KenbieActivity extends KenbieBaseActivity {
    public static final String NOTIFY_ACTIVITY_ACTION = "notify_message";
    private int type = 0;
    private ImageView searchBtn, backButton, userImg;
    private TextView mTitle;
    private ProgressDialog mProgress;
    private LinearLayout guestBottomOptions;
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kenbie_main);

        mProgress = new ProgressDialog(this, R.style.MyAlertDialogStyle);
//        mProgress.setMessage("Please wait...");
        mProgress.setIndeterminate(false);
        mProgress.setCancelable(true);
        mProgress.setCanceledOnTouchOutside(false);

        backButton = findViewById(R.id.back_button);
        if (mPref.getBoolean("isLogin", false))
            backButton.setBackgroundResource(R.drawable.ic_v_back);
        else
            backButton.setBackgroundResource(R.drawable.ic_v_gray_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 0) {
                    if (!mPref.getBoolean("isLogin", false)) {
                        Intent i = new Intent(KenbieActivity.this, LoginOptionsActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    } else
                        finish();
                } else
                    onBackPressed();
            }
        });

        mTitle = (TextView) findViewById(R.id.m_title);
        mTitle.setTypeface(KenbieApplication.S_NORMAL);

        userImg = (ImageView) findViewById(R.id.user_img);

        searchBtn = (ImageView) findViewById(R.id.action_search);
        searchBtn.setVisibility(View.VISIBLE);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mPref.getBoolean("isLogin", false))
//                    viewSettings();
            }
        });

        guestBottomOptions = findViewById(R.id.guest_bottom_options);

        TextView btnLogin = findViewById(R.id.btn_login);
        btnLogin.setText(mPref.getString("306", "LOG IN"));
        btnLogin.setTypeface(KenbieApplication.S_SEMI_BOLD);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(KenbieActivity.this, LoginActivity.class);
                i.putExtra("Type", 1);
                i.putExtra("IsBack", true);
                startActivity(i);
                finish();
            }
        });

        TextView btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setText(mPref.getString("307", "SIGN UP"));
        btnSignUp.setTypeface(KenbieApplication.S_SEMI_BOLD);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KenbieActivity.this, SignUpActivity.class);
                intent.putExtra("social_type", 0);
                intent.putExtra("android_token", token == null ? "" : token);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
//                finish();
            }
        });

        replaceFragment(new MessageUserListFragment(), false, false);
    }

    public void replaceFragment(Fragment fragment, boolean needToAddBackStack, boolean clearStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (needToAddBackStack && !clearStack) {
//            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            ft.replace(R.id.container, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
        } else {
            ft.replace(R.id.container, fragment).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPref.getBoolean("isLogin", false)) {
            guestBottomOptions.setVisibility(View.GONE);
        } else {
            token = FirebaseInstanceId.getInstance().getToken();
        }
        try {
            if (mMessageReceiver != null)
                registerReceiver(mMessageReceiver, new IntentFilter(NOTIFY_ACTIVITY_ACTION));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStop() {
        if (mMessageReceiver != null)
            unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
//            String message = intent.getStringExtra("message");

            if (intent != null && intent.getAction() != null && intent.getAction().equals(NOTIFY_ACTIVITY_ACTION)) {
          //      gettingProfileComplete();

                try {
                    MessageUserListFragment messageUserListFragment = (MessageUserListFragment)
                            getSupportFragmentManager().findFragmentById(R.id.container);
                    if (messageUserListFragment != null)
                        messageUserListFragment.refreshFromNotification();
//                    notificationManager.cancel(NOTIFICATION_ID);
                } catch (Exception e) {
                }

            }
        }
    };


    public void showProgressDialog(boolean isShow) {
        try {
            if (isShow) {
//                mProgress.setMessage("Please wait...");
                mProgress.show();
            } else
                mProgress.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgress != null && mProgress.isShowing())
            mProgress.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("Notification", false)) {
//            removeAllNotificationData();
            getIntent().putExtra("Notification", false);
            getIntent().putExtra("type", 0);
            replaceFragment(new MessageUserListFragment(), false, false);
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}
