package edu.csi.niu.z1818828.stocktick.ui.movers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.adapters.StockAdapter;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

import static android.content.Context.MODE_PRIVATE;

public class MoversFragment extends Fragment {
    private TextView textViewStatus;
    private TextView textViewWinners;
    private TextView textViewLosers;

    private RecyclerView recyclerViewWinners;
    private RecyclerView recyclerViewLosers;

    private int errorCode = 0;
    private boolean loaded = false;

    List<Stock> winners = new ArrayList<>();
    List<Stock> losers = new ArrayList<>();

    StockAdapter stockAdapterWinners;
    StockAdapter stockAdapterLosers;

    //The json values returned by api
    JSONArray jsonWinners;
    JSONArray jsonLosers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockAdapterWinners = new StockAdapter(getContext(), winners);
        stockAdapterLosers = new StockAdapter(getContext(), losers);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movers, container, false);

        textViewWinners = root.findViewById(R.id.textViewWinners);
        textViewLosers = root.findViewById(R.id.textViewLosers);
        textViewStatus = root.findViewById(R.id.textViewStatus);
        setStatusView();

        recyclerViewWinners = root.findViewById(R.id.recyclerViewWinners);
        recyclerViewLosers = root.findViewById(R.id.recyclerViewLosers);

        recyclerViewWinners.setAdapter(stockAdapterWinners);
        recyclerViewWinners.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        recyclerViewLosers.setAdapter(stockAdapterLosers);
        recyclerViewLosers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        stockAdapterWinners.notifyDataSetChanged();
        stockAdapterLosers.notifyDataSetChanged();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        //loadData();

        //Start a thread to retrieve the data (this improves the performance of the fragment)
        new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveDataWinner();
                retrieveDataLoser();

                loaded = true;

                //Start a UI thread to update the UI
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setStatusView();
                    }
                });
            }
        }).start();

        //saveData();
    }

    /**
     * Set the retrieval status
     */
    private void setStatusView() {
        if (errorCode == 0) {
            if (loaded) {
                textViewStatus.setVisibility(View.GONE);
                textViewLosers.setVisibility(View.VISIBLE);
                textViewWinners.setVisibility(View.VISIBLE);
            } else {
                textViewStatus.setVisibility(View.VISIBLE);
                textViewLosers.setVisibility(View.GONE);
                textViewWinners.setVisibility(View.GONE);
            }
        } else {
            try {
                textViewStatus.setTextColor(getContext().getResources().getColor(R.color.colorNegative));
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (errorCode) {
                case 1: //Connection error
                    textViewStatus.setVisibility(View.VISIBLE);
                    textViewStatus.setText("Could not connect to the API");
                    break;
                case 2: //URL error
                    textViewStatus.setVisibility(View.VISIBLE);
                    textViewStatus.setText("URL error, something went wrong");
                    break;
                case 3: //JSON object error
                    textViewStatus.setVisibility(View.VISIBLE);
                    textViewStatus.setText("JSON error, the stock does not exist");
                    break;
            }
        }
    }

    /**
     * Retrieve the data from the
     */
    private void retrieveData() {
        boolean bParsedWinner = false;
        boolean bParsedLoser = false;

        //Retrieve the data from api
        retrieveWinners();
        retrieveLosers();

        //Parse the results, waiting 3 seconds max for results
        for (int i = 0; i < 3; i++) {
            if (jsonWinners == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!bParsedWinner) {
                    parseWinner();
                    bParsedWinner = true;
                }
            }
        }

        //Parse the results
        for (int i = 0; i < 3; i++) {
            if (jsonLosers == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!bParsedLoser) {
                    parseLoser();
                    bParsedLoser = true;
                }
            }
        }
    }

    /**
     * retrieve and parse the winner data
     */
    private void retrieveDataWinner() {
        boolean bParsedWinner = false;

        //Retrieve the data from api
        retrieveWinners();

        //Parse the results, waiting 3 seconds max for results
        for (int i = 0; i < 3; i++) {
            if (jsonWinners == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!bParsedWinner) {
                    parseWinner();
                    bParsedWinner = true;
                    return;
                }
            }
        }

        //If reached, data failed to be retrieved
        errorCode = 2;
    }

    /**
     * retrieve and parse the loser data
     */
    private void retrieveDataLoser() {
        boolean bParsedLoser = false;

        //Retrieve the data from api
        retrieveLosers();

        //Parse the results, waiting 3 seconds max for results
        for (int i = 0; i < 3; i++) {
            if (jsonLosers == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!bParsedLoser) {
                    parseLoser();
                    bParsedLoser = true;
                    return;
                }
            }
        }

        errorCode = 2;
    }


    /**
     * Go through the results retrieved from the api
     */
    private void parseWinner() {
        try {
            JSONArray item = jsonWinners;
            int numItems;

            //Determine the number of items to display
            if (item.length() < 5) {
                numItems = item.length();
            } else {
                numItems = 5;
            }

            //Iterate through the number of items to display and set their values
            for (int i = 0; i < numItems; i++) {
                JSONObject obj = item.getJSONObject(i);

                //Remove extra characters from the percentage
                String temp = obj.getString("changesPercentage");
                try {
                    temp = temp.replace('(', ' ');
                    temp = temp.replace('%', ' ');
                    temp = temp.replace(')', ' ');
                    temp = temp.replace('+', ' ');
                    temp = temp.replace('-', ' ');
                    temp = temp.trim();
                } catch (Exception e) {
                    //If it somehow fails, return the original
                    temp = obj.getString("changesPercentage");
                    e.printStackTrace();
                }

                //Create a stock and set the values
                Stock stock = new Stock();
                try {
                    stock.setSymbol(obj.getString("ticker"));
                    stock.setStockName(obj.getString("companyName"));
                    stock.setChange(Double.parseDouble(obj.getString("changes")));
                    stock.setPrice(Double.parseDouble(obj.getString("price")));
                    stock.setChangePct(Double.parseDouble(temp));
                } catch (Exception e) {
                    //If something fails, print
                    e.printStackTrace();
                }

                //Add to the list, the new stock
                winners.add(stock);

                //Start a UI thread to update the UI
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        stockAdapterWinners.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            //If anything fails, print
            e.printStackTrace();
            errorCode = 1;
        }
    }

    /**
     * Go through the results retrieved from the api
     */
    private void parseLoser() {
        try {
            JSONArray item = jsonLosers;
            int numItems;

            //Determine the number of items to display
            if (item.length() < 5) {
                numItems = item.length();
            } else {
                numItems = 5;
            }

            //Iterate through the number of items to display and set their values
            for (int i = 0; i < numItems; i++) {
                JSONObject obj = item.getJSONObject(i);

                //Remove extra characters from the percentage
                String temp = obj.getString("changesPercentage");
                try {
                    temp = temp.replace('(', ' ');
                    temp = temp.replace('%', ' ');
                    temp = temp.replace(')', ' ');
                    temp = temp.replace('+', ' ');
                    temp = temp.trim();
                } catch (Exception e) {
                    //If it somehow fails, return the original
                    temp = obj.getString("changesPercentage");
                    e.printStackTrace();
                }

                //Create a stock and set the values
                Stock stock = new Stock();
                try {
                    stock.setSymbol(obj.getString("ticker"));
                    stock.setStockName(obj.getString("companyName"));
                    stock.setChange(Double.parseDouble(obj.getString("changes")));
                    stock.setPrice(Double.parseDouble(obj.getString("price")));
                    stock.setChangePct(Double.parseDouble(temp));
                } catch (Exception e) {
                    //If something fails, print
                    e.printStackTrace();
                }

                //Add the new stock to the losers list
                losers.add(stock);

                //Start a UI thread to update the adapter
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        stockAdapterLosers.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            //If anything fails, print
            e.printStackTrace();
        }
    }

    /**
     * Generate the URL for the FMP api
     *
     * @return the URL to retrieve the winner data from
     */
    private URL generateWinnerUrl() {
        String key = getContext().getResources().getString(R.string.fmp);

        try {
            String url = "https://financialmodelingprep.com/api/v3/gainers?apikey=" + key;
            return new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate the URL for the FMP api
     *
     * @return the URL to retrieve the loser data from
     */
    private URL generateLoserUrl() {
        String key = getResources().getString(R.string.fmp);

        try {
            String url = "https://financialmodelingprep.com/api/v3/losers?apikey=" + key;
            return new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve the winners from the API and set the JSON jsonWinners array
     */
    private void retrieveWinners() {
        //Start a thread and do this in the background
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                //Try every 5 seconds for a minute
                for (int i = 0; i < 12; i++) {
                    //If the information has not been retrieved, keep trying
                    if (!retrieved) {
                        try {
                            //Create the connection
                            connection = (HttpURLConnection) generateWinnerUrl().openConnection();
                            int response = connection.getResponseCode();

                            //If connected
                            if (response == HttpURLConnection.HTTP_OK) {
                                StringBuilder builder = new StringBuilder();

                                //Attempt to read the response
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getContext(), "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                //Set the json array
                                jsonWinners = new JSONArray(builder.toString());
                                retrieved = true;

                                //Log what was retrieved
                                Log.i("JSONWinners", String.valueOf(jsonWinners));
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
                            //Disconnect from api
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
     * Retrieve the losers from the API and set the JSON jsonLosers array
     */
    private void retrieveLosers() {
        //Start a thread and do this in the background
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                //Try every 5 seconds for a minute
                for (int i = 0; i < 12; i++) {
                    //If the information has not been retrieved, keep trying
                    if (!retrieved) {
                        try {
                            //Create the connection
                            connection = (HttpURLConnection) generateLoserUrl().openConnection();
                            int response = connection.getResponseCode();

                            //If connected
                            if (response == HttpURLConnection.HTTP_OK) {
                                StringBuilder builder = new StringBuilder();

                                //Attempt to read the response
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getContext(), "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                //Set the json array
                                jsonLosers = new JSONArray(builder.toString());
                                retrieved = true;

                                //Log what was retrieved
                                Log.i("JSONLosers", String.valueOf(jsonLosers));
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
                            //Disconnect from api
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
     * Save the winners and losers to shared preferences
     */
    public void saveData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String jsonW = gson.toJson(winners);
        String jsonL = gson.toJson(losers);

        editor.putString("winners", jsonW);
        editor.putString("losers", jsonL);

        editor.apply();
    }

    /**
     * Load the winners and losers to shared preferences
     */
    public void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);

        Gson gson = new Gson();

        String jsonW = sharedPreferences.getString("winners", null);
        String jsonL = sharedPreferences.getString("losers", null);

        Type type = new TypeToken<ArrayList<Stock>>() {
        }.getType();

        winners = gson.fromJson(jsonW, type);
        if (winners == null) {
            winners = new ArrayList<>();
        }

        losers = gson.fromJson(jsonL, type);
        if (losers == null) {
            losers = new ArrayList<>();
        }
    }


    /**
     * Remove the winners and losers from shared preferences
     */
    public void removeData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("winners");
        editor.remove("losers");
        editor.apply();
    }

    /**
     * Remove the winners and losers from shared preferences, then save the new ones
     */
    public void refreshData() {
        removeData();
        saveData();
    }
}