package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GraphActivity extends AppCompatActivity {

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
            //((TextView) findViewById(R.id.noDataTextView)).setVisibility(View.VISIBLE);
            FetchStockTask stock = new FetchStockTask(getBaseContext());
            stock.execute(selectedsymbol);
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

    public class FetchStockTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchStockTask.class.getSimpleName();
        Context context;

        public FetchStockTask(Context context){ this.context = context;}

//        private void getstockdatafromjson(String jsonstring) throws JSONException {
//
//            try {
//                JSONArray resultsArray = null;
//                JSONObject stockobject = new JSONObject(jsonstring);
//                stockobject = stockobject.getJSONObject("query");
//
//                resultsArray = stockobject.getJSONObject("results").getJSONArray("quote");
//                float closeprice[] = new float[resultsArray.length()];
//                String labels[] = new String[resultsArray.length()];
//
//                if (resultsArray != null && resultsArray.length() != 0) {
//                    for (int i = 0; i < resultsArray.length(); i++) {
//                        stockobject = resultsArray.getJSONObject(i);
//                        String s = stockobject.getString("Close");
//                        closeprice[i] = Float.valueOf(s);
//                        labels[i] = " ";
//                    }
//                }
//            }catch (JSONException e){
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlconnection = null;
            BufferedReader reader = null;
            String jsonstring = null;

            Log.v(LOG_TAG, "the parameter is " + params[0]);

            try{
                StringBuilder myurl = new StringBuilder();
                myurl.append("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22");

                myurl.append(params[0]);
                myurl.append("%22%20and%20startDate%20%3D%20%222016-04-11%22%20and%20endDate%20%3D%20%" +
                        "222016-05-10%22&format=json&diagnostics=true&env=store" +
                        "%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

                String tyu = myurl.toString();
                URL url = new URL(tyu);
                Log.v(LOG_TAG, "Built url" + url);
                urlconnection = (HttpURLConnection) url.openConnection();
                urlconnection.setRequestMethod("GET");
                urlconnection.connect();

                InputStream input = urlconnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(input == null){
                    // Toast.makeText(context, "Please check your Internet Connectivity", Toast.LENGTH_SHORT).show();
                    return null;
                }
                // Toast.makeText(context, "Loading... Please Wait", Toast.LENGTH_SHORT).show();
                reader = new BufferedReader(new InputStreamReader(input));
                String line;

                while((line = reader.readLine())!=null){
                    buffer.append(line+"\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                jsonstring = buffer.toString();
                Log.v(LOG_TAG, "Stock json string" + jsonstring);
                //getstockdatafromjson(jsonstring);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "MalformedURLException", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IO Error", e);
                e.printStackTrace();
            } finally {
                if(urlconnection!=null){
                    urlconnection.disconnect();
                }
                if(reader!=null){
                    try{
                        reader.close();
                    }catch(final IOException e){
                        Log.e(LOG_TAG,"Error closing stream", e);
                    }
                }
            }
            return jsonstring;
        }

        @Override
        protected void onPostExecute(String jsonstring){
            try {
                JSONArray resultsArray;
                JSONObject stockobject = new JSONObject(jsonstring);
                stockobject = stockobject.getJSONObject("query");

                resultsArray = stockobject.getJSONObject("results").getJSONArray("quote");
                float closeprice[] = new float[resultsArray.length()];
                String labels[] = new String[resultsArray.length()];

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        stockobject = resultsArray.getJSONObject(i);
                        String s = stockobject.getString("Close");
                        closeprice[i] = Float.valueOf(s);
                        labels[i] = " ";
                    }
                }

                float min = 9999999;
                float max = 0;

                for(int i = 0;i<closeprice.length;++i){
                    float x = closeprice[i];
                    if(x<min)
                        min = x;

                    if(x>max)
                        max = x;
                }

                lineChartView.setAxisBorderValues((int)Math.floor(min), (int)Math.ceil(max));
                LineSet dataset = new LineSet(labels, closeprice);
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
