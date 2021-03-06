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

/**
 * Created by tim_barrett on 1/19/2016.
 */
public class WeatherTask{
//public class WeatherTask extends AsyncTask<String,Void,String[]>{
    protected final String LOG_TAG = WeatherTask.class.getSimpleName();
    protected String mCurrentCond;
    protected String mConditionDescript ;

    protected float mWindSpeed;
    protected float mWindDirection;
    protected Double mtemperature ;
    protected Double mLowTemperature;
    protected Double mHighTemperature;
    protected Double mPressure;
    protected Double mHumidity;
    protected String mLocatId;
    protected Integer mWeatherId;
    protected String mWeatherCity;
    protected Double mActualLat;
    protected Double mActualLon;
    //protected Activity mActivity;
    protected Context mContext;
    private LocationManager mLocationMgr;
    private Double mLatitude = 0.0;
    private Double mLongitude = 0.0;


    public WeatherTask(Context context){mContext = context;}

    protected String[] processWeather(String... Params){
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String forecastJsonStr = null;
        final String FORECAST_BASE_URL =
                "http://api.openweathermap.org/data/2.5/weather?";
        final String QUERY_PARAM_LAT = "lat";
        final String QUERY_PARAM_LON = "lon";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String APPID_PARAM = "APPID";
        final String APPID_VALUE = "Get yours at openweatherapi.org";
        Long locationId = 0L;
        String[] locationResult = {""};
        Log.d(LOG_TAG,"In processWeather");

        getGpsLocation();
        if (mLongitude == 0.0 && mLatitude == (0.0)){
            mLongitude = -71.6253;
            mLatitude = 42.8614;
        }
        String mCityName = Utility.getCityName(mContext,mLatitude,mLongitude);

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
            URL weatherUrl = new URL(weatherUri.toString());
            Log.i(LOG_TAG, "URL Built -" + weatherUrl.toString());

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
            // If the code didn't successfully get the weather data, there's no point in attempting
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
                Log.e(LOG_TAG, "JSON Exception parsing weather data " + e.getLocalizedMessage());
            }
        }
        locationResult[0] = locationId.toString();
        Log.i(LOG_TAG,"mWeatherId2 = "+ locationResult);
        return locationResult;

    }

    /**
     * parse the json returned collecting the values to be stored
     * NOTE optDouble used for fields that are sometimes not provided in the json string returned
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
        Double tempDirect = windObj.optDouble("deg",0.0);
        mWindDirection = tempDirect.floatValue();
        JSONObject tempHumidObj = (JSONObject)weatherJson.get("main");
        mtemperature = tempHumidObj.getDouble("temp");
        mLowTemperature = tempHumidObj.optDouble("temp_min",0.0);
        mHighTemperature = tempHumidObj.optDouble("temp_max",0.0);
        mHumidity = tempHumidObj.optDouble("humidity",0.0);
        mPressure = tempHumidObj.optDouble("pressure",0.0);
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
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, mLowTemperature);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, mHighTemperature);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, mPressure);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, mCurrentCond);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, mWeatherId);

        mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
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
        Cursor locationCursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_WEATHER_CITY + " = ?",
                new String[]{cityName},
                null);

            if (locationCursor != null &&locationCursor.moveToFirst()) {
                int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                locationId = locationCursor.getLong(locationIdIndex);
            } else {
                ContentValues locationValues = new ContentValues();

                locationValues.put(WeatherContract.LocationEntry.COLUMN_WEATHER_CITY, cityName);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_STARTING_COORD_LAT, lat);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_STARTING_COORD_LON, lon);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_WEATHER_COORD_LAT, wlat);
                locationValues.put(WeatherContract.LocationEntry.COLUMN_WEATHER_COORD_LON, wlon);

                // Finally, insert location data into the database.
                        Uri insertedUri = mContext.getContentResolver().insert(
                        WeatherContract.LocationEntry.CONTENT_URI,
                        locationValues
                );

                // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
                locationId = ContentUris.parseId(insertedUri);

            }
        Log.d(LOG_TAG,"LOCATION_ID =" + locationId);
        locationCursor.close();
        return locationId;
    }

    /**
     * check to see if gps is enabled on the device
     * TODO
     *
     * @return
     */
    protected boolean isGpsEnabled() {
        Log.i(LOG_TAG, "IN isGpsDisabled");
        LocationManager locMgr = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        return locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * getGpsLocation
     *      get the user's current latitude and longitude needed to collect weather data closets to them
     */
    private void getGpsLocation() {
        Log.d(LOG_TAG, "In getGpsLocation");
        mLocationMgr = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
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
