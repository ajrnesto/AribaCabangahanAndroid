package com.barangay360.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    View view;
    TextView tvFullname, tvEmail;
    TextInputEditText etSignupFirstName, etSignupMiddleName, etSignupLastName, etSignupBirthdate, etAddressPurok, etSignupMobile;
    AutoCompleteTextView menuCivilStatus, menuResidencyStatus;
    MaterialButton btnSave, btnLogOut;

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
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeFirebase();
        initializeViews();
        initializeSpinners();
        initializeDatePicker();
        loadUserInformation();
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        tvFullname = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        etSignupFirstName = view.findViewById(R.id.etSignupFirstName);
        etSignupMiddleName = view.findViewById(R.id.etSignupMiddleName);
        etSignupLastName = view.findViewById(R.id.etSignupLastName);
        etSignupBirthdate = view.findViewById(R.id.etSignupBirthdate);
        etAddressPurok = view.findViewById(R.id.etAddressPurok);
        etSignupMobile = view.findViewById(R.id.etSignupMobile);
        menuCivilStatus = view.findViewById(R.id.menuCivilStatus);
        menuResidencyStatus = view.findViewById(R.id.menuResidencyStatus);
        btnSave = view.findViewById(R.id.btnSave);
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

    private void loadUserInformation() {
        DB.collection("users").document(USER.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String firstName = snapshot.get("firstName").toString();
                    String middleName = snapshot.get("middleName").toString();
                    String lastName = snapshot.get("lastName").toString();
                    long birthdate = Long.parseLong(snapshot.get("birthdate").toString());
                    String mobile = snapshot.get("mobile").toString();
                    String civilStatus = snapshot.get("civilStatus").toString();
                    String addressPurok = snapshot.get("addressPurok").toString();
                    String residencyStatus = snapshot.get("residencyStatus").toString();

                    dpScheduleSelection = birthdate;

                    tvFullname.setText(firstName + " " + lastName);
                    tvEmail.setText(USER.getEmail());
                    etSignupFirstName.setText(firstName);
                    etSignupMiddleName.setText(middleName);
                    etSignupLastName.setText(lastName);
                    etSignupBirthdate.setText(new SimpleDateFormat("MMMM dd, yyyy").format(birthdate));
                    etAddressPurok.setText(addressPurok);
                    etSignupMobile.setText(mobile);
                    menuCivilStatus.setText(civilStatus, false);
                    menuResidencyStatus.setText(residencyStatus, false);
                });
    }

    private void initializeDatePicker() {
        bSchedule = MaterialDatePicker.Builder.datePicker();
        bSchedule.setTitleText("Select Birthdate")
                .setSelection(System.currentTimeMillis());
        dpSchedule = bSchedule.build();
        dpSchedule.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            dpScheduleSelection = dpSchedule.getSelection();
            etSignupBirthdate.setText(sdf.format(dpScheduleSelection).toUpperCase(Locale.ROOT));
            etSignupBirthdate.setEnabled(true);
        });
        dpSchedule.addOnNegativeButtonClickListener(view -> {
            etSignupBirthdate.setEnabled(true);
        });
        dpSchedule.addOnCancelListener(dialogInterface -> {
            etSignupBirthdate.setEnabled(true);
        });
    }

    private void handleUserInteraction() {
        btnSave.setOnClickListener(view -> validateUserInformationForm());

        btnLogOut.setOnClickListener(view -> signOut());

        etSignupBirthdate.setOnClickListener(view -> {
            etSignupBirthdate.setEnabled(false);
            dpSchedule.show(requireActivity().getSupportFragmentManager(), "SCHEDULE_DATE_PICKER");
        });
    }

    private void signOut() {
        AUTH.signOut();
        requireActivity().startActivity(new Intent(requireActivity(), AuthenticationActivity.class));
        requireActivity().finish();
    }

    private void validateUserInformationForm() {
        if (etSignupFirstName.getText().toString().isEmpty() ||
                etSignupMiddleName.getText().toString().isEmpty() ||
                etSignupLastName.getText().toString().isEmpty() ||
                etSignupBirthdate.getText().toString().isEmpty() ||
                etSignupMobile.getText().toString().isEmpty() ||
                etAddressPurok.getText().toString().isEmpty() ||
                menuCivilStatus.getText().toString().isEmpty() ||
                menuResidencyStatus.getText().toString().isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etSignupFirstName.getText().toString().toUpperCase();
        String middleName = etSignupMiddleName.getText().toString().toUpperCase();
        String lastName = etSignupLastName.getText().toString().toUpperCase();
        long birthdate = dpScheduleSelection;
        String mobile = etSignupMobile.getText().toString().toUpperCase();
        String addressPurok = etAddressPurok.getText().toString().toUpperCase();
        String civilStatus = menuCivilStatus.getText().toString();
        String residencyStatus = menuResidencyStatus.getText().toString();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("middleName", middleName);
        userInfo.put("lastName", lastName);
        userInfo.put("birthdate", birthdate);
        userInfo.put("mobile", mobile);
        userInfo.put("addressPurok", addressPurok);
        userInfo.put("civilStatus", civilStatus);
        userInfo.put("residencyStatus", residencyStatus);

        DB.collection("users").document(AUTH.getUid())
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(requireActivity(), MainActivity.class));
                        Utils.Cache.setInt(requireActivity(), "user_type", 0);
                        requireActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireActivity(), "Registration error: "+e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}