package com.example.chatapp.globalinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatapp.ContactItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoggedInUser
{

    private static ContactItem myContactInfo;

    /*
    private static String phoneNumber;
    private static String name;
    private static String photoPath;
    private static String userID;
    private static int gender;
    private static String status;*/

    public static void saveUserDate(Context context, String phoneNumber, String name, String photoPath, String userID , int gender
                , String status)
    {
        //(String userId, String name, String phoneNumber, String status,
        //                       boolean isActive,int gender, String imgPath)

        myContactInfo = new ContactItem(userID , name , phoneNumber , status,true,gender,photoPath);

        /*
        LoggedInUser.phoneNumber = FormatPhoneNumber(phoneNumber);
        LoggedInUser.name = name;
        LoggedInUser.photoPath = photoPath;
        LoggedInUser.userID = userID;
        LoggedInUser.gender = gender;
        LoggedInUser.status = status;*/

        commitDate(context);
    }

    public static void saveChanges(Context context)
    {
        commitDate(context);
    }

    private static void commitDate(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("chattingAppLoggedInUser" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phoneNumber" , myContactInfo.getPhoneNumber());
        editor.putString("name" , myContactInfo.getName());
        editor.putString("photoPath" , myContactInfo.getImagePath());
        editor.putString("userID" , myContactInfo.getUserID());
        editor.putInt("gender" , myContactInfo.getGender());
        editor.putString("status" , myContactInfo.getStatus());

        updateMyFirebaseInfo(context);

        editor.commit();
    }

    public static void loadData(Context context)
    {
        if (myContactInfo == null)
        {
            myContactInfo = new ContactItem();
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("chattingAppLoggedInUser" , Context.MODE_PRIVATE);

        myContactInfo.setPhoneNumber(sharedPreferences.getString("phoneNumber" , "none"));
        myContactInfo.setName(sharedPreferences.getString("name" , "none"));
        myContactInfo.setImagePath(sharedPreferences.getString("photoPath" , "none"));
        myContactInfo.setUserID(sharedPreferences.getString("userID" , "none"));
        myContactInfo.setGender(sharedPreferences.getInt("gender" , 1));
        myContactInfo.setStatus(sharedPreferences.getString("status" , "none"));
        myContactInfo.setIsActive(true);

        beOnlineOnFirebase();

        /*
        LoggedInUser.photoPath = sharedPreferences.getString("photoPath" , "none");
        LoggedInUser.userID = sharedPreferences.getString("userID" , "none");
        LoggedInUser.gender = sharedPreferences.getInt("gender" , 1);

        LoggedInUser.status = sharedPreferences.getString("status" , "none");*/
    }

    public static void beOfflineOnFirebase() {
        String path = "/users/"+LoggedInUser.getPhoneNumber() + "/userInfo/isActive";

        FirebaseDatabase.getInstance().getReference(path).setValue(false);
    }


    public static void beOnlineOnFirebase() {
        String path = "/users/"+LoggedInUser.getPhoneNumber() + "/userInfo/isActive";

        FirebaseDatabase.getInstance().getReference(path).setValue(true);
    }

    public static void setGender(int gender) {
        myContactInfo.setGender(gender);
    }

    public static String getStatus() {
        return myContactInfo.getStatus();
    }

    public static void setStatus(String status) {
        myContactInfo.setStatus(status);
    }

    //format phone number
    @NonNull
    public static String FormatPhoneNumber(String Oldnmber)
    {
        try{
            String numberOnly= Oldnmber.replaceAll("[^0-9]", "");
            if(Oldnmber.charAt(0)=='+') numberOnly="+" +numberOnly ;
            if (numberOnly.length()>=10)
                numberOnly=numberOnly.substring(numberOnly.length()-10);
            return(numberOnly);
        }
        catch (Exception ex){
            return(" ");
        }
    }

    public static String getPhoneNumber() {
        return myContactInfo.getPhoneNumber();
    }

    public static void setPhoneNumber(String phoneNumber) {
        myContactInfo.setPhoneNumber(FormatPhoneNumber(phoneNumber));
    }

    public static String getName() {
        return myContactInfo.getName();
    }

    public static void setName(String name) {
        myContactInfo.setName(name);
    }

    public static String getPhotoPath() {
        return myContactInfo.getImagePath();
    }

    public static void setPhotoPath(String photoPath) {
        myContactInfo.setImagePath(photoPath);
    }

    public static String getUserID() {
        return myContactInfo.getUserID();
    }

    public static void setUserID(String userID) {
        myContactInfo.setUserID(userID);
    }

    public static int getGender() {
        return myContactInfo.getGender();
    }

    private static void updateMyFirebaseInfo(final Context context)
    {
        Map<String, Object> myRecord = myContactInfo.toMap();
        String path = "/users/"+LoggedInUser.getPhoneNumber() + "/userInfo/";

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(path , myRecord);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"Done", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

}
