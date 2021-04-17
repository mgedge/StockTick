package edu.csi.niu.z1818828.stocktick.ui.movers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.adapters.StockAdapter;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

import static android.content.Context.MODE_PRIVATE;

public class MoversFragment extends Fragment {
    private RecyclerView recyclerViewWinners;
    private RecyclerView recyclerViewLosers;

    List<Stock> winners = new ArrayList<>();
    List<Stock> losers = new ArrayList<>();

    StockAdapter stockAdapterWinners;
    StockAdapter stockAdapterLosers;

    JSONArray jsonWinners;
    JSONArray jsonLosers;

    private MoversViewModel moversViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockAdapterWinners = new StockAdapter(getContext(), winners);
        stockAdapterLosers = new StockAdapter(getContext(), losers);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        moversViewModel = new ViewModelProvider(this).get(MoversViewModel.class);
        View root = inflater.inflate(R.layout.fragment_movers, container, false);

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveData();
            }
        }).start();

//        saveData();


    }

    private void retrieveData() {
        boolean bParsedWinner = false;
        boolean bParsedLoser = false;

        //Retrieve the data from api
        retrieveWinners();
        retrieveLosers();

        //Parse the results
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

    private void retrieveDataWinner() {
        boolean bParsedWinner = false;

        //Retrieve the data from api
        retrieveWinners();

        //Parse the results
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
    }

    private void retrieveDataLoser() {
        boolean bParsedLoser = false;

        //Retrieve the data from api
        retrieveLosers();

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


    private void parseWinner() {
        try {
            JSONArray item = jsonWinners;
            int numItems;

            if (item.length() < 5) {
                numItems = item.length();
            } else {
                numItems = 5;
            }

            for (int i = 0; i < numItems; i++) {
                JSONObject obj = item.getJSONObject(i);

                Stock stock = new Stock();
                stock.setSymbol(obj.getString("ticker"));
                stock.setStockName(obj.getString("companyName"));
                stock.setChange(Double.parseDouble(obj.getString("changes")));
                stock.setPrice(Double.parseDouble(obj.getString("price")));

                String temp = obj.getString("changesPercentage");
                temp = temp.replace('(', ' ');
                temp = temp.replace('%', ' ');
                temp = temp.replace(')', ' ');
                temp = temp.replace('+', ' ');
                temp = temp.replace('-', ' ');
                temp = temp.trim();

                stock.setChangePct(Double.parseDouble(temp));

                winners.add(stock);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        stockAdapterWinners.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseLoser() {
        try {
            JSONArray item = jsonLosers;
            int numItems;

            if (item.length() < 5) {
                numItems = item.length();
            } else {
                numItems = 5;
            }

            for (int i = 0; i < numItems; i++) {
                JSONObject obj = item.getJSONObject(i);

                Stock stock = new Stock();
                stock.setSymbol(obj.getString("ticker"));
                stock.setStockName(obj.getString("companyName"));
                stock.setChange(Double.parseDouble(obj.getString("changes")));
                stock.setPrice(Double.parseDouble(obj.getString("price")));

                String temp = obj.getString("changesPercentage");
                temp = temp.replace('(', ' ');
                temp = temp.replace('%', ' ');
                temp = temp.replace(')', ' ');
                temp = temp.replace('+', ' ');
                temp = temp.trim();

                stock.setChangePct(Double.parseDouble(temp));

                losers.add(stock);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        stockAdapterLosers.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URL generateWinnerUrl() {
        String key = getContext().getResources().getString(R.string.fmp);

        try {
            String url = "https://financialmodelingprep.com/api/v3/gainers?apikey=" + key;
            System.out.println("URL: " + url);

            return new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private URL generateLoserUrl() {
        String key = getResources().getString(R.string.fmp);

        try {
            String url = "https://financialmodelingprep.com/api/v3/losers?apikey=" + key;
            System.out.println("URL: " + url);

            return new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void retrieveWinners() {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                for (int i = 0; i < 12; i++) {
                    if (!retrieved) {
                        try {
                            connection = (HttpURLConnection) generateWinnerUrl().openConnection();
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
                                    Toast.makeText(getContext(), "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                jsonWinners = new JSONArray(builder.toString());
                                retrieved = true;

                                //this.notify(); //Allow the main thread to continue

                                Log.i("JSONWinners", String.valueOf(jsonWinners));
                            } else {
                                Toast.makeText(getContext(), "Could not connect to the API", Toast.LENGTH_SHORT).show();
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

    private void retrieveLosers() {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                for (int i = 0; i < 12; i++) {
                    if (!retrieved) {
                        try {
                            connection = (HttpURLConnection) generateLoserUrl().openConnection();
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
                                    Toast.makeText(getContext(), "Unable to read data", Toast.LENGTH_SHORT).show();
                                }

                                jsonLosers = new JSONArray(builder.toString());
                                retrieved = true;

                                //this.notify(); //Allow the main thread to continue

                                Log.i("JSONLosers", String.valueOf(jsonLosers));
                            } else {
                                Toast.makeText(getContext(), "Could not connect to the API", Toast.LENGTH_SHORT).show();
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

    public void clearData(@NotNull ArrayList<?> list) {
        list.clear();
    }

    public void removeData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("winners");
        editor.remove("losers");
        editor.apply();
    }

    public void refreshData() {
        removeData();
        saveData();
    }
}