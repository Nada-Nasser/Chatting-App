package com.example.chatapp.chattingroom;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.chatapp.ContactItem;
import com.example.chatapp.R;
import com.example.chatapp.globalinfo.Gender;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatRoom extends AppCompatActivity
{
    private static final int REQUEST_READ_STORAGE_CODE_PERMISSIONS = 111;
    private static final int RESULT_LOAD_IMAGE_CODE = 112;
    private static final int REQUEST_RECORD_AUDIO_CODE_PERMISSIONS = 113;

    String attachedImagePath = "none";
    String attachedAudioPath = "none";
    String lastRecordedAudioFileName = "none";
    ContactItem chattingContact;

    ArrayList<ChattingMessage> messagesList;
    MessagesListAdapter messagesListAdapter;

    ImageView contactPicImageView;
    TextView contactNameTextView;
    TextView contactActiveFlagTextView;
    ListView messagesListView;
    ImageView audioMsgButton;
    LinearLayout msgTestingLayout;
    ImageView imageOverview;
    TextView textOverview;


    EditText textInputMsgEditText;

    DatabaseReference chattingReference;
    AudioMessagesRecorder audioMessagesRecorder;
    Bitmap selectedImageBitmap = null;

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
            audioMsgButton = findViewById(R.id.audioMsgButton);

            msgTestingLayout = findViewById(R.id.msgTesting);
            imageOverview = findViewById(R.id.imageOverview);
            textOverview = findViewById(R.id.textOverview);

            LoggedInUser.beOnlineOnFirebase();

            textInputMsgEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String str = String.valueOf(charSequence);
                    textOverview.setText(str);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

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
            audioMessagesRecorder = new AudioMessagesRecorder(this);

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
                               {
                                   //(String msgId, Date sentTime, boolean sentByMe, String attachingPath)
                                   DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
                                   Date dateobj = new Date(); // TODO fetch msg.sentTime

                                   messagesList.add(new AudioMessage(msg.messageID , dateobj , msg.sentByMe,msg.audioPath));
                               }
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

        int gender  = bundle.getInt("gender" , 1);
        String imagePath = bundle.getString("imagePath" , "none");

        chattingContact = new ContactItem(userID,name,phoneNumber,status,isActive,gender,imagePath);

        updateUI();
    }

    private void updateUI()
    {
        FirebaseDatabase.getInstance().getReference().child("users").child(chattingContact.getPhoneNumber()).child("userInfo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chattingContact  = snapshot.getValue(ContactItem.class);
                        contactActiveFlagTextView.setText(chattingContact.getIsActive()?"online now":"");
                        updateContactImageView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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

    private void commitTextMessageToFirebase(String msgText)
    {
        sendTextMessage(msgText); // TODO sent current date as sentTime
        sendNotification(msgText);

        msgTestingLayout.setVisibility(View.GONE);
        imageOverview.setImageResource(R.drawable.loading_icon);
    }

    public void sendMessage(View view)
    {
        String msgText = textInputMsgEditText.getText().toString();

        textInputMsgEditText.setText("");
        msgTestingLayout.setVisibility(View.GONE);

        // There is a bitmap image?

        // yes: 1- upload the image to get its path. 2- send the message and notification 3- set Bitmap = null again

        // NO: send the message and notification

        try
        {
            if (msgText.length()>0 || selectedImageBitmap!=null)
            {
                if(selectedImageBitmap == null)
                {
                    commitTextMessageToFirebase(msgText);
                }
                else
                {
                    messagesList.add(new TextMessage("loading" , msgText,"",null,true));
                    refreshMessagesList();

                    commitTextMessageToFirebaseWithImage(selectedImageBitmap , msgText);
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    private void sendNotification(String msgText)
    {
        // sender phone number (MINE)
        // msg content

        ChattingNotification chattingNotification =
                new ChattingNotification(chattingContact.getPhoneNumber(), msgText);

        DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(chattingContact.getPhoneNumber()).child("notifications");

        String notificationId = notificationReference.push().getKey();

        Map<String, Object> myRecord = chattingNotification.toMap();

        Map<String, Object> childUpdates = new HashMap<>();

        String path = "/users/"+chattingContact.getPhoneNumber()+"/"+"notifications/";

        childUpdates.put(path + notificationId, myRecord);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"notify", Toast.LENGTH_LONG).show();
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

    // writing in the database
    private void sendTextMessage(String msgText)
    {
        String msgId = chattingReference.push().getKey();
        Date sentTime = Calendar.getInstance().getTime();
        String photoPath = attachedImagePath;

        FirebaseChattingMessage myFirebaseChattingMessage = new FirebaseChattingMessage(msgId,
                true, TextMessage.class.getName(),sentTime.getTime(),photoPath,msgText, "none");

        FirebaseChattingMessage contactFirebaseChattingMessage = new FirebaseChattingMessage(msgId,
                false, TextMessage.class.getName(),sentTime.getTime(),photoPath,msgText, "none");

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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        attachedImagePath = "none";
                    }
                })
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

    private void sendLastRecordedAudioMessage()
    {
        String msgId = chattingReference.push().getKey();
        Date sentTime = getCurrentDate();
        String audioPath = attachedAudioPath;

        FirebaseChattingMessage myFirebaseChattingMessage = new FirebaseChattingMessage(msgId,
                true, AudioMessage.class.getName(),sentTime.getTime(),"none","none", audioPath);

        FirebaseChattingMessage contactFirebaseChattingMessage = new FirebaseChattingMessage(msgId,
                false, AudioMessage.class.getName(),sentTime.getTime(),"none","none", audioPath);

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


    boolean isRecording = false;
    public void onClickSendVoice(View view)
    {
        CheckAudioPermissions();
    }

    private void CheckAudioPermissions()
    {
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_CODE_PERMISSIONS);
                return ;
            }
        }
        useAudio();// init the contact list
    }

    private void useAudio()
    {
        try {
            if (!isRecording)
            {
                Date cDate = getCurrentDate();
                lastRecordedAudioFileName = LoggedInUser.getPhoneNumber()+"_"+dateToStringFormatter(cDate);

                audioMessagesRecorder.onRecord(true , lastRecordedAudioFileName);
                audioMsgButton.setImageResource(R.drawable.active_voice_recorder_icon);
                Toast.makeText(getApplicationContext(), "Start recording your voice, press again to stop recording", Toast.LENGTH_LONG).show();
            }
            else
            {
                audioMessagesRecorder.onRecord(false , lastRecordedAudioFileName);
                audioMsgButton.setImageResource(R.drawable.mic_icon_blue);

                uploadAudioToFirebaseStorage(audioMessagesRecorder.getLastRecordedAudioFilePath());

                // lastRecordedAudioFileName = "none";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage() , Toast.LENGTH_LONG).show();
        }
        isRecording = !isRecording;
    }

    @Override
    protected void onStop() {
        super.onStop();
        audioMessagesRecorder.stopAudioManager();
    }

    public void onClickContactInfo(View view)
    {

    }

    public void onClickAttachImage(View view)
    {
        CheckReadExternalStoragePermissionAndPickImage(); // save the image path in a string

    }

    void updateContactImageView()
    {
        if(chattingContact.getImagePath().equalsIgnoreCase("none")) //when user has no image
        {
            int imageResource = chattingContact.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
            contactPicImageView.setImageResource(imageResource);
        }
        else // download the image from firebase storage and load it in the image view using picasso lib.
        {
            try
            {
                Picasso.get().setLoggingEnabled(true);

                FirebaseStorage.getInstance().getReference(chattingContact.getImagePath()).getDownloadUrl()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception)
                            {
                                int imageResource = chattingContact.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
                                contactPicImageView.setImageResource(imageResource);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                Picasso.get().load(downloadUrl).into(contactPicImageView);
                                Log.i("TAG", "onSuccess: " + downloadUrl + " ----> " + downloadUrl.toString());
                            }
                        });

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    void CheckReadExternalStoragePermissionAndPickImage(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_STORAGE_CODE_PERMISSIONS);
                return ;
            }
        }
        pickImage();// init the contact list
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE_CODE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"You can not put image without this permissions" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case REQUEST_RECORD_AUDIO_CODE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useAudio();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"You can not put image without this permissions" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void pickImage() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == RESULT_LOAD_IMAGE_CODE && resultCode == RESULT_OK && null != data)
        {
            msgTestingLayout.setVisibility(View.VISIBLE);

            try {

                Uri selectedImageUri = data.getData();
                String[] dataPath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver()
                        .query(selectedImageUri, dataPath, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(dataPath[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                //userPicImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                selectedImageBitmap = BitmapFactory.decodeFile(picturePath);
                imageOverview.setImageBitmap(selectedImageBitmap);

                //uploadImage(BitmapFactory.decodeFile(picturePath));

            }catch (Exception Ex)
            {
                Ex.printStackTrace();
                Toast.makeText(getApplicationContext() , "Could not upload the image" ,Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage(Bitmap bitmap)
    {
        try {
            final Bitmap imageBitmapCopy = bitmap;

            DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
            Date dateobj = new Date();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReferenceFromUrl("gs://chatapp-dfb4b.appspot.com");

            final String ImagePath = LoggedInUser.getUserID()+ "_" + df.format(dateobj) + ".jpg";

            final StorageReference mountainsRef = storageRef.child("images/" + ImagePath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = mountainsRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception)
                {
                        MyProgressDialogManager.hideProgressDialog();
                        Toast.makeText(getApplicationContext(),"couldn't Attach the image " + exception.getMessage()  , Toast.LENGTH_SHORT).show();
                        }
            })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            try {
                                attachedImagePath = mountainsRef.getPath();
                                imageOverview.setImageBitmap(imageBitmapCopy);

                                Toast.makeText(getApplicationContext(),"Image Attached" , Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                                MyProgressDialogManager.hideProgressDialog();
                                e.printStackTrace();
                            }
                        }

                    });

        }
        catch (Exception e)
        {
            e.printStackTrace();
            MyProgressDialogManager.hideProgressDialog();
            Toast.makeText(getApplicationContext(),"Couldn't upload the image" , Toast.LENGTH_LONG).show();
        }
    }


    private void commitTextMessageToFirebaseWithImage(Bitmap bitmap, final String msgText)
    {
        try {
            final Bitmap imageBitmapCopy = bitmap;

            DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
            Date dateobj = new Date();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReferenceFromUrl("gs://chatapp-dfb4b.appspot.com");

            final String ImagePath = LoggedInUser.getUserID()+ "_" + df.format(dateobj) + ".jpg";

            final StorageReference mountainsRef = storageRef.child("images/" + ImagePath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            UploadTask uploadTask = mountainsRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception)
                {
                    MyProgressDialogManager.hideProgressDialog();
                    Toast.makeText(getApplicationContext(),"couldn't Attach the image " + exception.getMessage()  , Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            try {
                                attachedImagePath = mountainsRef.getPath();
                                commitTextMessageToFirebase(msgText);
                                selectedImageBitmap = null;
                            }
                            catch (Exception e) {
                                MyProgressDialogManager.hideProgressDialog();
                                e.printStackTrace();
                            }
                        }

                    });

        }
        catch (Exception e)
        {
            e.printStackTrace();
            MyProgressDialogManager.hideProgressDialog();
            Toast.makeText(getApplicationContext(),"Couldn't upload the image" , Toast.LENGTH_LONG).show();
        }
    }


    private void uploadAudioToFirebaseStorage(String audioPath)
    {
        MyProgressDialogManager.showProgressDialog(this);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/mpeg")
                .build();

        Uri file = Uri.fromFile(new File(audioPath));

        final StorageReference riversRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://chatapp-dfb4b.appspot.com")
                .child("voiceNotes/"+ this.lastRecordedAudioFileName);

        UploadTask uploadTask = riversRef.putFile(file , metadata);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                try {
                    attachedAudioPath =  riversRef.getPath();
                    Toast.makeText(getApplicationContext(),"Audio Attached" , Toast.LENGTH_SHORT).show();

                    sendLastRecordedAudioMessage();
                    sendNotification(chattingContact.getName() + " sent you voice note..");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"couldn't Attached the audio" , Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                MyProgressDialogManager.hideProgressDialog();
            }
        });
    }

    Date getCurrentDate()
    {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        return c;
    }

    String dateToStringFormatter(Date date)
    {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(date);

        return  formattedDate;
    }

    public void onClickCloseOverViewLayout(View view)
    {
        closeMessageOverview();
    }

    void closeMessageOverview()
    {
        msgTestingLayout.setVisibility(View.GONE);
        attachedImagePath = "none";
        imageOverview.setImageResource(R.drawable.loading_icon);
    }
}