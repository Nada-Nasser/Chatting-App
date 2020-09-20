package com.example.chatapp.chattingroom.messages;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessagesListAdapter extends BaseAdapter
{
    Context context;
    ArrayList<ChattingMessage> messages;
    Activity activity;
    AudioMessagesPlayer audioMessagesPlayer;

    public MessagesListAdapter(Context context, ArrayList<ChattingMessage> messages)
    {
        this.activity = (Activity) context;
        this.context = context;
        this.messages = messages;
        audioMessagesPlayer = new AudioMessagesPlayer(context);
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
            LinearLayout msgLayout = view.findViewById(R.id.textMsgLayout);
            LinearLayout msgContentLayout = view.findViewById(R.id.msgContentLayout);
            ImageView msgPhoto = view.findViewById(R.id.msgPhoto);
            TextView msgText = view.findViewById(R.id.msgText);

            msgText.setText(msg.getText());

            if (msg.sentByMe) {
                msgLayout.setGravity(Gravity.RIGHT);
                msgContentLayout.setBackgroundResource(R.drawable.background_lightblue_chattingmsg);
            } else {
                msgLayout.setGravity(Gravity.LEFT);
                msgContentLayout.setBackgroundResource(R.drawable.background_lightyellow_chattingmsg);
            }

            if (msg.getPhotoPath().equals("none"))
            {
                msgPhoto.setVisibility(View.GONE);
            }
            else if (msg.getPhotoPath().equals("loading"))
            {
                msgPhoto.setVisibility(View.VISIBLE);
                msgPhoto.setImageResource(R.drawable.loading_icon);
                return view;
            }
            else {
                msgPhoto.setVisibility(View.VISIBLE);
                msgPhoto.setImageResource(R.drawable.loading_icon);
                updateMessageImageView(msgPhoto, msg.getPhotoPath());
            }


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

            final ImageView audioIcon = view.findViewById(R.id.audioIcon);
            final SeekBar msgSeekBar = view.findViewById(R.id.msgSeekBar);
            final TextView msgDuration = view.findViewById(R.id.msgDuration);

            final myThread thread = new myThread(msgSeekBar , msgDuration);


            final File file = downloadAudioFileFromFirebaseStorage(msg.getAttachingPath() ,audioIcon , msgSeekBar , msgDuration);

            audioIcon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try {
                        audioMessagesPlayer.onPlay(file.getAbsolutePath());
                        thread.start();
                        audioIcon.setImageResource(R.drawable.active_voice_recorder_icon);

                        audioMessagesPlayer.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                audioIcon.setImageResource(R.drawable.audio_msg_icon);
                            }
                        });

                    }catch (Exception e)
                    {
                        AudioMessagesPlayer.isPlaying = false;
                    }
                }
            });

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

    File downloadAudioFileFromFirebaseStorage(String audioPath , final ImageView audioIcon , final SeekBar msgSeekBar,
                                              final TextView msgDuration)
    {
        try {
            final File localFile = File.createTempFile("audio", "jpg");

            FirebaseStorage.getInstance().getReference(audioPath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                {
                    // Local temp file has been created

                    audioIcon.setImageResource(R.drawable.audio_msg_icon);

                    audioMessagesPlayer.prePlay(localFile.getAbsolutePath());

                    msgSeekBar.setMax(audioMessagesPlayer.getPlayer().getDuration());

                    msgDuration.setText(TimeFormatter(audioMessagesPlayer.getPlayer().getDuration()));

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

    class myThread extends Thread
    {
        SeekBar seekBar;
        TextView progressTextView;
        public myThread(SeekBar seekBar , TextView progressTextView)
        {
            this.progressTextView = progressTextView;
            this.seekBar = seekBar;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run() {
                        if (audioMessagesPlayer.getPlayer() != null) {
                            int progress = audioMessagesPlayer.getPlayer().getCurrentPosition();
                            seekBar.setProgress(progress);
                            progressTextView.setText(TimeFormatter(progress));

                            int max = audioMessagesPlayer.getPlayer().getDuration();

                            if(progress >= max)
                            {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                audioMessagesPlayer.stopAudioPlayer();
                                seekBar.setProgress(0);
                                progressTextView.setText(TimeFormatter(max));
                            }
                        }
                    }
                });
            }
        }
    }

    String TimeFormatter(long millis)
    {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String text = formatter.format(new Date(millis));

        return text;
    }

}
