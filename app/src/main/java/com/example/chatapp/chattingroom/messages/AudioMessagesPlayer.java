package com.example.chatapp.chattingroom.messages;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioMessagesPlayer
{
    private static final String LOG_TAG = "AudioRecordTest";

    static Context context;

    private static String fileName = null;

    private MediaPlayer player = null;

    static boolean isPlaying = false;

    public MediaPlayer getPlayer() {
        return player;
    }

    public void prePlay(String path)
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
    //    fileName = context.getFilesDir().getAbsolutePath() + "/" + path + ".3gp";
        fileName = path;
        startPlaying(fileName);
        isPlaying = true;
    }

    public void onStop()
    {
        stopPlaying();
        isPlaying = false;
    }

    private void startPlaying(String path)
    {
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isPlaying = false;
            }
        });

        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    public AudioMessagesPlayer(Context context)
    {
        player = new MediaPlayer();
        AudioMessagesPlayer.context = context;

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isPlaying = false;
            }
        });
    }

    public String getLastPlayedAudioFilePath()
    {
        return fileName;
    }

    public void stopAudioPlayer()
    {
        if (player != null) {
            player.release();
            player = null;
            isPlaying = false;
        }
    }
}
