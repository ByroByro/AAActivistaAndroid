package com.example.actionaidactivista.adapters;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.R;;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.feed;
import com.example.actionaidactivista.models.library_article;
import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class library_adapter extends RecyclerView.Adapter<library_adapter.ViewHolder> {

    private Context mContext;
    private List<library_article> mArticles;
    private List<library_article> filtered_Articles;
    //retrofit
    private ApiInterface apiInterface;
    private Dialog mDialog;
    private Handler mHandler;

    //article download variables
    private long mDownloadId;

    public static final int TEXT_TYPE = 0;//text
    public static final int IMAGE_TYPE = 1;//pictures
    public static final int VIDEO_TYPE = 2;//videos
    public static final int DOC_TYPE = 3;//documents - word,excel etc
    public static final int AUD_TYPE = 4;//audios

    public library_adapter(List<library_article> list, Context ctx) {
        this.mArticles = list;
        this.mContext = ctx;
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mDialog = new Dialog(ctx);
        this.mHandler = new Handler();
        this.filtered_Articles = new ArrayList<>(list);
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
                            || (item.getmAuthor() != null && item.getmAuthor().toLowerCase().contains(constraint)|| (item.getmFileType() != null && item.getmFileType().toLowerCase().contains(constraint))))) {
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.library_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new library_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            library_article article = mArticles.get(position);
            holder.mTitle.setText("Title : " + article.getmTitle());
            holder.mAuthor.setText("Author : " + article.getmAuthor());
            holder.mLibCardView.setOnClickListener(v -> {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Title : " + article.getmTitle());
                stringBuffer.append("\nAuthor : " + article.getmAuthor());
                stringBuffer.append("\nDate posted : " + article.getmDate());
                methods.showAlert("\nArticle Details", stringBuffer.toString(), mContext);
            });
            holder.mDownload.setOnClickListener(v -> {
                try {
                    Toast.makeText(mContext, "Downloading...", Toast.LENGTH_SHORT).show();
                    //downloadArticle(article.getmUrl());
                    downloadArticleRetrofit(article.getmUrl());
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
        return mArticles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mAuthor;
        public TextView mTitle;
        public Button mDownload;
        public CardView mLibCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuthor = itemView.findViewById(R.id.author);
            mTitle = itemView.findViewById(R.id.title);
            mDownload = itemView.findViewById(R.id.download);
            mLibCardView = itemView.findViewById(R.id.library_card_view);
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
            methods.showDialog(mDialog, "Uploading material...", true);
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
}
