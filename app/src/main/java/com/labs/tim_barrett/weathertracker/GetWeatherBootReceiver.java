package com.labs.tim_barrett.weathertracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by tim_barrett on 2/1/2016.
 */
    public class GetWeatherBootReceiver extends BroadcastReceiver {
        GetWeatherReceiver receiver = new GetWeatherReceiver();

        /**
         * onReceive
         *      called when the device is booted
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Log.d("BOOT", "BOOT_COMPLETED");
                String sap = "YES";
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //the following line gives the bundle size so that mainActivity:onCreate can determine it was called from here.
                i.putExtra("BOOT", sap);
                Intent j = new Intent(context,GetWeatherService.class);
                context.startService(j);
                receiver.scheduleWeather(context);
            }
        }
    }

