/************************************************************************
 * 	File Name: WatchlistFragment.java									*
 * 																		*
 *  Developer: Matthew Gedge											*
 *   																	*
 *    Purpose: This java class runs the Watchlist fragment. This is the *
 *    main page where users can browse their watchlist and view stocks. *
 *    Due to limitations with the API, stocks will not have up to date  *
 *    information. Given the low call count per minute, it was best for *
 *    this project to instead only retrieve information at the user's   *
 *    request. Users can quickly delete multiple stocks at once, by     *
 *    first long clicking a stock, which will highlight the stock and   *
 *    enable an action menu. The user can then quick select stocks and  *
 *    user the delete button to remove them. If the user deselects all  *
 *    stocks, the action menu will disappear and the user may then      *
 *    view stocks by clicking them as normal.                           *
 *																		*
 * *********************************************************************/
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
                }
                //Not in selection mode, open the stock
                else {
                    Intent intent = new Intent(getContext(), StockActivity.class);
                    intent.putExtra("stockSymbol", watchlist.get(position).getSymbol());

                    //Attempt to get the stock name
                    try {
                        intent.putExtra("stockName", watchlist.get(position).getStockName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    startActivity(intent);
                }
            }

            @Override
            public void onStockLongClick(int position) {
                toggleSelection(watchlist.get(position));
                enableActionMode(position);
            }
        });

        //Update the adapter
        watchlistAdapter.notifyDataSetChanged();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        //Setup the recycler view
        recyclerViewWatchlist.setAdapter(watchlistAdapter);
        recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        //Update the adapter
        watchlistAdapter.notifyDataSetChanged();

        //Change the view if there's no stocks
        updateNoStocks();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

//        clearData();

        //Load the data
        loadData();

        //Deselect anything that was selected
        deselectAll();

        //Display if there was no stocks
        updateNoStocks();

        watchlistAdapter.notifyDataSetChanged();
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
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //If api provides, display suggested stocks
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
                //Toggle all select
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

                //Update nostocks text
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

    /**
     * Used to enable action mode allowing users to select stocks with single clicks as long as 1
     * stock is selected
     *
     * @param position the position in the adapter that was selected
     */
    private void enableActionMode(int position) {
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
                    if (item.getItemId() == R.id.menu_delete) {
                        //Remove selected stocks
                        for (Iterator<Stock> iterator = watchlist.iterator(); iterator.hasNext(); ) {
                            Stock temp = iterator.next();
                            if (temp.isSelected()) {
                                iterator.remove();
                            }
                        }

                        //Remove then add the new stocks
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

        //Set the dynamic title to show how many items selected
        final int size = watchlistAdapter.getSelectedSize();
        if (size == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(size + "");
            actionMode.invalidate();
        }
    }

    /**
     * Update the textview if there are no stocks in the watchlist
     */
    private void updateNoStocks() {
        if (watchlist.size() == 0) {
            textViewEmptyList.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyList.setVisibility(View.GONE);
        }
    }

    /**
     * Toggles the isSelected value of the stock
     *
     * @param stock - stock to flip isSelected value
     */
    public void toggleSelection(Stock stock) {
        stock.setSelected(!stock.isSelected());
    }

    /**
     * Set all stocks in the watchlist to selected false
     */
    public void deselectAll() {
        if (watchlist.size() > 0) {
            for (int i = 0; i < watchlist.size(); i++) {
                watchlist.get(i).setSelected(false);
            }
        }
    }

    /**
     * Sets all stocks in the watchlist to selected true
     */
    public void selectAll() {
        if (watchlist.size() > 0) {
            for (int i = 0; i < watchlist.size(); i++) {
                watchlist.get(i).setSelected(true);
            }
        }
    }

    /**
     * Saves the watchlist to shared preferences
     */
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

    /**
     * Retrieve the watchlist from shared preferences
     */
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

    /**
     * Clear the watchlist
     */
    public void clearData() {
        watchlist.clear();
    }

    /**
     * Remove the watchlist from shared preferences
     */
    public void removeData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("watchlist");
        editor.apply();
    }

    /**
     * Remove then add all stocks from shared preferences
     */
    public void refreshData() {
        removeData();
        saveData();
    }
}