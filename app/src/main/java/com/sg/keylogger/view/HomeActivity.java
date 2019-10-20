package com.sg.keylogger.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.sg.keylogger.R;
import com.sg.keylogger.services.KeyLogger;
import com.sg.keylogger.services.network.CallService;
import com.sg.keylogger.services.network.RetrofitInstance;

import java.security.Key;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class HomeActivity extends AppCompatActivity {
    FloatingActionButton floatingActionButton;
    MaterialDialog notice;
    boolean enable=false;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        floatingActionButton=findViewById(R.id.floatingActionButton);
        Intent x=getIntent();
        if(x.hasExtra("username")){
            username=x.getStringExtra("username");
        }
        getSupportActionBar().setTitle(username);
        Intent i=new Intent(HomeActivity.this, KeyLogger.class);
        getApplicationContext().startService(i);
        KeyLogger.username=username;
        if(KeyLogger.enable){
            floatingActionButton.setImageResource(android.R.drawable.ic_media_pause);
        }
        else{
            floatingActionButton.setImageResource(android.R.drawable.ic_media_play);
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(KeyLogger.enable){
                    Snackbar.make(floatingActionButton.getRootView(),"Service is stopped.",Snackbar.LENGTH_SHORT).show();
                    floatingActionButton.setImageResource(android.R.drawable.ic_media_play);
                    KeyLogger.enable=false;
                }
                else{
                    if(!isAccessibilitySettingsOn(getApplicationContext())) {
                        materialdialog();
                    }
                    else{
                        KeyLogger.enable=true;
                        floatingActionButton.setImageResource(android.R.drawable.ic_media_pause);
                        Snackbar.make(floatingActionButton.getRootView(),"Service is started.",Snackbar.LENGTH_SHORT).show();


                    }
                }
            }
        });

    }


    public void materialdialog(){
        notice= new MaterialDialog.Builder(HomeActivity.this).title("Permission Notice")
                .content("You need to give us accessibility permissions in order to track your mental health via keystrokes.\nWe can't track your mental fitness without accessibility permissions.")
                .positiveText("Give Permissions")
                .negativeText("Exit!")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(!isAccessibilitySettingsOn(getApplicationContext())) {
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            notice.dismiss();
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        notice.dismiss();
                    }
                }).cancelable(false).autoDismiss(false).canceledOnTouchOutside(false)
                .show();
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + KeyLogger.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
