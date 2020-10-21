package com.example.chatapp.chattingroom;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioMessagesPlayer
{
    private static final String LOG_TAG = "AudioRecordTest";

    static Context context;

    private static MediaPlayer player = null;

    static boolean isPlaying = false;

    public AudioMessagesPlayer(Context context)
    {
        player = new MediaPlayer();
        this.context = context;
    }

    public void preparePlayer(String path)
    {
        player = new MediaPlayer();

        try {
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    // start = true -> means that the mediaPlayer will start playing the audio
    // else -> means the mediaPlayer will stop the audio
    public void onPlay(String path)
    {
        releaseAudioPlayer();

        player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();

            isPlaying = true;
            player.start();
            Log.e(LOG_TAG, "start() audio player");
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void releaseAudioPlayer()
    {
        if (player != null)
        {
            isPlaying = false;
            player.release();
            player = null;
            Log.e(LOG_TAG, "release() audio player");
        }
    }

    public MediaPlayer getPlayer() {
        return player;
    }
}
