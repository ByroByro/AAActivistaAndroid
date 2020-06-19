package com.example.actionaidactivista.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actionaidactivista.BuildConfig;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.UserAccountsActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SettingsAdpter extends RecyclerView.Adapter<SettingsAdpter.SettingsViewHolder> {

    private Context mContext;
    private ArrayList<settings> mList;
    private List<settings> filtered_Members;
    private ProgressDialog progressDialog;
    private Dialog mProgress;

    public SettingsAdpter(Context context, ArrayList<settings> list) {
        this.mContext = context;
        this.mList = list;
        filtered_Members = new ArrayList<>(list);
        progressDialog = new ProgressDialog(context);
        mProgress = new Dialog(context);
    }

//    public Filter getFilter() {
//        return filter;
//    }
//
//    Filter filter = new Filter() {
//
//        //run on a bg thread
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            List<member> filtered_items = new ArrayList<>();
//            if (constraint.toString().isEmpty()) {
//                filtered_items.addAll(filtered_Members);
//            } else {
//                for (member item : filtered_Members) {
//                    if ((item.getMemberno() != null && item.getMemberno().toLowerCase().contains(constraint)) || (item.getFirstname() != null && item.getFirstname().toLowerCase().contains(constraint)
//                            || (item.getSurname() != null && item.getSurname().toLowerCase().contains(constraint)))) {
//                        filtered_items.add(item);
//                    }
//                }
//            }
//
//            FilterResults filterResults = new FilterResults();
//            filterResults.values = filtered_items;
//
//            return filterResults;
//        }
//
//        //run on UI thread
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            mMembers.clear();
//            mMembers.addAll((Collection<? extends member>) results.values);
//            notifyDataSetChanged();
//        }
//    };

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_row_item, null);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        final settings setting = mList.get(position);
        holder.title.setText(setting.getTitle());
        holder.subtitle.setText(setting.getSubtitle());
        holder.icon.setImageResource(setting.getIcon());

        //click events for row item
        holder.settings_card_view.setOnClickListener(v -> {
            try {
                if (setting.getTitle().equalsIgnoreCase("Account")) {
                    //Intent profile = new Intent(mContext, ProfileActivity.class);
                    //mContext.startActivity(profile);
                    //Toast.makeText(mContext,"Profile",Toast.LENGTH_SHORT).show();
                } else if (setting.getTitle().equalsIgnoreCase("Update Center")) {
                    Toast.makeText(mContext, "Up", Toast.LENGTH_SHORT).show();
                } else if (setting.getTitle().equalsIgnoreCase("About App")) {
                    //Toast.makeText(mContext,"info",Toast.LENGTH_SHORT).show();
                    StringBuffer stringBuffer = new StringBuffer();
                    String version = BuildConfig.VERSION_NAME;

                    Calendar now = Calendar.getInstance();
                    SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String dateNow = myFormat.format(now.getTime());
                    String[] dnow = dateNow.split("/");
                    int nowyear = Integer.valueOf(dnow[2]);

                    String year = String.valueOf(nowyear);

                    stringBuffer.append("Version : " + version);
                    stringBuffer.append("\n\n\nCopyright ActionAid " + year);

                    methods.showAlert("App Info", stringBuffer.toString(), mContext);
                } else if(setting.getTitle().equalsIgnoreCase("User Accounts")){
                    Intent intent = new Intent(mContext, UserAccountsActivity.class);
                    mContext.startActivity(intent);
                    //Toast.makeText(mContext,"User accounts clicked",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class SettingsViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subtitle;
        public ImageView icon;
        public CardView settings_card_view;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);
            subtitle = itemView.findViewById(R.id.des);
            icon = itemView.findViewById(R.id.imageView);
            settings_card_view = itemView.findViewById(R.id.settings_card_view);
        }
    }
}

