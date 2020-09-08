package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatRoom extends AppCompatActivity
{
    ContactItem chattingContact;

    ImageView contactPicImageView;
    TextView contactNameTextView;
    TextView contactActiveFlagTextView;

    EditText textInputMsgEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        contactNameTextView = findViewById(R.id.contact_name);
        contactPicImageView = findViewById(R.id.contact_pic);
        contactActiveFlagTextView = findViewById(R.id.onlineFlag);


        Bundle bundle = getIntent().getExtras();
        getChattingContact(bundle);
    }

    private void getChattingContact(Bundle bundle)
    {
        int userID  = bundle.getInt("userID");

        String phoneNumber  = bundle.getString("phoneNumber" , null);
        String name  = bundle.getString("name" , phoneNumber!=null?phoneNumber:"unknown");
        String status  = bundle.getString("status",null);
        boolean isActive  = bundle.getBoolean("isActive",false);

        Date lastOnlineDate = new Date();
        lastOnlineDate.setTime(bundle.getLong("date", -1));

        int gender  = bundle.getInt("gender" , 1);
        // TODO: receive image path too.

        chattingContact = new ContactItem(userID,name,phoneNumber,status,isActive,lastOnlineDate,gender);

        updateUI();
    }

    private void updateUI()
    {
        contactPicImageView.setImageResource(chattingContact.getImagePath());
        contactActiveFlagTextView.setText(chattingContact.getActive()?"online now":chattingContact.getLastOnlineDate().toString());
        contactNameTextView.setText(chattingContact.getName());
    }

    public void sendMessage(View view)
    {

    }

    public void onClickSendVoice(View view)
    {

    }

    public void onClickContactInfo(View view)
    {

    }

    public void onClickAttachImage(View view)
    {

    }
}