package com.whitehats.suraksha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class ScreenOnOffReceiver extends BroadcastReceiver {
    public static final String SCREEN_TOGGLE_TAG = "ScreenOnOffReceiver";
    private static final int TAP_THRESHOLD = 10; // Time limit in seconds to reset tap count
    private int powerBtnTapCount = 0;
    private MediaPlayer mediaPlayer;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<String> emergencyContactNumbers = new ArrayList<>();

    private CountDownTimer countDownTimer = new CountDownTimer(TAP_THRESHOLD * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            // Do nothing, just wait for taps
        }

        @Override
        public void onFinish() {
            powerBtnTapCount = 0; // Reset the tap count if the threshold is reached
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action) || Intent.ACTION_SCREEN_ON.equals(action)) {
            powerBtnTapCount++;
            countDownTimer.start(); // Start/continue the timer on every tap

            if (powerBtnTapCount == 3) {
                // First, fetch emergency contacts from Firebase, then send SMS and make a call
                fetchEmergencyContactsAndSend(context);
                powerBtnTapCount = 0; // Reset tap count after action
                countDownTimer.cancel(); // Cancel timer
            } else if (powerBtnTapCount == 6) {
                startSiren(context); // Start the siren on 6 taps
                powerBtnTapCount = 0; // Reset tap count after action
                countDownTimer.cancel(); // Cancel timer
            } else if (powerBtnTapCount == 3 && mediaPlayer != null && mediaPlayer.isPlaying()) {
                stopSiren(context); // Stop the siren on 3 taps
                powerBtnTapCount = 0; // Reset tap count after action
                countDownTimer.cancel(); // Cancel timer
            }
        }
    }

    // Fetch emergency contacts from Firebase
    private void fetchEmergencyContactsAndSend(Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("emergency_contacts");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emergencyContactNumbers.clear(); // Clear the list before adding new contacts
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null && contact.getPhoneNumber() != null) {
                        emergencyContactNumbers.add(contact.getPhoneNumber());
                    }
                }

                // Now that contacts are loaded, send SMS and make a call
                sendSmsAndMakeCall(context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSmsAndMakeCall(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Handle lack of permission for location access
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                String message = "I am in danger, please come fast...";

                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        message += " I am at " + addresses.get(0).getLatitude() + ", " +
                                addresses.get(0).getLongitude() + ", " + addresses.get(0).getCountryName() +
                                ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Send SMS to all emergency contacts
                SmsManager smsManager = SmsManager.getDefault();

                for (String phoneNumber : emergencyContactNumbers) {
                    if (!phoneNumber.isEmpty()) {
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                        Toast.makeText(context, "Message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
                    }
                }

                // Check if CALL_PHONE permission is granted
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    // Make the call if permission is granted
                    if (!emergencyContactNumbers.isEmpty()) {
                        makePhoneCall(context, emergencyContactNumbers.get(0)); // Call the first contact
                    }
                } else {
                    // Handle missing permission by showing a message
                    Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void makePhoneCall(Context context, String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        try {
            // Check for the CALL_PHONE permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);
            } else {
                Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to make call due to missing permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSiren(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.police_siren); // Replace with actual siren sound
        }
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        Toast.makeText(context, "Siren started", Toast.LENGTH_SHORT).show();
    }

    private void stopSiren(Context context) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(context, "Siren stopped", Toast.LENGTH_SHORT).show();
        }
    }
}
