package edu.csi.niu.z1818828.stocktick.ui.stock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import edu.csi.niu.z1818828.stocktick.helper.StockHelper;
import edu.csi.niu.z1818828.stocktick.objects.Article;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

import static edu.csi.niu.z1818828.stocktick.R.color.colorAccent;
import static edu.csi.niu.z1818828.stocktick.R.color.colorNegative;
import static edu.csi.niu.z1818828.stocktick.R.color.colorPositive;
import static edu.csi.niu.z1818828.stocktick.R.color.colorStockChartGradient;

public class StockActivity extends AppCompatActivity {
    private final List<Article> articleList = new ArrayList<>();

    //Create object to store json results
    JSONObject jsonStockData;

    //Create arraylists to store stock data
    ArrayList<String> dateArray = new ArrayList<>();
    ArrayList<Float> volumeArray = new ArrayList<>();
    ArrayList<Float> priceArray = new ArrayList<>();

    //Create a default stock object
    Stock stock = new Stock();
    private TextView textViewStockPrice;
    private TextView textViewDayChange;
    private TextView textViewDayChangePercent;
    private TextView textViewDate;
    private TextView textViewOpenValue;
    private TextView textViewVolumeValue;
    private TextView textViewHighValue;
    private TextView textViewLowValue;
    private TextView textViewStatus;
    private RecyclerView recyclerViewNews;
    private LineChart lineChart;
    private BarChart barChart;
    private ImageButton buttonWatchlist;
//    private Toolbar toolbar;

    //Temporary strings to hold previous day
    private String lastHigh;
    private String lastLow;
    private String lastOpen;
    private String lastClose;
    private boolean loaded = false;
    private int errorCode = 0; //If there was a connection error (1), if url error (2)
    private NewsAdapter newsAdapter;
    private List<Stock> watchlist;

    private StockHelper stockHelper;

    //Set a listener for the toggleWatchlistButton
    View.OnClickListener toggleWatchlistButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (loaded) {
                //If the user has the stock, remove it from the watchlist
                if (hasStock()) {
                    removeFromWatchlist();
                } else {
                    saveToWatchlist();
                }

                //Update the icon
                setWatchlistIcon();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        stockHelper = new StockHelper(this);

        //Bind the views
        textViewStockPrice = findViewById(R.id.textViewStockPrice);
        textViewDayChange = findViewById(R.id.textViewDayChange);
        textViewDayChangePercent = findViewById(R.id.textViewDayChangePercent);
        textViewDate = findViewById(R.id.textViewChange);
        textViewOpenValue = findViewById(R.id.textViewOpenValue);
        textViewVolumeValue = findViewById(R.id.textViewVolumeValue);
        textViewHighValue = findViewById(R.id.textViewHighValue);
        textViewLowValue = findViewById(R.id.textViewLowValue);
        textViewStatus = findViewById(R.id.textViewStatus);

        setStatusView();

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

        //Load the watchlist
        loadWatchlist();

        //Start a new thread to retrieve data
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get the stock info
                try {
                    Intent intent = getIntent();

                    stock.setSymbol(intent.getStringExtra("stockSymbol").toUpperCase());

                    //Attempt to get the stockName if it exists
                    try {
                        stock.setStockName((intent.getStringExtra("stockName")).toUpperCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Load the data
                    loadStockData();

                    //Get the date
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

                    calculateChange();

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

                            //Set the change
                            if (stock.getChange() < 0) {
                                textViewDayChange.setTextColor(getResources().getColor(colorNegative));
                                textViewDayChangePercent.setTextColor(getResources().getColor(colorNegative));
                            } else {
                                textViewDayChange.setText("+" + stock.formatChange(stock.getChange()));
                                textViewDayChangePercent.setText("+" + stock.formatChangePercentage(stock.getChangePct()));
                                textViewDayChange.setTextColor(getResources().getColor(colorPositive));
                                textViewDayChangePercent.setTextColor(getResources().getColor(colorPositive));
                            }

                            //Update the adapter
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

                        //Wait half a second if either arrays are null
                        for (int i = 0; i < 50; i++) {
                            if (priceArray.size() == 0 || volumeArray.size() == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        //Setup the charts
                        setChartPrice();
                        setChartVolume();

                        //Data has been loaded
                        setLoaded(true);
                        setStatusView();
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Set the retrieval status
     */
    private void setStatusView() {
        if (errorCode == 0) {
            if (loaded) {
                textViewStatus.setVisibility(View.GONE);
            } else {
                textViewStatus.setVisibility(View.VISIBLE);
            }
        } else {
            textViewStatus.setTextColor(getResources().getColor(R.color.colorNegative));

            switch (errorCode) {
                case 1: //Connection error
                    textViewStatus.setVisibility(View.VISIBLE);
                    textViewStatus.setText("Could not connect to the API");
                    break;
                case 2: //URL error
                    textViewStatus.setVisibility(View.VISIBLE);
                    textViewStatus.setText("URL error, the stock may not exist");
                    break;
                case 3: //JSON object error
                    textViewStatus.setVisibility(View.VISIBLE);
                    textViewStatus.setText("JSON error, the stock does not exist");
                    break;
            }
        }
    }

    /**
     * Set the watchlisticon based on stock's status in the watchlst
     */
    private void setWatchlistIcon() {
        try {
            //If user has stock, update view
            if (hasStock()) {
                buttonWatchlist.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_delete_24));
                buttonWatchlist.setBackgroundColor(getResources().getColor(colorNegative));
            } else {
                buttonWatchlist.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_24));
                buttonWatchlist.setBackgroundColor(getResources().getColor(colorAccent));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Intermediate that retrieves stock data, then parses it
     *
     * @throws InterruptedException
     */
    private void retrieveStock() throws InterruptedException {
        //Retrieve the stock info
        retrieveStockData();

        //Wait for it to be retrieved
        for (int i = 0; i < 15; i++) {
            if (jsonStockData == null) {
                Thread.sleep(100);
            } else {
                //Parse it
                parseJSONStockDataObject();
            }
        }

        //Calculate the change
        calculateChange();
    }

    /**
     * Retrieve the stock data from the watchlist if it exists
     */
    private void loadStockData() {
        //Make sure there's something in the watchlist
        if (watchlist != null) {
            if (watchlist.size() > 0) {
                String temp = stock.getSymbol();

                //Iterate through the watchlist
                for (int i = 0; i < watchlist.size(); i++) {
                    String key = watchlist.get(i).getSymbol();

                    //If the stock is equal to the stock in the watchlist,
                    // set the stock to the watchlist stock
                    if (key.compareTo(temp) == 0) {
                        stock = watchlist.get(i);
                    }
                }
            }
        }
    }

    /**
     * Determine if the user is using night mode or not
     *
     * @return - return the color value for the text
     */
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

    @Override
    protected void onPause() {
        super.onPause();
        deleteWatchlist();
        saveWatchlist();
    }

    /**
     * Set if the data has been loaded or not
     *
     * @param status
     */
    private void setLoaded(boolean status) {
        loaded = status;
    }

    /**
     * Remove the stock from the user watchlist
     */
    private void removeFromWatchlist() {
        //Ensure there is something in the watchlist to remove (there should be)
        if (watchlist.size() > 0) {
            String temp = stock.getSymbol();

            //Iterate through the watchlist
            for (int i = 0; i < watchlist.size(); i++) {
                String key = watchlist.get(i).getSymbol();

                //If the iterator stock is the same as the stock in activity, rmeove it
                if (key.compareTo(temp) == 0) {
                    watchlist.remove(i);

                    //Save the new watchlist
                    saveWatchlist();

                    //Notify the user the stock was removed
                    Toast.makeText(this, stock.getSymbol() + " removed from watchlist", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    /**
     * Save the stock to the watchlist
     */
    private void saveToWatchlist() {
        //Determine if the user has the stock already, if not add
        if (watchlist == null)
            watchlist = new ArrayList<>();

        if (!hasStock()) {
            watchlist.add(stock);
        }

        //Save the watchlist
        saveWatchlist();

        //Notify the user stock was added
        Toast.makeText(this, stock.getSymbol() + " added to watchlist", Toast.LENGTH_SHORT).show();
    }

    /**
     * Determine if the user has the stock from the activity
     *
     * @return
     */
    private boolean hasStock() {
        //Ensure there is something in the watchlist
        if (watchlist != null) {
            if (watchlist.size() > 0) {
                String temp = stock.getSymbol();

                //Iterate through the watchlist
                for (int i = 0; i < watchlist.size(); i++) {
                    String key = watchlist.get(i).getSymbol();

                    //If the stock exists, return true
                    if (key.compareTo(temp) == 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Save the watchlist
     */
    private void saveWatchlist() {
        if (watchlist != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Gson gson = new Gson();

            String json = gson.toJson(watchlist);
            editor.putString("watchlist", json);

            editor.apply();
        }
    }

    /**
     * Load the watchlist
     */
    private void loadWatchlist() {
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

    /**
     * Delete the watchlist
     */
    private void deleteWatchlist() {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("watchlist");
        editor.apply();
    }

    /**
     * Parse the object returned from the api call
     */
    private void parseJSONStockDataObject() {
        try {
            int counter = 0;
            JSONObject item = jsonStockData.getJSONObject("Time Series (Daily)");

            Iterator<?> keys = item.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();

                //If the item exists
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
                        //stockHelper.setStockName(stock);
                    }
                    //Set the data for the previous day data
                    else if (counter == 1) {
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
            errorCode = 3;
        }
    }

    /**
     * Calculate the difference between today's close and yesterday's open (since data is not live,
     * we can only determine the change between days
     */
    private void calculateChange() {
        double close = stock.getPrice();
        double prevOpen = stock.getOpenPrice();

        stock.setChange(close - prevOpen);
        stock.setChangePct(((close - prevOpen) / prevOpen) * 100);
    }

    /**
     * Generate the url to retrieve the symbol
     *
     * @param query - the stock symbol to retrieve data from
     * @return the url from which the data is retrieved
     */
    private URL generateStockUrl(String query) {
        String key = getResources().getString(R.string.alphaVantage);

        try {
            String url = "https://www.alphavantage.co/query?function=" + query + "&symbol=" + stock.getSymbol() + "&apikey=" + key;

            String furl = url + URLEncoder.encode(stock.getSymbol(), "UTF-8");

            return new URL(furl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve the stock data from the api
     */
    private void retrieveStockData() {
        //Start a background thread
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                //Try every 5 seconds for a minute to get the results
                for (int i = 0; i < 12; i++) {
                    if (!retrieved) {
                        try {
                            //Open the connection
                            connection = (HttpURLConnection) generateStockUrl("TIME_SERIES_DAILY").openConnection();
                            int response = connection.getResponseCode();

                            //If connection established
                            if (response == HttpURLConnection.HTTP_OK) {
                                StringBuilder builder = new StringBuilder();

                                //Read the data
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                //Set the json object
                                jsonStockData = new JSONObject(builder.toString());

                                //Data has been retrieved, stop retrieving
                                retrieved = true;

                                //Log results
                                Log.i("JSONStockData", String.valueOf(jsonStockData));
                            } else {
                                Toast.makeText(getApplicationContext(), "Could not connect to the API", Toast.LENGTH_SHORT).show();
                                errorCode = 1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            //Wait 5 seconds before trying again
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        } finally {
                            //Disconnect from the api
                            try {
                                connection.disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Retrieve the news related to the symbol from the api
     */
    private void retrieveNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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

                                        //Add the article to the list
                                        articleList.add(new Article(
                                                articles.get(i).getTitle(),
                                                articles.get(i).getSource().getName(),
                                                articles.get(i).getUrl(),
                                                articles.get(i).getUrlToImage()
                                        ));

                                        //Update the adapter
                                        newsAdapter.notifyDataSetChanged();
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
                }
            }
        }).start();
    }

    /**
     * Setup the price chart
     */
    private void setChartPrice() {
        //Setup the chart
        {
            //Bind the view
            lineChart = findViewById(R.id.chart);

            //disable description text
            lineChart.getDescription().setEnabled(false);

            //enable touch gestures
            lineChart.setTouchEnabled(true);

            //Disable background grid
            lineChart.setDrawGridBackground(false);

            //enable scaling and dragging
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);

            //force pinch zoom along both axis
            lineChart.setPinchZoom(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            //Enable the labels
            xAxis.setDrawLabels(true);

            //Create 10 labels
            xAxis.setLabelCount(10);

            //Match the color to UI mode
            xAxis.setTextColor(colorMode());

            //Format the xAxis
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return stock.formatDate(dateArray.get((int) value));
                }
            });

            //Set a label angle
            xAxis.setLabelRotationAngle(45);

            //Put xAxis on bottom
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisRight();

            //Setup labels
            yAxis.setDrawLabels(true);

            //Match the color to UI mode
            yAxis.setTextColor(colorMode());

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(true);
            lineChart.getAxisLeft().setEnabled(false);

            //Format the yAxis
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
            //Bind the view
            legend = lineChart.getLegend();

            //Put the legend inside
            legend.setDrawInside(true);

//            legend.setEnabled(true);

            //Set the colormode to match the ui
            legend.setTextColor(colorMode());

            //Move legend to top right
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        }

        //Add data and animate
        setDataChart(priceArray.size(), 100);
        lineChart.animateX(1000);
    }

    /**
     * Setup the volume chart
     */
    private void setChartVolume() {
        //Setup the chart
        {
            //Bind the data
            barChart = findViewById(R.id.chartVol);

            //disable description text
            barChart.getDescription().setEnabled(false);

            //enable touch gestures
            barChart.setTouchEnabled(true);

            //Disable background grid
            barChart.setDrawGridBackground(false);

            //enable scaling and dragging
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = barChart.getXAxis();

            //Disable xAxis labels (use price table's)
            xAxis.setDrawLabels(false);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = barChart.getAxisRight();

            //Draw yAxis labels
            yAxis.setDrawLabels(true);

            //Draw 3 labels
            yAxis.setLabelCount(3);

            //Use correct color mode
            yAxis.setTextColor(colorMode());

            //Set axis to right side
            barChart.getAxisRight().setEnabled(true);
            barChart.getAxisLeft().setEnabled(false);

            //Format the volume values
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
            //Disable the legend for the volume chart
            legend = barChart.getLegend();
            legend.setEnabled(false);
        }

        //Add data and animate
        setDataVolume(volumeArray.size(), 100);
        barChart.animateX(1000);
    }

    /**
     * Bind the data for the price chart
     *
     * @param count the number of items to draw
     * @param range *Not used
     */
    private void setDataChart(int count, float range) {
        ArrayList<Entry> values = new ArrayList<>();

        //Set i to count or less than 90
        if (count != 0) {
            int i;

            if (count > 90)
                i = 90;
            else
                i = count;

            //Loop through the data up to determined count
            for (int k = 0; i > 1; i--, k++) {
                //set value
                try {
                    //set the value to the value in array
                    float val = (float) priceArray.get(i);
                    values.add(new Entry(k, val));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "NO DATA TO ADD!", Toast.LENGTH_SHORT).show();
        }

        //Creat the dataset for the chart
        LineDataSet set;

        //If there's actually something to display
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            //Set the values and update the chart
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set = new LineDataSet(values, "3 Month Chart");

            //Set the colors of the linechart
            set.setColor(getResources().getColor(colorStockChartGradient));
            set.setCircleColor(getResources().getColor(colorStockChartGradient));

            // line thickness
            set.setLineWidth(1f);

            //Disable points
            set.setDrawCircles(false);
            set.setDrawValues(false);

            // customize legend entry
            set.setFormLineWidth(1f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(15.f);

            //draw selection line as
//            set.disableDashedHighlightLine();

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
                set.setFillColor(getResources().getColor(colorStockChartGradient));
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
        }
    }

    /**
     * Set the data for the volume chart
     *
     * @param count the number of items to display ( number of days )
     * @param range
     */
    private void setDataVolume(int count, float range) {
        ArrayList<BarEntry> values = new ArrayList<>();

        //Set i to count or less than 90
        if (count != 0) {
            int i;

            if (count > 90)
                i = 90;
            else
                i = count;

            i--;

            //Up to the count (reverse loop)
            for (int k = 0; i >= 1; i--, k++) {
                //set value
                try {
                    float val = (float) volumeArray.get(i);
                    values.add(new BarEntry(k, val));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "NO DATA TO ADD!", Toast.LENGTH_SHORT).show();
        }


        //Create the chart dataset
        BarDataSet set;

        //If there's something to display
        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.setColors(new int[]{colorStockChartGradient, colorStockChartGradient}, this);

            //Update the displays
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

            //Dont draw values
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