package com.example.actionaidactivista.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.ReportCommentActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.ReportedComment;
import com.example.actionaidactivista.models.comment;
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

public class ReportedCommentsAdapter extends RecyclerView.Adapter<ReportedCommentsAdapter.ViewHolder> {

    private Context mContext;
    private List<ReportedComment> comments;
    //private List<comment> filtered_comments;
    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;

    public ReportedCommentsAdapter(List<ReportedComment> list, Context ctx) {
        this.comments = list;
        this.mContext = ctx;
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
        //this.filtered_comments = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.reported_comment_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new ReportedCommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            ReportedComment comm = comments.get(position);
            holder.comment.setText(comm.getComment());
            holder.reason.setPaintFlags(holder.reason.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.delete.setOnClickListener(v -> {
                try {
                    deleteComment(comm.getCommentid(), position);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error raising delete event", Toast.LENGTH_SHORT).show();
                }
            });
            holder.reason.setOnClickListener(v -> {
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(comm.getReason());
                    methods.showAlert("\nReason", stringBuffer.toString(), mContext);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
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

        public TextView comment;
        public TextView reason;
        public ImageButton delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.comment);
            reason = itemView.findViewById(R.id.view_reason);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    //method for adding more comments
    public void addComments(List<ReportedComment> list) {
        try {
            for (ReportedComment c : list) {
                comments.add(c);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(mContext, "Add comment error in adapter", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteComment(String commid, int position) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, commid);

            Call<ResponseBody> call = apiInterface.DeleteReportedComment(id);
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
                                Toast.makeText(mContext, "Comment deleted.", Toast.LENGTH_SHORT).show();
                            } else if (result.equalsIgnoreCase("Error")) {
                                Toast.makeText(mContext, "Server error.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Failed to delete.", Toast.LENGTH_SHORT).show();
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
