package com.example.chatapp.contactsmanager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.chatapp.R;
import com.example.chatapp.globalinfo.Gender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<ContactItem> contactItems;
    HashMap<String , Integer> notifyCount;

    public ContactsListAdapter(Context context, ArrayList<ContactItem> contactItems)
    {
        this.context = context;
        this.contactItems = contactItems;
        this.notifyCount = new HashMap<>();
    }

    @Override
    public int getCount() {
        return contactItems.size();
    }

    @Override
    public Object getItem(int i) {
        return contactItems.get(i);
    }

    public void receiveMessage(String phoneNumber , int count)
    {
        notifyCount.put(phoneNumber , count);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        final ContactItem contactItem = contactItems.get(i);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        view = inflater.inflate( R.layout.contact_list_item, null );

        final ImageView contactPicImageView = view.findViewById(R.id.contact_pic);

        TextView contactNameTextView = view.findViewById(R.id.contact_name);

        TextView contactLastMsgTextView = view.findViewById(R.id.contact_last_msg);

        contactNameTextView.setText(contactItem.getName());
        contactLastMsgTextView.setText("Hello, iam " + contactItem.getName());

        if(contactItem.getImagePath().equalsIgnoreCase("none")) //when user has no image
        {
            int imageResource = contactItem.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
            contactPicImageView.setImageResource(imageResource);
        }
        else // download the image from firebase storage and load it in the image view using picasso lib.
        {
            try
            {
                Picasso.get().setLoggingEnabled(true);

                FirebaseStorage.getInstance().getReference(contactItem.getImagePath()).getDownloadUrl()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception)
                            {
                                int imageResource = contactItem.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
                                contactPicImageView.setImageResource(imageResource);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                Picasso.get().load(downloadUrl).into(contactPicImageView);
                                Log.i("TAG", "onSuccess: " + downloadUrl + " ----> " + downloadUrl.toString());
                            }
                        });

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        TextView new_msgBu = view.findViewById(R.id.new_msg);

        try {
            if (notifyCount.containsKey(contactItem.getPhoneNumber())) {
                int count = notifyCount.get(contactItem.getPhoneNumber());
                if (count > 0) {
                    new_msgBu.setText(String.valueOf(count));
                    new_msgBu.setVisibility(View.VISIBLE);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return view;
    }
}
