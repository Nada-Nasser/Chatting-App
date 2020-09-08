package com.example.chatapp.chattingroom;

import java.util.Date;

public class TextMessage extends ChattingMessage
{
    private String photoPath; // TODO : change data type to string
    private String text;

    public TextMessage(String photoPath, String text, String msgId, Date sentTime, boolean sentByMe)
    {
        super(msgId,sentTime,sentByMe);
        this.photoPath = photoPath;
        this.text = text;
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


    @Override
    public String toString() {
        return "TextMessage{" +
                "photoPath='" + photoPath + '\'' +
                ", text='" + text + '\'' +
                ", msgId='" + msgId + '\'' +
                ", sentTime=" + sentTime +
                ", sentByMe=" + sentByMe +
                '}';
    }
}
