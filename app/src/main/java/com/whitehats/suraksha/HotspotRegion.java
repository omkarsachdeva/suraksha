package com.whitehats.suraksha;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class HotspotRegion extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker userLocationMarker;
    private DatabaseReference databaseReference;
    private HeatmapTileProvider heatmapProvider;
    private TileOverlay heatmapOverlay;
    private List<LatLng> redZones = new ArrayList<>();

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String CHANNEL_ID = "red_zone_channel";
    private static final long NOTIFICATION_INTERVAL_MS = 15 * 60 * 1000; // 15 minutes

    private boolean isNotificationSent = false;
    private long lastNotificationTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot_region);

        mapView = findViewById(R.id.mapView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("sos_alerts");

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        setupLocationRequest();
        setupLocationCallback();

        createNotificationChannel(); // Create notification channel
    }

    private void setupLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (googleMap != null && location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        if (userLocationMarker != null) {
                            userLocationMarker.setPosition(userLatLng);
                        } else {
                            userLocationMarker = googleMap.addMarker(new MarkerOptions()
                                    .position(userLatLng)
                                    .title("Your Location"));
                        }

                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));

                        checkProximityToRedZones(userLatLng);
                    }
                }
            }
        };
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Check for location permission and start location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true); // Enable My Location layer
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Load SOS alert data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LatLng> latLngs = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    LatLng latLng = new LatLng(latitude, longitude);
                    latLngs.add(latLng);
                }

                // Define the gradient of the heatmap
                Gradient gradient = new Gradient(
                        new int[]{Color.GREEN, Color.YELLOW, Color.RED},
                        new float[]{0.2f, 0.5f, 1f}
                );

                // Create the heatmap tile provider
                heatmapProvider = new HeatmapTileProvider.Builder()
                        .data(latLngs) // Use data method with LatLng
                        .gradient(gradient)
                        .build();

                // Add the heatmap tile overlay to the map
                if (heatmapOverlay != null) {
                    heatmapOverlay.remove();
                }
                heatmapOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapProvider));

                // Identify red zones based on the heatmap data
                identifyRedZones(latLngs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void identifyRedZones(List<LatLng> latLngs) {
        redZones.clear();
        // Iterate through the latLngs to find high-density areas
        for (LatLng latLng : latLngs) {
            // You can add logic here to determine red zones if needed
            redZones.add(latLng); // Add all LatLngs to redZones list for demonstration
        }
    }

    private void checkProximityToRedZones(LatLng userLatLng) {
        boolean nearRedZone = false;

        // Iterate through red zones to check proximity
        for (LatLng redZone : redZones) {
            float[] distance = new float[1];
            Location.distanceBetween(userLatLng.latitude, userLatLng.longitude, redZone.latitude, redZone.longitude, distance);

            if (distance[0] < 1000) { // Within 1km
                nearRedZone = true;
                break;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (nearRedZone) {
            if (!isNotificationSent || (currentTime - lastNotificationTime) >= NOTIFICATION_INTERVAL_MS) {
                showNotification();
                isNotificationSent = true;
                lastNotificationTime = currentTime;
            }
        } else {
            // User is not in a red zone
            if (isNotificationSent) {
                // Reset notification status when exiting red zone
                isNotificationSent = false;
            }
        }
    }

    private void showNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, HotspotRegion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);

        // Use FLAG_IMMUTABLE for an immutable PendingIntent
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert) // Replace with your notification icon
                .setContentTitle("Red Zone Alert")
                .setContentText("You are entering a red zone!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request notification permission if needed
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Red Zone Notifications";
            String description = "Channel for red zone notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
}
}
}
