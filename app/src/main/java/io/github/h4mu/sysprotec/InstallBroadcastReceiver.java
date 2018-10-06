package io.github.h4mu.sysprotec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;

public class InstallBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPref.getBoolean("installDisabled", false)) {
            Set<String> packages = sharedPref.getStringSet("packages", new HashSet<String>());
            Uri intentData = intent.getData();
            if (intentData != null && intentData.getSchemeSpecificPart() != null && !packages.contains(intentData.getSchemeSpecificPart())) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName name = new ComponentName(context, AdminReceiver.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && devicePolicyManager.isAdminActive(name)) {
                    devicePolicyManager.setApplicationHidden(name, intentData.getSchemeSpecificPart(), true);
                }

                Intent resultIntent = new Intent(Intent.ACTION_DELETE);
                resultIntent.setData(intentData);
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setCategory(Notification.CATEGORY_ERROR)
                        .setContentTitle(context.getString(R.string.appInstalled))
                        .setContentText(context.getString(R.string.untrustedAppFound, intentData.getSchemeSpecificPart()))
                        .setSmallIcon(android.R.drawable.ic_lock_lock)
                        .setAutoCancel(true)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setContentIntent(resultPendingIntent);
                NotificationManager mNotifyMgr =
                        (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(1, builder.build());
            }
        }
    }
}
