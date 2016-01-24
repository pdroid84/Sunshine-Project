package com.example.android.sunshine.app;

/**
 * Created by debashispaul on 13/12/2015.
 */

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private final int VIEW_TODAY = 0;
    private final int VIEW_FUTURE_DAY = 1;
    private boolean mTodayLaout = false;
    private final Context mContext;
    private Cursor mCursor;
    private final ForecastAdapterOnClickHandler mForecastAdapterOnClickHandler;
    private final TextView mEmptyView;
    private static final String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private final ItemChoiceManager mICM;

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler clickHandler, TextView emptyView,
                           int choiceMode) {
        mContext = context;
        mForecastAdapterOnClickHandler = clickHandler;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView forecastView;
        public final TextView maxView;
        public final TextView minView;
        public final TextView mCityName;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            maxView = (TextView) view.findViewById(R.id.list_item_high_textview);
            minView = (TextView) view.findViewById(R.id.list_item_low_textview);
            mCityName = (TextView) view.findViewById(R.id.list_item_city_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "onClick--> getLayoutPosition = " + getLayoutPosition() +
                    "& getAdapterPosition = " + getAdapterPosition());
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mForecastAdapterOnClickHandler.onClick(mCursor.getLong(dateColumnIndex),this);
            mICM.onClick(this);
        }
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(LOG_TAG,"onCreateViewHolder is called");
        if(parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_FUTURE_DAY:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId,parent,false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder viewHolder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder is called");
        // move the cursor to correct position
        mCursor.moveToPosition(position);

        // Read weather icon ID from cursor
        //int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

        //Read date from cursor
        long weatherDate = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String weatherDateText = Utility.getFriendlyDayString(mContext, weatherDate);
        if (mCursor.getPosition() == 0 & !mTodayLaout) {
            //Display the Day as "Today" for first item of tablet
            viewHolder.dateView.setText("Today");
        } else {
            //Display the Data as friendly date for rest
            viewHolder.dateView.setText(weatherDateText);
        }

        //Read weather forecast from cursor
        String weatherForecast = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //Display the weather forecast
        viewHolder.forecastView.setText(weatherForecast);
        // For accessibility, add a content description to the weather forecast description filed field.
        viewHolder.forecastView.setContentDescription(mContext.getString(R.string.a11y_forecast, weatherForecast));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read high temperature from cursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.maxView.setText(Utility.formatTemperature(mContext, high, isMetric));
        // For accessibility, add a content description to the high temp field.
        viewHolder.maxView.setContentDescription(mContext.getString(R.string.a11y_high_temp, Utility.formatTemperature(mContext, high, isMetric)));

        //Read low temperature from cursor
        double min = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.minView.setText(Utility.formatTemperature(mContext, min, isMetric));
        // For accessibility, add a content description to the min temp field.
        viewHolder.minView.setContentDescription(mContext.getString(R.string.a11y_low_temp,Utility.formatTemperature(mContext, min, isMetric)));

        //Retrieve and store the City name in MainActivity variable which is used to populate Detail view
        MainActivity.setmCityNameData(mCursor.getString(ForecastFragment.COL_CITY_NAME));

        //Get the view type to determine if colour or black and white image are to be populated
        int viewTypeForIcon = getItemViewType(mCursor.getPosition());
        int fallbackIconId;
        if (viewTypeForIcon == VIEW_TODAY) {
            fallbackIconId = Utility.getArtResourceForWeatherCondition(mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID));
            viewHolder.mCityName.setText(MainActivity.getmCityNameData());
        } else {
            fallbackIconId = Utility.getIconResourceForWeatherCondition(mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID));
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext,mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);

        // this enables better animations. even if we lose state due to a device rotation,
        // the animator can use this to re-find the original view
        ViewCompat.setTransitionName(viewHolder.iconView, "iconView" + position);

        mICM.onBindViewHolder(viewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    @Override
    public int getItemViewType(int position) {
//        Log.v(LOG_TAG,"getItemViewType is called");
        return (position == 0 && mTodayLaout) ? VIEW_TODAY : VIEW_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
//        Log.v(LOG_TAG,"getItemCount is called");
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void setTodayLaout(boolean todayLaout) {
        mTodayLaout = todayLaout;
    }

    public void swapCursor(Cursor newCursor) {
        Log.v(LOG_TAG,"swapCursor is called");
        mCursor = newCursor;
        notifyDataSetChanged();
        if(getItemCount() == 0) {
           mEmptyView.setVisibility(TextView.VISIBLE);
       } else {
           mEmptyView.setVisibility(TextView.GONE);
       }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public interface ForecastAdapterOnClickHandler {
        public void onClick(Long date, ForecastAdapterViewHolder vh);
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ForecastAdapterViewHolder ) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

}
