package com.labs.tim_barrett.weathertracker;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by tim_barrett on 1/19/2016.
 */
public class GetWeatherService extends IntentService{

    public static final String LOG_TAG = GetWeatherService.class.getSimpleName();


    public GetWeatherService(){ super("SchedulingService");}

    /**
     * onHandleIntent
     *      handles weather service intent
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent){
        Log.d(LOG_TAG,"onHandleIntent");
        final Context context = getApplicationContext();
        WeatherTask task = new WeatherTask(context);
        task.processWeather();

        GetWeatherReceiver.completeWakefulIntent(intent);

    }



}
