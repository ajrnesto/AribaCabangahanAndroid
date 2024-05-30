package com.barangay360;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;

import com.barangay360.Fragments.AnnouncementsFragment;
import com.barangay360.Fragments.BlotterFragment;
import com.barangay360.Fragments.CrimeReportFragment;
import com.barangay360.Fragments.DocumentRequestFragment;
import com.barangay360.Fragments.IncidentReportFragment;
import com.barangay360.Fragments.ProfileFragment;
import com.barangay360.Utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseAuth AUTH;
    FirebaseUser USER;

    private void initializeFirebase() {
        DB = FirebaseFirestore.getInstance();
        AUTH = FirebaseAuth.getInstance();
        USER = AUTH.getCurrentUser();
    }

    // BottomNavigationView bottom_navbar;
    MaterialButton btnMenu;
    TextView tvActivityTitle;
    DrawerLayout drawerLayout;
    NavigationView navView;
    View headerView;
    MenuItem miAnnouncements, miDocumentRequests, miIncidentReport, miCrimeReport, miAboutUs, miProfile;

    // handle clicks on menu button
    View.OnClickListener clickListenerMenu = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            drawerLayout.open();
        }
    };

    // handle clicks on back button
    View.OnClickListener clickListenerBack = view -> onBackPressed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNotificationPermsission();
        initializeFirebase();
        initializeViews();
        initializeMenuDrawer();
        handleUserInteraction();
        backstackListener();
        listenForDocumentRequestThatAreReadyForPickup();
        startUpHomeFragment();

        // set home fragment
        /*bottom_navbar.findViewById(R.id.miAnnouncements).performClick();
        tvActivityTitle.setText("Barangay Announcements");*/
    }

    private void startUpHomeFragment() {
        // set "home fragment" as the startup fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment shopFragment = new AnnouncementsFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentHolder, shopFragment, "HOME_FRAGMENT");
        fragmentTransaction.addToBackStack("HOME_FRAGMENT");
        fragmentTransaction.commit();
        navView.setCheckedItem(R.id.miAnnouncements);
    }

    private void listenForDocumentRequestThatAreReadyForPickup() {
        if (USER != null) {
            Query qryReadyRequests = DB.collection("requests")
                    .whereEqualTo("userUid", AUTH.getUid())
                    .whereEqualTo("status", "COMPLETED");
            qryReadyRequests.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    //If something went wrong
                    if (e != null)
                        Log.w("TAG", "ERROR : ", e);

                    int notificationCounter = 0;
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // check individual snapshots for requests that have not yet been notified to the user.
                        for (DocumentSnapshot docSnap : queryDocumentSnapshots) {
                            if (!docSnap.contains("wasNotified")) {
                                DB.collection("requests").document(docSnap.getId())
                                        .update("wasNotified", true);
                                notificationCounter++;
                            }
                        }
                    }

                    if (notificationCounter > 0) {
                        buildNotification();
                    }
                }
            });
        }
    }

    private void checkNotificationPermsission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onBackPressed() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) { // if navigation is at first backstack entry
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        // bottom_navbar = findViewById(R.id.bottom_navbar);
        navView = findViewById(R.id.navView);
        btnMenu = findViewById(R.id.btnMenu);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        drawerLayout = findViewById(R.id.drawerLayout);

        /*miAnnouncements = findViewById(R.id.miAnnouncements);
        miDocumentRequests = findViewById(R.id.miDocumentRequests);
        miIncidentReport = findViewById(R.id.miIncidentReport);
        miCrimeReport = findViewById(R.id.miCrimeReport);
        // miAboutUs = findViewById(R.id.miAboutUs);
        miProfile = findViewById(R.id.miProfile);*/

        tvActivityTitle.setText("Barangay Announcements");
    }

    private void handleUserInteraction() {
        /*bottom_navbar.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
            if (item.getItemId() == R.id.miProfile) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "To submit blotters and report incidents, please log in or sign up first.");
                    return false;
                }
                if (bottom_navbar.getSelectedItemId() != R.id.miProfile) {
                    Fragment profileFragment = new ProfileFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, profileFragment, "PROFILE_FRAGMENT");
                    fragmentTransaction.addToBackStack("PROFILE_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            else if (item.getItemId() == R.id.miCrimeReport) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "To report crimes, please log in or sign up first.");
                    return false;
                }
                if (bottom_navbar.getSelectedItemId() != R.id.miCrimeReport) {
                    Fragment crimeReportFragment = new CrimeReportFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, crimeReportFragment, "CRIME_REPORT_FRAGMENT");
                    fragmentTransaction.addToBackStack("CRIME_REPORT_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            else if (item.getItemId() == R.id.miAnnouncements) {
                Utils.hideKeyboard(this);
                Fragment announcementsFragment = new AnnouncementsFragment();
                fragmentTransaction.replace(R.id.fragmentHolder, announcementsFragment, "ANNOUNCEMENTS_FRAGMENT");
                fragmentTransaction.addToBackStack("ANNOUNCEMENTS_FRAGMENT");
                fragmentTransaction.commit();
            }
            else if (item.getItemId() == R.id.miDocumentRequests) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "To request barangay documents, please log in or sign up first.");
                    return false;
                }
                if (bottom_navbar.getSelectedItemId() != R.id.miDocumentRequests) {
                    Fragment documentRequestFragment = new DocumentRequestFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, documentRequestFragment, "DOCUMENT_REQUEST_FRAGMENT");
                    fragmentTransaction.addToBackStack("DOCUMENT_REQUEST_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            else {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, bottom_navbar, "To set up your profile, please log in or sign up first.");
                    return false;
                }
                if (bottom_navbar.getSelectedItemId() != R.id.miIncidentReport) {
                    Fragment incidentReportFragment = new IncidentReportFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, incidentReportFragment, "INCIDENT_REPORT_FRAGMENT");
                    fragmentTransaction.addToBackStack("INCIDENT_REPORT_FRAGMENT");
                    fragmentTransaction.commit();
                }
            }
            return true;
        });*/

        btnMenu.setIcon(getResources().getDrawable(R.drawable.menu_24));
        btnMenu.setOnClickListener(view -> {
            drawerLayout.open();
        });
    }

    private void initializeMenuDrawer() {
        // listen to navigation drawer item clicks
        navView.setNavigationItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (item.getItemId() == R.id.miAnnouncements) {
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miAnnouncements) {
                    Utils.hideKeyboard(this);
                    Fragment announcementsFragment = new AnnouncementsFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, announcementsFragment, "ANNOUNCEMENTS_FRAGMENT");
                    fragmentTransaction.addToBackStack("ANNOUNCEMENTS_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }
            else if (item.getItemId() == R.id.miDocumentRequests) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "To request barangay documents, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miDocumentRequests) {

                    Fragment documentRequestFragment = new DocumentRequestFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, documentRequestFragment, "DOCUMENT_REQUEST_FRAGMENT");
                    fragmentTransaction.addToBackStack("DOCUMENT_REQUEST_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }
            /*else if (item.getItemId() == R.id.miIncidentReport) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "To submit blotters and report incidents, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miIncidentReport) {

                    Fragment incidentReportFragment = new IncidentReportFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, incidentReportFragment, "INCIDENT_REPORT_FRAGMENT");
                    fragmentTransaction.addToBackStack("INCIDENT_REPORT_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }
            else if (item.getItemId() == R.id.miCrimeReport) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "To report crimes, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miCrimeReport) {

                    Fragment crimeReportFragment = new CrimeReportFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, crimeReportFragment, "CRIME_REPORT_FRAGMENT");
                    fragmentTransaction.addToBackStack("CRIME_REPORT_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }*/
            else if (item.getItemId() == R.id.miCrimesIncidents) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "To report an incident or crime, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miCrimesIncidents) {

                    Fragment crimeReportFragment = new BlotterFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, crimeReportFragment, "BLOTTER_FRAGMENT");
                    fragmentTransaction.addToBackStack("BLOTTER_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }
            else if (item.getItemId() == R.id.miBlotter) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "In order to blotter, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miBlotter) {

                    Fragment incidentReportFragment = new IncidentReportFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, incidentReportFragment, "INCIDENT_REPORT_FRAGMENT");
                    fragmentTransaction.addToBackStack("INCIDENT_REPORT_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }
            /*else if (item.getItemId() == R.id.miAboutUs) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "To request barangay documents, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miAboutUs) {

                    Fragment documentRequestFragment = new DocumentRequestFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, documentRequestFragment, "DOCUMENT_REQUEST_FRAGMENT");
                    fragmentTransaction.addToBackStack("DOCUMENT_REQUEST_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }*/
            else if (item.getItemId() == R.id.miProfile) {
                Utils.hideKeyboard(this);
                if (USER == null){
                    Utils.loginRequiredDialog(MainActivity.this, navView, "To manage your profile, please log in or sign up first.");
                    return false;
                }
                if (Objects.requireNonNull(navView.getCheckedItem()).getItemId() != R.id.miProfile) {

                    Fragment profileFragment = new ProfileFragment();
                    fragmentTransaction.replace(R.id.fragmentHolder, profileFragment, "PROFILE_FRAGMENT");
                    fragmentTransaction.addToBackStack("PROFILE_FRAGMENT");
                    fragmentTransaction.commit();
                    drawerLayout.close();
                }
            }
            return true;
        });
    }

    private void backstackListener() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE_FRAGMENT");
            DocumentRequestFragment documentRequestFragment = (DocumentRequestFragment) getSupportFragmentManager().findFragmentByTag("DOCUMENT_REQUEST_FRAGMENT");
            AnnouncementsFragment announcementsFragment = (AnnouncementsFragment) getSupportFragmentManager().findFragmentByTag("ANNOUNCEMENTS_FRAGMENT");
            BlotterFragment blotterFragment = (BlotterFragment) getSupportFragmentManager().findFragmentByTag("BLOTTER_FRAGMENT");
            /*CrimeReportFragment crimeReportFragment = (CrimeReportFragment) getSupportFragmentManager().findFragmentByTag("CRIME_REPORT_FRAGMENT");*/
            IncidentReportFragment incidentReportFragment = (IncidentReportFragment) getSupportFragmentManager().findFragmentByTag("INCIDENT_REPORT_FRAGMENT");

            if (profileFragment != null && profileFragment.isVisible()) {
                tvActivityTitle.setText("My Profile");
                // bottom_navbar.getMenu().getItem(4).setChecked(true);
                softKeyboardListener();
            }
            else if (documentRequestFragment != null && documentRequestFragment.isVisible()) {
                tvActivityTitle.setText("Document Requests");
                // bottom_navbar.getMenu().getItem(3).setChecked(true);
                softKeyboardListener();
            }
            else if (announcementsFragment != null && announcementsFragment.isVisible()) {
                tvActivityTitle.setText("Barangay Announcements");
                // bottom_navbar.getMenu().getItem(2).setChecked(true);
                softKeyboardListener();
            }
            /*else if (crimeReportFragment != null && crimeReportFragment.isVisible()) {
                tvActivityTitle.setText("Report a Crime");
                // bottom_navbar.getMenu().getItem(1).setChecked(true);
                softKeyboardListener();
            }*/
            else if (incidentReportFragment != null && incidentReportFragment.isVisible()) {
                tvActivityTitle.setText("Barangay Blotter");
                // bottom_navbar.getMenu().getItem(0).setChecked(true);
                softKeyboardListener();
            }
            else if (blotterFragment != null && blotterFragment.isVisible()) {
                tvActivityTitle.setText("Crimes/Incidents");
                // bottom_navbar.getMenu().getItem(0).setChecked(true);
                softKeyboardListener();
            }
        });
    }

    private void softKeyboardListener() {
        /*getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets, view);
            if (insetsCompat.isVisible(WindowInsetsCompat.Type.ime())) {
                bottom_navbar.setVisibility(View.GONE);
            }
            else {
                bottom_navbar.setVisibility(View.VISIBLE);
            }
            return windowInsets;
        });*/
    }

    private void buildNotification() {
        String channelID = "Document Request Notifications";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        builder.setContentTitle("Ariba Cabangahan")
                .setSmallIcon(R.mipmap.ic_aruba_cabangahan)
                .setContentText("The document you have requested has been processed and is now ready for pickup")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);

            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "Document requests", importance);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, builder.build());
    }
}