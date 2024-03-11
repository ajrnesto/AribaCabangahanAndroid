package com.barangay360.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Adapters.AnnouncementAdapter;
import com.barangay360.Adapters.IncidentsAdapter;
import com.barangay360.Objects.Announcement;
import com.barangay360.Objects.Incident;
import com.barangay360.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class AnnouncementsFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ArrayList<Announcement> arrAnnouncements;
    AnnouncementAdapter adapterAnnouncements;
    View view;
    RecyclerView rvAnnouncements;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_announcements, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView();

        return view;
    }

    private void initializeViews() {
        rvAnnouncements = view.findViewById(R.id.rvAnnouncements);
    }

    private void loadRecyclerView() {
        arrAnnouncements = new ArrayList<>();
        rvAnnouncements = view.findViewById(R.id.rvAnnouncements);
        rvAnnouncements.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvAnnouncements.setLayoutManager(linearLayoutManager);

        CollectionReference refAnnouncements = DB.collection("announcements");

        Query qryCrimes = refAnnouncements.orderBy("timestamp", Query.Direction.DESCENDING);

        qryCrimes.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrAnnouncements.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Announcement announcement = documentSnapshot.toObject(Announcement.class);

                    arrAnnouncements.add(announcement);
                    adapterAnnouncements.notifyDataSetChanged();
                }

                if (arrAnnouncements.isEmpty()) {
                    rvAnnouncements.setVisibility(View.GONE);
                }
                else {
                    rvAnnouncements.setVisibility(View.VISIBLE);
                }
            }
        });

        adapterAnnouncements = new AnnouncementAdapter(getContext(), arrAnnouncements);
        rvAnnouncements.setAdapter(adapterAnnouncements);
        adapterAnnouncements.notifyDataSetChanged();
    }
}