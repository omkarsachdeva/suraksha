package com.whitehats.suraksha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        findViewById(R.id.user).setOnClickListener(view -> {
            sharedPreferences.edit().putString("loginType", "user").apply();
            Intent intent = new Intent(LoginActivity.this, UserLoginActivity.class);
            startActivity(intent);
        });

        // Parent Login Button
        findViewById(R.id.parent).setOnClickListener(view -> {
            sharedPreferences.edit().putString("loginType", "parent").apply();
            Intent intent = new Intent(LoginActivity.this, ParentLoginActivity.class);
            startActivity(intent);
        });


    }
    private void saveLoginState(boolean isLoggedIn, String userType) {
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", isLoggedIn);  // Store login status
        editor.putString("user_type", userType);  // Store user type (e.g., "user" or "parent")
        editor.apply();
    }
}