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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.adapters.WatchlistAdapter;
import edu.csi.niu.z1818828.stocktick.objects.Stock;
import edu.csi.niu.z1818828.stocktick.ui.stock.StockActivity;

import static android.content.Context.MODE_PRIVATE;

public class WatchlistFragment extends Fragment {
    TextView textViewEmptyList;

    android.view.ActionMode actionMode;

    List<Stock> watchlist = new ArrayList<>();

    SwipeRefreshLayout swipeRefreshLayout;

    WatchlistAdapter watchlistAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);


        //Load the watchlist
        loadData();

        //Remove any selections that may have slipped through
        deselectAll();

        //Create adapter using watchlist
        watchlistAdapter = new WatchlistAdapter(getContext(), watchlist, null);
        watchlistAdapter.setListener(new WatchlistAdapter.OnStockClickListener() {
            @Override
            public void onStockClick(int position) {
                //If any stocks are selected, enable quick select
                if (watchlistAdapter.bSelectionGroup) {
                    toggleSelection(watchlist.get(position));
                    watchlistAdapter.toggleSelection(position);
                    watchlistAdapter.notifyDataSetChanged();
                    enableActionMode(position);
                } else {
                    Intent intent = new Intent(getContext(), StockActivity.class);
                    intent.putExtra("stockSymbol", watchlist.get(position).getSymbol());
                    startActivity(intent);
                }
            }

            @Override
            public void onStockLongClick(int position) {
                Log.w("WatchlistFragment", "onLongClick: pressed");
                toggleSelection(watchlist.get(position));
                enableActionMode(position);
            }
        });

        watchlistAdapter.notifyDataSetChanged();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_watchlist, container, false);

        final RecyclerView recyclerViewWatchlist = root.findViewById(R.id.recyclerViewWatchlist);
        textViewEmptyList = root.findViewById(R.id.textViewNoStocks);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Load data from preferences
                loadData();

                //Update the adapter
                watchlistAdapter.notifyDataSetChanged();

                //Update UI
                deselectAll();
                updateNoStocks();

                //End refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        recyclerViewWatchlist.setAdapter(watchlistAdapter);
        recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        watchlistAdapter.notifyDataSetChanged();

        updateNoStocks();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        watchlistAdapter.notifyDataSetChanged();

        deselectAll();

        updateNoStocks();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.watchlist_menu, menu);

        MenuItem search = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("Ticker symbol (e.g. ABC)");
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
            case R.id.menu_selectall:
                if (watchlistAdapter.isAllSelected()) {
                    watchlistAdapter.deselectAll();
                    deselectAll();
                } else {
                    watchlistAdapter.selectAll();
                    selectAll();
                    enableActionMode(-1);
                }
                return true;
            case R.id.menu_deletaall:
                //Remove data from preferences
                clearData();
                removeData();

                //Remove data from the adapter
                watchlistAdapter.selectAll();
                watchlistAdapter.deleteSelectedStocks();
                watchlistAdapter.notifyDataSetChanged();

                updateNoStocks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //Save the latest data
        refreshData();

        //Deselect any stocks
        deselectAll();
        watchlistAdapter.deselectAll();

        //Disable action mode
        enableActionMode(-1);
    }

    private void enableActionMode(int position) {
        Log.w("ActionMode", "enableActionMode: " + position);

        if (actionMode == null) {
            actionMode = getActivity().startActionMode(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.watchlist_menu_select, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    Log.w("ActionMode", "enableActionMode: onActionItemClicked item: " + item);

                    if (item.getItemId() == R.id.menu_delete) {
                        //Go through the stocks. Delete selected from database
//                        for (Stock stock : watchlist) {
//                            if (stock.isSelected()) {
//                                watchlist.remove(stock);
//                                refreshData();
//                                watchlistAdapter.notifyDataSetChanged();
//                            }
//                        }

                        //Remove selected stocks
                        for (Iterator<Stock> iterator = watchlist.iterator(); iterator.hasNext(); ) {
                            Stock temp = iterator.next();
                            if (temp.isSelected()) {
                                iterator.remove();
                            }
                        }

                        refreshData();


                        //Delete selected stocks from the adapter
                        watchlistAdapter.deleteSelectedStocks();

                        //Reset the stock objects so they are false
                        watchlistAdapter.deselectAll();

                        //Refresh the view
                        watchlistAdapter.notifyDataSetChanged();

                        //Show the no stocks text if all stocks removed
                        updateNoStocks();

                        mode.finish();
                        return true;
                    }

                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {
                    //Make sure all stocks are deselected
                    if ((watchlistAdapter.getSelectedSize() != 0))
                        watchlistAdapter.deselectAll();

                    //destroy the actionMode
                    actionMode = null;
                }
            });
        }

        //Set the dynamic title
        final int size = watchlistAdapter.getSelectedSize();
        if (size == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(size + "");
            actionMode.invalidate();
        }
    }

    private void updateNoStocks() {
        if (watchlist.size() == 0) {
            textViewEmptyList.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyList.setVisibility(View.GONE);
        }
    }

    public void toggleSelection(Stock stock) {
        if (stock.isSelected()) {
            stock.setSelected(false);
        } else {
            stock.setSelected(true);
        }
    }

    public void deselectAll() {
        if (watchlist.size() > 0) {
            for (int i = 0; i < watchlist.size(); i++) {
                watchlist.get(i).setSelected(false);
            }
        }
    }

    public void selectAll() {
        if (watchlist.size() > 0) {
            for (int i = 0; i < watchlist.size(); i++) {
                watchlist.get(i).setSelected(true);
            }
        }
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(watchlist);
        editor.putString("watchlist", json);

        for (int i = 0; i < watchlist.size(); i++) {
            Log.i("SAVEDATA", watchlist.get(i).getSymbol());
        }

        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);

        Gson gson = new Gson();

        String json = sharedPreferences.getString("watchlist", null);

        Type type = new TypeToken<ArrayList<Stock>>() {
        }.getType();

        watchlist = gson.fromJson(json, type);

        if (watchlist == null) {
            watchlist = new ArrayList<>();
        }
    }

    public void clearData() {
        watchlist.clear();
    }

    public void removeData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void refreshData() {
        removeData();
        saveData();
    }
}