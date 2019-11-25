package com.example.audiorecorder.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreaterUtils {

    public static final String AUDIO_RECORD = "AudioRecord";
    private File mFileDIr;

    public CreaterUtils() {
        createDir();
    }

    private void createDir() {
        if (isExternalStorageReadable()) {
            mFileDIr = new File(Environment.getExternalStorageDirectory(), AUDIO_RECORD);
            if (!mFileDIr.exists()) {
                mFileDIr.mkdirs();
            }
        }
    }

    public String createFile() {
        File file = new File(mFileDIr, createFileName());
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    private String createFileName() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.getDefault());
        String time = dateFormat.format(Calendar.getInstance().getTime());
        return time + ".aac";
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
