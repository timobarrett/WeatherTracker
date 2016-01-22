package com.labs.tim_barrett.weathertracker;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by tim_barrett on 1/19/2016.
 */
public class GetWeatherService extends IntentService{

    public static final String LOG_TAG = GetWeatherService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1;
    protected Activity mActivity;

    public GetWeatherService(){ super("SchedulingService");}

    @Override
    protected void onHandleIntent(Intent intent){

        final Context context = getApplicationContext();

        Intent localIntent = new Intent(Constants.BROADCAST_LOCATION_ACTION);

        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
      //TODO - Need to send notification to location/weather listener
        GetWeatherReceiver.completeWakefulIntent(intent);

       //weather is collectsed using the lat and lon of the device?  how do I get this???
       // weather.execute(mBeginLat,mBeginLon,mLocationId[0]); - fails not on main thread.
    }



}
