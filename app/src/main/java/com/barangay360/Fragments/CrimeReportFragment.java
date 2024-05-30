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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class CrimeReportFragment extends Fragment implements IncidentsAdapter.OnIncidentsListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryCrimes = null;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;
    TabLayout tlIncidentReports;
    ExtendedFloatingActionButton btnReportCrime;

    ArrayList<Incident> arrIncidents;
    IncidentsAdapter incidentsCrimes;
    IncidentsAdapter.OnIncidentsListener onIncidentsListener = this;
    TextView tvEmpty;

    RecyclerView rvIncidentReports;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_crime_report, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tlIncidentReports.getSelectedTabPosition());
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        rvIncidentReports = view.findViewById(R.id.rvIncidentReports);
        btnReportCrime = view.findViewById(R.id.btnReportCrime);
        tlIncidentReports = view.findViewById(R.id.tlIncidentReports);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void loadRecyclerView(int tabIndex) {
        arrIncidents = new ArrayList<>();
        rvIncidentReports = view.findViewById(R.id.rvIncidentReports);
        rvIncidentReports.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvIncidentReports.setLayoutManager(linearLayoutManager);

        CollectionReference refIncidents = DB.collection("crimes");

        if (tabIndex == 0) {
            qryCrimes = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "PENDING").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else if (tabIndex == 1) {
            qryCrimes = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "UNDER INVESTIGATION").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else {
            qryCrimes = refIncidents.whereEqualTo("userUid", AUTH.getUid()).whereIn("status", Arrays.asList("RESOLVED", "CLOSED")).orderBy("timestamp", Query.Direction.ASCENDING);
        }

        qryCrimes.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrIncidents.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Incident incident = documentSnapshot.toObject(Incident.class);

                    arrIncidents.add(incident);
                    incidentsCrimes.notifyDataSetChanged();
                }

                if (arrIncidents.isEmpty()) {
                    rvIncidentReports.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    if (tlIncidentReports.getSelectedTabPosition() == 0) {
                        tvEmpty.setText("You have no pending crime reports");
                    }
                    else if (tlIncidentReports.getSelectedTabPosition() == 1) {
                        tvEmpty.setText("You have no reports under investigation");
                    }
                    else if (tlIncidentReports.getSelectedTabPosition() == 2) {
                        tvEmpty.setText("You have no resolved reports");
                    }
                }
                else {
                    rvIncidentReports.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }
            }
        });

        incidentsCrimes = new IncidentsAdapter(getContext(), arrIncidents, onIncidentsListener);
        rvIncidentReports.setAdapter(incidentsCrimes);
        incidentsCrimes.notifyDataSetChanged();
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

        btnReportCrime.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment formCrimeReportFragment = new FormCrimeReportFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formCrimeReportFragment, "CRIME_REPORT_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("CRIME_REPORT_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onIncidentsClick(int position) {

    }
}