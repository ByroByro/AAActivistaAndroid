package com.example.actionaidactivista.logic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.actionaidactivista.R;

public class DownloadProgress {
    private Dialog dialog;
    private TextView downloading_text;
    private TextView progress_text;
    private ImageButton cancel;
    private Context context;

    public DownloadProgress(Dialog dialog, Context ctx) {
        this.dialog = dialog;
        this.context = ctx;
    }

    //method for showing dialog
    public void showDialog(AsyncTask<String, Integer, String> task) {
        try {
            dialog.setContentView(R.layout.download_progress_cancel);
            downloading_text = (TextView) dialog.findViewById(R.id.downloading_text);
            progress_text = (TextView) dialog.findViewById(R.id.progress_text);
            cancel = (ImageButton) dialog.findViewById(R.id.cancelDownload);

            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cancel download?");
                    builder.setMessage("Are you sure you want to cancel the download.");
                    //set dialog to be cancelled only by buttons
                    builder.setCancelable(false);

                    //set dismiss button
                    builder.setNeutralButton("No", (dialog1, which) -> dialog1.dismiss());

                    //set positive button
                    builder.setPositiveButton("Yes", (dialog1, which) -> {
                        try {
                            task.cancel(true);
                            dialog.dismiss();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    });

                    AlertDialog dialog1 = builder.create();
                    dialog1.show();
                }
                return true;
            });

            cancel.setOnClickListener(v -> {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Cancel download?");
                    builder.setMessage("Are you sure you want to cancel the download.");
                    //set dialog to be cancelled only by buttons
                    builder.setCancelable(false);

                    //set dismiss button
                    builder.setNeutralButton("No", (dialog1, which) -> dialog1.dismiss());

                    //set positive button
                    builder.setPositiveButton("Yes", (dialog1, which) -> {
                        try {
                            task.cancel(true);
                            dialog.dismiss();
                            Toast.makeText(context, "Download canceled.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    });

                    AlertDialog dialog1 = builder.create();
                    dialog1.show();
                } catch (Exception e) {
                    Toast.makeText(context, "Failed to cancel.", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to show dialog.", Toast.LENGTH_SHORT).show();
        }
    }

    //dismiss dialog
    public void dismissDialog() {
        this.dialog.dismiss();
    }

    //update progress
    public void updateProgress(int progress) {
        progress_text.setText(String.valueOf(progress) + " %");
    }
}
