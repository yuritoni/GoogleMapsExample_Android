package com.example.googlemapsapp;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap;
    private EditText e1;
    private Button searchButton;
    GoogleApiClient client;
    LocationRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        e1 = (EditText) findViewById(R.id.editSearch);
        searchButton  = (Button) findViewById(R.id.buttonSearch);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(e1.getText()==null)
                {
                    Toast.makeText(getApplicationContext(),"isi pada pencarian",Toast.LENGTH_SHORT).show();
                }else{
                    findMap(v);
                }

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        getToLocation(-34, 151, 16);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void getToLocation(double latitude, double longtitude, int zoom) {
        LatLng latLng = new LatLng(latitude, longtitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(update);
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this).build();
                //.addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this).build();
client.connect();

    }

    public void findMap(View viev) {

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> myAddress = geocoder.getFromLocationName(e1.getText().toString(),1);
            Address  address =myAddress.get(0);
            String locality = address.getLocality().toString();
            Toast.makeText(getApplicationContext(),locality.toString(),Toast.LENGTH_SHORT).show();
            double lat=address.getLatitude();
            double lng= address.getLongitude();
            getToLocation(lat,lng,16);
        }catch (IOException e){
            e.getMessage();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
       // LocationServices.FusedLocationApi.requestLocationUpdates(client,request, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null){
            Toast.makeText(getApplicationContext(),"Lokasi tidak ditemukan",Toast.LENGTH_LONG).show();
        }
        else {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng,15);
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
