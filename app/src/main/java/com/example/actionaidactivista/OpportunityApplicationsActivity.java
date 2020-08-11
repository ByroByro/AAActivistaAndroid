package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.opportunity_adapter;
import com.example.actionaidactivista.adapters.opportunity_applications_adapter;
import com.example.actionaidactivista.models.opportunity;
import com.example.actionaidactivista.models.opportunityapplications;
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

public class OpportunityApplicationsActivity extends AppCompatActivity {

    private List<opportunityapplications> mList;
    private RecyclerView mRecyclerView;
    private opportunity_applications_adapter mAdapter;
    private Dialog mDialog;

    //retrofit
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set content view
            setContentView(R.layout.activity_opportunity_applications);
            try {
                getSupportActionBar().setTitle("Opportunity Applications");
            } catch (Exception e) {
                System.out.println(e);
            }
            //initialize widgets
            mRecyclerView = findViewById(R.id.opp_app_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(this);

            getOpportunityApplications();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    //method for getting opportunity applications
    private void getOpportunityApplications() {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
            RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");
            Call<ResponseBody> opps = apiInterface.GetOpportunityApplications();
            methods.showDialog(mDialog, "Loading opportunity applications...", true);
            opps.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(OpportunityApplicationsActivity.this, "No more data.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                Toast.makeText(OpportunityApplicationsActivity.this, "No more data.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mList = new ArrayList<>();
                            opportunityapplications opportunity;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                opportunity = new opportunityapplications();
                                opportunity.setId(jsonObject.getString("id"));
                                opportunity.setTitle(jsonObject.getString("title"));
                                opportunity.setDescription(jsonObject.getString("description"));
                                opportunity.setApplications(jsonObject.getString("applications"));
                                opportunity.setLocation(jsonObject.getString("location"));
                                mList.add(opportunity);
                            }

                            mAdapter = new opportunity_applications_adapter(mList, OpportunityApplicationsActivity.this);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            Toast.makeText(OpportunityApplicationsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), OpportunityApplicationsActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", t.toString(), OpportunityApplicationsActivity.this);
                }
            });
        } catch (Exception e) {
            methods.showAlert("Failure", e.toString(), OpportunityApplicationsActivity.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
                    if (mAdapter != null) {
                        mAdapter.getFilter().filter(newText);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            MenuItem item = menu.findItem(R.id.m_refresh);
            item.setVisible(false);
        } catch (Exception e) {
            System.out.println(e);
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
