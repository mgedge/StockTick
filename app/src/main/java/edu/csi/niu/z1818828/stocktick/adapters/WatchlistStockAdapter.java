package edu.csi.niu.z1818828.stocktick.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.csi.niu.z1818828.stocktick.MainActivity;
import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.Stock;
import edu.csi.niu.z1818828.stocktick.ui.stock.StockActivity;
import edu.csi.niu.z1818828.stocktick.ui.stock.StockFragment;

public class WatchlistStockAdapter extends RecyclerView.Adapter<WatchlistStockAdapter.ViewHolder> {
    private List<Stock> stocks;
    private LayoutInflater inflater;
    private Context context;
    private AdapterView.OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    }

    public interface ContextProvider {
        Context getContext();
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
//        void onDeleteClick(int position);
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    public WatchlistStockAdapter(Context context, List<Stock> stocks) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.stocks = stocks;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.watchlist_stock, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stocks.get(position);

        holder.textViewSymbol.setText(stock.getSymbol());
        holder.textViewExchange.setText(stock.getExchange());
        holder.textViewStockName.setText(stock.getStockName());
        holder.textViewPrice.setText(String.format("%.2f", stock.getPrice()));

        if(stock.getRange() >= 0) {
            holder.textViewPercentChange.setText("+" + String.format("%.2f", stock.getRange()) + "%");
            holder.textViewPercentChange.setBackgroundColor(context.getResources().getColor(R.color.colorPositive));
        }
        else {
            holder.textViewPercentChange.setText(String.format("%.2f", stock.getRange()) + "%");
            holder.textViewPercentChange.setBackgroundColor(context.getResources().getColor(R.color.colorNegative));
        }

        holder.itemView.setOnClickListener(v -> {
            //Toast.makeText(context, position + " has been selected", Toast.LENGTH_SHORT).show();

//            ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.nav_host_fragment, new StockFragment()).commit();

//            Fragment fragment = new Fragment();
//            FragmentManager fm = context.get
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container_view_tag, fragment);
//            ft.commit();
            Intent intent = new Intent(v.getContext(), StockActivity.class);
            intent.putExtra("stockSymbol", stock.getSymbol());
            intent.putExtra("stockName", stock.getStockName());
            intent.putExtra("volume", stock.getVolume());
            intent.putExtra("pcntRange", stock.getRange());

            //Store data for card
            //intent.putExtra(EXTRA, item.getTitle());

            //Start new activity
            v.getContext().startActivity(intent);

            if(context.getClass().equals(MainActivity.class)) {
                ((MainActivity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSymbol;
        TextView textViewExchange;
        TextView textViewStockName;
        TextView textViewPercentChange;
        TextView textViewPrice;

        public ViewHolder(@NonNull View itemView, AdapterView.OnItemClickListener listener) {
            super(itemView);

            textViewSymbol = itemView.findViewById(R.id.textViewSymbol);
            textViewExchange = itemView.findViewById(R.id.textViewExchange);
            textViewStockName = itemView.findViewById(R.id.textViewStockName);
            textViewPercentChange = itemView.findViewById(R.id.textViewPercentChange);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}
