package com.example.chatapp.chattingroom;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class FirebaseChattingMessage
{
    public String messageID;
    public boolean sentByMe;
    public String type; // audio, text
    public Long sentTime;
    public String photoPath;
    public String text;
    public String audioPath;

    public FirebaseChattingMessage(String messageID, boolean sentByMe, String type, Long sentTime, String photoPath, String text, String audioPath)
    {
        this.messageID = messageID;
        this.sentByMe = sentByMe;
        this.type = type;
        this.sentTime = sentTime;
        this.photoPath = (photoPath != null)?photoPath:"none";
        this.text = text;
        this.audioPath = audioPath;
    }

    @Override
    public String toString() {
        return "FirebaseChattingMessage{" +
                "messageID='" + messageID + '\'' +
                ", sentByMe=" + sentByMe +
                ", type='" + type + '\'' +
                ", sentTime=" + sentTime +
                ", photoPath='" + photoPath + '\'' +
                ", text='" + text + '\'' +
                ", audioPath='" + audioPath + '\'' +
                '}';
    }

    // use it when you need to write msg object in a database
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("messageID", messageID);
        result.put("sentByMe", sentByMe);
        result.put("type", type);
        result.put("sentTime", sentTime);
        result.put("photoPath", photoPath);
        result.put("text", text);
        result.put("audioPath", audioPath);

        return result;
    }

    public FirebaseChattingMessage() {
    }


}
