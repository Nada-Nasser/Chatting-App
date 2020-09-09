package com.example.chatapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.chatapp.globalinfo.Gender;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends AppCompatActivity
{
    private static final int REQUEST_READ_STORAGE_CODE_PERMISSIONS = 111;
    private static final int RESULT_LOAD_IMAGE_CODE = 112;

    ImageView userPicImageView;
    EditText userPhoneNumberEditText;
    int userGender;
    private String attachedImageURL = null;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userPicImageView = findViewById(R.id.user_pic);
        userPhoneNumberEditText = findViewById(R.id.user_phone_number);

        firebaseAuth = FirebaseAuth.getInstance();

    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        signInAnonymously();
    }

    private void signInAnonymously()
    {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d("firebaseAuth", "signInAnonymously:success");
                           // Toast.makeText(getApplicationContext(), "Authentication Succeed.", Toast.LENGTH_SHORT).show();

                            LoggedInUser.setUserID(firebaseAuth.getCurrentUser().getUid());
                        }
                        else {
                            Log.w("firebaseAuth", "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed. check your internet connection", Toast.LENGTH_SHORT).show();
                          // TODO  finish();
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();

        MyProgressDialogManager.hideProgressDialog();
    }

    public void onRadioButtonClicked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_male:
                if (checked)
                    userGender = Gender.MALE;
                    break;
            case R.id.radio_female:
                if (checked)
                    userGender = Gender.FEMALE;
                    break;
        }
    }

    public void onClickAddPhoto(View view)
    {
        CheckReadExternalStoragePermissionAndPickImage();
    }

    public void onClickLoginBu(View view)
    {
        LoggedInUser.saveUserDate(this,userPhoneNumberEditText.getText().toString()
        ,"you",attachedImageURL,firebaseAuth.getCurrentUser().getUid(),userGender);
/*
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(LoggedInUser.getPhoneNumber()).setValue("0");
*/
        // TODO: check if image equals null.

        // TODO: step1: logged in using Firebase Auth (with phone number). and delete the Anonymous user

        Intent mainActivityIntent = new Intent(this,MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
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
                    pickImage();// init the contact list
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
            try {
                MyProgressDialogManager.showProgressDialog(this);

                Uri selectedImageUri = data.getData();
                String[] dataPath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver()
                        .query(selectedImageUri, dataPath, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(dataPath[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                userPicImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

                uploadImage(BitmapFactory.decodeFile(picturePath));

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
            MyProgressDialogManager.showProgressDialog(this);

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
                    Toast.makeText(getApplicationContext(),"couldn't Attach the image " + exception.getMessage()  , Toast.LENGTH_SHORT).show();
                    userPicImageView.setImageResource(R.drawable.add_user_pic);
                }
            })
            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            MyProgressDialogManager.hideProgressDialog();
                        }
                    })
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    try {
                        attachedImageURL = mountainsRef.getPath();

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
}