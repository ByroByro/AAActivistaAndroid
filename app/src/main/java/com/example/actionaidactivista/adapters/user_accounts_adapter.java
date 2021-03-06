package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class user_accounts_adapter extends RecyclerView.Adapter<user_accounts_adapter.ViewHolder> {

    private Context mContext;
    private List<contact> mContacts;
    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;

    public user_accounts_adapter(List<contact> list, Context ctx) {
        this.mContacts = list;
        this.mContext = ctx;
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.approval_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new user_accounts_adapter.ViewHolder(view);
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
                requestOptions.placeholder(R.drawable.ic_contacts_red);
                requestOptions.error(R.drawable.ic_contacts_red);

                Glide.with(mContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(contact.getmProfileUrl())
                        .into(holder.mProfile);
            } else {
                holder.mProfile.setImageResource(R.drawable.ic_contacts_red);
            }
            holder.mMore.setOnClickListener(v -> {
                try {

                    //Creating the instance of PopupMenu
                    final PopupMenu popup = new PopupMenu(mContext, holder.mMore, Gravity.CENTER);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.activation_menu, popup.getMenu());

                    //disable approve,disapprove,delete for non-admins
//                    Menu menu = popup.getMenu();
//                    MenuItem approve = menu.findItem(R.id.action_approve);
//                    MenuItem disapprove = menu.findItem(R.id.action_disapprove);
//                    MenuItem delete = menu.findItem(R.id.action_delete);
//
//                    disapprove.setVisible(false);

                    popup.setOnMenuItemClickListener(item -> {
                        try {
                            if (item.getItemId() == R.id.action_details) {
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Name : " + contact.getmName());
                                stringBuffer.append("\nSurname : " + contact.getmSurname());
                                stringBuffer.append("\nGender : " + contact.getmGender());
                                stringBuffer.append("\nDate of Birth : " + contact.getmDob());
                                stringBuffer.append("\nOccupation : " + contact.getmOccupation());
                                stringBuffer.append("\nApproval status(IsApproved) : " + contact.getmStatus());
                                stringBuffer.append("\nActive status(IsActive) : " + contact.getmActive());
                                methods.showAlert("\nUser Details", stringBuffer.toString(), mContext);
                            } else if (item.getItemId() == R.id.action_activate) {
                                /*
                                 * prompt if user is sure to activate if yes then activate,
                                 * else do not approve,
                                 * also check activation status of user if already activated
                                 * then no need
                                 */
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm action");
                                builder.setMessage("Are you sure to activate this account.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        if (contact.getmStatus().equalsIgnoreCase("Yes")) {
                                            methods.showAlert("Already active", "The selected user is already activated.", mContext);
                                        } else {
                                            actionOnUser(contact.getmContactID(), "activate");
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (item.getItemId() == R.id.action_deactivate) {
                                /*
                                 * prompt if user is sure to deactivate if yes then deactivate,
                                 * else do not approve,
                                 * also check deactivation status of user if already deactivated
                                 * then no need
                                 */
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm action");
                                builder.setMessage("Are you sure to deactivate this account.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        if (contact.getmActive().equalsIgnoreCase("No")) {
                                            methods.showAlert("Already inactive", "The selected user is already inactivated.", mContext);
                                        } else {
                                            actionOnUser(contact.getmContactID(), "deactivate");
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
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

    private void actionOnUser(String userId, String action) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, userId);
            RequestBody op = RequestBody.create(MultipartBody.FORM, action);
            Call<ResponseBody> call = apiInterface.UserActions(id, op);
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

    //add more users
    public void addUser(List<contact> list){
        try{
            for(contact c : list){
                mContacts.add(c);
            }
        }catch (Exception e){
            Toast.makeText(mContext, "Error adding more users.", Toast.LENGTH_SHORT).show();
        }
    }
}
