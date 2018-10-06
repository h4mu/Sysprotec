package io.github.h4mu.sysprotec;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ((ToggleButton) findViewById(R.id.toggleButton)).setChecked(sharedPref.getBoolean("locked", false));
        ToggleButton installDisableToggleButton = findViewById(R.id.installDisableToggleButton);
        installDisableToggleButton.setChecked(sharedPref.getBoolean("installDisabled", false));
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName name = new ComponentName(this, AdminReceiver.class);
        boolean isAdmin = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && devicePolicyManager.isAdminActive(name);
        installDisableToggleButton.setEnabled(isAdmin);
        findViewById(R.id.disableAdminButton).setEnabled(isAdmin);
    }

    public void toggleClicked(View view) {
        ToggleButton button = (ToggleButton) view;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (button.isChecked()) {
            Set<String> packages = new HashSet<>();
            List<ApplicationInfo> installedApplications = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo appInfo : installedApplications) {
                if (appInfo.enabled && (appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                    packages.add(appInfo.packageName);
                }
            }
            editor.putStringSet("packages", packages);
        }
        editor.putBoolean("locked", button.isChecked());
        editor.apply();
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName name = new ComponentName(this, AdminReceiver.class);
        findViewById(R.id.installDisableToggleButton).setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && devicePolicyManager.isAdminActive(name));
    }

    public void installDisableClicked(View view) {
        ToggleButton button = (ToggleButton) view;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("installDisabled", button.isChecked());
        editor.apply();
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName name = new ComponentName(this, AdminReceiver.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && devicePolicyManager.isAdminActive(name)) {
            if (button.isChecked()) {
                devicePolicyManager.addUserRestriction(name, UserManager.DISALLOW_INSTALL_APPS);
//            devicePolicyManager.setSecureSetting(name, Settings.Secure.INSTALL_NON_MARKET_APPS, "0");
            } else {
                devicePolicyManager.clearUserRestriction(name, UserManager.DISALLOW_INSTALL_APPS);
            }
        }
    }

    public void disableAdminClicked(View view) {
        view.setEnabled(false);
        ToggleButton installDisabledButton = findViewById(R.id.installDisableToggleButton);
        if (installDisabledButton.isChecked()) {
            installDisabledButton.setChecked(false);
            installDisableClicked(installDisabledButton);
        }
        installDisabledButton.setEnabled(false);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName name = new ComponentName(this, AdminReceiver.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && devicePolicyManager.isAdminActive(name)) {
            devicePolicyManager.clearDeviceOwnerApp(getPackageName());
        }
    }
}
