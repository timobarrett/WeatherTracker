package com.labs.tim_barrett.weathertracker;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.tim_barrett.weathertracker.data.WeatherContract;


/**
 * Created by tim_barrett on 1/25/2016.
 *      perform a database lookup and get the weather information for date selected
 *      paint the screen with result
 *      THEN
 *      perform a database lookup to get the city name
 *      NOTE city name is the location of the nearest BMC to the user.
 */
public class DailyInfoActivity extends AppCompatActivity {
    protected final String LOG_TAG = DailyInfoActivity.class.getSimpleName();
    //variables to address screen
    protected TextView mDetailDay;
    protected TextView mDetailDate;
    protected TextView mCityName;
    protected TextView mHighTemp;
    protected TextView mCurrentTemp;
    protected TextView mLowTemp;
    protected TextView mForecast;
    protected TextView mHumidity;
    protected TextView mPressure;
    protected TextView mWind;
    protected ImageView mIcon;
    protected TextView mHighTitle;
    protected TextView mCurrentTitle;
    protected TextView mLowTitle;
    protected String mLocName;

    //column indexes
    static final int COL_ID = 0;
    static final int COL_WEATHER_LOCATION_ID = 1;
    static final int COL_WEATHER_DATE = 2;
    static final int COL_WEATHER_DESC = 3;
    static final int COL_WEATHER_ID = 4;
    static final int COL_WEATHER_TEMP = 5;
    static final int COL_WEATHER_MIN_TEMP = 6;
    static final int COL_WEATHER_MAX_TEMP = 7;
    static final int COL_WEATHER_HUMIDITY = 8;
    static final int COL_WEATHER_PRESSURE = 9;
    static final int COL_WEATHER_WIND = 10;
    static final int COL_WEATHER_DEGREES = 11;

    static final int COL_LOCATION_CITY = 1;

    // values passed in Intent
    protected Long mWeatherDate;

    /**
     * onCreate
     *      get the daily information screen references
     *      trigger lookup and display update
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate ");
        setContentView(R.layout.daily_info_fragment);
        // map the screen fields to member variables
        mDetailDay = (TextView)findViewById(R.id.detail_day_textview);
        mDetailDate = (TextView)findViewById(R.id.detail_date_textview);
        mCityName = (TextView)findViewById(R.id.city_name_textview);
        mHighTemp = (TextView)findViewById(R.id.detail_high_textview);
        mCurrentTemp = (TextView)findViewById(R.id.detail_current_textview);
        mLowTemp = (TextView)findViewById(R.id.detail_low_textview);
        mForecast = (TextView)findViewById(R.id.detail_forecast_textview);
        mHumidity = (TextView)findViewById(R.id.detail_humidity_textview);
        mWind = (TextView)findViewById(R.id.detail_wind_textview);
        mPressure = (TextView)findViewById(R.id.detail_pressure_textview);
        mIcon = (ImageView)findViewById(R.id.detail_icon);
        mHighTitle = (TextView)findViewById(R.id.detail_high_title);
        mCurrentTitle = (TextView)findViewById(R.id.detail_current_title);
        mLowTitle = (TextView)findViewById(R.id.detail_low_title);


        Intent intent = getIntent();
        mWeatherDate = intent.getLongExtra("DATE", 0);
        Log.d(LOG_TAG, "DATE = " + mWeatherDate);
        
        //Look up the record for the date in the database
        Cursor weatherCursor = lookupWeather();
        Log.d(LOG_TAG,"CURSORCNT = "+ weatherCursor.getCount());
        if (weatherCursor != null && weatherCursor.getCount()>0){
            weatherCursor.moveToFirst();
            lookupLocation(weatherCursor.getInt(COL_WEATHER_LOCATION_ID));
            updateTheContainer(weatherCursor);
            weatherCursor.close();
        }
        else{
            mDetailDay.setText(R.string.no_data);
        }
    }

    /**
     *  lookupWeather
     *      reads the weather information from the weather table.
     *      NOTE - could implement a join to get location data as well.
     * @return
     */
    private Cursor lookupWeather(){
        Log.d(LOG_TAG,"lookupWeather - mWeatherDate = "+mWeatherDate);
        String[] weatherInformation = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_LOC_KEY,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,

                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_WIND_DEGREES
        };

        Uri weatherUri = WeatherContract.WeatherEntry.CONTENT_URI;
        String selection=WeatherContract.WeatherEntry.COLUMN_DATE + "=?";
        String[] selectionArgs = new String[]{Long.toString(mWeatherDate)};
        Log.d(LOG_TAG,"URI="+weatherUri);
        return getApplicationContext().getContentResolver().query(
                weatherUri,
                weatherInformation,
                selection,
                selectionArgs,
                null);
    }

    /**
     * lookupLocation
     *     Reads the city name weather source from the location table
     *     Could implement a join with weather lookup and save this step
     * @param locId
     */
    private void lookupLocation(Integer locId){
        String[] locationInformation = {
                WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.LocationEntry.COLUMN_WEATHER_CITY,
                WeatherContract.LocationEntry.COLUMN_STARTING_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_STARTING_COORD_LON,
                WeatherContract.LocationEntry.COLUMN_WEATHER_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_WEATHER_COORD_LON
        };
        Uri locationUri = WeatherContract.LocationEntry.CONTENT_URI;
        String selection= WeatherContract.LocationEntry._ID + "=?";
        String[] selectionArgs = new String[]{Integer.toString(locId)};
        Cursor loc = getApplicationContext().getContentResolver().query(
                locationUri,
                locationInformation,
                selection,
                selectionArgs,
                null);
        if (loc != null && loc.moveToFirst()){
            mLocName = loc.getString(COL_LOCATION_CITY);
        }
        loc.close();
    }

    /**
     * updateTheContainer
     *      displays and populates the weather detail for the day selected.
     * @param weatherCursor
     */

    private void updateTheContainer(Cursor weatherCursor){
        Log.d(LOG_TAG,"updateTheContainer");
        if (weatherCursor.moveToFirst()){
            String friendlyDateText = Utility.getDayName(getApplicationContext(), mWeatherDate);
            mDetailDay.setText(friendlyDateText);
            String dateText = Utility.getFormattedMonthDay(getApplicationContext(), mWeatherDate);
            mDetailDate.setText(dateText);
            mCityName.setText(mLocName);
            float humidity = weatherCursor.getFloat(COL_WEATHER_HUMIDITY);
            mHumidity.setText(getApplicationContext().getString(R.string.format_humidity, humidity));
            float pressure = weatherCursor.getFloat(COL_WEATHER_PRESSURE);
            mPressure.setText(getApplicationContext().getString(R.string.format_pressure, pressure));
            double highTemp = weatherCursor.getDouble(COL_WEATHER_MAX_TEMP);
            String highTemperature = Utility.formatTemperature(getApplicationContext(), highTemp);
            mHighTemp.setText(highTemperature);
            mHighTitle.setText("High");
            double currentTemp = weatherCursor.getDouble(COL_WEATHER_TEMP);
            String currentTemperature = Utility.formatTemperature(getApplicationContext(), currentTemp);
            mCurrentTemp.setText(currentTemperature);
            mCurrentTitle.setText("Current");
            double lowTemp = weatherCursor.getDouble(COL_WEATHER_MIN_TEMP);
            String lowTemperature = Utility.formatTemperature(getApplicationContext(),lowTemp);
            mLowTemp.setText(lowTemperature);
            mLowTitle.setText("Low");
            mIcon.setImageResource(Utility.returnPngForWeatherCondition(weatherCursor.getInt(COL_WEATHER_ID)));
            mForecast.setText(weatherCursor.getString(COL_WEATHER_DESC));
            mWind.setText(Utility.getFormattedWind(getApplicationContext(), Float.parseFloat(weatherCursor.getString(COL_WEATHER_WIND)), Float.parseFloat(weatherCursor.getString(COL_WEATHER_DEGREES))));
        }
    }
}
