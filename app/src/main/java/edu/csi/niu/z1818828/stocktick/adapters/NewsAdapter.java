package edu.csi.niu.z1818828.stocktick.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.objects.Article;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final List<Article> articles;
    private final LayoutInflater inflater;
    private final Context context;

    /**
     * Creates an adapter to bind news cards into a recycler view
     *
     * @param context  the context from which the card is drawn
     * @param articles the list of articles to display
     */
    public NewsAdapter(Context context, List<Article> articles) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.articles = articles;
    }

    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.news_item, parent, false);
        return new NewsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.ViewHolder holder, int position) {
        //Retrieve the article information
        Article article = articles.get(position);

        //Retrieve the image
        loadImage(holder.imageViewPicture, article.getImageUrl());

        //Set the views
        holder.textViewNewsTitle.setText(article.getTitle());
        holder.textViewSource.setText(article.getSource());

        //Set a click listener to open the article
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(article.getUrl()));
            v.getContext().startActivity(intent);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Bind the views
            textViewNewsTitle = itemView.findViewById(R.id.textViewNewsTitle);
            textViewSource = itemView.findViewById(R.id.textViewSource);
            imageViewPicture = itemView.findViewById(R.id.imageView);
        }
    }

    /**
     * Load the image of the article using the Glide library.
     * <p>
     * The purpose of this library is to improve the loading speed for the images.
     * During development, binding the images would crash the app since the images were high res,
     * causing the app to run out of memory. This library vastly improves the performance of this
     * method.
     * <p>
     * Credits: https://github.com/bumptech/glide
     *
     * @param image the imageView to be updated
     * @param url   the url of the image to be placed in image
     */
    private void loadImage(ImageView image, String url) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Glide.with(context).load(url).into(image);
                } catch (Exception e) {
                    //If anything went wrong, hide it from the view
                    image.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        });
    }
}
