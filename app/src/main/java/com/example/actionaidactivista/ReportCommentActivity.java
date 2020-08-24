package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.actionaidactivista.models.ReportedComment;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportCommentActivity extends AppCompatActivity {

    Intent intent;
    String reported_id;//person whose comment is being reported
    String comment_id;
    String comment;
    //retrofit
    private ApiInterface apiInterface;
    private Dialog mDialog;
    TextInputEditText reason;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set UI
            setContentView(R.layout.activity_report_comment);
            try {
                getSupportActionBar().setTitle("Report Comment");
            } catch (Exception e) {
                System.out.println(e);
            }
            intent = getIntent();
            reported_id = intent.getStringExtra("reported_id");
            comment_id = intent.getStringExtra("comment_id");
            comment = intent.getStringExtra("comment");
            //initialise api interface
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            reason = (TextInputEditText) findViewById(R.id.reason);
            submit = (Button) findViewById(R.id.submit);

            submit.setOnClickListener(v -> {
                try {

                    String resn = reason.getText().toString().trim();
                    if(resn.equalsIgnoreCase("")){
                        Toast.makeText(this, "Give a reason", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String likertype;
                    String uid;
                    if (methods.isAdmin(ReportCommentActivity.this)) {
                        likertype = "admin";
                    } else {
                        likertype = "user";
                    }

                    RequestBody rid = RequestBody.create(MultipartBody.FORM, String.valueOf(methods.getUserId(ReportCommentActivity.this)));
                    RequestBody rpdid = RequestBody.create(MultipartBody.FORM, reported_id);
                    RequestBody comm_id = RequestBody.create(MultipartBody.FORM, comment_id);
                    RequestBody rep_type = RequestBody.create(MultipartBody.FORM, likertype);
                    RequestBody rezn = RequestBody.create(MultipartBody.FORM, resn);
                    RequestBody comm = RequestBody.create(MultipartBody.FORM, comment);

                    Call<ResponseBody> report = apiInterface.ReportComment(rid,rpdid,comm_id,rep_type,rezn,comm);
                    methods.showDialog(mDialog, "Reporting...", true);
                    report.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                methods.showDialog(mDialog, "dismiss", false);
                                String result = response.body().string();
                                String message = methods.removeQoutes(result);

                                if (message.equalsIgnoreCase("Failed")) {
                                    methods.showAlert("Response", "Failed.Try again.", ReportCommentActivity.this);
                                } else if (message.equalsIgnoreCase("Success")) {
                                    methods.showAlert("Response", "Report submitted", ReportCommentActivity.this);
                                } else if (message.equalsIgnoreCase("Error")) {
                                    methods.showAlert("Response", "Server error.", ReportCommentActivity.this);
                                } else if (message.equalsIgnoreCase("Exist")) {
                                    methods.showAlert("Response", "You have reported this comment before.", ReportCommentActivity.this);
                                }
                                //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(ReportCommentActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            try {
                                methods.showDialog(mDialog, "dismiss", false);
                                methods.showRequestFailedDialog(ReportCommentActivity.this);
                            } catch (Exception e) {
                                Toast.makeText(ReportCommentActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(this, "Error raising submit", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }
}
