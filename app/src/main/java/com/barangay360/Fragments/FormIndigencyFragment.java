package com.barangay360.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.barangay360.R;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormIndigencyFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }
    View view;

    TextInputEditText etFirstName, etMiddleName, etLastName, etBirthdate, etAddressPurok;
    AutoCompleteTextView menuCivilStatus, menuResidencyStatus;
    MaterialButton btnSubmit;

    // date picker items
    MaterialDatePicker.Builder<Long> bSchedule;
    MaterialDatePicker<Long> dpSchedule;
    long dpScheduleSelection = 0;

    // Spinner items
    String[] itemsCivilStatus, itemsResidencyStatus;
    ArrayAdapter<String> adapterCivilStatus, adapterResidencyStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_form_indigency, container, false);

        initializeFirebase();
        initializeViews();
        initializeSpinners();
        initializeDatePicker();
        autoFillForm();
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        etFirstName = view.findViewById(R.id.etFirstName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etLastName = view.findViewById(R.id.etLastName);
        etBirthdate = view.findViewById(R.id.etBirthdate);
        menuCivilStatus = view.findViewById(R.id.menuCivilStatus);
        etAddressPurok = view.findViewById(R.id.etAddressPurok);
        menuResidencyStatus = view.findViewById(R.id.menuResidencyStatus);
        btnSubmit = view.findViewById(R.id.btnSubmit);
    }

    private void initializeSpinners() {
        // civil status
        itemsCivilStatus = new String[]{"SINGLE", "MARRIED", "WIDOWED", "SEPARATED"};
        adapterCivilStatus = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsCivilStatus);
        menuCivilStatus.setAdapter(adapterCivilStatus);

        // residency
        itemsResidencyStatus = new String[]{"PERMANENTLY RESIDING", "PRESENTLY RESIDING"};
        adapterResidencyStatus = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsResidencyStatus);
        menuResidencyStatus.setAdapter(adapterResidencyStatus);
    }

    private void initializeDatePicker() {
        bSchedule = MaterialDatePicker.Builder.datePicker();
        bSchedule.setTitleText("Select Birthdate")
                .setSelection(System.currentTimeMillis());
        dpSchedule = bSchedule.build();
        dpSchedule.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            dpScheduleSelection = dpSchedule.getSelection();
            etBirthdate.setText(sdf.format(dpScheduleSelection).toUpperCase(Locale.ROOT));
            etBirthdate.setEnabled(true);
        });
        dpSchedule.addOnNegativeButtonClickListener(view -> {
            etBirthdate.setEnabled(true);
        });
        dpSchedule.addOnCancelListener(dialogInterface -> {
            etBirthdate.setEnabled(true);
        });
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
                        long birthdate = Long.parseLong(snapshot.get("birthdate").toString());
                        String civilStatus = snapshot.get("civilStatus").toString();
                        String addressPurok = snapshot.get("addressPurok").toString();
                        String residencyStatus = snapshot.get("residencyStatus").toString();

                        dpScheduleSelection = birthdate;

                        etFirstName.setText(firstName);
                        etMiddleName.setText(middleName);
                        etLastName.setText(lastName);
                        etBirthdate.setText(new SimpleDateFormat("MMMM dd, yyyy").format(birthdate));
                        etAddressPurok.setText(addressPurok);
                        menuCivilStatus.setText(civilStatus, false);
                        menuResidencyStatus.setText(residencyStatus, false);
                    }
                });
    }

    private void handleUserInteraction() {
        btnSubmit.setOnClickListener(view -> validateIndigencyForm());

        etBirthdate.setOnClickListener(view -> {
            etBirthdate.setEnabled(false);
            dpSchedule.show(requireActivity().getSupportFragmentManager(), "SCHEDULE_DATE_PICKER");
        });
    }

    private void validateIndigencyForm() {
        btnSubmit.setEnabled(false);
        if (etFirstName.getText().toString().isEmpty() ||
                etMiddleName.getText().toString().isEmpty() ||
                etLastName.getText().toString().isEmpty() ||
                etBirthdate.getText().toString().isEmpty() ||
                etAddressPurok.getText().toString().isEmpty() ||
                menuCivilStatus.getText().toString().isEmpty() ||
                menuResidencyStatus.getText().toString().isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        String firstName = etFirstName.getText().toString().toUpperCase();
        String middleName = etMiddleName.getText().toString().toUpperCase();
        String lastName = etLastName.getText().toString().toUpperCase();
        long birthdate = dpScheduleSelection;
        String addressPurok = etAddressPurok.getText().toString().toUpperCase();
        String civilStatus = menuCivilStatus.getText().toString();
        String residencyStatus = menuResidencyStatus.getText().toString();

        Map<String, Object> indigencyRequest = new HashMap<>();
        indigencyRequest.put("userUid", AUTH.getUid());
        indigencyRequest.put("firstName", firstName);
        indigencyRequest.put("middleName", middleName);
        indigencyRequest.put("lastName", lastName);
        indigencyRequest.put("birthdate", birthdate);
        indigencyRequest.put("addressPurok", addressPurok);
        indigencyRequest.put("civilStatus", civilStatus);
        indigencyRequest.put("residencyStatus", residencyStatus);
        indigencyRequest.put("timestamp", System.currentTimeMillis());
        indigencyRequest.put("status", "PENDING");
        indigencyRequest.put("documentType", "INDIGENCY");

        DocumentReference refRequest =  DB.collection("requests").document();

        indigencyRequest.put("uid", refRequest.getId());

        refRequest.set(indigencyRequest)
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