package com.example.actionaidactivista.navigation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.actionaidactivista.ActivistaApprovalFragment;
import com.example.actionaidactivista.ActivityUploadActivity;
import com.example.actionaidactivista.AlumniFragment;
import com.example.actionaidactivista.ContactFragment;
import com.example.actionaidactivista.ContentModerationFragment;
import com.example.actionaidactivista.FeedFragment;
import com.example.actionaidactivista.FeedsMonitoringActivity;
import com.example.actionaidactivista.GeoLocationActivity;
import com.example.actionaidactivista.LibraryFragment;
import com.example.actionaidactivista.OpportunitiesFragment;
import com.example.actionaidactivista.OpportunityApplicationsActivity;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.RegistrationActivity;
import com.example.actionaidactivista.ReportedCommentsActivity;
import com.example.actionaidactivista.SettingsActivity;
import com.example.actionaidactivista.UploadLibraryMaterialActivity;
import com.example.actionaidactivista.UploadOpportunityFragment;
import com.example.actionaidactivista.loginandsignup.AdminLoginActivity;
import com.example.actionaidactivista.loginandsignup.UserLoginAndSignUpActivity;
import com.example.actionaidactivista.methods;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainBottomNavActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private FrameLayout mainframe;
    private final int FILE_SYSTEM = 1000;
    private FeedFragment feedFragment;
    private ActivityUploadActivity activityUploadActivity;
    private OpportunitiesFragment opportunitiesFragment;
    private ContactFragment contactFragment;
    private UploadOpportunityFragment uploadOpportunityFragment;
    private ActivistaApprovalFragment activistaApprovalFragment;
    private AlumniFragment alumniFragment;
    SharedPreferences sharedPreferences;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_feed:
                    setFragment(feedFragment);
                    return true;
                case R.id.action_social_media:
                    List<String> sites = new ArrayList<>();
                    sites.add("Facebook");
                    sites.add("Twitter");
                    //sites.add("Instagram");
                    String[] listItems = new String[sites.size()];
                    for (int j = 0; j < sites.size(); j++) {
                        listItems[j] = sites.get(j);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainBottomNavActivity.this);
                    builder.setTitle("Select Platform");
                    //builder.setIcon(R.drawable.ic_account_circle);
                    builder.setSingleChoiceItems(listItems, -1, (dialog, which) -> {
                        String site = listItems[which];
                        String link = "";
                        if (site.equalsIgnoreCase("Facebook")) {
                            link = getFaceBookLink();
                        } else if (site.equalsIgnoreCase("Twitter")) {
                            link = getTwitterLink();
                        } else if (site.equalsIgnoreCase("Instagram")) {
                            //link = this.getString(R.string.instagram_link);
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(link));

                        // Always use string resources for UI text.
                        String title = getString(R.string.browser_intent);
                        // Create and start the chooser
                        Intent chooser = Intent.createChooser(intent, title);
                        startActivity(chooser);
                        dialog.dismiss();
                    });
                    builder.setCancelable(false);
                    //set dismiss button
                    builder.setNegativeButton("Dismiss", (dialog, which) -> {
                        dialog.dismiss();
                    });

                    //show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                case R.id.action_opportunities:
                    setFragment(opportunitiesFragment);
                    return true;
                case R.id.action_register:
                    Intent reg = new Intent(MainBottomNavActivity.this, RegistrationActivity.class);
                    reg.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(reg);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main_bottom_nav);
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            mainframe = (FrameLayout) findViewById(R.id.main_frame);
            //instantiate account preferences
            //this will create the file if not existing
            sharedPreferences = getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            feedFragment = new FeedFragment();
            activityUploadActivity = new ActivityUploadActivity();
            opportunitiesFragment = new OpportunitiesFragment();
            contactFragment = new ContactFragment();
            uploadOpportunityFragment = new UploadOpportunityFragment();
            activistaApprovalFragment = new ActivistaApprovalFragment();
            alumniFragment = new AlumniFragment();
            setFragment(feedFragment);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading MainBottomNav.", Toast.LENGTH_SHORT).show();
        }

        //ask for file permissions
        try {
            //get user permission to read/write file system
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_SYSTEM);


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void setFragment(Fragment fragment) {
        try {
            FragmentTransaction fg = getSupportFragmentManager().beginTransaction();
            fg.replace(R.id.main_frame, fragment);
            fg.commit();
        } catch (Exception e) {
            Toast.makeText(this, "Error setting fragment.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == FILE_SYSTEM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //check if media is mounted and if there is enough space when creating app files
                File root = Environment.getExternalStorageDirectory();
                File base_dir = new File(root.getAbsolutePath() + getResources().getString(R.string.base_dir));
                if (!base_dir.exists()) {
                    base_dir.mkdirs();
                }
            } else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file system !", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();
        switch (i) {
            case R.id.action_upload:
                setFragment(activityUploadActivity);
                break;
            case R.id.action_members:
                setFragment(contactFragment);
                break;
            case R.id.action_upload_opportunity:
                setFragment(uploadOpportunityFragment);
                break;
            case R.id.action_upload_library_material:
                Intent upload_lib_material = new Intent(MainBottomNavActivity.this, UploadLibraryMaterialActivity.class);
                startActivity(upload_lib_material);
                break;
            case R.id.action_settings:
                Intent settings = new Intent(MainBottomNavActivity.this, SettingsActivity.class);
                settings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(settings);
                break;
            case R.id.action_admin_login:
                Intent admin_login = new Intent(MainBottomNavActivity.this, AdminLoginActivity.class);
                admin_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(admin_login);
                break;
            case R.id.action_user_login:
                Intent user_login = new Intent(MainBottomNavActivity.this, UserLoginAndSignUpActivity.class);
                user_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(user_login);
                break;
            case R.id.action_application_approval:
                setFragment(activistaApprovalFragment);
                break;
            case R.id.action_moderate_content:
                Intent cont_mode = new Intent(MainBottomNavActivity.this, ContentModerationFragment.class);
                startActivity(cont_mode);
                break;
            case R.id.action_subscriber_monitor:
                Intent intent1 = new Intent(MainBottomNavActivity.this, FeedsMonitoringActivity.class);
                startActivity(intent1);
                break;
            case R.id.action_view_opportunity_applications:
                Intent opp_apps = new Intent(MainBottomNavActivity.this, OpportunityApplicationsActivity.class);
                startActivity(opp_apps);
                break;
            case R.id.action_library:
                Intent library = new Intent(MainBottomNavActivity.this, LibraryFragment.class);
                startActivity(library);
                break;
            case R.id.action_alumni_members:
                setFragment(alumniFragment);
                break;
            case R.id.action_view_reported_comments:
                Intent intent = new Intent(this, ReportedCommentsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {

            MenuItem library_material = menu.findItem(R.id.action_upload_library_material);
            MenuItem approval = menu.findItem(R.id.action_application_approval);
            MenuItem moderation = menu.findItem(R.id.action_moderate_content);
            MenuItem upload_Oppo = menu.findItem(R.id.action_upload_opportunity);
            MenuItem monitor = menu.findItem(R.id.action_subscriber_monitor);
            MenuItem apps = menu.findItem(R.id.action_view_opportunity_applications);
            MenuItem register = menu.findItem(R.id.action_register);
            MenuItem library = menu.findItem(R.id.action_library);
            MenuItem members = menu.findItem(R.id.action_members);
            MenuItem activity_upload = menu.findItem(R.id.action_upload);
            MenuItem alumni_list = menu.findItem(R.id.action_alumni_members);
            MenuItem repo_comms = menu.findItem(R.id.action_view_reported_comments);
            //check preferences
            if (sharedPreferences.contains(RegistrationActivity.IsLogged)) {
                //check value
                boolean isLogged = sharedPreferences.getBoolean(RegistrationActivity.IsLogged, false);
                //check if user is logged
                if (isLogged) {
                    //get user account type
                    String accType = sharedPreferences.getString(RegistrationActivity.AccountType, "none");
                    if (accType.equalsIgnoreCase("none")) {
                        //do not show admin options
                    } else if (accType.equalsIgnoreCase("admin")) {
                        //show admin options
                        library_material.setVisible(true);
                        approval.setVisible(true);
                        moderation.setVisible(true);
                        upload_Oppo.setVisible(true);
                        monitor.setVisible(true);
                        apps.setVisible(true);
                        library.setVisible(true);
                        members.setVisible(true);
                        activity_upload.setVisible(true);
                        alumni_list.setVisible(true);
                        repo_comms.setVisible(true);
                    } else if (accType.equalsIgnoreCase("user")) {
                        //show user options
                        library.setVisible(true);
                        members.setVisible(true);
                        activity_upload.setVisible(true);
                        alumni_list.setVisible(true);
                    }

                    //remove the register option
                    register.setVisible(false);
                } else {
                    //set register option to invisible
                    register.setVisible(true);
                }
            } else {
                //do nothing the user is not logged
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /*
     * method for setting action bar title in fragments
     */
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /*
     * handle on back pressed events
     */

    @Override
    public void onBackPressed() {
        try {
            Fragment f = this.getSupportFragmentManager().findFragmentById(R.id.main_frame);

            if (f != null && f instanceof FeedFragment) {
                finishAffinity();
            } else {
                FragmentTransaction home = getSupportFragmentManager().beginTransaction();
                home.replace(R.id.main_frame, new FeedFragment());
                home.commit();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error onBackPressed." + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //get facebook link
    private String getFaceBookLink() {
        return this.getString(R.string.facebook_link);
    }

    //get twitter link
    private String getTwitterLink() {
        return this.getString(R.string.twitter_link);
    }
}
