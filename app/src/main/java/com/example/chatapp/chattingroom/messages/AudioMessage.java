package com.example.chatapp.chattingroom.messages;

import java.util.Date;

public class AudioMessage extends ChattingMessage
{
    private String attachingPath; // downloadUrl from firebase storage (attached path)
    private String localPath; // use it to play the audio

    public AudioMessage(String msgId, Date sentTime, boolean sentByMe, String attachingPath) {
        super(msgId, sentTime, sentByMe);
        this.attachingPath = attachingPath;
    }

    public String getAttachingPath() {
        return attachingPath;
    }

    public void setAttachingPath(String attachingPath) {
        this.attachingPath = attachingPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
