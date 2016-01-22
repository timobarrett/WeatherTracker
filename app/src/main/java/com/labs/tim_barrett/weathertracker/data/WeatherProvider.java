package com.labs.tim_barrett.weathertracker.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.labs.tim_barrett.weathertracker.Utility;

/**
 * Created by tim_barrett on 1/19/2016.
 */
public class WeatherProvider extends ContentProvider{
    protected final String LOG_TAG = WeatherProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;
  
 
    private static double totalAvgTemp = 0;
    private static int totalBadWeatherCnt = 0;
    private static int totalRunDays = 0;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    //       "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";
                    "." + WeatherContract.LocationEntry.COLUMN_WEATHER_CITY + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    //       "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    "." + WeatherContract.LocationEntry.COLUMN_WEATHER_CITY + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    //         "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    "." + WeatherContract.LocationEntry.COLUMN_WEATHER_CITY + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

    /**
     * gets a cursor of weather data by location
     * @param uri
     * @param projection
     * @param sortOrder
     * @return
     */
    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * return a cursor of weather information by location and date
     * @param uri
     * @param projection
     * @param sortOrder
     * @return
     */
    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    /*
       This UriMatcher will match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.
     */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
        return matcher;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "IN onCreate");
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    /**
     * call to get type of URI
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * call to query database tables for records
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE: {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
      
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * call to insert database table records
     * @param uri
     * @param values
     * @return
     */
    @Override
    public  Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        Log.d(LOG_TAG, "IN insert" + match);
        switch (match) {
            case WEATHER: {
                //  normalizeDate(values);  //this blocks multiple daily records for weather.  // TODO: 11/10/2015 adjust to work with gregorian calander changes. 
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else {
                    Log.d("WeatherProvider", "Failed to insert row into " + uri);
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                }
                break;
            }
            case LOCATION: {
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     * call o delete database table records
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case WEATHER:
                rowsDeleted = db.delete(
                        WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(
                        WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Call to update database table records
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WEATHER:
                // Utility.normalizeDate(values);
                rowsUpdated = db.update(WeatherContract.WeatherEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

//REMOVED SUPPORT FOR BULK INSERT - NOT NEEDED

    /**
     * designed to keep all database related functions in this class
     * @param method
     * @param arg
     * @param extras
     * @return
     */
    @Override
    public Bundle call(String method, String arg, Bundle extras){
        Bundle returnBun = new Bundle();
        switch(method){
            case "getMonthlyTotals":
              //  getMonthlyTotals();
                returnBun.putDouble("avgTemp", totalAvgTemp);
                returnBun.putInt("badWeatherCnt", totalBadWeatherCnt);
                returnBun.putInt("daysRunCnt", totalRunDays);
                break;

        }


        return returnBun;
    }
    //TODO   do I want to provide a monthly summary?
//    /**
//     * rollup the monthly totals from the distance table.
//     * initialize the values first to prevent multi counting
//     *
//     * @return
//     */
//    public boolean getMonthlyTotals() {
//        Log.d("WeatherProvider","getMonthlyTotals");
//        boolean status = false;
//        totalMileage = 0;
//        totalRunDuration = 0;
//        totalDuration = 0;
//        totalMonthDate = 0;
//        totalAvgTemp = 0;
//        totalRunDays = 0;
//        totalBadWeatherCnt = 0;
//        double tempAvgTotal = 0;
//
//        Cursor dbCursor = query(WeatherContract.DistanceEntry.CONTENT_URI,null,null,null,null);
//        if (dbCursor.moveToFirst()) {
//            do {
//                totalMileage += dbCursor.getFloat(dbCursor.getColumnIndex(WeatherContract.DistanceEntry.COLUMN_MILEAGE));
//                totalRunDuration += dbCursor.getInt(dbCursor.getColumnIndex(WeatherContract.DistanceEntry.COLUMN_RUNNING_DURATION));
//                totalDuration += dbCursor.getInt(dbCursor.getColumnIndex(WeatherContract.DistanceEntry.COLUMN_TOTAL_DURATION));
//                totalMonthDate = dbCursor.getLong(dbCursor.getColumnIndex(WeatherContract.DistanceEntry.COLUMN_DATE));
//            }while (dbCursor.moveToNext());
//
//            totalRunDays = dbCursor.getCount();
//            Log.d(LOG_TAG,"TotalMileage"+ totalMileage);
//            status = true;
//        }
//        Cursor cursor2 = query(WeatherContract.WeatherEntry.CONTENT_URI,null,null,null,null);
//        if (cursor2.moveToFirst()){
//            do {
//                tempAvgTotal += cursor2.getFloat(cursor2.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMP));
//                int weatherId = cursor2.getInt(cursor2.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
//                if (weatherId >= 800 && weatherId <= 804){
//                    totalBadWeatherCnt ++;
//                }
//            }while (cursor2.moveToNext());
//            totalAvgTemp = tempAvgTotal/cursor2.getCount();
//            status = true;
//        }
//        return status;
//    }




    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}

