package com.example.actionaidactivista;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ContentModerationFragment extends Fragment {

    private RecyclerView mActivitiesRecyclerView;
    private ArrayList<feed> mList;
    private content_mod_feed_adapter mFeedAdapter;

    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;

    public ContentModerationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_content_moderation, container, false);

            //set title
            //((MainBottomNavActivity) getActivity()).setActionBarTitle(getString(R.string.content_moderation_bar_title));
            ((MainBottomNavActivity) getActivity()).setActionBarTitle("Content Moderation");
            mActivitiesRecyclerView = (RecyclerView)root.findViewById(R.id.activities_list_recycler);

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mActivitiesRecyclerView.setLayoutManager(linearLayoutManager);
            mActivitiesRecyclerView.setItemAnimator(new DefaultItemAnimator());

            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(getContext());

            //get feeds
            getAllFeeds();

        }catch (Exception e){
            Toast.makeText(getContext(),"Error loading UI",Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    //get all feeds
    private void getAllFeeds(){
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
            RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");
            Call<ResponseBody> articles = apiInterface.getAllFeeds();
            methods.showDialog(mDialog, "Loading feeds...", true);
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
                                feed.setmStatus(jsonObject.getString("status"));
                                feed.setmLocation(jsonObject.getString("location"));
                                feed.setmGeoLocation(jsonObject.getString("geolocation"));
                                mList.add(feed);
                            }
                            mFeedAdapter = new content_mod_feed_adapter(mList,getContext());
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
