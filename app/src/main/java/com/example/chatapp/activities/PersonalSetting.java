package com.example.chatapp.activities;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.chatapp.R;
import com.example.chatapp.globalinfo.Gender;
import com.example.chatapp.globalinfo.GlobalOperations;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PersonalSetting extends AppCompatActivity
{
    private static final int REQUEST_READ_STORAGE_CODE_PERMISSIONS = 111;
    private static final int RESULT_LOAD_IMAGE_CODE = 112;
    private static final int REQUEST_RECORD_AUDIO_CODE_PERMISSIONS = 113;

//    ContactItem myInfo;
    ImageView userImageView;
    EditText statusEditText;

    String attachedImagePath = "none";
    Bitmap selectedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_setting);

        userImageView = findViewById(R.id.userImage);
        statusEditText = findViewById(R.id.userStatus);

        MyProgressDialogManager.showProgressDialog(this);

        LoggedInUser.beOnlineOnFirebase();

        statusEditText.setText(LoggedInUser.getStatus());
        updateContactImageView();
    }


    void updateContactImageView()
    {
        if(LoggedInUser.getPhotoPath().equalsIgnoreCase("none")) //when user has no image
        {
            int imageResource = LoggedInUser.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
            userImageView.setImageResource(imageResource);
        }
        else // download the image from firebase storage and load it in the image view using picasso lib.
        {
            try
            {
                Picasso.get().setLoggingEnabled(true);

                FirebaseStorage.getInstance().getReference(LoggedInUser.getPhotoPath()).getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                MyProgressDialogManager.hideProgressDialog();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception)
                            {
                                int imageResource = LoggedInUser.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
                                userImageView.setImageResource(imageResource);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                Picasso.get().load(downloadUrl).into(userImageView);
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

                Uri selectedImageUri = data.getData();
                String[] dataPath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver()
                        .query(selectedImageUri, dataPath, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(dataPath[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                selectedImageBitmap = BitmapFactory.decodeFile(picturePath);
                userImageView.setImageBitmap(selectedImageBitmap);

            }catch (Exception Ex)
            {
                Ex.printStackTrace();
                Toast.makeText(getApplicationContext() , "Could not upload the image" ,Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickEditImage(View view)
    {
        CheckReadExternalStoragePermissionAndPickImage();
    }

    public void onClickApplyChanges(View view)
    {
        MyProgressDialogManager.showProgressDialog(this);
        if (selectedImageBitmap == null)
        {
            if(statusEditText.getText().toString().length() >0 ) {
                UpdateMyInfoOnFirebase(statusEditText.getText().toString());
            }
            else
            {
                UpdateMyInfoOnFirebase("none");
            }
        }
        else {
            uploadImage(selectedImageBitmap);
        }

    }

    private void uploadImage(Bitmap bitmap)
    {
        try {
            final Bitmap imageBitmapCopy = bitmap;

            DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
            Date date = GlobalOperations.getCurrentDate();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReferenceFromUrl("gs://chatapp-dfb4b.appspot.com");

            final String ImagePath = LoggedInUser.getUserID() + "_" + df.format(date) + ".jpg";

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

                                Toast.makeText(getApplicationContext(),"Image Attached" , Toast.LENGTH_SHORT).show();

                                if(statusEditText.getText().toString().length() >0 ) {
                                    UpdateMyInfoOnFirebase(statusEditText.getText().toString());
                                }
                                else
                                {
                                    UpdateMyInfoOnFirebase("none");
                                }
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


    private void UpdateMyInfoOnFirebase(String status)
    {
        if (!attachedImagePath.equals("none"))
        {
            LoggedInUser.setPhotoPath(attachedImagePath);
        }

        LoggedInUser.setStatus(status);

        LoggedInUser.saveChanges(this);

        MyProgressDialogManager.hideProgressDialog();

    }


}