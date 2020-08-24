package com.example.actionaidactivista;


import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.adapters.feed_adapter;
import com.example.actionaidactivista.database.feed_cache;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private RecyclerView mActivitiesRecyclerView;
    private ArrayList<feed> mList;
    private ArrayList<feed> mOfflineList;
    private feed_adapter mFeedAdapter;

    private CardView mOnErrorCard;
    private CircleImageView mOnErrorImageView;
    private TextView mOnErrorTextView;

    private Dialog dialog;
    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;

    private boolean hasLoadedFeedsBefore = false;
    private boolean hasLoadedFromOnline = false;
    private boolean currentlyLoadingOnline = false;
    private feed_cache feed_cache;
    private Handler handler;
    private LinearLayout rootLayout;

    //variables for pagination
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previousTotal = 0;
    private int view_threshold = 0;
    private int row_num = 30;
    private int page_num = 1;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_feed, container, false);
            //set title
            ((MainBottomNavActivity) getActivity()).setActionBarTitle(getString(R.string.feed_app_bar_title));
            mActivitiesRecyclerView = (RecyclerView) root.findViewById(R.id.activities_list_recycler);

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mActivitiesRecyclerView.setLayoutManager(linearLayoutManager);
            mActivitiesRecyclerView.setItemAnimator(new DefaultItemAnimator());

            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(getContext());
            dialog = new Dialog(getContext());
            rootLayout = (LinearLayout) root.findViewById(R.id.rootLayout);

            mOnErrorImageView = (CircleImageView) root.findViewById(R.id.imageView);
            mOnErrorCard = (CardView) root.findViewById(R.id.on_error_card);
            mOnErrorTextView = (TextView) root.findViewById(R.id.on_error_text);
            feed_cache = new feed_cache(getActivity(), "", null);
            handler = new Handler();

            //set recycler view on scroll listener
            //raise this event only for online
            mActivitiesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if(currentlyLoadingOnline) {
                        if (dy > 0) {
                            if (isLoading) {
                                if (totalItemCount > previousTotal) {
                                    isLoading = false;
                                    previousTotal = totalItemCount;
                                }
                            }

                            if (!isLoading && (totalItemCount - visibleItemCount) <= (pastVisibleItems + view_threshold)) {
                                page_num++;
                                getApprovedFeedsPagination();
                                isLoading = true;
                            }
                        }
                    }else {
                        //offline
                    }
                }
            });
            //hide all UI widgets
            //mActivitiesRecyclerView.setVisibility(View.GONE);
            mOnErrorCard.setVisibility(View.GONE);

            //this will make sure the options menu appear
            setHasOptionsMenu(true);
            //TODO : CHECK IF USER'S PHONE IS CONNECTED TO EITHER DATA
            //TODO : OR WIFI WHEN RELEASING THE APP ELSE FOR LOCALHOST
            //TODO : CAN GO WITHOUT
            //get feeds if phone is connected to either data or wifi
            //if (methods.isConnected(getContext())) {
            getApprovedFeeds();
            //} else {
            //    mOnErrorCard.setVisibility(View.VISIBLE);
            //}

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading UI", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            inflater.inflate(R.menu.search_menu, menu);
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int selected = item.getItemId();
            switch (selected) {
                case R.id.m_refresh:
                    page_num = 1;
                    getApprovedFeeds();
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //get approved feeds
    private void getApprovedFeeds() {
        try {
            //page_num = 1;
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            Call<ResponseBody> articles = apiInterface.getApprovedFeeds(rows,page);
            methods.showDialog(mDialog, "Loading feed...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {

                            hasLoadedFeedsBefore = true;
                            mActivitiesRecyclerView.setVisibility(View.VISIBLE);
                            mOnErrorCard.setVisibility(View.GONE);

                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(getContext(), "No feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                //Snackbar.make(rootLayout, "There are no feeds or no more feeds", Snackbar.LENGTH_SHORT).show();
                                //methods.showAlert("No more content", "There are no feeds or no more feeds", getContext());
                                Toast.makeText(getContext(), "No feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mList = new ArrayList<>();
                            ArrayList<feed> toCache = new ArrayList<>();
                            hasLoadedFromOnline = true;
                            currentlyLoadingOnline = true;
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
                                feed.setNoOfLikes(jsonObject.getString("nooflikes"));
                                feed.setUploaderName(jsonObject.getString("firstname"));
                                feed.setUploaderSurname(jsonObject.getString("surname"));
                                feed.setUploaderProfile(jsonObject.getString("profile"));
                                mList.add(feed);
                                if (toCache.size() < 20) {
                                    toCache.add(feed);
                                }
                            }
                            mFeedAdapter = new feed_adapter(mList, getContext(), true,rootLayout);
                            mActivitiesRecyclerView.setAdapter(mFeedAdapter);

                            //save to local database as cache
                            //use a background thread
                            new Thread(() -> saveFeedsToCache(toCache)).start();
                        } else {
//                            if (!hasLoadedFromOnline) {
//
//                            } else if (!hasLoadedFeedsBefore) {
//                                mActivitiesRecyclerView.setVisibility(View.GONE);
//                                mOnErrorCard.setVisibility(View.VISIBLE);
//                            } else {
//                                Toast.makeText(getContext(), "Request unsuccessful", Toast.LENGTH_SHORT).show();
//                            }
                            loadFeedFromCache();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), getContext());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
//                    if (!hasLoadedFeedsBefore) {
//                        mActivitiesRecyclerView.setVisibility(View.GONE);
//                        mOnErrorCard.setVisibility(View.VISIBLE);
//                    } else {
//                        methods.showAlert("Failure", "An error occurred.Please check your connection.", getContext());
//                    }
                    loadFeedFromCache();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }

    //perform pagination
    private void getApprovedFeedsPagination() {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            Call<ResponseBody> articles = apiInterface.getApprovedFeeds(rows,page);
            methods.showDialog(mDialog, "Loading feed...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {

                            hasLoadedFeedsBefore = true;
                            mActivitiesRecyclerView.setVisibility(View.VISIBLE);
                            mOnErrorCard.setVisibility(View.GONE);

                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(getContext(), "No more feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                //Snackbar.make(rootLayout, "There are no feeds or no more feeds", Snackbar.LENGTH_SHORT).show();
                                //methods.showAlert("No more content", "There are no feeds or no more feeds", getContext());
                                Toast.makeText(getContext(), "No more feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ArrayList<feed> moreFeeds = new ArrayList<>();
                            hasLoadedFromOnline = true;
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
                                feed.setNoOfLikes(jsonObject.getString("nooflikes"));
                                feed.setUploaderName(jsonObject.getString("firstname"));
                                feed.setUploaderSurname(jsonObject.getString("surname"));
                                feed.setUploaderProfile(jsonObject.getString("profile"));
                                moreFeeds.add(feed);
                            }

                            mFeedAdapter.addFeed(moreFeeds);

                        } else {
                            Toast.makeText(getContext(), "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), getContext());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", "An error occurred.Please check your connection.", getContext());
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * performs saving of feeds in cache
     */
    private void saveFeedsToCache(ArrayList<feed> list) {
        try {
            feed_cache.deleteAllRecordsFromTable("Feeds_table");
            for (feed f : list) {
                feed_cache.insertFeed(f);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error trying to save cache", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * performs get feeds from local cache
     */
    private void loadFeedFromCache() {
        try {
            new Thread(() -> {
                mList = new ArrayList<>();
                Cursor cursor = feed_cache.getFeeds();
                feed feed;
                try {
                    while (cursor.moveToNext()) {
                        feed = new feed();
                        feed.setmId(cursor.getString(cursor.getColumnIndex("FEEDID")));
                        feed.setmDescription(cursor.getString(cursor.getColumnIndex("DES")));
                        feed.setmDate(cursor.getString(cursor.getColumnIndex("DATEPOSTED")));
                        feed.setmFileType(cursor.getString(cursor.getColumnIndex("FILETYPE")));
                        feed.setmIntType(cursor.getString(cursor.getColumnIndex("INTTYPE")));
                        feed.setmMimeType(cursor.getString(cursor.getColumnIndex("MIMETYPE")));
                        feed.setmPath(cursor.getString(cursor.getColumnIndex("PATH")));
                        feed.setmUrl(cursor.getString(cursor.getColumnIndex("URL")));
                        feed.setmContent(cursor.getString(cursor.getColumnIndex("CONTENT")));
                        feed.setmLocation(cursor.getString(cursor.getColumnIndex("LOCATION")));
                        feed.setmGeoLocation(cursor.getString(cursor.getColumnIndex("GEOLOCATION")));
                        feed.setmUploaderId(cursor.getString(cursor.getColumnIndex("USERID")));
                        mList.add(feed);
                    }

                    handler.post(() -> {
                        try {
                            mFeedAdapter = new feed_adapter(mList, getContext(), false,rootLayout);
                            mActivitiesRecyclerView.setAdapter(mFeedAdapter);
                            if (mFeedAdapter.getItemCount() > 0 && !mFeedAdapter.isOnline) {
                                //Snackbar.make(rootLayout, "VIEWING OFFLINE DATA", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(getContext(), "VIEWING OFFLINE DATA.", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            System.out.println(e);
                        }
                    });

                } catch (Exception e) {
                    Log.e("tag", e.toString());
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //onDestroy

    @Override
    public void onDestroy() {
        try {
            mFeedAdapter.releaseExoPlayer();
        }catch (Exception e){
            System.out.println(e);
        }
        super.onDestroy();
    }
}
