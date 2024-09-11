package com.whitehats.suraksha;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText etContactName, etPhoneNumber;
    private Button btnAddNewContact;
    private RecyclerView recyclerViewContacts;
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactsList = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        etContactName = findViewById(R.id.et_contact_name);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnAddNewContact = findViewById(R.id.btn_add_new_contact);
        //btnSubmitContacts = findViewById(R.id.btn_submit_contacts);
        recyclerViewContacts = findViewById(R.id.recycler_view_contacts);

        databaseReference = FirebaseDatabase.getInstance().getReference("emergency_contacts");

        // Setup RecyclerView
        contactsAdapter = new ContactsAdapter(contactsList, databaseReference);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContacts.setAdapter(contactsAdapter);

        // Load contacts from Firebase
        loadContactsFromFirebase();

        // Add a new contact
        btnAddNewContact.setOnClickListener(v -> {
            String name = etContactName.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(EmergencyContactsActivity.this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Contact object
            String id = databaseReference.push().getKey();
            Contact contact = new Contact(id, name, phone);

            // Add contact to Firebase
            databaseReference.child(id).setValue(contact).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EmergencyContactsActivity.this, "Contact added", Toast.LENGTH_SHORT).show();
                    contactsList.add(contact);
                    contactsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EmergencyContactsActivity.this, "Failed to add contact", Toast.LENGTH_SHORT).show();
                }
            });

            // Clear the input fields
            etContactName.setText("");
            etPhoneNumber.setText("");
        });
    }

    private void loadContactsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactsList.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    contactsList.add(contact);
                }
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencyContactsActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
