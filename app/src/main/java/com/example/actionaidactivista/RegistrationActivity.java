package com.example.actionaidactivista;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.actionaidactivista.database.prov_dis_helper;
import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private Button mDob;//date of birth button
    private Button mJoiningDate;//joining date
    private Button mRegister;//register button
    Calendar calendar;//calender
    int year, month, day;
    private Dialog mDialog;//custom dialog
    DatePickerDialog datePickerDialog;
    private boolean isDobPublic = false;
    private TextInputEditText mName;//first name
    private TextInputEditText mSurname;//surname
    private TextInputEditText mOccupation;//person's occupation
    private TextInputEditText phoneNumber;//user phone no
    private Spinner mProvince;//province spinner
    private Spinner mDistrict;//province spinner
    private Spinner mGender;//province spinner
    private ProgressDialog mProgress;//progress dialog
    private CheckBox mDobStatus;//determining whether dob will appear as age or as is
    private TextInputEditText mEmail;//email field
    private TextInputEditText mBiography;//biography field
    private Spinner mMimeType;//national id file type {image , doc }
    private String mDocType;//doc type string
    private Uri mFilepathUri;//file uri
    private File file;//file
    private ImageButton mAttachId;//attach nat id
    //code for file system request
    final int FILE_SYSTEM = 100;
    private com.example.actionaidactivista.database.prov_dis_helper prov_dis_helper;
    //retrofit
    private ApiInterface apiInterface;
    //SHARED PREFS CODE
    public static final String ACC_PREFERENCES = "AccountPreferences";
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

            //initialise widgets
            mDob = (Button) findViewById(R.id.dob);
            mJoiningDate = (Button) findViewById(R.id.joining_date);
            mRegister = (Button) findViewById(R.id.register);
            calendar = Calendar.getInstance();
            mName = (TextInputEditText) findViewById(R.id.first_name);
            mSurname = (TextInputEditText) findViewById(R.id.surname);
            mOccupation = (TextInputEditText) findViewById(R.id.occupation);
            phoneNumber = (TextInputEditText) findViewById(R.id.phone);
            mProvince = (Spinner) findViewById(R.id.province);
            mDistrict = (Spinner) findViewById(R.id.district);
            mGender = (Spinner) findViewById(R.id.gender);
            mDobStatus = (CheckBox) findViewById(R.id.dob_Status);
            mEmail = (TextInputEditText) findViewById(R.id.email);
            mBiography = (TextInputEditText) findViewById(R.id.biography);
            mProgress = new ProgressDialog(this);
            mDialog = new Dialog(this);
            mMimeType = (Spinner) findViewById(R.id.file_type);
            mAttachId = (ImageButton) findViewById(R.id.attach_file);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            prov_dis_helper = new prov_dis_helper(this, "", null);

            //check if there are zero provinces
            if (prov_dis_helper.getProvinces().size() == 0) {
                //get provinces from server
                getProvinces();
            }
            //check if there are zero provinces
            if (prov_dis_helper.getDistricts().size() == 0) {
                //get districts from server
                getDistricts();
            }

            mDobStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    if (isChecked) {
                        //set public to true
                        isDobPublic = true;
                        //Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
                    } else {
                        //set public to false
                        isDobPublic = false;
                        //Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to react to check change", Toast.LENGTH_SHORT).show();
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
                    String occupation = mOccupation.getText().toString().trim();//occupation
                    String phone = phoneNumber.getText().toString().trim();//phone number
                    String mail = mEmail.getText().toString().trim();//email
                    String bio = mBiography.getText().toString().trim();//biography
                    //String joining_date = mJoiningDate.getText().toString().trim();//joining date

                    if (dob.equalsIgnoreCase("Pick date of birth")) {
                        Toast.makeText(RegistrationActivity.this, "Pick date of birth.", Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    if (joining_date.equalsIgnoreCase("Joining date")) {
//                        Toast.makeText(RegistrationActivity.this, "Pick joining date.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    //check age
                    if (methods.checkAge(dob, this)) {
                        //over age
                        methods.showAlert("Over Age", "You are not allowed to register because members must be less than 36 years.", this);
                        return;
                    }
                    if (fname.equalsIgnoreCase("") || surname.equalsIgnoreCase("") || occupation.equalsIgnoreCase("") || phone.equalsIgnoreCase("")) {
                        methods.showAlert("Missing Info", "Enter all info.", this);
                        return;
                    }

                    String b;
                    if (bio.equalsIgnoreCase("")) {
                        b = "N/A";
                    } else {
                        b = bio;
                    }

                    if (mail.equalsIgnoreCase("")) {
                        methods.showAlert("Missing Info", "Enter email.", this);
                        return;
                    }

                    String dobPublic;
                    if (isDobPublic) {
                        dobPublic = "True";
                    } else {
                        dobPublic = "False";
                    }

                    //identity doc
                    //check if its binary upload and uri is not null
                    if (file == null) {
                        methods.showAlert("Missing info", "Select a file.", this);
                        return;
                    }

                    RequestBody the_file = RequestBody.create(
                            MediaType.parse(this.getContentResolver().getType(mFilepathUri)),
                            file
                    );

                    MultipartBody.Part actual_file = MultipartBody.Part.createFormData("file", file.getName(), the_file);

                    RequestBody name = RequestBody.create(MultipartBody.FORM, fname);
                    RequestBody sur = RequestBody.create(MultipartBody.FORM, surname);
                    RequestBody db = RequestBody.create(MultipartBody.FORM, methods.changeDateFormat(dob));
                    RequestBody prov = RequestBody.create(MultipartBody.FORM, prov_dis_helper.provID(province));
                    RequestBody dis = RequestBody.create(MultipartBody.FORM, prov_dis_helper.disID(district));
                    RequestBody sex = RequestBody.create(MultipartBody.FORM, gender);
                    RequestBody occu = RequestBody.create(MultipartBody.FORM, occupation);
                    RequestBody number = RequestBody.create(MultipartBody.FORM, phone);
                    RequestBody biography = RequestBody.create(MultipartBody.FORM, b);
                    RequestBody email = RequestBody.create(MultipartBody.FORM, mail);
                    RequestBody status = RequestBody.create(MultipartBody.FORM, dobPublic);
                    //RequestBody join_date = RequestBody.create(MultipartBody.FORM, methods.changeDateFormat(joining_date));
                    RequestBody doc_type = RequestBody.create(MultipartBody.FORM, mDocType);

                    Call<ResponseBody> registerUser = apiInterface.RegisterUser(name, sur, db, prov, dis, sex, occu, number, email, biography, status,doc_type,actual_file);
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
                                    methods.showAlert("Response", "Successfully registered.Your account is pending approval.You can use Settings menu to check.", RegistrationActivity.this);
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
                                methods.showRequestFailedDialog(RegistrationActivity.this);
                            } catch (Exception e) {
                                Toast.makeText(RegistrationActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(RegistrationActivity.this, "Error raising registration event", Toast.LENGTH_SHORT).show();
                }
            });
            //set joining date on click listener
            mJoiningDate.setOnClickListener(v -> {
                if (v == mJoiningDate) {
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            String datenow;
                            if (dayOfMonth > 9 && (month + 1) < 10) {
                                datenow = dayOfMonth + "-0" + (month + 1) + "-" + year;
                            } else if (dayOfMonth < 10 && (month + 1) > 9) {

                                datenow = "0" + dayOfMonth + "-" + (month + 1) + "-" + year;
                            } else if (dayOfMonth < 10 && (month + 1) < 10) {

                                datenow = "0" + dayOfMonth + "-0" + (month + 1) + "-" + year;
                            } else {

                                datenow = dayOfMonth + "-" + (month + 1) + "-" + year;
                            }
                            mJoiningDate.setText(datenow);
                        }
                    }, year, month, day);
                    datePickerDialog.show();
                }
            });
            //attach file event
            mAttachId.setOnClickListener(v -> {
                try {
                    //check for permission to read/write external storage
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        //intent for picking file
                        //get file type
                        //check if media is mounted and if there is enough space when creating app files
                        File root1 = Environment.getExternalStorageDirectory();
                        File base_dir = new File(root1.getAbsolutePath() + getString(R.string.base_dir));
                        if (!base_dir.exists()) {
                            base_dir.mkdirs();
                        }
                        String type = mMimeType.getSelectedItem().toString();
                        if (type.equalsIgnoreCase("Picture")) {
                            mDocType = "image";
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Pic"), FILE_SYSTEM);
                        } else {
                            mDocType = "doc";
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            String[] mime_types = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword", "application/pdf"};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mime_types);
                            startActivityForResult(intent, FILE_SYSTEM);
                        }

                    } else {
                        askForPermission();
                    }
                } catch (Exception e) {
                    Toast.makeText(RegistrationActivity.this, "Error raising event.", Toast.LENGTH_SHORT).show();
                }
            });
            //remove focus on edit texts when the activity loads
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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

    private void askForPermission() {
        //get user permission to read/write file system
        ActivityCompat.requestPermissions(RegistrationActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_SYSTEM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FILE_SYSTEM && resultCode == RESULT_OK && data != null) {
            try {
                //Uri uri = data.getData();
                mFilepathUri = data.getData();

                String subdir = this.getString(R.string.temp);

                file = convertUriToFile(mFilepathUri, subdir);
                String filename = file.toString().substring(file.toString().lastIndexOf("/") + 1);
                Toast.makeText(this, filename, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * must perform this operation in thread, in case file os to large UI will hang
     * method takes URI of picked file and sub folder ,then gets the the file from uri
     * and writes it into the base dir + subfolder
     */
    public File convertUriToFile(Uri uri, String folder) {
        File realFile = null;
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.base_dir) + folder);
            if (file.exists() && file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    new File(file, children[i]).delete();
                }
            } else {
                file.mkdirs();
            }

            File file1 = new File(file + "/" + fileInfor(uri).get(0) + "." + fileInfor(uri).get(1));
            if (!file1.exists()) {
                file1.createNewFile();
            }
            OutputStream outputStream = new FileOutputStream(file1);
            byte[] buffer = new byte[4 * 1024];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            realFile = file1;

        } catch (Exception e) {
            System.out.println(e);
            try {
                inputStream.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        return realFile;
    }

    /*
     * gets uri and extracts the file name and extension then returns the info
     * in an List with name at 0 and ext at 1
     */
    public ArrayList<String> fileInfor(Uri uri) {
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

    //override on back pressed

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //go to main bottom nav activity
        Intent intent = new Intent(this, MainBottomNavActivity.class);
        startActivity(intent);
    }
}
