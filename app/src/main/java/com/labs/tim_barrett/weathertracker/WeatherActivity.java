package com.labs.tim_barrett.weathertracker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.labs.tim_barrett.weathertracker.data.WeatherContract;
import com.labs.tim_barrett.weathertracker.data.WeatherContract.WeatherEntry;
import com.labs.tim_barrett.weathertracker.data.WeatherDbHelper;

/**
 * Created by tim_barrett on 1/19/2016.
 */
public class WeatherActivity extends AsyncTask<String,Void,String[]>{
    protected final String LOG_TAG = WeatherActivity.class.getSimpleName();
    protected String mCurrentCond;
    protected String mConditionDescript ;

    protected float mWindSpeed;
    protected float mWindDirection;
    protected Double mtemperature ;
    protected Integer mHumidity;
    protected String mLocatId;
    protected Integer mWeatherId;
    protected String mWeatherCity;
//    protected Double mWeatherLat;
//    protected Double mWeatherLon;
    protected Double mActualLat;
    protected Double mActualLon;
    protected Activity mActivity;
    protected ContentResolver mCR;
    private LocationManager mLocationMgr;
    private Location mLocation;
    private Double mLatitude = 0.0;
    private Double mLongitude = 0.0;
    private String[] mLocationId = {""};

    public WeatherActivity(Activity activity){mActivity = activity;}

    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);
    }

    @Override
    protected String[] doInBackground(String... Params){
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
    //    Log.d(LOG_TAG, "DO_IN_BACKGROUND - Parameter 1 = " + Params[0]);
        String forecastJsonStr = null;
        this.getGpsLocation();
        //should try catch items below can trip a format exception
        final String FORECAST_BASE_URL =
                "http://api.openweathermap.org/data/2.5/weather?";
        final String QUERY_PARAM_LAT = "lat";
        final String QUERY_PARAM_LON = "lon";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String APPID_PARAM = "APPID";
        final String APPID_VALUE = "0da6960ae510202d1f8633e08e075162";
        Long locationId = 0L;
        String[] locationResult = {""};

        getGpsLocation();
        if (mLongitude == 0.0 && mLatitude == (0.0)){
            mLongitude = -71.65;
            mLatitude = 42.84;
        }

        Uri weatherUri = Uri.parse(FORECAST_BASE_URL)
                .buildUpon()
                .appendQueryParameter(QUERY_PARAM_LAT, mLatitude.toString())
                .appendQueryParameter(QUERY_PARAM_LON, mLongitude.toString())
                .appendQueryParameter(FORMAT_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(APPID_PARAM, APPID_VALUE)
                .build();
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            URL weatherUrl = new URL(weatherUri.toString());
            Log.i(LOG_TAG, "URL Built -" + weatherUrl.toString());
            //     URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?zip=03031&mode=json&units=metric&cnt=7");

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) weatherUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.i(LOG_TAG, "returned = " + forecastJsonStr.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
            try {
                Log.d(LOG_TAG,"JSONSTR = " + forecastJsonStr.toString());
                locationId =  getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Exception parsing weather data" + e.getLocalizedMessage());
            }
        }
        locationResult[0] = locationId.toString();
        Log.i(LOG_TAG,"mWeatherId2 = "+ locationResult);
        return locationResult;

    }

    /**
     * parse the json returned collecting the values to be stored
     * @param forecastJsonStr
     * @return
     * @throws JSONException
     */
    private Long getWeatherDataFromJson(String forecastJsonStr) throws JSONException {
        Log.d(LOG_TAG,"IN getWeatherDataFromJson" + forecastJsonStr);
        final String TAG_NAME = "main";

        JSONObject weatherJson = new JSONObject(forecastJsonStr);

        JSONObject weatherData = (JSONObject)weatherJson.get(TAG_NAME);
        JSONArray condition = (JSONArray) weatherJson.get("weather");
        JSONObject conditionObj = (JSONObject) condition.get(0);
        mCurrentCond = conditionObj.get("main").toString();
        mWeatherId = conditionObj.getInt("id");
        mConditionDescript = conditionObj.get("description").toString();
        JSONObject windObj = (JSONObject)weatherJson.get("wind");
        Double tempWind = windObj.getDouble("speed");
        mWindSpeed = tempWind.floatValue();
        Double tempDirect = windObj.getDouble("deg");
        mWindDirection = tempDirect.floatValue();
        JSONObject tempHumidObj = (JSONObject)weatherJson.get("main");
        mtemperature = tempHumidObj.getDouble("temp");
        //TODO do I need the opt approach for other data?
        mHumidity = tempHumidObj.optInt("humidity",0);
        mLocatId = weatherJson.get("id").toString();
        mWeatherCity = weatherJson.get("name").toString();
        JSONObject coordObj = (JSONObject)weatherJson.get("coord");
        mActualLat = coordObj.getDouble("lat");
        mActualLon = coordObj.getDouble("lon");

        Long locationId = addLocation(mWeatherCity, mActualLat, mActualLon, mLatitude, mLongitude);
        addWeatherData(locationId);
        Log.i(LOG_TAG,"mWeatherID1 = "+ mWeatherId);
        return locationId;
    }
    /**
     * add data to the weather table in the database
     * @param locationId
     */
    private void addWeatherData(long locationId) {
        Log.d(LOG_TAG, "IN addWeaterData");

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
        weatherValues.put(WeatherEntry.COLUMN_DATE, Utility.normalizeDate());
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, mHumidity);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, mWindSpeed);
        weatherValues.put(WeatherEntry.COLUMN_WIND_DEGREES, mWindDirection);
        weatherValues.put(WeatherEntry.COLUMN_TEMP, mtemperature);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, mCurrentCond);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, mWeatherId);

        mActivity.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
    }

    /**
     * add the current location information to the database
     * @param cityName
     * @param lat
     * @param lon
     * @param wlat
     * @param wlon
     * @return
     */
    Long addLocation(String cityName, double lat, double lon, double wlat, double wlon) {

        Long locationId = 100L;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = mActivity.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_WEATHER_CITY + " = ?",
                new String[]{cityName},
                null);

            if (locationCursor != null &&locationCursor.moveToFirst()) {
                int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                locationId = locationCursor.getLong(locationIdIndex);
            } else {
                // Now that the content provider is set up, inserting rows of data is pretty simple.
                // First create a ContentValues object to hold the data you want to insert.
                ContentValues locationValues = new ContentValues();

                // Then add the data, along with the corresponding name of the data type,
                // so the content provider knows what kind of value is being inserted.
                locationValues.put(WeatherContract.LocationEntry.COLUMN_WEATHER_CITY, cityName);
                // locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_STARTING_COORD_LAT, lat);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_STARTING_COORD_LON, lon);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_WEATHER_COORD_LAT, wlat);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_WEATHER_COORD_LON, wlon);

                // Finally, insert location data into the database.
                Uri insertedUri = mActivity.getContentResolver().insert(
                        WeatherContract.LocationEntry.CONTENT_URI,
                        locationValues
                );

                // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
                locationId = ContentUris.parseId(insertedUri);

            }


        Log.d(LOG_TAG,"LOCATION_ID =" + locationId);
        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }

    /**
     * check to see if gps is enabled on the device
     * TODO   HOOK THIS BITCH UP.
     *
     * @return
     */
    protected boolean isGpsEnabled() {
        Log.i(LOG_TAG, "IN isGpsDisabled");
        LocationManager locMgr = (LocationManager) mActivity.getSystemService(mActivity.LOCATION_SERVICE);
        return locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getGpsLocation() {
        Log.d(LOG_TAG, "In getGpsLocation");
        mLocationMgr = (LocationManager) mActivity.getSystemService(mActivity.LOCATION_SERVICE);
        try {
            Location mLocation = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (null != mLocation) {
                mLatitude = mLocation.getLatitude();
                mLongitude = mLocation.getLongitude();
            }
        } catch (SecurityException se) {
            Log.d(LOG_TAG, "Security Exception", se);
        }
    }
}
