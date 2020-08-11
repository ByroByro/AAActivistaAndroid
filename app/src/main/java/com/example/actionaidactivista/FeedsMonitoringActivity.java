package com.example.actionaidactivista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.feed_monitoring_adapter;
import com.example.actionaidactivista.adapters.library_adapter;
import com.example.actionaidactivista.models.feedmonitor;
import com.example.actionaidactivista.models.library_article;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
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

public class FeedsMonitoringActivity extends AppCompatActivity {

    private List<feedmonitor> mList;
    private RecyclerView mRecyclerView;
    private feed_monitoring_adapter mAdapter;
    private TextInputEditText mRows;

    private Dialog mDialog;
    //retrofit
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //inflate layout
            setContentView(R.layout.activity_feeds_monitoring);
            //set title
            getSupportActionBar().setTitle("Feeds Activity Monitor");

            //initialize widgets
            mRecyclerView = findViewById(R.id.monitor_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(this);
            mRows = (TextInputEditText) findViewById(R.id.rows);

        } catch (Exception e) {
            Toast.makeText(this, "error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feeds_monitor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int i = item.getItemId();
            String rows = mRows.getText().toString().trim();
            if (rows.equalsIgnoreCase("")) {
                methods.showAlert("Invalid number", "Enter a number.", this);
            } else if (Integer.valueOf(rows) < 0 || Integer.valueOf(rows) == 0) {
                methods.showAlert("Invalid number", "Enter a positive number.", this);
            } else {
                switch (i) {
                    case R.id.action_by_feeds_uploaded:
                        getTopUsers(rows, "byfeedsuploaded");
                        break;
                    case R.id.action_by_feeds_tagged:
                        getTopUsers(rows,"bytags");
                        break;
                    case R.id.action_by_feeds_liking:
                        getTopUsers(rows,"byfeedlikes");
                        break;
                }
            }
        }catch (Exception e){
            Toast.makeText(FeedsMonitoringActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //get users method
    private void getTopUsers(String user_rows,String type) {
        RequestBody page = RequestBody.create(MultipartBody.FORM, "1");
        RequestBody rows = RequestBody.create(MultipartBody.FORM, "5");

        RequestBody u_rows = RequestBody.create(MultipartBody.FORM, user_rows);
        RequestBody u_type = RequestBody.create(MultipartBody.FORM, type);

        Call<ResponseBody> users = apiInterface.GetTopUsers(u_rows,u_type);
        methods.showDialog(mDialog, "Loading " + user_rows + " users...", true);
        users.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if(result.length() == 0){
                            Toast.makeText(FeedsMonitoringActivity.this, "No more users.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        if(array.length() == 0){
                            Toast.makeText(FeedsMonitoringActivity.this, "No more users.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mList = new ArrayList<>();
                        feedmonitor user;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            user = new feedmonitor();
                            user.setFname(jsonObject.getString("fname"));
                            user.setNooffeeds(jsonObject.getString("nooffeeds"));
                            user.setSname(jsonObject.getString("lname"));
                            user.setUserid(jsonObject.getString("userid"));
                            user.setProfile(jsonObject.getString("profile"));
                            mList.add(user);
                        }
                        mAdapter = new feed_monitoring_adapter(mList,FeedsMonitoringActivity.this,type);
                        mRecyclerView.setAdapter(mAdapter);
                    } else {
                        Toast.makeText(FeedsMonitoringActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", e.toString(), FeedsMonitoringActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showAlert("Failure", t.toString(), FeedsMonitoringActivity.this);
            }
        });
    }
}
