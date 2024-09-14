package com.whitehats.suraksha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class ProfilePage extends AppCompatActivity {
    private Button logoutButton;
    private EditText inputOtp1, inputOtp2, inputOtp3, inputOtp4, inputOtp5, inputOtp6;
    private Button regenerateCodeButton;
    private DatabaseReference databaseReference;
    private String generatedCode;
    private String userId;
    private  SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        inputOtp1 = findViewById(R.id.inputotp1);
        inputOtp2 = findViewById(R.id.inputotp2);
        inputOtp3 = findViewById(R.id.inputotp3);
        inputOtp4 = findViewById(R.id.inputotp4);
        inputOtp5 = findViewById(R.id.inputotp5);
        inputOtp6 = findViewById(R.id.inputotp6);
        regenerateCodeButton = findViewById(R.id.buttongetotp);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        String savedCode = sharedPreferences.getString("generated_code", null);
        if (savedCode != null) {
            generatedCode = savedCode;
            displayCode(generatedCode);
        } else {
            generateAndDisplayCode();
        }


        // Regenerate the code on button click
        regenerateCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateAndDisplayCode();
            }
        });

        logoutButton = findViewById(R.id.buttonlogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void generateAndDisplayCode() {
        String oldCode = sharedPreferences.getString("generated_code", null);
        if (oldCode != null) {
            databaseReference.child("codes_to_users").child(oldCode).removeValue();
        }

        generatedCode = generateRandomCode(6);
        displayCode(generatedCode);
        saveCodeToFirebaseAndSharedPreferences(generatedCode);
    }
    private void displayCode(String code) {
        if (code.length() == 6) {
            inputOtp1.setText(String.valueOf(code.charAt(0)));
            inputOtp2.setText(String.valueOf(code.charAt(1)));
            inputOtp3.setText(String.valueOf(code.charAt(2)));
            inputOtp4.setText(String.valueOf(code.charAt(3)));
            inputOtp5.setText(String.valueOf(code.charAt(4)));
            inputOtp6.setText(String.valueOf(code.charAt(5)));
        }
    }
    private void saveCodeToFirebaseAndSharedPreferences(String code) {
        // Save the generated code to Firebase
        databaseReference.child("codes_to_users").child(code).setValue(userId);
        databaseReference.child("users").child(userId).child("unique_code").setValue(code).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfilePage.this, "Code saved successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("generated_code", code);
                editor.apply();
            } else {
                Toast.makeText(ProfilePage.this, "Failed to save code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateRandomCode(int length) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, length);
    }

    private void logout() {
        // Clear the session and log the user out
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.remove("generated_code");
        editor.apply();

        FirebaseAuth.getInstance().signOut();

        // Go back to login screen
        startActivity(new Intent(ProfilePage.this, LoginActivity.class));
        finish();
    }
}
