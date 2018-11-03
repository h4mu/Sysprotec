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
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;
import static io.github.h4mu.sysprotec.AppsHistoryActivity.COLUMN_PACKAGE;

public class InstallBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (sharedPref.getBoolean("locked", false) && !sharedPref.getBoolean("installDisabled", false)) {
            PendingIntent resultPendingIntent;
            Set<String> packages = sharedPref.getStringSet("packages", new HashSet<String>());
            Uri intentData = intent.getData();
            if (intentData != null && intentData.getSchemeSpecificPart() != null && !packages.contains(intentData.getSchemeSpecificPart())) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName name = new ComponentName(context, AdminReceiver.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && devicePolicyManager.isAdminActive(name)) {
                    uninstallPackage(context, intentData.getSchemeSpecificPart());
//                    devicePolicyManager.setApplicationHidden(name, intentData.getSchemeSpecificPart(), true);
                    editor.putString("history", sharedPref.getString("history", "") + ";" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "|" + intentData.getSchemeSpecificPart());
                    editor.apply();

                    Intent AppDetailsIntent = new Intent(context, AppDetailActivity.class);
                    AppDetailsIntent.putExtra(COLUMN_PACKAGE, intentData.getSchemeSpecificPart());
                    resultPendingIntent = PendingIntent.getActivity(context, 0, AppDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    Intent resultIntent = new Intent(Intent.ACTION_DELETE);
                    resultIntent.setData(intentData);
                    resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }

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

    private boolean uninstallPackage(Context context, String packageName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PackageManager packageManger = context.getPackageManager();
            PackageInstaller packageInstaller = packageManger.getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(packageName);
            int sessionId = 0;
            try {
                sessionId = packageInstaller.createSession(params);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            packageInstaller.uninstall(packageName, PendingIntent.getBroadcast(context, sessionId, new Intent(Intent.ACTION_MAIN), 0).getIntentSender());
            return true;
        }
        return false;
    }
}
