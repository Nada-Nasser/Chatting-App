package com.example.chatapp.chattingroom;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.ContactItem;
import com.example.chatapp.R;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom extends AppCompatActivity
{
    String attachedImagePath = "-1";
    ContactItem chattingContact;

    ArrayList<ChattingMessage> messagesList;
    MessagesListAdapter messagesListAdapter;

    ImageView contactPicImageView;
    TextView contactNameTextView;
    TextView contactActiveFlagTextView;
    ListView messagesListView;

    EditText textInputMsgEditText;

    DatabaseReference chattingReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        try {
            contactNameTextView = findViewById(R.id.contact_name);
            contactPicImageView = findViewById(R.id.contact_pic);
            contactActiveFlagTextView = findViewById(R.id.onlineFlag);
            messagesListView = findViewById(R.id.chattingList);
            textInputMsgEditText = findViewById(R.id.chattingTextInput);

            messagesList = new ArrayList<>();
            messagesListAdapter = new MessagesListAdapter(this,messagesList);
            messagesListView.setAdapter(messagesListAdapter);

            Bundle bundle = getIntent().getExtras();
            getChattingContact(bundle);

            chattingReference =  FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(String.valueOf(LoggedInUser.getPhoneNumber()))
                 .child("chats")
                .child(String.valueOf(chattingContact.getPhoneNumber()));

            loadMessages();
        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void loadMessages()
    {
        chattingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot!=null)
                {
                    try
                    {
                        messagesList.clear();

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                        {
                            FirebaseChattingMessage msg = childSnapshot.getValue(FirebaseChattingMessage.class);

                            if(msg != null)
                            {
                               if(AudioMessage.class.getName().equalsIgnoreCase(msg.type))
                               {}
                               else if(TextMessage.class.getName().equalsIgnoreCase(msg.type))
                               {

                                   DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
                                   Date dateobj = new Date(); // TODO fetch msg.sentTime

                                   messagesList.add( new TextMessage(msg.photoPath,msg.text
                                           ,msg.messageID,dateobj,msg.sentByMe));
                               }
                            }
                            else
                                Log.i("NULL","NO USER FOUNDED");
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    refreshMessagesList();

                    Log.i("LoadingDONE","DONE");
                    //  Toast.makeText(getApplicationContext(), USERS, Toast.LENGTH_LONG).show();
                }
                else {
                    Log.i("NULL_DATA_SNAPSHOT","NO dataSnapshot FOUNDED");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void refreshMessagesList()
    {
        messagesListAdapter.notifyDataSetChanged();
        scrollMyListViewToBottom();
    }

    private void getChattingContact(Bundle bundle)
    {
        String userID  = bundle.getString("userID","0");

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
// TODO        contactPicImageView.setImageResource(chattingContact.getImagePath());
        contactActiveFlagTextView.setText(chattingContact.getIsActive()?"online now":chattingContact.getLastOnlineDate().toString());
        contactNameTextView.setText(chattingContact.getName());
    }

    private void scrollMyListViewToBottom() {
        messagesListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                messagesListView.setSelection(messagesListAdapter.getCount() - 1);
            }
        });
    }

    public void sendMessage(View view)
    {
        String msgText = textInputMsgEditText.getText().toString();

        try {
            sendTextMessage( msgText);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    // writing in the database
    private void sendTextMessage(String msgText)
    {
        String msgId = chattingReference.push().getKey();
        Date sentTime = Calendar.getInstance().getTime();
        String photoPath = attachedImagePath;

        FirebaseChattingMessage myFirebaseChattingMessage = new FirebaseChattingMessage(msgId,
                true, TextMessage.class.getName(),sentTime.getTime(),photoPath,msgText, "-1");

        FirebaseChattingMessage contactFirebaseChattingMessage = new FirebaseChattingMessage(msgId,
                false, TextMessage.class.getName(),sentTime.getTime(),photoPath,msgText, "-1");

        Map<String, Object> myRecord = myFirebaseChattingMessage.toMap();
        String myPath = "/users/"+LoggedInUser.getPhoneNumber()
                +"/chats/"+chattingContact.getPhoneNumber()+"/";

        Map<String, Object> contactRecord = contactFirebaseChattingMessage.toMap();
        String contactPath = "/users/"+chattingContact.getPhoneNumber()
                +"/chats/"+LoggedInUser.getPhoneNumber() + "/";

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(myPath + msgId, myRecord);
        childUpdates.put(contactPath+ msgId, contactRecord);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Done", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
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