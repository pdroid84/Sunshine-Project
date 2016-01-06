package com.example.android.sunshine.app;

/**
 * Created by debashispaul on 13/12/2015.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TODAY = 0;
    private final int VIEW_FUTURE_DAY = 1;
    private boolean mTodayLaout = false;
    private Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    public boolean getTodayLaout() {
        return mTodayLaout;
    }

    public void setTodayLaout(boolean todayLaout) {
        mTodayLaout = todayLaout;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        View view;
        if (viewType == VIEW_TODAY) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast_today,parent,false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mTodayLaout) ? VIEW_TODAY : VIEW_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
                    This is where we fill-in the views with the contents of the cursor.
                 */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

        //Read date from cursor
        long weatherDate = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String weatherDateText = Utility.getFriendlyDayString(mContext, weatherDate);
        if (cursor.getPosition() == 0 & !mTodayLaout) {
            //Display the Day as "Today" for first item of tablet
            viewHolder.dateView.setText("Today");
        } else {
            //Display the Data as friendly date for rest
            viewHolder.dateView.setText(weatherDateText);
        }

        //Read weather forecast from cursor
        String weatherForecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        //Display the weather forecast
        viewHolder.forecastView.setText(weatherForecast);
        // For accessibility, add a content description to the weather forecast description filed field.
        viewHolder.forecastView.setContentDescription(context.getString(R.string.a11y_forecast, weatherForecast));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.maxView.setText(Utility.formatTemperature(context, high, isMetric));
        // For accessibility, add a content description to the high temp field.
        viewHolder.maxView.setContentDescription(context.getString(R.string.a11y_high_temp, Utility.formatTemperature(context, high, isMetric)));

        //Read low temperature from cursor
        double min = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.minView.setText(Utility.formatTemperature(context, min, isMetric));
        // For accessibility, add a content description to the min temp field.
        viewHolder.minView.setContentDescription(context.getString(R.string.a11y_low_temp,Utility.formatTemperature(context, min, isMetric)));

        //Retrieve and store the City name in MainActivity variable which is used to populate Detail view
        MainActivity.setmCityNameData(cursor.getString(ForecastFragment.COL_CITY_NAME));

        //Get the view type to determine if colour or black and white image are to be populated
        int viewTypeForIcon = getItemViewType(cursor.getPosition());
        int fallbackIconId;
        if (viewTypeForIcon == VIEW_TODAY) {
            fallbackIconId = Utility.getArtResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID));
            viewHolder.mCityName.setText(MainActivity.getmCityNameData());
        } else {
            fallbackIconId = Utility.getIconResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID));
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext,cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView forecastView;
        public final TextView maxView;
        public final TextView minView;
        public final TextView mCityName;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            forecastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            maxView = (TextView) view.findViewById(R.id.list_item_high_textview);
            minView = (TextView) view.findViewById(R.id.list_item_low_textview);
            mCityName = (TextView) view.findViewById(R.id.list_item_city_name);
        }
    }
}
