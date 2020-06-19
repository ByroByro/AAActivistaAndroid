package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.example.actionaidactivista.ApplyActivityActivity;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.opportunity;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                            || (item.getmLocation() != null && item.getmLocation().toLowerCase().contains(constraint)|| (item.getmDateposted() != null && item.getmDateposted().toLowerCase().contains(constraint))))) {
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
//            String mDes = "";
//            if (chance.getmDescription().length() > 25) {
//                char[] message = chance.getmDescription().toCharArray();
//                int x = 0;
//                while (x < 25) {
//                    stringBuffer.append(message[x]);
//                    x++;
//                }
//                stringBuffer.append("...");
//                mDes = stringBuffer.toString();
//            }else {
//                holder.mDes.setText(chance.getmDescription());
//            }
            holder.mDes.setText("Description : " + chance.getmDescription());
            try {
                if (methods.compareCurrentDateAndDbDate(methods.getDatev2(), chance.getmClosingdate())) {
                    holder.mApply.setVisibility(View.INVISIBLE);
                }
            }catch (Exception e){
                System.out.println(e);
            }
            holder.mDetails.setOnClickListener(v -> {
                try {
                    StringBuffer stringBuffer1 = new StringBuffer();
                    stringBuffer1.append("Title : " + chance.getmTitle());
                    stringBuffer1.append("\nDescription : " + chance.getmDescription());
                    stringBuffer1.append("\nPosted on : " + chance.getmDateposted());
                    stringBuffer1.append("\nClosing date : " + chance.getmClosingdate());
                    stringBuffer1.append("\nLocation : " + chance.getmLocation());
                    methods.showAlert("Opportunity Details", stringBuffer1.toString(), mContext);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                }
            });
            holder.mApply.setOnClickListener(v -> {
                try {
                    Intent apply = new Intent(mContext, ApplyActivityActivity.class);
                    apply.putExtra("opportunity_id", chance.getmID());
                    apply.putExtra("description", chance.getmTitle());
                    mContext.startActivity(apply);
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
                                    deleteOpportunity(chance.getmID());
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mDes = itemView.findViewById(R.id.description);
            mApply = itemView.findViewById(R.id.apply);
            mDetails = itemView.findViewById(R.id.opportunity_details);
            mCard = itemView.findViewById(R.id.opportunity_card_view);
        }
    }

    /*
     * performs deletion of opportunity
     */
    private void deleteOpportunity(String oppId) {
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
