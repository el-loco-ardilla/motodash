package ch.ellocoardilla.motodash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_FINE_LOCATION = 99;
    TextView tv_altitude, tv_speed_mean, tv_speed;
    ImageView iv_bearing;

    FusedLocationProviderClient fusedLocationProviderClient;

    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    private static volatile double[] speeds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_altitude = findViewById(R.id.tv_altitude);
        tv_speed = findViewById(R.id.tv_speed);
        iv_bearing = findViewById(R.id.iv_bearing);
        tv_speed_mean = findViewById(R.id.tv_speed_mean);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(5500);


        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };
        speeds = new double[]{0.0, 0.0, 0.0, 0.0};
        updateGPS();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "Require Permission: Error " + String.valueOf(grantResults[0]), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIValues(location);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
                  }
            });
        }
        else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    private void updateUIValues(Location location) {

        if (location.hasAltitude()) {
            tv_altitude.setText(String.format("%.1f m   ± %.0f m", location.getAltitude(), location.getVerticalAccuracyMeters()));
        }
        if (location.hasSpeed()) {
            tv_speed.setText(String.format("%.1f km/h", location.getSpeed() * 3.6));
        }
        if (location.hasBearing()) {
            iv_bearing.setRotation(360 - location.getBearing());
        }

        for (int i = 0; i < speeds.length - 1; i++) {
            speeds[i] = speeds[i + 1];
        }
        speeds[3] = location.getSpeed() * 3.6;

        double totalspeed = 0;
        for (int i = 0; i < speeds.length; i++) {
            totalspeed += speeds[i];
        }

        tv_speed_mean.setText(String.format("%.1f km/h",totalspeed/4));
    }
}