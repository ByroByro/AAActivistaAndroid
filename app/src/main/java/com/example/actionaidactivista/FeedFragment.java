package com.example.actionaidactivista;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.feed_adapter;
import com.example.actionaidactivista.adapters.library_adapter;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.models.library_article;
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
public class FeedFragment extends Fragment {

    private RecyclerView mActivitiesRecyclerView;
    private ArrayList<feed> mList;
    private feed_adapter mFeedAdapter;

    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;

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
            mActivitiesRecyclerView = (RecyclerView)root.findViewById(R.id.activities_list_recycler);

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mActivitiesRecyclerView.setLayoutManager(linearLayoutManager);
            mActivitiesRecyclerView.setItemAnimator(new DefaultItemAnimator());

            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(getContext());

            //this will make sure the options menu appear
            setHasOptionsMenu(true);
            //get feeds
            getApprovedFeeds();

        } catch (Exception e) {
            Toast.makeText(getContext(),"Error loading UI",Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try{
            inflater.inflate(R.menu.search_menu,menu);
            MenuItem item = menu.findItem(R.id.m_search);
            SearchView searchView = (SearchView)item.getActionView();
            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(mFeedAdapter != null) {
                        mFeedAdapter.getFilter().filter(newText);
                    }
                    return false;
                }
            });
        }catch (Exception e){
            System.out.println(e);
        }
    }

    //get approved feeds
    private void getApprovedFeeds(){
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
            RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");
            Call<ResponseBody> articles = apiInterface.getApprovedFeeds();
            methods.showDialog(mDialog, "Loading feed...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if(result.length() == 0){
                                Toast.makeText(getContext(), "No more feeds.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if(array.length() == 0){
                                methods.showAlert("No more content","There are no feeds or no more feeds",getContext());
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
                                feed.setmLocation(jsonObject.getString("location"));
                                feed.setmGeoLocation(jsonObject.getString("geolocation"));
                                mList.add(feed);
                            }
                            mFeedAdapter = new feed_adapter(mList,getContext());
                            mActivitiesRecyclerView.setAdapter(mFeedAdapter);
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
                    methods.showAlert("List onFailure", t.toString(), getContext());
                }
            });
        }catch (Exception e){
            Toast.makeText(getContext(),"Error raising get event",Toast.LENGTH_SHORT).show();
        }
    }
}
