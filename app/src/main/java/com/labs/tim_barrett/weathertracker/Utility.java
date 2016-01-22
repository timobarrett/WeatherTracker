package com.labs.tim_barrett.weathertracker;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by tim_barrett on 1/19/2016.
 */
public class Utility {
    protected final static String LOG_TAG = Utility.class.getSimpleName();
    /**
     * this method normalizes the date to just month day year.
     * ensures one record per day will exist in database.
     *
     * @return
     */
    public static long normalizeDate() {
        //todo remove log.d calls
        //todo anyway to combine calendar returneddate and return gettimeinmilliseconds?
        Calendar todaysDate = Calendar.getInstance();
        Log.d(LOG_TAG, "DAY = " + todaysDate.get(Calendar.DAY_OF_MONTH) + "MONTH = " + todaysDate.get(Calendar.MONTH) + "YEAR = " + todaysDate.get(Calendar.YEAR));
        Calendar returnedDate = new GregorianCalendar(todaysDate.get(Calendar.YEAR), todaysDate.get(Calendar.MONTH), todaysDate.get(Calendar.DAY_OF_MONTH));
        Log.d(LOG_TAG, "returnedDate = " + returnedDate.getTimeInMillis());
        return returnedDate.getTimeInMillis();
    }
}
