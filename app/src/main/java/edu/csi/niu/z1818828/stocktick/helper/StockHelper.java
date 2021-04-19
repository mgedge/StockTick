package edu.csi.niu.z1818828.stocktick.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

import static android.content.Context.MODE_PRIVATE;

/**
 * The purpose of this class was to provide assisting functions to stocks.
 * This class was meant to retrieve all stocks from the sp500, dow jones, and nasdaq indexes
 * in order to retrieve missing names, and generate a list to enable search results.
 */
public class StockHelper {
    private ArrayList<Stock> sp500;
    private ArrayList<Stock> dow;
    private ArrayList<Stock> nasdaq;

    private JSONArray jsonNasdaq;
    private JSONArray jsonDow;
    private JSONArray jsonSp500;

    private static Context context;

    /**
     * Constructor to call helper functions
     * Loads data from shared preferences
     *
     * @param context - the context from which this class is called
     */
    public StockHelper(Context context) {
        this.context = context;
        loadData(sp500, "sp500");
        loadData(dow, "dow");
        loadData(nasdaq, "nasdaq");
    }

    /**
     * Set the name of the stock
     *
     * @param stock - the stock to set the name for
     */
    public void setStockName(Stock stock) {
        //Retrieve sp500 stocks if they don't exist
        if (sp500 == null) {
            retrieveSP500();
        }

        //Look for the stock in sp500
        for (int i = 0; i < sp500.size(); i++) {
            if (sp500.get(i).getSymbol().compareTo(stock.getSymbol()) == 0) {
                stock.setStockName(sp500.get(i).getStockName());
                return;
            }
        }

        //Retrieve dow jones stocks if they don't exist
        if (dow == null) {
            retrieveDOW();
        }
        //Look for the stock in dow
        for (int i = 0; i < dow.size(); i++) {
            if (dow.get(i).getSymbol().compareTo(stock.getSymbol()) == 0) {
                stock.setStockName(dow.get(i).getStockName());
                return;
            }
        }

        //Retrieve nasdaq stocks if they don't exist
        if (nasdaq == null) {
            retrieveNASDAQ();
        }
        //Look for the stock in sp500
        for (int i = 0; i < nasdaq.size(); i++) {
            if (nasdaq.get(i).getSymbol().compareTo(stock.getSymbol()) == 0) {
                stock.setStockName(nasdaq.get(i).getStockName());
                return;
            }
        }
    }

    /**
     * Helper function to retrieve and parse data
     */
    private void retrieveSP500() {
        retrieveIndex("sp500_constituent");
        for (int i = 0; i < 5; i++) {
            if (sp500 != null) {
                parseStock(jsonSp500, sp500);
                saveData(sp500, "sp500");
                return;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("retrieveSP500", "FAILURE! - array was null when time ran out");
    }

    /**
     * Helper function to retrieve and parse data
     */
    private void retrieveDOW() {
        retrieveIndex("dowjones_constituent");
        for (int i = 0; i < 5; i++) {
            if (dow != null) {
                parseStock(jsonDow, dow);
                saveData(dow, "dow");
                return;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("retrieveDOW", "FAILURE! - array was null when time ran out");
    }

    /**
     * Helper function to retrieve and parse data
     */
    private void retrieveNASDAQ() {
        retrieveIndex("nasdaq_constituent");
        for (int i = 0; i < 5; i++) {
            if (nasdaq != null) {
                parseStock(jsonNasdaq, nasdaq);
                saveData(nasdaq, "nasdaq");
                return;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("retrieveNASDAQ", "FAILURE! - array was null when time ran out");
    }

    /**
     * Generate the API url using the query
     *
     * @param query - the query required to pull data
     * @return the url of the API request
     */
    private URL generateURL(String query) {
        String key = context.getResources().getString(R.string.fmp);

        try {
            String url = "https://financialmodelingprep.com/api/v3/" + query + "?apikey=" + key;

            return new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all the data from the API for the specified index
     *
     * @param index - NASDAQ, SP500, or DOW
     */
    private void retrieveIndex(String index) {
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
                            connection = (HttpURLConnection) generateURL(index).openConnection();
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
                                    Toast.makeText(context, "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                //Set the json object
                                if (index.compareTo("sp500_constituent") == 0)
                                    jsonSp500 = new JSONArray(builder.toString());
                                else if (index.compareTo("dowjones_constituent") == 0)
                                    jsonDow = new JSONArray(builder.toString());
                                else if (index.compareTo("nasdaq_constituent") == 0)
                                    jsonNasdaq = new JSONArray(builder.toString());
                                else
                                    return;

                                //Data has been retrieved, stop retrieving
                                retrieved = true;
                            } else {
                                Toast.makeText(context, "Could not connect to the API", Toast.LENGTH_SHORT).show();
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
     * Parse the json array and set the values in the list
     *
     * @param array - the jsonArray to parse
     * @param list  - the Stock list that items will be stored
     */
    private void parseStock(JSONArray array, ArrayList<Stock> list) {
        try {
            JSONArray item = array;

            //Iterate through the number of items to display and set their values
            for (int i = 0; i < item.length(); i++) {
                JSONObject obj = item.getJSONObject(i);

                //Create a stock and set the values
                Stock stock = new Stock();
                try {
                    stock.setSymbol(obj.getString("symbol"));
                    stock.setStockName(obj.getString("name"));
                } catch (Exception e) {
                    //If something fails, print
                    e.printStackTrace();
                }

                //Add the new stock to the losers list
                list.add(stock);
            }
        } catch (Exception e) {
            //If anything fails, print
            e.printStackTrace();
        }
    }

    /**
     * Saves the list to shared preferences
     */
    public void saveData(ArrayList<?> list, String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(list);
        editor.putString(name, json);

        for (int i = 0; i < list.size(); i++) {
            Log.i("StockHelper - " + name, list.get(i).toString());
        }

        editor.apply();
    }

    /**
     * Retrieve the list from shared preferences
     */
    public void loadData(ArrayList<?> list, String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPref", MODE_PRIVATE);

        Gson gson = new Gson();

        String json = sharedPreferences.getString(name, null);

        Type type = new TypeToken<ArrayList<Stock>>() {
        }.getType();

        list = gson.fromJson(json, type);

        if (list == null) {
            list = new ArrayList<>();
        }
    }
}
