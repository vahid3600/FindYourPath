package com.azmoonsara.app.findyourpath;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    Button search_button;
    EditText location;
    Marker firstMarker;
    Marker secondMarker;
    Marker userMarker;
    boolean firstLocation, path = false;
    HashMap<String, Object> userPath = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(this,"please select your start location", Toast.LENGTH_LONG).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        search_button = (Button) findViewById(R.id.search);
        search_button.setOnClickListener(this::searchLocation);

    }

    private void searchLocation(View v) {
        hideSoftKeyboard(v);

        location = findViewById(R.id.place_name);
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        try {
            List<Address> list = geocoder.getFromLocationName(location.getText().toString(), 10);
            Log.e("Tag ", "number of find location: " + list.size());
            for (int i = 0; i < list.size(); i++) {
                Address address = list.get(i);
                Log.e("Tag ", "result" + i + ": " + address.getLocality());
            }
            if (list.size() != 0) {
                Address address = list.get(0);
                goToLocation(address.getLatitude(), address.getLongitude());
            } else
                Toast.makeText(MapsActivity.this, "Location Not Found", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        float zoom = 5;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(cameraUpdate);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void setFirstMarker(String locality,String subLocality, double lat, double lng) {
        String title;
        if (locality != null)
            title = locality;
        else if (subLocality !=null)
            title = subLocality;
        else
            title = "Your Location";

        MarkerOptions markerOptions = new MarkerOptions()
                .title(title)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        firstMarker = mMap.addMarker(markerOptions);
    }

    private void setSecondMarker(String locality,String subLocality, double lat, double lng) {
        String title;
        if (locality != null)
            title = locality;
        else if (subLocality !=null)
            title = subLocality;
        else
            title = "Your Destination";
        if (secondMarker != null) {
            secondMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .title(title)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        secondMarker = mMap.addMarker(markerOptions);
    }

    private void setUserMarker(double lat, double lng) {
        if (userMarker != null) {
            userMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .title("Your Destination")
                .position(new LatLng(lat, lng))
                .icon(bitmapDescriptorFromVector(this, R.drawable.person_icon));
        userMarker = mMap.addMarker(markerOptions);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!firstLocation) {
                    firstLocation = true;

                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    List<Address> userLocation = null;

                    try {
                        userLocation = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        firstLocation = false;
                        return;
                    }
                    Address address = userLocation.get(0);
                    MapsActivity.this.setFirstMarker(address.getLocality(),address.getSubLocality(), latLng.latitude, latLng.longitude);
                    userPath.put("first", latLng);
                    Toast.makeText(MapsActivity.this,"please select your destination", Toast.LENGTH_LONG).show();
                } else if (!path) {
                    path = true;
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    List<Address> userLocationPath = null;

                    try {
                        userLocationPath = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        path = false;
                        return;
                    }
                    Address address = userLocationPath.get(0);
                    MapsActivity.this.setSecondMarker(address.getLocality(),address.getSubLocality(), latLng.latitude, latLng.longitude);
                    userPath.put("second", latLng);
                    getRouteToMarker();
                    getUserLocation();
                }
            }
        });

    }

    void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
    }

    void getRouteToMarker(){
        LatLng latLng = (LatLng)userPath.get("second");
        Object dataTransfer[] = new Object[2];
        dataTransfer = new Object[3];
        String url = getDirectionsUrl();
        GetDirectionsData.GetDirections getDirectionsData = new GetDirectionsData.GetDirections();
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = latLng;
        getDirectionsData.execute(dataTransfer);
    }

    private String getDirectionsUrl()
    {
        LatLng startLocation = (LatLng)userPath.get("first");
        LatLng endLocation = (LatLng)userPath.get("second");
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+startLocation.latitude+","+startLocation.longitude);
        googleDirectionsUrl.append("&destination="+endLocation.latitude+","+endLocation.longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyCAcfy-02UHSu2F6WeQ1rhQhkCr51eBL9g");

        return googleDirectionsUrl.toString();
    }

    GetDirectionsData getData = new GetDirectionsData(() -> {
        Toast.makeText(MapsActivity.this, "your location is wrong, please select carefuly again.", Toast.LENGTH_LONG).show();
        firstMarker.remove();
        secondMarker.remove();
        firstLocation = false;
        path = false;
        userPath.clear();
    });

    @Override
    public void onLocationChanged(Location location) {
        setUserMarker(location.getLatitude(), location.getLongitude());
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
