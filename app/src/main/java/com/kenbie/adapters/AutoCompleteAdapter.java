package com.kenbie.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kenbie.KenbieApplication;
import com.kenbie.R;
import com.kenbie.connection.MConnection;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.model.LocationItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AutoCompleteAdapter extends ArrayAdapter<LocationItem> implements Filterable {
    private ArrayList<LocationItem> locationItemArrayList;
    private Context mContext;
    private String deviceId;

    public AutoCompleteAdapter(Context context, int resourceId, ArrayList<LocationItem> locationItems) {
        super(context, resourceId);
        mContext = context;
        locationItemArrayList = locationItems;
        if (locationItemArrayList == null) locationItemArrayList = new ArrayList<>();
        this.deviceId = deviceId;
    }


    public void refreshData(ArrayList<LocationItem> locationItemArrayList) {
        this.locationItemArrayList = locationItemArrayList;
        if (locationItemArrayList == null) this.locationItemArrayList = new ArrayList<>();
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return locationItemArrayList.size();
    }

    @Override
    public LocationItem getItem(int position) {
        return locationItemArrayList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.count = getCount();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.location_cell_view, parent, false);

        //get Country
        LocationItem cityValue = locationItemArrayList.get(position);

        TextView locTitle = (TextView) view.findViewById(R.id.loc_title);
        locTitle.setText(cityValue.getCity() + ", " + cityValue.getCountryName());
        locTitle.setTypeface(KenbieApplication.S_NORMAL);

//        locTitle.setText(cityValue.getCity() + ", " + cityValue.getStateProv() + ", " + cityValue.getCountryName());

        return view;
    }

    //Syncing location
    private class LocationSync extends AsyncTask<String, Void, ArrayList<LocationItem>> implements APIResponseHandler {
        ArrayList<LocationItem> locationList = new ArrayList<>();

        @Override
        protected ArrayList<LocationItem> doInBackground(String... params1) {
            try {
                Map<String, String> params = new HashMap<String, String>();
                params.put("city_search", params1[0]);
                params.put("device_id", deviceId == null ? "" : deviceId);
                new MConnection().postRequestWithHttpHeaders(mContext, "login", this, params, 101);
                return this.locationList;

            } catch (Exception e) {
                Log.d("HUS", "EXCEPTION " + e);
                return null;
            }
        }

        @Override
        public void getError(String error, int APICode) {

        }

        @Override
        public void getResponse(String response, int APICode) {
            try {
                if (response != null) {
                    JSONObject jo = new JSONObject(response);
                    if (jo.getBoolean("status")) {
                        JSONArray jsonArray = jo.getJSONArray("data");
                        locationItemArrayList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jo1 = jsonArray.getJSONObject(i);
                            LocationItem locationItem = new LocationItem();
                            locationItem.setCity(jo1.getString("city"));
                            locationItem.setStateProv(jo1.getString("stateprov"));
                            locationItem.setCountry(jo1.getString("country"));
                            locationItem.setZipCode(jo1.getString("zipcode"));
                            locationItem.setLatitude((float) jo1.getDouble("latitude"));
                            locationItem.setLongitude((float) jo1.getDouble("longitude"));
                            locationItem.setCountryName(jo1.getString("country_name_en"));
                            locationItem.setCountryId(jo1.getInt("country_id"));
                            locationItemArrayList.add(locationItem);
                        }

                        notifyDataSetChanged();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void networkError(String error, int APICode) {

        }
    }
}