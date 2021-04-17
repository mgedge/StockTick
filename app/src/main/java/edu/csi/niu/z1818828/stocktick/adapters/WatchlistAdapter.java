package edu.csi.niu.z1818828.stocktick.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.objects.Stock;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {
    private List<Stock> stocks;
    private LayoutInflater inflater;
    private OnStockClickListener clickListener;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private Context context;

    public boolean bSelectionGroup = false;

    public void setListener(OnStockClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public WatchlistAdapter(Context context, List<Stock> stocks, OnStockClickListener clickListener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.stocks = stocks;
        this.clickListener = clickListener;
    }

    public interface OnStockClickListener {
        void onStockClick(int position);

        void onStockLongClick(int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.watchlist_stock, parent, false);
        return new ViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stocks.get(position);

        holder.textViewSymbol.setText(stock.getSymbol());
        holder.textViewExchange.setText(stock.getExchange());
        holder.textViewStockName.setText(stock.getStockName());
        holder.textViewPrice.setText(String.format("%.2f", stock.getPrice()));
        holder.textViewPercentChange.setText(stock.formatChangePercentage(stock.getChangePct()));
        holder.textViewDate.setText(stock.formatDateDay(stock.getDate()));

        //Change the stock change color
        if (stock.getChangePct() >= 0) {
            holder.textViewPercentChange.setText("+" + stock.formatChangePercentage(stock.getChangePct()));
            holder.textViewPercentChange.setBackgroundColor(context.getResources().getColor(R.color.colorPositive));
        } else {
            holder.textViewPercentChange.setBackgroundColor(context.getResources().getColor(R.color.colorNegative));
        }

        //Set the selected/nonselected background
        if (stock.isSelected()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorAccentLight));
        } else {
            int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cardBackgroundDark));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cardBackgroundLight));
                    break;
                default:
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cardGray));
                    break;
            }
        }

        //Set the boolean value for any selection enable to true
        bSelectionGroup = getSelectedSize() > 0;

        //Listener for card long click
        holder.itemView.setOnLongClickListener(v -> {
            Log.d("Adapter", "onLongClick: pressed: " + position + " selected: " + stock.isSelected() + " sparseBool: " + selectedItems.get(position));

            toggleSelection(position);

            notifyDataSetChanged();

            if (clickListener != null) {
                clickListener.onStockLongClick(position);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewSymbol;
        TextView textViewExchange;
        TextView textViewStockName;
        TextView textViewPercentChange;
        TextView textViewPrice;
        TextView textViewDate;
        OnStockClickListener clickListener;

        public ViewHolder(@NonNull View itemView, OnStockClickListener listener) {
            super(itemView);

            textViewSymbol = itemView.findViewById(R.id.textViewNewsTitle);
            textViewExchange = itemView.findViewById(R.id.textViewSource);
            textViewStockName = itemView.findViewById(R.id.textViewStockName);
            textViewPercentChange = itemView.findViewById(R.id.textViewPercentChange);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewDate = itemView.findViewById(R.id.textViewChange);
            this.clickListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onStockClick(getAdapterPosition());
        }
    }

    /**
     * determine if all stocks are selected
     *
     * @return true if all stocks are selected
     */
    public boolean isAllSelected() {
        boolean all = true;
        for (Stock stock : stocks) {
            if (!stock.isSelected()) {
                all = false;
                break;
            }
        }

        return all;
    }

    /**
     * determine the num of stocks that are selected (true)
     *
     * @return the num of selected stocks
     */
    public int getSelectedSize() {
        int numSelected = 0;

        for (Stock stock : stocks) {
            if (stock.isSelected()) {
                numSelected++;
            }
        }

        return numSelected;
    }

    /**
     * delete all of the selected stocks from the adapter
     */
    public void deleteSelectedStocks() {
        ArrayList<Stock> stocks = new ArrayList<>();
        for (Stock stock : this.stocks) {
            if (stock.isSelected())
                stocks.add(stock);
        }

        this.stocks.removeAll(stocks);
        notifyDataSetChanged();
    }

    /**
     * sets all stocks' selected boolean to true as well as the support boolean array
     */
    public void selectAll() {
        int i = 0;

        for (Stock stock : this.stocks) {
            selectedItems.put(i, true);
            stock.setSelected(true);
            i++;
        }

        bSelectionGroup = true;
        notifyDataSetChanged();
    }

    /**
     * sets all stocks' selected boolean to false as well as the support boolean array
     */
    public void deselectAll() {
        int i = 0;

        for (Stock stock : this.stocks) {
            selectedItems.put(i, false);
            stock.setSelected(false);
            i++;
        }

        bSelectionGroup = false;
        notifyDataSetChanged();
    }

    /**
     * flip the selection boolean value for the stock and boolean array
     *
     * @param position of the stock that is flipped
     */
    public void toggleSelection(int position) {
        if (selectedItems.get(position)) {
            selectedItems.delete(position);
            stocks.get(position).setSelected(false);
        } else {
            selectedItems.put(position, true);
            stocks.get(position).setSelected(true);
            bSelectionGroup = true;
        }

        notifyItemChanged(position);
    }
}
