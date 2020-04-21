package com.kenbie.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kenbie.KenbieApplication;
import com.kenbie.MessageConvActivity;
import com.kenbie.R;
import com.kenbie.adapters.MsgUserListAdapter;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.listeners.MsgUserActionListeners;
import com.kenbie.model.MsgUserItem;
import com.kenbie.model.UserItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

public class MessageUserListFragment extends BaseFragment implements APIResponseHandler, MsgUserActionListeners, SwipeRefreshLayout.OnRefreshListener {
    private ArrayList<MsgUserItem> msgUserAllList;
    private int favPos, msgPos = -1;
    private ListView mUserListView;
    private MsgUserListAdapter msgUserListAdapter;
    private EditText searchUser;
    private TextView noText;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private boolean isLoading;

    public MessageUserListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mySwipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        mySwipeRefreshLayout.setColorSchemeResources(
                R.color.red_g_color);
        mySwipeRefreshLayout.setOnRefreshListener(this);

        searchUser = view.findViewById(R.id.search_user);
        searchUser.setHint(mActivity.mPref.getString("58", "Search"));
        searchUser.setTypeface(KenbieApplication.S_NORMAL);
        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (msgUserListAdapter != null)
                    msgUserListAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUserListView = (ListView) view.findViewById(R.id.m_user_list);
        noText = (TextView) view.findViewById(R.id.no_text);
        noText.setText(mActivity.mPref.getString("148", "Data is not found. Please pull to refresh."));
        noText.setTypeface(KenbieApplication.S_NORMAL);
    }

    private void getMsgUserListFromServer(boolean b) {
        if (mActivity.isOnline()) {
            isLoading = true;
            mySwipeRefreshLayout.setRefreshing(b);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", mActivity.mPref.getString("UserId", ""));
            params.put("login_key", mActivity.mPref.getString("LoginKey", ""));
            params.put("login_token", mActivity.mPref.getString("LoginToken", ""));
            mActivity.mConnection.postRequestWithHttpHeaders(mActivity, "getChatUserList", this, params, 101);
        } else {
            isLoading = false;
            mySwipeRefreshLayout.setRefreshing(false);
            mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), mActivity.mPref.getString("269", "Network failed! Please try later."));
        }
    }

    // Refresh user data
    private void refreshData() {
        try {
//        if (type == 1)
            if (msgUserAllList == null)
                msgUserAllList = new ArrayList<>();

            if (msgUserAllList.size() == 0) {
                noText.setVisibility(View.VISIBLE);
                mUserListView.setVisibility(View.GONE);
            } else {
                noText.setVisibility(View.GONE);
                mUserListView.setVisibility(View.VISIBLE);
            }

            if (msgUserListAdapter == null || mUserListView.getFirstVisiblePosition() == 0) {
                msgUserListAdapter = new MsgUserListAdapter(mActivity, msgUserAllList, this, mActivity.mPref.getInt("MemberShip", 0));

                mUserListView.setAdapter(msgUserListAdapter);
            } else {
                msgUserListAdapter.refreshData(msgUserAllList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getError(String error, int APICode) {
        if (error != null)
            mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), error);
        else
            mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), mActivity.mPref.getString("270", "Something Wrong! Please try later."));

        isLoading = false;
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void getResponse(String response, int APICode) {
        try {
            if (response != null) {
                JSONObject jo = new JSONObject(response);

                if (APICode == 101) { // Message Users List
                    if (jo.has("status") && jo.getBoolean("status")) {
                        if (jo.has("data"))
                            parseUserMsgData(jo.getString("data"));
                        refreshData();
                    } else if (jo.has("error"))
                        mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), jo.getString("error"));
                    else
                        mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), mActivity.mPref.getString("270", "Something Wrong! Please try later."));
                } else if (APICode == 102) {
                    if (jo.has("status") && jo.getBoolean("status")) {
                        //mActivity.bindBottomNavigationData();
                    }
                } else if (APICode == 103) {
                    msgUserAllList.remove(favPos);
                    msgUserListAdapter.refreshFromData(msgUserAllList);
                    if (msgUserAllList == null)
                        msgUserAllList = new ArrayList<>();

                    if (msgUserAllList.size() == 0) {
                        noText.setVisibility(View.VISIBLE);
                        mUserListView.setVisibility(View.GONE);
                    } else {
                        noText.setVisibility(View.GONE);
                        mUserListView.setVisibility(View.VISIBLE);
                    }

                    mUserListView.setAdapter(msgUserListAdapter);
                    mActivity.showProgressDialog(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isLoading = false;
        mySwipeRefreshLayout.setRefreshing(false);
    }

    public void showTopPosition(int i) {
        if (msgUserListAdapter != null && mUserListView != null)
            mUserListView.setAdapter(msgUserListAdapter);
    }

    // Refresh fav data updateFavStatus
    private void favRefreshData() {
        try {
            msgUserListAdapter.refreshData(msgUserAllList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void networkError(String error, int APICode) {
        mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), mActivity.mPref.getString("269", "Network failed! Please try later."));
        isLoading = false;
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void updateFavStatus(int type, int pos) {
        if (mActivity.mPref.getInt("MemberShip", 0) != 0) {
            this.favPos = pos;

            if (type == 1) {
//                this.favType = type;
                try {
                    msgUserAllList.get(favPos).setIsFav(msgUserAllList.get(pos).getIsFav() == 1 ? 0 : 1);
                    favRefreshData();
                    addedUserFavorites(msgUserAllList.get(pos).getUid());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (type == 2) {
                if (msgUserAllList.get(pos).getUid() != 1)
                    deleteChatUser(msgUserAllList.get(pos).getUid());
            } else if (type == 3) {
                msgPos = pos;
                Intent intent = new Intent(mActivity, MessageConvActivity.class);
                intent.putExtra("MsgItem", msgUserAllList.get(pos));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
            }
        } else {
            UserItem userItem = new UserItem();
            userItem.setId(msgUserAllList.get(pos).getUid());
            userItem.setFirstName(msgUserAllList.get(pos).getUser_name());
            userItem.setUserPic(msgUserAllList.get(pos).getUser_img());
//            mActivity.showMemberShipInfo(userItem, 1);
        }
    }


    @Override
    public void userConStart(MsgUserItem msgUserItem) {
        if (mActivity.mPref.getInt("MemberShip", 0) != 0) {
            Intent intent = new Intent(mActivity, MessageConvActivity.class);
            intent.putExtra("MsgItem", msgUserItem);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.startActivity(intent);
        } else {
            UserItem userItem = new UserItem();
            userItem.setId(msgUserItem.getUid());
            userItem.setFirstName(msgUserItem.getUser_name());
            userItem.setUserPic(msgUserItem.getUser_img());
//            mActivity.showMemberShipInfo(userItem, 1);
        }
    }

    // Delete chat user
    private void deleteChatUser(int uid) {
        if (mActivity.isOnline()) {
            mActivity.showProgressDialog(true);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", mActivity.mPref.getString("UserId", ""));
            params.put("login_key", mActivity.mPref.getString("LoginKey", ""));
            params.put("login_token", mActivity.mPref.getString("LoginToken", ""));
            params.put("chat_user_id", uid + "");
            mActivity.mConnection.postRequestWithHttpHeaders(mActivity, "deleteChat", this, params, 103);
        } else {
            mActivity.showProgressDialog(false);
            mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), mActivity.mPref.getString("269", "Network failed! Please try later."));
        }
    }


    // Added into fav list
    private void addedUserFavorites(int uid) {
        if (mActivity.isOnline()) {
//            mActivity.showProgressDialog(true);
            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", mActivity.mPref.getString("UserId", ""));
            params.put("login_key", mActivity.mPref.getString("LoginKey", ""));
            params.put("login_token", mActivity.mPref.getString("LoginToken", ""));
            params.put("user_type", mActivity.mPref.getInt("UserType", 1) + "");
            params.put("fav_id", uid + "");
            try {
                WifiManager wm = (WifiManager) mActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                if (ip == null)
                    params.put("ip", "");
                else
                    params.put("ip", ip + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mActivity.mConnection.postRequestWithHttpHeaders(mActivity, "addFavourite", this, params, 102);
        } else {
            mActivity.showProgressDialog(false);
            mActivity.showMessageWithTitle(mActivity, mActivity.mPref.getString("20", "Alert!"), mActivity.mPref.getString("269", "Network failed! Please try later."));
        }
    }

    // Parse message user list
    public void parseUserMsgData(String data) {
        try {
            msgUserAllList = new ArrayList<>();
//            msgUserOnlineList = new ArrayList<>();
//            msgUserFavouritesList = new ArrayList<>();

            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = new JSONObject(jsonArray.getString(i));
                MsgUserItem value = new MsgUserItem();
                value.setUid(jo.getInt("uid"));
                value.setCurrent_status(jo.getInt("current_status"));
                value.setIsFav(jo.getInt("isFav"));
                value.setUser_name(jo.getString("first_name"));
                value.setUser_img(jo.getString("user_img"));
                value.setLast_response_time(jo.getString("last_response_time"));
                value.setNew_msg_count(jo.getInt("new_msg_count"));
                msgUserAllList.add(value);
                    /*if (value.getCurrent_status() == 1)
                        msgUserOnlineList.add(value);
                    if (value.getIsFav() == 1)
                        msgUserFavouritesList.add(value);*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMsgPostedStatus();
        getMsgUserListFromServer(msgUserListAdapter == null);
        mActivity.hideKeyboard(mActivity);
    }

    private void updateMsgPostedStatus() {
        try {
            if (mActivity.mPref.getBoolean("MsgPosted", false) && msgUserAllList != null && msgPos != -1) {
                MsgUserItem msgUserItem = msgUserAllList.get(msgPos);
                msgUserAllList.remove(msgPos);
                msgUserAllList.add(0, msgUserItem);
                msgUserListAdapter.refreshData(msgUserAllList);
                SharedPreferences.Editor editor = mActivity.mPref.edit();
                editor.putBoolean("MsgPosted", false);
                editor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if (!isLoading)
            getMsgUserListFromServer(true);
    }

    public void refreshFromNotification() {
        getMsgUserListFromServer(msgUserListAdapter == null);
    }
}
