package com.example.actionaidactivista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.comments_adapter;
import com.example.actionaidactivista.adapters.feed_adapter;
import com.example.actionaidactivista.models.comment;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.snackbar.Snackbar;
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

public class FeedCommentsActivity extends AppCompatActivity {

    private Intent intent;
    private String feedId;
    private RecyclerView comments;
    private List<comment> commentsList;
    private comments_adapter comments_adapter;
    private ApiInterface apiInterface;
    private Dialog dialog;
    private LinearLayout rootLayout;

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
            //set content
            setContentView(R.layout.activity_feed_comments);
            //set title
            try {
                getSupportActionBar().setTitle("Feed Comments");
            } catch (Exception e) {
                System.out.println(e);
            }
            //get extra string
            intent = getIntent();
            feedId = intent.getStringExtra("feed_id");
            //commentsList = new ArrayList<>();
            comments = (RecyclerView)findViewById(R.id.comments_list_recycler);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            comments.setLayoutManager(linearLayoutManager);
            comments.setItemAnimator(new DefaultItemAnimator());

            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            dialog = new Dialog(this);
            rootLayout = (LinearLayout) findViewById(R.id.root);

            comments.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            getFeedCommentsPagination();
                            isLoading = true;
                        }
                    }
                }
            });

            //get comments
            getFeedComments();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    //get comments
    private void getFeedComments() {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            RequestBody feed_id = RequestBody.create(MultipartBody.FORM, String.valueOf(feedId));
            Call<ResponseBody> articles = apiInterface.GetFeedComments(feed_id, rows, page);
            methods.showDialog(dialog, "Loading comments...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(dialog, "Dismiss", false);
                        if (response.isSuccessful()) {

                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(FeedCommentsActivity.this, "No comments.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                Toast.makeText(FeedCommentsActivity.this, "No comments.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            commentsList = new ArrayList<>();

                            comment comm;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                comm = new comment();
                                comm.setCommId(jsonObject.getString("commid"));
                                comm.setCommentorId(jsonObject.getString("commentorid"));
                                comm.setDateCommented(jsonObject.getString("date"));
                                comm.setComment(jsonObject.getString("comment"));
                                comm.setCommType(jsonObject.getString("commtype"));
                                if (!jsonObject.getString("commtype").equalsIgnoreCase("user")) {
                                    comm.setFirstName("n/a");
                                    comm.setSurname("n/a");
                                    comm.setProfilePic("n/a");
                                } else {
                                    comm.setFirstName(jsonObject.getString("fname"));
                                    comm.setSurname(jsonObject.getString("surname"));
                                    comm.setProfilePic(jsonObject.getString("profile"));
                                }
                                commentsList.add(comm);
                            }
                            comments_adapter = new comments_adapter(commentsList, FeedCommentsActivity.this);
                            comments.setAdapter(comments_adapter);

                        } else {
                            Toast.makeText(FeedCommentsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), FeedCommentsActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(dialog, "Dismiss", false);
                    methods.showAlert("Failure", "An error occurred.Please check your connection.", FeedCommentsActivity.this);
                }
            });
        } catch (Exception e) {
            Toast.makeText(FeedCommentsActivity.this, "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }

    //get more comments
    private void getFeedCommentsPagination() {
        try {
            RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            RequestBody feed_id = RequestBody.create(MultipartBody.FORM, String.valueOf(feedId));
            Call<ResponseBody> articles = apiInterface.GetFeedComments(feed_id, rows, page);
            //methods.showDialog(dialog, "Loading comments...", true);
            articles.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(dialog, "Dismiss", false);
                        if (response.isSuccessful()) {

                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                Toast.makeText(FeedCommentsActivity.this, "No more comments.", Toast.LENGTH_SHORT).show();
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                Toast.makeText(FeedCommentsActivity.this, "No more comments.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            List<comment> comments = new ArrayList<>();
                            comment comm;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                comm = new comment();
                                comm.setCommId(jsonObject.getString("commid"));
                                comm.setCommentorId(jsonObject.getString("commentorid"));
                                comm.setDateCommented(jsonObject.getString("date"));
                                comm.setComment(jsonObject.getString("comment"));
                                comm.setCommType(jsonObject.getString("commtype"));
                                if (!jsonObject.getString("commtype").equalsIgnoreCase("user")) {
                                    comm.setFirstName("n/a");
                                    comm.setSurname("n/a");
                                    comm.setProfilePic("n/a");
                                } else {
                                    comm.setFirstName(jsonObject.getString("fname"));
                                    comm.setSurname(jsonObject.getString("surname"));
                                    comm.setProfilePic(jsonObject.getString("profile"));
                                }
                                comments.add(comm);
                            }
                            comments_adapter.addComments(comments);
                        } else {
                            Toast.makeText(FeedCommentsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), FeedCommentsActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(dialog, "Dismiss", false);
                    methods.showAlert("Failure", "An error occurred.Please check your connection.", FeedCommentsActivity.this);
                }
            });
        } catch (Exception e) {
            Toast.makeText(FeedCommentsActivity.this, "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }
}
