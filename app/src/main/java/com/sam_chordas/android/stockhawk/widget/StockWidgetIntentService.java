package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by lakshay on 29/6/16.
 */
public class StockWidgetIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public StockWidgetIntentService() {
        super("StockWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));

        Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[] { QuoteColumns.SYMBOL + ", " + QuoteColumns.BIDPRICE
                + ", " + QuoteColumns.CHANGE + ", " + QuoteColumns.PERCENT_CHANGE
                + ", " + QuoteColumns.ISUP}, QuoteColumns.ISCURRENT + "= ?",
                new String[] {"1"}, null);

        if (data == null || data.getCount() < 2) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        for(int appWidgetId: appWidgetIds){
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);

            views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
            views.setTextViewText(R.id.change, data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
            views.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));

            Intent i = new Intent(this, MyStocksActivity.class);
            PendingIntent pendingintent = PendingIntent.getActivity(this, 0, i, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingintent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
