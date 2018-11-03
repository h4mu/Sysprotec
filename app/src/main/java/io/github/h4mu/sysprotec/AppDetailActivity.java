package io.github.h4mu.sysprotec;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class AppDetailActivity extends AppCompatActivity {
    private ApplicationInfo applicationInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        String packageName = getIntent().getStringExtra(AppsHistoryActivity.COLUMN_PACKAGE);

        try {
            applicationInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            ((TextView) findViewById(R.id.nameText)).setText(packageName);
            ((TextView) findViewById(R.id.packageText)).setText(packageName);
            ((TextView) findViewById(R.id.appDescriptionText)).setText(R.string.not_installed);
            return;
        }

        findViewById(R.id.button).setEnabled(true);
        ((TextView) findViewById(R.id.nameText)).setText(applicationInfo.loadLabel(getPackageManager()));
        ((TextView) findViewById(R.id.packageText)).setText(packageName);
        ((TextView) findViewById(R.id.appDescriptionText)).setText(applicationInfo.loadDescription(getPackageManager()));
    }

    public void onRemoveAppClicked(View view) {
        if (applicationInfo != null) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", applicationInfo.packageName, null));
            startActivity(intent);
        }
    }
}
