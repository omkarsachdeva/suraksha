package com.whitehats.suraksha;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText phoneNumberEditText;
    private Button addContactButton, submitButton;
    private RecyclerView contactsRecyclerView;
    private ContactsAdapter contactsAdapter;
    private List<String> contactsList = new ArrayList<>();
    private DatabaseReference database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        phoneNumberEditText = findViewById(R.id.et_phone_number);
        addContactButton = findViewById(R.id.btn_add_contact);
        submitButton = findViewById(R.id.btn_submit_contacts);
        contactsRecyclerView = findViewById(R.id.recycler_view_contacts);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Setup RecyclerView
        contactsAdapter = new ContactsAdapter(contactsList);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsAdapter);

        // Add contact to the list
        addContactButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(EmergencyContactsActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add phone number to the list and update RecyclerView
            contactsList.add(phoneNumber);
            contactsAdapter.notifyDataSetChanged();
            phoneNumberEditText.setText(""); // Clear input field
        });

        // Submit contacts to Firebase using the user's phone number as the key
        submitButton.setOnClickListener(v -> submitContacts());
    }

    private void submitContacts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userPhoneNumber = currentUser.getPhoneNumber(); // Use phone number as the key

            DatabaseReference contactsRef = database.child("users").child(userPhoneNumber).child("emergency_contacts");
            contactsRef.setValue(contactsList).addOnSuccessListener(aVoid ->
                            Toast.makeText(EmergencyContactsActivity.this, "Emergency contacts saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(EmergencyContactsActivity.this, "Failed to save contacts", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(EmergencyContactsActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
