package com.example.chatapp.chattingroom;

import java.util.Date;

public abstract class ChattingMessage
{
    protected String msgId;
    protected Date sentTime;
    protected boolean sentByMe;

    public ChattingMessage(String msgId, Date sentTime, boolean sentByMe) {
        this.msgId = msgId;
        this.sentTime = sentTime;
        this.sentByMe = sentByMe;
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
}
