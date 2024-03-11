package com.barangay360.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.barangay360.AuthenticationActivity;
import com.barangay360.MainActivity;
import com.barangay360.R;
import com.barangay360.Utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firestore.v1.DocumentTransform;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormBusinessPermitFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    TextInputEditText etFirstName, etMiddleName, etLastName, etBusinessName, etAddressPurok;
    MaterialButton btnSubmit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_form_business_permit, container, false);

        initializeFirebase();
        initializeViews();
        autoFillForm();
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        etFirstName = view.findViewById(R.id.etFirstName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etLastName = view.findViewById(R.id.etLastName);
        etBusinessName = view.findViewById(R.id.etBusinessName);
        etAddressPurok = view.findViewById(R.id.etAddressPurok);
        btnSubmit = view.findViewById(R.id.btnSubmit);
    }

    private void autoFillForm() {
        DB.collection("users").document(AUTH.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        String firstName = snapshot.get("firstName").toString();
                        String middleName = snapshot.get("middleName").toString();
                        String lastName = snapshot.get("lastName").toString();
                        String addressPurok = snapshot.get("addressPurok").toString();

                        etFirstName.setText(firstName);
                        etMiddleName.setText(middleName);
                        etLastName.setText(lastName);
                        etAddressPurok.setText(addressPurok);
                    }
                });
    }

    private void handleUserInteraction() {
        btnSubmit.setOnClickListener(view -> validateBusinessPermitForm());
    }

    private void validateBusinessPermitForm() {
        btnSubmit.setEnabled(false);
        String firstName = etFirstName.getText().toString().toUpperCase();
        String middleName = etMiddleName.getText().toString().toUpperCase();
        String lastName = etLastName.getText().toString().toUpperCase();
        String businessName = etBusinessName.getText().toString().toUpperCase();
        String addressPurok = etAddressPurok.getText().toString().toUpperCase();

        if (firstName.isEmpty() ||
            middleName.isEmpty() ||
            lastName.isEmpty() ||
            businessName.isEmpty() ||
            addressPurok.isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        Map<String, Object> businessPermitRequest = new HashMap<>();
        businessPermitRequest.put("userUid", AUTH.getUid());
        businessPermitRequest.put("firstName", firstName);
        businessPermitRequest.put("middleName", middleName);
        businessPermitRequest.put("lastName", lastName);
        businessPermitRequest.put("businessName", businessName);
        businessPermitRequest.put("addressPurok", addressPurok);
        businessPermitRequest.put("timestamp", System.currentTimeMillis());
        businessPermitRequest.put("status", "PENDING");
        businessPermitRequest.put("documentType", "BUSINESS_PERMIT");

        DocumentReference refRequest =  DB.collection("requests").document();

        businessPermitRequest.put("uid", refRequest.getId());

        refRequest.set(businessPermitRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        requireActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnSubmit.setEnabled(true);
                    }
                });
    }
}