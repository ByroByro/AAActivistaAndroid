package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.ApplyOpportunityActivity;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.RegistrationActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.opportunity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class opportunity_adapter extends RecyclerView.Adapter<opportunity_adapter.ViewHolder> {

    private Context mContext;
    private List<opportunity> mOpportunities;
    private List<opportunity> filtered_Opps;
    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;

    public opportunity_adapter(List<opportunity> list, Context ctx) {
        this.mOpportunities = list;
        this.mContext = ctx;
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
        filtered_Opps = new ArrayList<>(list);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<opportunity> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_Opps);
            } else {
                for (opportunity item : filtered_Opps) {
                    if ((item.getmTitle() != null && item.getmTitle().toLowerCase().contains(constraint)) || (item.getmDescription() != null && item.getmDescription().toLowerCase().contains(constraint)
                            || (item.getmLocation() != null && item.getmLocation().toLowerCase().contains(constraint) || (item.getmDateposted() != null && item.getmDateposted().toLowerCase().contains(constraint))))) {
                        filtered_items.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered_items;

            return filterResults;
        }

        //run on UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mOpportunities.clear();
            mOpportunities.addAll((Collection<? extends opportunity>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.opportunity_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new opportunity_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            opportunity chance = mOpportunities.get(position);
            holder.mTitle.setText("Title : " + chance.getmTitle());
            StringBuffer stringBuffer = new StringBuffer();
            holder.mDes.setText("Description : " + chance.getmDescription());
            try {
                if (methods.compareCurrentDateAndDbDate(methods.getDatev2(), chance.getmClosingdate())) {
                    holder.mApply.setVisibility(View.GONE);
                    holder.mExpired.setVisibility(View.VISIBLE);
                } else {
                    holder.mApply.setVisibility(View.VISIBLE);
                    holder.mExpired.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            if (chance.getmDocsLink().equalsIgnoreCase("N/A")) {
                holder.mDocsLink.setVisibility(View.GONE);
            } else {
                holder.mDocsLink.setVisibility(View.VISIBLE);
                holder.mDocsLink.setText(chance.getmDocsLink());
                holder.mDocsLink.setPaintFlags(holder.mDocsLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
            holder.mDetails.setOnClickListener(v -> {
                try {
                    StringBuffer stringBuffer1 = new StringBuffer();
                    stringBuffer1.append("Title : " + chance.getmTitle());
                    stringBuffer1.append("\nDescription : " + chance.getmDescription());
                    stringBuffer1.append("\nPosted on : " + methods.getReadableDate(chance.getmDateposted(), mContext));
                    stringBuffer1.append("\nClosing date : " + methods.getReadableDate(chance.getmClosingdate(), mContext));
                    stringBuffer1.append("\nLocation : " + chance.getmLocation());
                    methods.showAlert("Opportunity Details", stringBuffer1.toString(), mContext);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                }
            });
            holder.mApply.setOnClickListener(v -> {
                try {
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
                    String acctype = sharedPreferences.getString(RegistrationActivity.AccountType, "none");
                    if (acctype.equalsIgnoreCase("admin")) {
                        methods.showAlert("Admin account", "You are an admin.", mContext);
                    } else {
                        if (sharedPreferences.contains(RegistrationActivity.UserId)) {
                            int mUserId = sharedPreferences.getInt(RegistrationActivity.UserId, 0);
                            if (mUserId == 0) {
                                methods.showAlert("Sign In required", "You need to sign in to be able to apply.", mContext);
                            } else {
                                Intent apply = new Intent(mContext, ApplyOpportunityActivity.class);
                                apply.putExtra("opportunity_id", chance.getmID());
                                apply.putExtra("description", chance.getmTitle());
                                mContext.startActivity(apply);
                            }
                        } else {
                            methods.showAlert("Sign In required", "You need to sign in to be able to apply.", mContext);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error raising apply event.", Toast.LENGTH_SHORT).show();
                }
            });
            holder.mCard.setOnClickListener(v -> {
                try {
                    //Creating the instance of PopupMenu
                    final PopupMenu popup = new PopupMenu(mContext, holder.mCard, Gravity.CENTER);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.opportunity_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {

                        if (item.getItemId() == R.id.action_delete) {
                            /*
                             * prompt if user is sure to delete if yes then delete,
                             * else do not delete
                             */
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Confirm action");
                            builder.setMessage("Are you sure to delete this opportunity.Note : This cannot be undone.");
                            //set dialog to be cancelled only by buttons
                            builder.setCancelable(false);

                            //set dismiss button
                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                            //set positive button
                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                try {
                                    deleteOpportunity(chance.getmID(), position);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        return false;
                    });
                    //check if its an admin
                    if (methods.isAdmin(mContext)) {
                        popup.show();
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error raising apply event.", Toast.LENGTH_SHORT).show();
                }
            });
            holder.mDocsLink.setOnClickListener(v -> {
                try{
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
                    String acctype = sharedPreferences.getString(RegistrationActivity.AccountType, "none");
                    if (acctype.equalsIgnoreCase("admin")) {
                        methods.showAlert("Admin account", "You are an admin.", mContext);
                    } else {
                        if (sharedPreferences.contains(RegistrationActivity.UserId)) {
                            int mUserId = sharedPreferences.getInt(RegistrationActivity.UserId, 0);
                            if (mUserId == 0) {
                                methods.showAlert("Sign In required", "You need to sign in to be able to apply.", mContext);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(chance.getmDocsLink()));
                                // Always use string resources for UI text.
                                String title = mContext.getString(R.string.browser_intent);
                                // Create and start the chooser
                                Intent chooser = Intent.createChooser(intent, title);
                                mContext.startActivity(chooser);
                            }
                        } else {
                            methods.showAlert("Sign In required", "You need to sign in to be able to apply.", mContext);
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(mContext,e.toString(),Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error binding view holder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mOpportunities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitle;
        public TextView mDes;
        public Button mApply;
        public Button mDetails;
        public CardView mCard;
        public Button mExpired;
        public TextView mDocsLink;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mDes = itemView.findViewById(R.id.description);
            mApply = itemView.findViewById(R.id.apply);
            mExpired = itemView.findViewById(R.id.expired);
            mDocsLink = itemView.findViewById(R.id.go_to_google_docs);
            mDetails = itemView.findViewById(R.id.opportunity_details);
            mCard = itemView.findViewById(R.id.opportunity_card_view);
        }
    }

    /*
     * performs deletion of opportunity
     */
    private void deleteOpportunity(String oppId, int position) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, oppId);
            //RequestBody op = RequestBody.create(MultipartBody.FORM, action);
            Call<ResponseBody> approve = apiInterface.DeleteOpportunity(id);
            methods.showDialog(mDialog, "Processing request operation...", true);
            approve.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            //JsonParser parser = new JsonParser();
                            //String result = parser.parse(responseData).getAsString();
                            String result = methods.removeQoutes(responseData);
                            if (result.equalsIgnoreCase("Success")) {
                                methods.showAlert("Result", "Operation success.", mContext);
                                notifyItemRemoved(position);
                            } else if (result.equalsIgnoreCase("Failed")) {
                                methods.showAlert("Result", "Operation failed.Try later.", mContext);
                            } else if (result.equalsIgnoreCase("Error")) {
                                methods.showAlert("Result", "Server error.", mContext);
                            } else if (result.equalsIgnoreCase("Already approved")) {
                                methods.showAlert("Result", "The user application is already approved.", mContext);
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
                    methods.showAlert("List onFailure", t.toString(), mContext);
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
