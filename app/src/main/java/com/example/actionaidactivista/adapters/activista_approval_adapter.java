package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.contact;
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

public class activista_approval_adapter extends RecyclerView.Adapter<activista_approval_adapter.ViewHolder> {

    private Context mContext;
    private List<contact> mContacts;
    private List<contact> filtered_Contacts;
    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;
    private Dialog dialog;

    public activista_approval_adapter(List<contact> list, Context ctx) {
        this.mContacts = list;
        this.mContext = ctx;
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
        dialog = new Dialog(mContext);
        this.filtered_Contacts = new ArrayList<>(list);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<contact> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_Contacts);
            } else {
                for (contact item : filtered_Contacts) {
                    if ((item.getmName() != null && item.getmName().toLowerCase().contains(constraint)) || (item.getmSurname() != null && item.getmSurname().toLowerCase().contains(constraint)
                            || (item.getmStatus() != null && item.getmStatus().toLowerCase().contains(constraint)))) {
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
            mContacts.clear();
            mContacts.addAll((Collection<? extends contact>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.approval_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new activista_approval_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            contact contact = mContacts.get(position);
            holder.mName.setText("Name : " + contact.getmName() + " " + contact.getmSurname());
            holder.mPhone.setText("Occupation : " + contact.getmOccupation());
            holder.mStatus.setText("Approved : " + contact.getmStatus());
            if (!contact.getmProfileUrl().equalsIgnoreCase("N/A")) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                requestOptions.placeholder(R.drawable.ic_account_circle);
                requestOptions.error(R.drawable.ic_account_circle);

                Glide.with(mContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(contact.getmProfileUrl())
                        .into(holder.mProfile);
            } else {
                holder.mProfile.setImageResource(R.drawable.ic_account_circle);
            }
            holder.mMore.setOnClickListener(v -> {
                try {

                    //Creating the instance of PopupMenu
                    final PopupMenu popup = new PopupMenu(mContext, holder.mMore, Gravity.CENTER);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.approval_menu, popup.getMenu());

                    //disable approve,disapprove,delete for non-admins
                    Menu menu = popup.getMenu();
                    MenuItem approve = menu.findItem(R.id.action_approve);
                    MenuItem disapprove = menu.findItem(R.id.action_disapprove);
                    MenuItem delete = menu.findItem(R.id.action_delete);

                    disapprove.setVisible(false);

                    popup.setOnMenuItemClickListener(item -> {
                        try {
                            if (item.getItemId() == R.id.action_details) {
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Name : " + contact.getmName());
                                stringBuffer.append("\nSurname : " + contact.getmSurname());
                                stringBuffer.append("\nGender : " + contact.getmGender());
                                if (contact.getmDobPublic().equalsIgnoreCase("True")) {
                                    stringBuffer.append("\nDate of Birth : " + methods.getReadableDate(contact.getmDob(), mContext));
                                    stringBuffer.append("\nAge : " + methods.getAge(contact.getmDob(), mContext));
                                } else {
                                    stringBuffer.append("\nAge : " + methods.getAge(contact.getmDob(), mContext));
                                }
                                stringBuffer.append("\nOccupation : " + contact.getmOccupation());
                                stringBuffer.append("\nPhone No : " + contact.getmPhone());
                                stringBuffer.append("\nApproval status(IsApproved) : " + contact.getmStatus());
                                stringBuffer.append("\nBiography : " + contact.getmBio());
                                methods.showAlert("\nMember Details", stringBuffer.toString(), mContext);
                            } else if (item.getItemId() == R.id.action_approve) {
                                /*
                                 * prompt if user is sure to approve if yes then approve,
                                 * else do not approve,
                                 * also check approval status of user if already approved
                                 * then no need
                                 */
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm action");
                                builder.setMessage("Are you sure to approve this user.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        if (contact.getmStatus().equalsIgnoreCase("Yes")) {
                                            methods.showAlert("Already approved", "The selected user is already approved.", mContext);
                                        } else {
                                            actionOnApplication(contact.getmContactID(), "approve", position);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (item.getItemId() == R.id.action_disapprove) {
                                /*
                                 * prompt if user is sure to approve if yes then approve,
                                 * else do not approve,
                                 * also check approval status of user if already approved
                                 * then no need
                                 */
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm action");
                                builder.setMessage("Are you sure to disapprove this user.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        if (contact.getmStatus().equalsIgnoreCase("No")) {
                                            methods.showAlert("Already disapproved", "The selected user is already disapproved.", mContext);
                                        } else {
                                            actionOnApplication(contact.getmContactID(), "disapprove", position);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (item.getItemId() == R.id.action_delete) {
                                /*
                                 * prompt if user is sure to delete if yes then delete,
                                 * else do not delete
                                 */
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm action");
                                builder.setMessage("Are you sure to delete this user application.Note : This cannot be undone.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        actionOnApplication(contact.getmContactID(), "delete", position);
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (item.getItemId() == R.id.action_identity_verification) {
                                if(!contact.getmDoc_type().equalsIgnoreCase("n/a")) {
                                    if (contact.getmDoc_type().equalsIgnoreCase("image")) {
                                        dialog.setContentView(R.layout.change_profile_dialog);
                                        ImageView imageView = dialog.findViewById(R.id.fullImage);
                                        Button cancel = dialog.findViewById(R.id.cancel);
                                        Button ok = dialog.findViewById(R.id.ok);
                                        cancel.setVisibility(View.GONE);
                                        ok.setText("Dismiss");
                                        //dialog.setCanceledOnTouchOutside(false);
                                        dialog.setOnKeyListener((dialog, keyCode, event) -> {
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                dialog.dismiss();
                                            }
                                            return true;
                                        });

                                        RequestOptions requestOptions = new RequestOptions();
                                        requestOptions.centerCrop();
                                        requestOptions.placeholder(R.drawable.ic_account_circle);
                                        requestOptions.error(R.drawable.ic_account_circle);

                                        Glide.with(mContext)
                                                .applyDefaultRequestOptions(requestOptions)
                                                .load(contact.getmDoc_url())
                                                .into(imageView);

                                        ok.setOnClickListener(v2 -> {
                                            dialog.dismiss();
                                        });

                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialog.show();
                                    } else {
                                        Intent open = new Intent(Intent.ACTION_VIEW);
                                        open.setData(Uri.parse(contact.getmDoc_url()));
                                        mContext.startActivity(open);
                                    }
                                }else {
                                    Toast.makeText(mContext, "No doc.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Error loading popup.", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    });

                    popup.show();

                } catch (Exception e) {
                    Toast.makeText(mContext, "Error raising download event.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error binding view holder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public TextView mPhone;
        public CardView mCardView;
        public ImageView mProfile;
        public TextView mStatus;
        public ImageButton mMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            mPhone = itemView.findViewById(R.id.phone);
            mProfile = itemView.findViewById(R.id.imageView);
            mCardView = itemView.findViewById(R.id.contact_card_view);
            mStatus = itemView.findViewById(R.id.status);
            mMore = itemView.findViewById(R.id.more);
        }
    }

    //add more members
    public void addMember(List<contact> list) {
        try {
            for (contact m : list) {
                mContacts.add(m);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(mContext, "Add feed error in adapter", Toast.LENGTH_SHORT).show();
        }
    }

    //delete,approve etc
    private void actionOnApplication(String userId, String action, int position) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, userId);
            RequestBody op = RequestBody.create(MultipartBody.FORM, action);
            Call<ResponseBody> call = apiInterface.ApplicationActions(id, op);
            methods.showDialog(mDialog, "Processing request operation...", true);
            call.enqueue(new Callback<ResponseBody>() {
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
                                if (action.equalsIgnoreCase("approve")) {
                                    contact contact = mContacts.get(position);
                                    contact.setmStatus("Yes");
                                    notifyItemChanged(position);
                                } else if (action.equalsIgnoreCase("disapprove")) {
                                    contact contact = mContacts.get(position);
                                    contact.setmStatus("No");
                                    notifyItemChanged(position);
                                } else if (action.equalsIgnoreCase("delete")) {
                                    notifyItemRemoved(position);
                                }
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
            Toast.makeText(mContext, "Error processing action event.", Toast.LENGTH_SHORT).show();
        }
    }
}
