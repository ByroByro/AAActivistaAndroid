package com.example.actionaidactivista.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.R;
import com.example.actionaidactivista.ViewApplicationsActivity;
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.applications;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.models.opportunityapplications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class opportunity_applications_adapter extends RecyclerView.Adapter<opportunity_applications_adapter.ViewHolder> {

    private Context mContext;
    private List<opportunityapplications> mList;
    private List<opportunityapplications> filtered_list;

    public opportunity_applications_adapter(List<opportunityapplications> list, Context ctx) {
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

            List<opportunityapplications> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_list);
            } else {
                for (opportunityapplications item : filtered_list) {
                    if ((item.getTitle() != null && item.getTitle().toLowerCase().contains(constraint)) || (item.getDescription() != null && item.getDescription().toLowerCase().contains(constraint)
                            || (item.getLocation() != null && item.getLocation().toLowerCase().contains(constraint)))) {
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
            mList.addAll((Collection<? extends opportunityapplications>) results.values);
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
        return new opportunity_applications_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            opportunityapplications opp = mList.get(position);
            holder.mTitle.setText("Title : " + opp.getTitle());
            holder.mNoApps.setText(opp.getApplications() + " application(s)");
            holder.mNoApps.setTextColor(mContext.getResources().getColor(R.color.colorAccent));

            holder.mCardView.setOnClickListener(v -> {
                try {
                    Intent applications = new Intent(mContext, ViewApplicationsActivity.class);
                    applications.putExtra("opp_id", opp.getId());
                    mContext.startActivity(applications);
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
}
