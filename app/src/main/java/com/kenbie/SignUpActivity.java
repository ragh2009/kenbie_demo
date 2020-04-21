package com.kenbie;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kenbie.connection.FileUploader;
import com.kenbie.connection.MConnection;
import com.kenbie.fragments.SignUpStepOne;
import com.kenbie.fragments.SignUpStepThree;
import com.kenbie.fragments.SignUpStepTwo;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.model.LocationItem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends KenbieBaseActivity implements APIResponseHandler {
    public String sId = null, email = null, token = "", name, dob, imageUrl = null, password, phone="", imgPath = null, deviceId = "", companyName = "";
    public Bitmap profilePicBitmap = null;
    public int loginType = 0;
    public String ip;
    //    private float latitude = 0, longitude = 0;
    public LocationItem locationItem;
    public int step = 0, userType = 0, sexType = 0;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        mProgress = new ProgressDialog(this, R.style.MyAlertDialogStyle);
//        mProgress.setMessage("Please wait...");
        mProgress.setIndeterminate(false);
        mProgress.setCancelable(true);
        mProgress.setCanceledOnTouchOutside(false);

        sId = getIntent().getStringExtra("social_id");
        loginType = getIntent().getIntExtra("social_type", 0);
        name = getIntent().getStringExtra("first_name");
        token = FirebaseInstanceId.getInstance().getToken();
        email = getIntent().getStringExtra("Email");
        imageUrl = getIntent().getStringExtra("ImageUrl");
        dob = getIntent().getStringExtra("DOB");
        sexType = getIntent().getIntExtra("Gender", 0);

        ImageView backBtn = (ImageView) findViewById(R.id.back_button);
        backBtn.setBackgroundResource(R.drawable.ic_v_gray_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (step == 1 && getIntent().getBooleanExtra("IsBack", false)) {
                    Intent intent = new Intent(SignUpActivity.this, LoginOptionsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else
                    onBackPressed();

             /*
             else if (step != 0)
                    finish();
             if (step == 1) {
                    Intent intent = new Intent(SignUpActivity.this, LoginOptionsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    if (step > 1)
                        step = step - 1;
                    onBackPressed();
                }*/
            }
        });

        ((ImageView) findViewById(R.id.app_logo)).setVisibility(View.GONE);

        initData();
        startView(1);
    }

    private void initData() {
        try {
//            ip = utility.GetDeviceIpMobileData();
            ip = utility.getIpAddress(this);
            if (ip == null) ip = "";
            deviceId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startView(int type) {
        step = type;
//        if (loginType == 0) {
        if (type == 1) // Step 1
            replaceFragment(new SignUpStepOne(), false, false);
        else if (type == 2) // Step 2
            replaceFragment(new SignUpStepTwo(), true, false);
        else if (type == 3) // Step 3
            replaceFragment(new SignUpStepThree(), true, false);
//        } else {
//            if (type == 1) // Step 1
//                replaceFragment(new SignUpStepOne(), false, false);
//            else if (sexType == 0) {
//                replaceFragment(new SignUpStepTwo(), true, false);
//            } else {
//                gettingSocialSignUpDetails();
//            }
//        }
    }

    // Social sign up process
    public void gettingSocialSignUpDetails() {
        if (isOnline()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("social_id", sId);
            params.put("first_name", name);
            params.put("user_type", userType + "");
            params.put("android_token", token == null ? "" : token);
            params.put("social_type", loginType + "");
            params.put("device_id", deviceId == null ? "" : deviceId);
            if (email != null)
                params.put("email_id", email);

            params.put("birthday", dob);
            params.put("gender", sexType + "");
            params.put("phone", phone + "");
            if (locationItem != null) {
                params.put("latitude", locationItem.getLatitude() + "");
                params.put("longitude", locationItem.getLongitude() + "");
                params.put("city", locationItem.getCity() == null ? "" : locationItem.getCity());
                params.put("country", locationItem.getCountryId() + "");
            } else {
                params.put("latitude", "");
                params.put("longitude", "");
                params.put("city", "");
                params.put("country", "");
            }

            if (imageUrl != null)
                params.put("img_url", imageUrl);
            mConnection.postRequestWithHttpHeaders(SignUpActivity.this, "socialSignup", this, params, 104);
        } else {
            showProgressDialog(false);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    public void replaceFragment(final Fragment fragment, final boolean needToAddBackStack, final boolean clearStack) {
        try {
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction ft = fm.beginTransaction();
            if (needToAddBackStack && !clearStack) {
//                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                ft.replace(R.id.frame_layout, fragment).addToBackStack(fragment.getClass().getSimpleName()).commit();
            } else {
                ft.replace(R.id.frame_layout, fragment).commitAllowingStateLoss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Start navigation
    public void nextNavigation(int nType, int userType1) {
        if (nType == 1)
            startView(1);
        else if (nType == 2) {
            userType = userType1;
            startView(2);
        } else if (nType == 3) {
            sexType = userType1;
            startView(3);
        } else if (nType == 4) {
            startSignUpProcess(101);
        }
    }

    public void startSignUpProcess(final int APICode) {
        if (isOnline()) {
            showProgressDialog(true);
            SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, MConnection.API_BASE_URL + (loginType == 0 ? "signup" : "socialSignup"),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                if (response != null) {
                                    Log.d("Response", response);

                                    JSONObject jResponse = new JSONObject(response);
                                    if (jResponse.has("status") && jResponse.getBoolean("status"))
                                        getResponse(response, APICode);
                                    else if (jResponse.has("error")) {
                                        if(jResponse.has("is_registered") && jResponse.getInt("is_registered") == 1){
                                            new AlertDialog.Builder(SignUpActivity.this)
                                                    .setTitle("")
                                                    .setMessage(Html.fromHtml(jResponse.getString("error")))
                                                    .setPositiveButton(mPref.getString("21", "Yes"), new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    })
                                                    .setIcon(R.mipmap.ic_launcher)
                                                    .show();
                                            showProgressDialog(false);
                                        }else
                                            getError(jResponse.getString("error"), APICode);
                                    }else
                                        getError(mPref.getString("270", "Something Wrong! Please try later."), APICode);

                                } else
                                    getError(mPref.getString("270", "Something Wrong! Please try later."), APICode);
                            } catch (Exception e) {
                                getError(mPref.getString("270", "Something Wrong! Please try later."), APICode);
                                e.printStackTrace();
                            }
                            showProgressDialog(false);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null && error.getMessage() != null)
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), mPref.getString("270", "Something Wrong! Please try later."), Toast.LENGTH_LONG).show();
                    showProgressDialog(false);
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Accept", "application/json");
                    return headers;
                }
            });

            // TODO - commented headers in new lib
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("Content-Type", "multipart/form-data;boundary=" + boundary);
//        params.put("X-API-KEY", MConnection.API_KEY);
//        smr.setHeaders(params);

            smr.addStringParam("X-API-KEY", MConnection.API_KEY);
            smr.addStringParam("lang", mPref.getString("UserSavedLangCode", "en")); // Language
            smr.addStringParam("email_id", email);
            smr.addStringParam("password", utility.md5(password));
            smr.addStringParam("first_name", name);
            smr.addStringParam("user_type", userType + "");
            smr.addStringParam("birthday", dob.replace("-", "/"));
            smr.addStringParam("gender", sexType + "");
            smr.addStringParam("phone", phone); // TODO phone
            smr.addStringParam("terms", "1");
            smr.addStringParam("android_token", token == null ? "" : token);
            smr.addStringParam("device_id", deviceId == null ? "" : deviceId);
            if (locationItem != null) {
                smr.addStringParam("latitude", locationItem.getLatitude() + "");
                smr.addStringParam("longitude", locationItem.getLongitude() + "");
                smr.addStringParam("city", locationItem.getCity() == null ? "" : locationItem.getCity());
                smr.addStringParam("country", locationItem.getCountryId() + "");
                smr.addStringParam("country_name", locationItem.getCountryName());
            } else {
                smr.addStringParam("latitude", "");
                smr.addStringParam("longitude", "");
                smr.addStringParam("city", "");
                smr.addStringParam("country", "");
                smr.addStringParam("country_name", locationItem.getCountryName());
            }

            smr.addStringParam("login_type", userType + "");
            if (userType == 2)
                smr.addStringParam("company_name", companyName);

            if (loginType != 0) {
                smr.addStringParam("social_id", sId);
                smr.addStringParam("social_type", loginType + "");
                if (imageUrl != null && imageUrl.length() > 4)
                    smr.addStringParam("img_url", imageUrl);
                else if (imgPath != null)
                    smr.addFile("userimg", imgPath);
            } else {
                smr.addFile("userimg", imgPath);
            }

            Volley.newRequestQueue(this).add(smr);
        } else {
            showProgressDialog(false);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    @Override
    public void getError(String error, int APICode) {
        if (error != null)
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), error);
        else
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));

        showProgressDialog(false);
    }

    @Override
    public void getResponse(String response, int APICode) {
        try {
            if (response != null) {
                if (APICode == 101) {
                    JSONObject jo = new JSONObject(response);
                    if (jo.has("success")) {
                        new AlertDialog.Builder(SignUpActivity.this)
                                .setTitle("")
                                .setMessage(Html.fromHtml(jo.getString("success")))
                                .setPositiveButton(mPref.getString("21", "Yes"), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setIcon(R.mipmap.ic_launcher)
                                .show();
                        showProgressDialog(false);
                    } else if (jo.has("error")) {
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), jo.getString("error"));
                        showProgressDialog(false);
                    } else {
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));
                        showProgressDialog(false);
                    }
                } else if (APICode == 104) {
                    checkSocialLogin(sId);
                    /*JSONObject jo = new JSONObject(response);
                    if (jo.has("data")) {
                        String data = jo.getString("data");
                        Object rData = new JSONTokener(data).nextValue();

                        JSONObject uData = null;
                        if (rData instanceof JSONArray) {
                            JSONArray mData = new JSONArray(data);
                            uData = new JSONObject(mData.getString(0));
                        } else if (rData instanceof JSONObject)
                            uData = new JSONObject(data);

                        if (uData != null && uData.has("user_id")) {
                            if (APICode == 104) // 104 - socialSignup
                                checkSocialLogin(uData.getString("user_id"));
                            else
                                loginProcess();
                        } else {
                            showMessageWithTitle(this, "Alert", Constants.GENERAL_FAIL_MSG);
                            showProgressDialog(false);
                        }
                    } else if (jo.has("error")) {
                        showMessageWithTitle(this, "Alert", jo.getString("error"));
                        showProgressDialog(false);
                    } else {
                        showMessageWithTitle(this, "Alert", Constants.GENERAL_FAIL_MSG);
                        showProgressDialog(false);
                    }*/
                } else if (APICode == 102 || APICode == 103) { // 102-login, 103- socialLogin
                    JSONObject jo = new JSONObject(response);
                    if (jo.has("data")) {
                        String data = jo.getString("data");
                        Object rData = new JSONTokener(data).nextValue();

                        JSONObject uData = null;
                        if (rData instanceof JSONArray) {
                            JSONArray mData = new JSONArray(data);
                            uData = new JSONObject(mData.getString(0));
                        } else if (rData instanceof JSONObject)
                            uData = new JSONObject(data);

                        saveUserData(uData);
                    } else {
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));
                        showProgressDialog(false);
                    }
                }
            } else {
                showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));
                showProgressDialog(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void networkError(String error, int APICode) {
        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        showProgressDialog(false);
    }

    // Login Success
    private void saveUserData(JSONObject uData) {
        try {
            if (uData != null) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putString("UserId", uData.getString("id"));
                editor.putString("Name", uData.getString("first_name"));
                editor.putInt("UserType", uData.getInt("user_type"));
                if (!uData.getString("social_type").equalsIgnoreCase("null"))
                    editor.putInt("LoginType", uData.getInt("social_type"));
                editor.putString("LoginKey", uData.getString("login_key"));
                editor.putString("LoginToken", uData.getString("login_token"));
                editor.putString("DeviceId", Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID));
                editor.putBoolean("isLogin", true);
                editor.putBoolean("GuestLogin", false);

                if (uData.has("email_id"))
                    editor.putString("Email", uData.getString("email_id"));

                if (uData.has("latitude"))
                    editor.putFloat("latitude", (float) uData.getDouble("latitude"));

                if (uData.has("longitude"))
                    editor.putFloat("longitude", (float) uData.getDouble("longitude"));

                if (loginType == 0)
                    editor.putString("Password", password);
                else
                    editor.putString("SocialId", sId);

                if (uData.has("is_paid"))
                    editor.putInt("MemberShip", uData.getInt("is_paid")); // 1- active, 0 - no
                if (uData.has("active_period"))
                    editor.putString("ActivePeriod", uData.getString("active_period"));

                if (loginType != 0 && uData.has("user_pic"))
                    editor.putString("ProfilePic", uData.getString("user_pic"));
                editor.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (loginType == 0 && imgPath != null)
                            new UploadImage().execute(imgPath);
                        else if (loginType != 0 && imageUrl == null && imgPath != null)
                            new UploadImage().execute(imgPath);
                    }
                });

                showProgressDialog(false);
                hideKeyboard(SignUpActivity.this);
                startMainScreen();
            } else {
                showProgressDialog(false);
                showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));
            }
        } catch (Exception e) {
            startMainScreen();
            e.printStackTrace();
        }
    }

    private void startMainScreen() {
        Intent i = new Intent(this, KenbieActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
//        finish();
    }


    // Login process after signup
    private void checkSocialLogin(String sId) {
        if (isOnline()) {
//            showProgressDialog(true);
            Map<String, String> params = new HashMap<String, String>();
            params.put("social_id", sId);
            params.put("social_type", loginType + "");
            params.put("android_token", token == null ? "" : token);
//            params.put("device_id", android_id == null ? "" : android_id);
            mConnection.postRequestWithHttpHeaders(SignUpActivity.this, "socialLogin", this, params, 103);
        } else {
            showProgressDialog(false);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    private void loginProcess() {
        if (isOnline()) {
//            showProgressDialog(true);
            Map<String, String> params = new HashMap<String, String>();
            params.put("email", email);
            params.put("password", utility.md5(password));
            params.put("android_token", token == null ? "" : token);
//            params.put("device_id", android_id == null ? "" : android_id);
            mConnection.postRequestWithHttpHeaders(SignUpActivity.this, "login", this, params, 102);
        } else {
            showProgressDialog(false);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    private class UploadImage extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            imageUpload(params[0]);
            return null;
        }
    }

    // Image upload
    private void imageUpload(String imagePath) {
//        showProgressDialog(true);
        try {
            FileUploader multipart = new FileUploader(MConnection.API_BASE_URL + "uploadPic", "UTF-8");
            multipart.addHeaderField("Content-Type", "application/x-www-form-urlencoded");
            multipart.addHeaderField("X-API-KEY", MConnection.API_KEY);
            multipart.addFormField("user_id", mPref.getString("UserId", ""));
            multipart.addFormField("login_key", mPref.getString("LoginKey", ""));
            multipart.addFormField("login_token", mPref.getString("LoginToken", ""));
            multipart.addFormField("device_id", mPref.getString("DeviceId", ""));
            multipart.addFormField("lang", mPref.getString("UserSavedLangCode", "en")); // Language

            File sourceFile = new File(imagePath);
            if (!sourceFile.isFile()) {
                Log.e("Upload file to server", "Source File Does not exist");
            }

            multipart.addFilePart("file", sourceFile);

            List<String> response = multipart.finish();

            System.out.println("SERVER REPLIED:");

            for (String line : response) {
                try {
                    JSONObject jObj = new JSONObject(line);
                    SharedPreferences.Editor editor = mPref.edit();
                    if (jObj.has("status") && jObj.getBoolean("status")) {
                        JSONObject jImg = new JSONObject(jObj.getString("data"));
                        if (jImg.has("user_pic"))
                            editor.putString("ProfilePic", jImg.getString("user_pic"));
                        editor.apply();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                showProgressDialog(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



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
}
