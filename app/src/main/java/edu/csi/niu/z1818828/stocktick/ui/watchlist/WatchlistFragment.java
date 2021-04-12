package edu.csi.niu.z1818828.stocktick.ui.watchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.Stock;
import edu.csi.niu.z1818828.stocktick.adapters.WatchlistStockAdapter;

public class WatchlistFragment extends Fragment {
    private WatchlistViewModel watchlistViewModel;

    List<Stock> watchlist = new ArrayList<>();


    WatchlistStockAdapter watchlistStockAdapter;

    @Override //called first
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Check the user's watchlist, query on create
        //TODO add a slide down to refresh (re-query)

        Stock stock1 = new Stock("TSLA", "Tesla Inc", "NASDAQ",745.40, 740.23, 754.34, 900.23, 500.23, 834.93, 700.34, 9000000, 0.45);
        Stock stock2 = new Stock("CTRM", "Castor Maritime Inc", "NYSE",745.40, 740.23, 754.34, 900.23, 500.23, 834.93, 700.34, 9000000, -0.45);
        Stock stock3 = new Stock("TSLA", "Tesla Inc", "NASDAQ",745.40, 740.23, 754.34, 900.23, 500.23, 834.93, 700.34, 9000000, 0.45);

        watchlist.add(stock1);
        watchlist.add(stock2);
        watchlist.add(stock3);

        //Layout Manager


        //Create adapter using watchlist
        watchlistStockAdapter = new WatchlistStockAdapter(getContext(), watchlist);

    }

    //Called 2nd
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_watchlist, container, false);

        final RecyclerView recyclerViewWatchlist = root.findViewById(R.id.recyclerViewWatchlist);
        final TextView textViewEmptyList = root.findViewById(R.id.textViewNoStocks);

        recyclerViewWatchlist.setAdapter(watchlistStockAdapter);
        watchlistStockAdapter.notifyDataSetChanged();

        recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        //Create the adapter
        //watchlistStockAdapter = new WatchlistStockAdapter(getContext(), watchlist);

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
    public void onPause() {
        super.onPause();
        //TODO implement saving of watchlist to storage
    }
}