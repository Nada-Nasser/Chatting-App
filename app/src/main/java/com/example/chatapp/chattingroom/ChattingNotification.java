package com.example.chatapp.chattingroom;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ChattingNotification
{
    public String senderPhoneNumber;
    public String msgContent;

    public ChattingNotification(String senderPhoneNumber, String msgContent) {
        this.senderPhoneNumber = senderPhoneNumber;
        this.msgContent = msgContent;
    }

    public ChattingNotification() {
    }

    @Override
    public String toString() {
        return "ChattingNotification{" +
                "senderPhoneNumber='" + senderPhoneNumber + '\'' +
                ", msgContent='" + msgContent + '\'' +
                '}';
    }

    // use it when you need to write ChattingNotification object in a database
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("senderPhoneNumber", senderPhoneNumber);
        result.put("msgContent", msgContent);
        return result;
    }
}
