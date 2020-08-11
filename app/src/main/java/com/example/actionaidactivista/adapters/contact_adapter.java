package com.example.actionaidactivista.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.actionaidactivista.methods;
import com.example.actionaidactivista.models.contact;
import com.example.actionaidactivista.models.library_article;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class contact_adapter extends RecyclerView.Adapter<contact_adapter.ViewHolder> {

    private Context mContext;
    private List<contact> mContacts;
    private List<contact> filtered_Contacts;

    public contact_adapter(List<contact> list, Context ctx) {
        this.mContacts = list;
        this.mContext = ctx;
        this.filtered_Contacts = new ArrayList<>(list);
    }

    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //run on a bg thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<contact> filtered_items = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filtered_items.addAll(filtered_Contacts);
            } else {
                for (contact item : filtered_Contacts) {
                    if ((item.getmName() != null && item.getmName().toLowerCase().contains(constraint)) || (item.getmSurname() != null && item.getmSurname().toLowerCase().contains(constraint)
                            || (item.getmStatus() != null && item.getmStatus().toLowerCase().contains(constraint)))) {
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
            mContacts.clear();
            mContacts.addAll((Collection<? extends contact>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.contact_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new contact_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            contact contact = mContacts.get(position);
            holder.mName.setText("Name : " + contact.getmName() + " " + contact.getmSurname());
            holder.mPhone.setText("Occupation : " + contact.getmOccupation());
            if (!contact.getmProfileUrl().equalsIgnoreCase("N/A")) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                requestOptions.placeholder(R.drawable.ic_contacts_red);
                requestOptions.error(R.drawable.ic_contacts_red);

                Glide.with(mContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(contact.getmProfileUrl())
                        .into(holder.mProfile);
            } else {
                holder.mProfile.setImageResource(R.drawable.ic_contacts_red);
            }
            holder.mCardView.setOnClickListener(v -> {
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("Name : " + contact.getmName());
                    stringBuffer.append("\nSurname : " + contact.getmSurname());
                    stringBuffer.append("\nGender : " + contact.getmGender());
                    if (contact.getmDobPublic().equalsIgnoreCase("True")) {
                        stringBuffer.append("\nDate of Birth : " + methods.getReadableDate(contact.getmDob(), mContext));
                    } else {
                       stringBuffer.append("\nAge : " + methods.getAge(contact.getmDob(),mContext));
                    }
                    stringBuffer.append("\nOccupation : " + contact.getmOccupation());
                    stringBuffer.append("\nPhone No : " + contact.getmPhone());
                    //stringBuffer.append("\nApproval status(IsApproved) : " + contact.getmStatus());
                    stringBuffer.append("\nBiography : " + contact.getmBio());
                    methods.showAlert("\nActivista Details", stringBuffer.toString(), mContext);
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
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public TextView mPhone;
        public CardView mCardView;
        public ImageView mProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            mPhone = itemView.findViewById(R.id.phone);
            mProfile = itemView.findViewById(R.id.imageView);
            mCardView = itemView.findViewById(R.id.contact_card_view);
        }
    }
}
