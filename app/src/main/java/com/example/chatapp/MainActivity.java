package com.example.chatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.chatapp.chattingroom.ChatRoom;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSIONS = 111;
    ArrayList<ContactItem> myContactsList;
    ContactsListAdapter contactsListAdapter;
    ListView myContactsListView;

    ContactItem myInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkUserDate();

        myContactsListView = findViewById(R.id.contacts_list);

        myContactsList = new ArrayList<>();
        contactsListAdapter = new ContactsListAdapter(this,myContactsList);

        myContactsListView.setAdapter(contactsListAdapter);

        myContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OpenChatRoom(myContactsList.get(i));
            }
        });

        try {
            CheckContactsPermissions(); // add contacts to chatting list
        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
      //  addTestUsers();
    }

    private void OpenChatRoom(ContactItem contactItem)
    {
        // Toast.makeText(getApplicationContext(),contactItem.toString(),Toast.LENGTH_LONG).show();

        Intent chattingRoomIntent = new Intent(getApplicationContext() , ChatRoom.class);

        chattingRoomIntent.putExtra("userID",contactItem.getUserID());
        chattingRoomIntent.putExtra("name",contactItem.getName());
        chattingRoomIntent.putExtra("phoneNumber",contactItem.getPhoneNumber());
        chattingRoomIntent.putExtra("status",contactItem.getStatus());
        chattingRoomIntent.putExtra("isActive",contactItem.getIsActive());

        chattingRoomIntent.putExtra("lastOnlineDate",contactItem.getLastOnlineDate().getTime());

        chattingRoomIntent.putExtra("gender",contactItem.getGender());
        // TODO: send image path too)

        startActivity(chattingRoomIntent);

    }


    //access to Permissions
    private void CheckContactsPermissions()
    {
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_ASK_CONTACTS_PERMISSIONS);
                return ;
            }
        }

        LoadUserContacts();// init the contact list
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LoadUserContacts();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private void LoadUserContacts()
    {
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(LoggedInUser.getPhoneNumber()).child("chats")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Map<String, ContactItem> numberChattingMap = new HashMap<>();

                myContactsList.clear();
                try {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        // key = phoneNumber, value = FirebaseChattingMessage
                        String Number = childSnapshot.getKey();
                        Log.i("Numbers" , Number);

                        if (Number != null) {
                            ContactItem contact = getContactInfo(Number);
                           // numberChattingMap.put(Number, contact);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // get all contact to list
                ArrayList<PairNumberName> listAllPhoneContacts  = getAllPhoneContactList();

                // if the name is save chane his text
                for (String keyNumber : numberChattingMap.keySet())
                {
                    boolean flag = false;
                    for (PairNumberName cs : listAllPhoneContacts)
                    {
                        if (cs.getPhoneNumber().length() > 0)
                            // we use contains instead of equals because phone numbers may have some extra signs like (+02 )
                            if (keyNumber.contains(cs.getPhoneNumber()))  // in case the phone number is one of my contacts.
                            {
                                ContactItem c = (ContactItem) numberChattingMap.get(keyNumber);
                                if (c!=null)
                                {
                                    flag = true;
                                    c.setName(cs.name);
                                    myContactsList.add(c);
                                    Toast.makeText(getApplicationContext(), "SHOW : " + cs.getPhoneNumber() + ":" + cs.getName(), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                    }
                    if (!flag)
                    {
                        ContactItem c = (ContactItem) numberChattingMap.get(keyNumber);
                        if( c != null) {
                            c.setName(c.getPhoneNumber());
                            myContactsList.add(c);
                        }
                    }

                    contactsListAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    private ArrayList<PairNumberName> getAllPhoneContactList()
    {
        ArrayList<PairNumberName> listAllPhoneContacts = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        assert cursor != null;
        while (cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            listAllPhoneContacts.add(new PairNumberName(name,FormatPhoneNumber(phoneNumber)));

            if(!phoneNumber.equalsIgnoreCase("0"))
                Log.i("Numbers", "onDataChange: " + phoneNumber + " Formated : " + FormatPhoneNumber(phoneNumber));
        }
        cursor.close();
        return listAllPhoneContacts;
    }

    ContactItem tempContact;
    private ContactItem getContactInfo(String number)
    {
        DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
        Date dateobj = new Date();
        tempContact = (new ContactItem("0","test","0147258369","test",true,dateobj,1));

        FirebaseDatabase.getInstance().getReference().child("users").child(number).child("userInfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempContact = snapshot.getValue(ContactItem.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return tempContact;
    }


    private void addTestUsers()
    {
        DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
        Date dateobj = new Date();

        myContactsList.add(new ContactItem("0","test","0147258369","test",true,dateobj,1));
        myContactsList.add(new ContactItem("0","test2","test","test",true,dateobj,2));
        myContactsList.add(new ContactItem("0","test3","test","test",false,dateobj,1));
        myContactsList.add(new ContactItem("0","test4","test","test",true,dateobj,2));

        contactsListAdapter.notifyDataSetChanged();
    }


    private void checkUserDate()
    {
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            Intent loginIntent = new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        else
        {
            MyProgressDialogManager.showProgressDialog(this);
            LoggedInUser.loadData(this);
            loadMyContactInfo();
            MyProgressDialogManager.hideProgressDialog();
        }
    }

    private void loadMyContactInfo()
    {
        int gender = LoggedInUser.getGender();
        String imgPath = LoggedInUser.getPhotoPath();
        boolean isActive = true;
        long lastOnlineDate = 0 ; // // TODO = means NOW
        String name = LoggedInUser.getPhoneNumber();
        String phoneNumber = LoggedInUser.getPhoneNumber();
        String status = "Iam fine"; // TODO set a real value
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Date date = new Date();
        date.setTime(lastOnlineDate);

        myInfo  = new ContactItem(userId,name,phoneNumber,status,true,date,gender,imgPath);

        Map<String, Object> myRecord = myInfo.toMap();
        String path = "/users/"+LoggedInUser.getPhoneNumber() + "/userInfo/";

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(path , myRecord);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu , menu);

        // TODO: Searching menu item

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.add_contact:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class PairNumberName{
        String name;
        String phoneNumber;

        public PairNumberName(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
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
            this.phoneNumber = phoneNumber;
        }
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