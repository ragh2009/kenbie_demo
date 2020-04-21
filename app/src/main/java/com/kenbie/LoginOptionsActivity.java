package com.kenbie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kenbie.util.Constants;

public class LoginOptionsActivity extends KenbieBaseActivity {
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_options);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        token = FirebaseInstanceId.getInstance().getToken();

        //  bindTitle();
        initUiSetup();
    }

    private void initUiSetup() {
        try {
            TextView btnLogin = findViewById(R.id.btn_login);
            btnLogin.setText(mPref.getString("2", "Log In"));
            btnLogin.setTypeface(KenbieApplication.S_NORMAL);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LoginOptionsActivity.this, LoginActivity.class);
                    i.putExtra("Type", 1);
                    i.putExtra("IsBack", true);
                    startActivity(i);
                    finish();
                }
            });

            TextView titleWelcome = findViewById(R.id.title_welcome);
            titleWelcome.setText(mPref.getString("3", "Welcome to Kenbie"));
            titleWelcome.setTypeface(KenbieApplication.S_SEMI_BOLD);

            TextView btnDiscover = findViewById(R.id.btn_discover);
            btnDiscover.setText(mPref.getString("4", "Discover"));
            btnDiscover.setTypeface(KenbieApplication.S_SEMI_BOLD);
            btnDiscover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putBoolean("GuestLogin", true);
                    editor.apply();
                    Intent guestIntent = new Intent(LoginOptionsActivity.this, KenbieActivity.class);
                    guestIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(guestIntent);
                    finish();
                }
            });

            TextView btnCreate = findViewById(R.id.btn_create);
            btnCreate.setText(mPref.getString("5", "Create Account"));
            btnCreate.setTypeface(KenbieApplication.S_SEMI_BOLD);
            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginOptionsActivity.this, SignUpActivity.class);
                    intent.putExtra("IsBack", true);
                    intent.putExtra("social_type", 0);
                    intent.putExtra("android_token", token == null ? "" : token);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

            TextView btnMoreOptions = findViewById(R.id.btn_more_options);
            btnMoreOptions.setText(mPref.getString("6", "More Options"));
            btnMoreOptions.setTypeface(KenbieApplication.S_NORMAL);
            btnMoreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LoginOptionsActivity.this, LoginActivity.class);
                    i.putExtra("IsBack", true);
                    i.putExtra("Type", 2);
                    startActivity(i);
                    finish();
                }
            });

            TextView titleLMsg = findViewById(R.id.title_l_msg);
            titleLMsg.setText(mPref.getString("7", "Kenbie is a trusted place to list, discover and book models around the world"));
            titleLMsg.setTypeface(KenbieApplication.S_NORMAL);

            TextView btnTerms = findViewById(R.id.btn_terms);
            btnTerms.setText(mPref.getString("8", "Terms of Service"));
            btnTerms.setTypeface(KenbieApplication.S_NORMAL);
            btnTerms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(LoginOptionsActivity.this, KenbieWebActivity.class);
//                    intent.putExtra("Type", 2);
//                    intent.putExtra("URL", Constants.TERMS_URL);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
                }
            });

            TextView titleEnd = findViewById(R.id.title_end);
            titleEnd.setTypeface(KenbieApplication.S_NORMAL);

            TextView btnPrivacy = findViewById(R.id.btn_privacy);
            btnPrivacy.setText(mPref.getString("9", "Privacy Policy"));
            btnPrivacy.setTypeface(KenbieApplication.S_NORMAL);
            btnPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(LoginOptionsActivity.this, KenbieWebActivity.class);
//                    intent.putExtra("Type", 1);
//                    intent.putExtra("URL", Constants.PRIVACY_URL);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
