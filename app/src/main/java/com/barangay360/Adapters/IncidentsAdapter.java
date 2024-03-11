package com.barangay360.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Objects.Incident;
import com.barangay360.Objects.InvolvedPerson;
import com.barangay360.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class IncidentsAdapter extends RecyclerView.Adapter<IncidentsAdapter.incidentsViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    Context context;
    ArrayList<Incident> arrIncidents;
    private OnIncidentsListener mOnIncidentsListener;

    public IncidentsAdapter(Context context, ArrayList<Incident> arrIncidents, OnIncidentsListener onIncidentsListener) {
        this.context = context;
        this.arrIncidents = arrIncidents;
        this.mOnIncidentsListener = onIncidentsListener;
    }

    @NonNull
    @Override
    public incidentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_incident, parent, false);
        return new incidentsViewHolder(view, mOnIncidentsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull incidentsViewHolder holder, int position) {
        Incident incidents = arrIncidents.get(position);
        String uid = incidents.getUid();
        String userUid = incidents.getUserUid();
        String incidentType = incidents.getIncidentType();
        String locationPurok = incidents.getLocationPurok();
        ArrayList<InvolvedPerson> arrInvolvedPersons = incidents.getInvolvedPersons();
        String incidentDetails = incidents.getIncidentDetails();
        ArrayList<String> arrMediaFileNames = incidents.getMediaFileNames();
        long incidentDate = incidents.getIncidentDate();
        long timestamp = incidents.getTimestamp();
        String status = incidents.getStatus();

        holder.tvIncidentType.setText(incidentType);
        loadTimestamp(holder, timestamp);
        loadStatus(holder, status);
    }

    private void loadTimestamp(incidentsViewHolder holder, long timestamp) {
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("MMMM dd, yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdfTimestamp.format(timestamp));
    }

    private void loadStatus(incidentsViewHolder holder, String status) {
        if (Objects.equals(status, "PENDING")) {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(context.getColor(R.color.majestic_blue));
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
        else if (Objects.equals(status, "HEARING SCHEDULED")) {
            holder.tvStatus.setText("Hearing Scheduled");
            holder.tvStatus.setTextColor(context.getColor(R.color.majestic_blue));
            holder.btnCancel.setVisibility(View.GONE);
        }
        else if (Objects.equals(status, "UNDER INVESTIGATION")) {
            holder.tvStatus.setText("Under Investigation");
            holder.tvStatus.setTextColor(context.getColor(R.color.majestic_blue));
            holder.btnCancel.setVisibility(View.GONE);
        }
        else if (Objects.equals(status, "RESOLVED")) {
            holder.tvStatus.setText("Resolved");
            holder.tvStatus.setTextColor(context.getColor(R.color.green_padua));
            holder.btnCancel.setVisibility(View.GONE);
        }
        else if (Objects.equals(status, "CLOSED")) {
            holder.tvStatus.setText("Closed");
            holder.tvStatus.setTextColor(context.getColor(R.color.terracotta));
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arrIncidents.size();
    }

    public class incidentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnIncidentsListener onIncidentsListener;
        TextView tvTimestamp, tvIncidentType, tvStatus;
        MaterialButton btnCancel;

        public incidentsViewHolder(@NonNull View itemView, OnIncidentsListener onIncidentsListener) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvIncidentType = itemView.findViewById(R.id.tvIncidentType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            this.onIncidentsListener = onIncidentsListener;
            itemView.setOnClickListener(this);

            btnCancel.setOnClickListener(view -> {
                MaterialAlertDialogBuilder dialogCancel = new MaterialAlertDialogBuilder(itemView.getContext());
                dialogCancel.setTitle("Cancel incident report?");
                dialogCancel.setMessage("Are you sure you want to cancel your report?");
                dialogCancel.setPositiveButton("Cancel Incident Report", (dialogInterface, i) -> {
                    int position = getAdapterPosition();
                    Incident currentIncident = arrIncidents.get(position);
                    String incidentUid = currentIncident.getUid();

                    DB.collection("incidents").document(incidentUid).delete();
                    DB.collection("crimes").document(incidentUid).delete();
                    notifyDataSetChanged();
                });
                dialogCancel.setNeutralButton("Do not cancel", (dialogInterface, i) -> {

                });
                dialogCancel.show();
            });
        }

        @Override
        public void onClick(View view) {
            onIncidentsListener.onIncidentsClick(getAdapterPosition());
        }
    }

    public interface OnIncidentsListener{
        void onIncidentsClick(int position);
    }
}
