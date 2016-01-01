package com.example.android.sunshine.app;

/**
 * Created by debashispaul on 29/11/2015.
 */

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.service.SunshineService;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int SUNSHINE_LOADER_ID = 0;
    protected ForecastAdapter mForecastAdapter;
    private ListView listView;
    private int mListCurPos;
    private final static String LIST_CURR_POS = "current_position";
    private Callback mCallback;
    public static final String WEATHER_DATA = "weather_data";
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_CITY_NAME = 9;

    public ForecastFragment() {
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
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SUNSHINE_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Retrieve the current position of the selected item
        if (savedInstanceState != null && savedInstanceState.containsKey(LIST_CURR_POS)) {
            mListCurPos = savedInstanceState.getInt(LIST_CURR_POS);
        }
        //inflate the view before referring any view using id
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Create a new customised cursor adapter - ForecastAdapter
        mForecastAdapter = new ForecastAdapter(getActivity(),null,0);

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                mListCurPos = position;
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                            cursor.getLong(COL_WEATHER_DATE));
                    mCallback.onItemSelected(uri);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListCurPos != ListView.INVALID_POSITION) {
            outState.putInt(LIST_CURR_POS, mListCurPos);
        }
        super.onSaveInstanceState(outState);
    }

    //This method ensure the loader is restarted whenever the location is changed
    public void onLocationChanged () {
        updateWeather();
        getLoaderManager().restartLoader(SUNSHINE_LOADER_ID, null, this);
    }

    //Stores the updated location
    private void updateWeather() {
//        String location = Utility.getPreferredLocation(getActivity());
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_NAME_EXTRA,location);
//        //Create pending intent for AlarmReceiver
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
//        //Create alarm manager
//        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        //set the alarm to fire in 5 sec
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, pendingIntent);

        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Use WeatherProvider to query the database
        String locationSettings = Utility.getPreferredLocation(getActivity());
        //Sort Order: Ascending by Date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        //Build the URI with location and current date
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSettings,
                System.currentTimeMillis());

        switch (id) {
            case SUNSHINE_LOADER_ID:
                return new CursorLoader(
                        getActivity(),          //Parent Activity Context
                        weatherForLocationUri,  //Table to query
                        FORECAST_COLUMNS,       //Projection to return
                        null,                   //Selection Clause, null->will return all data
                        null,                   //Selection Arg, null-> will return all data
                        sortOrder);             //Sort order, will be sorted by date in ascending order
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        //position the list to current position
        if(mListCurPos != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(mListCurPos);
        }
       // selectFirstItem(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback interface");
        }
    }

    //Following piece of code was to select and detail display first item for tablet but it's not working :(
//    public void selectFirstItem(Cursor cur) {
//        if (mForecastAdapter.getTodayLaout()) {
//            Log.d(LOG_TAG,"Phone, so no need to select the first list item");
//        } else {
//            Log.d(LOG_TAG,"Tablet, so need to select the first list item");
//            listView.setItemChecked(0, true);
//            long weatherDate = cur.getLong(COL_WEATHER_DATE);
//            String locationSetting = Utility.getPreferredLocation(getActivity());
//            Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, weatherDate);
//          //  mCallback.onItemSelected(uri);
//        }
//    }
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
        //public void selectTodayForTablet(Cursor cur);
    }
}