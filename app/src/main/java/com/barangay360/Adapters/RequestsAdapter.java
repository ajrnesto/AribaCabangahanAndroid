package com.barangay360.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Objects.Request;
import com.barangay360.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.requestsViewHolder> {

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    Context context;
    ArrayList<Request> arrRequests;
    private OnRequestsListener mOnRequestsListener;

    public RequestsAdapter(Context context, ArrayList<Request> arrRequests, OnRequestsListener onRequestsListener) {
        this.context = context;
        this.arrRequests = arrRequests;
        this.mOnRequestsListener = onRequestsListener;
    }

    @NonNull
    @Override
    public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_request, parent, false);
        return new requestsViewHolder(view, mOnRequestsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull requestsViewHolder holder, int position) {
        Request requests = arrRequests.get(position);
        String uid = requests.getUid();
        String userUid = requests.getUserUid();
        String firstName = requests.getFirstName();
        String middleName = requests.getMiddleName();
        String lastName = requests.getLastName();
        long birthdate = requests.getBirthdate();
        String civilStatus = requests.getCivilStatus();
        String addressPurok = requests.getAddressPurok();
        String residencyStatus = requests.getResidencyStatus();
        String businessName = requests.getBusinessName();
        long timestamp = requests.getTimestamp();
        String status = requests.getStatus();
        String documentType = requests.getDocumentType();

        loadDocType(holder, documentType);
        loadTimestamp(holder, timestamp);
        loadStatus(holder, status);
    }

    private void loadDocType(requestsViewHolder holder, String documentType) {
        if (documentType.equals("CLEARANCE")) {
            holder.tvDocumentName.setText("BARANGAY CLEARANCE");
        }
        else if (documentType.equals("RESIDENCY")) {
            holder.tvDocumentName.setText("RESIDENCY CERTIFICATE");
        }
        else if (documentType.equals("INDIGENCY")) {
            holder.tvDocumentName.setText("INDIGENCY CERTIFICATE");
        }
        else if (documentType.equals("BUSINESS_PERMIT")) {
            holder.tvDocumentName.setText("BUSINESS PERMIT");
        }
    }

    private void loadTimestamp(requestsViewHolder holder, long timestamp) {
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("MMMM dd, yyyy, hh:mm aa");
        holder.tvTimestamp.setText(sdfTimestamp.format(timestamp));
    }

    private void loadStatus(requestsViewHolder holder, String status) {
        if (Objects.equals(status, "PENDING")) {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(context.getColor(R.color.majestic_blue));
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
        else if (Objects.equals(status, "COMPLETED")) {
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setTextColor(context.getColor(R.color.green_padua));
            holder.btnCancel.setVisibility(View.GONE);
        }
        else if (Objects.equals(status, "DECLINED")) {
            holder.tvStatus.setText("Declined");
            holder.tvStatus.setTextColor(context.getColor(R.color.terracotta));
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arrRequests.size();
    }

    public class requestsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnRequestsListener onRequestsListener;
        TextView tvTimestamp, tvDocumentName, tvStatus;
        MaterialButton btnCancel;

        public requestsViewHolder(@NonNull View itemView, OnRequestsListener onRequestsListener) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            this.onRequestsListener = onRequestsListener;
            // itemView.setOnClickListener(this);

            btnCancel.setOnClickListener(view -> {
                MaterialAlertDialogBuilder dialogCancel = new MaterialAlertDialogBuilder(itemView.getContext());
                dialogCancel.setTitle("Cancel request");
                dialogCancel.setMessage("Are you sure you want to cancel your document request?");
                dialogCancel.setPositiveButton("Cancel Request", (dialogInterface, i) -> {
                    int position = getAdapterPosition();
                    Request currentRequest = arrRequests.get(position);
                    String requestUid = currentRequest.getUid();

                    DB.collection("requests").document(requestUid).delete();
                    notifyDataSetChanged();
                });
                dialogCancel.setNeutralButton("Do not cancel", (dialogInterface, i) -> {

                });
                dialogCancel.show();
            });
        }

        @Override
        public void onClick(View view) {
            onRequestsListener.onRequestsClick(getAdapterPosition());
        }
    }

    public interface OnRequestsListener{
        void onRequestsClick(int position);
    }
}
