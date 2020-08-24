package com.example.actionaidactivista.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.R;
import com.example.actionaidactivista.ViewApplicationsActivity;
import com.example.actionaidactivista.ViewAttachmentsActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.applications;
import com.example.actionaidactivista.models.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class applications_adapter extends RecyclerView.Adapter<applications_adapter.ViewHolder> {

    private Context mContext;
    private List<applications> mList;
    private List<applications> filtered_list;

    public applications_adapter(List<applications> list, Context ctx) {
        this.mList = list;
        this.mContext = ctx;
        this.filtered_list = new ArrayList<>(list);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<applications> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_list);
            } else {
                for (applications item : filtered_list) {
                    if ((item.getName() != null && item.getName().toLowerCase().contains(constraint)) || (item.getSurname() != null && item.getSurname().toLowerCase().contains(constraint)
                            || (item.getGender() != null && item.getGender().toLowerCase().contains(constraint)))) {
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
            mList.clear();
            mList.addAll((Collection<? extends applications>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.opportunity_applications_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new applications_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            applications app = mList.get(position);
            holder.mTitle.setText(app.getName() + " " + app.getSurname());
            holder.mNoApps.setText(app.getGender());

            holder.mCardView.setOnClickListener(v -> {
                try {

                    //Creating the instance of PopupMenu
                    final PopupMenu popup = new PopupMenu(mContext, holder.mCardView, Gravity.CENTER);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.applications_options_menu, popup.getMenu());

                    //getting the menu so as o access individual menus
                    Menu menu = popup.getMenu();

                    //get each individual menu item
                    MenuItem cover_letter = menu.findItem(R.id.action_view_cover_letter);
                    MenuItem moti_letter = menu.findItem(R.id.action_view_motivational_letter);
                    MenuItem cv = menu.findItem(R.id.action_view_cv);
                    MenuItem moti_video = menu.findItem(R.id.action_view_motivational_vid);
                    MenuItem moti_pic = menu.findItem(R.id.action_view_motivational_pic);
                    MenuItem moti_aud = menu.findItem(R.id.action_view_motivational_audio);
                    MenuItem in_app = menu.findItem(R.id.action_view_in_app);

                    if(app.getCoverletterurl().equalsIgnoreCase("N/A")){
                        cover_letter.setVisible(false);
                    }
                    if(app.getMotiletterurl().equalsIgnoreCase("N/A")){
                        moti_letter.setVisible(false);
                    }
                    if(app.getCvurl().equalsIgnoreCase("N/A")){
                        cv.setVisible(false);
                    }
                    if(app.getMotivideourl().equalsIgnoreCase("N/A")){
                        moti_video.setVisible(false);
                    }
                    if(app.getMotipicurl().equalsIgnoreCase("N/A")){
                        moti_pic.setVisible(false);
                    }
                    if(app.getAudio().equalsIgnoreCase("N/A")){
                        moti_aud.setVisible(false);
                    }
                    if(app.getMotivideourl().equalsIgnoreCase("N/A") && app.getMotipicurl().equalsIgnoreCase("N/A") && app.getAudio().equalsIgnoreCase("N/A")){
                        in_app.setVisible(false);
                    }

                    popup.setOnMenuItemClickListener(item -> {

                        if (item.getItemId() == R.id.action_view_motivational_vid) {
                            if (!app.getMotivideourl().equalsIgnoreCase("N/A")) {
                                Intent view = new Intent(Intent.ACTION_VIEW);
                                view.setDataAndType(Uri.parse(app.getMotivideourl()), "video/*");
                                Intent chooser = Intent.createChooser(view, "Open with");
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//optional
                                mContext.startActivity(chooser);
                            } else {
                                methods.showAlert("No Content", "No motivational video", mContext);
                            }
                        } else if (item.getItemId() == R.id.action_view_motivational_pic) {
                            if (!app.getMotipicurl().equalsIgnoreCase("N/A")) {
                                Intent view = new Intent(Intent.ACTION_VIEW);
                                view.setDataAndType(Uri.parse(app.getMotipicurl()), "image/*");
                                Intent chooser = Intent.createChooser(view, "Open with");
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//optional
                                mContext.startActivity(chooser);
                            } else {
                                methods.showAlert("No Content", "No motivational pic", mContext);
                            }
                        } else if (item.getItemId() == R.id.action_view_cover_letter) {
                            if (!app.getCoverletterurl().equalsIgnoreCase("N/A")) {
                                String ext = methods.getExtensionFromUrl(app.getCoverletterurl(), mContext);
                                if (ext.equalsIgnoreCase("pdf")) {
                                    //Intent intent = new Intent(mContext, PreviewDocActivity.class);
                                    //intent.putExtra("url", app.getCoverletterurl());
                                    //mContext.startActivity(intent);
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(app.getCoverletterurl()));
                                    mContext.startActivity(browserIntent);
                                } else if (ext.equalsIgnoreCase("doc") || ext.equalsIgnoreCase("docx")) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(app.getCoverletterurl()));
                                    mContext.startActivity(browserIntent);
                                } else {
                                    methods.showAlert("Unknown File Format", "File format is not supported.", mContext);
                                }
                            } else {
                                methods.showAlert("No Content", "No cover letter", mContext);
                            }
                        } else if (item.getItemId() == R.id.action_view_motivational_letter) {
                            if (!app.getMotiletterurl().equalsIgnoreCase("N/A")) {

                                String ext = methods.getExtensionFromUrl(app.getMotiletterurl(), mContext);
                                if (ext.equalsIgnoreCase("pdf")) {
                                    //Intent intent = new Intent(mContext, PreviewDocActivity.class);
                                    //intent.putExtra("url", app.getMotiletterurl());
                                    //mContext.startActivity(intent);
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(app.getMotiletterurl()));
                                    mContext.startActivity(browserIntent);
                                } else if (ext.equalsIgnoreCase("doc") || ext.equalsIgnoreCase("docx")) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(app.getMotiletterurl()));
                                    mContext.startActivity(browserIntent);
                                } else {
                                    methods.showAlert("Unknown File Format", "File format is not supported.", mContext);
                                }
                            } else {
                                methods.showAlert("No Content", "No motivational letter", mContext);
                            }
                        } else if (item.getItemId() == R.id.action_view_cv) {
                            if (!app.getCvurl().equalsIgnoreCase("N/A")) {
                                String ext = methods.getExtensionFromUrl(app.getCvurl(), mContext);
                                if (ext.equalsIgnoreCase("pdf")) {
                                    //Intent intent = new Intent(mContext, PreviewDocActivity.class);
                                    //intent.putExtra("url", app.getCvurl());
                                    //mContext.startActivity(intent);
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(app.getCvurl()));
                                    mContext.startActivity(browserIntent);
                                } else if (ext.equalsIgnoreCase("doc") || ext.equalsIgnoreCase("docx")) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                    browserIntent.setData(Uri.parse(app.getCvurl()));
                                    //Intent chooser = Intent.createChooser(browserIntent, "Open with");
                                    //chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // optional
                                    mContext.startActivity(browserIntent);
                                } else {
                                    methods.showAlert("Unknown File Format", "File format is not supported.", mContext);
                                }
                            } else {
                                methods.showAlert("No Content", "No curriculum vitae", mContext);
                            }
                        } else if (item.getItemId() == R.id.action_view_motivational_audio) {
                            if (!app.getAudio().equalsIgnoreCase("N/A")) {
                                Intent view = new Intent(Intent.ACTION_VIEW);
                                view.setDataAndType(Uri.parse(app.getAudio()), "audio/*");
                                Intent chooser = Intent.createChooser(view, "Open with");
                                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//optional
                                mContext.startActivity(chooser);
                            } else {
                                methods.showAlert("No Content", "No motivational audio", mContext);
                            }
                        } else if (item.getItemId() == R.id.action_view_in_app) {
                            Intent view = new Intent(mContext, ViewAttachmentsActivity.class);
                            view.putExtra("image", app.getMotipicurl());
                            view.putExtra("audio", app.getAudio());
                            view.putExtra("video", app.getMotivideourl());
                            mContext.startActivity(view);
                        }
                        return false;
                    });
                    //show the popup
                    popup.show();

                } catch (Exception e) {
                    Toast.makeText(mContext, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, "Error binding view holder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitle;
        public TextView mNoApps;
        public CardView mCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mNoApps = itemView.findViewById(R.id.no_applications);
            mCardView = itemView.findViewById(R.id.opp_app_card_view);
        }
    }

    //method for adding more applicants
    public void addApplicant(List<applications> list) {
        try {
            for (applications f : list) {
                mList.add(f);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(mContext, "Add application error in adapter", Toast.LENGTH_SHORT).show();
        }
    }
}
