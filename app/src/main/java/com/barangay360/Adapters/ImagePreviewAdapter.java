package com.barangay360.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Objects.Request;
import com.barangay360.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.imagePreviewViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    Context context;
    ArrayList<Uri> arrUri;
    private OnImagePreviewListener mOnImagePreviewListener;

    public ImagePreviewAdapter(Context context, ArrayList<Uri> arrUri, OnImagePreviewListener onImagePreviewListener) {
        this.context = context;
        this.arrUri = arrUri;
        this.mOnImagePreviewListener = onImagePreviewListener;
    }

    @NonNull
    @Override
    public imagePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_image_preview, parent, false);
        return new imagePreviewViewHolder(view, mOnImagePreviewListener);
    }

    @Override
    public void onBindViewHolder(@NonNull imagePreviewViewHolder holder, int position) {
        Uri uri = arrUri.get(position);

        Picasso.get().load(uri).resize(350,350).centerCrop().into(holder.imgIncident);

        try {
            holder.tvFileSize.setText(context.getContentResolver().openAssetFileDescriptor(uri, "r").getLength() / 1024 / 1024 + " MB");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (context.getContentResolver().getType(uri).startsWith("video")) {
            holder.tvFileType.setText("Video");
        }
        else {
            holder.tvFileType.setText("Image");
        }
    }

    @Override
    public int getItemCount() {
        return arrUri.size();
    }

    public class imagePreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnImagePreviewListener onImagePreviewListener;
        MaterialButton btnRemove;
        RoundedImageView imgIncident;
        TextView tvFileSize, tvFileType;

        public imagePreviewViewHolder(@NonNull View itemView, OnImagePreviewListener onImagePreviewListener) {
            super(itemView);

            btnRemove = itemView.findViewById(R.id.btnRemove);
            imgIncident = itemView.findViewById(R.id.imgIncident);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            tvFileType = itemView.findViewById(R.id.tvFileType);

            this.onImagePreviewListener = onImagePreviewListener;
            btnRemove.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onImagePreviewListener.onImagePreviewClick(getAdapterPosition());
        }
    }

    public interface OnImagePreviewListener{
        void onImagePreviewClick(int position);
    }
}
