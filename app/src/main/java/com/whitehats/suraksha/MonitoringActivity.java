package com.whitehats.suraksha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MonitoringActivity extends AppCompatActivity {
    private ImageButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        logoutButton = findViewById(R.id.logout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the session and log the user out
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_logged_in", false);
                editor.apply();

                FirebaseAuth.getInstance().signOut();

                // Go back to login screen
                startActivity(new Intent(MonitoringActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Find the camera button and set up the click listener
        findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the CameraActivity when the camera button is clicked
                Intent intent = new Intent(MonitoringActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
    }
}
