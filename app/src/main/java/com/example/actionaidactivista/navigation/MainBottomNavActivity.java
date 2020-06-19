package com.example.actionaidactivista.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.example.actionaidactivista.ActivistaApprovalFragment;
import com.example.actionaidactivista.ActivityUploadActivity;
import com.example.actionaidactivista.ContactFragment;
import com.example.actionaidactivista.ContentModerationFragment;
import com.example.actionaidactivista.FeedFragment;
import com.example.actionaidactivista.GeoLocationActivity;
import com.example.actionaidactivista.LibraryFragment;
import com.example.actionaidactivista.OpportunitiesFragment;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.RegistrationActivity;
import com.example.actionaidactivista.SettingsActivity;
import com.example.actionaidactivista.UploadLibraryMaterialActivity;
import com.example.actionaidactivista.UploadOpportunityFragment;
import com.example.actionaidactivista.loginandsignup.AdminLoginActivity;
import com.example.actionaidactivista.loginandsignup.UserLoginAndSignUpActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainBottomNavActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private FrameLayout mainframe;
    private FeedFragment feedFragment;
    private ActivityUploadActivity activityUploadActivity;
    private OpportunitiesFragment opportunitiesFragment;
    private LibraryFragment libraryFragment;
    private ContactFragment contactFragment;
    private UploadOpportunityFragment uploadOpportunityFragment;
    private ActivistaApprovalFragment activistaApprovalFragment;
    SharedPreferences sharedPreferences;
    private ContentModerationFragment contentModerationFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_feed:
                    setFragment(feedFragment);
                    return true;
                case R.id.navigation_library:
                    setFragment(libraryFragment);
                    return true;
                case R.id.navigation_contact:
                    setFragment(contactFragment);
                    return true;
                case R.id.navigation_upload:
                    setFragment(activityUploadActivity);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        libraryFragment = new LibraryFragment();
        contactFragment = new ContactFragment();
        uploadOpportunityFragment = new UploadOpportunityFragment();
        activistaApprovalFragment = new ActivistaApprovalFragment();
        contentModerationFragment = new ContentModerationFragment();
        setFragment(feedFragment);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fg = getSupportFragmentManager().beginTransaction();
        fg.replace(R.id.main_frame, fragment);
        fg.commit();
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
            case R.id.action_register:
                Intent reg = new Intent(MainBottomNavActivity.this, RegistrationActivity.class);
                startActivity(reg);
                break;
            case R.id.action_opportunities:
                setFragment(opportunitiesFragment);
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
                setFragment(contentModerationFragment);
                break;
            case R.id.action_subscriber_monitor:
                Intent intent1 = new Intent(MainBottomNavActivity.this, GeoLocationActivity.class);
                startActivity(intent1);
                break;
            case R.id.action_view_opportunity_applications:
                break;
            case R.id.action_social_media:
                List<String> sites = new ArrayList<>();
                sites.add("Facebook");
                sites.add("Twitter");
                sites.add("Instagram");
                String[] listItems = new String[sites.size()];
                for (int j = 0; j < sites.size(); j++) {
                    listItems[j] = sites.get(j);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Platform");
                //builder.setIcon(R.drawable.ic_account_circle);
                builder.setSingleChoiceItems(listItems, -1, (dialog, which) -> {
                    String site = listItems[which];
                    String link = "";
                    if (site.equalsIgnoreCase("Facebook")) {
                        link = this.getString(R.string.facebook_link);
                    } else if (site.equalsIgnoreCase("Twitter")) {
                        link = this.getString(R.string.twitter_link);
                    } else if (site.equalsIgnoreCase("Instagram")) {
                        link = this.getString(R.string.instagram_link);
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {

            MenuItem library = menu.findItem(R.id.action_upload_library_material);
            MenuItem approval = menu.findItem(R.id.action_application_approval);
            MenuItem moderation = menu.findItem(R.id.action_moderate_content);
            MenuItem uploadOppo = menu.findItem(R.id.action_upload_opportunity);
            MenuItem monitor = menu.findItem(R.id.action_subscriber_monitor);
            MenuItem apps = menu.findItem(R.id.action_view_opportunity_applications);
            //check preferences
            if(sharedPreferences.contains(RegistrationActivity.IsLogged)){
                //check value
                boolean isLogged = sharedPreferences.getBoolean(RegistrationActivity.IsLogged,false);
                //check if user is logged
                if(isLogged){
                    //get user account type
                    String accType = sharedPreferences.getString(RegistrationActivity.AccountType,"none");
                    if(accType.equalsIgnoreCase("none") || accType.equalsIgnoreCase("user")){
                        //do not show admin options
                    }else if(accType.equalsIgnoreCase("admin")){
                        //show admin options
                        library.setVisible(true);
                        approval.setVisible(true);
                        moderation.setVisible(true);
                        uploadOppo.setVisible(true);
                        monitor.setVisible(true);
                        apps.setVisible(true);
                    }
                }else {
                    //do nothing
                }
            }else {
                //do nothing the user is not logged
            }
        }catch (Exception e){
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
            Toast.makeText(this, "Error onBackPressed.", Toast.LENGTH_SHORT).show();
        }
    }
}
