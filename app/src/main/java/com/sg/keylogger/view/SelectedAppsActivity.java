package com.sg.keylogger.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.sg.keylogger.R;
import com.sg.keylogger.helper.ApkInfoExtractor;
import com.sg.keylogger.helper.AppsAdapter;

public class SelectedAppsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager recyclerViewLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selected_apps);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Passing the column number 1 to show online one column in each row.
        recyclerViewLayoutManager = new GridLayoutManager(SelectedAppsActivity.this, 1);

        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        adapter = new AppsAdapter(SelectedAppsActivity.this, new ApkInfoExtractor(SelectedAppsActivity.this).GetAllInstalledApkInfo());

        recyclerView.setAdapter(adapter);
    }
}
