package com.example.audiorecorder.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import androidx.annotation.NonNull;

import com.example.audiorecorder.presentation.MainActivity;

public class PlayerService extends Service {

    public static final int MSG_START_PLAYER = 101;
    public static final int MSG_STOP_PLAYER = 102;

    private MediaPlayer mMediaPlayer;

    private Messenger mMessenger = new Messenger(new InternalHandler());

    class InternalHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START_PLAYER:

                    if (msg.getData() != null) {
                        Bundle bundle = msg.getData();
                        String fileRecordPath = bundle.getString(MainActivity.PATH_RECORD);
                        startPlay(fileRecordPath);
                    }
                    break;
                case MSG_STOP_PLAYER:
                    stopPlay();
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public void startPlay(String fileName) {
        try {
            releasePlayer();
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(fileName);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    private void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
