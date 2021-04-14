package edu.csi.niu.z1818828.stocktick.ui.stock;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.adapters.NewsAdapter;
import edu.csi.niu.z1818828.stocktick.objects.Article;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

public class StockActivity extends AppCompatActivity {
    private TextView textViewStockPrice;
    private TextView textViewDayChange;
    private TextView textViewDayChangePercent;
    private TextView textViewDate;
    private TextView textViewOpenValue;
    private TextView textViewDayRangeValue;
    private TextView textViewVolumeValue;
    private TextView textViewCloseValue;
    private TextView textViewWeekRangeValue;
    private TextView textViewPercRangeValue;
    private RecyclerView recyclerViewNews;
    private LineChart lineChart;
    private BarChart barChart;
    private Button buttonWatchlist;

    //Create objects to store json results
    JSONObject jsonStock;
    JSONObject jsonStockData;
    JSONObject jsonNews;

    //Create arraylists to store stock data
    ArrayList<String> dateArray = new ArrayList<>();
    ArrayList<Float> volumeArray = new ArrayList<>();
    ArrayList<Float> priceArray = new ArrayList<>();

    //Create a default stock object
    Stock stock = new Stock();

    NewsAdapter newsAdapter;

    private String stockSymbol;
    private String stockName;

    List<Article> articleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);


        /*
        TODO
        Fix watchlist
        Finish stock page
        Fiz day:)

        Implement news page
        Mivers page???

        Modify watchlist stock xml
        Search results??


         */

        //Bind the views
        textViewStockPrice = findViewById(R.id.textViewStockPrice);
        textViewDayChange = findViewById(R.id.textViewDayChange);
        textViewDayChangePercent = findViewById(R.id.textViewDayChangePercent);
        textViewDate = findViewById(R.id.textViewDate);
        textViewOpenValue = findViewById(R.id.textViewOpenValue);
        textViewDayRangeValue = findViewById(R.id.textViewDayRangeValue);
        textViewVolumeValue = findViewById(R.id.textViewVolumeValue);
        textViewCloseValue = findViewById(R.id.textViewCloseValue);
        textViewWeekRangeValue = findViewById(R.id.textViewWeekRangeValue);
        textViewPercRangeValue = findViewById(R.id.textViewPercRangeValue);

        ActionBar toolbar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Bind recycler view
        recyclerViewNews = findViewById(R.id.recyclerViewNewsFeed);

        //Create the adapter
        newsAdapter = new NewsAdapter(this, articleList);
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //Set the adapter and refresh
        recyclerViewNews.setAdapter(newsAdapter);
        newsAdapter.notifyDataSetChanged();

        //Create listener for the add to watchlist button
        buttonWatchlist = findViewById(R.id.buttonAddToWatchlist);
        buttonWatchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(stockSymbol, stockSymbol);
                Toast.makeText(v.getContext(), "Added to the watchlist", Toast.LENGTH_SHORT).show();
            }
        });

        //Get the stock info
        try {
            Intent intent = getIntent();
            //stockName = (intent.getStringExtra("stockName")).toUpperCase();
            stockSymbol = intent.getStringExtra("stockSymbol").toUpperCase();

            //retrieveStock();
            retrieveStockData();
            retrieveNews();

            Thread.sleep(1500);
            //wait();
            //parseJSONStockObject();
            parseJSONStockDataObject();

            textViewStockPrice.setText(stock.formatPrice(stock.getPrice()));
            textViewOpenValue.setText(stock.formatPrice(stock.getOpenPrice()));
            textViewCloseValue.setText(stock.formatPrice(stock.getClosePrice()));
            textViewDate.setText(stock.formatDate(stock.getDate()));
            //textViewStockPrice.setText( Double.toString( stock.getDayLow() ) );
            textViewVolumeValue.setText(stock.prettifyVolume());
            //textViewDayChange.setText( Double.toString( stock.get() ) );
            textViewDayChangePercent.setText(Double.toString(stock.getRange()));

            newsAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Set the title of the page
        setTitle(stockSymbol);
        getSupportActionBar().setSubtitle(stockName);


        //Setup the charts
        setChartPrice();

        setChartVolume();

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void parseJSONStockObject() {
        try {
            JSONObject item = jsonStock.getJSONObject("Global Quote");

            String open = item.getString("02. open");
            String high = item.getString("03. high");
            String low = item.getString("04. low");
            String price = item.getString("05. price");
            String volume = item.getString("06. volume");
            String change = item.getString("09. change");
            String changePct = item.getString("10. change percent");

            stock.setOpenPrice(Double.valueOf(open));
            stock.setDayHigh(Double.valueOf(high));
            stock.setDayLow(Double.valueOf(low));
            stock.setPrice(Double.valueOf(price));
            stock.setVolume(Double.valueOf(volume));
            //stock.set(Double.valueOf(open));

            //TODO fix this. changepct is "31.000%" not actual number
            //stock.setRange(Double.valueOf(changePct));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseJSONStockDataObject() {
        try {
            Boolean first = true;
            JSONObject item = jsonStockData.getJSONObject("Time Series (Daily)");

            Iterator<?> keys = item.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (item.get(key) instanceof JSONObject) {
                    JSONObject object = new JSONObject(item.get(key).toString());

                    //Add date to date array
                    String date = key;
                    dateArray.add(date);

                    //Add close to price array
                    String close = object.getString("4. close");
                    priceArray.add(Float.valueOf(close));

                    //Add volume to volume array
                    String volume = object.getString("5. volume");
                    volumeArray.add(Float.valueOf(volume));

                    //Set stock data to most recent
                    if (first) {
                        String open = object.getString("1. open");
                        String high = object.getString("2. high");
                        String low = object.getString("3. low");
                        String price = object.getString("4. close");

                        stock.setDate(date);
                        stock.setOpenPrice(Double.valueOf(open));
                        stock.setClosePrice(Double.valueOf(price));
                        stock.setDayHigh(Double.valueOf(high));
                        stock.setDayLow(Double.valueOf(low));
                        stock.setPrice(Double.valueOf(price));
                        stock.setVolume(Double.valueOf(volume));

                        first = false;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URL generateStockUrl(String query) {
        String key = getResources().getString(R.string.alphaVantage);

        try {
            String url = "https://www.alphavantage.co/query?function=" + query + "&symbol=" + stockSymbol + "&apikey=" + key;
            System.out.println("URL: " + url);

            String furl = url + URLEncoder.encode(stockSymbol, "UTF-8");

            return new URL(furl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void retrieveStock() {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                for (int i = 0; i < 12; i++) {
                    if (!retrieved) {
                        try {
                            connection = (HttpURLConnection) generateStockUrl("GLOBAL_QUOTE").openConnection();
                            int response = connection.getResponseCode();

                            if (response == HttpURLConnection.HTTP_OK) {
                                StringBuilder builder = new StringBuilder();

                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                } catch (IOException e) {
//                            Toast.makeText(this, "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                jsonStock = new JSONObject(builder.toString());
                                retrieved = true;
                                //this.notify();

                                Log.i("JSONStock", String.valueOf(jsonStock));
                            } else {
//                        Toast.makeText(this, "Heel", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            try {
                                wait(5000);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        } finally {
                            connection.disconnect();
                        }
                    }
                }
            }
        }).start();
    }

    private void retrieveStockData() {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                for (int i = 0; i < 12; i++) {
                    if (!retrieved) {
                        try {
                            connection = (HttpURLConnection) generateStockUrl("TIME_SERIES_DAILY").openConnection();
                            int response = connection.getResponseCode();

                            if (response == HttpURLConnection.HTTP_OK) {
                                StringBuilder builder = new StringBuilder();

                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                jsonStockData = new JSONObject(builder.toString());
                                retrieved = true;

                                //this.notify(); //Allow the main thread to continue

                                Log.i("JSONStockData", String.valueOf(jsonStockData));
                            } else {
                                Toast.makeText(getApplicationContext(), "Could not connect to the API", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        } finally {
                            connection.disconnect();
                        }
                    }
                }
            }
        }).start();
    }

    private void retrieveNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean retrieved = false;

                try {
                    String key = getResources().getString(R.string.newsAPI);

                    NewsApiClient newsApiClient = new NewsApiClient(key);
                    newsApiClient.getEverything(
                            new EverythingRequest.Builder()
                                    .q(stockSymbol)
                                    .build(),
                            new NewsApiClient.ArticlesResponseCallback() {
                                @Override
                                public void onSuccess(ArticleResponse articleResponse) {
                                    for (int i = 0; i < articleResponse.getArticles().size(); i++) {
                                        List<com.kwabenaberko.newsapilib.models.Article> articles =
                                                (List<com.kwabenaberko.newsapilib.models.Article>) articleResponse.getArticles();

                                        articleList.add(new Article(
                                                articles.get(i).getTitle(),
                                                articles.get(i).getSource().getName(),
                                                articles.get(i).getUrl(),
                                                articles.get(i).getUrlToImage()
                                        ));

                                        newsAdapter.notifyDataSetChanged();

                                        Log.i("News", articles.get(i).getTitle());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    Toast.makeText(getApplicationContext(), "Could not connect to the API", Toast.LENGTH_SHORT).show();
                                    Log.e("JSONNews", String.valueOf(throwable));
                                }
                            }
                    );

                } catch (Exception e) {
                    e.printStackTrace();

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                } finally {
                    //connection.disconnect();
                }

            }
        }).start();
    }

    private void setChartPrice() {
        //Setup the chart
        {
            lineChart = findViewById(R.id.chart);

            // disable description text
            lineChart.getDescription().setEnabled(false);

            // enable touch gestures
            lineChart.setTouchEnabled(true);

            // set listeners
            //lineChart.setOnChartValueSelectedListener((OnChartValueSelectedListener) this);
            lineChart.setDrawGridBackground(false);

            // enable scaling and dragging
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            lineChart.setPinchZoom(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            xAxis.setDrawLabels(true);

            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            yAxis.setDrawLabels(true);

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
//            yAxis.setAxisMaximum(200f);
//            yAxis.setAxisMinimum(0);
        }
        Legend legend;
        {
            legend = lineChart.getLegend();
            legend.setEnabled(false);
        }

        //Add data
        setDataChart(priceArray.size(), 100);
        lineChart.animateX(1000);
    }

    private void setChartVolume() {
        //Setup the chart
        {
            barChart = findViewById(R.id.chartVol);

            // disable description text
            barChart.getDescription().setEnabled(false);

            // enable touch gestures
            barChart.setTouchEnabled(true);

//            barChart.setBackgroundColor(R.color.colorStockChartGradient);

            // set listeners
            //lineChart.setOnChartValueSelectedListener((OnChartValueSelectedListener) this);
            barChart.setDrawGridBackground(false);

//            // create marker to display box when values are selected
//            MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//
//            // Set the marker to the chart
//            mv.setChartView(chart);
//            chart.setMarker(mv);

            // enable scaling and dragging
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
//            barChart.setPinchZoom(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = barChart.getXAxis();

            xAxis.setDrawLabels(false);
//            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//
//            // vertical grid lines
//            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = barChart.getAxisLeft();

            yAxis.setDrawLabels(true);
            yAxis.setLabelCount(3);
//            yAxis.label

            yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);


            // disable dual axis (only use LEFT axis)
            barChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMinimum(0);
        }
        Legend legend;
        {
            legend = barChart.getLegend();
            legend.setEnabled(false);
        }

        //Add data
        setDataVolume(volumeArray.size(), 100);
        barChart.animateX(1000);
    }

    private void setDataChart(int count, float range) {
        ArrayList<Entry> values = new ArrayList<>();

        if (count != 0) {
            int i;

            if (count > 90)
                i = 90;
            else
                i = count;


            for (int k = 0; i >= 1; i--, k++) {
                //set value
                try {
                    float val = (float) priceArray.get(i);
                    values.add(new Entry(k, val));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "NO DATA TO ADD!", Toast.LENGTH_SHORT).show();
        }

        LineDataSet set;

        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set = new LineDataSet(values, "Data Set");

            // black lines and points
            set.setColor(R.color.colorStockChartGradient);
            set.setCircleColor(R.color.colorStockChartGradient);

            // line thickness
            set.setLineWidth(1f);

            //Disable points
            set.setDrawCircles(false);
            set.setDrawValues(false);

            // customize legend entry
            set.setFormLineWidth(1f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(15.f);

            // draw selection line as dashed
//            set.enableDashedHighlightLine(10f, 5f, 0f);
            set.disableDashedHighlightLine();


            // set the filled area
            set.setDrawFilled(true);
            set.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return lineChart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_chart);
                set.setFillDrawable(drawable);
            } else {
                set.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
        }
    }

    private void setDataVolume(int count, float range) {
        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = count - 1, k = 0; i >= 1; i--, k++) {
            //set value
            try {
                float val = (float) volumeArray.get(i);
                values.add(new BarEntry(k, val));
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        BarDataSet set;

        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set = new BarDataSet(values, "Data Set");

            // black lines and points
            set.setColor(R.color.colorStockChartGradient);
            set.setBarBorderColor(R.color.colorStockChartGradient);

            // customize legend entry
            set.setFormLineWidth(1f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(15.f);


            set.setDrawValues(false);
            set.setBarShadowColor(R.color.colorStockChartGradient);
            set.setColors(R.color.colorStockChartGradient);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set); // add the data sets

            // create a data object with the data sets
            BarData data = new BarData(dataSets);

            // set data
            barChart.setData(data);
        }
    }
}