package edu.csi.niu.z1818828.stocktick.ui.watchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.Stock;
import edu.csi.niu.z1818828.stocktick.adapters.WatchlistStockAdapter;

public class WatchlistFragment extends Fragment {
    RecyclerView recyclerViewWatchlist;
    TextView textViewEmptyList;

    private WatchlistViewModel watchlistViewModel;

    List<Stock> watchlist;

    WatchlistStockAdapter watchlistStockAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(watchlist.size() == 0) {
//            textViewEmptyList.setVisibility(View.VISIBLE);
//        }
//        else {
//            textViewEmptyList.setVisibility(View.GONE);
//        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_watchlist, container, false);

        recyclerViewWatchlist = root.findViewById(R.id.recyclerViewWatchlist);
        textViewEmptyList = root.findViewById(R.id.textViewNoStocks);

        //Create the adapter
        watchlistStockAdapter = new WatchlistStockAdapter(getContext(), watchlist);

        //Bind adapter for the recycler
        recyclerViewWatchlist.setAdapter(watchlistStockAdapter);

        //Refresh the stock
        watchlistStockAdapter.notifyDataSetChanged();





//        final TextView textView = root.findViewById(R.id.text_home);
//        watchlistViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }
}