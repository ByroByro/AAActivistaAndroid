package com.example.actionaidactivista;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actionaidactivista.database.prov_dis_helper;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private Button mDob;//date of birth button
    private Button mRegister;//register button
    Calendar calendar;//calender
    int year, month, day;
    private Dialog mDialog;//custom dialog
    DatePickerDialog datePickerDialog;
    private TextInputEditText mName;//first name
    private TextInputEditText mSurname;//surname
    private TextInputEditText mOcuupation;//person's occupation
    private Spinner mProvince;//province spinner
    private Spinner mDistrict;//province spinner
    private Spinner mGender;//province spinner
    private ProgressDialog mProgress;//progress dialog
    private com.example.actionaidactivista.database.prov_dis_helper prov_dis_helper;
    //retrofit
    private ApiInterface apiInterface;
    //SHARED PREFS CODE
    public static final String ACC_PREFERENCES = "AccountPreferences" ;
    public static final String AccNo = "accountno";
    public static final String UserId = "userid";
    public static final String AccountType = "acctype";//admin or user
    public static final String Level = "level";//activista or alumni
    public static final String ProfileUrl = "profileurl";
    public static final String IsApproved = "isapproved";
    public static final String IsLogged = "islogged";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //inflate layout
            setContentView(R.layout.activity_registration);

            //set app bar title
            getSupportActionBar().setTitle(R.string.registration_app_bar_title);

            //instantiate shared preferences
            //sharedpreferences = getSharedPreferences(ACC_PREFERENCES, Context.MODE_PRIVATE);
            //initialise widgets
            mDob = (Button) findViewById(R.id.dob);
            mRegister = (Button) findViewById(R.id.register);
            calendar = Calendar.getInstance();
            mName = (TextInputEditText) findViewById(R.id.first_name);
            mSurname = (TextInputEditText) findViewById(R.id.surname);
            mOcuupation = (TextInputEditText) findViewById(R.id.occupation);
            mProvince = (Spinner) findViewById(R.id.province);
            mDistrict = (Spinner) findViewById(R.id.district);
            mGender = (Spinner) findViewById(R.id.gender);
            mProgress = new ProgressDialog(this);
            mDialog = new Dialog(this);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            prov_dis_helper = new prov_dis_helper(this, "", null);

            //check if there are zero provinces
            if(prov_dis_helper.getProvinces().size() == 0){
                //get provinces from server
                getProvinces();
            }
            //check if there are zero provinces
            if(prov_dis_helper.getDistricts().size() == 0){
                //get districts from server
                getDistricts();
            }

            //populate provinces spinner
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, prov_dis_helper.getProvinces());
            itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mProvince.setAdapter(itemsAdapter);
            //set provinces spinner on item selected listener
            mProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    String myProvince = mProvince.getSelectedItem().toString();
                    ArrayAdapter<String> itemsAdapter1 = new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, prov_dis_helper.getDistricts(prov_dis_helper.provID(myProvince)));
                    itemsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mDistrict.setAdapter(itemsAdapter1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    //showMessage("Note","Please Select Provinces First");
                }

            });
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
            //set register onClick listener
            mRegister.setOnClickListener(v -> {
                try {
                    String dob = mDob.getText().toString().trim();//dob
                    String fname = mName.getText().toString().trim();//first name
                    String surname = mSurname.getText().toString().trim();//surname
                    String province = mProvince.getSelectedItem().toString().trim();//province
                    String district = mDistrict.getSelectedItem().toString().trim();//district
                    String gender = mGender.getSelectedItem().toString().trim();//gender
                    String occupation = mOcuupation.getText().toString().trim();//occupation

                    if (dob.equalsIgnoreCase("Pick date of birth")) {
                        Toast.makeText(RegistrationActivity.this, "Pick date of birth please.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //check age
                    if (methods.checkAge(dob, this)) {
                        //over age
                        methods.showAlert("Over Age", "You are not allowed to register because activistas must be less than 36 years.", this);
                        return;
                    }
                    if (fname.equalsIgnoreCase("") || surname.equalsIgnoreCase("") || occupation.equalsIgnoreCase("")) {
                        methods.showAlert("Missing Info", "Enter all info.", this);
                        return;
                    }

                    RequestBody name = RequestBody.create(MultipartBody.FORM, fname);
                    RequestBody sur = RequestBody.create(MultipartBody.FORM, surname);
                    RequestBody db = RequestBody.create(MultipartBody.FORM, methods.changeDateFormat(dob));
                    RequestBody prov = RequestBody.create(MultipartBody.FORM, prov_dis_helper.provID(province));
                    RequestBody dis = RequestBody.create(MultipartBody.FORM, prov_dis_helper.disID(district));
                    RequestBody sex = RequestBody.create(MultipartBody.FORM, gender);
                    RequestBody occu = RequestBody.create(MultipartBody.FORM, occupation);

                    Call<ResponseBody> registerUser = apiInterface.RegisterUser(name, sur, db, prov, dis, sex, occu);
                    mProgress.setMessage("Registering...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.setOnKeyListener((dialog, keyCode, event) -> false);
                    mProgress.show();
                    registerUser.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                mProgress.dismiss();
                                String result = response.body().string();
                                String message = methods.removeQoutes(result);
                                //String [] tokens = methods.removeQoutes(result).split(":");
                                //String message = tokens[0];
                                //String account_no = tokens[1];
                                if (message.equalsIgnoreCase("Account Already Exist")) {
                                    methods.showAlert("Response", "Looks like you are already registered.", RegistrationActivity.this);
                                } else if (message.equalsIgnoreCase("Success")) {
                                    methods.showAlert("Response", "Successfully registered.You are now a registered Activista.", RegistrationActivity.this);
                                    //create account prefs
                                    //SharedPreferences.Editor editor = sharedpreferences.edit();
                                    //editor.putString(AccNo,account_no);
                                    //editor.putInt(UserId,0);
                                    //editor.putString(AccountType,"none");
                                    //editor.putString(Level,"none");
                                    //editor.putString(ProfileUrl,"none");
                                    //editor.putBoolean(IsApproved,false);
                                    //editor.putBoolean(IsLogged,false);
                                    //editor.apply();
                                } else if (message.equalsIgnoreCase("Failed")) {
                                    methods.showAlert("Response", "Registration failed.Please try again.", RegistrationActivity.this);
                                } else if (message.equalsIgnoreCase("Error")) {
                                    methods.showAlert("Response", "Server error.", RegistrationActivity.this);
                                }
                                //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(RegistrationActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            try {
                                mProgress.dismiss();
                                methods.showAlert("Request failed", "Request failed " + t.toString(), RegistrationActivity.this);
                            } catch (Exception e) {
                                Toast.makeText(RegistrationActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(RegistrationActivity.this, "Error raising registration event", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    //get provinces method
    private void getProvinces() {
        try {
            Call<ResponseBody> provs = apiInterface.getProvinces();
            methods.showDialog(mDialog, "Loading provinces...", true);
            provs.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {

                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            JSONArray array = new JSONArray(result);
                            ArrayList<String> a;//province ids
                            ArrayList<String> b;//province names
                            ArrayList<String> c;//country ids
                            a = new ArrayList<>();
                            b = new ArrayList<>();
                            c = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                a.add(jsonObject.getString("pid"));
                                b.add(jsonObject.getString("name"));
                                c.add(jsonObject.getString("cid"));
                            }
                            prov_dis_helper.insert_provinces(a, b, c);
                            //if inserting is successful then populate spinner
                            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, prov_dis_helper.getProvinces());
                            itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mProvince.setAdapter(itemsAdapter);
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), RegistrationActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("List onFailure", t.toString(), RegistrationActivity.this);
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //get districts method
    private void getDistricts() {
        try {
            Call<ResponseBody> dis = apiInterface.getDistricts();
            methods.showDialog(mDialog, "Loading districts...", true);
            dis.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JsonParser parser = new JsonParser();
                            String result = parser.parse(responseData).getAsString();
                            JSONArray array = new JSONArray(result);
                            ArrayList<String> a;//district ids
                            ArrayList<String> b;//district names
                            ArrayList<String> c;//province ids
                            a = new ArrayList<>();
                            b = new ArrayList<>();
                            c = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                a.add(jsonObject.getString("did"));
                                b.add(jsonObject.getString("name"));
                                c.add(jsonObject.getString("pid"));
                            }
                            prov_dis_helper.insert_districts(a, b, c);
                            String myProvince = mProvince.getSelectedItem().toString();
                            ArrayAdapter<String> itemsAdapter1 = new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, prov_dis_helper.getDistricts(prov_dis_helper.provID(myProvince)));
                            itemsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mDistrict.setAdapter(itemsAdapter1);
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), RegistrationActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("List onFailure", t.toString(), RegistrationActivity.this);
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
