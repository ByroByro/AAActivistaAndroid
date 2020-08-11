package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.actionaidactivista.adapters.ReportedCommentsAdapter;
import com.example.actionaidactivista.adapters.comments_adapter;
import com.example.actionaidactivista.models.ReportedComment;
import com.example.actionaidactivista.models.comment;
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

public class ReportedCommentsActivity extends AppCompatActivity {

    private RecyclerView comments;
    private List<ReportedComment> commentsList;
    private ReportedCommentsAdapter mAdapter;
    private ApiInterface apiInterface;
    private Dialog dialog;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set UI
            setContentView(R.layout.activity_reported_comments);
            try{
                getSupportActionBar().setTitle("Reported Comments");
            }catch (Exception e){
                System.out.println(e);
            }
            comments = (RecyclerView)findViewById(R.id.reported_comments_list_recycler);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            comments.setLayoutManager(linearLayoutManager);
            comments.setItemAnimator(new DefaultItemAnimator());
            linearLayout = findViewById(R.id.root);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            dialog = new Dialog(this);

            getComments();
        }catch (Exception e){
            Toast.makeText(this, "Error loading UI.", Toast.LENGTH_SHORT).show();
        }
    }

    //get comments
    private void getComments() {
        try {
            //RequestBody page = RequestBody.create(MultipartBody.FORM, String.valueOf(page_num));
            //RequestBody rows = RequestBody.create(MultipartBody.FORM, String.valueOf(row_num));
            //RequestBody feed_id = RequestBody.create(MultipartBody.FORM, String.valueOf(feedId));
            Call<ResponseBody> comms = apiInterface.getReportedComments();
            methods.showDialog(dialog, "Loading comments...", true);
            comms.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(dialog, "Dismiss", false);
                        if (response.isSuccessful()) {

                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            if (result.length() == 0) {
                                //Toast.makeText(FeedCommentsActivity.this, "No more comments.", Toast.LENGTH_SHORT).show();
                                Snackbar.make(linearLayout, "There are no comments", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            JSONArray array = new JSONArray(result);
                            if (array.length() == 0) {
                                Snackbar.make(linearLayout, "There are no comments", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            commentsList = new ArrayList<>();

                            ReportedComment comm;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                comm = new ReportedComment();
                                comm.setId(jsonObject.getString("id"));
                                comm.setReporterid(jsonObject.getString("reporterid"));
                                comm.setOffenderid(jsonObject.getString("offenderid"));
                                comm.setCommentid(jsonObject.getString("commentid"));
                                comm.setRpttype(jsonObject.getString("rpttype"));
                                comm.setReason(jsonObject.getString("reason"));
                                comm.setComment(jsonObject.getString("comment"));
                                commentsList.add(comm);
                            }
                            mAdapter = new ReportedCommentsAdapter(commentsList, ReportedCommentsActivity.this);
                            comments.setAdapter(mAdapter);

                        } else {
                            Toast.makeText(ReportedCommentsActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), ReportedCommentsActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(dialog, "Dismiss", false);
                    methods.showAlert("Failure", "An error occurred.Please check your connection.", ReportedCommentsActivity.this);
                }
            });
        } catch (Exception e) {
            Toast.makeText(ReportedCommentsActivity.this, "Error raising get event", Toast.LENGTH_SHORT).show();
        }
    }
}
