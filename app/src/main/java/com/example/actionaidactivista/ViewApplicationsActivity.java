package com.example.actionaidactivista;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.adapters.applications_adapter;
import com.example.actionaidactivista.models.applications;
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

public class ViewApplicationsActivity extends AppCompatActivity {

    private List<applications> mList;
    private RecyclerView mRecyclerView;
    private applications_adapter mAdapter;
    private Dialog mDialog;

    //retrofit
    private ApiInterface apiInterface;

    //variables for pagination
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previousTotal = 0;
    private int view_threshold = 0;
    private int row_num = 30;
    private int page_num = 1;

    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_view_applications);
            try {
                getSupportActionBar().setTitle("Opportunity Applicants");
            } catch (Exception e) {
                System.out.println(e);
            }
            //initialize widgets
            mRecyclerView = findViewById(R.id.view_apps_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(this);
            //on scroll listener
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            getApplicationsPagination(id);
                            isLoading = true;
                        }
                    }
                }
            });
            //get id
            Intent intent = getIntent();
            id = intent.getStringExtra("opp_id");
            getApplications(id);
        } catch (Exception e) {

        }
    }

    //method for getting opportunity applications
    private void getApplications(String id) {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));

            RequestBody opid = RequestBody.create(MultipartBody.FORM, id);

            Call<ResponseBody> opps = apiInterface.GetApplicants(opid, rows, page);
            methods.showDialog(mDialog, "Loading applications...", true);
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
                                Toast.makeText(ViewApplicationsActivity.this, "No data.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                Toast.makeText(ViewApplicationsActivity.this, "No data.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mList = new ArrayList<>();
                            applications opportunity;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                opportunity = new applications();
                                opportunity.setName(jsonObject.getString("fname"));
                                opportunity.setSurname(jsonObject.getString("sname"));
                                opportunity.setGender(jsonObject.getString("gender"));
                                opportunity.setId(jsonObject.getString("id"));
                                opportunity.setPostid(jsonObject.getString("postid"));
                                opportunity.setApplicantid(jsonObject.getString("applicantid"));
                                opportunity.setCoverletterurl(jsonObject.getString("coverletterurl"));
                                opportunity.setMotiletterurl(jsonObject.getString("motiletterurl"));
                                opportunity.setCvurl(jsonObject.getString("cvurl"));
                                opportunity.setMotivideourl(jsonObject.getString("motivideourl"));
                                opportunity.setMotipicurl(jsonObject.getString("motipicurl"));
                                opportunity.setAudio(jsonObject.getString("motiaudurl"));
                                mList.add(opportunity);
                            }

                            mAdapter = new applications_adapter(mList, ViewApplicationsActivity.this);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            Toast.makeText(ViewApplicationsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), ViewApplicationsActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showRequestFailedDialog(ViewApplicationsActivity.this);
                }
            });
        } catch (Exception e) {
            methods.showAlert("Failure", e.toString(), ViewApplicationsActivity.this);
        }
    }

    //method for getting more opportunity applications
    private void getApplicationsPagination(String id) {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));

            RequestBody opid = RequestBody.create(MultipartBody.FORM, id);

            Call<ResponseBody> opps = apiInterface.GetApplicants(opid, rows, page);
            methods.showDialog(mDialog, "Loading more applications...", true);
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
                                Toast.makeText(ViewApplicationsActivity.this, "No more data.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                Toast.makeText(ViewApplicationsActivity.this, "No more data.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            List<applications> list = new ArrayList<>();
                            applications opportunity;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                opportunity = new applications();
                                opportunity.setName(jsonObject.getString("fname"));
                                opportunity.setSurname(jsonObject.getString("sname"));
                                opportunity.setGender(jsonObject.getString("gender"));
                                opportunity.setId(jsonObject.getString("id"));
                                opportunity.setPostid(jsonObject.getString("postid"));
                                opportunity.setApplicantid(jsonObject.getString("applicantid"));
                                opportunity.setCoverletterurl(jsonObject.getString("coverletterurl"));
                                opportunity.setMotiletterurl(jsonObject.getString("motiletterurl"));
                                opportunity.setCvurl(jsonObject.getString("cvurl"));
                                opportunity.setMotivideourl(jsonObject.getString("motivideourl"));
                                opportunity.setMotipicurl(jsonObject.getString("motipicurl"));
                                opportunity.setAudio(jsonObject.getString("motiaudurl"));
                                list.add(opportunity);
                            }

                            mAdapter.addApplicant(list);
                        } else {
                            Toast.makeText(ViewApplicationsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), ViewApplicationsActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showRequestFailedDialog(ViewApplicationsActivity.this);
                }
            });
        } catch (Exception e) {
            methods.showAlert("Failure", e.toString(), ViewApplicationsActivity.this);
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
