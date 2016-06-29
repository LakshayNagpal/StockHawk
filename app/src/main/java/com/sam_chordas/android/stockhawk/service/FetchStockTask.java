package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;

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

/**
 * Created by lakshay on 29/6/16.
 */
public class FetchStockTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchStockTask.class.getSimpleName();
    Context context;

    public FetchStockTask(Context context){ this.context = context;}

    private void getstockdatafromjson(String jsonstring) throws JSONException{

       try {
           LineChartView lineChartView = null;
           JSONArray resultsArray = null;
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

           lineChartView = (LineChartView) lineChartView.findViewById(R.id.linechart);
           LineSet dataset = new LineSet(labels, closeprice);
           lineChartView.setAxisThickness(3);
           lineChartView.addData(dataset);
           //lineChartView.setYAxis(false);
           lineChartView.setXLabels(AxisController.LabelPosition.OUTSIDE);
           lineChartView.setYLabels(AxisController.LabelPosition.INSIDE);
           lineChartView.show();

       }catch (JSONException e){
           Log.e(LOG_TAG, e.getMessage(), e);
           e.printStackTrace();
       }
    }

    @Override
    protected Void doInBackground(String... params) {

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
            getstockdatafromjson(jsonstring);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedURLException", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Error", e);
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
        return null;
    }
}
