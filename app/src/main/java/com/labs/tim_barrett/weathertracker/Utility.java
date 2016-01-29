package com.labs.tim_barrett.weathertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by tim_barrett on 1/19/2016.
 *  Not all methods used but liked em so they stayed.
 */
public class Utility {
    protected final static String LOG_TAG = Utility.class.getSimpleName();
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * this method normalizes the date to just month day year.
     * ensures one record per day will exist in database.
     *
     * @return
     */
    public static long normalizeDate() {
        Calendar todaysDate = Calendar.getInstance();
        Log.d(LOG_TAG, "DAY = " + todaysDate.get(Calendar.DAY_OF_MONTH) + "MONTH = " + todaysDate.get(Calendar.MONTH) + "YEAR = " + todaysDate.get(Calendar.YEAR));
        Calendar returnedDate = new GregorianCalendar(todaysDate.get(Calendar.YEAR), todaysDate.get(Calendar.MONTH), todaysDate.get(Calendar.DAY_OF_MONTH));
        Log.d(LOG_TAG, "returnedDate = " + returnedDate.getTimeInMillis());
        return returnedDate.getTimeInMillis();
    }

    /**
     * returns preference value set.  true if metric
     *
     * @param context
     * @return
     */
    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    /**
     * openweathermap sends back an ID which corresponds to a weather png
     *
     * @param weatherId
     * @return
     */
    public static int returnPngForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        } else {
            return -1;
        }
    }

    /**
     * format the temperature - convert to imperial as needed.
     *
     * @param context
     * @param temperature
     * @return
     */
    public static String formatTemperature(Context context, double temperature) {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    /**
     * transalte wind direction to generic compass points
     *
     * @param context
     * @param windSpeed
     * @param degrees
     * @return
     */
    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;

        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)

        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format("Wind: "+context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getCityName(Context context, Double latitude, Double longitude) {
        String cityName = " ";
        try {
            Geocoder gc = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Log.d(LOG_TAG, "Address " + addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception getting Locale Information. " + e.getLocalizedMessage());
        }
        return cityName;
    }
}

