package com.safetyapp.mainapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import java.util.*;


public class SupportResourceAdapter extends RecyclerView.Adapter<SupportResourceAdapter.ViewHolder> {


    private final List<SupportResource> resourceList;


    public SupportResourceAdapter(List<SupportResource> resourceList) {
        this.resourceList = resourceList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_support_resource, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SupportResource resource = resourceList.get(position);

        holder.nameTextView.setText(resource.getName());
        holder.categoryTextView.setText(resource.getCategory());
        holder.contactTextView.setText(resource.getContact());




        // Handle the location button visibility and click listener
        List<Double> location = resource.getLocation();
        if (location != null && location.size() == 2) {
            holder.locationIcon.setVisibility(View.VISIBLE);
            holder.locationIcon.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Double lat = location.get(0);
                Double lon = location.get(1);

                // Create a Uri from an intent string. Use the result to create an Intent.
                String uriString = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", lat, lon, lat, lon, Uri.encode(resource.getName()));
                Uri gmmIntentUri = Uri.parse(uriString);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            });
        } else {
            // Hide the icon if there is no location data
            holder.locationIcon.setVisibility(View.GONE);
        }






    }


    @Override
    public int getItemCount() {

        return resourceList != null ? resourceList.size() : 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView categoryTextView;
        TextView contactTextView;
        ImageView locationIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.textViewName);
            categoryTextView = itemView.findViewById(R.id.textViewCategory);
            contactTextView = itemView.findViewById(R.id.textViewContact);

            // Need to add an ImageView with this ID to XML file
            locationIcon = itemView.findViewById(R.id.imageViewLocation);
        }
    }
}