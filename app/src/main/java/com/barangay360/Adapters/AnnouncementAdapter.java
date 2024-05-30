package com.barangay360.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Dialogs.RequirementsDialog;
import com.barangay360.Dialogs.ViewImageDialog;
import com.barangay360.Objects.Announcement;
import com.barangay360.Objects.Announcement;
import com.barangay360.Objects.InvolvedPerson;
import com.barangay360.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.announcementsViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    Context context;
    ArrayList<Announcement> arrAnnouncements;

    public AnnouncementAdapter(Context context, ArrayList<Announcement> arrAnnouncements) {
        this.context = context;
        this.arrAnnouncements = arrAnnouncements;
    }

    @NonNull
    @Override
    public announcementsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_announcement, parent, false);
        return new announcementsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull announcementsViewHolder holder, int position) {
        Announcement announcements = arrAnnouncements.get(position);
        long timestamp = announcements.getTimestamp();
        String title = announcements.getTitle();
        String content = announcements.getContent();
        long thumbnail = announcements.getThumbnail();

        holder.tvTitle.setText(title);
        loadTimestamp(holder, timestamp);
        holder.tvContent.setText(content);

        FirebaseStorage.getInstance().getReference("announcement/"+thumbnail).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).resize(900,0).centerCrop().into(holder.imgBanner);
            }
        });
    }

    private void loadTimestamp(announcementsViewHolder holder, long timestamp) {
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("MMMM dd, yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdfTimestamp.format(timestamp));
    }

    @Override
    public int getItemCount() {
        return arrAnnouncements.size();
    }

    public class announcementsViewHolder extends RecyclerView.ViewHolder {

        ZoomageView imgBanner;
        TextView tvTitle, tvTimestamp, tvContent;

        public announcementsViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBanner = itemView.findViewById(R.id.imgBanner);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);

            itemView.setOnClickListener(view -> toggleContentVisibility());
            /*tvTitle.setOnClickListener(view -> toggleContentVisibility());
            tvTimestamp.setOnClickListener(view -> toggleContentVisibility());*/

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    /*Create an ACTION_SEND Intent*/
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    /*This will be the actual content you wish you share.*/
                    String shareBody = arrAnnouncements.get(getAdapterPosition()).getTitle() + "\n\n" + arrAnnouncements.get(getAdapterPosition()).getContent();
                    /*The type of the content is text, obviously.*/
                    intent.setType("text/plain");
                    /*Applying information Subject and Body.*/
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, arrAnnouncements.get(getAdapterPosition()).getTitle());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    /*Fire!*/
                    context.startActivity(Intent.createChooser(intent, "Share"));

                    return false;
                }
            });

            imgBanner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewImageDialog viewImageDialog = new ViewImageDialog();
                    Bundle args = new Bundle();
                    args.putLong("thumbnail", arrAnnouncements.get(getAdapterPosition()).getThumbnail());
                    viewImageDialog.setArguments(args);
                    viewImageDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "REQUIREMENTS_DIALOG");
                }
            });
        }

        private void toggleContentVisibility() {
            if (tvContent.getVisibility() == View.VISIBLE) {
                tvContent.setVisibility(View.GONE);
            }
            else {
                tvContent.setVisibility(View.VISIBLE);
            }
        }
    }
}
