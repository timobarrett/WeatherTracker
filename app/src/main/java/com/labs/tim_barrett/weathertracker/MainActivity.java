package com.labs.tim_barrett.weathertracker;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * MainActivity
 *  application collects, and stores, weather information by location at the same time every day.
 *  calendar interface is used to show the weather for any day.  GPS services are used to get longitude
 *  and latitude used to query for weather data.
 */
public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    protected boolean mGpsPermission = false;
    protected boolean mPermissionResponceRequested = false;
    protected final int GPS_PERMISSION = 7;

    GetWeatherReceiver weather = new GetWeatherReceiver();
    CalendarView weatherCal;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "InOnCreate savedInstanceState = " + savedInstanceState);
        //the following getExtras would contain a value if called from boot_completed receiver
        // Intent foo = getIntent();
       // if (foo != null && foo.getExtras()== null){
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        verifyPermissions();
        setupCalendar();

        CollectBroadcastReceiver broadRev = new CollectBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadRev,
                new IntentFilter(Constants.BROADCAST_LOCATION_ACTION));
    }

    /**
     * onCreateOptionsMenu
     *      in this case settings is the only option
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * onOptionsItemSelected
     *      called when the sttings option is selected from the menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.d(LOG_TAG,"In onOptionsItemSelected");
        int id = item.getItemId();
        if (id == R.id.action_settings){
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
            return super.onOptionsItemSelected(item);

    }

    /**
     * OnRequestPermissionsResult
     *      used to verify that the user has allowed need permissions for the app
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionResult");
        switch (requestCode) {
            case GPS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission to GPS Granted");
                    mGpsPermission = true;

                } else {
                    Log.d(LOG_TAG, "Permission to GPS Denied");
                }
            }
        }
    }
    /**
     * handles the case of both android M and prompting for permissions as well as prior versions
     * ensuring permissions were granted during installation
     */
    protected void verifyPermissions() {
        Log.i(LOG_TAG, "IN verifyPermissions");
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);
                mPermissionResponceRequested = true;
            }
            else{ mGpsPermission= true;}
        } else {
            int result = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (result == PackageManager.PERMISSION_GRANTED) {
                mGpsPermission = true;
            }
        }
        if(mGpsPermission) {
            weather.scheduleWeather(this);
        }

    }

    /**
     * setupCalendar
     *      sets up the calendar display.  Date selected on calendar displays weather data
     */

    public void setupCalendar(){
        Log.d(LOG_TAG,"setupCalendar");
        weatherCal = (CalendarView) findViewById(R.id.calendar);
       final Long  startDate = System.currentTimeMillis();
        Log.d(LOG_TAG, "DATE = " + startDate);
        weatherCal.setBackgroundColor(Color.LTGRAY);

        weatherCal.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int monthDay) {
                Log.d(LOG_TAG, "DATECHANGED" + weatherCal.getDate() + "STARTDATE " + startDate);
                Calendar foo = new GregorianCalendar(year, month, monthDay);
                Log.d(LOG_TAG, "DATE = " + foo.getTimeInMillis());
                performDateCheck(new GregorianCalendar(year, month, monthDay).getTimeInMillis());
            }
        });
    }

    /** performDateCheck
     *      Information has been collected so broadcast for processing
     * @param cmpDate
     */
    public void performDateCheck(Long cmpDate) {
        Log.d(LOG_TAG, "performDateCheck");
        Intent intent = new Intent (this,DailyInfoActivity.class);
        intent.putExtra("DATE", cmpDate);
        startActivity(intent);
    }

    /**
     * broadcast receiver class
     *      receives the weather data collected broadcast.
     */
    public class CollectBroadcastReceiver extends BroadcastReceiver {
        protected final String LOG_TAG = CollectBroadcastReceiver.class.getSimpleName();

        private Activity mActivity;
        CollectBroadcastReceiver(Activity activity) {mActivity = activity;}

        /**
         * onReceive
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Entering onReceive");

            WeatherTask weather = new WeatherTask(mActivity);
            weather.execute();
        }
    }


}
