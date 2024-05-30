package com.barangay360.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.barangay360.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequirementsDialog extends AppCompatDialogFragment {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;
    Query qryUser;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    TextView tvDialogTItle, tvRequirements;
    MaterialButton btnBack;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_requirements, null);

        initializeFirebase();
        initialize(view);
        loadRequirements();
        buttonHandler();

        builder.setView(view);
        return builder.create();
    }

    private void loadRequirements() {
        String title = "";
        String requirements = "";
        if (Objects.equals(getArguments().getString("documentType"), "CLEARANCE")) {
            title = "Requirements for Barangay Clearance";
            requirements = "• VALID ID: Please provide a valid ID such as a passport, driver's license, or any government-issued ID card to verify your identity." +
                    "\n• PROOF OF RESIDENCY: Please submit a document that confirms your residency in the barangay, such as a recent utility bill or lease agreement." +
                    "\n• CEDULA: You will also need to present a current cedula (community tax certificate), which can be obtained from the local government office.";
        }
        else if (Objects.equals(getArguments().getString("documentType"), "RESIDENCY")) {
            title = "Requirements for Certificate of Residency";
            requirements = "• VALID ID: Please provide a valid ID such as a passport, driver's license, or any government-issued ID card to verify your identity." +
                    "\n• PROOF OF RESIDENCY: Please submit a document that confirms your residency in the barangay, such as a recent utility bill or lease agreement." +
                    "\n• BARANGAY CLEARANCE: Before applying for the Certificate of Residency, please obtain a barangay clearance from the barangay office. You can also do so by requesting through the Ariba Cabangahan app." +
                    "\n• CEDULA: You will also need to present a current cedula (community tax certificate), which can be obtained from the local government office.";
        }
        else if (Objects.equals(getArguments().getString("documentType"), "INDIGENCY")) {
            title = "Requirements for Certificate of Indigency";
            requirements = "• VALID ID: Please provide a valid ID such as a passport, driver's license, or any government-issued ID card to verify your identity." +
                    "\n• PROOF OF RESIDENCY: Please submit a document that confirms your residency in the barangay, such as a recent utility bill or lease agreement." +
                    "\n• FINANCIAL STATEMENT: Please submit a statement or affidavit detailing your income and financial status to support your application for a Certificate of Indigency." +
                    "\n• SUPPORTING DOCUMENTS: You may also include supporting documents like a certification of unemployment or medical certificate to further support your application." +
                    "\n• BARANGAY OFFICIAL ENDORSEMENT: Please secure an endorsement from a barangay official who can attest to your indigent status.";
        }
        tvDialogTItle.setText(title);
        tvRequirements.setText(requirements);
    }

    private void initialize(View view) {
        tvDialogTItle = view.findViewById(R.id.tvDialogTItle);
        tvRequirements = view.findViewById(R.id.tvRequirements);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void buttonHandler() {
        btnBack.setOnClickListener(view -> dismiss());
    }
}
