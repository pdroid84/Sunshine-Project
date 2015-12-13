package com.example.android.sunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

/**
 * DetailActivityFragment
 */
public class DetailActivityFragment extends Fragment {

    private ShareActionProvider shareActionProvider;
    private static String mWeatherData;
    private static final String SHARE_DATA_HASHTAG = " #Sunshine App";
    private static String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.detail_textview);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(ForecastFragment.WEATHER_DATA)) {
            mWeatherData = intent.getStringExtra(ForecastFragment.WEATHER_DATA);
            textView.setText(mWeatherData);
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
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (shareActionProvider != null)
        {
            shareWeatherData(shareActionProvider);
        }
        else {
            Log.d(LOG_TAG,"Share Action Provider is null");
        }
    }

    /**
     * Share weather data with other applications
     */
    public void shareWeatherData(ShareActionProvider shareAction) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mWeatherData + SHARE_DATA_HASHTAG);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sendIntent.setType("text/plain");
        shareAction.setShareIntent(sendIntent);
    }
}
