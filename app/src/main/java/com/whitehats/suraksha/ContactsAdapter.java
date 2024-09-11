package com.whitehats.suraksha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> contactsList;
    private DatabaseReference databaseReference;

    public ContactsAdapter(List<Contact> contactsList, DatabaseReference databaseReference) {
        this.contactsList = contactsList;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactsList.get(position);
        holder.tvContactName.setText(contact.getName());
        holder.tvContactPhone.setText(contact.getPhoneNumber());

        holder.btnDeleteContact.setOnClickListener(v -> {
            // Remove from Firebase
            databaseReference.child(contact.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    contactsList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, contactsList.size());
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvContactPhone;
        Button btnDeleteContact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);
            tvContactPhone = itemView.findViewById(R.id.tv_contact_phone);
            btnDeleteContact = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}
