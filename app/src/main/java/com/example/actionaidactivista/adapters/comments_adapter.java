package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.actionaidactivista.ReportCommentActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.comment;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class comments_adapter extends RecyclerView.Adapter<comments_adapter.ViewHolder> {

    private Context mContext;
    private List<comment> comments;
    private List<comment> filtered_comments;
    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;

    public comments_adapter(List<comment> list, Context ctx) {
        this.comments = list;
        this.mContext = ctx;
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
        this.filtered_comments = new ArrayList<>(list);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<comment> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_comments);
            } else {
                for (comment item : filtered_comments) {
//                    if ((item.getmName() != null && item.getmName().toLowerCase().contains(constraint)) || (item.getmSurname() != null && item.getmSurname().toLowerCase().contains(constraint)
//                            || (item.getmStatus() != null && item.getmStatus().toLowerCase().contains(constraint)))) {
//                        filtered_items.add(item);
//                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered_items;

            return filterResults;
        }

        //run on UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            comments.clear();
            comments.addAll((Collection<? extends comment>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.comment_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new comments_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            comment comm = comments.get(position);
            if (!comm.getFirstName().equalsIgnoreCase("n/a") && !comm.getSurname().equalsIgnoreCase("n/a")) {
                holder.username.setText(comm.getFirstName() + " " + comm.getSurname());
            }
            holder.comment.setText(comm.getComment());
            holder.date.setText(methods.getReadableDate(comm.getDateCommented(),mContext));
            if (!comm.getProfilePic().equalsIgnoreCase("N/A")) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                requestOptions.placeholder(R.drawable.ic_contacts_red);
                requestOptions.error(R.drawable.ic_contacts_red);

                Glide.with(mContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(comm.getProfilePic())
                        .into(holder.profile);
            } else {
                holder.profile.setImageResource(R.drawable.ic_contacts_red);
            }

            //set delete button to visible if comment commenter id is
            //equal to currently logged account
            if (String.valueOf(methods.getUserId(mContext)).equalsIgnoreCase(comm.getCommentorId())) {
                holder.delete.setVisibility(View.VISIBLE);
            } else {
                holder.delete.setVisibility(View.GONE);
            }
            holder.delete.setOnClickListener(v -> {
                try {
                    deleteComment(comm.getCommId(), position);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error raising delete event", Toast.LENGTH_SHORT).show();
                }
            });
            holder.report.setOnClickListener(v -> {
                try {
                    //get to report intent
                    Intent intent = new Intent(mContext, ReportCommentActivity.class);
                    intent.putExtra("reported_id",comm.getCommentorId());
                    intent.putExtra("comment_id",comm.getCommId());
                    intent.putExtra("comment",comm.getComment());
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error raising report event", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error binding view holder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView comment;
        public ImageView profile;
        public ImageButton delete;
        public ImageButton report;
        public TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.comment);
            profile = itemView.findViewById(R.id.imageView);
            delete = itemView.findViewById(R.id.delete);
            report = itemView.findViewById(R.id.report);
            date = itemView.findViewById(R.id.date);
        }
    }

    //method for adding more comments
    public void addComments(List<comment> list) {
        try {
            for (comment c : list) {
                comments.add(c);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(mContext, "Add feed error in adapter", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteComment(String commid, int position) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, commid);

            Call<ResponseBody> call = apiInterface.DeleteComment(id);
            methods.showDialog(mDialog, "Processing ...", true);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            String result = methods.removeQoutes(responseData);
                            if (result.equalsIgnoreCase("Success")) {
                                notifyItemRemoved(position);
                            } else if (result.equalsIgnoreCase("Error")) {
                                Toast.makeText(mContext, "Server error.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Server error.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Request unsuccessful.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        methods.showAlert("Error", e.toString(), mContext);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    methods.showDialog(mDialog, "Dismiss", false);
                    //methods.showAlert("List onFailure", t.toString(), mContext);
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error processing action event.", Toast.LENGTH_SHORT).show();
        }
    }
}
