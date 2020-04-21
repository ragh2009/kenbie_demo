package com.kenbie;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.kenbie.connection.MConnection;
import com.kenbie.data.LanguageParser;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.model.MsgUserItem;
import com.kenbie.util.Constants;
import com.kenbie.util.Utility;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SplashActivity extends AppCompatActivity implements APIResponseHandler {
    private static int SPLASH_TIME_OUT = 3000;
    private SharedPreferences mPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mPref = getSharedPreferences("kPrefs", MODE_PRIVATE);

//        Log.d("DefaultSelectedLanguage", Locale.getDefault().getLanguage());

        if (handleBackgroundNotification()) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.cancelAll();
            finish();
        } else if (!mPref.getBoolean("SelLanguage", false) && new Utility().isOnline(this))
            getLanguage();
        else
            startApp();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        createNotificationChannel();
    }

    private boolean handleBackgroundNotification() {
        try {
            Intent intent = null;
            int type = 0;
            Bundle data = getIntent().getExtras();

            if (data != null && data.containsKey("type")) {
                type = Integer.valueOf((String) data.get("type"));
                if (type == 2) {
                    MsgUserItem value = new MsgUserItem();
                    if(data.containsKey("title"))
                        value.setUser_name((String) data.get("title"));
                    if(data.containsKey("sender_profile"))
                        value.setUser_img((String) data.get("sender_profile"));
                    value.setUid(Integer.valueOf((String) data.get("sender_id")));
                    intent = new Intent(this, MessageConvActivity.class);
                    intent.putExtra("MsgItem", value);
                    intent.putExtra("Notification", true);
//                    intent = new Intent(this, KenbieActivity.class);
//                    intent.putExtra("MsgItem", value);
//                    intent.putExtra("NavType", 8);
//                    intent.putExtra("Notification", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else {
                    intent = new Intent(this, KenbieActivity.class);
                    intent.putExtra("NavType", 2);
                    intent.putExtra("Notification", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
            }


/*            Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                for (String key : bundle.keySet()) {
                    Log.d("Bundle Debug", key + " = \"" + bundle.get(key) + "\"");
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void startApp() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = null;

                SharedPreferences.Editor editor = mPref.edit();
                editor.putString("DeviceId", Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID));
                editor.apply();

                if (mPref.getBoolean("isLogin", false))
                    i = new Intent(SplashActivity.this, KenbieActivity.class);
                else if (mPref.getBoolean("GuestLogin", false))
                    i = new Intent(SplashActivity.this, KenbieActivity.class);
                else {
                    i = new Intent(SplashActivity.this, LoginOptionsActivity.class);
                    i.putExtra("IsBack", true);
                }

                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    private void createNotificationChannel() {
        try {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
                channel.setDescription(description);
                channel.setShowBadge(true);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getError(String error, int APICode) {
        SPLASH_TIME_OUT = 1000;
        startApp();
    }

    @Override
    public void getResponse(String response, int APICode) {
        if (response != null)
            new LanguageParser().saveLanguageData(mPref, response);

        SPLASH_TIME_OUT = 1000;
        startApp();
    }

    @Override
    public void networkError(String error, int APICode) {
        SPLASH_TIME_OUT = 1000;
        startApp();
    }

    private void getLanguage() {
        Map<String, String> params = new HashMap<String, String>();
//        params.put("ip", new Utility().getIpAddress(this));
        params.put("lang", Locale.getDefault().getLanguage());
//        new MConnection().postRequestWithHttpHeaders(this, "langLoad", this, params, 101);
        new MConnection().postRequestWithHttpHeaders(this, "lang", this, params, 101);
    }

    private void PrintHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.kenbie", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, Base64.encodeToString(md.digest(), Base64.DEFAULT));
                startActivity(Intent.createChooser(intent, "Share with"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
