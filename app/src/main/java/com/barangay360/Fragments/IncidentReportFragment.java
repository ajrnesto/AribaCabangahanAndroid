package com.barangay360.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class IncidentReportFragment extends Fragment implements IncidentsAdapter.OnIncidentsListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;
    TabLayout tlIncidentReports;
    ExtendedFloatingActionButton btnReportIncident;

    ArrayList<Incident> arrIncidents;
    IncidentsAdapter incidentsAdapter;
    IncidentsAdapter.OnIncidentsListener onIncidentsListener = this;


    RecyclerView rvIncidentReports;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_incident_report, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tlIncidentReports.getSelectedTabPosition());
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        rvIncidentReports = view.findViewById(R.id.rvIncidentReports);
        btnReportIncident = view.findViewById(R.id.btnReportIncident);
        tlIncidentReports = view.findViewById(R.id.tlIncidentReports);
    }

    private void loadRecyclerView(int tabIndex) {
        arrIncidents = new ArrayList<>();
        rvIncidentReports = view.findViewById(R.id.rvIncidentReports);
        rvIncidentReports.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvIncidentReports.setLayoutManager(linearLayoutManager);

        CollectionReference refIncidents = DB.collection("incidents");
        Query qryIncidents = null;

        if (tabIndex == 0) {
            qryIncidents = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "PENDING").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else if (tabIndex == 1) {
            qryIncidents = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "HEARING SCHEDULED").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else if (tabIndex == 2) {
            qryIncidents = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "UNDER INVESTIGATION").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else {
            qryIncidents = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereIn("status", Arrays.asList("RESOLVED", "CLOSED")).orderBy("timestamp", Query.Direction.ASCENDING);
        }

        qryIncidents.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrIncidents.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Incident incident = documentSnapshot.toObject(Incident.class);

                    arrIncidents.add(incident);
                    incidentsAdapter.notifyDataSetChanged();
                }

                if (arrIncidents.isEmpty()) {
                    rvIncidentReports.setVisibility(View.GONE);
                }
                else {
                    rvIncidentReports.setVisibility(View.VISIBLE);
                }
            }
        });

        incidentsAdapter = new IncidentsAdapter(getContext(), arrIncidents, onIncidentsListener);
        rvIncidentReports.setAdapter(incidentsAdapter);
        incidentsAdapter.notifyDataSetChanged();
    }

    private void handleUserInteraction() {
        tlIncidentReports.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tlIncidentReports.getSelectedTabPosition());
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
            Fragment incidentReportFragment = new FormIncidentReportFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, incidentReportFragment, "INCIDENT_REPORT_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("INCIDENT_REPORT_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onIncidentsClick(int position) {
        
    }
}