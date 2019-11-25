package com.example.audiorecorder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.audiorecorder.R;
import com.example.audiorecorder.presentation.MainActivity;
import com.example.audiorecorder.utils.CreaterUtils;

import java.io.IOException;

public class RecorderService extends Service {

    private static final String CUSTOM_ACTION_PLAY = "play";
    private static final String CUSTOM_ACTION_STOP = "stop";

    private static final String CHANNEL_ID = "CHANNEL_RECORDER";
    private static final int NOTIFICATION_ID = 1;

    private MediaRecorder mMediaRecorder;
    private CreaterUtils mCreaterUtils;
    private Intent mIntent;

    @Override
    public void onCreate() {
        mCreaterUtils = new CreaterUtils();
        mIntent = new Intent(MainActivity.BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(initRecordRemoteViews(R.string.start_record)));

        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            if (intent.getAction().equals(CUSTOM_ACTION_PLAY)) {
                Toast.makeText(this, "play", Toast.LENGTH_SHORT).show();
                updateNotification(initRecordRemoteViews(R.string.recording_in_progress));
                startRecord();
            } else if (intent.getAction().equals(CUSTOM_ACTION_STOP)) {
                Toast.makeText(this, "stop", Toast.LENGTH_SHORT).show();
                stopRecord();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRecorder();
    }

    private void startRecord() {
        try {
            releaseRecorder();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setOutputFile(mCreaterUtils.createFile());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
            stopSelf();
        }
    }

    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    private RemoteViews initRecordRemoteViews(@StringRes int string) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.audio_recorder_view);
        remoteViews.setTextViewText(R.id.audio_text_view, getResources().getString(string));
        remoteViews.setOnClickPendingIntent(R.id.play_button, createStartPendingIntent());
        remoteViews.setOnClickPendingIntent(R.id.stop_button, createStopPendingIntent());
        return remoteViews;
    }

//    private RemoteViews initRecordingRemoteViews() {
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.audio_recorder_view);
//        remoteViews.setTextViewText(R.id.audio_text_view, getResources().getString(R.string.recording_in_progress));
//        remoteViews.setOnClickPendingIntent(R.id.play_button, createStartPendingIntent());
//        remoteViews.setOnClickPendingIntent(R.id.stop_button, createStopPendingIntent());
//        return remoteViews;
//    }

    private void updateNotification(RemoteViews remoteViews) {
        Notification notification = createNotification(remoteViews);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification(RemoteViews remoteViews) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "channel name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("channel description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private PendingIntent createStartPendingIntent() {
        Intent startButtonIntent = new Intent(this, RecorderService.class);
        startButtonIntent.setAction(CUSTOM_ACTION_PLAY);
        return PendingIntent.getService(this, 0, startButtonIntent, 0);
    }

    private PendingIntent createStopPendingIntent() {
        Intent stopButtonIntent = new Intent(this, RecorderService.class);
        stopButtonIntent.setAction(CUSTOM_ACTION_STOP);
        return PendingIntent.getService(this, 0, stopButtonIntent, 0);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
