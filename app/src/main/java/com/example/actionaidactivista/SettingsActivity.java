package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.adapters.SettingsAdpter;
import com.example.actionaidactivista.models.settings;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    RecyclerView settingsRecycleView;
    SettingsAdpter settingsAdapter;
    private ArrayList<settings> settingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //inflate UI
            setContentView(R.layout.activity_settings);
            //set title
            getSupportActionBar().setTitle("Settings");

            settingsRecycleView = (RecyclerView) findViewById(R.id.settings_recycler_view);
            settingsRecycleView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);
            settingsRecycleView.setLayoutManager(linearLayoutManager);

            settingsList();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    /* populate settings adapter */
    private void settingsList() {
        try {
            /* profile row item */
            settings profile = new settings();
            profile.setTitle("Account");
            profile.setSubtitle("Profile pic,info...");
            profile.setIcon(R.drawable.ic_account_circle);

            /* Change log row item
            settings changelog = new settings();
            changelog.setTitle("Change Log");
            changelog.setSubtitle("See what has changed");
            changelog.setIcon(R.drawable.ic_track_changes_black_24dp);
            */

            /* Update center row item
            settings updatec = new settings();
            updatec.setTitle("Update Center");
            updatec.setSubtitle("Check for app update");
            updatec.setIcon(R.drawable.ic_system_update_black_24dp);
            */

            /* About app row item */
            settings about_app = new settings();
            about_app.setTitle("About App");
            about_app.setSubtitle("See app version and info...");
            about_app.setIcon(R.drawable.ic_phone_android_black_24dp);

            /* About app row item */
            settings user_accounts = new settings();
            user_accounts.setTitle("User Accounts");
            user_accounts.setSubtitle("Activate or deactivate user accounts");
            user_accounts.setIcon(R.drawable.ic_account_circle);

            /* About app row item
            settings logout = new settings();
            logout.setTitle("Logout");
            logout.setSubtitle("Logout from this device...");
            logout.setIcon(R.drawable.ic_keyboard_backspace);
            */

            //add to list

            settingsList.add(profile);
            /*
             *settingsList.add(changelog);
             */
            /*
            settingsList.add(updatec);
            */
            //user account management is for admins only
            if(methods.isAdmin(this)) {
                settingsList.add(user_accounts);
            }
            settingsList.add(about_app);
            /*
             *settingsList.add(logout);
             */

            settingsAdapter = new SettingsAdpter(this, settingsList);
            settingsRecycleView.setAdapter(settingsAdapter);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading settings list", Toast.LENGTH_SHORT).show();
        }
    }
}
