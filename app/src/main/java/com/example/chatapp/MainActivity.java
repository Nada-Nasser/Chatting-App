package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.JobIntentService;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatapp.ui.MyProgressDialogManager;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
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
                // TODO Open chat activity.
                Toast.makeText(getApplicationContext(),myContactsList.get(i).toString(),Toast.LENGTH_LONG).show();
                ContactItem contactItem = myContactsList.get(i);

                Intent chattingRoomIntent = new Intent(getApplicationContext() , ChatRoom.class);

                chattingRoomIntent.putExtra("name",contactItem.getName());
                chattingRoomIntent.putExtra("phoneNumber",contactItem.getPhoneNumber());
                chattingRoomIntent.putExtra("status",contactItem.getStatus());
                chattingRoomIntent.putExtra("isActive",contactItem.getActive());

                chattingRoomIntent.putExtra("lastOnlineDate",contactItem.getLastOnlineDate().getTime());

                chattingRoomIntent.putExtra("gender",contactItem.getGender());
                // TODO: send image path too)

                startActivity(chattingRoomIntent);
            }
        });

        addTestUsers();
    }

    private void addTestUsers()
    {
        DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
        Date dateobj = new Date();

        myContactsList.add(new ContactItem(0,"test","test","test",true,dateobj,1));
        myContactsList.add(new ContactItem(0,"test2","test","test",true,dateobj,2));
        myContactsList.add(new ContactItem(0,"test3","test","test",false,dateobj,1));
        myContactsList.add(new ContactItem(0,"test4","test","test",true,dateobj,2));

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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}