package com.example.googlemapsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    int AUTOCOMPLETE_REQUEST_CODE = 1;
    PlacesClient placesClient;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private EditText e1;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    GoogleApiClient client;
    private static final String TAG = "MapActivity";
    private Boolean hasPermission = false;

    private AutoCompleteTextView mSearchText;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int PLACE_PICKER_REQUEST = 1;
    FindAutocompletePredictionsRequest request2;
    private AdapterView.OnItemClickListener mAutocompleteClickListener;
    Place placey;
    FragmentManager fragmenntManager;
    LocationRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
       // e1 = (EditText) findViewById(R.id.editSearch);
        Button searchButton = (Button) findViewById(R.id.buttonSearch);
        FloatingActionButton position = (FloatingActionButton) findViewById(R.id.floatingActionButton);
     //   mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
autocompleteFragment.getFragmentManager();
// Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS));
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, client,LAT_LNG_BOUNDS
                , null);

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


        mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                    places.release();
                    return;
                }
                final com.google.android.gms.location.places.Place place = places.get(0);

                try {
                    // mPlace = new PlaceInfo();

                    Log.d(TAG, "onResult: name: " + place.getName());

                    Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
//                mPlace.setId(place.getId());
                    Log.d(TAG, "onResult: id:" + place.getId());
                    //               mPlace.setLatlng(place.getLatLng());
                    Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                    //              mPlace.setRating(place.getRating());
                    Log.d(TAG, "onResult: rating: " + place.getRating());
                    //               mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                    Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                    //               mPlace.setWebsiteUri(place.getWebsiteUri());
                    Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

//                Log.d(TAG, "onResult: place: " + mPlace.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
                }

                getToLocation(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude), DEFAULT_ZOOM);

                places.release();
            }
        };

        mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideSoftKeyboard();

                final com.google.android.gms.location.places.AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
                final String placeId = item.getPlaceId();

                PendingResult<PlaceBuffer> placeResult = com.google.android.gms.location.places.Places.GeoDataApi
                        .getPlaceById(client, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        };



        //mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        //mSearchText.setOnItemClickListener(mAutocompleteClickListener);

/*
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    findMap();
                }

                return false;
            }
        });*/

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        //    Intent intent = new Autocomplete.IntentBuilder(
        //          AutocompleteActivityMode.OVERLAY, fields)
        //            .build(this);


        //   startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);



        position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission) {
                    setCurrentPosition();
                } else {
                    getLocationPermission();
                    setCurrentPosition();
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

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
                //  initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        client = new GoogleApiClient.Builder(this)
                .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                .addApi(com.google.android.gms.location.places.Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                 .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
        .addOnConnectionFailedListener(this).build();
        client.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        hasPermission = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            hasPermission = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    hasPermission = true;


                }
            }
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    public void setCurrentPosition() {
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (hasPermission) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            assert currentLocation != null;
                            getToLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
        /*
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
        });*/

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
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));

        hideSoftKeyboard();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Location")
                .snippet(""));
        mMap.moveCamera(update);


    }
    //intent autocomplete search
    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("NG") //NIGERIA
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }



    public void findMap() {

        Geocoder geocoder = new Geocoder(this);
        try {
            String search = "";
            if (!mSearchText.getText().toString().equals("")) {
                search = mSearchText.getText().toString();
            }
            List<Address> myAddress = geocoder.getFromLocationName(search, 10);
            Address address = myAddress.get(0);
            String locality = address.getLocality();
            Toast.makeText(getApplicationContext(), locality.toString(), Toast.LENGTH_SHORT).show();
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            LatLng latLng = new LatLng(lat, lng);
            getToLocation(latLng, 8.5f);
          //  AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

         /*   request2 = FindAutocompletePredictionsRequest.builder()
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
                    .setQuery()
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
            });*/

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
