package com.example.actionaidactivista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.activista_approval_adapter;
import com.example.actionaidactivista.adapters.user_accounts_adapter;
import com.example.actionaidactivista.models.contact;
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

public class UserAccountsActivity extends AppCompatActivity {

    private List<contact> mList;
    private RecyclerView mRecyclerView;
    private user_accounts_adapter mContactAdapter;
    private Dialog mDialog;

    //retrofit
    private ApiInterface apiInterface;

    //variables for pagination
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previousTotal = 0;
    private int view_threshold = 0;
    private int row_num = 30;
    private int page_num = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //layout
            setContentView(R.layout.activity_user_accounts);
            //set title
            try{
                getSupportActionBar().setTitle("Users");
            }catch (Exception e){
                System.out.println(e);
            }
            //initialise api interface
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            //initialize widgets
            mRecyclerView = findViewById(R.id.contact_list_recycler);
            mRecyclerView.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mDialog = new Dialog(this);

            //set on scroll listener
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
                            getMoreUsers();
                            isLoading = true;
                        }
                    }
                }
            });

            //get users
            getUsers();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    //get users
    private void getUsers() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
        RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
        Call<ResponseBody> activistas = apiInterface.getUsers(rows,page);
        methods.showDialog(mDialog, "Loading users...", true);
        activistas.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(UserAccountsActivity.this, "No users.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        mList = new ArrayList<>();
                        contact contact;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            contact = new contact();
                            contact.setmContactID(jsonObject.getString("id"));
                            contact.setmName(jsonObject.getString("name"));
                            contact.setmSurname(jsonObject.getString("surname"));
                            contact.setmDob(jsonObject.getString("dob"));
                            contact.setmGender(jsonObject.getString("gender"));
                            contact.setmOccupation(jsonObject.getString("occupation"));
                            contact.setmProvid(jsonObject.getString("provid"));
                            contact.setmDisid(jsonObject.getString("disid"));
                            contact.setmAccountNo(jsonObject.getString("accno"));
                            contact.setmProfileUrl(jsonObject.getString("profile"));
                            contact.setmStatus(jsonObject.getString("approved"));
                            contact.setmActive(jsonObject.getString("active"));
                            mList.add(contact);
                        }

                        mContactAdapter = new user_accounts_adapter(mList, UserAccountsActivity.this);
                        mRecyclerView.setAdapter(mContactAdapter);
                    } else {
                        Toast.makeText(UserAccountsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", "Request failed.Check your internet connection.", UserAccountsActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showRequestFailedDialog(UserAccountsActivity.this);
            }
        });
    }

    //get more users
    private void getMoreUsers() {
        RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
        RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
        Call<ResponseBody> users = apiInterface.getUsers(rows,page);
        methods.showDialog(mDialog, "Loading more users...", true);
        users.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(UserAccountsActivity.this, "No more users.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        List<contact> list = new ArrayList<>();
                        contact contact;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            contact = new contact();
                            contact.setmContactID(jsonObject.getString("id"));
                            contact.setmName(jsonObject.getString("name"));
                            contact.setmSurname(jsonObject.getString("surname"));
                            contact.setmDob(jsonObject.getString("dob"));
                            contact.setmGender(jsonObject.getString("gender"));
                            contact.setmOccupation(jsonObject.getString("occupation"));
                            contact.setmProvid(jsonObject.getString("provid"));
                            contact.setmDisid(jsonObject.getString("disid"));
                            contact.setmAccountNo(jsonObject.getString("accno"));
                            contact.setmProfileUrl(jsonObject.getString("profile"));
                            contact.setmStatus(jsonObject.getString("approved"));
                            contact.setmActive(jsonObject.getString("active"));
                            list.add(contact);
                        }

                       mContactAdapter.addUser(list);
                    } else {
                        Toast.makeText(UserAccountsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", e.toString(), UserAccountsActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showAlert("Failure", "Request failed.Check your internet connection.", UserAccountsActivity.this);
            }
        });
    }
}
