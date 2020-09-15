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
    public void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    // start = true -> means that the mediaPlayer will start playing the audio
    // else -> means the mediaPlayer will stop the audio
    public void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying()
    {
        player = new MediaPlayer();

        try {
            player.setDataSource(fileName);
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

    private void startRecording()
    {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
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
        fileName += "/audiorecordtest.3gp";
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
