package vijji.codepath.android.nytimessearch.activites;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import vijji.codepath.android.nytimessearch.Article;
import vijji.codepath.android.nytimessearch.ArticleArrayAdapter;
import vijji.codepath.android.nytimessearch.EndlessScrollListener;
import vijji.codepath.android.nytimessearch.R;

public class SearchActivity extends AppCompatActivity {

    GridView gvResults;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    String sQuery;

    public static String beginDate;
    public static boolean bArts, bFashion, bSports;
    public static int sortOrderIndex;

    private static int pageNum;
    private static int filtersOn;

    //for intent activity result code
    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupViews();
    }

    public void setupViews() {

        gvResults = (GridView)findViewById(R.id.gvResults);

        articles = new ArrayList<>();

        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);

        // set up listener for grid item click
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //create an intent to display the article
                Intent i = new Intent(getApplicationContext(),ArticleActivity.class);
                //get the article
                Article article = articles.get(position);
                //pass in that article into the intent
                //i.putExtra("url",article.getWebUrl());
                i.putExtra("article",article);
                //launch the article
                startActivity(i);
            }
        });

        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyDataSetChanged()`
            sendAPIRequest();
            adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                sQuery = query;
                onArticleSearch();
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void launchFilters(MenuItem item) {

        //Toast.makeText(this, "launch filter dialog", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(SearchActivity.this, FilterActivity.class);
        //startActivity(i);
        /*i.putExtra("beginDate", 5); // pass arbitrary data to launched activity
        i.putExtra("bArts", 1);
        i.putExtra("bFashion", 2);
        i.putExtra("bSports", 3);
        i.putExtra("bStyle", 4);
        i.putExtra("sortOrderIndex",6);*/
        startActivityForResult(i, REQUEST_CODE);
    }

    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the request code is same as what is passed  here it is 2
        if(requestCode==20)
        {
            if(data != null)
            {
                // fetch the message String
                sortOrderIndex =data.getIntExtra("sortOrderIndex",0);

                bArts = data.getBooleanExtra("bArts", false);
                bFashion = data.getBooleanExtra("bFashion", false);
                bSports = data.getBooleanExtra("bSports", false);

                beginDate = data.getStringExtra("beginDate");

                filtersOn = data.getIntExtra("filtersOn", 0);

                Toast.makeText(this, "beginDate "+beginDate, Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void onArticleSearch() {

        pageNum = 0;

        // 1. First, clear the array of data
        articles.clear();
        // 2. Notify the adapter of the update
        adapter.notifyDataSetChanged(); // or notifyItemRangeRemoved

        // Check for Network Connectivity
        if (isNetworkAvailable()) {
            // Perform a HTTP GET request with parameters.
            Toast.makeText(this, "Network available!", Toast.LENGTH_SHORT).show();
            sendAPIRequest();
        }
        else
            Toast.makeText(this, "Network not available!", Toast.LENGTH_SHORT).show();
    }


    public void sendAPIRequest() {

        //Toast.makeText(this,"Searching for " + query, Toast.LENGTH_LONG).show();

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

        RequestParams params = new RequestParams();

        // params.put("page",0);
        //params.put("q", sQuery);

        params.put("page", pageNum++);
        params.put("q", sQuery);
        //https://api.nytimes.com/svc/search/v2/articlesearch.json?begin_date=20160112&sort=oldest&fq=news_desk:(%22Education%22%20%22Health%22)&api-key=227c750bb7714fc39ef1559ef1bd8329
        // follow the http request url using the filters - format to do this
        if (filtersOn == 1)
        {
            params.put("sort", (sortOrderIndex == 0 ? "oldest" : "newest"));

            if (beginDate != null)
                params.put("begin_date", beginDate);

            if (bArts || bFashion || bSports) {
                String news_desk = "news_desk:(";
                ;

                if (bArts) {
                    news_desk += "\"Arts\"";
                }
                if (bFashion) {
                    if (bArts)
                        news_desk += " ";
                    news_desk += "\"Fashion & Style\"";
                }
                if (bSports) {
                    if (bArts || bFashion)
                        news_desk += " ";
                    news_desk += "\"Sports\"";
                }

                params.put("fq", news_desk + ")");
            }

        }

        params.put("api-key", "a79e3c76f3fe49c794bcf3670420efe4");


        client.get(url,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                JSONArray articleJsonResult = null;

                try {
                    articleJsonResult = response.getJSONObject("response").getJSONArray("docs");
                    articles.addAll(Article.fromJsonArray(articleJsonResult));
                    adapter.notifyDataSetChanged();
                    Log.d("DEBUG",articles.toString());
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

    }

    //check if internet connection is available
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
