package com.example.chatapp.chattingroom;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioMessagesManager
{
    private static final String LOG_TAG = "AudioRecordTest";

    private static String fileName = null;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    // start = true -> means that the record will start recording
    // else -> means the record will stop recording
    public void onRecord(boolean start , String path)
    {
        String newPath = fileName + "/" + path + ".3gp";
        if (start) {
            startRecording(newPath);
        } else {
            stopRecording();
        }
    }

    // start = true -> means that the mediaPlayer will start playing the audio
    // else -> means the mediaPlayer will stop the audio
    public void onPlay(boolean start , String path)
    {
        String newPath = fileName + "/" + path + ".3gp";

        if (start) {
            startPlaying(newPath);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(String path)
    {
        player = new MediaPlayer();

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

    private void startRecording(String newPath)
    {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(newPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public AudioMessagesManager(Context context)
    {
        recorder = new MediaRecorder();
        onCreateAudioManager(context);
    }

    private void onCreateAudioManager(Context context)
    {
        fileName = context.getExternalCacheDir().getAbsolutePath();
       // fileName += "/audiorecordtest.3gp";
    }

    public void stopAudioManager()
    {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
