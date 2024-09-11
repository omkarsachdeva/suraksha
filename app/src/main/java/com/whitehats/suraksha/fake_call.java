package com.whitehats.suraksha;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class fake_call extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        // Button 1 for Fake Call 1
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(v -> playAudio(R.raw.fake1));

        // Button 2 for Fake Call 2
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> playAudio(R.raw.fake1));

        // Button 3 for Fake Call 3
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(v -> playAudio(R.raw.fake1));

        // Button 4 for Fake Call 4
        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(v -> playAudio(R.raw.fake1));
    }

    // Function to play the audio file
    private void playAudio(int audioResource) {
        // Stop any existing playback before starting a new one
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();  // Reset the mediaPlayer before releasing it
            mediaPlayer.release();
            mediaPlayer = null;  // Ensure to set it to null after releasing
        }

        // Initialize MediaPlayer with the selected audio resource
        mediaPlayer = MediaPlayer.create(this, audioResource);
        if (mediaPlayer != null) {  // Check if MediaPlayer is properly initialized
            mediaPlayer.start(); // Start playing the audio

            // Release MediaPlayer once the audio completes
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.reset();  // Reset before release to clean up DRM and other internal states
                mediaPlayer.release();
                mediaPlayer = null;  // Ensure to set it to null after releasing
            });
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources if the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
}
}
}
