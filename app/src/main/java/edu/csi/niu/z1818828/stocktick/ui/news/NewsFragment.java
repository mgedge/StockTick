package edu.csi.niu.z1818828.stocktick.ui.news;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.util.ArrayList;
import java.util.List;

import edu.csi.niu.z1818828.stocktick.R;
import edu.csi.niu.z1818828.stocktick.adapters.NewsAdapter;
import edu.csi.niu.z1818828.stocktick.objects.Article;

public class NewsFragment extends Fragment {
    private RecyclerView recyclerViewNews;
    private EditText editTextSearch;
    private ImageButton imageButtonSearch;
    private NewsAdapter newsAdapter;

    private final List<Article> articleList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the title bar
        getActivity().setTitle("News search");

        //Create the adapter
        newsAdapter = new NewsAdapter(getContext(), articleList);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news, container, false);

        //Initialize the views
        recyclerViewNews = root.findViewById(R.id.recyclerViewNews);
        editTextSearch = root.findViewById(R.id.editTextSearch);
        editTextSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the users hits the enter button on keyboard, retrieve the news
                if ((event.getAction() == KeyEvent.ACTION_UP) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dismissKeyboard(editTextSearch);
                    articleList.clear();
                    searchNews(editTextSearch.getText().toString());
                    Log.i("NewsFragmentSearch", editTextSearch.getText().toString());
                    return true;
                }
                return false;
            }
        });

        imageButtonSearch = root.findViewById(R.id.imageButtonSearch);
        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(editTextSearch);
                articleList.clear();
                searchNews(editTextSearch.getText().toString());
                Log.i("NewsFragmentSearch", editTextSearch.getText().toString());
            }
        });

        //Set the adapter
        recyclerViewNews.setAdapter(newsAdapter);
        newsAdapter.notifyDataSetChanged();

        //Set manager
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return root;
    }

    /**
     * Lookup the news based on the searchKey
     *
     * @param searchKey - string value from the editText field, used to search for news
     */
    private void searchNews(String searchKey) {
        //Create a background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String key = getResources().getString(R.string.newsAPI);

                    //Create news api client (using NewsAPI's listed Java SDK)
                    // Credits: https://github.com/KwabenBerko/News-API-Java
                    NewsApiClient newsApiClient = new NewsApiClient(key);
                    newsApiClient.getEverything(
                            new EverythingRequest.Builder()
                                    .q(searchKey)
                                    .sortBy("relevancy")
                                    .build(),
                            new NewsApiClient.ArticlesResponseCallback() {
                                @Override
                                public void onSuccess(ArticleResponse articleResponse) {
                                    for (int i = 0; i < articleResponse.getArticles().size(); i++) {
                                        List<com.kwabenaberko.newsapilib.models.Article> articles =
                                                (List<com.kwabenaberko.newsapilib.models.Article>) articleResponse.getArticles();

                                        //Add the object to the list
                                        articleList.add(new Article(
                                                articles.get(i).getTitle(),
                                                articles.get(i).getSource().getName(),
                                                articles.get(i).getUrl(),
                                                articles.get(i).getUrlToImage()
                                        ));

                                        //Update the adapter
                                        newsAdapter.notifyDataSetChanged();

                                        //Log the results
                                        Log.i("News", articles.get(i).getTitle());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    Toast.makeText(getActivity(), "Could not connect to the API", Toast.LENGTH_SHORT).show();
                                    Log.e("JSONNews", String.valueOf(throwable));
                                }
                            }
                    );

                } catch (Exception e) {
                    e.printStackTrace();

//                    try {
//                        Thread.sleep(10000);
//                    }
//                    catch (InterruptedException interruptedException) {
//                        interruptedException.printStackTrace();
//                    }
                }
            }
        }).start();

    }

    /**
     * Hides the keyboard from the EditText
     *
     * @param editTextSearch - the editText bar to dismiss from
     */
    private void dismissKeyboard(EditText editTextSearch) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }
}