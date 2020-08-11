package com.example.actionaidactivista.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.ActivityUploadActivity;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.applications;

import java.util.ArrayList;
import java.util.List;

public class search_adapter extends RecyclerView.Adapter<search_adapter.ViewHolder> {

    private Context mContext;
    private List<search_model> mList;
    private String wchSearch;
    private String mOperation;

    //constructor for adding / removing participants on activity upload
    private ArrayList<String> mParticipants;

    public search_adapter(List<search_model> list, String whichSearch, String operation, Context ctx) {
        this.mList = list;
        this.mContext = ctx;
        this.wchSearch = whichSearch;
        this.mOperation = operation;
        this.mParticipants = new ArrayList<>();
    }

    public search_adapter() {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.custom_search_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new search_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            search_model data = mList.get(position);
            holder.mTitle.setText(data.getId());
            holder.mDisplay.setText(data.getDisplayName());

            holder.mCardView.setOnClickListener(v -> {
                try {

                    ActivityUploadActivity au = new ActivityUploadActivity();
                    /*
                     * this popup menu is for adding / removing participants
                     * for an activity, when it is uploaded
                     */
                    if (mOperation.equalsIgnoreCase("Add or remove participants")) {
                        //Creating the instance of PopupMenu
                        final PopupMenu popup = new PopupMenu(mContext, holder.mCardView, Gravity.CENTER);
                        //Inflating the Popup using xml file
                        popup.getMenuInflater().inflate(R.menu.custom_search_select_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(item -> {

                            if (item.getItemId() == R.id.action_add) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm addition");
                                builder.setMessage("Are you sure to add this person as a participant.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        /*
                                         * method to add a participant in a list of participants if they are not
                                         * already in the list
                                         */
                                        if (mParticipants.contains(data.getId())) {
                                            //do not add
                                            Toast.makeText(mContext, "Participant already added.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mParticipants.add(data.getId());
                                            Toast.makeText(mContext, "Participant added.", Toast.LENGTH_SHORT).show();
                                            //methods.showAlert("List", mParticipants.toString(), mContext);
                                        }
                                    } catch (Exception e) {
                                        methods.showAlert("Error", e.toString(), mContext);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (item.getItemId() == R.id.action_remove) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Confirm action");
                                builder.setMessage("Are you sure to remove this person as a participant.");
                                //builder.setIcon(R.drawable.ic_verified_trans_24dp);
                                //set dialog to be cancelled only by buttons
                                builder.setCancelable(false);

                                //set dismiss button
                                builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

                                //set positive button
                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    try {
                                        /*
                                         * method to add a participant in a list of participants if they are not
                                         * already in the list
                                         */
                                        if (mParticipants.contains(data.getId())) {
                                            //do not add
                                            mParticipants.remove(data.getId());
                                            Toast.makeText(mContext, "Participant removed.", Toast.LENGTH_SHORT).show();
                                            //methods.showAlert("List", mParticipants.toString(), mContext);
                                        } else {
                                            //mParticipants.add(id);
                                            Toast.makeText(mContext, "Participant not in list.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        methods.showAlert("Error", e.toString(), mContext);
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            return false;
                        });
                        //show the popup
                        popup.show();
                    }

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
        public TextView mDisplay;
        public CardView mCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mDisplay = itemView.findViewById(R.id.description);
            mCardView = itemView.findViewById(R.id.custom_card_view);
        }
    }

    /*
     * this methods returns the tagged participants
     */
    public ArrayList<String> getParticipants() {
        ArrayList<String> lst = new ArrayList<>();
        try {
            lst = mParticipants;
        } catch (Exception e) {
            System.out.println(e);
        }
        return lst;
    }
}
