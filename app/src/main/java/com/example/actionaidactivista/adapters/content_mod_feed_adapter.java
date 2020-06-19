package com.example.actionaidactivista.adapters;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
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

    //retrofit
    private ApiInterface apiInterface;

    private Dialog mDialog;

    public static final int TEXT_TYPE = 0;//text
    public static final int IMAGE_TYPE = 1;//image
    public static final int VIDEO_TYPE = 2;//video

    public content_mod_feed_adapter(ArrayList<feed> feeds, Context ctx) {
        this.mFeeds = feeds;
        this.mContext = ctx;
        this.mTotalTypes = mFeeds.size();
        //initialise api interface
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(mContext);
    }

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
                        //set card view on click listener
                        textTypeViewHolder.mTextCard.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((TextTypeViewHolder) holder).mTextCard, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            feedActions(feedInstance.getmId(), "approve");
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("no")) {
                                            methods.showAlert("Already Approved", "This content is already disapproved", mContext);
                                        } else {
                                            feedActions(feedInstance.getmId(), "disapprove");
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {

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
                        imageTypeViewHolder.mImageCard.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((ImageTypeViewHolder) holder).mImageCard, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            feedActions(feedInstance.getmId(), "approve");
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("no")) {
                                            methods.showAlert("Already Approved", "This content is already disapproved", mContext);
                                        } else {
                                            feedActions(feedInstance.getmId(), "disapprove");
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {

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
                        videoTypeViewHolder.mVideo.setVideoURI(Uri.parse(feedInstance.getmUrl()));
                        MediaController mediaController = new MediaController(mContext);
                        mediaController.setAnchorView(((VideoTypeViewHolder) holder).mVideo);
                        videoTypeViewHolder.mVideo.setMediaController(mediaController);
                        videoTypeViewHolder.mVideo.seekTo(100);

                        //set card view on click listener
                        videoTypeViewHolder.mVidCard.setOnClickListener(v -> {
                            try {
                                //Creating the instance of PopupMenu
                                final PopupMenu popup = new PopupMenu(mContext, ((VideoTypeViewHolder) holder).mVidCard, Gravity.CENTER);
                                //Inflating the Popup using xml file
                                popup.getMenuInflater().inflate(R.menu.content_mod_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    if (item.getItemId() == R.id.action_approve) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("yes")) {
                                            methods.showAlert("Already Approved", "This content is already approved", mContext);
                                        } else {
                                            feedActions(feedInstance.getmId(), "approve");
                                        }
                                    } else if (item.getItemId() == R.id.action_disapprove) {
                                        if (feedInstance.getmStatus().equalsIgnoreCase("no")) {
                                            methods.showAlert("Already Approved", "This content is already disapproved", mContext);
                                        } else {
                                            feedActions(feedInstance.getmId(), "disapprove");
                                        }
                                    } else if (item.getItemId() == R.id.action_delete) {

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
        CardView mTextCard;

        public TextTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mContent = itemView.findViewById(R.id.feedTextView);
            this.mTextCard = itemView.findViewById(R.id.text_type_card_view);
        }
    }

    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        ImageView mImage;
        CardView mImageCard;

        public ImageTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImage = itemView.findViewById(R.id.feedImageView);
            this.mImageCard = itemView.findViewById(R.id.image_type_card_view);
        }
    }

    public static class VideoTypeViewHolder extends RecyclerView.ViewHolder {

        VideoView mVideo;
        CardView mVidCard;

        public VideoTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mVideo = itemView.findViewById(R.id.feedVideoView);
            this.mVidCard = itemView.findViewById(R.id.video_type_card_view);
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
}
