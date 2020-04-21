package com.kenbie.model;

import java.io.Serializable;

/**
 * Created by rajaw on 9/7/2017.
 */

public class Option implements Serializable {
    private int id;
    private String title;
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
