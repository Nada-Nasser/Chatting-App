package com.example.chatapp.chattingroom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatapp.contactsmanager.ContactItem;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRoomManager {

    static public void OpenChatRoom(Context context, @NonNull ContactItem contactItem) {
        Activity activity = (Activity) context;
        Intent chattingRoomIntent = new Intent(context, ChatRoom.class);

        chattingRoomIntent.putExtra("userID", contactItem.getUserID());
        chattingRoomIntent.putExtra("name", contactItem.getName());
        chattingRoomIntent.putExtra("phoneNumber", contactItem.getPhoneNumber());
        chattingRoomIntent.putExtra("status", contactItem.getStatus());
        chattingRoomIntent.putExtra("isActive", contactItem.getIsActive());

        chattingRoomIntent.putExtra("gender", contactItem.getGender());
        chattingRoomIntent.putExtra("imagePath", contactItem.getImagePath());

        activity.startActivity(chattingRoomIntent);
    }

    static public void OpenChattingRoomWithContactIfExists(final Context context, String cNumber) {
        try {
            MyProgressDialogManager.showProgressDialog(context);
            FirebaseDatabase.getInstance().getReference().child("users").child(cNumber).child("userInfo")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                ContactItem contactItem = snapshot.getValue(ContactItem.class);

                                OpenChatRoom(context, contactItem);
                                MyProgressDialogManager.hideProgressDialog();
                            } catch (Exception e) {
                                MyProgressDialogManager.hideProgressDialog();
                                Toast.makeText(context, "This Contact has no account", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "This Contact has no account", Toast.LENGTH_LONG).show();
                        }

                    });
        } catch (Exception e) {
            MyProgressDialogManager.hideProgressDialog();
            Toast.makeText(context, "This Contact has no account", Toast.LENGTH_LONG).show();
        }
    }
}
