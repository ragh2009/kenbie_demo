package com.kenbie.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rajaw on 7/28/2017.
 */

public class OptionsData implements Serializable {
    private int id;
    private String name;
    private boolean isActive;
    private int imgId;
    private String optionData;
    private String optionCode;
    private ArrayList<Option> optionArrayList;
    private ArrayList<OptionsData> optionDataArrayList;

    public ArrayList<OptionsData> getOptionDataArrayList() {
        return optionDataArrayList;
    }

    public void setOptionDataArrayList(ArrayList<OptionsData> optionDataArrayList) {
        this.optionDataArrayList = optionDataArrayList;
    }

    public ArrayList<Option> getOptionArrayList() {
        return optionArrayList;
    }

    public void setOptionArrayList(ArrayList<Option> optionArrayList) {
        this.optionArrayList = optionArrayList;
    }

    public String getOptionData() {
        return optionData;
    }

    public void setOptionData(String optionData) {
        this.optionData = optionData;
    }

    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int compareTo(OptionsData data) {
        return data.isActive() ? 1 : -1;
    }
}
