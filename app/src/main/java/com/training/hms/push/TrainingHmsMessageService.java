package com.training.hms.push;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public class TrainingHmsMessageService extends HmsMessageService {
    private static final String TAG = "--TrainingPush--";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        onSendToken(token);
    }

    @Override
    public void onNewToken(String token, Bundle bundle) {
        super.onNewToken(token, bundle);
        onSendToken(token);
    }

    private void onSendToken(String token) {
        Log.i(TAG, "receive token:" + token);
        Intent intent = new Intent("com.training.hms.push.onNewToken");
        intent.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
