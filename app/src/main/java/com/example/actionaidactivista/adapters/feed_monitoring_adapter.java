package com.example.actionaidactivista.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.actionaidactivista.models.feedmonitor;

import java.util.List;

public class feed_monitoring_adapter extends RecyclerView.Adapter<feed_monitoring_adapter.ViewHolder> {

    private Context mContext;
    private List<feedmonitor> mContacts;
    private String mCategory;

    public feed_monitoring_adapter(List<feedmonitor> list, Context ctx,String cate){
        this.mContacts = list;
        this.mContext = ctx;
        this.mCategory = cate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(mContext).inflate(R.layout.contact_row_item, parent, false);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error adapter create view holder", Toast.LENGTH_SHORT).show();
        }
        return new feed_monitoring_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            feedmonitor user = mContacts.get(position);
            holder.mName.setText("Name : " + user.getFname() + " " + user.getSname());
            if(mCategory.equalsIgnoreCase("byfeedsuploaded")) {
                holder.mPhone.setText("No of feeds/activities : " + user.getNooffeeds());
            } else if(mCategory.equalsIgnoreCase("bytags")){
                holder.mPhone.setText("No of feeds/activities tagged in : " + user.getNooffeeds());
            } else if(mCategory.equalsIgnoreCase("byfeedlikes")){
                holder.mPhone.setText("No of likes on activities : " + user.getNooffeeds());
            }
            if (!user.getProfile().equalsIgnoreCase("N/A")) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.centerCrop();
                requestOptions.placeholder(R.drawable.ic_contacts_red);
                requestOptions.error(R.drawable.ic_contacts_red);

                Glide.with(mContext)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(user.getProfile())
                        .into(holder.mProfile);
            } else {
                holder.mProfile.setImageResource(R.drawable.ic_contacts_red);
            }
            holder.mCardView.setOnClickListener(v -> {
               try{

               }catch (Exception e){
                   Toast.makeText(mContext,"Error raising download event.",Toast.LENGTH_SHORT).show();
               }
            });
        }catch (Exception e){
            Toast.makeText(mContext,"Error binding view holder",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

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
