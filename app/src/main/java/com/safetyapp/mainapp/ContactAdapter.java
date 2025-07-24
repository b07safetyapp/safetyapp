package com.safetyapp.mainapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    @SuppressWarnings("FieldMayBeFinal")
    private List<Contact> contactList;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textName, textRelationship, textPhone;
        public ViewHolder(View V){
            super(V);
            textName = V.findViewById(R.id.textName);
            textRelationship = V.findViewById(R.id.textRelationship);
            textPhone = V.findViewById(R.id.textPhone);
        }
    }
    public ContactAdapter(List<Contact> contactList){
        this.contactList = contactList;
    }
    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(V);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Contact contact = contactList.get(position);
        holder.textName.setText(contact.name);
        holder.textRelationship.setText(contact.relationship);
        holder.textPhone.setText(contact.phone);
    }
    @Override
    public int getItemCount(){ return contactList.size(); }

    public void deleteItem(int position){
        contactList.remove(position);
        notifyItemRemoved(position);
    }
}
