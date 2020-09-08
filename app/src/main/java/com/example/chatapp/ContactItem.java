package com.example.chatapp;

import com.example.chatapp.globalinfo.Gender;

import java.util.Date;

public class ContactItem
{
    private int userID;
    private String name;
    private String phoneNumber;
    private String status;
    private Boolean isActive;
    private Date lastOnlineDate;
    private int gender;
    private int imagePath; // TODO: int for testing, change it to string to be a firebase storage path.

    public int getImagePath() {
        return imagePath;
    }

    public void setImagePath(int imagePath) {
        this.imagePath = imagePath;
    }

    public ContactItem(int userID, String name, String phoneNumber, String status, Boolean isActive, Date lastOnlineDate, int gender) {
        this.userID = userID;
        this.name = name;
        this.phoneNumber = FormatPhoneNumber(phoneNumber);
        this.status = status;
        this.isActive = isActive;
        this.lastOnlineDate = lastOnlineDate;
        this.gender = gender;

        // TODO: delete this...
        imagePath = getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactItem)) return false;

        ContactItem that = (ContactItem) o;

        if (getUserID() != that.getUserID()) return false;
        if (getGender() != that.getGender()) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        if (getPhoneNumber() != null ? !getPhoneNumber().equals(that.getPhoneNumber()) : that.getPhoneNumber() != null)
            return false;
        if (getStatus() != null ? !getStatus().equals(that.getStatus()) : that.getStatus() != null)
            return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null)
            return false;
        return getLastOnlineDate() != null ? getLastOnlineDate().equals(that.getLastOnlineDate()) : that.getLastOnlineDate() == null;
    }


    //TODO change this to add image path
    @Override
    public int hashCode() {
        int result = getUserID();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getPhoneNumber() != null ? getPhoneNumber().hashCode() : 0);
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (getLastOnlineDate() != null ? getLastOnlineDate().hashCode() : 0);
        result = 31 * result + getGender();
        return result;
    }

    @Override
    public String toString() {
        return "ContactItem{" +
                "userID=" + userID +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                ", isActive=" + isActive +
                ", lastOnlineDate=" + lastOnlineDate +
                ", gender=" + gender +
                '}';
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Date getLastOnlineDate() {
        return lastOnlineDate;
    }

    public void setLastOnlineDate(Date lastOnlineDate) {
        this.lastOnlineDate = lastOnlineDate;
    }

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
