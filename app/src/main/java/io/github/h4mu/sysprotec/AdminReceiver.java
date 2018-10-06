package io.github.h4mu.sysprotec;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// To enable run "dpm set-device-owner io.github.h4mu.sysprotec/.AdminReceiver"
public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("AdminReceiver", "Device Owner Enabled");
    }
}
