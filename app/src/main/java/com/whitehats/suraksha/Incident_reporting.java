package com.whitehats.suraksha;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class Incident_reporting extends Activity {

    private EditText incidentVictim , incidentDefender, incidentPhone , incidentDescription, incidentDateTime, incidentLocation;
    private Button uploadButton, submitButton;
    private Uri evidenceUri;
    private static final int PICK_IMAGE_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_reporting);

        incidentVictim = findViewById(R.id.incident_nameofvic);
        incidentDefender = findViewById(R.id.incident_nameofdef);
        incidentPhone = findViewById(R.id.incident_nofvic);
        incidentDescription = findViewById(R.id.incident_description);
        incidentDateTime = findViewById(R.id.incident_date_time);
        incidentLocation = findViewById(R.id.incident_location);
        uploadButton = findViewById(R.id.upload_button);
        submitButton = findViewById(R.id.submit_button);

        // Open date and time picker
        incidentDateTime.setOnClickListener(v -> showDateTimePicker());

        // Handle Upload Button
        uploadButton.setOnClickListener(v -> openGallery());

        // Handle Submit Button
        submitButton.setOnClickListener(v -> submitReport());
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                String dateTime = dayOfMonth + "/" + (month + 1) + "/" + year + " " + hourOfDay + ":" + minute;
                                incidentDateTime.setText(dateTime);
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Open Gallery for Evidence Upload
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/* video/*");
        startActivityForResult(intent, PICK_IMAGE_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_VIDEO && resultCode == RESULT_OK && data != null) {
            evidenceUri = data.getData();
            Toast.makeText(this, "Evidence uploaded successfully", Toast.LENGTH_SHORT).show();
        }
    }

    // Submit Report
    private void submitReport() {
        String victim = incidentVictim.getText().toString().trim();
        String defender = incidentDefender.getText().toString().trim();
        String phone = incidentPhone.getText().toString().trim();
        String description = incidentDescription.getText().toString().trim();
        String dateTime = incidentDateTime.getText().toString().trim();
        String location = incidentLocation.getText().toString().trim();


        if (victim.isEmpty() || defender.isEmpty() || phone.isEmpty() || description.isEmpty() || dateTime.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            // Process incident report submission (e.g., send data to the server)
            Toast.makeText(this, "Incident report submitted successfully", Toast.LENGTH_LONG).show();
}
}
}
