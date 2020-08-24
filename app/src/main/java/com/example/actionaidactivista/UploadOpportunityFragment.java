package com.example.actionaidactivista;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.actionaidactivista.loginandsignup.AdminLoginActivity;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.Calendar;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadOpportunityFragment extends Fragment {

    final int FILE_SYSTEM = 101;
    private Uri mFilepathUri;
    private File file;

    private TextInputEditText mTitle;
    private TextInputEditText mDescription;
    private TextInputEditText mGoogleDocsLink;
    private Button mClosingDate;
    private TextInputEditText mLocation;
    private Button mPost;

    private Dialog mDialog;//progress dialog
    //retrofit
    private ApiInterface apiInterface;

    Calendar calendar;//calender
    int year, month, day;
    DatePickerDialog datePickerDialog;

    public UploadOpportunityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_upload_opportunity, container, false);
            //set title
            ((MainBottomNavActivity) getActivity()).setActionBarTitle("Upload Opportunity");
            //initialise widgets
            mTitle = (TextInputEditText) root.findViewById(R.id.title);
            mDescription = (TextInputEditText) root.findViewById(R.id.description);
            mLocation = (TextInputEditText) root.findViewById(R.id.location);
            mGoogleDocsLink = (TextInputEditText) root.findViewById(R.id.docs_link);
            mClosingDate = (Button) root.findViewById(R.id.closing_date);
            mPost = (Button) root.findViewById(R.id.post);
            mDialog = new Dialog(getContext());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mPost.setOnClickListener(v -> {
                try {
                    String title = mTitle.getText().toString().trim();
                    String des = mDescription.getText().toString().trim();
                    String closingDate = mClosingDate.getText().toString().trim();
                    String location = mLocation.getText().toString().trim();
                    String docs = mGoogleDocsLink.getText().toString().trim();

                    if (title.equalsIgnoreCase("") || des.equalsIgnoreCase("") || location.equalsIgnoreCase("")) {
                        methods.showAlert("Missing fields", "Enter all information.", getContext());
                        return;
                    }

                    if (closingDate.equalsIgnoreCase("Closing date")) {
                        methods.showAlert("Missing fields", "Enter closing date.", getContext());
                        return;
                    }
                    String link;
                    if (docs.equalsIgnoreCase("")) {
                        link = "N/A";
                    } else {
                        link = docs;
                    }

                    String dtePosted = methods.getDateForSqlServer();
                    if(!methods.checkOpportunityEndDate(dtePosted,closingDate)){
                        methods.showAlert("Invalid Date !!","The closing date you have selected seem to be before the current date.",getContext());
                        return;
                    }
                    postActivity(title, des, dtePosted, methods.changeDateFormat(closingDate), location,link);

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error raising event.", Toast.LENGTH_SHORT).show();
                }
            });
            calendar = Calendar.getInstance();
            //set get date button click event
            mClosingDate.setOnClickListener(v -> {
                if (v == mClosingDate) {
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    datePickerDialog = new DatePickerDialog(getContext(), (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
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
                        mClosingDate.setText(datenow);
                    }, year, month, day);
                    datePickerDialog.show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading UI", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    private void postActivity(String title, String des, String datePosted, String clsngDate, String location,String link) {
        try {
            RequestBody postTitle = RequestBody.create(MultipartBody.FORM, title);
            RequestBody description = RequestBody.create(MultipartBody.FORM, des);
            RequestBody dtepost = RequestBody.create(MultipartBody.FORM, datePosted);
            RequestBody loca = RequestBody.create(MultipartBody.FORM, location);
            RequestBody clsdte = RequestBody.create(MultipartBody.FORM, clsngDate);
            RequestBody doc_link = RequestBody.create(MultipartBody.FORM, link);

            Call<ResponseBody> login = apiInterface.PostOpportunity(postTitle, description, dtepost, clsdte, loca,doc_link);
            methods.showDialog(mDialog, "Posting opportunity...", true);
            login.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        //String[] tokens = methods.removeQoutes(result).split(":");
                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Upload failed.Try again.", getContext());
                        } else if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Upload successful.", getContext());

                            //clear edit texts
                            mTitle.setText("");
                            mDescription.setText("");
                            mLocation.setText("");
                            mClosingDate.setText("Closing date");
                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", getContext());
                        } else if (message.equalsIgnoreCase("Exist")) {
                            methods.showAlert("Response", "There is another opportunity with the same details.", getContext());
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showRequestFailedDialog(getContext());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
