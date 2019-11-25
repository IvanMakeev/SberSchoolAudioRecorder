package com.example.audiorecorder.presentation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;


public class UpdateRecyclerBroadcastReceiver extends BroadcastReceiver {

    private WeakReference<IFilesChangeListener> mFilesChangeListener;

    public UpdateRecyclerBroadcastReceiver() { }

    public UpdateRecyclerBroadcastReceiver(IFilesChangeListener filesChangeListener) {
        mFilesChangeListener = new WeakReference<>(filesChangeListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(MainActivity.BROADCAST_ACTION)){
            if (mFilesChangeListener.get() != null){
                mFilesChangeListener.get().onChange();
            }
        }
    }
}
