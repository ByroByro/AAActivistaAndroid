package com.example.actionaidactivista.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.List;

public class contact_adapter extends RecyclerView.Adapter<contact_adapter.ViewHolder> {

    private Context mContext;
    private List<contact> mContacts;

    public contact_adapter(List<contact> list, Context ctx){
        this.mContacts = list;
        this.mContext = ctx;
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
        return new contact_adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
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
               try{
                   StringBuffer stringBuffer = new StringBuffer();
                   stringBuffer.append("Name : " + contact.getmName());
                   stringBuffer.append("\nSurname : " + contact.getmSurname());
                   stringBuffer.append("\nGender : " + contact.getmGender());
                   stringBuffer.append("\nDate of Birth : " + contact.getmDob());
                   stringBuffer.append("\nOccupation : " + contact.getmOccupation());
                   stringBuffer.append("\nApproval status(IsApproved) : " + contact.getmStatus());
                   methods.showAlert("\nActivista Details",stringBuffer.toString(),mContext);
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
