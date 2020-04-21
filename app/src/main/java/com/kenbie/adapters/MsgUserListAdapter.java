package com.kenbie.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.kenbie.KenbieApplication;
import com.kenbie.R;
import com.kenbie.listeners.MsgUserActionListeners;
import com.kenbie.model.MsgUserItem;
import com.kenbie.util.Constants;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rajaw on 9/12/2017.
 */

public class MsgUserListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MsgUserItem> mUserList;
    private ArrayList<MsgUserItem> oUserList;
    private LayoutInflater mLayoutInflater;
    private MsgUserActionListeners mActionListeners;
    private int mType;
    private RequestOptions options;
    private ItemFilter mFilter = new ItemFilter();
    private SharedPreferences mPref;

    public MsgUserListAdapter(Context context, ArrayList<MsgUserItem> userList, MsgUserActionListeners msgUserActionListeners, int type) {
        mContext = context;
        mUserList = userList;
        oUserList = userList;
        mLayoutInflater = LayoutInflater.from(mContext);
        mActionListeners = msgUserActionListeners;
        mPref = mContext.getSharedPreferences("kPrefs", MODE_PRIVATE);
        mType = type;
        if (mUserList == null)
            mUserList = new ArrayList<>();
        if (oUserList == null)
            oUserList = new ArrayList<>();
        options = new RequestOptions()
                .circleCrop()
                .placeholder(R.drawable.img_c_user_dummy)
                .priority(Priority.HIGH);
    }


    public void refreshData(ArrayList<MsgUserItem> userList) {
        mUserList = userList;
        if (mUserList == null)
            mUserList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void refreshFromData(ArrayList<MsgUserItem> userList) {
        mUserList = userList;
        if (mUserList == null)
            mUserList = new ArrayList<>();
    }

    public int getCount() {
        return mUserList.size();
    }

    public Object getItem(int position) {
        return mUserList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mLayoutInflater.inflate(R.layout.msg_user_cell_view, null);
        else ;

        ImageView userImg = (ImageView) convertView.findViewById(R.id.u_img);

        TextView uStatus = (TextView) convertView.findViewById(R.id.u_status);
        if (mUserList.get(position).getCurrent_status() == 1)
            uStatus.setBackgroundResource(R.drawable.bg_round_green);
        else
            uStatus.setBackgroundResource(R.drawable.bg_round_gray);

        TextView unreadCount = (TextView) convertView.findViewById(R.id.u_unread_count);
        unreadCount.setTypeface(KenbieApplication.S_NORMAL);
        unreadCount.setText(mUserList.get(position).getNew_msg_count() + "");
        if (mUserList.get(position).getNew_msg_count() > 0)
            unreadCount.setVisibility(View.VISIBLE);
        else
            unreadCount.setVisibility(View.GONE);


        TextView userName = (TextView) convertView.findViewById(R.id.u_name);
        userName.setTypeface(KenbieApplication.S_BOLD);

        LinearLayout uFavStatus = (LinearLayout) convertView.findViewById(R.id.u_fav_status);
        uFavStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListeners.updateFavStatus(1, position);
            }
        });

        ImageView favImg = (ImageView) convertView.findViewById(R.id.u_fav_img);

        if (mUserList.get(position).getIsFav() == 1)
            favImg.setBackgroundResource(R.drawable.ic_m_fav);
        else
            favImg.setBackgroundResource(R.drawable.ic_unfav);

        ((LinearLayout) convertView.findViewById(R.id.user_action)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListeners.updateFavStatus(3, position);
//                mActionListeners.userConStart(mUserList.get(position));
            }
        });


        TextView chatDeleteBtn = (TextView) convertView.findViewById(R.id.btn_delete_chat);
        chatDeleteBtn.setText(mPref.getString("343", "Delete"));
        chatDeleteBtn.setTypeface(KenbieApplication.S_NORMAL);
        chatDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListeners.updateFavStatus(2, position);
            }
        });

        if (mType == 1) { // Has membership
            userImg.setBackgroundResource(0);
            userName.setBackgroundResource(0);
            Glide.with(mContext).load(Constants.BASE_IMAGE_URL + mUserList.get(position).getUser_img()).apply(options).into(userImg);
            userName.setText(mUserList.get(position).getUser_name());

            if (mUserList.get(position).getUid() == 1)
                uFavStatus.setVisibility(View.GONE);
            else
                uFavStatus.setVisibility(View.VISIBLE);
        } else {
//            Glide.with(mContext).load(Constants.BASE_IMAGE_URL + mUserList.get(position).getUser_img())
//                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(mContext, 25))).circleCrop()
//                    .into(userImg);
//            Glide.with(mContext).load(Constants.BASE_IMAGE_URL + mUserList.get(position).getUser_img()).apply(options).into(userImg);
//            userImg.setAlpha((float) 0.8);
            userImg.setBackgroundResource(R.drawable.bg_round_light_gray);
            userName.setBackgroundColor(mContext.getResources().getColor(R.color.divider_color));
            uFavStatus.setVisibility(View.GONE);
        }

        return convertView;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<MsgUserItem> list = oUserList;
            int count = list.size();
            final ArrayList<MsgUserItem> sList = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                if (list.get(i).getUser_name().toLowerCase().startsWith(filterString)) {
                    sList.add(list.get(i));
                }
            }

            results.values = sList;
            results.count = sList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUserList = (ArrayList<MsgUserItem>) results.values;
            notifyDataSetChanged();
        }
    }
}



