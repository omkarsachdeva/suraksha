package com.whitehats.suraksha;

import io.socket.client.IO;
import io.socket.client.Socket;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import org.webrtc.*;

public class CameraActivity extends AppCompatActivity {

    private Socket socket;
    private String roomId;
    private SurfaceViewRenderer remoteView;
    private EglBase eglBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        remoteView = findViewById(R.id.remote_view);

        // Initialize WebRTC components
        initializeRemoteVideoView();

        // Initialize the socket connection
        try {
            socket = IO.socket("https://jj4t3363-3000.inc1.devtunnels.ms/");  // Replace with your server URL
            socket.connect();

            // Listen for the room ID created by the user
            socket.on("room-created", args -> {
                roomId = (String) args[0];
                Log.d("SocketHandler", "Received Room ID: " + roomId);

                // Join the room
                socket.emit("join-room", roomId);

                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Joined Room ID: " + roomId, Toast.LENGTH_SHORT).show());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeRemoteVideoView() {
        eglBase = EglBase.create();
        remoteView.init(eglBase.getEglBaseContext(), null);
        remoteView.setZOrderMediaOverlay(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
        }
    }
}
