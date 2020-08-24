package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
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
import com.example.actionaidactivista.FeedCommentsActivity;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.material.snackbar.Snackbar;
import com.hsalf.smilerating.SmileRating;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
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
    private LinearLayout rootLayout;
    public boolean isOnline;
    //retrofit
    private ApiInterface apiInterface;
    private Dialog mDialog;

    public static final int TEXT_TYPE = 0;//text
    public static final int IMAGE_TYPE = 1;//image
    public static final int VIDEO_TYPE = 2;//video
    public static final int AUDIO_TYPE = 4;//audio

    private SimpleExoPlayer exoVideoPlayer;
    private SimpleExoPlayer exoAudioPlayer;

    public feed_adapter(ArrayList<feed> feeds, Context ctx, boolean isonline, LinearLayout linearLayout) {

        this.mFeeds = feeds;
        this.mContext = ctx;
        this.mTotalTypes = mFeeds.size();
        this.filtered_Feeds = new ArrayList<>(feeds);
        this.mDialog = new Dialog(ctx);
        this.rootLayout = linearLayout;
        this.apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        this.isOnline = isonline;
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
        } else if (viewType == AUDIO_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_feed, parent, false);
            return new VideoTypeViewHolder(view);
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
                        if (isOnline) {
                            textTypeViewHolder.uploaderUsername.setText(feedInstance.getUploaderName() + " " + feedInstance.getUploaderSurname());
                            textTypeViewHolder.noOfLikes.setText(feedInstance.getNoOfLikes() + " like(s)");
                            textTypeViewHolder.viewComments.setPaintFlags(textTypeViewHolder.viewComments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                            if (!feedInstance.getUploaderProfile().equalsIgnoreCase("N/A")) {

                                Glide.with(mContext)
                                        .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                                        .load(feedInstance.getUploaderProfile())
                                        .into(textTypeViewHolder.uploaderProfile);
                            } else {
                                textTypeViewHolder.uploaderProfile.setImageResource(R.drawable.ic_account_circle);
                            }
                        } else {
                            //hide some controls
                            textTypeViewHolder.uploaderUsername.setText("(username)");
                            textTypeViewHolder.mRelative.setVisibility(View.GONE);
                        }

                        //delete feed
                        //check if isOnline
                        if (isOnline) {
                            //check if feed uploader id is the same as that of
                            //actively logged in account
                            if (feedInstance.getmUploaderId().equalsIgnoreCase(String.valueOf(methods.getUserId(mContext)))) {
                                //if they are the same, this means that
                                //the current active signed in account
                                //is the owner of that feed, show delete button
                                textTypeViewHolder.delete.setVisibility(View.VISIBLE);
                            } else {
                                //current account is not owner of feed
                                //hide delete button
                                textTypeViewHolder.delete.setVisibility(View.GONE);
                            }

                        } else {
                            //hide delete button
                            textTypeViewHolder.delete.setVisibility(View.GONE);
                        }
                        //get number of comments
                        if (isOnline) {
                            //getNoOfComments(feedInstance.getmId(), textTypeViewHolder.noOfComments);
                            getNoOfComments(feedInstance.getmId(), textTypeViewHolder.viewComments);
                        }
                        //check if liked feed before
                        //checkIfLikedFeedBefore(String.valueOf(methods.getUserId(mContext)),feedInstance.getmId(),textTypeViewHolder.like,textTypeViewHolder.unlike);

                        try {
                            if (isOnline) {
                                //view comments on click listener
                                textTypeViewHolder.viewComments.setOnClickListener(v13 -> {
                                    Intent comments = new Intent(mContext, FeedCommentsActivity.class);
                                    comments.putExtra("feed_id", feedInstance.getmId());
                                    mContext.startActivity(comments);
                                });
                                //ImageButton onclick events
                                //like
                                textTypeViewHolder.like.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                        methods.showAlert("Can't like", "You cannot like your own activity", mContext);
                                    } else if (methods.checkAccountIsLogged(mContext)) {
                                        //like
                                        String likerid = String.valueOf(methods.getUserId(mContext));
                                        String feedid = feedInstance.getmId();
                                        String likertype;
                                        String uid;
                                        if (methods.isAdmin(mContext)) {
                                            likertype = "admin";
                                            uid = "admin" + likerid;
                                        } else {
                                            likertype = "user";
                                            uid = "user" + likerid;
                                        }
                                        String date = methods.getDateForSqlServer();

                                        //post the like to server
                                        likeFeed(likerid, likertype, date, uid, feedid, feedInstance.getmUploaderId(), position);
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can like.", mContext);
                                    }
                                });
                                //unlike
                                textTypeViewHolder.unlike.setOnClickListener(v12 -> {
                                    unLikeFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                });
                                //comment
                                textTypeViewHolder.comment.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    //if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                    //    methods.showAlert("Can't comment", "You cannot comment your own activity", mContext);
                                    //} else
                                    if (methods.checkAccountIsLogged(mContext)) {
                                        //comment
                                        //show dialog
                                        try {
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
                                                try {
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if (methods.isAdmin(mContext)) {
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    } else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if (comment.equalsIgnoreCase("") || comment == null) {
                                                        methods.showAlert("Invalid Comment", "Enter a comment.", mContext);
                                                    } else {
                                                        commentFeed(likerid, likertype, date, uid, feedid, comment, feedInstance.getmUploaderId());
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } catch (Exception e) {
                                            Toast.makeText(mContext, "Error showing dialog.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can comment.", mContext);
                                    }
                                });
                                //delete
                                textTypeViewHolder.delete.setOnClickListener(v -> {
                                    try {
                                        /*
                                         * prompt if user is sure to delete if yes then delete,
                                         * else do not delete
                                         */
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Delete feed?");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    } catch (Exception e) {
                                        Toast.makeText(mContext, "Error raising event.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                //offline
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case IMAGE_TYPE:
                        ImageTypeViewHolder imageTypeViewHolder = (ImageTypeViewHolder) holder;
                        imageTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        imageTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
                        if (isOnline) {
                            imageTypeViewHolder.uploaderUsername.setText(feedInstance.getUploaderName() + " " + feedInstance.getUploaderSurname());
                            imageTypeViewHolder.noOfLikes.setText(feedInstance.getNoOfLikes() + " like(s)");
                            imageTypeViewHolder.viewComments.setPaintFlags(imageTypeViewHolder.viewComments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            if (!feedInstance.getUploaderProfile().equalsIgnoreCase("N/A")) {

                                Glide.with(mContext)
                                        .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                                        .load(feedInstance.getUploaderProfile())
                                        .into(imageTypeViewHolder.uploaderProfile);
                            } else {
                                imageTypeViewHolder.uploaderProfile.setImageResource(R.drawable.ic_account_circle);
                            }
                        } else {
                            //hide some controls
                            imageTypeViewHolder.uploaderUsername.setText("(username)");
                            imageTypeViewHolder.mRelative.setVisibility(View.GONE);
                        }

                        //delete feed
                        //check if isOnline
                        if (isOnline) {
                            //check if feed uploader id is the same as that of
                            //actively logged in account
                            if (feedInstance.getmUploaderId().equalsIgnoreCase(String.valueOf(methods.getUserId(mContext)))) {
                                //if they are the same, this means that
                                //the current active signed in account
                                //is the owner of that feed, show delete button
                                imageTypeViewHolder.delete.setVisibility(View.VISIBLE);
                            } else {
                                //current account is not owner of feed
                                //hide delete button
                                imageTypeViewHolder.delete.setVisibility(View.GONE);
                            }

                        } else {
                            //hide delete button
                            imageTypeViewHolder.delete.setVisibility(View.GONE);
                        }

                        //get number of comments
                        if (isOnline) {
                            getNoOfComments(feedInstance.getmId(), imageTypeViewHolder.viewComments);
                            //getNoOfComments(feedInstance.getmId(), imageTypeViewHolder.noOfComments);
                        }

                        //check if liked feed before
                        //checkIfLikedFeedBefore(String.valueOf(methods.getUserId(mContext)),feedInstance.getmId(),imageTypeViewHolder.like,imageTypeViewHolder.unlike);

                        try {
                            if (isOnline) {
                                //view comments on click listener
                                imageTypeViewHolder.viewComments.setOnClickListener(v13 -> {
                                    Intent comments = new Intent(mContext, FeedCommentsActivity.class);
                                    comments.putExtra("feed_id", feedInstance.getmId());
                                    mContext.startActivity(comments);
                                });
                                //ImageButton onclick events
                                //like
                                imageTypeViewHolder.like.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                        methods.showAlert("Can't like", "You cannot like your own activity", mContext);
                                    } else if (methods.checkAccountIsLogged(mContext)) {
                                        //like
                                        String likerid = String.valueOf(methods.getUserId(mContext));
                                        String feedid = feedInstance.getmId();
                                        String likertype;
                                        String uid;
                                        if (methods.isAdmin(mContext)) {
                                            likertype = "admin";
                                            uid = "admin" + likerid;
                                        } else {
                                            likertype = "user";
                                            uid = "user" + likerid;
                                        }
                                        String date = methods.getDateForSqlServer();

                                        //post the like to server
                                        likeFeed(likerid, likertype, date, uid, feedid, feedInstance.getmUploaderId(), position);
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can like.", mContext);
                                    }
                                });
                                //unlike
                                imageTypeViewHolder.unlike.setOnClickListener(v12 -> {
                                    unLikeFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                });
                                //comment
                                imageTypeViewHolder.comment.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    //if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                    //    methods.showAlert("Can't comment", "You cannot comment your own activity", mContext);
                                    //} else
                                    if (methods.checkAccountIsLogged(mContext)) {
                                        //comment
                                        //show dialog
                                        try {
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
                                                try {
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if (methods.isAdmin(mContext)) {
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    } else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if (comment.equalsIgnoreCase("") || comment == null) {
                                                        methods.showAlert("Invalid Comment", "Enter a comment.", mContext);
                                                    } else {
                                                        commentFeed(likerid, likertype, date, uid, feedid, comment, feedInstance.getmUploaderId());
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } catch (Exception e) {
                                            Toast.makeText(mContext, "Error showing dialog.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can comment.", mContext);
                                    }
                                });
                                //delete
                                imageTypeViewHolder.delete.setOnClickListener(v -> {
                                    try {
                                        /*
                                         * prompt if user is sure to delete if yes then delete,
                                         * else do not delete
                                         */
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Delete feed?");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    } catch (Exception e) {
                                        Toast.makeText(mContext, "Error raising event.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                //offline
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                        }

                        if (!feedInstance.getmUrl().equalsIgnoreCase("N/A")) {
                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions.centerCrop();
                            requestOptions.placeholder(R.drawable.ic_camera_grey);
                            requestOptions.error(R.drawable.ic_camera_grey);

                            Glide.with(mContext)
                                    .applyDefaultRequestOptions(requestOptions)
                                    .load(feedInstance.getmUrl())
                                    .into(((ImageTypeViewHolder) holder).mImage);
                        } else {
                            ((ImageTypeViewHolder) holder).mImage.requestLayout();
                            ((ImageTypeViewHolder) holder).mImage.getLayoutParams().height = 50;
                            ((ImageTypeViewHolder) holder).mImage.setImageResource(R.drawable.ic_camera_grey);
                        }

                        if (!isOnline) {
                            ((ImageTypeViewHolder) holder).mImage.requestLayout();
                            ((ImageTypeViewHolder) holder).mImage.getLayoutParams().height = 50;
                            ((ImageTypeViewHolder) holder).mImage.setImageResource(R.drawable.ic_camera_grey);
                        }

                        break;
                    case VIDEO_TYPE:
                        VideoTypeViewHolder videoTypeViewHolder = (VideoTypeViewHolder) holder;
                        videoTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        videoTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
                        if (isOnline) {
                            videoTypeViewHolder.uploaderUsername.setText(feedInstance.getUploaderName() + " " + feedInstance.getUploaderSurname());
                            videoTypeViewHolder.noOfLikes.setText(feedInstance.getNoOfLikes() + " like(s)");
                            videoTypeViewHolder.viewComments.setPaintFlags(videoTypeViewHolder.viewComments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            if (!feedInstance.getUploaderProfile().equalsIgnoreCase("N/A")) {

                                Glide.with(mContext)
                                        .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                                        .load(feedInstance.getUploaderProfile())
                                        .into(videoTypeViewHolder.uploaderProfile);
                            } else {
                                videoTypeViewHolder.uploaderProfile.setImageResource(R.drawable.ic_account_circle);
                            }
                        } else {
                            //hide some controls
                            videoTypeViewHolder.uploaderUsername.setText("(username)");
                            videoTypeViewHolder.mRelative.setVisibility(View.GONE);
                        }

                        //delete feed
                        //check if isOnline
                        if (isOnline) {
                            //check if feed uploader id is the same as that of
                            //actively logged in account
                            if (feedInstance.getmUploaderId().equalsIgnoreCase(String.valueOf(methods.getUserId(mContext)))) {
                                //if they are the same, this means that
                                //the current active signed in account
                                //is the owner of that feed, show delete button
                                videoTypeViewHolder.delete.setVisibility(View.VISIBLE);
                            } else {
                                //current account is not owner of feed
                                //hide delete button
                                videoTypeViewHolder.delete.setVisibility(View.GONE);
                            }

                        } else {
                            //hide delete button
                            videoTypeViewHolder.delete.setVisibility(View.GONE);
                        }

                        //get number of comments
                        if (isOnline) {
                            getNoOfComments(feedInstance.getmId(), videoTypeViewHolder.viewComments);
                            //getNoOfComments(feedInstance.getmId(), videoTypeViewHolder.noOfComments);
                        }

                        //check if liked feed before
                        //checkIfLikedFeedBefore(String.valueOf(methods.getUserId(mContext)),feedInstance.getmId(),videoTypeViewHolder.like,videoTypeViewHolder.unlike);

                        try {
                            if (isOnline) {
                                //view comments on click listener
                                videoTypeViewHolder.viewComments.setOnClickListener(v13 -> {
                                    Intent comments = new Intent(mContext, FeedCommentsActivity.class);
                                    comments.putExtra("feed_id", feedInstance.getmId());
                                    mContext.startActivity(comments);
                                });
                                //ImageButton onclick events
                                //like
                                videoTypeViewHolder.like.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                        methods.showAlert("Can't like", "You cannot like your own activity", mContext);
                                    } else if (methods.checkAccountIsLogged(mContext)) {
                                        //like
                                        String likerid = String.valueOf(methods.getUserId(mContext));
                                        String feedid = feedInstance.getmId();
                                        String likertype;
                                        String uid;
                                        if (methods.isAdmin(mContext)) {
                                            likertype = "admin";
                                            uid = "admin" + likerid;
                                        } else {
                                            likertype = "user";
                                            uid = "user" + likerid;
                                        }
                                        String date = methods.getDateForSqlServer();

                                        //post the like to server
                                        likeFeed(likerid, likertype, date, uid, feedid, feedInstance.getmUploaderId(), position);
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can like.", mContext);
                                    }
                                });
                                //unlike
                                videoTypeViewHolder.unlike.setOnClickListener(v12 -> {
                                    unLikeFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                });
                                //comment
                                videoTypeViewHolder.comment.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    //if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                    //    methods.showAlert("Can't comment", "You cannot comment your own activity", mContext);
                                    //} else
                                    if (methods.checkAccountIsLogged(mContext)) {
                                        //comment
                                        //show dialog
                                        try {
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
                                                try {
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if (methods.isAdmin(mContext)) {
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    } else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if (comment.equalsIgnoreCase("") || comment == null) {
                                                        methods.showAlert("Invalid Comment", "Enter a comment.", mContext);
                                                    } else {
                                                        commentFeed(likerid, likertype, date, uid, feedid, comment, feedInstance.getmUploaderId());
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } catch (Exception e) {
                                            Toast.makeText(mContext, "Error showing dialog.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can comment.", mContext);
                                    }
                                });
                                //delete
                                videoTypeViewHolder.delete.setOnClickListener(v -> {
                                    try {
                                        /*
                                         * prompt if user is sure to delete if yes then delete,
                                         * else do not delete
                                         */
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Delete feed?");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    } catch (Exception e) {
                                        Toast.makeText(mContext, "Error raising event.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                //offline
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                        }

                        //videoTypeViewHolder.mVideo.setVideoURI(Uri.parse(feedInstance.getmUrl()));
                        //MediaController mediaController = new MediaController(mContext);
                        //mediaController.setAnchorView(((VideoTypeViewHolder) holder).mAnchor);
                        //videoTypeViewHolder.mVideo.setMediaController(mediaController);
                        //videoTypeViewHolder.mVideo.seekTo(100);

                        //exo player code
                        BandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
                        TrackSelector trackSelector2 = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter2));
                        Uri uri2;
                        exoVideoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector2);
                        uri2 = Uri.parse(feedInstance.getmUrl());
                        DefaultHttpDataSourceFactory dataSourceFactory2 = new DefaultHttpDataSourceFactory("exoplayer_video");
                        ExtractorsFactory extractorsFactory2 = new DefaultExtractorsFactory();
                        MediaSource mediaSource2 = new ExtractorMediaSource(uri2, dataSourceFactory2, extractorsFactory2, null, null);
                        videoTypeViewHolder.mExoVideo.setPlayer(exoVideoPlayer);
                        exoVideoPlayer.prepare(mediaSource2);
                        exoVideoPlayer.setPlayWhenReady(false);

                        break;
                    case AUDIO_TYPE:
                        VideoTypeViewHolder videoTypeViewHolder2 = (VideoTypeViewHolder) holder;
                        videoTypeViewHolder2.mDescription.setText(feedInstance.getmDescription());
                        videoTypeViewHolder2.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
                        if (isOnline) {
                            videoTypeViewHolder2.uploaderUsername.setText(feedInstance.getUploaderName() + " " + feedInstance.getUploaderSurname());
                            videoTypeViewHolder2.noOfLikes.setText(feedInstance.getNoOfLikes() + " like(s)");
                            videoTypeViewHolder2.viewComments.setPaintFlags(videoTypeViewHolder2.viewComments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            if (!feedInstance.getUploaderProfile().equalsIgnoreCase("N/A")) {

                                Glide.with(mContext)
                                        .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_account_circle))
                                        .load(feedInstance.getUploaderProfile())
                                        .into(videoTypeViewHolder2.uploaderProfile);
                            } else {
                                videoTypeViewHolder2.uploaderProfile.setImageResource(R.drawable.ic_account_circle);
                            }
                        } else {
                            //hide some controls
                            videoTypeViewHolder2.uploaderUsername.setText("(username)");
                            videoTypeViewHolder2.mRelative.setVisibility(View.GONE);
                        }

                        //delete feed
                        //check if isOnline
                        if (isOnline) {
                            //check if feed uploader id is the same as that of
                            //actively logged in account
                            if (feedInstance.getmUploaderId().equalsIgnoreCase(String.valueOf(methods.getUserId(mContext)))) {
                                //if they are the same, this means that
                                //the current active signed in account
                                //is the owner of that feed, show delete button
                                videoTypeViewHolder2.delete.setVisibility(View.VISIBLE);
                            } else {
                                //current account is not owner of feed
                                //hide delete button
                                videoTypeViewHolder2.delete.setVisibility(View.GONE);
                            }

                        } else {
                            //hide delete button
                            videoTypeViewHolder2.delete.setVisibility(View.GONE);
                        }

                        //get number of comments
                        if (isOnline) {
                            getNoOfComments(feedInstance.getmId(), videoTypeViewHolder2.viewComments);
                            //getNoOfComments(feedInstance.getmId(), videoTypeViewHolder2.noOfComments);
                        }
                        //check if liked feed before
                        //checkIfLikedFeedBefore(String.valueOf(methods.getUserId(mContext)),feedInstance.getmId(),videoTypeViewHolder2.like,videoTypeViewHolder2.unlike);

                        try {
                            if (isOnline) {
                                //view comments on click listener
                                videoTypeViewHolder2.viewComments.setOnClickListener(v13 -> {
                                    Intent comments = new Intent(mContext, FeedCommentsActivity.class);
                                    comments.putExtra("feed_id", feedInstance.getmId());
                                    mContext.startActivity(comments);
                                });
                                //ImageButton onclick events
                                //like
                                videoTypeViewHolder2.like.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //liking
                                    if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                        methods.showAlert("Can't like", "You cannot like your own activity", mContext);
                                    } else if (methods.checkAccountIsLogged(mContext)) {
                                        //like
                                        String likerid = String.valueOf(methods.getUserId(mContext));
                                        String feedid = feedInstance.getmId();
                                        String likertype;
                                        String uid;
                                        if (methods.isAdmin(mContext)) {
                                            likertype = "admin";
                                            uid = "admin" + likerid;
                                        } else {
                                            likertype = "user";
                                            uid = "user" + likerid;
                                        }
                                        String date = methods.getDateForSqlServer();

                                        //post the like to server
                                        likeFeed(likerid, likertype, date, uid, feedid, feedInstance.getmUploaderId(), position);
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can like.", mContext);
                                    }
                                });
                                //unlike
                                videoTypeViewHolder2.unlike.setOnClickListener(v12 -> {
                                    unLikeFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                });
                                //comment
                                videoTypeViewHolder2.comment.setOnClickListener(v12 -> {
                                    //check if the feed's user id is not the same as the person who is
                                    //comment
                                    //if (methods.getUserId(mContext) == Integer.parseInt(feedInstance.getmUploaderId())) {
                                    //    methods.showAlert("Can't comment", "You cannot comment your own activity", mContext);
                                    //} else
                                    if (methods.checkAccountIsLogged(mContext)) {
                                        //comment
                                        //show dialog
                                        try {
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
                                                try {
                                                    String likerid = String.valueOf(methods.getUserId(mContext));
                                                    String feedid = feedInstance.getmId();
                                                    String likertype;
                                                    String uid;
                                                    if (methods.isAdmin(mContext)) {
                                                        likertype = "admin";
                                                        uid = "admin" + likerid;
                                                    } else {
                                                        likertype = "user";
                                                        uid = "user" + likerid;
                                                    }
                                                    String date = methods.getDateForSqlServer();
                                                    String comment = mComment.getText().toString().trim();
                                                    if (comment.equalsIgnoreCase("") || comment == null) {
                                                        methods.showAlert("Invalid Comment", "Enter a comment.", mContext);
                                                    } else {
                                                        commentFeed(likerid, likertype, date, uid, feedid, comment, feedInstance.getmUploaderId());
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            mDialog.show();
                                        } catch (Exception e) {
                                            Toast.makeText(mContext, "Error showing dialog.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        //account needed
                                        methods.showAlert("Sign in required", "Sign first before you can comment.", mContext);
                                    }
                                });
                                //delete
                                videoTypeViewHolder2.delete.setOnClickListener(v -> {
                                    try {
                                        /*
                                         * prompt if user is sure to delete if yes then delete,
                                         * else do not delete
                                         */
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Delete feed?");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), String.valueOf(methods.getUserId(mContext)), position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    } catch (Exception e) {
                                        Toast.makeText(mContext, "Error raising event.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                //offline
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Error raising event", Toast.LENGTH_SHORT).show();
                        }

                        //videoTypeViewHolder2.mVideo.setVideoURI(Uri.parse(feedInstance.getmUrl()));
                        //MediaController mediaController2 = new MediaController(mContext);
                        //mediaController2.setAnchorView(((VideoTypeViewHolder) holder).mAnchor);
                        //videoTypeViewHolder2.mVideo.setMediaController(mediaController2);
                        //videoTypeViewHolder2.mVideo.seekTo(100);

                        //exo player code
                        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                        Uri uri;
                        exoAudioPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
                        uri = Uri.parse(feedInstance.getmUrl());
                        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
                        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                        MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
                        videoTypeViewHolder2.mExoVideo.setPlayer(exoAudioPlayer);
                        exoAudioPlayer.prepare(mediaSource);
                        exoAudioPlayer.setPlayWhenReady(false);
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
        RelativeLayout mRelative;

        CircleImageView uploaderProfile;
        TextView uploaderUsername;
        TextView noOfLikes;
        TextView noOfComments;
        TextView viewComments;
        ImageButton like;
        ImageButton unlike;
        ImageButton delete;
        ImageButton comment;

        public TextTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mContent = itemView.findViewById(R.id.feedTextView);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);

            this.uploaderProfile = itemView.findViewById(R.id.imageView);
            this.uploaderUsername = itemView.findViewById(R.id.username);
            this.noOfLikes = itemView.findViewById(R.id.no_of_likes);
            this.noOfComments = itemView.findViewById(R.id.no_of_comments);
            this.viewComments = itemView.findViewById(R.id.view_comments);
            this.like = itemView.findViewById(R.id.like);
            this.unlike = itemView.findViewById(R.id.unlike);
            this.comment = itemView.findViewById(R.id.comment);
            this.delete = itemView.findViewById(R.id.delete);
            this.mRelative = itemView.findViewById(R.id.relative_Layout);
        }
    }

    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView mDescription;
        TextView mDate;
        CardView mFeed;
        RelativeLayout mRelative;

        CircleImageView uploaderProfile;
        TextView uploaderUsername;
        TextView noOfLikes;
        TextView noOfComments;
        TextView viewComments;
        ImageButton like;
        ImageButton unlike;
        ImageButton comment;
        ImageButton delete;

        public ImageTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImage = itemView.findViewById(R.id.feedImageView);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);

            this.uploaderProfile = itemView.findViewById(R.id.imageView);
            this.uploaderUsername = itemView.findViewById(R.id.username);
            this.noOfLikes = itemView.findViewById(R.id.no_of_likes);
            this.noOfComments = itemView.findViewById(R.id.no_of_comments);
            this.viewComments = itemView.findViewById(R.id.view_comments);
            this.like = itemView.findViewById(R.id.like);
            this.unlike = itemView.findViewById(R.id.unlike);
            this.comment = itemView.findViewById(R.id.comment);
            this.delete = itemView.findViewById(R.id.delete);
            this.mRelative = itemView.findViewById(R.id.relative_Layout);
        }
    }

    public static class VideoTypeViewHolder extends RecyclerView.ViewHolder {

        VideoView mVideo;
        TextView mDescription;
        TextView mDate;
        CardView mFeed;
        LinearLayout mAnchor;
        RelativeLayout mRelative;

        CircleImageView uploaderProfile;
        TextView uploaderUsername;
        TextView noOfLikes;
        TextView noOfComments;
        TextView viewComments;
        ImageButton like;
        ImageButton unlike;
        ImageButton comment;
        ImageButton delete;
        private SimpleExoPlayerView mExoVideo;

        public VideoTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mVideo = itemView.findViewById(R.id.feedVideoView);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
            this.mAnchor = itemView.findViewById(R.id.videoView_Anchor);

            this.uploaderProfile = itemView.findViewById(R.id.imageView);
            this.uploaderUsername = itemView.findViewById(R.id.username);
            this.noOfLikes = itemView.findViewById(R.id.no_of_likes);
            this.noOfComments = itemView.findViewById(R.id.no_of_comments);
            this.viewComments = itemView.findViewById(R.id.view_comments);
            this.like = itemView.findViewById(R.id.like);
            this.unlike = itemView.findViewById(R.id.unlike);
            this.comment = itemView.findViewById(R.id.comment);
            this.delete = itemView.findViewById(R.id.delete);
            this.mRelative = itemView.findViewById(R.id.relative_Layout);
            this.mExoVideo = itemView.findViewById(R.id.libVideoView2);
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
            case 4:
                return AUDIO_TYPE;
            default:
                return -1;
        }
    }

    //release exo players
    public void releaseExoPlayer() {
        try {
            if (exoVideoPlayer != null) {
                exoVideoPlayer.release();
            }
            if (exoAudioPlayer != null) {
                exoAudioPlayer.release();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Error : could not release player.", Toast.LENGTH_SHORT).show();
        }
    }

    //method for liking a feed
    private void likeFeed(String likerid, String likertype, String dateliked, String uid, String feedid, String ownerid, int position) {
        try {
            RequestBody lid = RequestBody.create(MultipartBody.FORM, likerid);
            RequestBody ltyp = RequestBody.create(MultipartBody.FORM, likertype);
            RequestBody dtepost = RequestBody.create(MultipartBody.FORM, dateliked);
            RequestBody ide = RequestBody.create(MultipartBody.FORM, uid);
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);
            RequestBody oid = RequestBody.create(MultipartBody.FORM, ownerid);

            Call<ResponseBody> like = apiInterface.LikeFeed(lid, ltyp, dtepost, ide, fid, oid);
            methods.showDialog(mDialog, "Posting like...", true);
            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            methods.showAlert("Response", "Failed.Try again.", mContext);
                        } else if (message.equalsIgnoreCase("Success")) {
                            Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                            feed feed = mFeeds.get(position);
                            String num = String.valueOf(Integer.parseInt(feed.getNoOfLikes()) + 1);
                            feed.setNoOfLikes(num);
                            notifyItemChanged(position);
                        } else if (message.equalsIgnoreCase("Error")) {
                            Toast.makeText(mContext, "Server error.", Toast.LENGTH_SHORT).show();
                        } else if (message.equalsIgnoreCase("Exist")) {
                            Toast.makeText(mContext, "Exist.", Toast.LENGTH_SHORT).show(); //Snackbar.make(rootLayout, "You liked the same content before.", Snackbar.LENGTH_SHORT).show();
                        } else if (message.equalsIgnoreCase("Inactive Account")) {
                            methods.showAlert("Inactive Account", "Your account is currently deactivated.Contact your admin(s).", mContext);
                        }
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
    private void commentFeed(String commentorid, String commentortype, String datecommented, String uid, String feedid, String comment, String ownerid) {
        try {
            RequestBody lid = RequestBody.create(MultipartBody.FORM, commentorid);
            RequestBody ltyp = RequestBody.create(MultipartBody.FORM, commentortype);
            RequestBody dtepost = RequestBody.create(MultipartBody.FORM, datecommented);
            RequestBody ide = RequestBody.create(MultipartBody.FORM, uid);
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);
            RequestBody comm = RequestBody.create(MultipartBody.FORM, comment);
            RequestBody oid = RequestBody.create(MultipartBody.FORM, ownerid);

            Call<ResponseBody> like = apiInterface.CommentFeed(lid, ltyp, dtepost, ide, fid, comm, oid);
            methods.showDialog(mDialog, "Posting comment...", true);
            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Failed")) {
                            Toast.makeText(mContext, "Upload failed.Try again.", Toast.LENGTH_SHORT).show();
                            //Snackbar.make(rootLayout, "Upload failed.Try again.", Snackbar.LENGTH_SHORT).show();
                        } else if (message.equalsIgnoreCase("Success")) {
                            Toast.makeText(mContext, "Success.", Toast.LENGTH_SHORT).show();
                            //Snackbar.make(rootLayout, "Success.", Snackbar.LENGTH_SHORT).show();
                        } else if (message.equalsIgnoreCase("Error")) {
                            Toast.makeText(mContext, "Server error.", Toast.LENGTH_SHORT).show();
                            //Snackbar.make(rootLayout, "Server error.", Snackbar.LENGTH_SHORT).show();
                        } else if (message.equalsIgnoreCase("Inactive Account")) {
                            methods.showAlert("Inactive Account", "Your account is currently deactivated.Contact your admin(s).", mContext);
                        }

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

    //method for adding more feeds
    public void addFeed(List<feed> list) {
        try {
            for (feed f : list) {
                mFeeds.add(f);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(mContext, "Add feed error in adapter", Toast.LENGTH_SHORT).show();
        }
    }

    //get no of comments on feed
    public void getNoOfComments(String feedid, TextView textView) {
        try {
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);
            Call<ResponseBody> getCommentsNo = apiInterface.GetNoOfComments(fid);

            getCommentsNo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String result = response.body().string();
                        String message = methods.removeQoutes(result);
                        if (message.equalsIgnoreCase("Error")) {

                        } else {
                            int c = Integer.parseInt(message);
                            if (c > 1) {
                                textView.setText("View all " + message + " comments");
                            } else if (c == 1) {
                                textView.setText("View " + message + " comment");
                            } else {
                                textView.setText(message + " comments");
                            }
                        }

                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {

                    } catch (Exception e) {

                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Get comments error", Toast.LENGTH_SHORT).show();
        }
    }

    //unlike a feed
    public void unLikeFeed(String feedid, String likerid, int position) {
        try {
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);
            RequestBody lid = RequestBody.create(MultipartBody.FORM, likerid);
            String acc_type;
            if (methods.isAdmin(mContext)) {
                acc_type = "admin";
            } else {
                acc_type = "user";
            }
            RequestBody u_type = RequestBody.create(MultipartBody.FORM, acc_type);

            Call<ResponseBody> like = apiInterface.UnlikeFeed(fid, lid, u_type);
            //methods.showDialog(mDialog, "Posting like...", true);
            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Error")) {
                            Toast.makeText(mContext, "Failed.Try again.", Toast.LENGTH_SHORT).show();
                        } else if (message.equalsIgnoreCase("Success")) {
                            feed feed = mFeeds.get(position);
                            String num = String.valueOf(Integer.parseInt(feed.getNoOfLikes()) - 1);
                            feed.setNoOfLikes(num);
                            notifyItemChanged(position);
                        } else if (message.equalsIgnoreCase("Inactive Account")) {
                            methods.showAlert("Inactive Account", "Your account is currently deactivated.Contact your admin(s).", mContext);
                        } else if(message.equalsIgnoreCase("Like not found")){
                            Toast.makeText(mContext, "You haven't liked this before.", Toast.LENGTH_SHORT).show();
                        }
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
            Toast.makeText(mContext, "Un like feed error.", Toast.LENGTH_SHORT).show();
        }
    }

    //check if current user has liked a feed before
    public void checkIfLikedFeedBefore(String userid, String feedid) {

        try {
            RequestBody usrid = RequestBody.create(MultipartBody.FORM, userid);
            RequestBody fid = RequestBody.create(MultipartBody.FORM, feedid);

            String acc_type;
            if (methods.isAdmin(mContext)) {
                acc_type = "admin";
            } else {
                acc_type = "user";
            }
            RequestBody u_type = RequestBody.create(MultipartBody.FORM, acc_type);

            Call<ResponseBody> like = apiInterface.CheckLikeFeed(usrid, fid, u_type);

            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        //methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Error")) {

                        } else if (message.equalsIgnoreCase("Yes")) {

                        } else if (message.equalsIgnoreCase("No")) {

                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        //methods.showDialog(mDialog, "dismiss", false);
                        //methods.showAlert("Request failed", "Request failed " + t.toString(), mContext);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Un like feed error.", Toast.LENGTH_SHORT).show();
        }
    }

    //delete feed
    public void deleteFeed(String feed_id, String user_id, int position) {
        try {
            RequestBody f_id = RequestBody.create(MultipartBody.FORM, feed_id);
            RequestBody u_id = RequestBody.create(MultipartBody.FORM, user_id);

            Call<ResponseBody> like = apiInterface.DeleteMyFeed(f_id, u_id);
            methods.showDialog(mDialog, "Deleting...", true);
            like.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        String result = response.body().string();

                        String message = methods.removeQoutes(result);

                        if (message.equalsIgnoreCase("Error")) {
                            methods.showAlert("Response", "Failed.Try again.", mContext);
                        } else if (message.equalsIgnoreCase("Success")) {
                            notifyItemRemoved(position);
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
            Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
