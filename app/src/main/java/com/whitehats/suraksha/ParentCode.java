package com.whitehats.suraksha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentCode extends AppCompatActivity {
    private ImageButton logoutButton;
    private EditText inputOtp1, inputOtp2, inputOtp3, inputOtp4, inputOtp5, inputOtp6;
    private Button verifyButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_code);

        inputOtp1 = findViewById(R.id.inputotp1);
        inputOtp2 = findViewById(R.id.inputotp2);
        inputOtp3 = findViewById(R.id.inputotp3);
        inputOtp4 = findViewById(R.id.inputotp4);
        inputOtp5 = findViewById(R.id.inputotp5);
        inputOtp6 = findViewById(R.id.inputotp6);
        verifyButton = findViewById(R.id.submitcode);

        databaseReference = FirebaseDatabase.getInstance().getReference("codes_to_users");

        verifyButton.setOnClickListener(v -> verifyCode());

        logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear session and log out
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_logged_in", false);
                editor.apply();

                FirebaseAuth.getInstance().signOut();

                // Redirect to Login Activity
                startActivity(new Intent(ParentCode.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void verifyCode() {
        // Get the code entered by the parent
        String enteredCode = inputOtp1.getText().toString().trim() +
                inputOtp2.getText().toString().trim() +
                inputOtp3.getText().toString().trim() +
                inputOtp4.getText().toString().trim() +
                inputOtp5.getText().toString().trim() +
                inputOtp6.getText().toString().trim();

        if (TextUtils.isEmpty(enteredCode) || enteredCode.length() != 6) {
            Toast.makeText(this, "Please enter the full code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the userId associated with the entered emergency code
        databaseReference.child(enteredCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userId = snapshot.getValue(String.class);
                if (userId != null) {
                    // Code is correct, allow parent to log in
                    Toast.makeText(ParentCode.this, "Code verified!", Toast.LENGTH_SHORT).show();
                    // Redirect to parent main screen or monitoring activity
                    Intent intent = new Intent(ParentCode.this, MonitoringActivity.class);
                    intent.putExtra("USER_ID", userId);  // Pass the userId to MonitoringActivity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ParentCode.this, "Invalid code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentCode.this, "Failed to retrieve code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
