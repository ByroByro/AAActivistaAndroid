package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.example.actionaidactivista.retrofit.ApiClient;
import com.example.actionaidactivista.retrofit.ApiInterface;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreviewDocActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    PDFView pdfView;
    private int pageNumber;
    private String fileName;
    private InputStream inputStream;
    private String url;
    Dialog mDialog;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set content view
            setContentView(R.layout.activity_preview_doc);

            //init widgets
            pdfView = (PDFView) findViewById(R.id.pdfView);
            mDialog = new Dialog(this);
            //init api interface
            apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

            //get extra data
            Intent intent = getIntent();
            url = intent.getStringExtra("url");
            fileName = methods.getFileNameFromUrl(url, this);

            //set title
            try {
                getSupportActionBar().setTitle(fileName);
            } catch (Exception e) {
                Toast.makeText(this, "Could not set title", Toast.LENGTH_SHORT).show();
            }

            //download file from url
            //downloadFile(url);

            //load pdf
            displayDocument();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    public void displayDocument() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.base_dir) + "/Downloads/licenseguide.pdf");

            pdfView.fromFile(file)
                    .defaultPage(pageNumber)
                    .enableAnnotationRendering(true)
                    .onPageChange(this)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .onPageError(this)
                    .load();
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), this);
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {
            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        try {
            getSupportActionBar().setTitle(String.format("%s %s / %s", fileName, page + 1, pageCount));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {

    }

    private void downloadFile(String url) {
        try {
            Call<ResponseBody> download = apiInterface.downloadArticle(url);
            methods.showDialog(mDialog, "Loading file...", true);
            download.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            new AsyncTask<Void, Void, InputStream>() {

                                @Override
                                protected InputStream doInBackground(Void... voids) {
                                    InputStream inputStream = getInputstream(response.body());
                                    return inputStream;
                                }

                                @Override
                                protected void onPostExecute(InputStream aVoid) {
                                    super.onPostExecute(aVoid);
                                    //displayDocument(aVoid);
                                    methods.showDialog(mDialog, "dismiss", false);
                                    //methods.showAlert("Download Result", "Download finished", PreviewDocActivity.this);
                                }
                            }.execute();
                        } else {
                            Toast.makeText(PreviewDocActivity.this, "Request failed.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(PreviewDocActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        methods.showDialog(mDialog, "dismiss", false);
                        methods.showAlert("Request failed", "Request failed " + t.toString(), PreviewDocActivity.this);
                    } catch (Exception e) {
                        Toast.makeText(PreviewDocActivity.this, "Error " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(PreviewDocActivity.this, "Error raising download event.", Toast.LENGTH_SHORT).show();
        }
    }

    private InputStream getInputstream(ResponseBody body) {
        try {
            InputStream inputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                return inputStream;
            } catch (Exception e) {
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
}
