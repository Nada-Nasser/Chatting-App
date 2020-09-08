package com.example.chatapp.globalinfo;

import android.content.Context;
import android.content.SharedPreferences;

public class LoggedInUser
{
    private static String phoneNumber;
    private static String name;
    private static String photoPath;
    private static String userID;
    private static int gender;

    public static void saveUserDate(Context context, String phoneNumber, String name, String photoPath, String userID , int gender)
    {
        LoggedInUser.phoneNumber = FormatPhoneNumber(phoneNumber);
        LoggedInUser.name = name;
        LoggedInUser.photoPath = photoPath;
        LoggedInUser.userID = userID;
        LoggedInUser.gender = gender;

        commitDate(context);
    }

    private static void commitDate(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("chattingAppLoggedInUser" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phoneNumber" , LoggedInUser.phoneNumber);
        editor.putString("name" , LoggedInUser.name);
        editor.putString("photoPath" , LoggedInUser.photoPath);
        editor.putString("userID" , LoggedInUser.userID);
        editor.putInt("gender" , LoggedInUser.gender);

        editor.commit();
    }

    public static void loadData(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("chattingAppLoggedInUser" , Context.MODE_PRIVATE);

        LoggedInUser.phoneNumber = sharedPreferences.getString("phoneNumber" , "none");
        LoggedInUser.name = sharedPreferences.getString("name" , "none");
        LoggedInUser.photoPath = sharedPreferences.getString("photoPath" , "none");
        LoggedInUser.userID = sharedPreferences.getString("userID" , "none");
        LoggedInUser.gender = sharedPreferences.getInt("gender" , 1);
    }


    //format phone number
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
        return phoneNumber;
    }

    public static void setPhoneNumber(String phoneNumber) {
        LoggedInUser.phoneNumber = FormatPhoneNumber(phoneNumber);
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        LoggedInUser.name = name;
    }

    public static String getPhotoPath() {
        return photoPath;
    }

    public static void setPhotoPath(String photoPath) {
        LoggedInUser.photoPath = photoPath;
    }

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        LoggedInUser.userID = userID;
    }


    public static int getGender() {
        return gender;
    }
}
