package com.example.chatapp.messagesservices;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.chatapp.R;
import com.example.chatapp.activities.MainActivity;
import com.example.chatapp.chattingroom.ChattingNotification;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessagesListenerService extends Service {
    public MessagesListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TAG", "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        startListening();

        return START_STICKY;
    }

    private void startListening()
    {
        final Context context = getApplicationContext();
        LoggedInUser.loadData(this);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(LoggedInUser.getPhoneNumber()).child("notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        try
                        {
                            int NotificationID = 0;
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                            {
                                ChattingNotification chattingNotification =
                                        childSnapshot.getValue(ChattingNotification.class);
                                String CHANNEL_ID = childSnapshot.getKey();

                                if(chattingNotification != null)
                                {
                                    // send notification
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.chat_icon)
                                            .setContentTitle(chattingNotification.senderPhoneNumber)
                                            .setContentText(chattingNotification.msgContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            // Set the intent that will fire when the user taps the notification
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true);

                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                    // notificationId is a unique int for each notification that you must define
                                    notificationManager.notify(NotificationID++, builder.build());

                                    FirebaseDatabase.getInstance().getReference().child("users")
                                            .child(LoggedInUser.getPhoneNumber()).child("notifications")
                                            .setValue(null);

                                }
                                else
                                    Log.i("NULL","NO USER FOUNDED");
                            }


                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(LoggedInUser.getPhoneNumber()).child("notifications")
                                    .setValue(null);

                        }catch (Exception e) {
                            e.printStackTrace();}
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });


    }


    @Override
    public void onCreate() {
        Log.e("TAG", "onCreate");


    }

    @Override
    public void onDestroy() {
        Log.e("TAG", "onDestroy");
        //stoptimertask();
         //startService(new Intent(this, MessagesListenerService.class));
        super.onDestroy();
    }


}
