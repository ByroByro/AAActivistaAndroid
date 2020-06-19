package com.example.actionaidactivista.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.hsalf.smilerating.SmileRating;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class feed_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<feed> mFeeds;
    private List<feed> filtered_Feeds;
    private Context mContext;
    private int mTotalTypes;
    //retrofit
    private ApiInterface apiInterface;
    private Dialog mDialog;

    public static final int TEXT_TYPE = 0;//text
    public static final int IMAGE_TYPE = 1;//image
    public static final int VIDEO_TYPE = 2;//video

    public feed_adapter(ArrayList<feed> feeds, Context ctx) {
        this.mFeeds = feeds;
        this.mContext = ctx;
        this.mTotalTypes = mFeeds.size();
        this.filtered_Feeds = new ArrayList<>(feeds);
        mDialog = new Dialog(ctx);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<feed> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_Feeds);
            } else {
                for (feed item : filtered_Feeds) {
                    if ((item.getmDescription() != null && item.getmDescription().toLowerCase().contains(constraint)) || (item.getmDate() != null && item.getmDate().toLowerCase().contains(constraint)
                            || (item.getmMimeType() != null && item.getmMimeType().toLowerCase().contains(constraint)))) {
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
            mFeeds.clear();
            mFeeds.addAll((Collection<? extends feed>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TEXT_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_feed, parent, false);
            return new TextTypeViewHolder(view);
        } else if (viewType == IMAGE_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_feed, parent, false);
            return new ImageTypeViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_feed, parent, false);
            return new VideoTypeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            feed feedInstance = mFeeds.get(position);
            if (feedInstance != null) {
                switch (holder.getItemViewType()) {
                    case TEXT_TYPE:
                        TextTypeViewHolder textTypeViewHolder = (TextTypeViewHolder) holder;
                        textTypeViewHolder.mContent.setText(feedInstance.getmContent());
                        textTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        textTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
                        textTypeViewHolder.mFeed.setOnClickListener(v -> {
                            try {
                                final PopupMenu popupMenu = new PopupMenu(mContext, textTypeViewHolder.mFeed, Gravity.CENTER_HORIZONTAL);
                                popupMenu.getMenuInflater().inflate(R.menu.feed_like_comment, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(item -> {
                                    int menu = item.getItemId();
                                    if (menu == R.id.action_like) {
                                        if (methods.checkAccountIsLogged(mContext)) {
                                            //like
                                            String likerid = String.valueOf(methods.getUserId(mContext));
                                            String feedid = feedInstance.getmId();
                                            String likertype = "";
                                            String uid = "";
                                            if(methods.isAdmin(mContext)){
                                                likertype = "admin";
                                                uid = "admin" + likerid;
                                            }else {
                                                likertype = "user";
                                                uid = "user" + likerid;
                                            }
                                            String date = methods.getDateForSqlServer();

                                            //post the like to server
                                            likeFeed(likerid,likertype,date,uid,feedid);
                                        } else {
                                            //account needed
                                            methods.showAlert("Sign in required","Sign first before you can like.",mContext);
                                        }
                                    } else if (menu == R.id.action_comment) {
                                        if (methods.checkAccountIsLogged(mContext)) {
                                            //like
                                            //show dialog
                                            mDialog.setContentView(R.layout.comment_feed_dialog);

                                            EditText mComment = (EditText) mDialog.findViewById(R.id.comment_detail);
                                            Button ok = (Button) mDialog.findViewById(R.id.comment);
                                            mDialog.setCanceledOnTouchOutside(false);
                                            mDialog.setOnKeyListener((dialog, keyCode, event) -> {
                                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                    mDialog.dismiss();
                                                }
                                                return true;
                                            });

                                            //set on click listener for the button
                                            ok.setOnClickListener(v1 -> {
                                                try{
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if(methods.isAdmin(mContext)){
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    }else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if(comment.equalsIgnoreCase("") || comment == null){
                                                        methods.showAlert("Invalid Comment","Enter a comment.",mContext);
                                                    }else {
                                                        commentFeed(likerid,likertype,date,uid,feedid,comment);
                                                    }
                                                }catch (Exception e){
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } else {
                                            //account needed
                                            methods.showAlert("Sign in required","Sign first before you can like.",mContext);
                                        }

                                    }
                                    return false;
                                });
                                popupMenu.show();
                            } catch (Exception e) {
                                Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case IMAGE_TYPE:
                        ImageTypeViewHolder imageTypeViewHolder = (ImageTypeViewHolder) holder;
                        imageTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        imageTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
                        imageTypeViewHolder.mFeed.setOnClickListener(v -> {
                            try {
                                final PopupMenu popupMenu = new PopupMenu(mContext, imageTypeViewHolder.mFeed, Gravity.CENTER_HORIZONTAL);
                                popupMenu.getMenuInflater().inflate(R.menu.feed_like_comment, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(item -> {
                                    int menu = item.getItemId();
                                    if (menu == R.id.action_like) {
                                        if (methods.checkAccountIsLogged(mContext)) {
                                            //like
                                            String likerid = String.valueOf(methods.getUserId(mContext));
                                            String feedid = feedInstance.getmId();
                                            String likertype;
                                            String uid;
                                            if(methods.isAdmin(mContext)){
                                                likertype = "admin";
                                                uid = "admin" + likerid;
                                            }else {
                                                likertype = "user";
                                                uid = "user" + likerid;
                                            }
                                            String date = methods.getDateForSqlServer();

                                            //post the like to server
                                            likeFeed(likerid,likertype,date,uid,feedid);
                                        } else {
                                            //account needed
                                            methods.showAlert("Sign in required","Sign first before you can like.",mContext);
                                        }
                                    } else if (menu == R.id.action_comment) {
                                        if (methods.checkAccountIsLogged(mContext)) {
                                            //like
                                            //show dialog
                                            mDialog.setContentView(R.layout.comment_feed_dialog);

                                            EditText mComment = (EditText) mDialog.findViewById(R.id.comment_detail);
                                            Button ok = (Button) mDialog.findViewById(R.id.comment);
                                            mDialog.setCanceledOnTouchOutside(false);
                                            mDialog.setOnKeyListener((dialog, keyCode, event) -> {
                                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                    mDialog.dismiss();
                                                }
                                                return true;
                                            });

                                            //set on click listener for the button
                                            ok.setOnClickListener(v1 -> {
                                                try{
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if(methods.isAdmin(mContext)){
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    }else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if(comment.equalsIgnoreCase("") || comment == null){
                                                        methods.showAlert("Invalid Comment","Enter a comment.",mContext);
                                                    }else {
                                                        commentFeed(likerid,likertype,date,uid,feedid,comment);
                                                    }
                                                }catch (Exception e){
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } else {
                                            //account needed
                                            methods.showAlert("Sign in required","Sign first before you can like.",mContext);
                                        }

                                    }
                                    return false;
                                });
                                popupMenu.show();
                            } catch (Exception e) {
                                Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (!feedInstance.getmUrl().equalsIgnoreCase("N/A")) {
                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions.centerCrop();
                            requestOptions.placeholder(R.drawable.ic_contacts_red);
                            requestOptions.error(R.drawable.ic_contacts_red);

                            Glide.with(mContext)
                                    .applyDefaultRequestOptions(requestOptions)
                                    .load(feedInstance.getmUrl())
                                    .into(((ImageTypeViewHolder) holder).mImage);
                        } else {
                            ((ImageTypeViewHolder) holder).mImage.setImageResource(R.drawable.ic_menu_gallery);
                        }

                        break;
                    case VIDEO_TYPE:
                        VideoTypeViewHolder videoTypeViewHolder = (VideoTypeViewHolder) holder;
                        videoTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        videoTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext)  + " : " + feedInstance.getmLocation());
                        videoTypeViewHolder.mFeed.setOnClickListener(v -> {
                            try {
                                final PopupMenu popupMenu = new PopupMenu(mContext, videoTypeViewHolder.mFeed, Gravity.CENTER_HORIZONTAL);
                                popupMenu.getMenuInflater().inflate(R.menu.feed_like_comment, popupMenu.getMenu());
                                popupMenu.setOnMenuItemClickListener(item -> {
                                    int menu = item.getItemId();
                                    if (menu == R.id.action_like) {
                                        if (methods.checkAccountIsLogged(mContext)) {
                                            //like
                                            String likerid = String.valueOf(methods.getUserId(mContext));
                                            String feedid = feedInstance.getmId();
                                            String likertype;
                                            String uid ;
                                            if(methods.isAdmin(mContext)){
                                                likertype = "admin";
                                                uid = "admin" + likerid;
                                            }else {
                                                likertype = "user";
                                                uid = "user" + likerid;
                                            }
                                            String date = methods.getDateForSqlServer();

                                            //post the like to server
                                            likeFeed(likerid,likertype,date,uid,feedid);
                                        } else {
                                            //account needed
                                            methods.showAlert("Sign in required","Sign first before you can like.",mContext);
                                        }
                                    } else if (menu == R.id.action_comment) {
                                        if (methods.checkAccountIsLogged(mContext)) {
                                            //like
                                            //show dialog
                                            mDialog.setContentView(R.layout.comment_feed_dialog);

                                            EditText mComment = (EditText) mDialog.findViewById(R.id.comment_detail);
                                            Button ok = (Button) mDialog.findViewById(R.id.comment);
                                            mDialog.setCanceledOnTouchOutside(false);
                                            mDialog.setOnKeyListener((dialog, keyCode, event) -> {
                                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                    mDialog.dismiss();
                                                }
                                                return true;
                                            });

                                            //set on click listener for the button
                                            ok.setOnClickListener(v1 -> {
                                                try{
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if(methods.isAdmin(mContext)){
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    }else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if(comment.equalsIgnoreCase("") || comment == null){
                                                        methods.showAlert("Invalid Comment","Enter a comment.",mContext);
                                                    }else {
                                                        commentFeed(likerid,likertype,date,uid,feedid,comment);
                                                    }
                                                }catch (Exception e){
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } else {
                                            //account needed
                                            methods.showAlert("Sign in required","Sign first before you can like.",mContext);
                                        }

                                    }
                                    return false;
                                });
                                popupMenu.show();
                            } catch (Exception e) {
                                Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                            }
                        });
                        videoTypeViewHolder.mVideo.setVideoURI(Uri.parse(feedInstance.getmUrl()));
                        MediaController mediaController = new MediaController(mContext);
                        mediaController.setAnchorView(((VideoTypeViewHolder) holder).mAnchor);
                        videoTypeViewHolder.mVideo.setMediaController(mediaController);
                        videoTypeViewHolder.mVideo.seekTo(100);
                        break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Error binding view holder " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return mFeeds.size();
    }

    public static class TextTypeViewHolder extends RecyclerView.ViewHolder {

        TextView mContent;
        TextView mDescription;
        TextView mDate;
        CardView mFeed;

        public TextTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mContent = itemView.findViewById(R.id.feedTextView);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
        }
    }

    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView mDescription;
        TextView mDate;
        CardView mFeed;

        public ImageTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImage = itemView.findViewById(R.id.feedImageView);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
        }
    }

    public static class VideoTypeViewHolder extends RecyclerView.ViewHolder {

        VideoView mVideo;
        TextView mDescription;
        TextView mDate;
        CardView mFeed;
        LinearLayout mAnchor;

        public VideoTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mVideo = itemView.findViewById(R.id.feedVideoView);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
            this.mAnchor = itemView.findViewById(R.id.videoView_Anchor);
        }
    }

    @Override
    public int getItemViewType(int position) {

        switch (Integer.parseInt(mFeeds.get(position).getmIntType())) {
            case 0:
                return TEXT_TYPE;
            case 1:
                return IMAGE_TYPE;
            case 2:
                return VIDEO_TYPE;
            default:
                return -1;
        }
    }

    //method for liking a feed
    private void likeFeed(String likerid, String likertype, String dateliked, String uid,String feedid) {
        try {
            RequestBody lid = RequestBody.create(MultipartBody.FORM, likerid);
            RequestBody ltyp = RequestBody.create(MultipartBody.FORM, likertype);
            RequestBody dtepost = RequestBody.create(MultipartBody.FORM, dateliked);
            RequestBody ide = RequestBody.create(MultipartBody.FORM, uid);
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);

            Call<ResponseBody> like = apiInterface.LikeFeed(lid, ltyp, dtepost, ide,fid);
            methods.showDialog(mDialog, "Posting like...", true);
            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Upload failed.Try again.", mContext);
                        } else if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Liking successful.", mContext);

                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", mContext);
                        } else if (message.equalsIgnoreCase("Exist")) {
                            methods.showAlert("Response", "You liked the same content before.", mContext);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), mContext);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error raising like event.", Toast.LENGTH_SHORT).show();
        }
    }
    //method for liking a feed
    private void commentFeed(String commentorid, String commentortype, String datecommented, String uid,String feedid,String comment) {
        try {
            RequestBody lid = RequestBody.create(MultipartBody.FORM, commentorid);
            RequestBody ltyp = RequestBody.create(MultipartBody.FORM, commentortype);
            RequestBody dtepost = RequestBody.create(MultipartBody.FORM, datecommented);
            RequestBody ide = RequestBody.create(MultipartBody.FORM, uid);
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);
            RequestBody comm = RequestBody.create(MultipartBody.FORM, comment);

            Call<ResponseBody> like = apiInterface.CommentFeed(lid, ltyp, dtepost, ide,fid,comm);
            methods.showDialog(mDialog, "Posting comment...", true);
            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Upload failed.Try again.", mContext);
                        } else if (message.equalsIgnoreCase("Success")) {
                            methods.showAlert("Response", "Commenting successful.", mContext);

                        } else if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Server error.", mContext);
                        } else if (message.equalsIgnoreCase("Exist")) {
                            methods.showAlert("Response", "You commented the same content before.", mContext);
                        }
                        //Toast.makeText(RegistrationActivity.this, result, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), mContext);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error raising like event.", Toast.LENGTH_SHORT).show();
        }
    }
}
