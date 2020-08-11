package com.example.actionaidactivista;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.adapters.contact_adapter;
import com.example.actionaidactivista.database.prov_dis_helper;
import com.example.actionaidactivista.models.UserEdit;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    final int REQUEST_CODE_GALLERY = 10;
    final int REQUEST_CODE_CAMERA = 11;
    Intent chooseFile;
    private CircleImageView mProfile;
    private File realProfilePath;
    RequestOptions requestOptions;
    private Uri filepath;
    SharedPreferences sharedPreferences;
    //Dialog mDialog;
    ApiInterface apiInterface;
    private boolean isGallery;
    private boolean isCamera;
    private Dialog dialog;

    private TextView editBiography;
    private Button mDob;//date of birth button
    private Button mUpdate;//register button
    private Button remove_pic;
    private Button pic_gallery;
    private Button camera;
    private CardView pic_options;
    Calendar calendar;//calender
    private Dialog mDialog;//custom dialog
    private TextInputEditText mName;//first name
    private TextInputEditText mSurname;//surname
    private TextInputEditText mOccupation;//person's occupation
    private TextInputEditText phoneNumber;//user phone no
    private TextView mProvince;//province
    private TextView mDistrict;//district
    private TextView mEditLocation;//district
    private Spinner mGender;//province spinner
    private ProgressDialog mProgress;//progress dialog
    private CheckBox mDobStatus;//determining whether dob will appear as age or as is
    private TextInputEditText mEmail;//email field
    private TextView mCurrDob;//current dob from server
    private String mBiography;//hold biography
    private TextView mEditPic;//edi profile pic text view
    private String mProfileServerPath;//hold profile pic path
    private String mProfileServerUrl;//hold profile url
    private prov_dis_helper prov_dis_helper;//province and district sql_ite helper

    private String mCurrentPathFromCameraFile = null;
    private File cameraFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set content
            setContentView(R.layout.activity_profile);
            //set action bar title
            try {
                getSupportActionBar().setTitle(R.string.profile_app_bar_title);
            } catch (Exception e) {
                System.out.println(e);
            }

            mProfile = (CircleImageView) findViewById(R.id.imageViewProfile);
            mDialog = new Dialog(this);
            dialog = new Dialog(this);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            sharedPreferences = getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);

            editBiography = (TextView) findViewById(R.id.edit_biography);
            //initialise widgets
            mDob = (Button) findViewById(R.id.dob);
            mUpdate = (Button) findViewById(R.id.update);
            calendar = Calendar.getInstance();
            mName = (TextInputEditText) findViewById(R.id.first_name);
            mSurname = (TextInputEditText) findViewById(R.id.surname);
            mOccupation = (TextInputEditText) findViewById(R.id.occupation);
            phoneNumber = (TextInputEditText) findViewById(R.id.phone);
            mProvince = (TextView) findViewById(R.id.province);
            mDistrict = (TextView) findViewById(R.id.district);
            mEditLocation = (TextView) findViewById(R.id.edit_location);
            mGender = (Spinner) findViewById(R.id.gender);
            mDobStatus = (CheckBox) findViewById(R.id.dob_Status);
            mEmail = (TextInputEditText) findViewById(R.id.email);
            mCurrDob = (TextView) findViewById(R.id.curr_dob);
            mEditPic = (TextView) findViewById(R.id.edit_pic);//edit pic text view
            pic_options = (CardView) findViewById(R.id.edit_pic_card);//pic options card
            camera = (Button) findViewById(R.id.camera);//camera
            pic_gallery = (Button) findViewById(R.id.gallery);//gallery
            remove_pic = (Button) findViewById(R.id.remove_pic);//remove
            mProgress = new ProgressDialog(this);
            mDialog = new Dialog(this);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            prov_dis_helper = new prov_dis_helper(this, "", null);

            //underline the edit biography label
            editBiography.setPaintFlags(editBiography.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            editBiography.setOnClickListener(v -> {
                //go to edit biography intent
                Intent intent = new Intent(this, UpdateDOBBiographyActivity.class);
                intent.putExtra("which_value", "bio");
                intent.putExtra("value", mBiography);
                startActivity(intent);
            });

            //underline edit location
            mEditLocation.setPaintFlags(mEditLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            //edit location
            mEditLocation.setOnClickListener(v -> {
                try{
                    //go to edit location intent
                    Intent intent = new Intent(this, EditProvDisActivity.class);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });

            //change dob
            mDob.setOnClickListener(v -> {
                //go to change dob
                Intent intent = new Intent(this, UpdateDOBBiographyActivity.class);
                intent.putExtra("which_value", "dob");
                startActivity(intent);
            });
            //edit pic text view
            mEditPic.setOnClickListener(v -> {
                try {
                    //set pic options to visible
                    pic_options.setVisibility(View.VISIBLE);
                    //if profile or path is n/a then there is nothing to remove
                    if (mProfileServerPath.equalsIgnoreCase("N/A") || mProfileServerUrl.equalsIgnoreCase("N/A")) {
                        remove_pic.setVisibility(View.GONE);
                    } else {
                        remove_pic.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
            //gallery click
            pic_gallery.setOnClickListener(v -> {
                try {
                    if (methods.isAdmin(this)) {
                        Toast.makeText(this, "This is for users.", Toast.LENGTH_LONG).show();
                    } else {
                        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED) {
                            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                            chooseFile.setType("image/*");
                            startActivityForResult(chooseFile, REQUEST_CODE_GALLERY);
                        } else {
                            askForPermission();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error trying to pick image.", Toast.LENGTH_SHORT).show();
                }
            });
            //camera click
            camera.setOnClickListener(v -> {
                try {
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (camera.resolveActivity(getPackageManager()) != null) {
                        File imageFile = getImageFile();
                        cameraFile = imageFile;
                        if (imageFile != null) {
                            Uri imageUri = FileProvider.getUriForFile(this, "com.example.actionaidactivista.provider", imageFile);
                            camera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(camera, REQUEST_CODE_CAMERA);
                        }
                    } else {
                        Toast.makeText(this, "No camera app(s) found.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    Toast.makeText(ProfileActivity.this, "Error trying to raise camera", Toast.LENGTH_LONG).show();
                }
            });

            //remove click
            remove_pic.setOnClickListener(v -> {
                try {
                    removeProfile(mProfileServerUrl, mProfileServerPath);
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error trying to raise remove", Toast.LENGTH_LONG).show();
                }
            });
            //update on click listener
            mUpdate.setOnClickListener(v -> {
                try {
                    updateDetails();
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error raising update event.", Toast.LENGTH_SHORT).show();
                }
            });
            //check if there are zero provinces
            if (prov_dis_helper.getProvinces().size() == 0) {
                //get provinces from server and save them locally
                getProvinces();
            }
            //check if there are zero provinces
            if (prov_dis_helper.getDistricts().size() == 0) {
                //get districts from server and save them locally
                getDistricts();
            }
            //get current details
            getMyDetails();
        } catch (Exception e) {
            Toast.makeText(this, "error loading ui", Toast.LENGTH_SHORT).show();
        }
    }

    private void askForPermission() {
        //get user permission to read/write file system
        ActivityCompat.requestPermissions(ProfileActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                filepath = data.getData();

                //upload the image as soon as the image is picked
                convertUriToFile(filepath);
                isGallery = true;
                isCamera = false;

            } catch (Exception e) {
                Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_CODE_CAMERA) {
            //upload if file path is not null
            if(cameraFile == null){
                return;
            }
            //upload the image as soon as the image is picked
            //convertUriToFile(filepath);
            isGallery = false;
            isCamera = true;

        }

        try{
            dialog.setContentView(R.layout.change_profile_dialog);
            ImageView imageView = dialog.findViewById(R.id.fullImage);
            Button cancel = dialog.findViewById(R.id.cancel);
            Button ok = dialog.findViewById(R.id.ok);
            //dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }
                return true;
            });

            requestOptions = new RequestOptions();
            requestOptions.centerCrop();
            requestOptions.placeholder(R.drawable.ic_account_circle);
            requestOptions.error(R.drawable.ic_account_circle);
            if(isCamera) {
                Glide.with(ProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(cameraFile)
                        .into(imageView);
            }

            if(isGallery){
                Glide.with(ProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(realProfilePath)
                        .into(imageView);
            }

            ok.setOnClickListener(v -> {
                if(isGallery){
                    dialog.dismiss();
                    if (methods.getUserId(ProfileActivity.this) != 0) {
                        uploadImage(String.valueOf(methods.getUserId(ProfileActivity.this)), realProfilePath, filepath);
                    } else {
                        Toast.makeText(this, "Sign Up required.", Toast.LENGTH_SHORT).show();
                    }
                }
                if(isCamera){
                    dialog.dismiss();
                    if (methods.getUserId(ProfileActivity.this) != 0) {
                        uploadImage(String.valueOf(methods.getUserId(ProfileActivity.this)), cameraFile, FileProvider.getUriForFile(this, "com.example.actionaidactivista.provider", cameraFile));
                    } else {
                        Toast.makeText(this, "Sign Up required.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            cancel.setOnClickListener(v -> {
                isCamera = false;
                isGallery = false;
                dialog.dismiss();
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }catch (Exception e){
            System.out.println(e);
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage(String userid, File profile, Uri uri) {
        try {
            RequestBody uid = RequestBody.create(MultipartBody.FORM, userid);

            if (profile == null) {
                methods.showAlert("File needed.", "Select a photo first.", this);
                return;
            }
            if (uri == null) {
                methods.showAlert("File needed.", "Select a photo first.", this);
                return;
            }
            RequestBody the_file = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(uri)),
                    profile
            );

            MultipartBody.Part actual = MultipartBody.Part.createFormData("image", profile.getName(), the_file);

            Call<ResponseBody> upload = apiInterface.UpdateProfile(uid, actual);
            methods.showDialog(mDialog, "Updating profile pic...", true);
            upload.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);
                        String[] tokens = message.split(";");
                        String res = tokens[0];

                        if (res.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Upload failed.Try again.", ProfileActivity.this);
                        } else if (res.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Upload successful.", ProfileActivity.this);
                            String url = tokens[1];
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RegistrationActivity.ProfileUrl, url);
                            editor.apply();
                            Glide.with(ProfileActivity.this)
                                    .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                                    .load(url)
                                    .into(mProfile);
                            pic_options.setVisibility(View.GONE);
                            //try to delete camera file
                            try {
                                if (cameraFile != null && !cameraFile.isDirectory()) {
                                    cameraFile.delete();
                                }
                            } catch (Exception e) {
                                Toast.makeText(ProfileActivity.this, "Could not delete temp file.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (res.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", ProfileActivity.this);
                        } else if (res.equalsIgnoreCase("Database Error")) {
                            methods.showAlert("Response", "Could not write database reference.", ProfileActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (
                            Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), ProfileActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (
                Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void convertUriToFile(Uri uri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.base_dir) + "/" + getString(R.string.temp));
            if (file.exists() && file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    new File(file, children[i]).delete();
                }
            } else {
                file.mkdirs();
            }

            File profile = new File(file + "/" + fileInfor(uri).get(0) + "." + fileInfor(uri).get(1));
            if (!profile.exists()) {
                profile.createNewFile();
            }
            outputStream = new FileOutputStream(profile);
            byte[] buffer = new byte[4 * 1024];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            realProfilePath = profile;

        } catch (Exception e) {
            System.out.println(e);
            try {
                inputStream.close();
                outputStream.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private ArrayList<String> fileInfor(Uri uri) {
        Uri returnUri = uri;
        ArrayList<String> fileinfor = new ArrayList<>();
        Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String mimeType = getContentResolver().getType(returnUri);
        String name = returnCursor.getString(nameIndex);

        int dot = name.lastIndexOf(".");
        String extension = name.substring(dot + 1);
        String real_name = name.substring(0, dot);
        fileinfor.add(real_name);
        fileinfor.add(extension);

        return fileinfor;
    }

    /*
     * method performs retrieval of user details
     */
    private void getMyDetails() {
        RequestBody id = RequestBody.create(MultipartBody.FORM, String.valueOf(methods.getUserId(ProfileActivity.this)));
        RequestBody acc = RequestBody.create(MultipartBody.FORM, methods.getUserAccountNo(ProfileActivity.this));
        Call<ResponseBody> details = apiInterface.GetMyDetails(id, acc);
        methods.showDialog(mDialog, "Loading details...", true);
        details.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Dismiss", false);
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JsonParser parser = new JsonParser();
                        String result = parser.parse(responseData).getAsString();
                        if (result.length() == 0) {
                            Toast.makeText(ProfileActivity.this, "Could not load user info.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray array = new JSONArray(result);
                        if (array.length() == 0) {
                            Toast.makeText(ProfileActivity.this, "Could not load user info.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject jsonObject = array.getJSONObject(0);
                        String res = jsonObject.getString("response");
                        if (res.equalsIgnoreCase("error")) {
                            methods.showAlert("Server error", "Server error", ProfileActivity.this);
                        } else if (res.equalsIgnoreCase("no user found")) {
                            methods.showAlert("User not found", "Could not find user.", ProfileActivity.this);
                        } else if (res.equalsIgnoreCase("success")) {
                            mName.setText(jsonObject.getString("name"));
                            mSurname.setText(jsonObject.getString("surname"));
                            mCurrDob.setText(mCurrDob.getText() + " : " + methods.getReadableDate(jsonObject.getString("dob"), ProfileActivity.this));
                            //mDateOfBirth = jsonObject.getString("dob");
                            if (jsonObject.getString("isdobpublic").equalsIgnoreCase("")) {
                                mDobStatus.setChecked(false);
                            } else {
                                if (jsonObject.getString("isdobpublic").equalsIgnoreCase("True")) {
                                    mDobStatus.setChecked(true);
                                } else {
                                    mDobStatus.setChecked(false);
                                }
                            }
                            if (jsonObject.getString("gender").equalsIgnoreCase("M")) {
                                mGender.setSelection(0);
                            } else if (jsonObject.getString("gender").equalsIgnoreCase("F")) {
                                mGender.setSelection(1);
                            } else {
                                mGender.setSelection(2);
                            }
                            mOccupation.setText(jsonObject.getString("occupation"));
                            phoneNumber.setText(jsonObject.getString("phone"));
                            if (jsonObject.getString("email").equalsIgnoreCase("")) {
                                //mEmail.setText("no email");
                            } else {
                                mEmail.setText(jsonObject.getString("email"));
                            }
                            if (jsonObject.getString("biography").equalsIgnoreCase("")) {
                                mBiography = "N/A";
                            } else {
                                mBiography = jsonObject.getString("biography");
                            }
                            String prov_name = prov_dis_helper.getProvinceName(jsonObject.getString("prov_id"));
                            String dis_name = prov_dis_helper.getDistrictName(jsonObject.getString("dist_id"));
                            mProvince.setText(mProvince.getText() + prov_name);
                            mDistrict.setText(mDistrict.getText() + dis_name);
                            mProfileServerPath = jsonObject.getString("path");
                            mProfileServerUrl = jsonObject.getString("profile");
                            requestOptions = new RequestOptions();
                            requestOptions.centerCrop();
                            requestOptions.placeholder(R.drawable.ic_account_circle);
                            requestOptions.error(R.drawable.ic_account_circle);

                            Glide.with(ProfileActivity.this)
                                    .applyDefaultRequestOptions(requestOptions)
                                    .load(jsonObject.getString("profile"))
                                    .into(mProfile);

                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    methods.showAlert("Error", e.toString(), ProfileActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                methods.showDialog(mDialog, "Dismiss", false);
                methods.showAlert("Failure", t.toString(), ProfileActivity.this);
            }
        });
    }

    /*
     * method performs updating of user details
     */
    private void updateDetails() {

        String fname = mName.getText().toString().trim();//first name
        String surname = mSurname.getText().toString().trim();//surname
        String dobPublic;
        if (mDobStatus.isChecked()) {
            dobPublic = "True";
        } else {
            dobPublic = "False";
        }
        //String province = mProvince.getSelectedItem().toString().trim();//province
        //String district = mDistrict.getSelectedItem().toString().trim();//district
        String gender = mGender.getSelectedItem().toString().trim();//gender
        String occupation = mOccupation.getText().toString().trim();//occupation
        String phone = phoneNumber.getText().toString().trim();//phone number
        String mail = mEmail.getText().toString().trim();//email

        RequestBody name = RequestBody.create(MultipartBody.FORM, fname);
        RequestBody sur = RequestBody.create(MultipartBody.FORM, surname);
        //RequestBody prov = RequestBody.create(MultipartBody.FORM, prov_dis_helper.provID(province));
        //RequestBody dis = RequestBody.create(MultipartBody.FORM, prov_dis_helper.disID(district));
        RequestBody sex = RequestBody.create(MultipartBody.FORM, gender);
        RequestBody occu = RequestBody.create(MultipartBody.FORM, occupation);
        RequestBody number = RequestBody.create(MultipartBody.FORM, phone);
        RequestBody email = RequestBody.create(MultipartBody.FORM, mail);
        RequestBody status = RequestBody.create(MultipartBody.FORM, dobPublic);
        RequestBody id = RequestBody.create(MultipartBody.FORM, String.valueOf(methods.getUserId(ProfileActivity.this)));
        RequestBody acc = RequestBody.create(MultipartBody.FORM, methods.getUserAccountNo(ProfileActivity.this));
        Call<ResponseBody> update = apiInterface.UpdateProfileInfo(name, sur, status, sex, occu, number, email, id, acc);
        methods.showDialog(mDialog, "Updating...", true);
        update.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "Updating...", false);
                    String result = response.body().string();
                    String message = methods.removeQoutes(result);

                    if (message.equalsIgnoreCase("Success")) {
                        methods.showAlert("Response", "Successfully updated details.", ProfileActivity.this);
                    } else if (message.equalsIgnoreCase("Failed")) {
                        methods.showAlert("Response", "Registration failed.Please try again.", ProfileActivity.this);
                    } else if (message.equalsIgnoreCase("Error")) {
                        methods.showAlert("Response", "Server error.", ProfileActivity.this);
                    }
                    //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    methods.showDialog(mDialog, "Updating...", false);
                    methods.showAlert("Request failed", "Request failed " + t.toString(), ProfileActivity.this);
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
     * method performs removal of profile pic
     */
    private void removeProfile(String url, String path) {
        RequestBody urL = RequestBody.create(MultipartBody.FORM, url);
        RequestBody patH = RequestBody.create(MultipartBody.FORM, path);
        RequestBody uid = RequestBody.create(MultipartBody.FORM, String.valueOf(methods.getUserId(ProfileActivity.this)));
        Call<ResponseBody> upload = apiInterface.RemoveProfile(uid, urL, patH);
        methods.showDialog(mDialog, "Removing profile pic...", true);
        upload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    methods.showDialog(mDialog, "dismiss", false);
                    String result = response.body().string();
                    String res = methods.removeQoutes(result);

                    if (res.equalsIgnoreCase("Failed")) {
                        methods.showAlert("Response", "Could not remove profile pic.Try again.", ProfileActivity.this);
                    } else if (res.equalsIgnoreCase("Success")) {
                        methods.showAlert("Response", "Profile removed.", ProfileActivity.this);
                        pic_options.setVisibility(View.GONE);
                        Glide.with(ProfileActivity.this)
                                .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                                .load(R.drawable.ic_account_circle)
                                .into(mProfile);
                    } else if (res.equalsIgnoreCase("Error")) {
                        methods.showAlert("Response", "Server error.", ProfileActivity.this);
                    }
                    //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    methods.showDialog(mDialog, "dismiss", false);
                    methods.showAlert("Request failed", "Request failed " + t.toString(), ProfileActivity.this);
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
     * temp file for image captured from camera
     */
    private File tempFile() {
        File file = null;
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.base_dir) + getString(R.string.temp_camera_image));
            if (dir.exists() && dir.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            } else {
                dir.mkdirs();
            }
            file = new File(dir + "/" + "temp_image.jpg");
        } catch (Exception e) {
            Toast.makeText(this, "Error creating temp file.", Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    //file for camera image(internal storage)
    private File getImageFile() throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageName = "image_" + time;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        mCurrentPathFromCameraFile = imageFile.getAbsolutePath();
        return imageFile;
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
                        } else {
                            Toast.makeText(ProfileActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), ProfileActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("List onFailure", t.toString(), ProfileActivity.this);
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

                        } else {
                            Toast.makeText(ProfileActivity.this, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), ProfileActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("List onFailure", t.toString(), ProfileActivity.this);
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
