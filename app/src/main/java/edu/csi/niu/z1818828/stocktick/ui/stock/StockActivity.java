package edu.csi.niu.z1818828.stocktick.ui.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.adapters.NewsAdapter;
import edu.csi.niu.z1818828.stocktick.objects.Article;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

import static edu.csi.niu.z1818828.stocktick.R.color.colorAccent;
import static edu.csi.niu.z1818828.stocktick.R.color.colorNegative;
import static edu.csi.niu.z1818828.stocktick.R.color.colorPositive;
import static edu.csi.niu.z1818828.stocktick.R.color.colorPrimary;
import static edu.csi.niu.z1818828.stocktick.R.color.colorStockChartGradient;

public class StockActivity extends AppCompatActivity {
    private TextView textViewStockPrice;
    private TextView textViewDayChange;
    private TextView textViewDayChangePercent;
    private TextView textViewDate;
    private TextView textViewOpenValue;
    private TextView textViewDayRangeValue;
    private TextView textViewVolumeValue;
    private TextView textViewCloseValue;
    private TextView textViewHighValue;
    private TextView textViewLowValue;
    private RecyclerView recyclerViewNews;
    private LineChart lineChart;
    private BarChart barChart;
    private ImageButton buttonWatchlist;

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

//    private String stockSymbol;
//    private String stockName;

    private String lastHigh;
    private String lastLow;
    private String lastOpen;
    private String lastClose;

    private String stockChange = "--";
    private String stockChangePercent = "--";

    private boolean loaded = false;

    private NewsAdapter newsAdapter;

    private List<Article> articleList = new ArrayList<>();
    private List<Stock> watchlist;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        //Load the watchlist
        loadData();

        //Bind the views
        textViewStockPrice = findViewById(R.id.textViewStockPrice);
        textViewDayChange = findViewById(R.id.textViewDayChange);
        textViewDayChangePercent = findViewById(R.id.textViewDayChangePercent);
        textViewDate = findViewById(R.id.textViewChange);
        textViewOpenValue = findViewById(R.id.textViewOpenValue);
        textViewVolumeValue = findViewById(R.id.textViewVolumeValue);
        textViewHighValue = findViewById(R.id.textViewHighValue);
        textViewLowValue = findViewById(R.id.textViewLowValue);

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
        buttonWatchlist.setOnClickListener(toggleWatchlistButton);

        //Start a new thread to retrieve data
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get the stock info
                try {
                    Intent intent = getIntent();
                    try {
                        stock.setStockName((intent.getStringExtra("stockName")).toUpperCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    stock.setSymbol(intent.getStringExtra("stockSymbol").toUpperCase());

                    //Load the data
                    loadStockData();

                    Date date = new Date();
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    String today = df.format(date);

                    //If the user does not have stock, get it
                    if (!hasStock()) {
                        retrieveStock();
                    }
                    //If they do have the stock, is it up to date?
                    else {
                        if (stock.getDate() != null) {
                            if (stock.getDate().compareTo(today) != 0) {
                                retrieveStock();
                            }
                        }
                    }

                    //Get the news
                    retrieveNews();

                    //Retrieve the UI thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            textViewStockPrice.setText(stock.formatPrice(stock.getPrice()));
                            textViewOpenValue.setText(stock.formatPrice(stock.getOpenPrice()));
                            textViewHighValue.setText(stock.formatPrice(stock.getDayHigh()));
                            textViewLowValue.setText(stock.formatPrice(stock.getDayLow()));
                            textViewDate.setText(stock.formatDateDay(stock.getDate()));
                            textViewVolumeValue.setText(stock.prettifyVolume());


                            if (stock.getChange() < 0) {
                                textViewDayChange.setTextColor(colorNegative);
                                textViewDayChangePercent.setTextColor(colorNegative);
                            } else {
                                textViewDayChange.setText("+" + stock.formatChange(stock.getChange()));
                                textViewDayChangePercent.setText("+" + stock.formatChangePercentage(stock.getChangePct()));
                                textViewDayChange.setTextColor(colorPositive);
                                textViewDayChangePercent.setTextColor(colorPositive);
                            }

                            newsAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Retrieve the UI thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Set the icon status
                        setWatchlistIcon();

                        //Set the title of the page
                        setTitle(stock.getSymbol());
                        getSupportActionBar().setSubtitle(stock.getStockName());

                        //Setup the charts
                        setChartPrice();
                        setChartVolume();

                        setLoaded(true);
                    }
                });
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    View.OnClickListener toggleWatchlistButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (loaded) {
                if (hasStock()) {
                    removeFromWatchlist();
                    setWatchlistIcon();
                } else {
                    saveToWatchlist();
                    setWatchlistIcon();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setWatchlistIcon() {
        //Set button
        if (hasStock()) {
            buttonWatchlist.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_delete_24));
            buttonWatchlist.setBackgroundColor(getResources().getColor(colorNegative));
        } else {
            buttonWatchlist.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_24));
            buttonWatchlist.setBackgroundColor(getResources().getColor(colorAccent));
        }
    }


    private void retrieveStock() throws InterruptedException {
        //Retrieve the stock info
        retrieveStockData();
        Thread.sleep(1500);
        parseJSONStockDataObject();
        calculateChange();
    }

    private void loadStockData() {
        if (watchlist.size() > 0) {
            String temp = stock.getSymbol();
            for (int i = 0; i < watchlist.size(); i++) {
                String key = watchlist.get(i).getSymbol();
                if (key.compareTo(temp) == 0) {
                    stock = watchlist.get(i);
                }
            }
        }
    }

    private int colorMode() {
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                return getResources().getColor(R.color.textColorDark);
            case Configuration.UI_MODE_NIGHT_NO:
                return getResources().getColor(R.color.textColorLight);
            default:
                return getResources().getColor(R.color.textColorGray);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setLoaded(boolean status) {
        loaded = status;
    }

    private void removeFromWatchlist() {
        if (watchlist.size() > 0) {
            String temp = stock.getSymbol();
            for (int i = 0; i < watchlist.size(); i++) {
                String key = watchlist.get(i).getSymbol();
                if (key.compareTo(temp) == 0) {
                    watchlist.remove(i);
                    saveData();
                    Toast.makeText(this, stock.getSymbol() + " removed from watchlist", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    private void saveToWatchlist() {
        Log.i("STOCK ADDED", stock.getSymbol());
        if (!hasStock()) {
            watchlist.add(stock);
        }

        saveData();
        Toast.makeText(this, stock.getSymbol() + " added to watchlist", Toast.LENGTH_SHORT).show();
    }

    private boolean hasStock() {
        if (watchlist.size() > 0) {
            String temp = stock.getSymbol();
            for (int i = 0; i < watchlist.size(); i++) {
                String key = watchlist.get(i).getSymbol();
                if (key.compareTo(temp) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(watchlist);
        editor.putString("watchlist", json);

        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);

        Gson gson = new Gson();

        String json = sharedPreferences.getString("watchlist", null);

        Type type = new TypeToken<ArrayList<Stock>>() {
        }.getType();

        watchlist = gson.fromJson(json, type);

        if (watchlist == null) {
            watchlist = new ArrayList<>();
        }
    }

    private void parseJSONStockDataObject() {
        try {
            int counter = 0;
            JSONObject item = jsonStockData.getJSONObject("Time Series (Daily)");

            Iterator<?> keys = item.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (item.get(key) instanceof JSONObject) {
                    JSONObject object = new JSONObject(item.get(key).toString());

                    //Add date to date array
                    String date = key;
                    dateArray.add(0, date);

                    //Add close to price array
                    String close = object.getString("4. close");
                    priceArray.add(Float.valueOf(close));

                    //Add volume to volume array
                    String volume = object.getString("5. volume");
                    volumeArray.add(Float.valueOf(volume));

                    //Set stock data to most recent
                    if (counter == 0) {
                        String open = object.getString("1. open");
                        String high = object.getString("2. high");
                        String low = object.getString("3. low");
                        String price = object.getString("4. close");

                        stock.setDate(date);
                        stock.setOpenPrice(Double.parseDouble(open));
                        stock.setPrice(Double.parseDouble(price));
                        stock.setDayHigh(Double.parseDouble(high));
                        stock.setDayLow(Double.parseDouble(low));
                        stock.setPrice(Double.parseDouble(price));
                        stock.setVolume(Double.parseDouble(volume));
                    } else if (counter == 1) {
                        lastOpen = object.getString("1. open");
                        lastClose = object.getString("2. high");
                        lastHigh = object.getString("3. low");
                        lastLow = object.getString("4. close");
                    }

                    counter++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateChange() {
        double close = stock.getPrice();
        double prevOpen = stock.getOpenPrice();

        stock.setChange(close - prevOpen);
        stock.setChangePct(((close - prevOpen) / prevOpen) * 100);
    }

    private URL generateStockUrl(String query) {
        String key = getResources().getString(R.string.alphaVantage);

        try {
            String url = "https://www.alphavantage.co/query?function=" + query + "&symbol=" + stock.getSymbol() + "&apikey=" + key;
            System.out.println("URL: " + url);

            String furl = url + URLEncoder.encode(stock.getSymbol(), "UTF-8");

            return new URL(furl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
                                    .q(stock.getSymbol())
                                    .sortBy("publishedAt")
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

                                        //Log.i("News", articles.get(i).getTitle());
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

            // force pinch zoom along both axis
            lineChart.setPinchZoom(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            xAxis.setDrawLabels(true);
            xAxis.setLabelCount(10);
            xAxis.setTextColor(colorMode());

            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return stock.formatDate(dateArray.get((int) value));
                }
            });

            xAxis.setLabelRotationAngle(45);

            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisRight();

            //Setup labels
            yAxis.setDrawLabels(true);
            yAxis.setTextColor(colorMode());

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(true);
            lineChart.getAxisLeft().setEnabled(false);

            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return stock.formatPriceLabel(value);
                }
            });

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);
        }
        Legend legend;
        {
            legend = lineChart.getLegend();
            legend.setDrawInside(true);
            legend.setEnabled(true);
            legend.setTextColor(colorMode());

            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
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

            // set listeners
            barChart.setDrawGridBackground(false);

            // enable scaling and dragging
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = barChart.getXAxis();

            xAxis.setDrawLabels(false);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = barChart.getAxisRight();

            yAxis.setDrawLabels(true);
            yAxis.setLabelCount(3);
            yAxis.setTextColor(colorMode());

            barChart.getAxisRight().setEnabled(true);
            barChart.getAxisLeft().setEnabled(false);

            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return stock.prettifyVolumeLabel(value);
                }
            });

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
            set = new LineDataSet(values, "3 Month Chart");

            // black lines and points
            set.setColor(colorStockChartGradient);
            set.setCircleColor(colorStockChartGradient);

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
            set.disableDashedHighlightLine();

            set.setHighLightColor(getResources().getColor(colorNegative));

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
            set.setColors(new int[]{colorStockChartGradient, colorStockChartGradient}, this);
            set.notifyDataSetChanged();
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set = new BarDataSet(values, "Data Set");

            // customize legend entry
            set.setFormLineWidth(1f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(15.f);

            set.setDrawValues(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set); // add the data sets

            // create a data object with the data sets
            BarData data = new BarData(dataSets);

            // set data
            barChart.setData(data);
        }
    }
}