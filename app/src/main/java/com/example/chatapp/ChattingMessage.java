package com.example.chatapp;

import java.util.Date;

public class ChattingMessage
{
    private String photoPath;
    private String text;
    private String msgId;
    private Date sentTime;
    private boolean sentByMe;

    public ChattingMessage(String photoPath, String text, String msgId, Date sentTime, boolean sentByMe) {
        this.photoPath = photoPath;
        this.text = text;
        this.msgId = msgId;
        this.sentTime = sentTime;
        this.sentByMe = sentByMe;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        this.sentByMe = sentByMe;
    }

    @Override
    public String toString() {
        return "ChattingMessage{" +
                "photoPath='" + photoPath + '\'' +
                ", text='" + text + '\'' +
                ", msgId='" + msgId + '\'' +
                ", sentTime=" + sentTime +
                ", sentByMe=" + sentByMe +
                '}';
    }
}
