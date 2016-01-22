package com.labs.tim_barrett.weathertracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    GetWeatherReceiver weather = new GetWeatherReceiver();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollectBroadcastReceiver broadRev = new CollectBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadRev,
                new IntentFilter(Constants.BROADCAST_LOCATION_ACTION));

        weather.scheduleWeather(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings){
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        weather.scheduleWeather(this);
            return false;

    }
//    public void setSupportActionBar(@Nullable Toolbar toolbar) {
//        setSupportActionBar(toolbar);
//    }

    public class CollectBroadcastReceiver extends BroadcastReceiver {
        protected final String LOG_TAG = CollectBroadcastReceiver.class.getSimpleName();

        private Activity mActivity;

        // LocationBroadcastReceiver(Activity activity){ mActivity = activity;}
        CollectBroadcastReceiver(Activity activity) {mActivity = activity;}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Entering onReceive");

            WeatherActivity weather = new WeatherActivity(mActivity);
            weather.execute();
        }
    }
}
