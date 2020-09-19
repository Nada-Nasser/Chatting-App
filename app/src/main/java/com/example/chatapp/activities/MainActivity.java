package com.example.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

import com.example.chatapp.R;
import com.example.chatapp.chattingroom.ChatRoom;
import com.example.chatapp.contactsmanager.ContactItem;
import com.example.chatapp.contactsmanager.ContactsListAdapter;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.example.chatapp.messagesservices.MessagesListenerService;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSIONS = 111;
    static final int PICK_CONTACT_REQUEST_CODE = 1;

    ArrayList<ContactItem> myContactsList;
    ContactsListAdapter contactsListAdapter;
    ListView myContactsListView;

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
    }


    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, MessagesListenerService.class));
    }

    private void OpenChatRoom(ContactItem contactItem)
    {
        Intent chattingRoomIntent = new Intent(getApplicationContext() , ChatRoom.class);

        chattingRoomIntent.putExtra("userID",contactItem.getUserID());
        chattingRoomIntent.putExtra("name",contactItem.getName());
        chattingRoomIntent.putExtra("phoneNumber",contactItem.getPhoneNumber());
        chattingRoomIntent.putExtra("status",contactItem.getStatus());
        chattingRoomIntent.putExtra("isActive",contactItem.getIsActive());

        chattingRoomIntent.putExtra("gender",contactItem.getGender());
        chattingRoomIntent.putExtra("imagePath",contactItem.getImagePath());

        startActivity(chattingRoomIntent);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LoggedInUser.beOfflineOnFirebase();
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
                    Toast.makeText( this,"You can not use the app correctly without this permission" , Toast.LENGTH_SHORT)
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
                ArrayList<String> NumbersFromFirebase = new ArrayList<>();

                myContactsList.clear();
                try {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        // key = phoneNumber, value = FirebaseChattingMessage
                        String Number = childSnapshot.getKey();

                        Log.i("Numbers" , Number);

                        if (Number != null) {
                            NumbersFromFirebase.add(Number);
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
                for (String keyNumber : NumbersFromFirebase)
                {
                    boolean flag = false;
                    for (PairNumberName cs : listAllPhoneContacts)
                    {
                        if (cs.getPhoneNumber().length() > 0)
                            // we use contains instead of equals because phone numbers may have some extra signs like (+02 )
                            // in case the phone number is one of my contacts.
                            if (keyNumber.contains(cs.getPhoneNumber()))
                            {
                                ContactItem c = new ContactItem("unKnown",cs.name,keyNumber,"unKnown",false,1,"none");
                                if (c!=null)
                                {
                                    flag = true;
                                    c.setName(cs.name);
                                    myContactsList.add(c);
                                    startContactInfoListener(c); // to get the contacts info
                           //         Toast.makeText(getApplicationContext(), "SHOW : " + cs.getPhoneNumber() + ":" + cs.getName(), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                    }
                    if (!flag)
                    {
                        ContactItem c =  new ContactItem("unKnown",keyNumber,keyNumber,"unKnown",false,1,"-1");
                        if( c != null) {
                            c.setName(c.getPhoneNumber());
                            startContactInfoListener(c);
                            myContactsList.add(c);
                        }
                    }
                }

                contactsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startContactInfoListener(@NonNull ContactItem c)
    {
        final String name = c.getName();
        FirebaseDatabase.getInstance().getReference().child("users").child(c.getPhoneNumber()).child("userInfo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        ContactItem contactItem  = snapshot.getValue(ContactItem.class);
                        contactItem.setName(name);
                        updateContactListInfo(contactItem,contactItem.getPhoneNumber());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateContactListInfo(ContactItem newContactItem, String phoneNumber)
    {
        for(int i = 0 ; i < myContactsList.size();i++)
        {
            if (myContactsList.get(i).getPhoneNumber().equalsIgnoreCase(phoneNumber))
            {
                myContactsList.set(i,newContactItem);
                contactsListAdapter.notifyDataSetChanged();
            }
        }
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

            listAllPhoneContacts.add(new PairNumberName(name, FormatPhoneNumber(phoneNumber)));
        }
        cursor.close();
        return listAllPhoneContacts;
    }

    ContactItem tempContact;
    private ContactItem getContactInfo(final String number)
    {
        tempContact = new ContactItem();

        FirebaseDatabase.getInstance().getReference().child("users").child(number).child("userInfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ContactItem contactItem  = snapshot.getValue(ContactItem.class);

                Toast.makeText(getApplicationContext(),contactItem.toString() , Toast.LENGTH_LONG).show();

                copyContactItemToTemp(contactItem);
                Log.i("CONTACTS : " , contactItem.toString() + " ADDAD at getContactInfo");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return tempContact;
    }

    private void copyContactItemToTemp(@NonNull ContactItem contactItem) {
        tempContact = new ContactItem(contactItem.getUserID(),contactItem.getName(),
                contactItem.getPhoneNumber(),contactItem.getStatus(),contactItem.getIsActive(),contactItem.getGender(),contactItem.getImagePath());
    }

    private void checkUserDate()
    {
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            Intent loginIntent = new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        else // There is a logged in user
        {
            MyProgressDialogManager.showProgressDialog(this);
            LoggedInUser.loadData(this);
            MyProgressDialogManager.hideProgressDialog();
        }
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
                PickContact();
                return true;
            case R.id.app_bar_setting:
                openSettingActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettingActivity()
    {
        Intent settingIntent = new Intent(this , PersonalSetting.class);
        startActivity(settingIntent);
    }

    // pick phone number
    void PickContact()
    {
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_ASK_CONTACTS_PERMISSIONS);
                return ;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST_CODE);
    }

    // Declare
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode)
        {
            case (PICK_CONTACT_REQUEST_CODE) :
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);

                    if (c.moveToFirst())
                    {
                        String id       = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        String cNumber="No number";
                        if (hasPhone.equalsIgnoreCase("1"))
                        {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);

                            phones.moveToFirst();
                            cNumber = FormatPhoneNumber (phones.getString(phones.getColumnIndex("data1")));
                            System.out.println("number is:"+cNumber);
                        }

                        // Update firebase
                        if(!cNumber.equalsIgnoreCase("No number"))
                        {
                            ContactItem contact = getContactInfo(cNumber);
                            OpenChattingRoomWithContactIfExists(cNumber);

/*
                            if(contact != null)
                            {
                                contact.setName(name);
                                OpenChatRoom(contact);
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"This Contact has no account" , Toast.LENGTH_LONG).show();
                            }*/
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"This Contact has no account" , Toast.LENGTH_LONG).show();
                        }
                    }
                    c.close();
                }
                break;
        }
    }

    private void OpenChattingRoomWithContactIfExists(String cNumber)
    {
        MyProgressDialogManager.showProgressDialog(this);
        FirebaseDatabase.getInstance().getReference().child("users").child(cNumber).child("userInfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        ContactItem contactItem  = snapshot.getValue(ContactItem.class);

                        OpenChatRoom(contactItem);
                        MyProgressDialogManager.hideProgressDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(),"This Contact has no account" , Toast.LENGTH_LONG).show();
                    }

                });
    }

    static class PairNumberName{
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
}