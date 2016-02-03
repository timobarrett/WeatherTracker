package com.labs.tim_barrett.weathertracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.labs.tim_barrett.weathertracker.data.WeatherContract;

import java.util.Calendar;

/**
 * Created by tim_barrett on 1/19/2016.
 *  schedule the weather to be collected every day at the same time
 *  Boot receiver will call this to schedule when device is booted
 */
public class GetWeatherReceiver extends WakefulBroadcastReceiver {
    private final String LOG_TAG = GetWeatherReceiver.class.getSimpleName();
    protected Activity mActivity;
    private AlarmManager scheduleMgr;
    private PendingIntent alarmIntent;

    /**
     * onReceive
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent){
        Log.d(LOG_TAG, "Entering onReceive");

       // use google services to get the lat and lon
      Intent service = new Intent(context, GetWeatherService.class);

      startWakefulService(context, service);

    }

    /**
     * scheduleWeather
     *      schedules the weather collection daily at 5:00am
     *
     * @param context
     */
    public void scheduleWeather(Context context) {
        Log.d(LOG_TAG,"In scheduleWeather");
        mActivity = (Activity)context;
        scheduleMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
     //   Intent intent = new Intent(context, GetWeatherReceiver.class);
        Intent intent = new Intent(context, GetWeatherReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 4:00 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 00);

        // Set the alarm to fire at approximately 4:00 a.m., according to the device's
        // clock, and to repeat once a day.
        scheduleMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
        calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, GetWeatherBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(LOG_TAG,"Exiting - scheduleWeather");
    }
}
