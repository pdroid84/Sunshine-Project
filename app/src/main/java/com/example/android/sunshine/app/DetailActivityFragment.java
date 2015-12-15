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
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * DetailActivityFragment
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mDetailData;
    TextView mTextView;
    private ShareActionProvider mShareActionProvider;
    private static String mForecastStr;
    private static final String SHARE_DATA_HASHTAG = " #Sunshine App";
    private static String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int DETAIL_LOADER_ID = 0;
    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.detail_textview);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mForecastStr = intent.getDataString();
            Log.v(LOG_TAG,"String received from ForecastFragment-> "+mForecastStr);
        }
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
        if (mForecastStr != null) {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
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
        //Parse the String and convert to Uri
        Uri weatherLocationDateUri = Uri.parse(mForecastStr);
        //Sort Order: Ascending by Date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        switch (id) {
            case DETAIL_LOADER_ID:
                return new CursorLoader(
                        getActivity(),          //Parent Activity Context
                        weatherLocationDateUri, //Table to query
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
            String date = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
            String desc = data.getString(COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());
            String highTemp = Utility.formatTemperature(data.getLong(COL_WEATHER_MAX_TEMP), isMetric);
            String lowTemp = Utility.formatTemperature(data.getLong(COL_WEATHER_MIN_TEMP), isMetric);
            mDetailData = String.format("%s - %s - %s/%s",date,desc,highTemp,lowTemp);
            mTextView.setText(mDetailData);
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
