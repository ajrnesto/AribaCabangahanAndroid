package com.barangay360.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.barangay360.MainActivity;
import com.barangay360.R;
import com.barangay360.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FormClearanceFragment extends Fragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    FirebaseStorage STORAGE;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
        STORAGE = FirebaseStorage.getInstance();
    }
    View view;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ScrollView svForm, svNonResidentRequirements;
    TextInputEditText etFirstName, etMiddleName, etLastName, etBirthdate, etAddressPurok, etLetterOfRequest;
    RoundedImageView imgId, imgCedula;
    AutoCompleteTextView menuCivilStatus, menuResidencyStatus, menuPurpose;
    MaterialRadioButton radioResident;
    MaterialButton btnSubmit, btnUploadId, btnUploadCedula, btnSubmitVerification;

    // date picker items
    MaterialDatePicker.Builder<Long> bSchedule;
    MaterialDatePicker<Long> dpSchedule;
    long dpScheduleSelection = 0;
    Uri uriSelectedForId = null;
    Uri uriSelectedForCedula = null;
    String currentImageSelectCode = "";

    // Spinner items
    String[] itemsCivilStatus, itemsResidencyStatus, itemPurpose;
    ArrayAdapter<String> adapterCivilStatus, adapterResidencyStatus, adapterPurpose;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_form_clearance, container, false);

        initializeFirebase();
        initializeViews();
        initializeActivityResultLauncher();
        initializeSpinners();
        initializeDatePicker();
        autoFillForm();
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        svForm = view.findViewById(R.id.svForm);
        svNonResidentRequirements = view.findViewById(R.id.svNonResidentRequirements);
        etFirstName = view.findViewById(R.id.etFirstName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etLastName = view.findViewById(R.id.etLastName);
        etBirthdate = view.findViewById(R.id.etBirthdate);
        menuCivilStatus = view.findViewById(R.id.menuCivilStatus);
        etAddressPurok = view.findViewById(R.id.etAddressPurok);
        menuResidencyStatus = view.findViewById(R.id.menuResidencyStatus);
        menuPurpose = view.findViewById(R.id.menuPurpose);
        radioResident = view.findViewById(R.id.radioResident);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        // verification
        etLetterOfRequest = view.findViewById(R.id.etLetterOfRequest);
        imgId = view.findViewById(R.id.imgId);
        btnUploadId = view.findViewById(R.id.btnUploadId);
        imgCedula = view.findViewById(R.id.imgCedula);
        btnUploadCedula = view.findViewById(R.id.btnUploadCedula);
        btnSubmitVerification = view.findViewById(R.id.btnSubmitVerification);
    }

    private void initializeSpinners() {
        // civil status
        itemsCivilStatus = new String[]{"SINGLE", "MARRIED", "WIDOWED", "SEPARATED"};
        adapterCivilStatus = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsCivilStatus);
        menuCivilStatus.setAdapter(adapterCivilStatus);

        // residency
        itemsResidencyStatus = new String[]{"PERMANENT ADDRESS", "PRESENT ADDRESS", "BUSINESS ADDRESS"};
        adapterResidencyStatus = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsResidencyStatus);
        menuResidencyStatus.setAdapter(adapterResidencyStatus);

        // purpose
        itemPurpose = new String[]{"EMPLOYMENT", "RESIDENCY VERIFICATION", "BUSINESS PERMIT/LICENSES", "SCHOOL REFERENCE", "OVERSEAS TRAVEL", "MARRIAGE LICENSE", "LOAN APPLICATION"};
        adapterPurpose = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemPurpose);
        menuPurpose.setAdapter(adapterPurpose);
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
        btnSubmit.setOnClickListener(view -> validateClearanceForm());

        btnUploadId.setOnClickListener(view -> {
            currentImageSelectCode = "id";
            selectImageFromDevice();
        });

        btnUploadCedula.setOnClickListener(view -> {
            currentImageSelectCode = "cedula";
            selectImageFromDevice();
        });

        btnSubmitVerification.setOnClickListener(view -> validateVerificationForm());

        etBirthdate.setOnClickListener(view -> {
            etBirthdate.setEnabled(false);
            dpSchedule.show(requireActivity().getSupportFragmentManager(), "SCHEDULE_DATE_PICKER");
        });
    }

    private void validateClearanceForm() {
        btnSubmit.setEnabled(false);
        if (etFirstName.getText().toString().isEmpty() ||
                etLastName.getText().toString().isEmpty() ||
                etBirthdate.getText().toString().isEmpty() ||
                etAddressPurok.getText().toString().isEmpty() ||
                menuPurpose.getText().toString().isEmpty() ||
                menuCivilStatus.getText().toString().isEmpty() ||
                menuResidencyStatus.getText().toString().isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the required fields", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        /*boolean userIsResident = radioResident.isChecked();
        if (!userIsResident) {
            Utils.hideKeyboard(requireActivity());
            svForm.setVisibility(View.GONE);
            svNonResidentRequirements.setVisibility(View.VISIBLE);

            MaterialAlertDialogBuilder dialogNonResident = new MaterialAlertDialogBuilder(requireContext());
            dialogNonResident.setTitle("Verification Required");
            dialogNonResident.setMessage("To proceed with your document request, please note that as a non-resident of Barangay Cabangahan, additional requirements are needed for verification purposes.");
            dialogNonResident.setPositiveButton("I Understand", (dialogInterface, i) -> {});
            dialogNonResident.show();
            return;
        }*/

        submitForm();
    }

    private void validateVerificationForm() {
        btnSubmitVerification.setEnabled(false);
        if (etLetterOfRequest.getText().toString().isEmpty() ||
                uriSelectedForId == null ||
                uriSelectedForCedula == null)
        {
            Toast.makeText(requireContext(), "Please provide all required documents", Toast.LENGTH_SHORT).show();
            btnSubmitVerification.setEnabled(true);
            return;
        }

        submitForm();
    }

    private void submitForm() {
        String firstName = etFirstName.getText().toString().toUpperCase();
        String middleName = etMiddleName.getText().toString().toUpperCase();
        String lastName = etLastName.getText().toString().toUpperCase();
        long birthdate = dpScheduleSelection;
        String addressPurok = etAddressPurok.getText().toString().toUpperCase();
        String purpose = menuPurpose.getText().toString().toUpperCase();
        String civilStatus = menuCivilStatus.getText().toString();
        String residencyStatus = menuResidencyStatus.getText().toString();
        String letterOfRequest;

        Map<String, Object> clearanceRequest = new HashMap<>();
        clearanceRequest.put("userUid", AUTH.getUid());
        clearanceRequest.put("firstName", firstName);
        clearanceRequest.put("middleName", middleName);
        clearanceRequest.put("lastName", lastName);
        clearanceRequest.put("birthdate", birthdate);
        clearanceRequest.put("addressPurok", addressPurok);
        clearanceRequest.put("civilStatus", civilStatus);
        clearanceRequest.put("residencyStatus", residencyStatus);
        clearanceRequest.put("timestamp", System.currentTimeMillis());
        clearanceRequest.put("purpose", purpose);
        clearanceRequest.put("status", "PENDING");
        clearanceRequest.put("documentType", "CLEARANCE");

        DocumentReference refRequest =  DB.collection("requests").document();

        clearanceRequest.put("uid", refRequest.getId());

        boolean userIsResident = radioResident.isChecked();
        if (!userIsResident) {
            // letterOfRequest = etLetterOfRequest.getText().toString();
            clearanceRequest.put("isNonResident", true);
            // clearanceRequest.put("letterOfRequest", letterOfRequest);

            // StorageReference sRefId = STORAGE.getReference().child("id/"+refRequest.getId());
            /*sRefId.putFile(uriSelectedForId).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Failed to upload ID. Please try again.", Toast.LENGTH_SHORT).show();
                        btnSubmitVerification.setEnabled(true);
                    }
                    else if (task.isSuccessful()) {*/
                        // StorageReference sRefCedula = STORAGE.getReference().child("cedula/"+refRequest.getId());
                        /*sRefCedula.putFile(uriSelectedForCedula).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Failed to upload cedula. Please try again.", Toast.LENGTH_SHORT).show();
                                    btnSubmitVerification.setEnabled(true);
                                }
                                else if (task.isSuccessful()) {*/
                                    /*refRequest.set(clearanceRequest)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    requireActivity().onBackPressed();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(requireContext(), "Failed to submit verification. Please try again.", Toast.LENGTH_SHORT).show();
                                                    btnSubmitVerification.setEnabled(true);
                                                }
                                            });*/
                                /*}
                            }
                        })*/;
            /*        }
                }
            });*/
        }
        /*else {
            refRequest.set(clearanceRequest)
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
        }*/
        refRequest.set(clearanceRequest)
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

    private void selectImageFromDevice() {
        Intent iImageSelect = new Intent();
        iImageSelect.setType("image/*");
        iImageSelect.setAction(Intent.ACTION_GET_CONTENT);

        activityResultLauncher.launch(Intent.createChooser(iImageSelect, "Select ID"));
    }

    private void initializeActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uriRetrieved = Objects.requireNonNull(data).getData();

                        if (Objects.equals(currentImageSelectCode, "id")) {
                            uriSelectedForId = uriRetrieved;
                            Picasso.get().load(uriRetrieved).resize(800,0).centerCrop().into(imgId);
                        }
                        else if (Objects.equals(currentImageSelectCode, "cedula")) {
                            uriSelectedForCedula = uriRetrieved;
                            Picasso.get().load(uriRetrieved).resize(800,0).centerCrop().into(imgCedula);
                        }
                    }
                }
        );
    }
}