package com.example.actionaidactivista.loginandsignup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class UserLoginAndSignUpActivity extends AppCompatActivity {

    private Button mSignUp;//sign up button
    private Button mLogin;//login button
    private TextInputEditText mAccount;//account no edit text
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
            setContentView(R.layout.activity_user_login_and_sign_up);
            //set title
            getSupportActionBar().setTitle("Login/SignUp");

            mSignUp = (Button) findViewById(R.id.user_sign_up);
            mLogin = (Button)findViewById(R.id.user_login);
            mAccount = (TextInputEditText) findViewById(R.id.user_username);
            mPassword = (TextInputEditText) findViewById(R.id.user_password);
            mDialog = new Dialog(this);
            sharedPreferences = getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mSignUp.setOnClickListener(v -> {
                try {
                    String user = mAccount.getText().toString().trim();
                    String pass = mPassword.getText().toString().trim();
                    if (user.equalsIgnoreCase("") || pass.equalsIgnoreCase("")) {
                        methods.showAlert("Missing info", "Enter all details", this);
                        return;
                    }
                    sign_up(user, pass);
                } catch (Exception e) {
                    Toast.makeText(UserLoginAndSignUpActivity.this, "Error raising event.", Toast.LENGTH_SHORT).show();
                }
            });
            mLogin.setOnClickListener(v -> {
                try {
                    String user = mAccount.getText().toString().trim();
                    String pass = mPassword.getText().toString().trim();
                    if (user.equalsIgnoreCase("") || pass.equalsIgnoreCase("")) {
                        methods.showAlert("Missing info", "Enter all details", this);
                        return;
                    }
                    login(user, pass);
                } catch (Exception e) {
                    Toast.makeText(UserLoginAndSignUpActivity.this, "Error raising event.", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(this,"Error loading UI",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent home = new Intent(UserLoginAndSignUpActivity.this, MainBottomNavActivity.class);
        startActivity(home);
    }

    //sign up method
    private void sign_up(String acc, String pass) {
        try {
            RequestBody accno = RequestBody.create(MultipartBody.FORM, acc.toUpperCase());
            RequestBody password = RequestBody.create(MultipartBody.FORM, pass);
            Call<ResponseBody> userSignUp = apiInterface.UserSignUp(accno, password);
            methods.showDialog(mDialog, "Signing up...", true);
            userSignUp.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        //String[] tokens = methods.removeQoutes(result).split(":");
                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "SignUp failed.Try later", UserLoginAndSignUpActivity.this);
                        } else if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "SignUp success.", UserLoginAndSignUpActivity.this);
                            //clear edit texts
                            mAccount.setText("");
                            mPassword.setText("");
                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", UserLoginAndSignUpActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(UserLoginAndSignUpActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog,"dismiss",false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), UserLoginAndSignUpActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(UserLoginAndSignUpActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(UserLoginAndSignUpActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    private void login(String user, String pass) {
        try {
            RequestBody username = RequestBody.create(MultipartBody.FORM, user.toUpperCase());
            RequestBody password = RequestBody.create(MultipartBody.FORM, pass);
            Call<ResponseBody> login = apiInterface.UserLogin(username, password);
            methods.showDialog(mDialog, "Signing in...", true);
            login.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String[] tokens = methods.removeQoutes(result).split(";");
                        String message = tokens[0];

                        if (message.equalsIgnoreCase("Invalid username or password")) {
                            methods.showAlert("Response", "Invalid username or password.", UserLoginAndSignUpActivity.this);
                        } else if (message.equalsIgnoreCase("Success")) {
                            //methods.showAlert("Response", "Sign in successful.", UserLoginAndSignUpActivity.this);
                            Toast.makeText(UserLoginAndSignUpActivity.this,"Login Successful",Toast.LENGTH_LONG).show();
                            String acc_type = tokens[1];
                            String user_id = tokens[2];
                            String pic = tokens[3];
                            //create account prefs
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RegistrationActivity.AccNo,user.toUpperCase());
                            editor.putInt(RegistrationActivity.UserId,Integer.parseInt(user_id));
                            editor.putString(RegistrationActivity.AccountType,acc_type);
                            editor.putString(RegistrationActivity.Level,"activista");
                            editor.putString(RegistrationActivity.ProfileUrl,pic);
                            editor.putBoolean(RegistrationActivity.IsLogged,true);
                            editor.apply();
                            //clear edit texts
                            mAccount.setText("");
                            mPassword.setText("");
                            Intent main = new Intent(UserLoginAndSignUpActivity.this, MainBottomNavActivity.class);
                            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            UserLoginAndSignUpActivity.this.startActivity(main);
                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", UserLoginAndSignUpActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(UserLoginAndSignUpActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog,"dismiss",false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), UserLoginAndSignUpActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(UserLoginAndSignUpActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(UserLoginAndSignUpActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
