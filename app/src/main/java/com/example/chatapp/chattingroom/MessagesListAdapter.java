package com.example.chatapp.chattingroom;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatapp.R;

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

            LinearLayout msgLayout = view.findViewById(R.id.textMsgLayout);
            LinearLayout msgContentLayout = view.findViewById(R.id.msgContentLayout);

            if (msg.sentByMe) {
                msgLayout.setGravity(Gravity.RIGHT);
                msgContentLayout.setBackgroundResource(R.drawable.background_lightblue_chattingmsg);
            } else {
                msgLayout.setGravity(Gravity.LEFT);
                msgContentLayout.setBackgroundResource(R.drawable.background_lightyellow_chattingmsg);
            }

            ImageView msgPhoto = view.findViewById(R.id.msgPhoto);

            //int img = msg.getPhotoPath();

            if(msg.getPhotoPath() != null) {
                if (Integer.parseInt(msg.getPhotoPath()) != -1)
                    msgPhoto.setImageResource(R.drawable.male_user); // TODO Load From Firebase storage and use picasso
                else
                    msgPhoto.setVisibility(View.GONE);
            }
            else
            {
                msgPhoto.setVisibility(View.GONE);
            }

            TextView msgText = view.findViewById(R.id.msgText);
            msgText.setText(msg.getText());

            return view;
        }
        else if(messages.get(i) instanceof AudioMessage)
        {
            view = inflater.inflate(R.layout.audio_message_list_item, null);
            return view;
        }
        else
            return null;
    }
}
