package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.firebase.database.DatabaseReference;

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
    protected void onHandleWork(@NonNull Intent intent) {
        /**
         * Write code here.. Perform Long operation here such as Download/Upload of file, Sync Some data
         * The system or framework is already holding a wake lock for us at this point
         */

        /*
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(GlobalInfo.getPhoneNumber()).child("updates")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        Location myLocation = TrackerListener.location;

                        if(myLocation==null)
                            return;

                        databaseReference.child("users").child(GlobalInfo.getPhoneNumber()).child("Location")
                                .child("Latitude").setValue(myLocation.getLatitude());

                        databaseReference.child("users").child(GlobalInfo.getPhoneNumber()).child("Location")
                                .child("Longitude").setValue(myLocation.getLongitude());

                        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        Date date = new Date();
                        databaseReference.child("users").child(GlobalInfo.getPhoneNumber()).child("Location")
                                .child("LastOnlineDate").setValue(dateFormat.format(date));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
                */

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
