package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

import java.util.concurrent.ExecutionException;

/**
 * Created by debashispaul on 27/01/2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    private final static String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME,
            WeatherContract.WeatherEntry.COLUMN_DATE,

    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_COND_ID = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_CITY_NAME = 5;
    static final int COL_WEATHER_DATE = 6;

    private static final String SORT_ORDER = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DetailWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DetailWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        private int mAppWidgetId;
        Cursor mCursor = null;

        public DetailWidgetRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
            // for example downloading or creating content etc, should be deferred to onDataSetChanged()
            // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        }

        @Override
        public void onDataSetChanged() {
            // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
            // on the collection view corresponding to this factory. You can do heaving lifting in
            // here, synchronously. For example, if you need to process an image, fetch something
            // from the network, etc., it is ok to do it here, synchronously. The widget will remain
            // in its current state while work is being done here, so you don't need to worry about
            // locking up the widget.
            if (mCursor != null) {
                mCursor.close();
            }
            //Get today's weather data from the database
            final long identityToken = Binder.clearCallingIdentity();
            String locationSettings = Utility.getPreferredLocation(mContext);
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSettings,
                    System.currentTimeMillis());
            mCursor = getApplicationContext().getContentResolver().query(
                    weatherForLocationUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    SORT_ORDER
            );
            Binder.restoreCallingIdentity(identityToken);

            if (mCursor == null) {
                Log.v(LOG_TAG, "Cursor is null!!");
                return;
            }
            if (!mCursor.moveToFirst()) {
                Log.v(LOG_TAG, "Cursor is empty!!");
                mCursor.close();
                return;
            }
            Log.v(LOG_TAG, "Total record in the cursor:" + mCursor.getCount());
        }

        @Override
        public void onDestroy() {
            // In onDestroy() you should tear down anything that was setup for your data source,
            // eg. cursors, connections, etc.
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // position will always range from 0 to getCount() - 1.

            //Position the cursor to right row
            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }

            // Extract the weather data from the Cursor
            int weatherId = mCursor.getInt(COL_WEATHER_COND_ID);
            int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
            Bitmap weatherArtImage = null;
            if (!Utility.usingLocalGraphics(DetailWidgetRemoteViewsService.this)) {
                String weatherArtResourceUrl = Utility.getArtUrlForWeatherCondition(
                        DetailWidgetRemoteViewsService.this, weatherId);
                try {
                    weatherArtImage = Glide.with(DetailWidgetRemoteViewsService.this)
                            .load(weatherArtResourceUrl)
                            .asBitmap()
                            .error(weatherArtResourceId)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(LOG_TAG, "Error retrieving large icon from " + weatherArtResourceUrl, e);
                }
            }

            double maxTemp = mCursor.getDouble(COL_WEATHER_MAX_TEMP);
            String formattedMaxTemperature = Utility.formatTemperature(mContext, maxTemp, Utility.isMetric(mContext));
            double minTemp = mCursor.getDouble(COL_WEATHER_MIN_TEMP);
            String formattedMinTemperature = Utility.formatTemperature(mContext, minTemp, Utility.isMetric(mContext));
            String weatherDesc = mCursor.getString(COL_WEATHER_DESC);
            String cityName = mCursor.getString(COL_CITY_NAME);
            long weatherDate = mCursor.getLong(COL_WEATHER_DATE);
            String weatherDateText = Utility.getFriendlyDayString(mContext, weatherDate);
            long dateInMillis = mCursor.getLong(COL_WEATHER_DATE);

            // We construct a remote views item based on our widget item xml file, and set the
            // text based on the position.
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_item);

            // Add the data to the RemoteViews
            if (weatherArtImage != null) {
                rv.setImageViewBitmap(R.id.widget_list_item_icon, weatherArtImage);
            } else {
                rv.setImageViewResource(R.id.widget_list_item_icon, weatherArtResourceId);
            }
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(rv, weatherDesc);
            }
            rv.setTextViewText(R.id.widget_list_item_high_textview, formattedMaxTemperature);
            rv.setTextViewText(R.id.widget_list_item_low_textview, formattedMinTemperature);
            rv.setTextViewText(R.id.widget_list_item_forecast_textview, weatherDesc);
            if (position == 0) {
                //Display the Day as "Today" for first item of tablet
                rv.setTextViewText(R.id.widget_list_item_date_textview, "Today");
            } else {
                //Display the Data as friendly date for rest
                rv.setTextViewText(R.id.widget_list_item_date_textview, weatherDateText);
            }

            // Next, we set a fill-intent which will be used to fill-in the pending intent template
            // which is set on the collection view in StackWidgetProvider.
            final Intent fillInIntent = new Intent();
            String locationSetting = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, dateInMillis);
            fillInIntent.setData(weatherUri);
            rv.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
            // You can do heaving lifting in here, synchronously. For example, if you need to
            // process an image, fetch something from the network, etc., it is ok to do it here,
            // synchronously. A loading view will show up in lieu of the actual contents in the
            // interim.
            try {
                System.out.println("Loading view " + position);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Return the remote views object.
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            // You can create a custom loading view (for instance when getViewAt() is slow.) If you
            // return null here, you will get the default loading view.
            return new RemoteViews(getPackageName(), R.layout.widget_detail_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position))
                return mCursor.getLong(COL_WEATHER_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}
