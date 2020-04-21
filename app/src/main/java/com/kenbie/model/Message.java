package com.kenbie.model;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by rajaw on 9/13/2017.
 */

public class Message implements Serializable {
    private int msgId;
    private int msg_user_id;
    private String msg;
    private int msg_status;
    private String msg_time;
    private String attachment;
    private String attachment_type;
    private Uri imgUri;
    private int mediaType;
    private String attachmentPath;
    private boolean isLoaderEnable;

    public boolean isLoaderEnable() {
        return isLoaderEnable;
    }

    public void setLoaderEnable(boolean loaderEnable) {
        isLoaderEnable = loaderEnable;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getAttachment_type() {
        return attachment_type;
    }

    public void setAttachment_type(String attachment_type) {
        this.attachment_type = attachment_type;
    }

    public int getMsg_user_id() {
        return msg_user_id;
    }

    public void setMsg_user_id(int msg_user_id) {
        this.msg_user_id = msg_user_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMsg_status() {
        return msg_status;
    }

    public void setMsg_status(int msg_status) {
        this.msg_status = msg_status;
    }

    public String getMsg_time() {
        return msg_time;
    }

    public void setMsg_time(String msg_time) {
        this.msg_time = msg_time;
    }

}
