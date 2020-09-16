package com.example.chatapp.chattingroom;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class MessagesListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<ChattingMessage> messages;

    public MessagesListAdapter(Context context, ArrayList<ChattingMessage> messages)
    {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        if(messages.get(i) instanceof TextMessage)
        {
            view = inflater.inflate(R.layout.message_list_item, null);

            TextMessage msg = ((TextMessage) messages.get(i));

            ImageView msgPhoto = view.findViewById(R.id.msgPhoto);
            if (msg.getPhotoPath().equals("none"))
            {
                msgPhoto.setVisibility(View.GONE);
            }
            else {
                msgPhoto.setVisibility(View.VISIBLE);
                msgPhoto.setImageResource(R.drawable.loading_icon);
                updateMessageImageView(msgPhoto, msg.getPhotoPath());
            }

            LinearLayout msgLayout = view.findViewById(R.id.textMsgLayout);
            LinearLayout msgContentLayout = view.findViewById(R.id.msgContentLayout);

            if (msg.sentByMe) {
                msgLayout.setGravity(Gravity.RIGHT);
                msgContentLayout.setBackgroundResource(R.drawable.background_lightblue_chattingmsg);
            } else {
                msgLayout.setGravity(Gravity.LEFT);
                msgContentLayout.setBackgroundResource(R.drawable.background_lightyellow_chattingmsg);
            }

            TextView msgText = view.findViewById(R.id.msgText);
            msgText.setText(msg.getText());

            return view;
        }
        else if(messages.get(i) instanceof AudioMessage)
        {
            final AudioMessage msg = ((AudioMessage) messages.get(i));
            view = inflater.inflate(R.layout.audio_my_message_list_item, null);
            LinearLayout AudioMsgLayout = view.findViewById(R.id.AudioMsgLayout);
            LinearLayout AudioMsgLayoutContent = view.findViewById(R.id.AudioMsgLayoutContent);

            if (msg.sentByMe) {
                AudioMsgLayout.setGravity(Gravity.RIGHT);
                AudioMsgLayoutContent.setBackgroundResource(R.drawable.background_lightblue_chattingmsg);
            } else {
                AudioMsgLayout.setGravity(Gravity.LEFT);
                AudioMsgLayoutContent.setBackgroundResource(R.drawable.background_lightyellow_chattingmsg);
            }

            ImageView audioIcon = view.findViewById(R.id.audioIcon);

            audioIcon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                        downloadAudioFileFromFirebaseStorage(msg.getAttachingPath());

                }
            });

            SeekBar msgSeekBar = view.findViewById(R.id.msgSeekBar);
            TextView msgDuration = view.findViewById(R.id.msgDuration);



            return view;
        }
        else
            return null;
    }

    void updateMessageImageView(final ImageView imageView , String imagePath)
    {
        // download the image from firebase storage and load it in the image view using picasso lib.
        try
        {
            Picasso.get().setLoggingEnabled(true);

            FirebaseStorage.getInstance().getReference(imagePath).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl)
                        {
                            Picasso.get().load(downloadUrl).into(imageView);

                        }
                    });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    File downloadAudioFileFromFirebaseStorage(String audioPath)
    {
        try {
            final File localFile = File.createTempFile("audio", "jpg");

            FirebaseStorage.getInstance().getReference(audioPath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                {
                    // Local temp file has been created
                    AudioMessagesPlayer audioMessagesPlayer = new AudioMessagesPlayer(context);
                    audioMessagesPlayer.onPlay(localFile.getAbsolutePath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

            return localFile;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
