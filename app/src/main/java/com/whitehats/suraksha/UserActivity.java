package com.whitehats.suraksha;

import io.socket.client.IO;
import io.socket.client.Socket;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class UserActivity extends AppCompatActivity {

    private Socket socket;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Generate a unique room ID
        roomId = UUID.randomUUID().toString();

        // Initialize the socket connection
        try {
            socket = IO.socket("https://jj4t3363-3000.inc1.devtunnels.ms/"); // Replace with your server URL
            socket.connect();
            Log.d("SocketHandler", "Socket Connected");

            // Emit the generated room ID to the server
            socket.on(Socket.EVENT_CONNECT, args -> {
                socket.emit("create-room", roomId);
                Log.d("SocketHandler", "Room Created: " + roomId);
                runOnUiThread(() -> Toast.makeText(UserActivity.this, "Created Room ID: " + roomId, Toast.LENGTH_SHORT).show());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
        }
    }
}
