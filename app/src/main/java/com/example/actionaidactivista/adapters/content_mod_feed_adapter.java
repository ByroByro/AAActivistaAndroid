package com.example.actionaidactivista.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.methods;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class content_mod_feed_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<feed> mFeeds;
    private Context mContext;
    private int mTotalTypes;
    private List<feed> filtered_Feeds;

    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;

    public static final int TEXT_TYPE = 0;//text
    public static final int IMAGE_TYPE = 1;//image
    public static final int VIDEO_TYPE = 2;//video
    public static final int AUDIO_TYPE = 4;//audio

    private SimpleExoPlayer exoVideoPlayer;
    private SimpleExoPlayer exoAudioPlayer;

    public content_mod_feed_adapter(ArrayList<feed> feeds, Context ctx) {
        this.mFeeds = feeds;
        this.mContext = ctx;
        this.mTotalTypes = mFeeds.size();
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
        this.filtered_Feeds = new ArrayList<>(feeds);
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_mod_text_feed, parent, false);
            return new TextTypeViewHolder(view);
        } else if (viewType == IMAGE_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_mod_image_feed, parent, false);
            return new ImageTypeViewHolder(view);
        } else if (viewType == AUDIO_TYPE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_mod_video_feed, parent, false);
            return new VideoTypeViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_mod_video_feed, parent, false);
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
                        //set card view on click listener
                        textTypeViewHolder.mMore.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((TextTypeViewHolder) holder).mFeed, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to approve this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "approve");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("no")) {
                                            methods.showAlert("Already Disapproved", "This content is already disapproved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to disapprove this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "disapprove");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Confirm action");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), feedInstance.getmPath(), "no",position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                    return false;
                                });
                                popup.show();
                            } catch (Exception e) {
                                System.out.println(e);
                                Toast.makeText(mContext, "Error raising card click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case IMAGE_TYPE:
                        ImageTypeViewHolder imageTypeViewHolder = (ImageTypeViewHolder) holder;
                        imageTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        imageTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
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
                        //set card view on click listener
                        imageTypeViewHolder.mMore.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((ImageTypeViewHolder) holder).mFeed, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to approve this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "approve");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("no")) {
                                            methods.showAlert("Already disapproved", "This content is already disapproved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to disapprove this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "disapprove");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Confirm action");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), feedInstance.getmPath(), "yes",position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                    return false;
                                });
                                popup.show();
                            } catch (Exception e) {
                                System.out.println(e);
                                Toast.makeText(mContext, "Error raising card click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case VIDEO_TYPE:
                        VideoTypeViewHolder videoTypeViewHolder = (VideoTypeViewHolder) holder;
                        videoTypeViewHolder.mDescription.setText(feedInstance.getmDescription());
                        videoTypeViewHolder.mDate.setText(methods.getReadableDate(feedInstance.getmDate(), mContext) + " : " + feedInstance.getmLocation());
                        //videoTypeViewHolder.mVideo.setVideoURI(Uri.parse(feedInstance.getmUrl()));
                        //MediaController mediaController = new MediaController(mContext);
                        //mediaController.setAnchorView(((VideoTypeViewHolder) holder).mVideo);
                        //videoTypeViewHolder.mVideo.setMediaController(mediaController);
                        //videoTypeViewHolder.mVideo.seekTo(100);

                        //set card view on click listener
                        videoTypeViewHolder.mMore.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((VideoTypeViewHolder) holder).mFeed, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to approve this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "approve");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("no")) {
                                            methods.showAlert("Already disapproved", "This content is already disapproved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to disapprove this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "disapprove");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Confirm action");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), feedInstance.getmPath(), "yes",position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                    return false;
                                });
                                popup.show();
                            } catch (Exception e) {
                                System.out.println(e);
                                Toast.makeText(mContext, "Error raising card click", Toast.LENGTH_SHORT).show();
                            }
                        });
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
                        //videoTypeViewHolder2.mVideo.setVideoURI(Uri.parse(feedInstance.getmUrl()));
                        //MediaController mediaController2 = new MediaController(mContext);
                        //mediaController2.setAnchorView(((VideoTypeViewHolder) holder).mVideo);
                        //videoTypeViewHolder2.mVideo.setMediaController(mediaController2);
                        //videoTypeViewHolder2.mVideo.seekTo(100);

                        //set card view on click listener
                        videoTypeViewHolder2.mMore.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((VideoTypeViewHolder) holder).mFeed, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to approve this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "approve");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already disapproved", "This content is already disapproved", mContext);
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                            builder.setTitle("Confirm action");
                                            builder.setMessage("Are you sure to disapprove this feed.");
                                            //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                            //set dialog to be cancelled only by buttons
                                            builder.setCancelable(false);

                                            //set dismiss button
                                            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                            //set positive button
                                            builder.setPositiveButton("Yes", (dialog, which) -> {
                                                try {
                                                    feedActions(feedInstance.getmId(), "disapprove");
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                            });

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Confirm action");
                                        builder.setMessage("Are you sure to delete this feed.");
                                        //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                        //set dialog to be cancelled only by buttons
                                        builder.setCancelable(false);

                                        //set dismiss button
                                        builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());

                                        //set positive button
                                        builder.setPositiveButton("Yes", (dialog, which) -> {
                                            try {
                                                deleteFeed(feedInstance.getmId(), feedInstance.getmPath(), "yes",position);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        });

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                    return false;
                                });
                                popup.show();
                            } catch (Exception e) {
                                System.out.println(e);
                                Toast.makeText(mContext, "Error raising card click", Toast.LENGTH_SHORT).show();
                            }
                        });
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
        CardView mFeed;
        TextView mDescription;
        TextView mDate;
        ImageButton mMore;

        public TextTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mContent = itemView.findViewById(R.id.feedTextView);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mMore = itemView.findViewById(R.id.more);
        }
    }

    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        CardView mFeed;
        TextView mDescription;
        TextView mDate;
        ImageButton mMore;

        public ImageTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImage = itemView.findViewById(R.id.feedImageView);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mMore = itemView.findViewById(R.id.more);
        }
    }

    public static class VideoTypeViewHolder extends RecyclerView.ViewHolder {

        VideoView mVideo;
        TextView mDescription;
        TextView mDate;
        CardView mFeed;
        LinearLayout mAnchor;
        ImageButton mMore;
        private SimpleExoPlayerView mExoVideo;

        public VideoTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mVideo = itemView.findViewById(R.id.feedVideoView);
            this.mFeed = itemView.findViewById(R.id.feed_detail_card);
            this.mDescription = itemView.findViewById(R.id.feed_des);
            this.mDate = itemView.findViewById(R.id.feed_date);
            this.mAnchor = itemView.findViewById(R.id.videoView_Anchor);
            this.mMore = itemView.findViewById(R.id.more);
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

    /*
     * performs approval of feeds
     */
    private void feedActions(String feedId, String action) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, feedId);
            RequestBody op = RequestBody.create(MultipartBody.FORM, action);
            Call<ResponseBody> approve = apiInterface.ApproveFeed(id, op);
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

    /*
     * delete feed
     */
    private void deleteFeed(String feedId, String path, String type,int position) {
        try {
            RequestBody id = RequestBody.create(MultipartBody.FORM, feedId);
            RequestBody pth = RequestBody.create(MultipartBody.FORM, path);
            RequestBody typ = RequestBody.create(MultipartBody.FORM, type);
            Call<ResponseBody> delete = apiInterface.DeleteFeed(id, pth, typ);
            methods.showDialog(mDialog, "Processing request operation...", true);
            delete.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        methods.showDialog(mDialog, "Dismiss", false);
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            //JsonParser parser = new JsonParser();
                            //String result = parser.parse(responseData).getAsString();
                            String result = methods.removeQoutes(responseData);
                            if (result.equalsIgnoreCase("Ok")) {
                                methods.showAlert("Result", "Operation success.", mContext);
                                notifyItemRemoved(position);
                            } else if (result.equalsIgnoreCase("Failed")) {
                                methods.showAlert("Result", "Operation failed.Try later.", mContext);
                            } else if (result.equalsIgnoreCase("Error")) {
                                methods.showAlert("Result", "Server error.", mContext);
                            } else if (result.equalsIgnoreCase("Bad")) {
                                methods.showAlert("Result", "Failed to delete.", mContext);
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
            Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //release exo players
    public void releaseExoPlayer(){
        try{
            if(exoVideoPlayer != null){
                exoVideoPlayer.release();
            }
            if(exoAudioPlayer != null){
                exoAudioPlayer.release();
            }
        }catch (Exception e){
            Toast.makeText(mContext,"Error : could not release player.",Toast.LENGTH_SHORT).show();
        }
    }
}
