package com.labs.tim_barrett.weathertracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tim_barrett on 1/20/2016.
 */
public class GetWeatherBootReceiver extends BroadcastReceiver {
    GetWeatherReceiver receiver = new GetWeatherReceiver();
    @Override
    public void onReceive(Context context, Intent intent){
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            receiver.scheduleWeather(context);
        }
    }
}
