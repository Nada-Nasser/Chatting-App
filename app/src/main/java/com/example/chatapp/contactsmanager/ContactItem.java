package com.example.chatapp.contactsmanager;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ContactItem
{
    private String userID;
    private String name;
    private String phoneNumber;
    private String status;
    private Boolean isActive;
    //private Date lastOnlineDate;
    private int gender;
    private String imagePath;

    public ContactItem() {
    }

    public ContactItem(String userId, String name, String phoneNumber, String status,
                       boolean isActive,int gender, String imgPath) {
        this.userID = userId;
        this.name = name;
        this.phoneNumber = FormatPhoneNumber(phoneNumber);
        this.status = status;
        this.isActive = isActive;
      //  this.lastOnlineDate = date;
        this.gender = gender;
        this.imagePath = imgPath;
    }

    // use it when you need to write msg object in a database
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userID", userID);
        result.put("name", name);
        result.put("status", status);
        result.put("phoneNumber", phoneNumber);
        result.put("isActive", isActive);
        //result.put("lastOnlineDate", lastOnlineDate);
        result.put("gender", gender);
        result.put("imagePath", imagePath);


        return result;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ContactItem(String userID, String name, String phoneNumber, String status, Boolean isActive, int gender) {
        this.userID = userID;
        this.name = name;
        this.phoneNumber = FormatPhoneNumber(phoneNumber);
        this.status = status;
        this.isActive = isActive;
       // this.lastOnlineDate = lastOnlineDate;
        this.gender = gender;

        imagePath = "none";
    }


    @Override
    public String toString() {
        return "ContactItem{" +
                "userID='" + userID + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                ", isActive=" + isActive +
                ", gender=" + gender +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = FormatPhoneNumber(phoneNumber);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
/*
    public Date getLastOnlineDate() {
        return lastOnlineDate;
    }

    public void setLastOnlineDate(Date lastOnlineDate) {
        this.lastOnlineDate = lastOnlineDate;
    }
*/
    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
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


}
