package com.example.actionaidactivista;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.content_mod_feed_adapter;
import com.example.actionaidactivista.adapters.feed_adapter;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContentModerationFragment extends AppCompatActivity {

    private RecyclerView mActivitiesRecyclerView;
    private ArrayList<feed> mList;
    private content_mod_feed_adapter mFeedAdapter;

    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;
    private ProgressBar progressBar;

    //pagination variables
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previousTotal = 0;
    private int view_threshold = 0;
    private int row_num = 30;
    private int page_num = 1;

    public ContentModerationFragment() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Inflate the layout
            setContentView(R.layout.fragment_content_moderation);

            //set title
            getSupportActionBar().setTitle("Content Moderation");
            mActivitiesRecyclerView = (RecyclerView) findViewById(R.id.activities_list_recycler);

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ContentModerationFragment.this);
            mActivitiesRecyclerView.setLayoutManager(linearLayoutManager);
            mActivitiesRecyclerView.setItemAnimator(new DefaultItemAnimator());
            progressBar = new ProgressBar(this);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(this);

            //set recycler view on scroll listener
            mActivitiesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (dy > 0) {
                        if (isLoading) {
                            if (totalItemCount > previousTotal) {
                                isLoading = false;
                                previousTotal = totalItemCount;
                            }
                        }

                        if (!isLoading && (totalItemCount - visibleItemCount) <= (pastVisibleItems + view_threshold)) {
                            page_num++;
                            getAllFeedsPagination();
                            isLoading = true;
                        }
                    }
                }
            });

            //setHasOptionsMenu(true);
            //get feeds
            getAllFeeds();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    //get all feeds
    private void getAllFeeds() {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            Call<ResponseBody> articles = apiInterface.getAllFeeds(rows, page);
            progressBar.setVisibility(View.VISIBLE);
            //methods.showDialog(mDialog, "Loading feeds...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        //methods.showDialog(mDialog, "Dismiss", false);
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(ContentModerationFragment.this, "No feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                methods.showAlert("No content", "There are no feeds", ContentModerationFragment.this);
                                //Toast.makeText(getContext(), "No more feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mList = new ArrayList<>();
                            feed feed;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                feed = new feed();
                                feed.setmId(jsonObject.getString("id"));
                                feed.setmDescription(jsonObject.getString("des"));
                                feed.setmDate(jsonObject.getString("dateposted"));
                                feed.setmFileType(jsonObject.getString("filetype"));
                                feed.setmIntType(jsonObject.getString("inttype"));
                                feed.setmMimeType(jsonObject.getString("mimetype"));
                                feed.setmPath(jsonObject.getString("path"));
                                feed.setmUrl(jsonObject.getString("url"));
                                feed.setmContent(jsonObject.getString("content"));
                                feed.setmStatus(jsonObject.getString("status"));
                                feed.setmLocation(jsonObject.getString("location"));
                                feed.setmGeoLocation(jsonObject.getString("geolocation"));
                                mList.add(feed);
                            }
                            mFeedAdapter = new content_mod_feed_adapter(mList, ContentModerationFragment.this);
                            mActivitiesRecyclerView.setAdapter(mFeedAdapter);
                        } else {
                            Toast.makeText(ContentModerationFragment.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(),ContentModerationFragment.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    //methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", "Request failed.Check your internet connection.", ContentModerationFragment.this);
                }
            });
        } catch (Exception e) {
            Toast.makeText(ContentModerationFragment.this, "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }

    //get next feeds through pagination
    private void getAllFeedsPagination() {
        try {

            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            Call<ResponseBody> articles = apiInterface.getApprovedFeeds(rows, page);
            progressBar.setVisibility(View.VISIBLE);
            //methods.showDialog(mDialog, "Loading more feeds...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        //dismiss progress
                        progressBar.setVisibility(View.GONE);
                        //methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(ContentModerationFragment.this, "No more feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                //Snackbar.make(rootLayout, "There are no feeds or no more feeds", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(ContentModerationFragment.this, "No more feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ArrayList<feed> moreFeeds = new ArrayList<>();
                            feed feed;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                feed = new feed();
                                feed.setmId(jsonObject.getString("id"));
                                feed.setmDescription(jsonObject.getString("des"));
                                feed.setmDate(jsonObject.getString("dateposted"));
                                feed.setmFileType(jsonObject.getString("filetype"));
                                feed.setmIntType(jsonObject.getString("inttype"));
                                feed.setmMimeType(jsonObject.getString("mimetype"));
                                feed.setmPath(jsonObject.getString("path"));
                                feed.setmUrl(jsonObject.getString("url"));
                                feed.setmContent(jsonObject.getString("content"));
                                feed.setmLocation(jsonObject.getString("location"));
                                feed.setmGeoLocation(jsonObject.getString("geolocation"));
                                feed.setmUploaderId(jsonObject.getString("userid"));
                                moreFeeds.add(feed);
                            }

                            mFeedAdapter.addFeed(moreFeeds);

                        } else {
                            Toast.makeText(ContentModerationFragment.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", "Request failed.Check your internet connection.", ContentModerationFragment.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //dismiss progress
                    progressBar.setVisibility(View.GONE);
                    //methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", "An error occurred.Please check your connection.", ContentModerationFragment.this);
                }
            });
        } catch (Exception e) {
            Toast.makeText(ContentModerationFragment.this, "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        try {
            getMenuInflater().inflate(R.menu.search_menu, menu);
            MenuItem item = menu.findItem(R.id.m_search);
            SearchView searchView = (SearchView) item.getActionView();
            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (mFeedAdapter != null) {
                        mFeedAdapter.getFilter().filter(newText);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {

        MenuItem item = menu.findItem(R.id.m_refresh);
        item.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    //onDestroy
    @Override
    public void onDestroy() {
        try {
            mFeedAdapter.releaseExoPlayer();
        } catch (Exception e) {
            System.out.println(e);
        }
        super.onDestroy();
    }

    //onBackPressed

    @Override
    public void onBackPressed() {
        try {
            mFeedAdapter.releaseExoPlayer();
        } catch (Exception e) {
            System.out.println(e);
        }
        super.onBackPressed();
    }
}
