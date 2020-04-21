package com.kenbie;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kenbie.connection.MConnection;
import com.kenbie.util.Utility;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class KenbieBaseActivity extends AppCompatActivity {
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    public double longitude = 0, latitude = 0;
    public SharedPreferences mPref = null;
    public Utility utility;
    public int position = 0, uType;
    public MConnection mConnection;
    public String ip, deviceId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initUtils();
        super.onCreate(savedInstanceState);
    }

    private void initUtils() {
        try {
            mPref = getSharedPreferences("kPrefs", MODE_PRIVATE);
            uType = mPref.getInt("UserType", 1);
            position = uType - 1;
            utility = new Utility();
            mConnection = new MConnection();

            initData();
            if (mPref.getBoolean("isLogin", false)) {
                latitude = mPref.getFloat("latitude", 0);
                longitude = mPref.getFloat("longitude", 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        try {
            ip = utility.getIpAddress(this);
            if (ip == null) ip = "";
            deviceId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            ip = "";
            e.printStackTrace();
        }
    }

    private void initLocations() {
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public boolean isOnline() {
        boolean isConnected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            isConnected = cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception ex) {
            isConnected = false;
        }
        return isConnected;
    }

    public void showToast(Context applicationContext, String string) {
        Toast.makeText(applicationContext, string, Toast.LENGTH_LONG).show();
    }

    public boolean isValidEmail(String email, Context context) {
        String EMAIL_REGEX = context.getResources().getString(R.string.email_val);
        Boolean b = email.matches(EMAIL_REGEX);
        System.out.println("is e-mail: " + email + " :Valid = " + b);
        return b;
    }

    public void hideKeyboard(Context context, EditText mEditText) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show dialog with message and title
    public void showMessageWithTitle(Context context, String title, String message) {
        try {
            new AlertDialog.Builder(context)
                    .setTitle("")
                    .setMessage(Html.fromHtml(message))
                    .setPositiveButton(mPref.getString("21", "Yes"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(R.mipmap.ic_stat_notification)
                    .show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(KenbieBaseActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
