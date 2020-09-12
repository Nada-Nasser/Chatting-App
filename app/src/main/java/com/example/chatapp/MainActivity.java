package com.example.chatapp;

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

import java.util.ArrayList;
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
/*
        Intent mIntent = new Intent(this, MessagesListenerJobIntentService.class);
        MessagesListenerJobIntentService.enqueueWork(this, mIntent);
*/
    //    startService(new Intent(this, MessagesListenerService.class));

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
        //addTestUsers();
    }


    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, MessagesListenerService.class));
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

        //chattingRoomIntent.putExtra("lastOnlineDate",contactItem.getLastOnlineDate().getTime());

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
                        // TODO: get his Info
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
                                ContactItem c = new ContactItem("unKnown",cs.name,keyNumber,"unKnown",false,1,"-1");
                                if (c!=null)
                                {
                                    flag = true;
                                    c.setName(cs.name);
                                    myContactsList.add(c);
                                    startContactInfoListener(c , myContactsList.size()-1);
                                    Toast.makeText(getApplicationContext(), "SHOW : " + cs.getPhoneNumber() + ":" + cs.getName(), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                    }
                    if (!flag)
                    {
                        ContactItem c =  new ContactItem("unKnown",keyNumber,keyNumber,"unKnown",false,1,"-1");
                        if( c != null) {
                            c.setName(c.getPhoneNumber());
                            startContactInfoListener(c , myContactsList.size()-1);
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

    private void startContactInfoListener(ContactItem c, int i)
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

            listAllPhoneContacts.add(new PairNumberName(name,FormatPhoneNumber(phoneNumber)));

            /*
            if(!phoneNumber.equalsIgnoreCase("0"))
                Log.i("Numbers", "onDataChange: " + phoneNumber + " Formated : " + FormatPhoneNumber(phoneNumber));
        */

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

    private void copyContactItemToTemp(ContactItem contactItem) {
        tempContact = new ContactItem(contactItem.getUserID(),contactItem.getName(),
                contactItem.getPhoneNumber(),contactItem.getStatus(),contactItem.getIsActive(),contactItem.getGender(),contactItem.getImagePath());
    }

    private void addTestUsers()
    {

        myContactsList.add(new ContactItem("0","test","0147258369","test",true,1));
        myContactsList.add(new ContactItem("0","test2","test","test",true,2));
        myContactsList.add(new ContactItem("0","test3","test","test",false,1));
        myContactsList.add(new ContactItem("0","test4","test","test",true,2));

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
            loadMyFirebaseInfo();
            MyProgressDialogManager.hideProgressDialog();
        }
    }

    private void loadMyFirebaseInfo()
    {
        int gender = LoggedInUser.getGender();
        String imgPath = LoggedInUser.getPhotoPath();

        String name = LoggedInUser.getPhoneNumber();
        String phoneNumber = LoggedInUser.getPhoneNumber();
        String status = "Iam fine"; // TODO set a real value
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        myInfo  = new ContactItem(userId,name,phoneNumber,status,true,gender,imgPath);

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
                PickContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    static final int PICK_CONTACT_REQUEST_CODE = 1;
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
                        if(cNumber.equalsIgnoreCase("No NNumber"))
                        {
                            ContactItem contact = getContactInfo(cNumber);
                            if(contact != null)
                            {
                                contact.setName(name);
                                OpenChatRoom(contact);
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"This Contact has no account" , Toast.LENGTH_LONG).show();
                            }
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