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

import com.example.chatapp.PairNumberName;
import com.example.chatapp.R;
import com.example.chatapp.chattingroom.ChatRoomManager;
import com.example.chatapp.contactsmanager.ContactItem;
import com.example.chatapp.contactsmanager.ContactsListAdapter;
import com.example.chatapp.globalinfo.GlobalOperations;
import com.example.chatapp.globalinfo.LoggedInUser;
import com.example.chatapp.messagesservices.MessagesListenerService;
import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSIONS = 111;
    static final int PICK_CONTACT_REQUEST_CODE = 1;
    ArrayList<ContactItem> myContactsList;
    ContactsListAdapter contactsListAdapter;
    ListView myContactsListView;
    HashMap<String , Integer> notifyCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkUserDate();
        notifyCount = new HashMap<>();

        myContactsListView = findViewById(R.id.contacts_list);

        myContactsList = new ArrayList<>();
        contactsListAdapter = new ContactsListAdapter(this,myContactsList);

        myContactsListView.setAdapter(contactsListAdapter);

        myContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatRoomManager.OpenChatRoom(getApplicationContext(), myContactsList.get(i));
            }
        });

        try {
            CheckContactsPermissions(); // add contacts to chatting list
            ListenNotifyCount();
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

            listAllPhoneContacts.add(new PairNumberName(name, GlobalOperations.FormatPhoneNumber(phoneNumber)));
        }
        cursor.close();
        return listAllPhoneContacts;
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
                            cNumber = GlobalOperations.FormatPhoneNumber(phones.getString(phones.getColumnIndex("data1")));
                            System.out.println("number is:" + cNumber);
                        }

                        // Update firebase
                        if(!cNumber.equalsIgnoreCase("No number"))
                        {
                            ChatRoomManager.OpenChattingRoomWithContactIfExists(this, cNumber);

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

    private void ListenNotifyCount()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(LoggedInUser.getPhoneNumber()).child("notify-count")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        try {
                            for (DataSnapshot childSnapshot : snapshot.getChildren())
                            {
                                String phoneNumber = childSnapshot.getKey();
                                long count = (long) childSnapshot.getValue();

                                contactsListAdapter.receiveMessage(phoneNumber, (int) count);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}