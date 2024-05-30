package com.barangay360.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Adapters.IncidentsAdapter;
import com.barangay360.Objects.Incident;
import com.barangay360.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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

public class BlotterFragment extends Fragment implements IncidentsAdapter.OnIncidentsListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;
    TabLayout tlBlotter;
    ExtendedFloatingActionButton btnReportIncident;

    ArrayList<Incident> arrBlotters;
    IncidentsAdapter blottersBlotters;
    IncidentsAdapter.OnIncidentsListener onBlottersListener = this;
    TextView tvEmpty;

    RecyclerView rvBlotter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_blotter, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tlBlotter.getSelectedTabPosition());
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        rvBlotter = view.findViewById(R.id.rvBlotter);
        btnReportIncident = view.findViewById(R.id.btnReportIncident);
        tlBlotter = view.findViewById(R.id.tlBlotter);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void loadRecyclerView(int tabIndex) {
        arrBlotters = new ArrayList<>();
        rvBlotter = view.findViewById(R.id.rvBlotter);
        rvBlotter.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvBlotter.setLayoutManager(linearLayoutManager);

        CollectionReference refBlotters = DB.collection("blotter");
        Query qryBlotters = null;

        if (tabIndex == 0) {
            qryBlotters = refBlotters.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "PENDING").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        /*else if (tabIndex == 1) {
            qryBlotters = refBlotters.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "HEARING SCHEDULED").orderBy("timestamp", Query.Direction.ASCENDING);
        }*/
        else if (tabIndex == 1) {
            qryBlotters = refBlotters.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "UNDER INVESTIGATION").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else {
            qryBlotters = refBlotters.whereEqualTo("userUid", AUTH.getUid()).whereIn("status", Arrays.asList("RESOLVED", "CLOSED")).orderBy("timestamp", Query.Direction.ASCENDING);
        }

        qryBlotters.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrBlotters.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Incident blotter = documentSnapshot.toObject(Incident.class);

                    arrBlotters.add(blotter);
                    blottersBlotters.notifyDataSetChanged();
                }

                if (arrBlotters.isEmpty()) {
                    rvBlotter.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);

                    if (tlBlotter.getSelectedTabPosition() == 0) {
                        tvEmpty.setText("You have no pending reports");
                    }
                    /*else if (tlBlotter.getSelectedTabPosition() == 1) {
                        tvEmpty.setText("You have no reports scheduled for hearing");
                    }*/
                    else if (tlBlotter.getSelectedTabPosition() == 1) {
                        tvEmpty.setText("You have no reports under investigation");
                    }
                    else {
                        tvEmpty.setText("You have no resolved reports");
                    }
                }
                else {
                    rvBlotter.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }
            }
        });

        blottersBlotters = new IncidentsAdapter(getContext(), arrBlotters, onBlottersListener);
        rvBlotter.setAdapter(blottersBlotters);
        blottersBlotters.notifyDataSetChanged();
    }

    private void handleUserInteraction() {
        tlBlotter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tlBlotter.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btnReportIncident.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment formBlotterReportFragment = new FormBlotterFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formBlotterReportFragment, "CRIME_REPORT_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("CRIME_REPORT_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onIncidentsClick(int position) {

    }
}