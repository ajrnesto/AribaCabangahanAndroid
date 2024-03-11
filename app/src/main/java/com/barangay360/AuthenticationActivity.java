package com.barangay360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.barangay360.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    // authentication views
    ConstraintLayout clLogin;
    TextInputEditText etLoginEmail, etLoginPassword;
    MaterialButton btnLogin, btnGotoSignup;
    
    // registration views
    ConstraintLayout clSignup;
    TextInputEditText etSignupFirstName, etSignupMiddleName, etSignupLastName, etSignupBirthdate, etAddressPurok, etSignupMobile, etSignupEmail, etSignupPassword;
    AutoCompleteTextView menuCivilStatus, menuResidencyStatus;
    MaterialButton btnSignup, btnGotoLogin;
    
    // date picker items
    MaterialDatePicker.Builder<Long> bSchedule;
    MaterialDatePicker<Long> dpSchedule;
    long dpScheduleSelection = 0;

    // Spinner items
    String[] itemsCivilStatus, itemsResidencyStatus, itemsBarangays;
    ArrayAdapter<String> adapterCivilStatus, adapterResidencyStatus, adapterBarangays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        initializeFirebase();
        checkPreviousLoggedSession();
        initializeViews();
        initializeSpinners();
        initializeDatePicker();
        handleUserInteractions();
    }

    private void checkPreviousLoggedSession() {
        if (USER != null) {
            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initializeSpinners() {
        // civil status
        itemsCivilStatus = new String[]{"SINGLE", "MARRIED", "WIDOWED", "SEPARATED"};
        adapterCivilStatus = new ArrayAdapter<>(this, R.layout.list_item, itemsCivilStatus);
        menuCivilStatus.setAdapter(adapterCivilStatus);

        // residency
        itemsResidencyStatus = new String[]{"PERMANENTLY RESIDING", "PRESENTLY RESIDING"};
        adapterResidencyStatus = new ArrayAdapter<>(this, R.layout.list_item, itemsResidencyStatus);
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

    private void handleUserInteractions() {
        btnSignup.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            validateRegistrationForm();
        });

        btnLogin.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            validateAuthenticationForm();
        });

        btnGotoSignup.setOnClickListener(view -> {
            clLogin.setVisibility(View.GONE);
            clSignup.setVisibility(View.VISIBLE);
        });

        btnGotoLogin.setOnClickListener(view -> {
            clLogin.setVisibility(View.VISIBLE);
            clSignup.setVisibility(View.GONE);
        });

        etSignupBirthdate.setOnClickListener(view -> {
            etSignupBirthdate.setEnabled(false);
            dpSchedule.show(getSupportFragmentManager(), "SCHEDULE_DATE_PICKER");
        });
    }

    private void validateAuthenticationForm() {
        if (etLoginEmail.getText().toString().isEmpty() ||
            etLoginPassword.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        AUTH.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthenticationActivity.this, "Signed in as "+email, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                        finish();
                    }
                    else {
                        Utils.basicDialog(this, "Incorrect email or password.", "Try again");
                        btnLogin.setEnabled(true);
                    }
                });
    }

    private void validateRegistrationForm() {
        if (etSignupFirstName.getText().toString().isEmpty() ||
            etSignupMiddleName.getText().toString().isEmpty() ||
            etSignupLastName.getText().toString().isEmpty() ||
            etSignupBirthdate.getText().toString().isEmpty() ||
            etSignupMobile.getText().toString().isEmpty() ||
            etAddressPurok.getText().toString().isEmpty() ||
            etSignupEmail.getText().toString().isEmpty() ||
            etSignupPassword.getText().toString().isEmpty() ||
            menuCivilStatus.getText().toString().isEmpty() ||
            menuResidencyStatus.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etSignupFirstName.getText().toString().toUpperCase();
        String middleName = etSignupMiddleName.getText().toString().toUpperCase();
        String lastName = etSignupLastName.getText().toString().toUpperCase();
        long birthdate = dpScheduleSelection;
        String mobile = etSignupMobile.getText().toString().toUpperCase();
        String addressPurok = etAddressPurok.getText().toString().toUpperCase();
        String email = etSignupEmail.getText().toString();
        String password = etSignupPassword.getText().toString();
        String civilStatus = menuCivilStatus.getText().toString();
        String residencyStatus = menuResidencyStatus.getText().toString();

        if (password.length() < 6) {
            Utils.basicDialog(this, "Please use a password with at least 6 characters.", "Okay");
            return;
        }

        btnSignup.setEnabled(false);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("middleName", middleName);
        userInfo.put("lastName", lastName);
        userInfo.put("birthdate", birthdate);
        userInfo.put("mobile", mobile);
        userInfo.put("addressPurok", addressPurok);
        userInfo.put("email", email);
        userInfo.put("civilStatus", civilStatus);
        userInfo.put("residencyStatus", residencyStatus);

        AUTH.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userInfo.put("uid", AUTH.getUid());
                        DB.collection("users").document(AUTH.getUid())
                                .set(userInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                                            Utils.Cache.setInt(AuthenticationActivity.this, "user_type", 0);
                                            finish();
                                            btnSignup.setEnabled(true);
                                        }
                                        else {
                                            Toast.makeText(AuthenticationActivity.this, "Registration error: "+task.getException(), Toast.LENGTH_SHORT).show();
                                            btnSignup.setEnabled(true);
                                        }
                                    }
                                });
                    }
                    else {
                        Utils.basicDialog(this, "Something went wrong when trying to create your account.", "Try again");
                        btnSignup.setEnabled(true);
                    }
                });
    }

    private void initializeViews() {
        clLogin = findViewById(R.id.clLogin);
        clSignup = findViewById(R.id.clSignup);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGotoSignup = findViewById(R.id.btnGotoSignup);
        etSignupFirstName = findViewById(R.id.etSignupFirstName);
        etSignupMiddleName = findViewById(R.id.etSignupMiddleName);
        etSignupLastName = findViewById(R.id.etSignupLastName);
        etSignupBirthdate = findViewById(R.id.etSignupBirthdate);
        etSignupMobile = findViewById(R.id.etSignupMobile);
        etAddressPurok = findViewById(R.id.etAddressPurok);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        menuCivilStatus = findViewById(R.id.menuCivilStatus);
        menuResidencyStatus = findViewById(R.id.menuResidencyStatus);
        btnSignup = findViewById(R.id.btnSignup);
        btnGotoLogin = findViewById(R.id.btnGotoLogin);
    }
}