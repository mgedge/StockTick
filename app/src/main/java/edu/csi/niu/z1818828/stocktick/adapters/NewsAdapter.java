package edu.csi.niu.z1818828.stocktick.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.MainActivity;
import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.objects.Article;
import edu.csi.niu.z1818828.stocktick.objects.Stock;
import edu.csi.niu.z1818828.stocktick.ui.stock.StockActivity;

import static android.os.FileUtils.copy;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<Article> articles;
    private LayoutInflater inflater;
    private Context context;
    private AdapterView.OnItemClickListener listener;

    public void setOnItemClickListener(NewsAdapter.OnItemClickListener onItemClickListener) {
    }

    public interface ContextProvider {
        Context getContext();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    public NewsAdapter(Context context, List<Article> articles) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.articles = articles;
    }

    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.news_item, parent, false);
        return new NewsAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.ViewHolder holder, int position) {
        Article article = articles.get(position);

//        URL url = null;
//
//        try {
//            url = new URL(article.getImageUrl());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }


//        try {
//            Bitmap icon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            holder.imageViewPicture.setImageBitmap(icon);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        if(article.getImageUrl() != null) {
//            new loadImage(article.getImageUrl(), holder.imageViewPicture);
//        }


        System.out.println(article.getImageUrl());

        holder.textViewNewsTitle.setText(article.getTitle());
        holder.textViewSource.setText(article.getSource());
//        holder.imageViewPicture.setImageBitmap();

        loadImage(holder.imageViewPicture, article.getImageUrl());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, position + " has been selected", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(article.getUrl()));
            v.getContext().startActivity(intent);

//            ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.nav_host_fragment, new StockFragment()).commit();

//            Fragment fragment = new Fragment();
//            FragmentManager fm = context.get
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container_view_tag, fragment);
//            ft.commit();
//            Intent intent = new Intent(v.getContext(), StockActivity.class);
//            intent.putExtra("stockSymbol", stock.getSymbol());
//            intent.putExtra("stockName", stock.getStockName());
//            intent.putExtra("volume", stock.getVolume());
//            intent.putExtra("pcntRange", stock.getRange());

            //Store data for card
            //intent.putExtra(EXTRA, item.getTitle());

            //Start new activity
//            v.getContext().startActivity(intent);
//
//            if(context.getClass().equals(MainActivity.class)) {
//                ((MainActivity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNewsTitle;
        TextView textViewSource;
        ImageView imageViewPicture;

        public ViewHolder(@NonNull View itemView, AdapterView.OnItemClickListener listener) {
            super(itemView);

            textViewNewsTitle = itemView.findViewById(R.id.textViewNewsTitle);
            textViewSource = itemView.findViewById(R.id.textViewSource);
            imageViewPicture = itemView.findViewById(R.id.imageView);
        }
    }

    public void loadImage(ImageView image, String url) {
        new Thread(new Runnable() {
            Bitmap icon = null;

            @Override
            public void run() {
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    icon = BitmapFactory.decodeStream(in);
                    image.setImageBitmap(icon);
                    //TODO https://developer.android.com/topic/performance/graphics/load-bitmap
                } catch (Exception e) {
                    image.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }).start();
    }

}