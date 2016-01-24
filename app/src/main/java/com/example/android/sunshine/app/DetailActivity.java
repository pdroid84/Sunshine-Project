package com.example.android.sunshine.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class DetailActivity extends ActionBarActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            //Create a Bundle to pass the data to Fragment
            Bundle args = new Bundle();
            //Get the data from the intent and put that to Bundle
            args.putParcelable(DetailActivityFragment.DTL_ACT_FRAG_URI, getIntent().getData());
            args.putBoolean(DetailActivityFragment.DETAIL_TRANSITION_ANIMATION, true);

            //Create a DetailActivityFragment
            DetailActivityFragment dFragment = new DetailActivityFragment();
            //Attach the data to the Fragment
            dFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, dFragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }
}
