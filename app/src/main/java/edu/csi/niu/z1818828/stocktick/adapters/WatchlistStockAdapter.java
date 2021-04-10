package edu.csi.niu.z1818828.stocktick.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.Stock;

public class WatchlistStockAdapter extends RecyclerView.Adapter<WatchlistStockAdapter.ViewHolder> {
    private List<Stock> stocks;
    private LayoutInflater inflater;
    private Context context;

    public WatchlistStockAdapter(Context context, List<Stock> stocks) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.stocks = stocks;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.watchlist_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stocks.get(position);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //items in stock card

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
