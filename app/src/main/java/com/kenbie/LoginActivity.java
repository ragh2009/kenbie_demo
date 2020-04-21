package com.kenbie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kenbie.data.LanguageParser;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.util.Constants;
import com.kenbie.util.Utility;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends KenbieBaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, APIResponseHandler {
    private static final int RC_SIGN_IN = 9001;
    private EditText emailEt, passwordEt;
    private GoogleApiClient mGoogleApiClient;
    //    private FirebaseAuth mAuth;
    private String sId = null, email = null, token = null, name, dob, imageUrl, gender, androidToken;
    private int loginType, type;
    private TwitterAuthClient authClient = null;
    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private LinearLayout loginLayout, socialLayout;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        type = getIntent().getIntExtra("Type", 1);

        ImageView backBtn = (ImageView) findViewById(R.id.back_button);
        if (type == 2)
            backBtn.setBackgroundResource(R.drawable.ic_v_close);
        else
            backBtn.setBackgroundResource(R.drawable.ic_v_gray_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type != 2 || getIntent().getBooleanExtra("IsBack", false)) {
                    Intent intent = new Intent(LoginActivity.this, LoginOptionsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else
                    onBackPressed();
            }
        });

        ((ImageView) findViewById(R.id.app_logo)).setVisibility(View.GONE);
        TextView forgetBtn = (TextView) findViewById(R.id.m_title2);
        forgetBtn.setText(mPref.getString("11", "Forgot Password?"));
        if (type == 2)
            forgetBtn.setVisibility(View.INVISIBLE);
        else
            forgetBtn.setVisibility(View.VISIBLE);

        forgetBtn.setTypeface(KenbieApplication.S_NORMAL);
        forgetBtn.setOnClickListener(this);
   /* old
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
                .requestIdToken("510221218616-09hkcdp9uqma3b5t1urrha5nsea3t0nt.apps.googleusercontent.com")
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this *//* OnConnectionFailedListener *//*)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        mAuth = FirebaseAuth.getInstance();
        token = FirebaseInstanceId.getInstance().getToken();

        initUiSetup();

        initTwitter();
        initFB();
    }


    private void initUiSetup() {
        try {
            mProgress = new ProgressDialog(this, R.style.MyAlertDialogStyle);
//            mProgress.setMessage("Please wait...");
            mProgress.setIndeterminate(false);
            mProgress.setCancelable(true);
            mProgress.setCanceledOnTouchOutside(false);

            loginLayout = findViewById(R.id.login_layout);
            socialLayout = findViewById(R.id.social_layout);

            if (type == 1) { // Show View Login
                loginLayout.setVisibility(View.VISIBLE);
                socialLayout.setVisibility(View.GONE);
            } else { // Type - 2 show social options
                loginLayout.setVisibility(View.GONE);
                socialLayout.setVisibility(View.VISIBLE);
            }

            TextView screenTitle = (TextView) findViewById(R.id.screen_title);
            screenTitle.setText(mPref.getString("2", "Log In"));
            screenTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);

            TextView emailTitle = (TextView) findViewById(R.id.email_title);
            emailTitle.setText(mPref.getString("12", "EMAIL ADDRESS"));
            emailTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);

            emailEt = (EditText) findViewById(R.id.et_email);
            emailEt.setHint(mPref.getString("16", "Email Address"));
            emailEt.setTypeface(KenbieApplication.S_NORMAL);
            emailEt.setFocusableInTouchMode(true);
            emailEt.clearFocus();
            emailEt.setCursorVisible(false);
            emailEt.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    emailEt.setCursorVisible(true);
                    return false;
                }
            });

            TextView passwordTitle = (TextView) findViewById(R.id.password_title);
            passwordTitle.setText(mPref.getString("13", "PASSWORD"));
            passwordTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);

            passwordEt = (EditText) findViewById(R.id.et_password);
            passwordEt.setHint(mPref.getString("17", "Password"));
            passwordEt.setTypeface(KenbieApplication.S_NORMAL);
            passwordEt.setFocusableInTouchMode(true);
            passwordEt.clearFocus();

            TextView signInBtn = (TextView) findViewById(R.id.sign_in_btn);
            signInBtn.setText(mPref.getString("2", "Log In"));
            signInBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
            signInBtn.setOnClickListener(this);
            signInBtn.setFocusable(true);

//            remember = (CheckBox) findViewById(R.id.remember);
//            remember.setText(titleArray.get(4));
//            remember.setTypeface(KenbieApplication.S_NORMAL);

//            TextView forgotTxt = (TextView) findViewById(R.id.forgot_txt);
//            forgotTxt.setTypeface(KenbieApplication.S_NORMAL);
//            forgotTxt.setText(titleArray.get(5));
//            forgotTxt.setOnClickListener(this);


/*            TextView notMemberTxt = (TextView) findViewById(R.id.not_member_txt);
            notMemberTxt.setTypeface(KenbieApplication.S_NORMAL);
            notMemberTxt.setText(titleArray.get(7));

            TextView guestBtn = (TextView) findViewById(R.id.guest_btn);
            guestBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
            guestBtn.setText(titleArray.get(19));
            guestBtn.setOnClickListener(this);

            TextView orSmallText = (TextView) findViewById(R.id.or_small_text);
            orSmallText.setTypeface(KenbieApplication.S_NORMAL);
            orSmallText.setText(titleArray.get(9));

            TextView orText = (TextView) findViewById(R.id.or_text);
            orText.setTypeface(KenbieApplication.S_BOLD);
            orText.setText(titleArray.get(9));*/

            TextView fbBtn = (TextView) findViewById(R.id.fb_btn);
            fbBtn.setText(mPref.getString("23", "Log in with Facebook"));
            fbBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
            fbBtn.setOnClickListener(this);

            TextView gBtn = (TextView) findViewById(R.id.g_plus_btn);
            gBtn.setText(mPref.getString("24", "Log in with Google"));
            gBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
            gBtn.setOnClickListener(this);

            TextView twitterBtn = (TextView) findViewById(R.id.twitter_btn);
            twitterBtn.setText(mPref.getString("25", "Log in with Twitter"));
            twitterBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
            twitterBtn.setOnClickListener(this);

            TextView loginInEmailBtn = (TextView) findViewById(R.id.sign_in_email_btn);
            loginInEmailBtn.setText(mPref.getString("25", "Log in with Email"));
            loginInEmailBtn.setTypeface(KenbieApplication.S_BOLD);
            loginInEmailBtn.setOnClickListener(this);

            TextView signUpBtn = (TextView) findViewById(R.id.sign_up_btn);
            signUpBtn.setText(mPref.getString("5", "Create Account"));
            signUpBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
            signUpBtn.setOnClickListener(this);

//            if (mPref.getBoolean("Remember", false)) {
//                remember.setChecked(true);
//                emailEt.setText(mPref.getString("Email", ""));
//                passwordEt.setText(mPref.getString("Password", ""));
//            }

            androidToken = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        emailEt.setFocusableInTouchMode(true);
        emailEt.clearFocus();
        passwordEt.clearFocus();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.m_title2:
                if (emailEt.getText().toString().equalsIgnoreCase(""))
                    showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("42", "The Email Address field is required."));
                else if (!isValidEmail(emailEt.getText().toString(), this))
                    showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("264", "Please enter valid email id"));
                else {
                    forgotProgress(emailEt.getText().toString());
                }
                break;
            case R.id.sign_in_btn:
                loginType = 0;
                if (isInfoValid()) {
                    loginProcess();
//                    SharedPreferences.Editor editor = mPref.edit();
//                    editor.putBoolean("Remember", remember.isChecked());
//                    editor.apply();
                }
                break;
            case R.id.sign_in_email_btn:
                Intent i = new Intent(LoginActivity.this, LoginActivity.class);
                i.putExtra("Type", 1);
                i.putExtra("IsBack", true);
                startActivity(i);
                finish();
                break;
            case R.id.sign_up_btn:
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                intent.putExtra("IsBack", true);
                intent.putExtra("social_type", 0);
                intent.putExtra("android_token", token == null ? "" : token);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.fb_btn:
                loginType = 1;
                fbLoginProcess();
                break;
            case R.id.g_plus_btn:
                loginType = 2;
                gPlusProcess();
                break;
            case R.id.twitter_btn:
                loginType = 3;
                twitterLoginProcess();
                break;
            default:
                break;
        }
    }

    /*  Facebook Login Process */
    private void fbLoginProcess() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void initFB() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        showProgressDialog(true, "Please wait...");
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        // Application code
                                        try {
                                            sId = object.getString("id");
                                            name = object.getString("name");
                                            if (object.has("email"))
                                                email = object.getString("email");
                                            if (object.has("birthday")) {
                                                dob = object.getString("birthday");
                                                if (dob != null)
                                                    dob = Utility.getDayMonthDateFormat(object.getString("birthday"));
                                            }
                                            if (object.has("gender"))
                                                gender = object.getString("gender");
                                            checkSocialLogin(sId);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    /* Twitter login */
    private void initTwitter() {
//        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
//        TwitterAuthToken authToken = session.getAuthToken();
//        String token = authToken.token;
//        String secret = authToken.secret;

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Constants.API_KEY, Constants.API_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }


    private void twitterLoginProcess() {
        showProgressDialog(true, "Please wait...");
        authClient = new TwitterAuthClient();
        authClient.authorize(this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d("", "onConnectionFailed:" + result.toString());
                TwitterSession session = result.data;
                name = session.getUserName();
                sId = session.getUserId() + "";
                checkSocialLogin(sId);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                showProgressDialog(false, null);
//                Log.d("", "onConnectionFailed:" + exception.toString());
            }
        });
    }

    /* Google Plus Login Process */
    private void gPlusProcess() {
        showProgressDialog(true, "Please wait...");
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void loginProcess() {
        if (isOnline()) {
            showProgressDialog(true, "Please wait...");
            Map<String, String> params = new HashMap<String, String>();
            params.put("email", emailEt.getText().toString());
            params.put("password", utility.md5(passwordEt.getText().toString()));
            params.put("android_token", token == null ? "" : token);
            params.put("device_id", androidToken == null ? "" : androidToken);
            Log.v("Login Request", params.toString());
            mConnection.postRequestWithHttpHeaders(LoginActivity.this, "login", this, params, 101);
        } else {
            showProgressDialog(false, null);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    private boolean isInfoValid() {
        if (emailEt.getText().toString().equalsIgnoreCase("")) {
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("42", "The Email Address field is required."));
            return false;
        } else if (!isValidEmail(emailEt.getText().toString(), this)) {
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("264", "Please enter valid email id"));
            return false;
        } else if (passwordEt.getText().toString().equalsIgnoreCase("")) {
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("44", "The Password field is required."));
            return false;
        } else if (passwordEt.getText().toString().length() < 6) {
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("19", "Password should be minimum 6 digits"));
            return false;
        } else if (!isOnline()) {
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
            return false;
        }

        return true;
    }


    private void forgotProgress(String email) {
        if (isOnline()) {
            showProgressDialog(true, "Please wait...");
            Map<String, String> params = new HashMap<String, String>();
            params.put("email_id", email);
            mConnection.postRequestWithHttpHeaders(LoginActivity.this, "forgetPassword", this, params, 102);
        } else {
            showProgressDialog(false, null);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

   /*   Old code
     GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign-in error. Please try again.", Toast.LENGTH_SHORT).show();
                showProgressDialog(false);
            }*/
        } else if (loginType == 1)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        else {
            authClient.onActivityResult(requestCode, resultCode, data);
        }
    }
    // [END onactivityresult]

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            sId = acct.getId();
            name = acct.getDisplayName();
            email = acct.getEmail();
            if (acct.getPhotoUrl() != null)
                imageUrl = acct.getPhotoUrl().toString();

            checkSocialLogin(sId);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google sign-in error. Please try again.", Toast.LENGTH_SHORT).show();
            showProgressDialog(false, null);
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("", "firebaseAuthWithGoogle:" + acct.getId());
        sId = acct.getId();
        name = acct.getDisplayName();
        email = acct.getEmail();
        if (acct.getPhotoUrl() != null)
            imageUrl = acct.getPhotoUrl().toString();

        checkSocialLogin(sId);
    }

    // Social Login
    private void checkSocialLogin(String sId) {
        if (isOnline()) {
            showProgressDialog(true, "Please wait...");
            Map<String, String> params = new HashMap<String, String>();
            params.put("social_id", sId);
            params.put("social_type", loginType + "");
            params.put("android_token", token == null ? "" : token);
            params.put("device_id", androidToken == null ? "" : androidToken);
            mConnection.postRequestWithHttpHeaders(LoginActivity.this, "socialLogin", this, params, 103);
        } else {
            showProgressDialog(false, null);
            showMessageWithTitle(this, "Alert", Constants.NETWORK_FAIL_MSG);
        }
    }

    @Override
    public void getError(String error, int APICode) {
        if (APICode == 103) // 103 - socialLogin
            getSignUpProcess();
        else if (error != null)
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), error);
        else
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));


        showProgressDialog(false, null);
    }

    @Override
    public void getResponse(String response, int APICode) {
        try {
            if (response != null) {
                JSONObject jo = new JSONObject(response);
                if (APICode == 102) { // 102 - forgetPassword
                    if (jo.has("success"))
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), jo.getString("success"));
                } else if (APICode == 104) {
                    new LanguageParser().saveLanguageData(mPref, response);
                    hideKeyboard(LoginActivity.this);
                    Intent i = new Intent(this, KenbieActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else if (jo.has("data")) {
                    String data = jo.getString("data");
                    Object rData = new JSONTokener(data).nextValue();

                    JSONObject uData = null;
                    if (rData instanceof JSONArray) {
                        JSONArray mData = new JSONArray(data);
                        uData = new JSONObject(mData.getString(0));
                    } else if (rData instanceof JSONObject)
                        uData = new JSONObject(data);

                    saveUserData(uData);
                    showProgressDialog(false, null);
                } else
                    showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));
            } else
                showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));

            showProgressDialog(false, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    editor.putString("Password", passwordEt.getText().toString());
                else
                    editor.putString("SocialId", sId);

                if (uData.has("is_paid"))
                    editor.putInt("MemberShip", uData.getInt("is_paid")); // 1- active, 0 - no
                if (uData.has("active_period"))
                    editor.putString("ActivePeriod", uData.getString("active_period"));

                if (uData.has("user_pic"))
                    editor.putString("ProfilePic", uData.getString("user_pic"));
                editor.apply();

             /*   String userLang = mPref.getString("UserSavedLangCode", "");

                if (uData.has("selected_lang") && userLang.length() > 0 && uData.getString("selected_lang").equalsIgnoreCase(userLang)) {
                    hideKeyboard(LoginActivity.this);
                    Intent i = new Intent(this, KenbieActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else*/

                saveLanguage(uData.getString("selected_lang"));
            } else {
                showMessageWithTitle(this, "Alert", Constants.GENERAL_FAIL_MSG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLanguage(String value) {
        if (isOnline()) {
            showProgressDialog(true, null);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", mPref.getString("UserId", ""));
            params.put("login_key", mPref.getString("LoginKey", ""));
            params.put("login_token", mPref.getString("LoginToken", ""));
            params.put("lang", value);
            mConnection.postRequestWithHttpHeaders(this, "lang", this, params, 104);
        } else {
            hideKeyboard(LoginActivity.this);
            Intent i = new Intent(this, KenbieActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    private void getSignUpProcess() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        intent.putExtra("social_id", sId);
        intent.putExtra("social_type", loginType);
        intent.putExtra("first_name", name);
//        intent.putExtra("android_token", token == null ? "" : token);
        intent.putExtra("Email", email);
        intent.putExtra("ImageUrl", imageUrl);
        intent.putExtra("DOB", dob);
        if (gender != null)
            intent.putExtra("Gender", gender.equalsIgnoreCase("male") ? 1 : 2);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void networkError(String error, int APICode) {
        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        showProgressDialog(false, null);
    }


    public void showProgressDialog(boolean isShow, String msg) {
        try {
            if (isShow) {
//                if (msg != null)
//                    mProgress.setMessage(msg);
//                else
//                    mProgress.setMessage("Please wait...");
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

