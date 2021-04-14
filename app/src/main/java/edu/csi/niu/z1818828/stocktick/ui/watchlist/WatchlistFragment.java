package edu.csi.niu.z1818828.stocktick.ui.watchlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.objects.Stock;
import edu.csi.niu.z1818828.stocktick.adapters.WatchlistStockAdapter;
import edu.csi.niu.z1818828.stocktick.ui.stock.StockActivity;

import static android.content.Context.MODE_PRIVATE;

public class WatchlistFragment extends Fragment {
    private WatchlistViewModel watchlistViewModel;

    List<Stock> watchlist = new ArrayList<>();

    JSONObject jsonSearch = null;

    WatchlistStockAdapter watchlistStockAdapter;
    Toolbar toolbar;

    @Override //called first
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
//        getContext().getSupportActionBar(toolbar);
//        toolbar.inflateMenu(R.menu.watchlist_menu);


        //Check the user's watchlist, query on create
        //TODO add a slide down to refresh (re-query)

        Stock stock1 = new Stock("TSLA", "Tesla Inc", "NASDAQ", 745.40, 740.23, 754.34, 900.23, 500.23, 834.93, 700.34, 9000000, 0.45);
        Stock stock2 = new Stock("CTRM", "Castor Maritime Inc", "NYSE", 745.40, 740.23, 754.34, 900.23, 500.23, 834.93, 700.34, 9000000, -0.45);
        Stock stock3 = new Stock("GME", "GameStop Corp", "NYSE", 140.99, 141.88, 141.09, 185.88, 132.00, 145.38, 132.00, 10611571, 0.45);

        watchlist.add(stock1);
        watchlist.add(stock2);
        watchlist.add(stock3);


        //Create adapter using watchlist
        watchlistStockAdapter = new WatchlistStockAdapter(getContext(), watchlist);

    }

    //Called 2nd
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_watchlist, container, false);

        final RecyclerView recyclerViewWatchlist = root.findViewById(R.id.recyclerViewWatchlist);
        final TextView textViewEmptyList = root.findViewById(R.id.textViewNoStocks);
//        toolbar = root.findViewById(R.id.toolbar);

        recyclerViewWatchlist.setAdapter(watchlistStockAdapter);
        watchlistStockAdapter.notifyDataSetChanged();

        recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if(watchlist.size() == 0) {
            textViewEmptyList.setVisibility(View.VISIBLE);
        }
        else {
            textViewEmptyList.setVisibility(View.GONE);
        }

//        final TextView textView = root.findViewById(R.id.text_home);
//        watchlistViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.watchlist_menu, menu);

        MenuItem search = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getContext(), StockActivity.class);
                intent.putExtra("stockSymbol", query);
                Log.i("Submit", query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //retrieveStockQuery(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_search:
                Toast.makeText(getContext(), "Search selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO implement saving of watchlist to storage
        saveData();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < watchlist.size(); i++) {
            editor.putString(watchlist.get(i).getSymbol(), watchlist.get(i).getSymbol());
        }

        editor.apply();
    }

    private void retrieveStockQuery(String query) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                boolean retrieved = false;

                for (int i = 0; i < 12; i++) {
                    if (!retrieved) {
                        try {
                            String key = getResources().getString(R.string.alphaVantage);
                            String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=" + query + "&symbol=" + query + "&apikey=" + key;
                            System.out.println("URL: " + url);
                            String furl = url + URLEncoder.encode(query, "UTF-8");
                            URL httpQuery = new URL(furl);

                            connection = (HttpURLConnection) httpQuery.openConnection();
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

                                jsonSearch = new JSONObject(builder.toString());
                                retrieved = true;

                                //this.notify(); //Allow the main thread to continue

                                Log.i("JSONSearch", String.valueOf(jsonSearch));
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
}