package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by lakshay on 29/6/16.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Intent intent = new Intent(context, StockWidgetIntentService.class);
        intent.setAction(MyStocksActivity.ACTION_DATA_UPDATE);
        context.startService(intent);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Intent intent = new Intent(context, StockWidgetIntentService.class);
        intent.setAction(MyStocksActivity.ACTION_DATA_UPDATE);
        context.startService(intent);    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (MyStocksActivity.ACTION_DATA_UPDATE.equals(intent.getAction())) {
            intent = new Intent(context, StockWidgetIntentService.class);
            intent.setAction(MyStocksActivity.ACTION_DATA_UPDATE);
            context.startService(intent);        }
    }
}
