package com.thomaskioko.customlistview;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.thomaskioko.customlistview.adapter.ListAdapter;
import com.thomaskioko.customlistview.app.AppController;
import com.thomaskioko.customlistview.model.Movie;
import com.thomaskioko.customlistview.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * MainActivity class that handles display of the List items. We fetch objects using the Volley library
 *
 * @author <a href="kiokotomas@gmail.com">Thomas Kioko</a>
 */

public class MainActivity extends AppCompatActivity {
    /**
     * Used for logging purposes.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog mProgressDialog;
    private List<Movie> mMovieList = new ArrayList<>();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        /**
         * We uses a LinearLayoutManager to set the orientation of the RecyclerView and
         * make it look like a ListView
         */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mProgressDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        fetchMovies();

    }

    /**
     * This method fetches data from the provided URL using volley library. We then parse the json,
     * store all the json data into an ArrayList as Movie objects.
     */
    private void fetchMovies() {
        Toast.makeText(MainActivity.this, "Executing Task", Toast.LENGTH_SHORT).show();
        // Creating volley request obj
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Constants.URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        hidePDialog();
                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                Movie movie = new Movie();
                                movie.setTitle(obj.getString("title"));
                                movie.setThumbnailUrl(obj.getString("image"));
                                movie.setRating(((Number) obj.get("rating")).doubleValue());
                                movie.setYear(obj.getInt("releaseYear"));

                                // Genre is json array
                                JSONArray genreArray = obj.getJSONArray("genre");
                                ArrayList<String> genre = new ArrayList<>();
                                for (int j = 0; j < genreArray.length(); j++) {
                                    genre.add((String) genreArray.get(j));
                                }
                                movie.setGenre(genre);

                                // adding movie to movies array
                                mMovieList.add(movie);

                                if (mMovieList != null) {
                                    mRecyclerView.setAdapter(new ListAdapter(getApplicationContext(), mMovieList,
                                            new LinkedList<>(Collections.nCopies(mMovieList.size(),
                                                    R.drawable.movie_add_touch))));
                                } else {
                                    Log.e(TAG, "@fetchMovies Error: Adapter is null");
                                }

                            } catch (JSONException jsonException) {
                                Log.e(TAG, "@fetchMovies JsonException Error: " + jsonException.getMessage());
                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, "@fetchMovies Error: " + error.getMessage());
                        Log.e(TAG, "@fetchMovies Error: " + error.getMessage());
                        hidePDialog();

                    }
                });



        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    /**
     * This method checks if the progress dialog is being displayed. If true close it.
     */
    private void hidePDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            fetchMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
