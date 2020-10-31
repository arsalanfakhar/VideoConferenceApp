package com.trulyfuture.videoappclone;

import java.io.Serializable;
import java.util.Date;

public class VideoSession{
    String API_KEY;
    String SESSION_ID;
    String TOKEN;
    Date stream;


    public VideoSession(){

    }
    public void setStream(Date dateStream) {
        stream = dateStream;
    }

    public void setAPI_KEY(String KEY) {
        API_KEY = KEY;
    }

    public void setSESSION_ID(String sessionId) {
        SESSION_ID = sessionId;
    }

    public void setTOKEN(String token) {
        TOKEN = token;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }

    public String getSESSION_ID() {
        return SESSION_ID;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public Date getStream() {
        return stream;
    }
}
