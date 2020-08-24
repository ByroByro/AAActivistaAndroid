package com.example.actionaidactivista.loginandsignup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.actionaidactivista.R;
import com.example.actionaidactivista.RegistrationActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private Button mLogin;//login button
    private TextInputEditText mUsername;//username edit text
    private TextInputEditText mPassword;//password edit text
    private Dialog mDialog;//progress dialog
    //retrofit
    private ApiInterface apiInterface;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //inflate UI
            setContentView(R.layout.activity_admin_login);
            //set title
            getSupportActionBar().setTitle("Login");
            //initialise widgets
            mLogin = (Button) findViewById(R.id.admin_login);
            mUsername = (TextInputEditText) findViewById(R.id.admin_username);
            mPassword = (TextInputEditText) findViewById(R.id.admin_password);
            mDialog = new Dialog(this);
            sharedPreferences = getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mLogin.setOnClickListener(v -> {
                try {
                    String user = mUsername.getText().toString().trim();
                    String pass = mPassword.getText().toString().trim();
                    if (user.equalsIgnoreCase("") || pass.equalsIgnoreCase("")) {
                        methods.showAlert("Missing info", "Enter all details", this);
                        return;
                    }
                    login(user, pass);
                } catch (Exception e) {
                    Toast.makeText(AdminLoginActivity.this, "Error raising event.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent home = new Intent(AdminLoginActivity.this, MainBottomNavActivity.class);
        startActivity(home);
    }

    private void login(String user, String pass) {
        try {
            RequestBody username = RequestBody.create(MultipartBody.FORM, user);
            RequestBody password = RequestBody.create(MultipartBody.FORM, pass);
            Call<ResponseBody> login = apiInterface.AdminLogin(username, password);
            methods.showDialog(mDialog, "Signing in...", true);
            login.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String[] tokens = methods.removeQoutes(result).split(":");
                        String message = tokens[0];

                        if (message.equalsIgnoreCase("Invalid username or password")) {
                            methods.showAlert("Response", "Invalid username or password.", AdminLoginActivity.this);
                        } else if (message.equalsIgnoreCase("Success")) {
                            //methods.showAlert("Response", "Sign in successful.", AdminLoginActivity.this);
                            Toast.makeText(AdminLoginActivity.this,"Login Successful",Toast.LENGTH_LONG).show();
                            String acc_type = tokens[1];
                            String user_id = tokens[2];
                            //create account prefs
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RegistrationActivity.AccNo,"none");
                            editor.putInt(RegistrationActivity.UserId,Integer.parseInt(user_id));
                            editor.putString(RegistrationActivity.AccountType,acc_type);
                            editor.putString(RegistrationActivity.Level,"none");
                            editor.putString(RegistrationActivity.ProfileUrl,"none");
                            editor.putBoolean(RegistrationActivity.IsLogged,true);
                            editor.apply();
                            //clear edit texts
                            mUsername.setText("");
                            mPassword.setText("");
                            //go to main dash
                            Intent main = new Intent(AdminLoginActivity.this, MainBottomNavActivity.class);
                            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            AdminLoginActivity.this.startActivity(main);
                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", AdminLoginActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(AdminLoginActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog,"dismiss",false);
                        methods.showAlert("Request failed", "Request failed..Check your network connection.", AdminLoginActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(AdminLoginActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(AdminLoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
