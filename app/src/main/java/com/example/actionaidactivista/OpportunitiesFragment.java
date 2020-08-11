package com.example.actionaidactivista;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.example.actionaidactivista.adapters.contact_adapter;
import com.example.actionaidactivista.adapters.opportunity_adapter;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.models.opportunity;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class OpportunitiesFragment extends Fragment {

    private List<opportunity> mList;
    private RecyclerView mRecyclerView;
    private opportunity_adapter mOpportunityAdapter;
    private Dialog mDialog;

    //retrofit
    private ApiInterface apiInterface;

    public OpportunitiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_opportunities, container, false);
            //set title
            ((MainBottomNavActivity) getActivity()).setActionBarTitle(getString(R.string.opportunities_app_bar_title));
            //initialize widgets
            mRecyclerView = root.findViewById(R.id.oppo_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(getContext());

            setHasOptionsMenu(true);
            //get opportunities list
            getOpportunities();

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
                    if(mOpportunityAdapter != null) {
                        mOpportunityAdapter.getFilter().filter(newText);
                    }
                    return false;
                }
            });
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void getOpportunities() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
        RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");
        Call<ResponseBody> opps = apiInterface.getOpportunities();
        methods.showDialog(mDialog, "Loading opportunities...", true);
        opps.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if(result.length() == 0){
                            Toast.makeText(getContext(), "No more opportunities.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        mList = new ArrayList<>();
                        opportunity opportunity;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            opportunity = new opportunity();
                            opportunity.setmID(jsonObject.getString("id"));
                            opportunity.setmTitle(jsonObject.getString("title"));
                            opportunity.setmDescription(jsonObject.getString("des"));
                            opportunity.setmDateposted(jsonObject.getString("dateposted"));
                            opportunity.setmClosingdate(jsonObject.getString("closingdate"));
                            opportunity.setmLocation(jsonObject.getString("location"));
                            if (jsonObject.getString("docs").equalsIgnoreCase("")) {
                                opportunity.setmDocsLink("N/A");
                            } else {
                                opportunity.setmDocsLink(jsonObject.getString("docs"));
                            }
                            mList.add(opportunity);
                        }

                        mOpportunityAdapter = new opportunity_adapter(mList,getContext());
                        mRecyclerView.setAdapter(mOpportunityAdapter);
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
                methods.showAlert("Failure", t.toString(), getContext());
            }
        });
    }

}
