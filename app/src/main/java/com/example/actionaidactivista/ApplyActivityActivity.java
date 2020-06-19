package com.example.actionaidactivista;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApplyActivityActivity extends AppCompatActivity {

    Intent mIntent;
    String mOpportunityID;
    private Spinner mDocType;
    private Button mAttach;
    private Button mUpload;

    //code for file system request
    final int FILE_SYSTEM = 100;
    private Uri mFilepathUri;
    private File file;

    //retrofit
    private ApiInterface apiInterface;
    private Dialog mDialog;
    private String mDocuType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //inflate layout
            setContentView(R.layout.activity_apply_activity);
            //set title
            getSupportActionBar().setTitle("Apply Opportunity");
            //get extra info
            mIntent = getIntent();
            mOpportunityID = mIntent.getStringExtra("opportunity_id");
            mAttach = (Button) findViewById(R.id.attach_opportunity_file);
            mUpload = (Button) findViewById(R.id.upload_opportunity_file);
            mDocType = (Spinner) findViewById(R.id.doc_type);
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            mDialog = new Dialog(this);

            mAttach.setOnClickListener(v -> {
                try {
                    //check for file system permissions
                    if (ContextCompat.checkSelfPermission(ApplyActivityActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {

                        File root1 = Environment.getExternalStorageDirectory();
                        File base_dir = new File(root1.getAbsolutePath() + getString(R.string.base_dir));
                        if (!base_dir.exists()) {
                            base_dir.mkdirs();
                        }

                        long type = mDocType.getSelectedItemId();
                        if (type == 0) {
                            mDocuType = "cl";
                        } else if (type == 1) {
                            mDocuType = "ml";
                        } else if (type == 2) {
                            mDocuType = "cv";
                        }
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        String[] mime_types = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword", "application/pdf"};
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mime_types);
                        startActivityForResult(intent, FILE_SYSTEM);
                    } else {
                        //ask for it
                        askForPermission();
                    }
                } catch (Exception e) {
                    Toast.makeText(ApplyActivityActivity.this, "Error raising event.", Toast.LENGTH_SHORT).show();
                }
            });
            mUpload.setOnClickListener(v -> {
                try {
                    if (mDocuType.equalsIgnoreCase("") || mDocuType == null) {
                        methods.showAlert("Missing fields", "Enter all information.", ApplyActivityActivity.this);
                        return;
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
                    String acctype = sharedPreferences.getString(RegistrationActivity.AccountType, "none");
                    if (acctype.equalsIgnoreCase("admin")) {
                        methods.showAlert("Admin account", "You are an admin.", ApplyActivityActivity.this);
                    } else {
                        if (sharedPreferences.contains(RegistrationActivity.UserId)) {
                            int mUserId = sharedPreferences.getInt(RegistrationActivity.UserId, 0);
                            if (mUserId == 0) {
                                methods.showAlert("Sign In required", "You need to sign in to be able to apply.", ApplyActivityActivity.this);
                            } else {
                                postApplicationFile(mOpportunityID, String.valueOf(mUserId), mDocuType, mFilepathUri, file);
                            }
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(ApplyActivityActivity.this, "Error raising event." + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void askForPermission() {
        //get user permission to read/write file system
        ActivityCompat.requestPermissions(ApplyActivityActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_SYSTEM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == FILE_SYSTEM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                File root = Environment.getExternalStorageDirectory();
                File base_dir = new File(root.getAbsolutePath() + getString(R.string.base_dir));
                if (!base_dir.exists()) {
                    base_dir.mkdirs();
                }
                long type = mDocType.getSelectedItemId();
                if (type == 0) {
                    mDocuType = "cl";
                } else if (type == 1) {
                    mDocuType = "ml";
                } else if (type == 2) {
                    mDocuType = "cv";
                }
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                String[] mime_types = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword", "application/pdf"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mime_types);
                startActivityForResult(intent, FILE_SYSTEM);
            } else {
                Toast.makeText(this, "You don't have permission to access file system !", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FILE_SYSTEM && resultCode == RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                mFilepathUri = data.getData();
                //Toast.makeText(getContext(), mFilepathUri.toString(), Toast.LENGTH_LONG).show();
                String subdir = getResources().getString(R.string.temp);
                file = convertUriToFile(mFilepathUri, subdir);

                String filename = file.toString().substring(file.toString().lastIndexOf("/") + 1);
                Toast.makeText(this, filename, Toast.LENGTH_LONG).show();

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

    /*
     * method for posting material
     */
    private void postApplicationFile(String postId, String userid, String doctype, Uri uri, File file) {
        try {
            //include the userid of admin posting the materiAL
            RequestBody pid = RequestBody.create(MultipartBody.FORM, postId);
            RequestBody uid = RequestBody.create(MultipartBody.FORM, userid);
            RequestBody doc = RequestBody.create(MultipartBody.FORM, doctype);

            RequestBody the_file = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(uri)),
                    file
            );

            MultipartBody.Part actual = MultipartBody.Part.createFormData("file", file.getName(), the_file);

            Call<ResponseBody> upload = apiInterface.ApplyOpportunity(pid, uid, doc, actual);
            methods.showDialog(mDialog, "Uploading document...", true);
            upload.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Upload failed.Try again.", ApplyActivityActivity.this);
                        } else if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Upload successful.", ApplyActivityActivity.this);

                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", ApplyActivityActivity.this);
                        } else if (message.equalsIgnoreCase("Exist")) {
                            methods.showAlert("Response", "There is another opportunity with the same details.", ApplyActivityActivity.this);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ApplyActivityActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), ApplyActivityActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(ApplyActivityActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
