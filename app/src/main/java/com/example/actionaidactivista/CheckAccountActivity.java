package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.actionaidactivista.RegistrationActivity.AccNo;

public class CheckAccountActivity extends AppCompatActivity {

    private Button chek;
    private TextInputEditText email;
    SharedPreferences sharedPreferences;
    //retrofit
    private ApiInterface apiInterface;
    private Spinner whoseAcc;
    private Dialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set ui
            setContentView(R.layout.activity_check_account);
            //set title
            try {
                getSupportActionBar().setTitle("Account");
            } catch (Exception e) {
                System.out.println(e);
            }
            //init widgets
            chek = (Button) findViewById(R.id.check);
            email = (TextInputEditText) findViewById(R.id.email);
            whoseAcc = (Spinner) findViewById(R.id.whose_acc);
            sharedPreferences = getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            mProgress = new Dialog(this);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            chek.setOnClickListener(v -> {
                try {
                    String mail = email.getText().toString().trim();
                    if (mail.equalsIgnoreCase("")) {
                        Toast.makeText(CheckAccountActivity.this, "Enter email address", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //check method
                    Check(mail);

                } catch (Exception e) {
                    Toast.makeText(CheckAccountActivity.this, "error raising check event", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    //method for checking
    private void Check(String number) {
        try {

            RequestBody num = RequestBody.create(MultipartBody.FORM, number);

            Call<ResponseBody> check = apiInterface.CheckAcc(num);
            methods.showDialog(mProgress, "Checking...", true);
            check.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mProgress, "Checking...", false);
                        String result = response.body().string();
                        String res = methods.removeQoutes(result);

                        if (res.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.Try later.", CheckAccountActivity.this);
                        } else if (res.equalsIgnoreCase("Yes")) {
                            methods.showAlert("Response", "Your account has been approved.We have send you an email with your account details.", CheckAccountActivity.this);
                        } else if (res.equalsIgnoreCase("No")) {
                            methods.showAlert("Response", "Your account has not yet been approved.", CheckAccountActivity.this);
                        } else if (res.equalsIgnoreCase("Email not found")) {
                            methods.showAlert("Response", "We could not find the address you supplied.Enter correctly and try again.", CheckAccountActivity.this);
                        } else {
                            methods.showAlert("Response", "Server error.", CheckAccountActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(CheckAccountActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mProgress, "...", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), CheckAccountActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(CheckAccountActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            methods.showDialog(mProgress, "...", false);
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
