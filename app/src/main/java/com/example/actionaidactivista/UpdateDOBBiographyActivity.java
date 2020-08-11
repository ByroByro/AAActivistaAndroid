package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateDOBBiographyActivity extends AppCompatActivity {

    Intent intent;
    private Button mDob;//date of birth button
    private Button mUpdate;//register button
    Calendar calendar;//calendar
    int year, month, day;
    private Dialog mDialog;//custom dialog
    DatePickerDialog datePickerDialog;
    private TextInputEditText biography;//first name
    private CardView updateDobCard;
    private CardView updateBioCard;
    private String mWhich;
    //retrofit
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set layout
            setContentView(R.layout.activity_update_dobbiography);
            intent = getIntent();
            biography = (TextInputEditText) findViewById(R.id.update_bio);
            mDob = (Button) findViewById(R.id.dob);
            mDialog = new Dialog(this);
            calendar = Calendar.getInstance();
            mUpdate = (Button) findViewById(R.id.update);
            updateBioCard = (CardView) findViewById(R.id.biography_card);
            updateDobCard = (CardView) findViewById(R.id.dob_card);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            //get data
            String which = intent.getStringExtra("which_value");
            String bio;
            String title;
            if (which.equalsIgnoreCase("bio")) {
                bio = intent.getStringExtra("value");
                biography.setText(bio);
                title = "Update Biography";
                mWhich = "bio";
                updateBioCard.setVisibility(View.VISIBLE);
                updateDobCard.setVisibility(View.GONE);

            } else {
                title = "Update Date of birth";
                mWhich = "dob";
                updateBioCard.setVisibility(View.GONE);
                updateDobCard.setVisibility(View.VISIBLE);
            }
            //set title
            try {
                getSupportActionBar().setTitle(title);
            } catch (Exception e) {
                System.out.println(e);
            }
            //set get date button click event
            mDob.setOnClickListener(v -> {
                if (v == mDob) {
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    datePickerDialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
                        String datenow = null;
                        if (dayOfMonth > 9 && (month + 1) < 10) {
                            datenow = dayOfMonth + "-0" + (month + 1) + "-" + year;
                        } else if (dayOfMonth < 10 && (month + 1) > 9) {

                            datenow = "0" + dayOfMonth + "-" + (month + 1) + "-" + year;
                        } else if (dayOfMonth < 10 && (month + 1) < 10) {

                            datenow = "0" + dayOfMonth + "-0" + (month + 1) + "-" + year;
                        } else {

                            datenow = dayOfMonth + "-" + (month + 1) + "-" + year;
                        }
                        mDob.setText(datenow);
                    }, year, month, day);
                    datePickerDialog.show();
                }
            });
            mUpdate.setOnClickListener(v -> {
                try {
                    String value;

                    if (mWhich.equalsIgnoreCase("dob")) {
                        String dob = mDob.getText().toString().trim();//dob
                        if (dob.equalsIgnoreCase("Pick date of birth")) {
                            Toast.makeText(UpdateDOBBiographyActivity.this, "Pick date of birth please.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        value = methods.changeDateFormat(dob);
                    } else {
                        String biog = biography.getText().toString().trim();//bio
                        if (biog.equalsIgnoreCase("")) {
                            value = "N/A";
                        } else {
                            value = biog;
                        }
                    }
                    update(mWhich,value);
                } catch (Exception e) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void update(String which_value, String value) {
        try {
            RequestBody which = RequestBody.create(MultipartBody.FORM, which_value);
            RequestBody val = RequestBody.create(MultipartBody.FORM, value);
            RequestBody id = RequestBody.create(MultipartBody.FORM, String.valueOf(methods.getUserId(UpdateDOBBiographyActivity.this)));
            RequestBody acc = RequestBody.create(MultipartBody.FORM, methods.getUserAccountNo(UpdateDOBBiographyActivity.this));
            Call<ResponseBody> update = apiInterface.UpdateDOBBioInfo(which,val,id,acc);
            methods.showDialog(mDialog, "Updating...", true);
            update.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Updating...", false);
                        String result = response.body().string();
                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Successfully updated.", UpdateDOBBiographyActivity.this);
                        } else if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Update failed.Please try again.", UpdateDOBBiographyActivity.this);
                        } else if (message.equalsIgnoreCase("Server Error")) {
                            methods.showAlert("Response", "Server error.", UpdateDOBBiographyActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(UpdateDOBBiographyActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "Updating...", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), UpdateDOBBiographyActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(UpdateDOBBiographyActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error raising update.", Toast.LENGTH_SHORT).show();
        }
    }
}
