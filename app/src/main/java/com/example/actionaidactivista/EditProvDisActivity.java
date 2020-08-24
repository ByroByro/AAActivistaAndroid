package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.actionaidactivista.database.prov_dis_helper;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProvDisActivity extends AppCompatActivity {

    private Spinner mProvince;//province spinner
    private Spinner mDistrict;//province spinner
    private Dialog mDialog;
    private prov_dis_helper prov_dis_helper;//database helper
    private ApiInterface apiInterface;
    private Button mUpdate;//update button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set content view
            setContentView(R.layout.activity_edit_prov_dis);
            //set title
            try {
                getSupportActionBar().setTitle("Update Location Details");
            } catch (Exception e) {
                Toast.makeText(this, "Error setting title", Toast.LENGTH_SHORT).show();
            }
            //initialise widgets
            mProvince = (Spinner) findViewById(R.id.province);
            mDistrict = (Spinner) findViewById(R.id.district);
            mDialog = new Dialog(this);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            prov_dis_helper = new prov_dis_helper(this, "", null);
            mUpdate = (Button) findViewById(R.id.update);

            mUpdate.setOnClickListener(v -> {
                try {
                    String province = mProvince.getSelectedItem().toString().trim();//province
                    String district = mDistrict.getSelectedItem().toString().trim();//district
                    update(prov_dis_helper.provID(province),prov_dis_helper.disID(district));
                } catch (Exception e) {
                    Toast.makeText(EditProvDisActivity.this, "Error raising update event.", Toast.LENGTH_SHORT).show();
                }
            });
            //populate provinces spinner
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, prov_dis_helper.getProvinces());
            itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mProvince.setAdapter(itemsAdapter);
            //set provinces spinner on item selected listener
            mProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    String myProvince = mProvince.getSelectedItem().toString();
                    ArrayAdapter<String> itemsAdapter1 = new ArrayAdapter<String>(EditProvDisActivity.this, android.R.layout.simple_spinner_item, prov_dis_helper.getDistricts(prov_dis_helper.provID(myProvince)));
                    itemsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mDistrict.setAdapter(itemsAdapter1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    //showMessage("Note","Please Select Provinces First");
                }

            });
        } catch (Exception e) {
            Toast.makeText(this, "error loading ui.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * update location details method
     */
    private void update(String provice_id, String district_id) {
        try {
            RequestBody prov = RequestBody.create(MultipartBody.FORM, provice_id);
            RequestBody dis = RequestBody.create(MultipartBody.FORM, district_id);
            RequestBody id = RequestBody.create(MultipartBody.FORM, String.valueOf(methods.getUserId(EditProvDisActivity.this)));
            RequestBody acc = RequestBody.create(MultipartBody.FORM, methods.getUserAccountNo(EditProvDisActivity.this));
            Call<ResponseBody> update = apiInterface.UpdateProvDis(prov, dis, id, acc);
            methods.showDialog(mDialog, "Updating...", true);
            update.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Updating...", false);
                        String result = response.body().string();
                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Successfully updated.", EditProvDisActivity.this);
                        } else if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Update failed.Please try again.", EditProvDisActivity.this);
                        } else if (message.equalsIgnoreCase("Server Error")) {
                            methods.showAlert("Response", "Server error.", EditProvDisActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(EditProvDisActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "Updating...", false);
                        methods.showRequestFailedDialog(EditProvDisActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(EditProvDisActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error raising update.", Toast.LENGTH_SHORT).show();
        }
    }
}
