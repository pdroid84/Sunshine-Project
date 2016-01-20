package com.example.android.sunshine.app;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String DETAILFRAGMENT_TAG = "detail_fragment";
    private String mLocation;
    private boolean mTwoPane = false;
    private static String mCityNameData = "test";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //Following is to ensure bach home button is not showed for main activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //Following is to ensure activity title is not shown, otherwise it will push the logo in the middle
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //Store the current location
        mLocation = Utility.getPreferredLocation(this);
        //If we rotate the phone, the system saves the fragment state in the saved state bundle
        // and is smart enough to restore this state. So check for null and create only when starting for the first time
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            //get rid of an unnecessary shadow below the action bar
            getSupportActionBar().setElevation(0f);
        }
            //Access setter method setTodayLayout of Adapter via ForecastFragment which has the member mForecastAdapter
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (ff != null) {
                ff.mForecastAdapter.setTodayLaout(!mTwoPane);
            }
        //Initialize the syncAdapter
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        Log.v(LOG_TAG,"onItemSelected of ForecastFragment.Callback is called");
        if (mTwoPane) {
            //Create a Bundle to pass the data to Fragment
            Bundle args = new Bundle();
            //Get the data and put that to Bundle
            args.putParcelable(DetailActivityFragment.DTL_ACT_FRAG_URI, dateUri);

            //Create a DetailActivityFragment
            DetailActivityFragment dFragment = new DetailActivityFragment();
            //Attach the data to the Fragment
            dFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, dFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }

    public static String getmCityNameData() {
        return mCityNameData;
    }

    public static void setmCityNameData(String mCityNameData) {
        MainActivity.mCityNameData = mCityNameData;
    }
}
