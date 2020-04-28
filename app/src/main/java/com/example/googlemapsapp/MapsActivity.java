package com.example.googlemapsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {
    private static int LOCATION_PERMISSION_REQUEST_CODE = 1;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    PlacesClient placesClient;
    private GoogleMap mMap;

    private EditText e1;
    GoogleApiClient client;
    FindAutocompletePredictionsRequest request2;

    PlaceAutocompleteFragment placeAutoComplete;
    Place placey;
    LocationRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        e1 = (EditText) findViewById(R.id.editSearch);
        Button searchButton = (Button) findViewById(R.id.buttonSearch);
        FloatingActionButton position = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
        placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

// Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        //placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng myLat = place.getLatLng();
                getToLocation(myLat, 8.5f);
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (e1.getText() == null) {
                    Toast.makeText(getApplicationContext(), "isi pada pencarian", Toast.LENGTH_SHORT).show();
                } else {
                    findMap();
                }

            }
        });


        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        //    Intent intent = new Autocomplete.IntentBuilder(
        //          AutocompleteActivityMode.OVERLAY, fields)
        //            .build(this);


        //   startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);


        position.setOnClickListener(new View.OnClickListener() {
            private static final String ACCESS_FINE_LOCATION = "1";


            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    setCurrentPosition();
                } else {

                    if (Build.VERSION.SDK_INT >= 23) { // Marshmallow

                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    } else {

                        setCurrentPosition();
                    }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng lat = place.getLatLng();

                Log.i("Alamat", "Place: " + place.getName() + ", " + place.getId());
                getToLocation(lat, 8.5f);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("error", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void setCurrentPosition() {
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        ((Task) placeResponse).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = (FindCurrentPlaceResponse) task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i("ditemukan", String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                        Place place=placeLikelihood.getPlace();
                        getToLocation(place.getLatLng(),15.0f);
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("gagal", "Place not found: " + apiException.getStatusCode());
                        Toast.makeText(getApplicationContext(), "Place not found: " + apiException.getStatusCode(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                setCurrentPosition();

            } else { // if permission is not granted

                Toast.makeText(getApplicationContext(), "Tidak Mendapat Izin", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        LatLng latLng = new LatLng(-34, 151);
        getToLocation(latLng, 16);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void getToLocation(LatLng latLng, float zoom) {

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.addCircle(new CircleOptions().radius(200).strokeColor(Color.argb(65, 255, 144, 133)).fillColor(Color.argb(55, 255, 144, 133)).center(latLng));
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(update);
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this).build();
        //.addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this).build();
        client.connect();

    }

    public void findMap() {

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> myAddress = geocoder.getFromLocationName(e1.getText().toString(), 1);
            Address address = myAddress.get(0);
            String locality = address.getLocality();
            Toast.makeText(getApplicationContext(), locality.toString(), Toast.LENGTH_SHORT).show();
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            LatLng latLng = new LatLng(lat, lng);
            getToLocation(latLng, 8.5f);
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

            request2 = FindAutocompletePredictionsRequest.builder()
                    // Call either setLocationBias() OR setLocationRestriction().
                    .setLocationBias(RectangularBounds.newInstance(
                            new LatLng(-33.880490, 151.184363),
                            new LatLng(-33.858754, 151.229596)))
                    //.setLocationRestriction(RectangularBounds.newInstance(
                    //                new LatLng(-33.880490, 151.184363),
                    //                new LatLng(-33.858754, 151.229596)))

                    .setOrigin(new LatLng(-33.8749937, 151.2041382))
                    .setCountries("AU", "NZ")
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setSessionToken(token)
                    .setQuery(e1.getText().toString())
                    .build();
            placesClient.findAutocompletePredictions(request2).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                @Override
                public void onSuccess(FindAutocompletePredictionsResponse response) {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        Log.i("id", prediction.getPlaceId());
                        Log.i("Alamat", prediction.getPrimaryText(null).toString());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("error", "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        } catch (IOException e) {
            e.getMessage();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        new LocationRequest();
        request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        // LocationServices.FusedLocationApi.requestLocationUpdates(client,request, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(getApplicationContext(), "Lokasi tidak ditemukan", Toast.LENGTH_LONG).show();
        } else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            getToLocation(latLng, 16);
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
