package com.whitehats.suraksha;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_splash);
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        lottieAnimationView = findViewById (R.id.lottie_layer_name);


        lottieAnimationView.addAnimatorListener (new AnimatorListenerAdapter () {
            @Override
            public void onAnimationEnd(Animator animation) {
                String loginType = sharedPreferences.getString("loginType", "");

                if (loginType.equals("user")) {
                    // If login type is "user", navigate to MainActivity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (loginType.equals("parent")) {
                    // If login type is "parent", navigate to MonitoringActivity
                    Intent intent = new Intent(SplashActivity.this, MonitoringActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If no login type is found, show the login selection screen
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}