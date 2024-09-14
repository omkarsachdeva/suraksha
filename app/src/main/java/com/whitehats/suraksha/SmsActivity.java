package com.whitehats.suraksha;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SmsActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference databaseReference;
    private List<String> phoneNumbers;
    private EditText messageInput;
    private Button tryItButton, saveMessageButton;
    private SharedPreferences sharedPreferences;

    private static final String DEFAULT_MESSAGE = "I am in danger, please come fast...";
    private static final String PREF_KEY_CUSTOM_MESSAGE = "custom_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("sms_preferences", MODE_PRIVATE);

        // Retrieve the message input EditText from XML
        messageInput = findViewById(R.id.txt_sms);

        // Set the saved custom message (if any) in the EditText when the activity starts
        String savedMessage = sharedPreferences.getString(PREF_KEY_CUSTOM_MESSAGE, "");
        messageInput.setText(savedMessage);

        // Retrieve emergency contacts from Firebase
        phoneNumbers = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("emergency_contacts");

        // Reference the Try It button and Save button
        tryItButton = findViewById(R.id.button);
        saveMessageButton = findViewById(R.id.Save_btn);

        // Set click listener for the Save Message button
        saveMessageButton.setOnClickListener(v -> {
            saveCustomMessage();
        });

        // Set click listener for the Try It button
        tryItButton.setOnClickListener(v -> {
            // Load contacts from Firebase and then send messages/call
            loadEmergencyContacts();
        });
    }

    private void saveCustomMessage() {
        String customMessage = messageInput.getText().toString().trim();

        if (!customMessage.isEmpty()) {
            // Save the custom message to SharedPreferences
            sharedPreferences.edit().putString(PREF_KEY_CUSTOM_MESSAGE, customMessage).apply();
            Toast.makeText(SmsActivity.this, "Message saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            // If the message is empty, clear the custom message in SharedPreferences
            sharedPreferences.edit().putString(PREF_KEY_CUSTOM_MESSAGE, "").apply();
            Toast.makeText(SmsActivity.this, "Message cleared, default will be used", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEmergencyContacts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                phoneNumbers.clear(); // Clear the list before adding new contacts

                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        phoneNumbers.add(contact.getPhoneNumber());
                    }
                }

                // After contacts are loaded, try sending the SMS and making calls
                tryIt();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SmsActivity.this, "Failed to load emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tryIt() {
        // Ensure we have permission to send SMS and make calls
        if (ContextCompat.checkSelfPermission(SmsActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(SmsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                sendLocationMessage();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
        }
    }

    // Send SMS with location details to all emergency contacts
    private void sendLocationMessage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                // Initialize location
                Location location = task.getResult();
                String message;

                // Retrieve custom message from SharedPreferences or use the default message
                String customMessage = sharedPreferences.getString(PREF_KEY_CUSTOM_MESSAGE, "").trim();
                if (!customMessage.isEmpty()) {
                    message = customMessage;
                } else {
                    message = DEFAULT_MESSAGE;
                }

                if (location != null) {
                    try {
                        // Get location details
                        Geocoder geocoder = new Geocoder(SmsActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        message += " I am at " + addresses.get(0).getLatitude() +
                                ", " + addresses.get(0).getLongitude() + ", " + addresses.get(0).getCountryName() +
                                ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    message += " Software was not able to retrieve live location due to some internal errors.";
                }

                // Send SMS to all emergency contacts
                SmsManager smsManager = SmsManager.getDefault();
                for (String phoneNumber : phoneNumbers) {
                    if (!phoneNumber.isEmpty()) {
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                        Toast.makeText(SmsActivity.this, "Message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
                        storeLocationInFirebase(location.getLatitude(),location.getLongitude());
                    }
                }

                // Optionally make a call after sending the SMS
                makeCall();
            }
        });
    }

    // Call the first number in the list
    private void makeCall() {
        if (phoneNumbers.size() > 0 && !phoneNumbers.get(0).isEmpty()) {
            String phoneNumber = phoneNumbers.get(0);
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            if (ContextCompat.checkSelfPermission(SmsActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            }
        } else {
            Toast.makeText(this, "No phone number available to call", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeLocationInFirebase(double latitude , double longitude){
        String alertId = databaseReference.push().getKey();
        if(alertId!=null){
            SOSData sosData = new SOSData(latitude,longitude);
            databaseReference.child(alertId).setValue(sosData);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle SMS and CALL permissions
        if (requestCode == 0) {  // For SMS permissions
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tryIt();
            } else {
                Toast.makeText(this, "Permission denied for sending SMS", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) {  // For CALL permissions
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            } else {
                Toast.makeText(this, "Permission denied for making a call", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 44) {  // For Location permissions
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendLocationMessage();
            } else {
                Toast.makeText(this, "Permission denied for accessing location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
