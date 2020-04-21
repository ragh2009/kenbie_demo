package com.kenbie.data;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class LanguageParser {

    public void saveLanguageData(SharedPreferences pref, String response) {
        try {
            JSONObject jo = new JSONObject(response);
            SharedPreferences.Editor editor = pref.edit();

            if (jo.has("available_lang"))
                editor.putString("AvailableLang", jo.getString("available_lang"));

            editor.putBoolean("SelLanguage", true);

            if (jo.has("lang"))
                editor.putString("UserSavedLangCode", jo.getString("lang"));

            if (jo.has("lang_dir"))
                editor.putString("UserSavedLang", jo.getString("lang_dir"));

            if (jo.has("data")) {
                JSONArray langData = jo.getJSONArray("data");
                for (int i = 0; i < langData.length(); i++) {
                    JSONObject jData = langData.getJSONObject(i);
                    editor.putString(jData.getString("id"), jData.getString("title"));
                }
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
