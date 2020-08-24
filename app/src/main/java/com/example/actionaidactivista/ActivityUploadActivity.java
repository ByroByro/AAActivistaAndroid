package com.example.actionaidactivista;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.navigation.MainBottomNavActivity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.example.actionaidactivista.search.search_adapter;
import com.example.actionaidactivista.search.search_model;
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
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class ActivityUploadActivity extends Fragment {

    private EditText mDescriptionText, mFilename;
    private Button mDate;//button for date
    //private Button mPickMedia;//button for attaching media file
    private ImageButton mPickMedia;//image button for picking media
    private Spinner mmimeType;//spinner for mime type
    private EditText mContent;//edit text for getting content if mime type is text
    private String mMimeType = "";
    private Button mDismiss;//button for dismissing card view
    private CardView mTip;//card view tip
    private TextInputEditText mLocation;
    //code for file system request
    final int FILE_SYSTEM = 100;
    private Uri mFilepathUri;
    private File file;
    private Calendar calendar;
    int year, month, day;
    private DatePickerDialog datePickerDialog;
    private String mimeType = "";
    private String intType = "";
    private String fileType = "";
    private Dialog mDialog;
    private Dialog dialog;
    private ApiInterface apiInterface;
    private SharedPreferences sharedPreferences;
    private ArrayList<String> mParticipants;
    private search_adapter mAdapter;//search adapter
    private List<search_model> list;//list of type model
    private Spinner mActivityType;//activity type spinner

    public ActivityUploadActivity() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        try {
            // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.activity_upload, container, false);
            //set title
            ((MainBottomNavActivity) getActivity()).setActionBarTitle(getString(R.string.upload_app_bar_title));

            mPickMedia = root.findViewById(R.id.attach_file);
            mmimeType = root.findViewById(R.id.file_type);
            mFilename = root.findViewById(R.id.filename);
            mContent = (EditText) root.findViewById(R.id.text_content);
            calendar = Calendar.getInstance();
            mDate = (Button) root.findViewById(R.id.date);
            mDismiss = (Button) root.findViewById(R.id.dismiss);
            mTip = (CardView) root.findViewById(R.id.tip_card_view);
            mActivityType = (Spinner) root.findViewById(R.id.activity_type);
            mDescriptionText = (EditText) root.findViewById(R.id.description);
            mLocation = (TextInputEditText) root.findViewById(R.id.location);

            //set attach file and content to gone
            mPickMedia.setVisibility(View.VISIBLE);
            mContent.setVisibility(View.GONE);
            sharedPreferences = getContext().getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);

            mDialog = new Dialog(getContext());
            dialog = new Dialog(getContext());
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

            mParticipants = new ArrayList<>();

            //make sure options menu for fragment shows
            setHasOptionsMenu(true);

            //get activity types
            getActivityTypes();

            Button upload = (Button) root.findViewById(R.id.upload_activity);
            upload.setOnClickListener(v -> {
                try {

                    String participants;
                    //do participants logic
                    if (mParticipants.size() > 0) {
                        //activity has tagged participants
                        //methods.showAlert("List", mParticipants.toString(), getContext());
                        participants = mParticipants.toString();
                    } else {
                        //activity has no tagged participants
                        //methods.showAlert("List", mParticipants.toString(), getContext());
                        participants = "No tagged";
                    }
                    //check if person is registered
                    //check if user is logged
                    if (!methods.checkUserValidity(getContext())) {
                        methods.showAlert("User Account Required", "A valid user account is required.Sign in first.Or your account is admin.", getContext());
                        return;
                    }
                    String userid = "";
                    String accno;
                    if (methods.getUserAccountNo(getContext()).equalsIgnoreCase("unloged")) {
                        methods.showAlert("Sign in required", "You need to sign in before uploading activities.", getContext());
                        return;
                    }
                    if (methods.getUserAccountNo(getContext()).equalsIgnoreCase("error")) {
                        methods.showAlert("Error", "An error occurred.", getContext());
                        return;
                    }

                    //check if is admin
                    if (methods.getUserAccountNo(getContext()).equalsIgnoreCase("admin")) {
                        methods.showAlert("User only", "You are an admin.", getContext());
                        return;
                    }

                    accno = methods.getUserAccountNo(getContext());

                    if (methods.getUserId(getContext()) == 0) {
                        methods.showAlert("Sign in required", "Please sign in first", getContext());
                    } else {
                        userid = String.valueOf(methods.getUserId(getContext()));
                    }

                    String description = mDescriptionText.getText().toString().trim();
                    String location = mLocation.getText().toString().trim();
                    String date = mDate.getText().toString().trim();
                    String type = mmimeType.getSelectedItem().toString();
                    String activity_type = mActivityType.getSelectedItem().toString();
                    if (mFilepathUri == null && !type.equalsIgnoreCase("text")) {
                        Toast.makeText(getContext(), "Select file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String content = "";
                    if (mmimeType.getSelectedItemId() == 1) {
                        fileType = "N/A";
                        mimeType = "text/plain";
                        intType = "0";
                        content = mContent.getText().toString().trim();
                    } else if (mmimeType.getSelectedItemId() == 0) {
                        mimeType = "image";
                        intType = "1";
                        mimeType = getContext().getContentResolver().getType(mFilepathUri);
                    } else if (mmimeType.getSelectedItemId() == 2) {
                        mimeType = "video";
                        intType = "2";
                        mimeType = getContext().getContentResolver().getType(mFilepathUri);
                    }

                    if (mmimeType.getSelectedItemId() == 1) {
                        if (description.equalsIgnoreCase("") || content.equalsIgnoreCase("")) {
                            methods.showAlert("Missing fields", "Enter all information.", getContext());
                            return;
                        }
                    }

                    if (date.equalsIgnoreCase("Pick date")) {
                        methods.showAlert("Missing fields", "Enter date.", getContext());
                        return;
                    }
                    if (location.equalsIgnoreCase("")) {
                        methods.showAlert("Missing fields", "Enter location.", getContext());
                        return;
                    }

                    String dtePosted = methods.changeDateFormat(date);
                    if (mmimeType.getSelectedItemId() == 1) {
                        postTextActivity(description, dtePosted, fileType, mimeType, intType, content, userid, accno, location, "N/A", participants, activity_type);
                    } else {
                        postBinaryActivity(description, dtePosted, fileType, mimeType, intType, file, mFilepathUri, userid, accno, location, "N/A", participants, activity_type);
                    }

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error raising event", Toast.LENGTH_SHORT).show();
                }
            });

            mDismiss.setOnClickListener(v -> {
                try {
                    mTip.setVisibility(View.GONE);//set the card view to gone
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error raising event", Toast.LENGTH_SHORT).show();
                }
            });

            mPickMedia.setOnClickListener(v -> {
                //try to create base dir
                try {
                    //check for permission to read/write external storage
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        //intent for picking file
                        //get file type
                        //check if media is mounted and if there is enough space when creating app files
                        File root1 = Environment.getExternalStorageDirectory();
                        File base_dir = new File(root1.getAbsolutePath() + getString(R.string.base_dir));
                        if (!base_dir.exists()) {
                            base_dir.mkdirs();
                        }
                        String type = mmimeType.getSelectedItem().toString();

                        if (type.equalsIgnoreCase("image")) {
                            mMimeType = "image/*";
                            fileType = "image";
                            intType = "1";
                        } else if (type.equalsIgnoreCase("audio")) {
                            mMimeType = "audio/mp3";
                            fileType = "audio";
                            intType = "4";
                            try {
                                //Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("audio/*");
                                startActivityForResult(Intent.createChooser(intent, "Select Audio"), FILE_SYSTEM);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        } else if (type.equalsIgnoreCase("video")) {
                            mMimeType = "video/*";
                            fileType = "video";
                            intType = "2";
                        }
                        if (!fileType.equalsIgnoreCase("audio")) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType(mMimeType);
                            startActivityForResult(intent, FILE_SYSTEM);
                        }
                    } else {
                        askForPermission();
                    }

                } catch (Exception e) {
                    System.out.println(e);
                }
            });

            mDate.setOnClickListener(v -> {
                if (v == mDate) {
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

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
                            mDate.setText(datenow);
                        }
                    }, year, month, day);
                    datePickerDialog.show();
                }
            });

            mmimeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        String sel = mmimeType.getSelectedItem().toString();
                        switch (sel) {
                            case "Video":
                                mPickMedia.setVisibility(View.VISIBLE);
                                mContent.setVisibility(View.GONE);
                                break;
                            case "Text":
                                mPickMedia.setVisibility(View.GONE);
                                mContent.setVisibility(View.VISIBLE);
                                break;
                            case "Image":
                                mPickMedia.setVisibility(View.VISIBLE);
                                mContent.setVisibility(View.GONE);
                                break;
                            case "Audio":
                                mPickMedia.setVisibility(View.VISIBLE);
                                mContent.setVisibility(View.GONE);
                                break;
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error on mime type selection", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading UI", Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    private void askForPermission() {
        //get user permission to read/write file system
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_SYSTEM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FILE_SYSTEM && resultCode == RESULT_OK && data != null) {
            try {
                //Uri uri = data.getData();
                mFilepathUri = data.getData();

                String subdir = getContext().getString(R.string.temp);

                file = convertUriToFile(mFilepathUri, subdir);
                mFilename.setEnabled(false);
                mFilename.setText("");
                String filename = file.toString().substring(file.toString().lastIndexOf("/") + 1);
                mFilename.setText(filename);
            } catch (Exception e) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            inflater.inflate(R.menu.custom_search_menu, menu);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int selected = item.getItemId();
            switch (selected) {
                case R.id.m_custom_search:
                    //custom_search cs = new custom_search("Users", "Add or remove participants", getContext());
                    //cs.showDialog(dialog);

                    //show dialog
                    dialog.setContentView(R.layout.custom_search_dialog);

                    //initialise widgets
                    TextView mTip = dialog.findViewById(R.id.search_tip);
                    //set text on tip
                    mTip.setText("Search Query = Name or Surname");

                    TextInputEditText mSearchQuery = dialog.findViewById(R.id.first_param);
                    Button mSearch = dialog.findViewById(R.id.search);
                    Button mOk = dialog.findViewById(R.id.ok);
                    RecyclerView mSearchedResult = dialog.findViewById(R.id.search_list_recycler);
                    //deny cancelling when user touches outside
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setOnKeyListener((dialog, keyCode, event) -> {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            //mDialog.dismiss();
                        }
                        return true;
                    });

                    mOk.setOnClickListener(v -> {
                        try {
                            dialog.dismiss();
                            mParticipants = mAdapter.getParticipants();
                        } catch (Exception e) {
                            //Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    //set on search button click listener
                    mSearch.setOnClickListener(v -> {
                        try {
                            String queryString = mSearchQuery.getText().toString().trim();
                            if (queryString.equalsIgnoreCase("")) {
                                Toast.makeText(getContext(), "Enter query string.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                            mSearchedResult.setLayoutManager(linearLayoutManager);
                            mSearchedResult.setItemAnimator(new DefaultItemAnimator());

                            RequestBody query = RequestBody.create(MultipartBody.FORM, queryString);

                            Call<ResponseBody> users = apiInterface.Search(query);
                            methods.showDialog(mDialog, "Loading users...", true);
                            users.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        methods.showDialog(mDialog, "Dismiss", false);
                                        if (response.isSuccessful()) {
                                            String responseData = response.body().string();
                                            JsonParser parser = new JsonParser();
                                            String result = parser.parse(responseData).getAsString();
                                            if (result.length() == 0) {
                                                Toast.makeText(getContext(), "No more users.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            JSONArray array = new JSONArray(result);
                                            if (array.length() == 0) {
                                                methods.showAlert("No more data", "There is/are no user(s) found tha match the name or surname.", getContext());
                                                //Toast.makeText(getContext(), "No more feeds.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            list = new ArrayList<>();
                                            search_model data;
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject jsonObject = array.getJSONObject(i);
                                                data = new search_model();
                                                data.setId(jsonObject.getString("userid"));
                                                data.setDisplayName(jsonObject.getString("fname") + " " + jsonObject.getString("lname"));
                                                list.add(data);
                                            }
                                            mAdapter = new search_adapter(list, "Users", "Add or remove participants", getContext());
                                            mSearchedResult.setAdapter(mAdapter);
                                        } else {
                                            Toast.makeText(getContext(), "Request unsuccessful", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                        methods.showAlert("Error", e.toString(), getContext());
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    methods.showDialog(mDialog, "Dismiss", false);
                                    methods.showAlert("Failure", t.toString(), getContext());
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error raising search.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //show the dialog
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    break;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error on select menu item", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
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
            inputStream = getActivity().getContentResolver().openInputStream(uri);
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
        Cursor returnCursor = getActivity().getContentResolver().query(returnUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String mimeType = getActivity().getContentResolver().getType(returnUri);
        String name = returnCursor.getString(nameIndex);

        int dot = name.lastIndexOf(".");
        String extension = name.substring(dot + 1);
        String real_name = name.substring(0, dot);
        fileinfor.add(real_name);
        fileinfor.add(extension);

        return fileinfor;
    }

    /*
     * performs post text data to server
     */
    private void postTextActivity(String des, String date, String ftyp, String mime, String intType, String content, String id, String accno, String loca, String geoloca, String tags, String act_type) {
        try {
            RequestBody dscr = RequestBody.create(MultipartBody.FORM, des);
            RequestBody dat = RequestBody.create(MultipartBody.FORM, date);
            RequestBody filetype = RequestBody.create(MultipartBody.FORM, ftyp);
            RequestBody mimetyp = RequestBody.create(MultipartBody.FORM, mime);
            RequestBody intTyp = RequestBody.create(MultipartBody.FORM, intType);
            RequestBody cont = RequestBody.create(MultipartBody.FORM, content);
            RequestBody userid = RequestBody.create(MultipartBody.FORM, id);
            RequestBody acc = RequestBody.create(MultipartBody.FORM, accno);
            RequestBody location = RequestBody.create(MultipartBody.FORM, loca);
            RequestBody geolocation = RequestBody.create(MultipartBody.FORM, geoloca);
            RequestBody parts = RequestBody.create(MultipartBody.FORM, tags);
            RequestBody activity = RequestBody.create(MultipartBody.FORM, act_type);

            Call<ResponseBody> text = apiInterface.PostTextFeed(dscr, dat, filetype, mimetyp, intTyp, cont, userid, acc, location, geolocation, parts, activity);
            methods.showDialog(mDialog, "Posting text feed...", true);
            text.enqueue(new Callback<ResponseBody>() {
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
                            mDate.setText("Pick Date");
                            mDescriptionText.setText("");
                            mContent.setText("");
                            mLocation.setText("");

                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", getContext());
                        } else if (message.equalsIgnoreCase("Exist")) {
                            methods.showAlert("Response", "There is another activity with the same details.", getContext());
                        } else if (message.equalsIgnoreCase("Account Inactive")) {
                            methods.showAlert("Response", "Your account is deactivated.Contact your admin(s).", getContext());
                        } else if (message.equalsIgnoreCase("alumni")) {
                            methods.showAlert("Response", "You are now an alumni,therefor you are no longer allowed to upload activities.", getContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RegistrationActivity.Level, "alumni");
                            editor.apply();
                        }else if (message.equalsIgnoreCase("3 months have lapsed")) {
                            methods.showAlert("Response", "Your account has been deactivated because of 90 days of inactivity.Contact your admin(s).", getContext());
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showRequestFailedDialog(getContext());
                        //methods.showAlert("Request failed", "Request failed " + t.toString(), getContext());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error raising upload operation", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * performs post binary data to server
     */
    private void postBinaryActivity(String des, String date, String ftyp, String mime, String intType, File file, Uri uri, String id, String accno, String loca, String geoloca, String tags, String act_type) {
        try {
            //check if its binary upload and uri is not null
            if (mFilepathUri == null && mmimeType.getSelectedItemId() == 1) {
                methods.showAlert("Missing data", "Select a file please.", getContext());
                return;
            }

            RequestBody dscr = RequestBody.create(MultipartBody.FORM, des);
            RequestBody dat = RequestBody.create(MultipartBody.FORM, date);
            RequestBody filetype = RequestBody.create(MultipartBody.FORM, ftyp);
            RequestBody mimetyp = RequestBody.create(MultipartBody.FORM, mime);
            RequestBody intTyp = RequestBody.create(MultipartBody.FORM, intType);
            RequestBody userid = RequestBody.create(MultipartBody.FORM, id);
            RequestBody acc = RequestBody.create(MultipartBody.FORM, accno);
            RequestBody location = RequestBody.create(MultipartBody.FORM, loca);
            RequestBody geolocation = RequestBody.create(MultipartBody.FORM, geoloca);
            RequestBody parts = RequestBody.create(MultipartBody.FORM, tags);
            RequestBody activity = RequestBody.create(MultipartBody.FORM, act_type);

            RequestBody the_file = RequestBody.create(
                    MediaType.parse(getContext().getContentResolver().getType(uri)),
                    file
            );

            MultipartBody.Part actual = MultipartBody.Part.createFormData("file", file.getName(), the_file);

            Call<ResponseBody> binary = apiInterface.PostMediaFeed(dscr, dat, filetype, mimetyp, intTyp, userid, acc, actual, location, geolocation, parts, activity);
            methods.showDialog(mDialog, "Posting media feed...", true);
            binary.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Upload failed.Try again.", getContext());
                        } else if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Upload successful.", getContext());

                            //clear edit texts
                            mDate.setText("Pick Date");
                            mDescriptionText.setText("");
                            mContent.setText("");
                            mLocation.setText("");

                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", getContext());
                        } else if (message.equalsIgnoreCase("Exist")) {
                            methods.showAlert("Response", "There is another opportunity with the same details.", getContext());
                        } else if (message.equalsIgnoreCase("Account Inactive")) {
                            methods.showAlert("Response", "Your account is deactivated.Contact your admin(s).", getContext());
                        } else if (message.equalsIgnoreCase("alumni")) {
                            methods.showAlert("Response", "You are now an alumni,therefor you are longer allowed to upload activities.", getContext());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(RegistrationActivity.Level, "alumni");
                            editor.apply();
                        } else if (message.equalsIgnoreCase("3 months have lapsed")) {
                            methods.showAlert("Response", "Your account has been deactivated because of 90 days of inactivity.Contact your admin(s).", getContext());
                        }
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
            Toast.makeText(getContext(), "Error raising upload operation", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * performs retrieve activity types
     */
    private void getActivityTypes() {
        try {
            Call<ResponseBody> dis = apiInterface.getActivityTypes();
            methods.showDialog(mDialog, "Loading activity types...", true);
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
                            ArrayList<String> acts = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                acts.add(jsonObject.getString("description"));
                            }

                            ArrayAdapter<String> itemsAdapter1 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, acts);
                            itemsAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mActivityType.setAdapter(itemsAdapter1);
                        } else {
                            Toast.makeText(getContext(), "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), getContext());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("List onFailure", t.toString(), getContext());
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
