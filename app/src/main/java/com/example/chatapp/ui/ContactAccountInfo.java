package com.example.chatapp.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.chatapp.R;
import com.example.chatapp.contactsmanager.ContactItem;
import com.example.chatapp.globalinfo.Gender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class ContactAccountInfo extends DialogFragment {

    View contactInfoView;

    ImageView userImageView;
    TextView statusTextView;

    ContactItem contactItem;

    public ContactAccountInfo(ContactItem contact)
    {
        contactItem = contact;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contactInfoView = inflater.inflate(R.layout.fragment_contact_account_info, container, false);

        userImageView = contactInfoView.findViewById(R.id.userImage);
        statusTextView = contactInfoView.findViewById(R.id.userStatus);

        updateUserImageView(userImageView , contactItem.getImagePath());

        statusTextView.setText(contactItem.getStatus());

        return contactInfoView;
    }

    void updateUserImageView(final ImageView imageView , String imagePath)
    {
        if(contactItem.getImagePath().equalsIgnoreCase("none")) //when user has no image
        {
            int imageResource = contactItem.getGender() == Gender.MALE? R.drawable.male_user : R.drawable.female_user;
            imageView.setImageResource(imageResource);
        }
        else {

            // download the image from firebase storage and load it in the image view using picasso lib.
            try {
                Picasso.get().setLoggingEnabled(true);

                FirebaseStorage.getInstance().getReference(imagePath).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                Picasso.get().load(downloadUrl).into(imageView);

                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}