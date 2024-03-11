package com.barangay360.Fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Adapters.ImagePreviewAdapter;
import com.barangay360.Adapters.ImagePreviewAdapter;
import com.barangay360.Adapters.InvolvedPersonAdapter;
import com.barangay360.Objects.InvolvedPerson;
import com.barangay360.Objects.Request;
import com.barangay360.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FormIncidentReportFragment extends Fragment implements ImagePreviewAdapter.OnImagePreviewListener, InvolvedPersonAdapter.OnInvolvedPersonListener {

    FirebaseFirestore DB;
    FirebaseStorage STORAGE;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        STORAGE = FirebaseStorage.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    ArrayList<Uri> arrUri = new ArrayList<>();
    ArrayList<InvolvedPerson> arrInvolvedPerson = new ArrayList<>();
    ImagePreviewAdapter imagePreviewAdapter;
    ImagePreviewAdapter.OnImagePreviewListener onImagePreviewListener = this;
    InvolvedPersonAdapter involvedPersonAdapter;
    InvolvedPersonAdapter.OnInvolvedPersonListener onInvolvedPersonListener = this;

    ActivityResultLauncher<Intent> activityResultLauncher;
    int SELECT_IMAGE_CODE = 1;

    View view;
    ConstraintLayout clProgress;

    CircularProgressIndicator progressUploading;
    TextView tvUploading;
    TextInputEditText etIncidentDate, etLocationPurok, etIncidentDetails;
    AutoCompleteTextView menuIncidentType;
    RecyclerView rvMedia, rvInvolvedPersons;
    MaterialButton btnAddInvolvedPerson, btnAddMedia, btnSubmit;

    // date picker items
    MaterialDatePicker.Builder<Long> IncidentDate;
    MaterialDatePicker<Long> dpIncidentDate;
    long dpIncidentDateSelection = 0;

    // Spinner items
    String[] itemsIncidentType;
    ArrayAdapter<String> adapterIncidentType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_form_incident_report, container, false);

        initializeFirebase();
        initializeViews();
        initializeActivityResultLauncher();
        initializeSpinners();
        initializeDatePicker();
        handleUserInteraction();

        return view;
    }

    private void initializeViews() {
        clProgress = view.findViewById(R.id.clProgress);
        progressUploading = view.findViewById(R.id.progressUploading);
        tvUploading = view.findViewById(R.id.tvUploading);
        etIncidentDate = view.findViewById(R.id.etIncidentDate);
        etLocationPurok = view.findViewById(R.id.etLocationPurok);
        etIncidentDetails = view.findViewById(R.id.etIncidentDetails);
        menuIncidentType = view.findViewById(R.id.menuIncidentType);
        rvMedia = view.findViewById(R.id.rvMedia);
        rvInvolvedPersons = view.findViewById(R.id.rvInvolvedPersons);
        btnAddInvolvedPerson = view.findViewById(R.id.btnAddInvolvedPerson);
        btnAddMedia = view.findViewById(R.id.btnAddMedia);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        rvMedia.setHasFixedSize(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        rvMedia.setLayoutManager(gridLayoutManager);
        imagePreviewAdapter = new ImagePreviewAdapter(requireContext(), arrUri, onImagePreviewListener);
        rvMedia.setAdapter(imagePreviewAdapter);

        rvInvolvedPersons.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        rvInvolvedPersons.setLayoutManager(linearLayoutManager);
        involvedPersonAdapter = new InvolvedPersonAdapter(requireContext(), arrInvolvedPerson, onInvolvedPersonListener);
        rvInvolvedPersons.setAdapter(involvedPersonAdapter);

        // prefill complainant
        DB.collection("users").document(AUTH.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "yep", Toast.LENGTH_SHORT).show();
                    String firstName = task.getResult().getString("firstName");
                    String middleName = task.getResult().getString("middleName");
                    String lastName = task.getResult().getString("lastName");
                    String addressPurok = task.getResult().getString("addressPurok");

                    arrInvolvedPerson.add(new InvolvedPerson(firstName + " " + middleName.charAt(0) + ". " + lastName, addressPurok + ", CABANGAHAN, SIATON", "COMPLAINANT"));
                    involvedPersonAdapter.notifyItemInserted(0);
                }
            }
        });
    }

    private void initializeSpinners() {
        // civil status
        itemsIncidentType = new String[]{"ANIMAL CONTROL", "DOMESTIC DISPUTES", "FIRE INCIDENT", "HARASSMENT AND THREAT", "ILLEGAL PARKING", "MISSING PERSONS", "NOISE COMPLAINTS", "PUBLIC DISTURBANCES", "THEFT AND BURGLARY", "TRAFFIC ACCIDENTS", "VANDALISM", "OTHER"};
        adapterIncidentType = new ArrayAdapter<>(requireContext(), R.layout.list_item, itemsIncidentType);
        menuIncidentType.setAdapter(adapterIncidentType);
    }

    private void initializeDatePicker() {
        IncidentDate = MaterialDatePicker.Builder.datePicker();
        IncidentDate.setTitleText("Select Incident Date")
                .setSelection(System.currentTimeMillis());
        dpIncidentDate = IncidentDate.build();
        dpIncidentDate.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
            dpIncidentDateSelection = dpIncidentDate.getSelection();
            etIncidentDate.setText(sdf.format(dpIncidentDateSelection).toUpperCase(Locale.ROOT));
            etIncidentDate.setEnabled(true);
        });
        dpIncidentDate.addOnNegativeButtonClickListener(view -> {
            etIncidentDate.setEnabled(true);
        });
        dpIncidentDate.addOnCancelListener(dialogInterface -> {
            etIncidentDate.setEnabled(true);
        });
    }

    private void handleUserInteraction() {
        btnAddInvolvedPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrInvolvedPerson.add(new InvolvedPerson("", "", ""));
                involvedPersonAdapter.notifyItemInserted(arrInvolvedPerson.size()-1);
            }
        });

        btnAddMedia.setOnClickListener(view -> selectImageFromDevice());

        btnSubmit.setOnClickListener(view -> validateIncidentReportForm());

        etIncidentDate.setOnClickListener(view -> {
            etIncidentDate.setEnabled(false);
            dpIncidentDate.show(requireActivity().getSupportFragmentManager(), "INCIDENT_DATE_PICKER");
        });
    }

    private void validateIncidentReportForm() {
        btnSubmit.setEnabled(false);
        if (menuIncidentType.getText().toString().isEmpty() ||
                etIncidentDate.getText().toString().isEmpty() ||
                etLocationPurok.getText().toString().isEmpty() ||
                etIncidentDetails.getText().toString().isEmpty())
        {
            Toast.makeText(requireContext(), "Please fill out all the text fields", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        // validate involved persons
        if (arrInvolvedPerson.size() == 0) {
            Toast.makeText(requireContext(), "Please add at least 1 person involved", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }
        for (int i = 0; i < rvInvolvedPersons.getAdapter().getItemCount(); i++) {
            View itemView = rvInvolvedPersons.getChildAt(i);
            TextInputEditText etFullName = itemView.findViewById(R.id.etFullName);
            TextInputEditText etFullAddress = itemView.findViewById(R.id.etFullAddress);
            AutoCompleteTextView menuInvolvement = itemView.findViewById(R.id.menuInvolvement);

            String fullName = etFullName.getText().toString().toUpperCase();
            String fullAddress = etFullAddress.getText().toString().toUpperCase();
            String involvement = menuInvolvement.getText().toString().toUpperCase();

            if (fullName.isEmpty() || fullAddress.isEmpty() || involvement.isEmpty()) {
                Toast.makeText(requireContext(), "Please complete all involved persons' information", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                return;
            }
            arrInvolvedPerson.get(i).setFullName(fullName);
            arrInvolvedPerson.get(i).setFullAddress(fullAddress);
            arrInvolvedPerson.get(i).setInvolvement(involvement);

            Log.d("TAG", "etFullName: "+etFullName.getText().toString().toUpperCase()+"\n etFullAddress: "+etFullAddress.getText().toString().toUpperCase());
        }

        String incidentType = menuIncidentType.getText().toString().toUpperCase();
        long incidentDate = dpIncidentDateSelection;
        String locationPurok = etLocationPurok.getText().toString().toUpperCase();
        String incidentDetails = etIncidentDetails.getText().toString().toUpperCase();

        // store media file names
        ArrayList<String> arrMediaFileNames = new ArrayList<>();
        for (Uri uri : arrUri) {
            arrMediaFileNames.add(uri.getLastPathSegment()+System.currentTimeMillis());
        }

        if (arrMediaFileNames.size() == 0) {
            Map<String, Object> incidentReport = new HashMap<>();
            incidentReport.put("userUid", AUTH.getUid());
            incidentReport.put("incidentType", incidentType);
            incidentReport.put("incidentDate", incidentDate);
            incidentReport.put("locationPurok", locationPurok);
            incidentReport.put("incidentDetails", incidentDetails);
            incidentReport.put("involvedPersons", arrInvolvedPerson);
            incidentReport.put("mediaFileNames", arrMediaFileNames);
            incidentReport.put("timestamp", System.currentTimeMillis());
            incidentReport.put("status", "PENDING");

            DocumentReference refIncident =  DB.collection("incidents").document();
            incidentReport.put("uid", refIncident.getId());

            refIncident.set(incidentReport)
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
        else {
            Map<String, Object> incidentReport = new HashMap<>();
            incidentReport.put("userUid", AUTH.getUid());
            incidentReport.put("incidentType", incidentType);
            incidentReport.put("incidentDate", incidentDate);
            incidentReport.put("locationPurok", locationPurok);
            incidentReport.put("incidentDetails", incidentDetails);
            incidentReport.put("involvedPersons", arrInvolvedPerson);
            incidentReport.put("mediaFileNames", arrMediaFileNames);
            incidentReport.put("timestamp", System.currentTimeMillis());
            incidentReport.put("status", "PENDING");

            // upload all media to firebase storage
            final int[] filesUploaded = {0};
            for (int i = 0; i < arrUri.size(); i++) {
                Uri uri = arrUri.get(i);
                clProgress.setVisibility(View.VISIBLE);
                tvUploading.setText("Uploading media ("+ filesUploaded[0] +"/"+arrUri.size()+")");

                StorageReference refMedia = STORAGE.getReference().child("media/"+arrMediaFileNames.get(i));

                UploadTask uploadTask = refMedia.putFile(uri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Upload failed: "+e, Toast.LENGTH_SHORT).show();

                        clProgress.setVisibility(View.GONE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filesUploaded[0]++;
                        tvUploading.setText("Uploading media ("+ filesUploaded[0] +"/"+arrUri.size()+")");

                        if (filesUploaded[0] == arrUri.size()) {
                            clProgress.setVisibility(View.GONE);

                            DocumentReference refIncident =  DB.collection("incidents").document();
                            incidentReport.put("uid", refIncident.getId());

                            refIncident.set(incidentReport)
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
                });
            }
        }
    }

    private void selectImageFromDevice() {
        Intent iImageSelect = new Intent();
        iImageSelect.setType("image/* video/*");
        iImageSelect.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        iImageSelect.setAction(Intent.ACTION_PICK);

        activityResultLauncher.launch(Intent.createChooser(iImageSelect, "Upload supporting documents"));
    }

    private void initializeActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        int count = result.getData().getClipData().getItemCount();

                        for (int i = 0; i < count; i++) {
                            Uri uriMedia = result.getData().getClipData().getItemAt(i).getUri();
                            arrUri.add(uriMedia);

                            imagePreviewAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );
    }

    @Override
    public void onImagePreviewClick(int position) {
        arrUri.remove(position);
        imagePreviewAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onInvolvedPersonListener(int position) {
        if (arrInvolvedPerson.size() > 0) {
            View itemView = rvInvolvedPersons.getChildAt(position);

            TextInputEditText etFullName = itemView.findViewById(R.id.etFullName);
            TextInputEditText etFullAddress = itemView.findViewById(R.id.etFullAddress);
            etFullName.getText().clear();
            etFullAddress.getText().clear();

            arrInvolvedPerson.remove(position);
            involvedPersonAdapter.notifyItemRemoved(position);
        }
    }
}