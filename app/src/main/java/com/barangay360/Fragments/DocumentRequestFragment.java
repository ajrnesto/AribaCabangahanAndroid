package com.barangay360.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Adapters.RequestsAdapter;
import com.barangay360.Objects.Request;
import com.barangay360.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class DocumentRequestFragment extends Fragment implements RequestsAdapter.OnRequestsListener {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    ArrayList<Request> arrRequests;
    RequestsAdapter requestsAdapter;
    RequestsAdapter.OnRequestsListener onRequestsListener = this;
    TabLayout tlRequestStatus;

    RecyclerView rvDocumentRequests;
    ExtendedFloatingActionButton btnRequestNewDocument;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_document_request, container, false);

        initializeFirebase();
        initializeViews();
        loadRecyclerView(tlRequestStatus.getSelectedTabPosition());
        handleUserInteractions();

        return view;
    }

    private void initializeViews() {
        btnRequestNewDocument = view.findViewById(R.id.btnRequestNewDocument);
        rvDocumentRequests = view.findViewById(R.id.rvDocumentRequests);
        tlRequestStatus = view.findViewById(R.id.tlRequestStatus);
    }

    private void loadRecyclerView(int tabIndex) {
        arrRequests = new ArrayList<>();
        rvDocumentRequests = view.findViewById(R.id.rvDocumentRequests);
        rvDocumentRequests.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvDocumentRequests.setLayoutManager(linearLayoutManager);

        CollectionReference refRequests = DB.collection("requests");
        Query qryRequests = null;

        if (tabIndex == 0) {
            qryRequests = refRequests.whereEqualTo("userUid", AUTH.getUid()).whereEqualTo("status", "PENDING").orderBy("timestamp", Query.Direction.ASCENDING);
        }
        else {
            qryRequests = refRequests.whereEqualTo("userUid", AUTH.getUid()).whereIn("status", Arrays.asList("COMPLETED", "DECLINED")).orderBy("timestamp", Query.Direction.ASCENDING);
        }

        qryRequests.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                arrRequests.clear();

                if (queryDocumentSnapshots == null) {
                    return;
                }

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    Request request = documentSnapshot.toObject(Request.class);

                    arrRequests.add(request);
                    requestsAdapter.notifyDataSetChanged();
                }

                if (arrRequests.isEmpty()) {
                    rvDocumentRequests.setVisibility(View.GONE);
                }
                else {
                    rvDocumentRequests.setVisibility(View.VISIBLE);
                }
            }
        });

        requestsAdapter = new RequestsAdapter(getContext(), arrRequests, onRequestsListener);
        rvDocumentRequests.setAdapter(requestsAdapter);
        requestsAdapter.notifyDataSetChanged();
    }

    private void handleUserInteractions() {
        tlRequestStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadRecyclerView(tlRequestStatus.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btnRequestNewDocument.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
            Fragment selectDocumentFragment = new SelectDocumentFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, selectDocumentFragment, "SELECT_DOCUMENT_FRAGMENT");
            fragmentTransaction.addToBackStack("SELECT_DOCUMENT_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onRequestsClick(int position) {

    }
}