package com.kenbie.connection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kenbie.LoginOptionsActivity;
import com.kenbie.R;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.util.Constants;
import com.kenbie.util.Utility;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rajaw on 5/24/2017.
 */

public class MConnection {
    public static String API_BASE_URL = "https://kenbie.com/api/";
    public static String API_KEY = "Kenbie@2017";

    // GET connection
    public void getRequestWithHttpHeaders(final Context context, final String method, final APIResponseHandler mListeners, final int APICode) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.GET, API_BASE_URL + method,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            if (response != null) {
                                JSONObject jResponse = new JSONObject(response);
                                if (jResponse.has("status") && jResponse.getBoolean("status"))
                                    mListeners.getResponse(response, APICode);
                                else if (jResponse.has("logout") && jResponse.getBoolean("logout"))
                                    startLogoutProcess(context, jResponse.getString("error"));
                                else if (jResponse.has("error"))
                                    mListeners.getError(jResponse.getString("error"), APICode);
                                else
                                    mListeners.getError(Constants.GENERAL_FAIL_MSG, APICode);

                                Log.d("Response", response);
                            } else
                                mListeners.getError(Constants.GENERAL_FAIL_MSG, APICode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListeners.getError(error.toString(), APICode);
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("X-API-KEY", API_KEY);
                return params;
            }
        };
        queue.add(postRequest);
    }

    // POST connection
    public void postRequestWithHttpHeaders(final Context context, final String method, final APIResponseHandler mListeners, final Map<String, String> params, final int APICode) {
        SharedPreferences mPref = context.getSharedPreferences("kPrefs", MODE_PRIVATE);
        params.put("device_id", Utility.getDeviceId(mPref, context)); // Phone device id
        if (!params.containsKey("lang"))
            params.put("lang", mPref.getString("UserSavedLangCode", "en")); // Language
        Log.d("Request", method + "::::::::" + params.toString());
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, API_BASE_URL + method,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            if (response != null) {
                                Log.d("Response:::::::::::" + method, response);

                                JSONObject jResponse = new JSONObject(response);
                                if (jResponse.has("status") && jResponse.getBoolean("status"))
                                    mListeners.getResponse(response, APICode);
                                else if (jResponse.has("logout") && jResponse.getBoolean("logout"))
                                    startLogoutProcess(context, jResponse.getString("error"));
                                else if (jResponse.has("error"))
                                    mListeners.getError(jResponse.getString("error"), APICode);
                                else
                                    mListeners.getError(mPref.getString("270", "Something Wrong! Please try later."), APICode);

                            } else
                                mListeners.getError(mPref.getString("270", "Something Wrong! Please try later."), APICode);
                        } catch (Exception e) {
                            mListeners.getError(mPref.getString("270", "Something Wrong! Please try later."), APICode);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListeners.getError(error.toString(), APICode);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("X-API-KEY", API_KEY);
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        queue.add(postRequest);
    }


    private void startLogoutProcess(Context context, String error) {
        SharedPreferences mPref = context.getSharedPreferences("kPrefs", MODE_PRIVATE);
        try {
            new AlertDialog.Builder(context)
                    .setTitle("")
                    .setMessage(Html.fromHtml(error))
                    .setPositiveButton(mPref.getString("21", "Yes"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            SharedPreferences.Editor editor = mPref.edit();
                            editor.putString("UserId", "0");
                            editor.putString("LoginKey", "");
                            editor.putString("LoginToken", "");
                            editor.putString("ProfilePic", "");
                            editor.putString("DeviceId", "");
                            editor.putBoolean("isLogin", false);
                            editor.putBoolean("GuestLogin", false);
                            editor.apply();
                            logoutProcess(context);
                        }
                    })
                    .setIcon(R.mipmap.ic_stat_notification)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logoutProcess(Context context) {
        try {
            Intent i = new Intent(context, LoginOptionsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
