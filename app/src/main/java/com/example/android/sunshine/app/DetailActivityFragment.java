package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * DetailActivityFragment
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mDateWeek, mMaxView, mMinView, mHumidityView, mWindView, mPressureView, mDescView, mDateCalendar, mCityName;
    private ImageView mIconView;
    private String mDetailData;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    public static final String DTL_ACT_FRAG_URI = "detail_fragment_uri";
    private static final String SHARE_DATA_HASHTAG = " #Sunshine App";
    private static String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int DETAIL_LOADER_ID = 0;
    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_PRESSURE = 7;
    private static final int COL_WEATHER_WIND_DEGREE = 8;
    private static final int COL_WEATHER_COND_ID = 9;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get the bundle from the Fragment
        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(DTL_ACT_FRAG_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mDateWeek = (TextView) rootView.findViewById(R.id.detail_date_week);
        mDateCalendar = (TextView) rootView.findViewById(R.id.detail_date_calendar);
        mMaxView = (TextView) rootView.findViewById(R.id.detail_maxTempView);
        mMinView = (TextView) rootView.findViewById(R.id.detail_minTempView);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidityView);
        mWindView = (TextView) rootView.findViewById(R.id.detail_windView);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressureView);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_weather_icon);
        mDescView = (TextView) rootView.findViewById(R.id.detail_weather_desc);
        mCityName = (TextView) rootView.findViewById(R.id.detail_city_name);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Following line needed to let android know that Fragment has options menu
        //If this line is not added then associated method (e.g. OnCreateOptionsMenu) does not get supported
        //even in auto code completion
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu
        inflater.inflate(R.menu.detailfragment, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        //Show share option if onLoadFinished is complete and we have data
        if(mDetailData != null) {
            shareWeatherData(mShareActionProvider);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Create a loader when we have valid data
        if (mUri != null) {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        }
    }

    //This method ensure the loader is restarted whenever the location is changed
    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
    }

    /**
     * Share weather data with other applications
     */
    public void shareWeatherData(ShareActionProvider shareAction) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mDetailData + SHARE_DATA_HASHTAG);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sendIntent.setType("text/plain");
        shareAction.setShareIntent(sendIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Sort Order: Ascending by Date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        switch (id) {
            case DETAIL_LOADER_ID:
                return new CursorLoader(
                        getActivity(),          //Parent Activity Context
                        mUri, //Table to query
                        DETAIL_COLUMNS,         //Projection to return
                        null,                   //Selection Clause, null->will return all data
                        null,                   //Selection Arg, null-> will return all data
                        sortOrder);             //Sort order, will be sorted by date in ascending order
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            String dayWeek = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
            String dateCal = Utility.getFormattedMonthDay(getActivity(), data.getLong(COL_WEATHER_DATE));
            String desc = data.getString(COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());
            String highTemp = Utility.formatTemperature(getActivity(), data.getFloat(COL_WEATHER_MAX_TEMP), isMetric);
            String lowTemp = Utility.formatTemperature(getActivity(), data.getFloat(COL_WEATHER_MIN_TEMP), isMetric);
            String humidity = String.format(getActivity().getString(R.string.format_humidity), data.getFloat(COL_WEATHER_HUMIDITY));
            String windSpeed = Utility.getFormattedWind(getActivity(), data.getLong(COL_WEATHER_WIND_SPEED),
                data.getLong(COL_WEATHER_WIND_DEGREE));
            String pressure = String.format(getActivity().getString(R.string.format_pressure), data.getFloat(COL_WEATHER_PRESSURE));

            mDetailData = String.format("%s - %s - %s/%s",dayWeek,desc,highTemp,lowTemp);

            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_COND_ID)));
            // For accessibility, add a content description to the icon field. Because the ImageView
            // is independently focusable, it's better to have a description of the image. Usin
            // null is appropriate when the image is purely decorative or when the image already
            // has text describing it in the same UI component.
            mIconView.setContentDescription(getString(R.string.a11y_forecast_icon, desc));
            mDateWeek.setText(dayWeek);
            mDateCalendar.setText(dateCal);
            mMaxView.setText(highTemp);
            // For accessibility, add a content description to the high temp field.
            mMaxView.setContentDescription(getString(R.string.a11y_high_temp, highTemp));
            mMinView.setText(lowTemp);
            // For accessibility, add a content description to the low temp field.
            mMaxView.setContentDescription(getString(R.string.a11y_low_temp,lowTemp));
            mHumidityView.setText(humidity);
            // For accessibility, add a content description to the humidity field.
            mHumidityView.setContentDescription(getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY)));
            mWindView.setText(windSpeed);
            // For accessibility, add a content description to the wind speed field.
            mWindView.setContentDescription(mWindView.getText());
            mPressureView.setText(pressure);
            // For accessibility, add a content description to the pressure field.
            mPressureView.setContentDescription(mPressureView.getText());
            mDescView.setText(desc);
            // For accessibility, add a content description to the description field.
            mDescView.setContentDescription(getString(R.string.a11y_forecast, desc));
            mCityName.setText(MainActivity.getmCityNameData());
        }

        if (mShareActionProvider != null)
        {
            //If onCreateOptionMenus has happened already then update the share intent
            shareWeatherData(mShareActionProvider);
        }
        else {
            Log.d(LOG_TAG, "Share Action Provider is null");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Do nothing
    }
}
