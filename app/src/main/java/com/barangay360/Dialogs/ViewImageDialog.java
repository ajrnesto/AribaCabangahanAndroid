package com.barangay360.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.barangay360.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ViewImageDialog extends AppCompatDialogFragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryUser;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    ZoomageView imgBanner;
    MaterialButton btnBack;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view_image, null);

        initializeFirebase();
        initialize(view);
        loadImage();
        buttonHandler();

        builder.setView(view);
        return builder.create();
    }

    private void loadImage() {
        long thumbnail = getArguments().getLong("thumbnail");

        FirebaseStorage.getInstance().getReference("announcement/"+thumbnail).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).resize(900,0).centerCrop().into(imgBanner);
            }
        });
    }

    private void initialize(View view) {
        imgBanner = view.findViewById(R.id.imgBanner);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void buttonHandler() {
        btnBack.setOnClickListener(view -> dismiss());
    }
}
