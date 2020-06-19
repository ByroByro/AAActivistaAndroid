package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class GeoLocationActivity extends AppCompatActivity implements LocationListener {

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    final int LOCATION = 1000;
    TextView txtLat;
    String lat;
    String provider;
    protected String latitude,longitude;
    protected boolean gps_enabled,network_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_location);

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_DENIED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    //get user permission to access location
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            }
        }catch (Exception e){
            methods.showAlert("Error",e.toString(),this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try{
            methods.showAlert("Location","Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(),this);
        }catch (Exception e){
            methods.showAlert("Error",e.toString(),this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
