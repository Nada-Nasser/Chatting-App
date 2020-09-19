package com.example.chatapp.globalinfo;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GlobalOperations
{
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

    // in chattingroom
    public static Date getCurrentDate()
    {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        return c;
    }

    // in chattingroom
    public static String dateToStringFormatter(Date date)
    {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(date);

        return  formattedDate;
    }
}
