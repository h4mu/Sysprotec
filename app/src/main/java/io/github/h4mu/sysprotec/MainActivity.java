package io.github.h4mu.sysprotec;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        ((ToggleButton)findViewById(R.id.toggleButton)).setChecked(sharedPref.getBoolean("locked", false));
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
        editor.commit();
    }
}
