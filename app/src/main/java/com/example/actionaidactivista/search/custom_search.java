package com.example.actionaidactivista.search;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class custom_search {

    private Context ctx;
    private String toSearch;
    private String mOperation;
    private List<search_model> list;//list of type model
    public ArrayList<String> mParticipants;//list for participants i.e for activity upload
    private search_adapter mAdapter;//search adapter

    public custom_search(String what_to_search, String operation, Context context) {
        this.ctx = context;
        this.toSearch = what_to_search;
        this.mOperation = operation;
    }

    public custom_search() {

    }

    public void showDialog(Dialog mDialog) {
        try {
            //show dialog
            mDialog.setContentView(R.layout.custom_search_dialog);

            //initialise widgets
            TextView mTip = mDialog.findViewById(R.id.search_tip);
            //set text on tip
            if (toSearch.equalsIgnoreCase("Users")) {
                mTip.setText("Search Query = Name or Surname");
            }
            TextInputEditText mSearchQuery = mDialog.findViewById(R.id.first_param);
            Button mSearch = mDialog.findViewById(R.id.search);
            Button mOk = mDialog.findViewById(R.id.ok);
            RecyclerView mSearchedResult = mDialog.findViewById(R.id.search_list_recycler);
            //deny cancelling when user touches outside
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //mDialog.dismiss();
                }
                return true;
            });

            mOk.setOnClickListener(v -> {
                try {
                    mDialog.dismiss();
                    mParticipants = mAdapter.getParticipants();
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            //set on search button click listener
            mSearch.setOnClickListener(v -> {
                try {
                    String queryString = mSearchQuery.getText().toString().trim();
                    if (queryString.equalsIgnoreCase("")) {
                        Toast.makeText(ctx, "Enter query string.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(ctx, "Yep = " + queryString, Toast.LENGTH_SHORT).show();
                    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx);
                    mSearchedResult.setLayoutManager(linearLayoutManager);
                    mSearchedResult.setItemAnimator(new DefaultItemAnimator());

                    list = new ArrayList<>();
                    search_model item;
                    for (int i = 1; i <= 10; i++) {
                        item = new search_model();
                        item.setId(String.valueOf(i));
                        item.setDisplayName("This is result " + String.valueOf(i));
                        list.add(item);
                    }
                    mAdapter = new search_adapter(list, toSearch, mOperation, ctx);
                    mSearchedResult.setAdapter(mAdapter);
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error raising search.", Toast.LENGTH_SHORT).show();
                }
            });

            //show the dialog
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.show();
        } catch (Exception e) {
            Toast.makeText(ctx, "Error loading dialog.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * this methods sets the tagged participants of an activity
     */
    public void setParticipants(ArrayList<String> list) {
        this.mParticipants = new ArrayList<>(list);
    }

    /*
     * this method returns the participants tagged for an activity
     */
    public ArrayList<String> getmParticipants() {
        ArrayList<String> lst = new ArrayList<>();
        try {
            lst = mParticipants;
        } catch (Exception e) {
            System.out.println(e);
        }
        return lst;
    }
}
