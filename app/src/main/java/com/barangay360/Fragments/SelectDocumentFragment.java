package com.barangay360.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.barangay360.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SelectDocumentFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    MaterialButton btnClearance, btnResidency, btnIndigency, btnBusinessPermit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_select_document, container, false);

        initializeFirebase();
        initializeViews();
        handleUserInteraction();

        return view;
    }

    private void handleUserInteraction() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        btnClearance.setOnClickListener(view -> {
            Fragment formClearanceFragment = new FormClearanceFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formClearanceFragment, "CLEARANCE_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("SELECT_DOCUMENT_FRAGMENT");
            fragmentTransaction.commit();
        });

        btnResidency.setOnClickListener(view -> {
            Fragment formResidencyFragment = new FormResidencyFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formResidencyFragment, "RESIDENCY_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("RESIDENCY_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });

        btnIndigency.setOnClickListener(view -> {
            Fragment formIndigencyFragment = new FormIndigencyFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formIndigencyFragment, "INDIGENCY_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("INDIGENCY_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });

        btnBusinessPermit.setOnClickListener(view -> {
            Fragment formBusinessPermitFragment = new FormBusinessPermitFragment();
            fragmentTransaction.replace(R.id.fragmentHolder, formBusinessPermitFragment, "BUSINESS_PERMIT_FORM_FRAGMENT");
            fragmentTransaction.addToBackStack("BUSINESS_PERMIT_FORM_FRAGMENT");
            fragmentTransaction.commit();
        });
    }

    private void initializeViews() {
        btnClearance = view.findViewById(R.id.btnClearance);
        btnResidency = view.findViewById(R.id.btnResidency);
        btnIndigency = view.findViewById(R.id.btnIndigency);
        btnBusinessPermit = view.findViewById(R.id.btnBusinessPermit);
    }
}