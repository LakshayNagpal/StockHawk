package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class GraphActivity extends Activity {

    private static String LOG_TAG = GraphActivity.class.getSimpleName();
    public static final String ss = "selected_symbol";
    private LineChartView lineChartView;
    private Cursor data;
    private String selectedsymbol;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        Intent intent = getIntent();
        if(intent!=null){
            selectedsymbol = intent.getExtras().getString("selected_symbol");
            Log.v(LOG_TAG,selectedsymbol);
        }
//        else{
//            Log.v(LOG_TAG,"No intent data found");
//        }

            lineChartView = (LineChartView) findViewById(R.id.linechart);
        data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[] {QuoteColumns.BIDPRICE}, QuoteColumns.SYMBOL + "=?",
                new String[]{selectedsymbol}, null);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(LOG_TAG, "on start called");

        int count = data.getCount();
        Log.v(LOG_TAG, "value of count" + String.valueOf(count));

        if (data == null || count < 2) {
            ((TextView) findViewById(R.id.noDataTextView)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) findViewById(R.id.noDataTextView)).setVisibility(View.GONE);

            if (!data.moveToFirst()) {
                data.close();
                return;
            }

            String labels[] = new String[count];
            float price[] = new float[count];
            i = 0;
            float min = 9999999;
            float max = 0;
            while (!data.isAfterLast()) {
                float bidprice = data.getFloat(0);
                if (bidprice < min)
                    min = bidprice;

                if (bidprice > max)
                    max = bidprice;

                Log.v(LOG_TAG, String.valueOf(bidprice));
                price[i] = bidprice;
                labels[i] = "";
                if (i == price.length - 1) {
                    break;
                }
                data.moveToNext();
                ++i;
                --count;
            }
            Log.v(LOG_TAG, "value of min" + String.valueOf(min));
            Log.v(LOG_TAG, "value of max" + String.valueOf(max));

            lineChartView.setAxisBorderValues((int)Math.floor(min), (int)Math.ceil(max));
            LineSet dataset = new LineSet(labels, price);
            dataset.setDotsColor(getResources().getColor(R.color.yellow_500));
            dataset.setColor(getResources().getColor(R.color.light_blue_400));
            lineChartView.setAxisColor(getResources().getColor(R.color.white));
            lineChartView.setLabelsColor(getResources().getColor(R.color.yellow_500));
            lineChartView.setAxisThickness(3);
            lineChartView.addData(dataset);
            //lineChartView.setYAxis(false);
            lineChartView.setXLabels(AxisController.LabelPosition.OUTSIDE);
            lineChartView.setYLabels(AxisController.LabelPosition.INSIDE);
            lineChartView.show();
        }
    }
}
