package io.github.h4mu.sysprotec;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class AppsHistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static final String COLUMN_PACKAGE = "package";
    private static final String COLUMN_INSTALLDATE = "install date";
    private ArrayList<HashMap<String, String>> apps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_history);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        for (String item : sharedPref.getString("history", "").split(";")) {
            String[] pkgDate = item.split("\\|");
            if (pkgDate.length >= 2) {
                HashMap<String, String> row = new HashMap<>();
                row.put(COLUMN_PACKAGE, pkgDate[1]);
                row.put(COLUMN_INSTALLDATE, pkgDate[0]);
                apps.add(row);
            }
        }

        ListView listView = findViewById(R.id.packageList);
        listView.setAdapter(new SimpleAdapter(this, apps, android.R.layout.simple_list_item_2,
                new String[]{COLUMN_INSTALLDATE, COLUMN_PACKAGE}, new int[]{android.R.id.text1, android.R.id.text2}));
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(AppsHistoryActivity.this, AppDetailActivity.class);
        intent.putExtra(COLUMN_PACKAGE, apps.get(position).get(COLUMN_PACKAGE));
        startActivity(intent);
    }
}
