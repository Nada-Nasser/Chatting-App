package com.example.chatapp.messagesservices;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
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

public class MessagesListenerJobIntentService extends JobIntentService
{
    final Handler mHandler = new Handler();

    private static final String TAG = "UpdateLocationService";

    /**
     * Unique job ID for this service.
     */

    private static final int JOB_ID = 2;

    static boolean isRunning = false;
    DatabaseReference databaseReference;

    public static void enqueueWork (Context context, Intent intent) {
        enqueueWork (context, MessagesListenerJobIntentService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
     //   databaseReference = FirebaseDatabase.getInstance().getReference();
        showToast("Job Execution Started");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent)
    {

        final Context context = getApplicationContext();
        LoggedInUser.loadData(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

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
/*                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.chat_icon)
                                            .setContentTitle(chattingNotification.senderPhoneNumber)
                                            .setContentText(chattingNotification.msgContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
*/
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

    private void createNotificationChannel(String channel_name) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "";
            String description = "getString(R.string.channel_description)";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_name, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showToast("Job Execution Finished");
    }


    // Helper for showing tests
    void showToast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MessagesListenerJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

// to run the service
/*
Intent mIntent = new Intent(this, MyJobIntentService.class);
mIntent.putExtra("maxCountValue", 1000);
MyJobIntentService.enqueueWork(this, mIntent);
        */
