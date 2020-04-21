package com.kenbie.model;

import java.io.Serializable;

/**
 * Created by rajaw on 9/13/2017.
 */

public class MsgUserItem implements Serializable {
    private int uid;
    private int current_status;
    private int isFav;
    private String user_name;
    private String user_img;
    private String last_response_time;
    private int new_msg_count;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCurrent_status() {
        return current_status;
    }

    public void setCurrent_status(int current_status) {
        this.current_status = current_status;
    }

    public int getIsFav() {
        return isFav;
    }

    public void setIsFav(int isFav) {
        this.isFav = isFav;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_img() {
        return user_img;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public String getLast_response_time() {
        return last_response_time;
    }

    public void setLast_response_time(String last_response_time) {
        this.last_response_time = last_response_time;
    }

    public int getNew_msg_count() {
        return new_msg_count;
    }

    public void setNew_msg_count(int new_msg_count) {
        this.new_msg_count = new_msg_count;
    }

}
