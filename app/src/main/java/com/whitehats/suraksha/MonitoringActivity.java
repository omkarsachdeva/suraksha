package com.whitehats.suraksha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MonitoringActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        findViewById(R.id.front).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start streaming with the front camera
                Intent intent = new Intent(MonitoringActivity.this, MonitoringActivity.class);
                intent.putExtra("camera", "front");
                startActivity(intent);
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start streaming with the front camera
                Intent intent = new Intent(MonitoringActivity.this, MonitoringActivity.class);
                intent.putExtra("camera", "back");
                startActivity(intent);
            }
        });
    }
}