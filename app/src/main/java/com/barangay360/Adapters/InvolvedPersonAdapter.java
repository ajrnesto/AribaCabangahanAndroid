package com.barangay360.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barangay360.Objects.InvolvedPerson;
import com.barangay360.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class InvolvedPersonAdapter extends RecyclerView.Adapter<InvolvedPersonAdapter.involvedPersonViewHolder> {

    Context context;
    ArrayList<InvolvedPerson> arrInvolvedPerson;
    private OnInvolvedPersonListener mOnInvolvedPersonListener;

    // Spinner items
    String[] itemsInvolvement;
    ArrayAdapter<String> adapterInvolvement;

    public InvolvedPersonAdapter(Context context, ArrayList<InvolvedPerson> arrInvolvedPerson, OnInvolvedPersonListener mOnInvolvedPersonListener) {
        this.context = context;
        this.arrInvolvedPerson = arrInvolvedPerson;
        this.mOnInvolvedPersonListener = mOnInvolvedPersonListener;
    }

    @NonNull
    @Override
    public involvedPersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_involved_person, parent, false);
        return new involvedPersonViewHolder(view, mOnInvolvedPersonListener);
    }

    @Override
    public void onBindViewHolder(@NonNull involvedPersonViewHolder holder, int position) {
        initializeSpinners(holder);
        InvolvedPerson involvedPerson = arrInvolvedPerson.get(position);

        holder.etFullName.setText(involvedPerson.getFullName());
        holder.etFullAddress.setText(involvedPerson.getFullAddress());
        holder.menuInvolvement.setText(involvedPerson.getInvolvement(), false);
    }

    private void initializeSpinners(involvedPersonViewHolder holder) {
        itemsInvolvement = new String[]{"COMPLAINANT", "RESPONDENT", "WITNESS"};
        adapterInvolvement = new ArrayAdapter<>(context, R.layout.list_item, itemsInvolvement);
        holder.menuInvolvement.setAdapter(adapterInvolvement);
    }

    @Override
    public int getItemCount() {
        return arrInvolvedPerson.size();
    }

    public class involvedPersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        OnInvolvedPersonListener onInvolvedPersonListener;
        TextInputEditText etFullName, etFullAddress;
        AutoCompleteTextView menuInvolvement;
        MaterialButton btnRemove;

        public involvedPersonViewHolder(@NonNull View itemView, OnInvolvedPersonListener onInvolvedPersonListener) {
            super(itemView);

            etFullName = itemView.findViewById(R.id.etFullName);
            etFullAddress = itemView.findViewById(R.id.etFullAddress);
            menuInvolvement = itemView.findViewById(R.id.menuInvolvement);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            this.onInvolvedPersonListener = onInvolvedPersonListener;
            btnRemove.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onInvolvedPersonListener.onInvolvedPersonListener(getAdapterPosition());
        }
    }

    public interface OnInvolvedPersonListener{
        void onInvolvedPersonListener(int position);
    }
}
