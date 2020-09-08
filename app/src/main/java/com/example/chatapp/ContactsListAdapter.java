package com.example.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactsListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<ContactItem> contactItems;

    public ContactsListAdapter(Context context, ArrayList<ContactItem> contactItems)
    {
        this.context = context;
        this.contactItems = contactItems;
    }

    @Override
    public int getCount() {
        return contactItems.size();
    }

    @Override
    public Object getItem(int i) {
        return contactItems.get(i);
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

        ImageView contactPicImageView = view.findViewById(R.id.contact_pic);

        TextView contactNameTextView = view.findViewById(R.id.contact_name);

        TextView contactLastMsgTextView = view.findViewById(R.id.contact_last_msg);

        contactNameTextView.setText(contactItem.getName());
        contactLastMsgTextView.setText("Hello, iam " + contactItem.getName());

        //int imageResource = contactItem.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;

        int imageResource= contactItem.getImagePath();

        contactPicImageView.setImageResource(imageResource);

        // TODO: onClick image view: open the image in a fragment

        return view;
    }
}
