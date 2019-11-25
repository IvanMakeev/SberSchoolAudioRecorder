package com.example.audiorecorder.presentation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiorecorder.R;
import com.example.audiorecorder.service.RecorderService;
import com.example.audiorecorder.service.PlayerService;
import com.example.audiorecorder.utils.CreaterUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IClickListener, IFilesChangeListener {

    public final static String BROADCAST_ACTION = "com.example.audiorecorder";
    public static final String PATH_RECORD = "pathRecord";

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 101;
    private RecyclerView mRecyclerView;
    private File[] mFiles;
    private AudioFileAdapter mAudioFileAdapter;
    private IntentFilter mBroadcastIntentFilter;
    private Intent mPlayerServiceIntent;
    private UpdateRecyclerBroadcastReceiver mBroadcastReceiver;
    private Messenger mServiceMessenger;

    private boolean mIsServiceBound;
    private int mPositionFile;
    private TextView mRecordName;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();

        findViewById(R.id.record_button).setOnClickListener(this);
        findViewById(R.id.play_recording_button).setOnClickListener(this);
        findViewById(R.id.stop_recording_button).setOnClickListener(this);
        findViewById(R.id.previous_recording_button).setOnClickListener(this);
        findViewById(R.id.next_recording_button).setOnClickListener(this);
        mRecordName = findViewById(R.id.record_name_text_view);
        mRecyclerView = findViewById(R.id.recycler);
        mAudioFileAdapter = new AudioFileAdapter(mFiles, this);
        mRecyclerView.setAdapter(mAudioFileAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mBroadcastIntentFilter = new IntentFilter(BROADCAST_ACTION);
        mBroadcastReceiver = new UpdateRecyclerBroadcastReceiver(this);

        mPlayerServiceIntent = new Intent(this, PlayerService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
        bindService(mPlayerServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        if (mIsServiceBound) {
            unbindService(mServiceConnection);
            mIsServiceBound = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_button: {
                Intent startIntent = new Intent(MainActivity.this, RecorderService.class);
                startService(startIntent);
                break;
            }
            case R.id.play_recording_button: {
                String fileRecordPath = getFileRecordPath(mPositionFile);
                if (isFileRecordPathNotExist(fileRecordPath)) return;
                mRecordName.setText(getSimpleRecordName(fileRecordPath));
                sendPlayMessage(fileRecordPath);
                break;
            }
            case R.id.stop_recording_button: {
                Message message = Message.obtain(null, PlayerService.MSG_STOP_PLAYER);
                try {
                    mServiceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.previous_recording_button: {
                String fileRecordPath = getFileRecordPath(--mPositionFile);
                if (isFileRecordPathNotExist(fileRecordPath)) return;
                mRecordName.setText(getSimpleRecordName(fileRecordPath));
                sendPlayMessage(fileRecordPath);
                break;
            }
            case R.id.next_recording_button: {
                String fileRecordPath = getFileRecordPath(++mPositionFile);
                if (isFileRecordPathNotExist(fileRecordPath)) return;
                mRecordName.setText(getSimpleRecordName(fileRecordPath));
                sendPlayMessage(fileRecordPath);
                break;
            }
        }
    }

    @Override
    public void onClick(int positionFile) {
        mPositionFile = positionFile;
    }

    @Override
    public void onChange() {
        int permissionReadStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionReadStatus == PackageManager.PERMISSION_GRANTED) {
            if (isExternalStorageReadable()) {
                mFiles = new File(Environment.getExternalStorageDirectory(), CreaterUtils.AUDIO_RECORD).listFiles();
                mAudioFileAdapter.updateAdapter(mFiles);
            } else {
                Toast.makeText(this, getString(R.string.unmount), Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (isExternalStorageReadable()) {
                        mFiles = new File(Environment.getExternalStorageDirectory(), CreaterUtils.AUDIO_RECORD).listFiles();
                    } else {
                        Toast.makeText(this, getString(R.string.unmount), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_permission), Toast.LENGTH_SHORT).show();
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initPermission() {
        int permissionWriteStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionReadStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionRecordStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        Log.d("TAG", "initPermission: " + permissionWriteStatus + "  " + permissionReadStatus + "  " + permissionRecordStatus);

        if (permissionWriteStatus == PackageManager.PERMISSION_GRANTED) {
            if (isExternalStorageReadable()) {
                mFiles = new File(Environment.getExternalStorageDirectory(), CreaterUtils.AUDIO_RECORD).listFiles();
            } else {
                Toast.makeText(this, getString(R.string.unmount), Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private void sendPlayMessage(String fileRecordPath) {
        if (mIsServiceBound) {
            Message message = Message.obtain(null, PlayerService.MSG_START_PLAYER);
            Bundle bundle = new Bundle();
            bundle.putString(PATH_RECORD, fileRecordPath);
            message.setData(bundle);
            try {
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileRecordPath(int positionFile) {
        if (positionFile < 0) {
            return "";
        }

        if (mFiles != null && mFiles.length <= positionFile) {
            Toast.makeText(this, "Дальше записей нет!", Toast.LENGTH_SHORT).show();
            return "";
        }
        if (mFiles != null && mFiles.length > 0) {
            return mFiles[positionFile].getAbsolutePath();
        }
        return "";
    }

    private boolean isFileRecordPathNotExist(String fileRecordPath) {
        return fileRecordPath == null || fileRecordPath.equals("");
    }

    private String getSimpleRecordName(String fileRecordPath) {
        String[] pathArray = fileRecordPath.split("/");
        return pathArray[pathArray.length - 1];
    }
}
