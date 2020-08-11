package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actionaidactivista.BuildConfig;
import com.example.actionaidactivista.CheckAccountActivity;
import com.example.actionaidactivista.ProfileActivity;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.RegistrationActivity;
import com.example.actionaidactivista.UserAccountsActivity;
import com.example.actionaidactivista.database.prov_dis_helper;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.settings;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsAdpter extends RecyclerView.Adapter<SettingsAdpter.SettingsViewHolder> {

    private Context mContext;
    private ArrayList<settings> mList;
    private List<settings> filtered_Members;
    private ProgressDialog progressDialog;
    private Dialog mProgress;
    private Dialog mDialog;
    private com.example.actionaidactivista.database.prov_dis_helper prov_dis_helper;
    //retrofit
    private ApiInterface apiInterface;

    public SettingsAdpter(Context context, ArrayList<settings> list) {
        this.mContext = context;
        this.mList = list;
        filtered_Members = new ArrayList<>(list);
        progressDialog = new ProgressDialog(context);
        mProgress = new Dialog(context);
        mDialog = new Dialog(context);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        prov_dis_helper = new prov_dis_helper(context, "", null);
    }

//    public Filter getFilter() {
//        return filter;
//    }
//
//    Filter filter = new Filter() {
//
//        //run on a bg thread
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            List<member> filtered_items = new ArrayList<>();
//            if (constraint.toString().isEmpty()) {
//                filtered_items.addAll(filtered_Members);
//            } else {
//                for (member item : filtered_Members) {
//                    if ((item.getMemberno() != null && item.getMemberno().toLowerCase().contains(constraint)) || (item.getFirstname() != null && item.getFirstname().toLowerCase().contains(constraint)
//                            || (item.getSurname() != null && item.getSurname().toLowerCase().contains(constraint)))) {
//                        filtered_items.add(item);
//                    }
//                }
//            }
//
//            FilterResults filterResults = new FilterResults();
//            filterResults.values = filtered_items;
//
//            return filterResults;
//        }
//
//        //run on UI thread
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            mMembers.clear();
//            mMembers.addAll((Collection<? extends member>) results.values);
//            notifyDataSetChanged();
//        }
//    };

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_row_item, null);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        final settings setting = mList.get(position);
        holder.title.setText(setting.getTitle());
        holder.subtitle.setText(setting.getSubtitle());
        holder.icon.setImageResource(setting.getIcon());

        //try to set profile icon
        if(setting.getTitle().equalsIgnoreCase("Account")){
            Glide.with(mContext)
                    .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                    .load(methods.getUserProfile(mContext))
                    .into(holder.icon);
        }

        //click events for row item
        holder.settings_card_view.setOnClickListener(v -> {
            try {
                if (setting.getTitle().equalsIgnoreCase("Account")) {
                    //check if it is an admin
                    if (methods.isAdmin(mContext)) {
                        Toast.makeText(mContext, "This is for users.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //only go if a user is logged
                    if (methods.checkAccountIsLogged(mContext)) {
                        Intent profile = new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(profile);
                    } else {
                        Toast.makeText(mContext,"Login required",Toast.LENGTH_LONG).show();
                    }
                } else if (setting.getTitle().equalsIgnoreCase("Update Center")) {
                    Toast.makeText(mContext, "Up", Toast.LENGTH_SHORT).show();
                } else if (setting.getTitle().equalsIgnoreCase("About App")) {
                    StringBuffer stringBuffer = new StringBuffer();
                    String version = BuildConfig.VERSION_NAME;

                    Calendar now = Calendar.getInstance();
                    SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String dateNow = myFormat.format(now.getTime());
                    String[] dnow = dateNow.split("/");
                    int nowyear = Integer.valueOf(dnow[2]);

                    String year = String.valueOf(nowyear);

                    stringBuffer.append("Version : " + version);
                    stringBuffer.append("\n\n\nCopyright ActionAid " + year);

                    methods.showAlert("App Info", stringBuffer.toString(), mContext);
                } else if (setting.getTitle().equalsIgnoreCase("User Accounts")) {
                    Intent intent = new Intent(mContext, UserAccountsActivity.class);
                    mContext.startActivity(intent);
                } else if (setting.getTitle().equalsIgnoreCase("Sync Reg Data")) {
                    getProvinces();
                    getDistricts();
                } else if (setting.getTitle().equalsIgnoreCase("Check Acc Approval")) {
                    Intent intent = new Intent(mContext, CheckAccountActivity.class);
                    mContext.startActivity(intent);
                } else if (setting.getTitle().equalsIgnoreCase("Logout")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Sign out?");
                    builder.setMessage("Are you sure to sign out.This will clear all your data on this device.");
                    //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                    //set dialog to be cancelled only by buttons
                    builder.setCancelable(false);

                    //set dismiss button
                    builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                    //set positive button
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            logoutUser();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class SettingsViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subtitle;
        public ImageView icon;
        public CardView settings_card_view;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);
            subtitle = itemView.findViewById(R.id.des);
            icon = itemView.findViewById(R.id.imageView);
            settings_card_view = itemView.findViewById(R.id.settings_card_view);
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

                        } else {
                            Toast.makeText(mContext, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), mContext);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", t.toString(), mContext);
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
                            Toast.makeText(mContext, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), mContext);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", t.toString(), mContext);
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //logout user/admin
    private void logoutUser() {
        try {
            Call<ResponseBody> logout;
            String id = String.valueOf(methods.getUserId(mContext));
            RequestBody user_id = RequestBody.create(MultipartBody.FORM, id);
            if (methods.isAdmin(mContext)) {
                //Toast.makeText(mContext, "Admin operation no supported yet.", Toast.LENGTH_LONG).show();
                logout = apiInterface.LogoutAdmin(user_id);
            } else {
                logout = apiInterface.LogoutUser(user_id);
            }

            methods.showDialog(mDialog, "Signing out...", true);
            logout.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            String message = methods.removeQoutes(responseData);
                            if (message.equalsIgnoreCase("Success")) {
                                SharedPreferences sharedPreferences = mContext.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(RegistrationActivity.IsLogged, false);
                                editor.putString(RegistrationActivity.AccNo, "none");
                                editor.putInt(RegistrationActivity.UserId, 0);
                                editor.putString(RegistrationActivity.AccountType, "none");
                                editor.putString(RegistrationActivity.Level, "none");
                                editor.putString(RegistrationActivity.ProfileUrl, "none");
                                editor.apply();
                                Toast.makeText(mContext, "Sign out successful.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, "Sign out failed.Try later.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Request unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), mContext);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    methods.showAlert("Failure", t.toString(), mContext);
                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}

