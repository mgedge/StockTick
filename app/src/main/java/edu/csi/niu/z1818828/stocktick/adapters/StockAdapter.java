package edu.csi.niu.z1818828.stocktick.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.objects.Stock;
import edu.csi.niu.z1818828.stocktick.ui.stock.StockActivity;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {
    private final List<Stock> stocks;
    private final LayoutInflater inflater;
    private final Context context;

    /**
     * Creates an adapter to bind stock cards into a recycler view
     *
     * @param context the context from which the card is drawn
     * @param stocks  the list of stocks to display
     */
    public StockAdapter(Context context, List<Stock> stocks) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.stocks = stocks;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.mover_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Get the stock information
        Stock stock = stocks.get(position);

        //Set the texts
        holder.textViewSymbol.setText(stock.getSymbol());
        holder.textViewExchange.setText(stock.getExchange());
        holder.textViewStockName.setText(stock.getStockName());
        holder.textViewPrice.setText(String.format("%.2f", stock.getPrice()));
        holder.textViewPercentChange.setText(stock.formatChangePercentage(stock.getChangePct()));

        //Change the stock change color
        if (stock.getChangePct() >= 0) {
            holder.textViewPercentChange.setText("+" + stock.formatChangePercentage(stock.getChangePct()));
            holder.textViewPercentChange.setBackgroundColor(context.getResources().getColor(R.color.colorPositive));
        } else {
            holder.textViewPercentChange.setBackgroundColor(context.getResources().getColor(R.color.colorNegative));
        }

        //When card is clicked, open the stock activity for more information
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StockActivity.class);
                intent.putExtra("stockSymbol", stock.getSymbol());

                //If stock name exists, add that
                try {
                    intent.putExtra("stockName", stock.getStockName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Start the activity
                context.startActivity(intent);
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
        TextView textViewChange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Bind the views
            textViewSymbol = itemView.findViewById(R.id.textViewNewsTitle);
            textViewExchange = itemView.findViewById(R.id.textViewSource);
            textViewStockName = itemView.findViewById(R.id.textViewStockName);
            textViewPercentChange = itemView.findViewById(R.id.textViewPercentChange);
            textViewChange = itemView.findViewById(R.id.textViewChange);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}
