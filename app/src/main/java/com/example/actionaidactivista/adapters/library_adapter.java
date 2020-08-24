package com.example.actionaidactivista.adapters;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actionaidactivista.PreviewOnlineDocActivity;
import com.example.actionaidactivista.R;;
import com.example.actionaidactivista.logic.DownloadProgress;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.models.library_article;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class library_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<library_article> mArticles;
    private List<library_article> filtered_Articles;
    //retrofit
    private ApiInterface apiInterface;
    private Dialog mDialog;
    private Handler mHandler;
    private SimpleExoPlayer exoVideoPlayer;
    private SimpleExoPlayer exoAudioPlayer;
    private Dialog progressDialog;
    private DownloadProgress downloadProgress;
    private DownloadTask downloadTask;

    //article download variables
    private long mDownloadId;

    public static final int TEXT_TYPE = 0;//text
    public static final int IMAGE_TYPE = 1;//pictures
    public static final int VIDEO_TYPE = 2;//videos
    public static final int DOC_TYPE = 3;//documents - word,excel etc
    public static final int AUDIO_TYPE = 4;//audios

    public library_adapter(List<library_article> list, Context ctx) {
        this.mArticles = list;
        this.mContext = ctx;
        this.apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        this.mDialog = new Dialog(ctx);
        this.mHandler = new Handler();
        this.filtered_Articles = new ArrayList<>(list);
        this.progressDialog = new Dialog(mContext);
        this.downloadProgress = new DownloadProgress(progressDialog, mContext);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<library_article> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_Articles);
            } else {
                for (library_article item : filtered_Articles) {
                    if ((item.getmTitle() != null && item.getmTitle().toLowerCase().contains(constraint)) || (item.getmDate() != null && item.getmDate().toLowerCase().contains(constraint)
                            || (item.getmAuthor() != null && item.getmAuthor().toLowerCase().contains(constraint) || (item.getmFileType() != null && item.getmFileType().toLowerCase().contains(constraint))))) {
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
            mArticles.clear();
            mArticles.addAll((Collection<? extends library_article>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            if (viewType == DOC_TYPE) {
                view = LayoutInflater.from(mContext).inflate(R.layout.library_row_item, parent, false);
                //return new DocumentTypeViewHolder(view);
            } else if (viewType == IMAGE_TYPE) {
                view = LayoutInflater.from(mContext).inflate(R.layout.lib_image_feed, parent, false);
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.lib_video_feed, parent, false);
                //return new VideoTypeViewHolder(view);
            }
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), mContext);
        }
        if (viewType == DOC_TYPE) {
            return new DocumentTypeViewHolder(view);
        } else if (viewType == IMAGE_TYPE) {
            return new ImageTypeViewHolder(view);
        } else {
            return new VideoTypeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            library_article article = mArticles.get(position);
            if (article != null) {
                switch (holder.getItemViewType()) {
                    case DOC_TYPE:
                        DocumentTypeViewHolder doc = (DocumentTypeViewHolder) holder;
                        doc.mTitle.setText("Title : " + article.getmTitle());
                        doc.mAuthor.setText("Author : " + article.getmAuthor());
                        doc.mLibCardView.setOnClickListener(v -> {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append("Title : " + article.getmTitle());
                            stringBuffer.append("\nAuthor : " + article.getmAuthor());
                            stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                            stringBuffer.append("\nArticle type : " + article.getmFileType());
                            methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                        });
                        doc.mDetails.setOnClickListener(v -> {
                            try {
                                // details card on click
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Title : " + article.getmTitle());
                                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                                stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                                stringBuffer.append("\nArticle type : " + article.getmFileType());
                                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        doc.mDownload.setOnClickListener(v -> {
                            try {
                                Toast.makeText(mContext, "Downloading...", Toast.LENGTH_SHORT).show();
                                //downloadArticle(article.getmUrl());
                                //downloadArticleRetrofit(article.getmUrl());
                                downloadTask = new DownloadTask(mContext);
                                downloadTask.execute(article.getmUrl());
                            } catch (Exception e) {
                                Toast.makeText(mContext, "Error raising download event.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        doc.mPreview.setOnClickListener(v -> {
                            try {
//                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
//                                browserIntent.setDataAndType(Uri.parse(article.getmUrl()), "application/pdf");
//                                Intent chooser = Intent.createChooser(browserIntent, "Open with");
//                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // optional
//                                mContext.startActivity(chooser);

                                Intent intent = new Intent(mContext, PreviewOnlineDocActivity.class);
                                intent.putExtra("url", article.getmUrl());
                                intent.putExtra("type", "pdf");
                                intent.putExtra("mode", "library");
                                mContext.startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(mContext, "Error trying preview.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case VIDEO_TYPE:
                        VideoTypeViewHolder video = (VideoTypeViewHolder) holder;
                        video.mTitle.setText("Title : " + article.getmTitle());
                        video.mAuthor.setText("Author" + article.getmAuthor());
                        video.mCard.setOnClickListener(v -> {
                            try {
                                // details card on click
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Title : " + article.getmTitle());
                                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                                stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                                stringBuffer.append("\nArticle type : " + article.getmFileType());
                                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        video.mDetails.setOnClickListener(v -> {
                            try {
                                // details card on click
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Title : " + article.getmTitle());
                                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                                stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                                stringBuffer.append("\nArticle type : " + article.getmFileType());
                                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        video.mDownload.setOnClickListener(v -> {
                            try {
                                // download button on click
                                //downloadArticleRetrofit(article.getmUrl());
                                downloadTask = new DownloadTask(mContext);
                                downloadTask.execute(article.getmUrl());
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        //video.mVideo.setVideoURI(Uri.parse(article.getmUrl()));
                        //MediaController mediaController = new MediaController(mContext);
                        //mediaController.setAnchorView(((library_adapter.VideoTypeViewHolder) holder).mAnchor);
                        //video.mVideo.setMediaController(mediaController);
                        //video.mVideo.seekTo(100);

                        //exo player code
                        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
                        Uri uri;
                        exoVideoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
                        uri = Uri.parse(article.getmUrl());
                        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
                        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                        MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
                        video.mExoVideo.setPlayer(exoVideoPlayer);
                        exoVideoPlayer.prepare(mediaSource);
                        exoVideoPlayer.setPlayWhenReady(false);

                        break;
                    case AUDIO_TYPE:
                        VideoTypeViewHolder audio = (VideoTypeViewHolder) holder;
                        audio.mTitle.setText("Title : " + article.getmTitle());
                        audio.mAuthor.setText("Author : " + article.getmAuthor());
                        audio.mCard.setOnClickListener(v -> {
                            try {
                                // details card on click
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Title : " + article.getmTitle());
                                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                                stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                                stringBuffer.append("\nArticle type : " + article.getmFileType());
                                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        audio.mDetails.setOnClickListener(v -> {
                            try {
                                // details card on click
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Title : " + article.getmTitle());
                                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                                stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                                stringBuffer.append("\nArticle type : " + article.getmFileType());
                                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        audio.mDownload.setOnClickListener(v -> {
                            try {
                                // download button on click
                                //downloadArticleRetrofit(article.getmUrl());
                                downloadTask = new DownloadTask(mContext);
                                downloadTask.execute(article.getmUrl());
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });

                        //audio.mVideo.setVideoURI(Uri.parse(article.getmUrl()));
                        //MediaController mediaController2 = new MediaController(mContext);
                        //mediaController2.setAnchorView(((library_adapter.VideoTypeViewHolder) holder).mAnchor);
                        //audio.mVideo.setMediaController(mediaController2);
                        //audio.mVideo.seekTo(100);

                        //exo player code
                        BandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
                        TrackSelector trackSelector2 = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter2));
                        Uri uri2;
                        exoAudioPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector2);
                        uri2 = Uri.parse(article.getmUrl());
                        DefaultHttpDataSourceFactory dataSourceFactory2 = new DefaultHttpDataSourceFactory("exoplayer_video");
                        ExtractorsFactory extractorsFactory2 = new DefaultExtractorsFactory();
                        MediaSource mediaSource2 = new ExtractorMediaSource(uri2, dataSourceFactory2, extractorsFactory2, null, null);
                        audio.mExoVideo.setPlayer(exoAudioPlayer);
                        exoAudioPlayer.prepare(mediaSource2);
                        exoAudioPlayer.setPlayWhenReady(false);
                        break;
                    case IMAGE_TYPE:
                        ImageTypeViewHolder image = (ImageTypeViewHolder) holder;
                        image.mTitle.setText("Title : " + article.getmTitle());
                        image.mAuthor.setText("Author : " + article.getmAuthor());
                        image.mLibCardView.setOnClickListener(v -> {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append("Title : " + article.getmTitle());
                            stringBuffer.append("\nAuthor : " + article.getmAuthor());
                            stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                            stringBuffer.append("\nArticle type : " + article.getmFileType());
                            methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                        });
                        image.mDetails.setOnClickListener(v -> {
                            try {
                                // details card on click
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("Title : " + article.getmTitle());
                                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                                stringBuffer.append("\nDate posted : " + methods.getReadableDate(article.getmDate(), mContext));
                                stringBuffer.append("\nArticle type : " + article.getmFileType());
                                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                        image.mDownload.setOnClickListener(v -> {
                            try {
                                Toast.makeText(mContext, "Downloading...", Toast.LENGTH_SHORT).show();
                                //downloadArticle(article.getmUrl());
                                //downloadArticleRetrofit(article.getmUrl());
                                downloadTask = new DownloadTask(mContext);
                                downloadTask.execute(article.getmUrl());
                            } catch (Exception e) {
                                Toast.makeText(mContext, "Error raising download event.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Glide.with(mContext)
                                .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_contacts_red))
                                .load(article.getmUrl())
                                .into(image.mImageView);
                        break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Error binding view holder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    @Override
    public int getItemViewType(int position) {

        switch (Integer.parseInt(mArticles.get(position).getmIntType())) {
            case 0:
                return TEXT_TYPE;
            case 1:
                return IMAGE_TYPE;
            case 2:
                return VIDEO_TYPE;
            case 3:
                return DOC_TYPE;
            case 4:
                return AUDIO_TYPE;
            default:
                return -1;
        }
    }

    public class DocumentTypeViewHolder extends RecyclerView.ViewHolder {

        //this view holder is for doc type

        public TextView mAuthor;
        public TextView mTitle;
        public ImageButton mDownload;
        public CardView mLibCardView;
        public Button mDetails;
        public Button mPreview;

        public DocumentTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuthor = itemView.findViewById(R.id.author);
            mTitle = itemView.findViewById(R.id.title);
            mDownload = itemView.findViewById(R.id.download);
            mLibCardView = itemView.findViewById(R.id.library_card_view);
            this.mDetails = itemView.findViewById(R.id.details);
            mPreview = itemView.findViewById(R.id.preview);
            mPreview.setVisibility(View.GONE);
        }
    }

    public class VideoTypeViewHolder extends RecyclerView.ViewHolder {

        VideoView mVideo;
        private SimpleExoPlayerView mExoVideo;
        TextView mTitle;
        TextView mAuthor;
        CardView mCard;
        LinearLayout mAnchor;
        ImageButton mDownload;
        Button mDetails;

        public VideoTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mVideo = itemView.findViewById(R.id.libVideoView);
            this.mExoVideo = itemView.findViewById(R.id.libVideoView2);
            this.mTitle = itemView.findViewById(R.id.title);
            this.mAuthor = itemView.findViewById(R.id.author);
            this.mCard = itemView.findViewById(R.id.detail_card);
            this.mAnchor = itemView.findViewById(R.id.videoView_Anchor);
            this.mDownload = itemView.findViewById(R.id.download);
            this.mDetails = itemView.findViewById(R.id.details);
        }
    }

    public class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        //this view holder is for doc type

        public TextView mAuthor;
        public TextView mTitle;
        public ImageButton mDownload;
        public CardView mLibCardView;
        public Button mDetails;
        public ImageView mImageView;

        public ImageTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuthor = itemView.findViewById(R.id.author);
            mTitle = itemView.findViewById(R.id.title);
            mDownload = itemView.findViewById(R.id.download);
            mLibCardView = itemView.findViewById(R.id.detail_card);
            this.mDetails = itemView.findViewById(R.id.details);
            mImageView = itemView.findViewById(R.id.imageView);
        }
    }

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

    /*
     * performs article download operation - download manager
     */
    private void downloadArticle(String url) {
        try {

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + mContext.getString(R.string.base_dir) + mContext.getString(R.string.downloads));
            if (!file.exists()) {
                file.mkdirs();
            }
            String filename = methods.getFileNameFromUrl(url, mContext);
            int dot = filename.lastIndexOf(".");
            String extension = filename.substring(dot + 1);
            File finalFile = new File(file + "/" + filename);

//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
//                    .setTitle(filename)
//                    .setDescription("Downloading...")
//                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                    .setDestinationUri(Uri.fromFile(finalFile))
//                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
//                    .setAllowedOverMetered(true)
//                    .setAllowedOverRoaming(true);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                    .setTitle(filename)
                    .setDescription("Downloading...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(Uri.fromFile(finalFile));
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            mDownloadId = downloadManager.enqueue(request);

        } catch (Exception e) {
            Toast.makeText(mContext, "Error raising download event.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * performs article download operation - retrofit2 and async task
     */
    private void downloadArticleRetrofit(String url) {
        try {
            Call<ResponseBody> download = apiInterface.downloadArticle(url);
            methods.showDialog(mDialog, "Downloading material...", true);
            download.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... voids) {
                                    writeFileToDisk(response.body(), url);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    methods.showDialog(mDialog, "dismiss", false);
                                    methods.showAlert("Download Result", "Download finished", mContext);
                                }
                            }.execute();
                        } else {
                            Toast.makeText(mContext, "Request failed.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "Error raising download event.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean writeFileToDisk(ResponseBody body, String url) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + mContext.getString(R.string.base_dir) + mContext.getString(R.string.downloads));
            if (!file.exists()) {
                file.mkdirs();
            }
            String filename = methods.getFileNameFromUrl(url, mContext);
            int dot = filename.lastIndexOf(".");
            //String extension = filename.substring(dot + 1);
            File finalFile = new File(file + "/" + filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(finalFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;

                    long finalFileSizeDownloaded = fileSizeDownloaded;
                    mHandler.post(() -> {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showDialog(mDialog, "Downloading... " + String.valueOf(finalFileSizeDownloaded) + " of " + String.valueOf(fileSize), true);
                    });
                }
                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * download task using AsyncTask
     */
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        //pre-execute
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //take CPU lock to prevent CPU from going off if the user
            //presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            //show dialog
            downloadProgress.showDialog(downloadTask);
        }

        //progress update
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //if we get here length is known
            downloadProgress.updateProgress(values[0]);
        }

        //post execute
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mWakeLock.release();
            downloadProgress.dismissDialog();
            if (s != null) {
                methods.showAlert("Download error", s, mContext);
            } else {
                Toast.makeText(mContext, "Download finished", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }
                int fileLength = connection.getContentLength();
                inputStream = connection.getInputStream();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + mContext.getString(R.string.base_dir) + mContext.getString(R.string.downloads));
                if (!file.exists()) {
                    file.mkdirs();
                }
                String filename = methods.getFileNameFromUrl(strings[0], mContext);
                File finalFile = new File(file + "/" + filename);

                outputStream = new FileOutputStream(finalFile);
                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = inputStream.read(data)) != -1) {
                    //allow cancelling
                    if (isCancelled()) {
                        inputStream.close();
                        return null;
                    }
                    //Thread.sleep(500);
                    total += count;
                    //publishing progress
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    outputStream.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                //close output stream
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //close input stream
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //disconnect connection
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }
    }

    //method for adding more members
    public void addArticle(List<library_article> list) {
        try {
            for (library_article f : list) {
                mArticles.add(f);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(mContext, "Add feed error in adapter", Toast.LENGTH_SHORT).show();
        }
    }
}
