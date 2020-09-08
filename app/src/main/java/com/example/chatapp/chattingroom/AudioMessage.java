package com.example.chatapp.chattingroom;

import java.util.Date;

// TODO  class AudioMessage
public class AudioMessage extends ChattingMessage
{
    String audioPath;

    public AudioMessage(String msgId, Date sentTime, boolean sentByMe) {
        super(msgId, sentTime, sentByMe);
    }
}
