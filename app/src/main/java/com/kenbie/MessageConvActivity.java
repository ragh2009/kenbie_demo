package com.kenbie;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.kenbie.adapters.MsgConvAdapter;
import com.kenbie.connection.MConnection;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.listeners.MsgUserActionListeners;
import com.kenbie.model.Message;
import com.kenbie.model.MsgUserItem;
import com.kenbie.model.OptionsData;
import com.kenbie.model.UserItem;
import com.kenbie.util.Constants;
import com.kenbie.util.ImageResize;
import com.kenbie.util.RuntimePermissionUtils;
import com.kenbie.util.Utility;
import com.kenbie.views.StoryListView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConvActivity extends KenbieBaseActivity implements APIResponseHandler, MsgUserActionListeners, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int YOUR_DOC_RESULT_CODE = 101;
    private Uri IMAGE_CAPTURE_URI;
    private StoryListView msgListView;
    private MsgConvAdapter msgConvAdapter;
    private MsgUserItem msgUserItem;
    private Message convData;
    private ArrayList<Message> chatData;
    private AppCompatEditText etMsg;
    private String lastSyncTime = null, filePath = "", lastMsgId = "", mCurrentPhotoPath;
    private CountDownTimer countDownTimer;
    private LinearLayout attachLayout, messageOptions;
    private ProgressDialog mProgress;
    private int pageNo = 1, firstVisPos, top, newAddedBeforeFirstVisible, currentFirstVisibleItem, currentScrollState, currentVisibleItemCount, userId;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private boolean isFirstLoading = true, isPostingMsg, isLoadingMore = true, isLoading, newMessage = false;
    private String timeZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_conv);

        Calendar cal = Calendar.getInstance();
        timeZone = cal.getTimeZone().getID();
        userId = Integer.valueOf(mPref.getString("UserId", "0"));
        mProgress = new ProgressDialog(this, R.style.MyAlertDialogStyle);
//        mProgress.setMessage("Please wait...");
        mProgress.setIndeterminate(false);
        mProgress.setCancelable(true);
        mProgress.setCanceledOnTouchOutside(false);

        final View activityRootView = findViewById(R.id.message_layout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > Utility.dpToPx(MessageConvActivity.this, 180)) { // if more than 200 dp, it's probably a keyboard...
                    // ... do something here
                    if (chatData != null)
                        msgListView.setSelection(chatData.size());
                }
            }
        });

        mySwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mySwipeRefreshLayout.setColorSchemeResources(
                R.color.red_g_color);
        mySwipeRefreshLayout.setOnRefreshListener(this);

        msgUserItem = (MsgUserItem) getIntent().getSerializableExtra("MsgItem");

        ImageView menuBtn = (ImageView) findViewById(R.id.back_button);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                onBackPressed();
//                finish();
            }
        });

        TextView mTitle = (TextView) findViewById(R.id.m_title);
        mTitle.setTypeface(KenbieApplication.S_BOLD);
        mTitle.setText(msgUserItem.getUser_name().toUpperCase());
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgUserItem.getUid() != 1) {
                    UserItem userItem = new UserItem();
                    userItem.setId(msgUserItem.getUid());
                    userItem.setFirstName(msgUserItem.getUser_name());
                    Intent intent = new Intent(MessageConvActivity.this, KenbieActivity.class);
                    intent.putExtra("NavType", 1);
                    intent.putExtra("UserItem", userItem);
                    startActivity(intent);
                }
            }
        });

        ImageView userImg = (ImageView) findViewById(R.id.user_img);
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (msgUserItem.getUid() != 1) {
                    UserItem userItem = new UserItem();
                    userItem.setId(msgUserItem.getUid());
                    userItem.setFirstName(msgUserItem.getUser_name());
                    Intent intent = new Intent(MessageConvActivity.this, KenbieActivity.class);
                    intent.putExtra("NavType", 1);
                    intent.putExtra("UserItem", userItem);
                    startActivity(intent);
                }
            }
        });

        userImg.setVisibility(View.VISIBLE);
        RequestOptions options = new RequestOptions()
                .optionalCircleCrop()
                .placeholder(getResources().getDrawable(R.drawable.img_c_user_dummy))
                .priority(Priority.HIGH);
        Glide.with(this).load(Constants.BASE_IMAGE_URL + msgUserItem.getUser_img()).apply(options).into(userImg);


//        if (msgUserItem.getUser_img() != null && msgUserItem.getUser_img().length() < 10)
//        else
//            Glide.with(this).load(Constants.BASE_IMAGE_URL + msgUserItem.getUser_img()).apply(RequestOptions.circleCropTransform()).into(userImg);

//        Glide.with(this).load(Constants.BASE_IMAGE_URL + msgUserItem.getUser_img()).apply(RequestOptions.circleCropTransform()).into(userImg);

        msgListView = (StoryListView) findViewById(R.id.msg_list);
        msgListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                currentScrollState = scrollState;
                if (currentVisibleItemCount > 0 && currentScrollState == SCROLL_STATE_IDLE) {
                    if (currentFirstVisibleItem == 0) {
                        if (!isFirstLoading && !isLoading) {
                            if (isLoadingMore) {
//                        lastVisiblePosition = msgListView.getFirstVisiblePosition();
                                firstVisPos = msgListView.getFirstVisiblePosition();
                                View firstVisView = msgListView.getChildAt(0);
                                if (firstVisView != null) {
//                                    top = firstVisView != null ? firstVisView.getTop() : 0;
                                    top = firstVisView.getTop();
//                                    msgListView.setBlockLayoutChildren(true);
                                    pageNo++;
                                    stopCountDown();
                                    gettingUserMessageList(103);
                                }
                            } else {
                                mySwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }
                }
//                Log.d("scrollState ::::::::", scrollState + "");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                Log.d("Scroll ::::::::", firstVisibleItem + ":" + visibleItemCount + ":" + totalItemCount);
                currentFirstVisibleItem = firstVisibleItem;
                currentVisibleItemCount = totalItemCount;

//                if (!isFirstLoading && chatData.get(msgListView.getFirstVisiblePosition()).getMsgId() == chatData.get(0).getMsgId() && !isLoading) {
          /*      if (!isFirstLoading && msgListView.getFirstVisiblePosition() == 0 && !isLoading) {
                    if (isLoadingMore) {
//                        lastVisiblePosition = msgListView.getFirstVisiblePosition();
                        firstVisPos = msgListView.getFirstVisiblePosition();
                        View firstVisView = msgListView.getChildAt(0);
                        top = firstVisView != null ? firstVisView.getTop() : 0;
                        msgListView.setBlockLayoutChildren(true);
                        pageNo++;
                        stopCountDown();
                        gettingUserMessageList(103);
                    } else
                        mySwipeRefreshLayout.setRefreshing(false);
                }*/

//                currentFirstVisibleItem = firstVisibleItem;
//                currentVisibleItemCount = visibleItemCount;
//                currentTotalItemCount = totalItemCount;

//                if (currentFirstVisibleItem == 0 && currentVisibleItemCount == 0)
//                    Log.d("Scroll ::::::::", firstVisibleItem + ":" + visibleItemCount + ":" + totalItemCount);
//                else
//                    Log.d("Scroll ::::::::", firstVisibleItem + ":" + visibleItemCount + ":" + totalItemCount);
            }
        });

        etMsg = (AppCompatEditText) findViewById(R.id.et_msg);
        etMsg.setHint(mPref.getString("96", "Write a message"));
        etMsg.setTypeface(KenbieApplication.S_NORMAL);
        etMsg.setCursorVisible(false);
        etMsg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etMsg.setCursorVisible(true);
                return false;
            }
        });
/*        etMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus && chatData != null)
//                    msgListView.setSelection(chatData.size());
            }
        });*/

        ((ImageView) findViewById(R.id.attach_btn)).setOnClickListener(this);
        attachLayout = (LinearLayout) findViewById(R.id.attach_layout);
        messageOptions = (LinearLayout) findViewById(R.id.message_options);
        if (msgUserItem.getUid() == 1) { //                android:stackFromBottom="true"
            msgListView.setStackFromBottom(false);
            messageOptions.setVisibility(View.GONE);
        } else {
            msgListView.setStackFromBottom(true);
            messageOptions.setVisibility(View.VISIBLE);
        }

        ((LinearLayout) findViewById(R.id.doc_btn)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.media_btn)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.camera_btn)).setOnClickListener(this);

        ImageView sendBtn = (ImageView) findViewById(R.id.send_btn);
//        sendBtn.setText(mPref.getString("97", "Send"));
//        sendBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
        sendBtn.setOnClickListener(this);

        gettingUserMessageList(101);
        hideSoftKeyboard();
    }

    // Post message to user
    private void postMessageToUser(String msg) {
        if (isOnline()) {
            Message manualAddedMsg = new Message();
            manualAddedMsg.setMsgId(-1);
            manualAddedMsg.setMsg(msg.replace("\n", "<br/>"));
            manualAddedMsg.setMsg_status(0);
            manualAddedMsg.setMsg_user_id(userId);
            manualAddedMsg.setMsg_time(Utility.getMessageTime());
            if (chatData == null)
                chatData = new ArrayList<>();
            chatData.add(manualAddedMsg);
            refreshChatList(102);
            isPostingMsg = false;
            etMsg.setText("");
//            showProgressDialog(true);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", userId + "");
            params.put("login_key", mPref.getString("LoginKey", ""));
            params.put("login_token", mPref.getString("LoginToken", ""));
            params.put("chat_user_id", msgUserItem.getUid() + "");
            params.put("chat_index", (chatData.size() - 1) + "");
            params.put("tz", timeZone);
            params.put("msg", msg);
            new MConnection().postRequestWithHttpHeaders(this, "sendMsg", this, params, 102);
        } else {
            showProgressDialog(false);
            showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        }
    }

    //Fetching message user list
    private void gettingUserMessageList(int APIcode) {
        if (isOnline()) {
            if (APIcode == 101 || APIcode == 103)
                mySwipeRefreshLayout.setRefreshing(true);
            isLoading = true;
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", userId + "");
            params.put("login_key", mPref.getString("LoginKey", ""));
            params.put("login_token", mPref.getString("LoginToken", ""));
            params.put("chat_user_id", msgUserItem.getUid() + "");
            params.put("tz", timeZone);

            if (APIcode == 101 || APIcode == 103)
                new MConnection().postRequestWithHttpHeaders(this, "getUserNewMsg/page/" + (pageNo), this, params, APIcode);
            else {
                if (lastSyncTime != null && !lastSyncTime.equalsIgnoreCase("null"))
                    params.put("last_req_time", lastSyncTime);
                else
                    params.put("last_req_time", "");

//                params.put("lci", lastMsgId);
                new MConnection().postRequestWithHttpHeaders(this, "getUserNewMsg", this, params, APIcode);
            }
        } else {
            mySwipeRefreshLayout.setRefreshing(false);
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
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void getResponse(String response, int APICode) {
        try {
            if (response != null) {
                JSONObject jo = new JSONObject(response);

                //101 - first time, 102 - post, 103 - refresh, 104 - auto
                if (APICode == 101 || APICode == 103 || APICode == 104) { // Message List
                    if (jo.has("status") && jo.getBoolean("status")) {
                        if (jo.has("lci"))
                            lastMsgId = jo.getString("lci");
                        if (jo.has("data"))
                            parseUserMsgData(jo.getString("data"), APICode);
                        refreshChatList(APICode);
                    }

                    mySwipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
                    /* else if (jo.has("error"))
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), jo.getString("error"));
                    else
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));*/
//                    hideSoftKeyboard();
                } else if (APICode == 102) { // Post Message
                    if (jo.has("status") && jo.getBoolean("status")) {
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putBoolean("MsgPosted", true);
                        editor.apply();
                        // Removed after posting
                        if (jo.has("data")) {
                            parseUserMsgData(jo.getString("data"), APICode);
                            refreshChatList(APICode);
                        }

//                        startCountDown();

                        if (jo.has("lci"))
                            lastMsgId = jo.getString("lci");
//                        hideSoftKeyboard();
                    } else if (jo.has("error"))
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), jo.getString("error"));
                    else
                        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("270", "Something Wrong! Please try later."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mySwipeRefreshLayout.setRefreshing(false);
        showProgressDialog(false);
    }

    private void parseUserMsgData(String data, int APICode) {
        if (APICode == 101)
            chatData = new ArrayList<>();

        if (chatData == null)
            chatData = new ArrayList<>();
        try {
            ArrayList<Message> msgList = new ArrayList<>();

            JSONObject jChat = new JSONObject(data);
            if (jChat.has("msg_list")) {
                JSONObject jo = null;
                JSONArray jsonArray = null;
                Object json = new JSONTokener(jChat.getString("msg_list")).nextValue();
                if (json instanceof JSONObject) {
                    jo = new JSONObject(jChat.getString("msg_list"));
                    if (jo.has("msg_list"))
                        jsonArray = new JSONArray(jo.getString("msg_list"));
                } else
                    jsonArray = new JSONArray(jChat.getString("msg_list"));

                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jMsg = new JSONObject(jsonArray.getString(i));
                        Message message = new Message();
                        message.setMsg(jMsg.getString("msg"));
                        message.setMsg_status(jMsg.getInt("msg_status"));
                        message.setMsg_time(jMsg.getString("msg_time"));
                        if (jMsg.has("msg_id"))
                            message.setMsgId(jMsg.getInt("msg_id"));
                        message.setMsg_user_id(jMsg.getInt("msg_user_id"));
                        if (jMsg.has("attachment"))
                            message.setAttachment(jMsg.getString("attachment"));
                        if (jMsg.has("attachment_type"))
                            message.setAttachment_type(jMsg.getString("attachment_type"));
                        message.setLoaderEnable(false);
                        msgList.add(message);
                    }
                }

                if (APICode == 102 && msgList.size() > 0) {
                    if (jChat.has("chat_index")) {
                        try {
                            int chatIndex = jChat.getInt("chat_index");
                            Message msMessage = msgList.get(0);
                            if (msMessage.getMsg_user_id() == userId && msMessage.getAttachment_type() != null && msMessage.getAttachment_type().equalsIgnoreCase("img")) {
                                msMessage.setImgUri(chatData.get(chatIndex).getImgUri());
                                msMessage.setMediaType(chatData.get(chatIndex).getMediaType());
                                msMessage.setLoaderEnable(false);
                                chatData.set(chatIndex, msMessage);
                            } else {
                                msMessage.setLoaderEnable(false);
                                chatData.set(chatIndex, msMessage);
                            }
                            msgList.remove(0);
                            msgConvAdapter.refreshData(chatData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

//                if (msgList.size() > 0)
//                    removedManualAddedMessage(msgList, APICode);
            }

            if (jChat.has("old_msg_status")) {
                ArrayList<Integer> readMsgData = new ArrayList<>();
                ArrayList<Integer> readMsgStatus = new ArrayList<>();

                /*-------------- For message status change ----------------------------------*/
                JSONArray readStatusArray = jChat.getJSONArray("old_msg_status");

                for (int i = 0; i < readStatusArray.length(); i++) {
                    readMsgData.add(readStatusArray.getJSONObject(i).getInt("id"));
                    readMsgStatus.add(readStatusArray.getJSONObject(i).getInt("is_read"));
                }

                if (readMsgData.size() > 0)
                    lastMsgId = readMsgData.get(0) + "";

                if (chatData.size() > 0) {
                    for (int i = chatData.size() - 1; (i >= 0 && i < chatData.size()); i--) {
                        if (chatData.get(i).getMsg_status() == 0) {
                            int index = readMsgData.indexOf(chatData.get(i).getMsgId());
                            if (index != -1)
                                chatData.get(i).setMsg_status(readMsgStatus.get(index));
                        } else
                            break;
                    }
                }
            }

            /*-------------- End message status change ----------------------------------*/

            if (msgList != null && msgList.size() > 0) {
                if (APICode == 103) {
                    newAddedBeforeFirstVisible = msgList.size();
                    chatData.addAll(0, msgList);
                    isLoadingMore = true;
                } else {
                    if (APICode == 104)
                        newMessage = true;
                    chatData.addAll(msgList);
                }
            } else if (APICode == 103)
                isLoadingMore = false;

            if (jChat.has("last_response_time"))
                lastSyncTime = jChat.getString("last_response_time");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removedManualAddedMessage(ArrayList<Message> msgList, int APICode) {
        ArrayList<Integer> lastMsgIds = new ArrayList<>();
        try {
            if (chatData != null && chatData.size() > 0) {
                // Removed repetition data
                try {
                    if (APICode == 104) {
                        int lastCount = chatData.size();
                        if (lastCount < 5)
                            lastCount = chatData.size() - 1;
                        for (int i = chatData.size() - 1; lastMsgIds.size() < lastCount; i--) {
                            lastMsgIds.add(chatData.get(i).getMsgId());
                        }

                        ArrayList<Integer> isDeleteMsgIds = new ArrayList<>();
                        for (int j = 0; j < msgList.size(); j++) {
                            if (lastMsgIds.indexOf(msgList.get(j).getMsgId()) != -1)
                                isDeleteMsgIds.add(j);
                        }

                        for (int k = 0; k < isDeleteMsgIds.size(); k++) {
                            msgList.remove(k);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (int i = chatData.size() - 1; (i >= 0 && i < chatData.size()); i--) {
                    if (chatData.get(i).getMsgId() == -1) {
                        if (chatData.get(i).getAttachment_type() == null)
                            chatData.remove(i);
                        else if (chatData.get(i).getAttachment_type() != null && chatData.get(i).getAttachment_type().equalsIgnoreCase("img")) {
                            try {
                                if (msgList != null && msgList.size() > 0) {
                                    for (int j = 0; j < msgList.size(); j++) {
                                        if (msgList.get(j).getMsg_user_id() == userId && msgList.get(j).getAttachment_type() != null && msgList.get(j).getAttachment_type().equalsIgnoreCase("img")) {
                                            Message value = msgList.get(j);
                                            value.setImgUri(chatData.get(i).getImgUri());
                                            value.setMediaType(chatData.get(i).getMediaType());
                                            chatData.set(i, value);
                                            msgList.remove(j);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            chatData.remove(i);
                    } else
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Refresh chat list
    private void refreshChatList(int type) {
        try {
            if (chatData == null)
                chatData = new ArrayList<>();
            if (msgConvAdapter == null) {
                msgConvAdapter = new MsgConvAdapter(this, chatData, this, userId);
                msgListView.setAdapter(msgConvAdapter);
            } else if (type == 104) {
                if (newMessage)
                    msgConvAdapter.refreshData(chatData);
            } else
                msgConvAdapter.refreshData(chatData);

            if (type == 102) {
                Log.d("Refresh4", "Post Refresh");
//                msgListView.setSelection(chatData.size());
//                msgConvAdapter.notifyDataSetChanged();
//                if (chatData.size() < 4)
//                    hideSoftKeyboard();
            } else if (type == 103 && isLoadingMore) {
                Log.d("Refresh2", "Loading More");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    msgListView.setSelectionFromTop(firstVisPos + newAddedBeforeFirstVisible, top);
                } else {
                    msgListView.setSelection(firstVisPos + newAddedBeforeFirstVisible);
                }
//                msgListView.setBlockLayoutChildren(false);
            } else if (type == 104 && newMessage) {
                Log.d("Refresh3", "Auto Refresh");
                newMessage = false;
                msgListView.setSelection(chatData.size());
            }

            if (isFirstLoading) {
                Log.d("Refresh1", "First Loading");
                msgListView.setSelection(chatData.size());
                isFirstLoading = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        startCountDown();
    }

    @Override
    public void networkError(String error, int APICode) {
        showMessageWithTitle(this, mPref.getString("20", "Alert!"), mPref.getString("269", "Network failed! Please try later."));
        showProgressDialog(false);
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void updateFavStatus(int type, final int pos) {
        try {
            hideKeyboard(MessageConvActivity.this);
            convData = chatData.get(pos);
            OptionsData od = new OptionsData();
            od.setId(convData.getMsgId());
            od.setName(convData.getAttachment());
            od.setImgId(convData.getMediaType());
            if (convData.getImgUri() != null) {
                od.setOptionCode(convData.getImgUri().toString());
                od.setId(-1);
            }
            Intent intent = new Intent(MessageConvActivity.this, ImageViewFullActivity.class);
            intent.putExtra("MediaData", od);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userConStart(MsgUserItem msgUserItem) {
        hideKeyboard(MessageConvActivity.this);
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(etMsg.getWindowToken(), 0);
    }

    public void startCountDown() {
        try {
            stopCountDown();
            countDownTimer = new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    if (!isPostingMsg)
                        gettingUserMessageList(104);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopCountDown() {
        try {
            if (countDownTimer != null)
                countDownTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.send_btn) {
            if (!etMsg.getText().toString().trim().equalsIgnoreCase("") && !isPostingMsg) {
//                showMessageWithTitle(MessageConvActivity.this, mPref.getString("20", "Alert!"), "Please write a message");
                isPostingMsg = true;
                postMessageToUser(etMsg.getText().toString().trim());
            }
        } else if (id == R.id.doc_btn || id == R.id.media_btn) {
            if (checkGalleryPermissions()) {
                openPhoneGallery();
            }
        } else if (id == R.id.camera_btn) {
            if (checkPermissions()) {
                startCamera();
            }
        } else if (id == R.id.attach_btn) {
            if (attachLayout.getVisibility() == View.VISIBLE)
                attachLayout.setVisibility(View.GONE);
            else {
                attachLayout.setVisibility(View.VISIBLE);
                if (chatData != null)
                    msgListView.setSelection(chatData.size());
                hideSoftKeyboard();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CAMERA_MANUAL) {
            if (data != null && !data.getBooleanExtra("Cancel", false)) {
                mCurrentPhotoPath = data.getStringExtra("Path");
                galleryAddPic();
                filePath = mCurrentPhotoPath;
                if (filePath != null) {
                    attachLayout.setVisibility(View.GONE);
                    fileUpload(2, IMAGE_CAPTURE_URI, filePath);
                }
            } else
                attachLayout.setVisibility(View.GONE);
        } else if (requestCode == Constants.CAMERA_CLICK && resultCode == RESULT_OK) {
            galleryAddPic();
            filePath = mCurrentPhotoPath;
            if (filePath != null) {
                attachLayout.setVisibility(View.GONE);
                fileUpload(1, IMAGE_CAPTURE_URI, filePath);
            }
        } else if ((requestCode == YOUR_DOC_RESULT_CODE || requestCode == Constants.GALLERY_CLICK) && resultCode == RESULT_OK) {
            try {
                if (data != null) {
                    Uri _uri = data.getData();
                    if (_uri != null) {
                        Cursor cursor = getContentResolver().query(_uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
//                        Cursor cursor = mActivity.getContentResolver().query(_uri, new String[]{MediaStore.Images.Media.RELATIVE_PATH}, null, null, null);
                        if (cursor != null)
                            cursor.moveToFirst();
                        try {
                            filePath = cursor.getString(0);
                            cursor.close();
                            if (filePath != null) {
                                attachLayout.setVisibility(View.GONE);
                                fileUpload(2, _uri, filePath);
                            }

                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        IMAGE_CAPTURE_URI = Uri.fromFile(f);
        mediaScanIntent.setData(IMAGE_CAPTURE_URI);
        sendBroadcast(mediaScanIntent);
    }

    private void startCamera() {
        Intent intent = new Intent(MessageConvActivity.this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, Constants.CAMERA_MANUAL);
    }

    private void openPhoneGallery() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            String[] mimeTypes = {"image/jpeg", "image/png"};
            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(photoPickerIntent, Constants.GALLERY_CLICK);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, Constants.GALLERY_CLICK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RuntimePermissionUtils.REQUEST_CAMERA:
                if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        startCamera();
                    } else {
                        startCamera();
                    }
                break;
            case RuntimePermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openPhoneGallery();
                break;
        }
    }

    // File Upload
    private void fileUpload(int mediaType, Uri IMAGE_CAPTURE_URI, String path) {
        Message manualAddedMsg = new Message();
        manualAddedMsg.setMsgId(-1);
        manualAddedMsg.setMsg("");
        manualAddedMsg.setAttachment_type("img");
        manualAddedMsg.setMediaType(mediaType);
        manualAddedMsg.setAttachment(mediaType + "");
//        manualAddedMsg.setImgUri(IMAGE_CAPTURE_URI);
        manualAddedMsg.setImgUri(null);
        manualAddedMsg.setAttachmentPath(path);
        manualAddedMsg.setMsg_status(0);
        manualAddedMsg.setMsg_user_id(userId);
        manualAddedMsg.setMsg_time(Utility.getMessageTime());
        manualAddedMsg.setLoaderEnable(true);
        if (chatData == null)
            chatData = new ArrayList<>();
        chatData.add(manualAddedMsg);
        refreshChatList(102);

//        uploadImageOnServer(path, (chatData.size() - 1), msgUserItem.getUid(), null);

        new ImageUploadAsync(IMAGE_CAPTURE_URI, path).execute((chatData.size() - 1), msgUserItem.getUid());
//      showProgressDialog(true);
    }

    private class ImageUploadAsync extends AsyncTask<Integer, Void, Void> {
        private Uri imgUri;
        private String imgPath, imgNewPath;
        private int chatIndex, chatUserId;

        public ImageUploadAsync(Uri image_capture_uri, String path) {
            imgUri = image_capture_uri;
            imgPath = path;
        }

        @Override
        protected Void doInBackground(Integer... data) {
            Log.d("sendMsg", "Image process start");
            chatIndex = data[0];
            chatUserId = data[1];
//            imgNewPath = rotateImage(imgPath);
            new ImageResize().compressImage(imgPath, imgPath);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("sendMsg", "Image process end");
            try {
                chatData.get(chatIndex).setImgUri(imgUri);
                msgConvAdapter.immediateRefreshData(chatData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            uploadImageOnServer(imgPath, chatIndex, chatUserId, imgNewPath);
        }
    }

    private void uploadImageOnServer(String imgPath, int chatIndex, int chatUserId, String imgNewPath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, MConnection.API_BASE_URL + "sendMsg",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("sendMsg + Response", response);
                                try {
                                    JSONObject jObj = new JSONObject(response);
                                    if (jObj.has("status") && jObj.getBoolean("status")) {
//                                JSONObject jo = new JSONObject(jObj.getString("data"));
//                                {
//                                    if (jo.has("msg_list")) {
//                                old code for message bind
                                        parseUserMsgData(jObj.getString("data"), 102);
//                                        refreshChatList(102);
//                                    }
//                                }

//                                if (jObj.has("success"))
//                                    Toast.makeText(getApplicationContext(), jObj.getString("success"), Toast.LENGTH_LONG).show();
                                    } else if (jObj.has("logout") && jObj.getBoolean("logout"))
                                        startLogoutProcess(getApplicationContext(), jObj.getString("error"));
                                    else if (jObj.has("error"))
                                        Toast.makeText(getApplicationContext(), jObj.getString("error"), Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(getApplicationContext(), mPref.getString("270", "Something Wrong! Please try later."), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
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
                });

                // TODO - commented headers in new lib
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("Content-Type", "multipart/form-data;boundary=" + boundary);
//        params.put("X-API-KEY", MConnection.API_KEY);
//        smr.setHeaders(params);

                smr.addStringParam("X-API-KEY", MConnection.API_KEY);

                smr.addStringParam("user_id", userId + "");
                smr.addStringParam("login_key", mPref.getString("LoginKey", ""));
                smr.addStringParam("login_token", mPref.getString("LoginToken", ""));
                smr.addStringParam("chat_user_id", chatUserId + "");
                smr.addStringParam("device_id", mPref.getString("DeviceId", ""));
                smr.addStringParam("lang", mPref.getString("UserSavedLangCode", "en")); // Language
                smr.addStringParam("msg", "");
                smr.addStringParam("chat_index", chatIndex + "");
                smr.addStringParam("tz", timeZone);
                if (imgNewPath != null)
                    smr.addFile("attachedfile", imgNewPath);
                else
                    smr.addFile("attachedfile", imgPath);
                Log.d("sendMsg + request", userId + "," + mPref.getString("LoginKey", "") + "," + mPref.getString("LoginToken", "") + "," + msgUserItem.getUid() + "" + "," + mPref.getString("DeviceId", "") + "," + mPref.getString("UserSavedLangCode", "en") + "," + filePath);
                Volley.newRequestQueue(MessageConvActivity.this).add(smr);
            }
        });
    }

    public Bitmap scalePreserveRatio(Bitmap imageToScale) {
        int MAX_DIMENSION_FOR_UPLOAD = 1280;
        boolean isLandscape = imageToScale.getWidth() > imageToScale.getHeight();

        int newWidth, newHeight;
        if (isLandscape) {
            newWidth = MAX_DIMENSION_FOR_UPLOAD;
            newHeight = Math.round(((float) newWidth / imageToScale.getWidth()) * imageToScale.getHeight());
        } else {
            newHeight = MAX_DIMENSION_FOR_UPLOAD;
            newWidth = Math.round(((float) newHeight / imageToScale.getHeight()) * imageToScale.getWidth());
        }

        if (newWidth > 0 && newWidth > 0 && imageToScale != null) {
            int width = imageToScale.getWidth();
            int height = imageToScale.getHeight();

            //Calculate the max changing amount and decide which dimension to use
            float widthRatio = (float) newWidth / (float) width;
            float heightRatio = (float) newWidth / (float) height;

            //Use the ratio that will fit the image into the desired sizes
            int finalWidth = (int) Math.floor(width * widthRatio);
            int finalHeight = (int) Math.floor(height * widthRatio);
            if (finalWidth > newWidth || finalHeight > newWidth) {
                finalWidth = (int) Math.floor(width * heightRatio);
                finalHeight = (int) Math.floor(height * heightRatio);
            }

            //Scale given bitmap to fit into the desired area
            imageToScale = Bitmap.createScaledBitmap(imageToScale, finalWidth, finalHeight, true);

            //Created a bitmap with desired sizes
            Bitmap scaledImage = Bitmap.createBitmap(newWidth, newWidth, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(scaledImage);

            //Draw background color
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

            //Calculate the ratios and decide which part will have empty areas (width or height)
            float ratioBitmap = (float) finalWidth / (float) finalHeight;
            float destinationRatio = (float) newWidth / (float) newWidth;
            float left = ratioBitmap >= destinationRatio ? 0 : (float) (newWidth - finalWidth) / 2;
            float top = ratioBitmap < destinationRatio ? 0 : (float) (newWidth - finalHeight) / 2;
            canvas.drawBitmap(imageToScale, left, top, null);

            return scaledImage;
        } else {
            return imageToScale;
        }
    }

    public Bitmap transform(Bitmap source) {
        int MAX_DIMENSION_FOR_UPLOAD = 1280;
        boolean isLandscape = source.getWidth() > source.getHeight();

        int newWidth, newHeight;
        if (isLandscape) {
            newWidth = MAX_DIMENSION_FOR_UPLOAD;
            newHeight = Math.round(((float) newWidth / source.getWidth()) * source.getHeight());
        } else {
            newHeight = MAX_DIMENSION_FOR_UPLOAD;
            newWidth = Math.round(((float) newHeight / source.getHeight()) * source.getWidth());
        }

        Bitmap result = Bitmap.createScaledBitmap(source, newWidth, newHeight, false);

        if (result != source)
            source.recycle();

        return result;
    }

    private Bitmap cropImageSize(Bitmap originalImage) {
        int MAX_DIMENSION_FOR_UPLOAD = 1280;
        boolean isLandscape = originalImage.getWidth() > originalImage.getHeight();

        int newWidth, newHeight;
        if (isLandscape) {
            newWidth = MAX_DIMENSION_FOR_UPLOAD;
            newHeight = Math.round(((float) newWidth / originalImage.getWidth()) * originalImage.getHeight());
        } else {
            newHeight = MAX_DIMENSION_FOR_UPLOAD;
            newWidth = Math.round(((float) newHeight / originalImage.getHeight()) * originalImage.getWidth());
        }
        Bitmap background = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();

        Canvas canvas = new Canvas(background);

        float scale = newWidth / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (newHeight - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(originalImage, transformation, paint);

        return background;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

//        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onResume() {
        if (!isFirstLoading)
            startCountDown();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopCountDown();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountDown();
        if (mProgress != null && mProgress.isShowing())
            mProgress.dismiss();
    }

    @Override
    public void onRefresh() {
        mySwipeRefreshLayout.setRefreshing(false);
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
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA);
        }
        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] permissions = neededPermissions.toArray(new String[neededPermissions.size()]);
        if (neededPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, RuntimePermissionUtils.REQUEST_CAMERA);
            }
        } else {
            return true;
        }
        return false;
    }


    private boolean checkGalleryPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String[] permissions = neededPermissions.toArray(new String[neededPermissions.size()]);
        if (neededPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, RuntimePermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            return true;
        }
        return false;
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
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("Notification", false)) {
//            removeAllNotificationData();
            getIntent().putExtra("Notification", false);
            Intent intent = new Intent(this, KenbieActivity.class);
            intent.putExtra("MsgItem", msgUserItem);
            intent.putExtra("NavType", 8);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else
            super.onBackPressed();
    }
}
