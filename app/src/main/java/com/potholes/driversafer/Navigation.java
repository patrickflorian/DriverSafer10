package com.potholes.driversafer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.content.ContextWrapper;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.LinearLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.potholes.View.Dialog.EditPotholesDialog;
import com.potholes.View.Dialog.SettingDialogBuilder;
import com.potholes.View.Dialog.SimpleDialogBuilder;
import com.potholes.View.Markers.CustomInfoWindowAdapter;
import com.potholes.db.HttpHandler;
import com.potholes.db.Potholes;
import com.potholes.db.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
        ,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private static final String TAG = "NavigationActivity";
    public Bundle bundleSavedInstance;
    private List<Potholes> potholes_list = new ArrayList<Potholes>();
    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private AlertDialog potholesEditDialog;
    private RelativeLayout placesearchLayout;
    private ImageView ic_magnify, start_zoom_btn, arrival_zoom_btn, stop_nav_btn, next, prev;

    private NavigationView navigationView;
    private FloatingActionButton exit_btn;
    private LinearLayout bottom_bar;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation = null;
    private Marker mylocationMarker;
    private Location destLocation;
    private Location startLocation;


    private AsyncTask mPotholesEditTask;
    private Boolean navigation_mode = false;
    private LocationUpdateManager myLocationManager;
    private AlertDialog baterryDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.bundleSavedInstance = savedInstanceState;
        setContentView(R.layout.activity_navigation);


        myLocationManager = new LocationUpdateManager();
        myLocationManager.startLocationUpdates();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        bottom_bar = findViewById(R.id.bottom_bar);
        bottom_bar.setVisibility(View.INVISIBLE);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSearchText = findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_my_car);

        /*exit_btn = findViewById(R.id.exit_to_app);
        ex it_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmExit();
            }
        });*/

        placesearchLayout = findViewById(R.id.relLayout1);
        mSearchText.setVisibility(View.INVISIBLE);
        mSearchText.setEnabled(false);
        start_zoom_btn = findViewById(R.id.start_zoom);
        arrival_zoom_btn = findViewById(R.id.arrival_zoom);
        stop_nav_btn = findViewById(R.id.stop_nav);
        next = findViewById(R.id.next_pothole);
        prev = findViewById(R.id.prev_pothole);

        getLocationPermission();

        ImageView nav_show_button = findViewById(R.id.ic_menu);


        nav_show_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (navigationView.isShown()) {
                    navigationView.showContextMenu();
                } else {
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    } else {
                        drawer.openDrawer(GravityCompat.START);
                    }
                }
            }
        });
        ic_magnify = findViewById(R.id.ic_magnify);

        ic_magnify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PotholesRetriever my_pr = new PotholesRetriever();
                my_pr.execute();
            }
        });
        initBatteryDialog();
        checkBatteryLevel();
//        FloatingActionButton setting_btn = findViewById(R.id.settings_btn);
//        setting_btn.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                editSettings();
//            }
//        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

            if (mLocationPermissionGranted) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                //setting custom info windows

                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(Navigation.this));
                mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        if (marker.getTitle().equals("Potholes")) {
                            Potholes p = (Potholes) marker.getTag();
                            editPotholes(p);
                        }
                        hideSoftKeyboard();
                    }
                });
                mMap.setMinZoomPreference(7);
                //getDeviceLocation();

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {

                    }
                });
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (placesearchLayout.getAlpha() == 0) {
                            showTools();
                        } else {
                            hideTools();
                        }
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        hideTools();
                        return false;
                    }
                });
                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {


                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        if (marker.getTitle().equals("Arrivée")) {
                            drawRoads();

                        }
                    }
                });

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(5.3491364, 10.423561)));
                init();

            }
        } catch (Exception e) {
            Log.e(TAG, "onMapReady: " + e.getMessage());
        }
    }

    private void drawRoads() {
    }


    void checkBatteryLevel() {
        if (getBatteryLevel() < 20) {
            if (!baterryDialog.isShowing()) baterryDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            confirmExit();
        }
    }

    private void confirmExit() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.exit_dialog_layout, null);

        SimpleDialogBuilder dialogBuilder = new SimpleDialogBuilder(this, alertView, "Exit the application \n all your data will be lost", SimpleDialogBuilder.DIALOG_STYLE_DANGER);
        dialogBuilder.setCancelable(true);

        final AlertDialog dialog = dialogBuilder.create();


        dialogBuilder.setPositiveButtonListener("exit", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                Navigation.super.onBackPressed();

            }
        });
        dialogBuilder.setNegativeButtonListener("cancel", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "No button clicked", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();


    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent = new Intent(Navigation.this, DetectionActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_destination) {
            if (item.getTitle().toString().equals("Navigation")) {
                navigation_mode = false;
                updateLocation();
                item.setTitle("Stop Navigation");
                myLocationManager.startLocationUpdates();
                addNavigationsWidgets();
                mSearchText.setVisibility(View.VISIBLE);
                mSearchText.setEnabled(true);
            } else {
                item.setTitle("Navigation");
                if (mMap != null) mMap.clear();
                myLocationManager.stopLocationUpdates();
                lastLocation = myLocationManager.mCurrentLocation;
                navigation_mode = true;
                mSearchText.setVisibility(View.INVISIBLE);
                mSearchText.setEnabled(false);
            }

            placesearchLayout.animate().alpha(1).setDuration(400).translationY(0);

        } else if (id == R.id.nav_manage) {
            editSettings();
        } else if (id == R.id.nav_share) {
            unavailableModule();
        } else if (id == R.id.nav_send) {
            unavailableModule();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void unavailableModule() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.exit_dialog_layout, null);

        SimpleDialogBuilder dialogBuilder = new SimpleDialogBuilder(this, alertView,
                "This module is on construction", SimpleDialogBuilder.DIALOG_STYLE_DANGER);
        dialogBuilder.setCancelable(true);

        final AlertDialog dialog = dialogBuilder.create();


        dialogBuilder.setPositiveButtonListener("save", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialogBuilder.hidePositiveButton(true);
        dialogBuilder.setNegativeButtonListener("close", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "No button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();


    }

    void initBatteryDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.exit_dialog_layout, null);

        SimpleDialogBuilder dialogBuilder = new SimpleDialogBuilder(this, alertView,
                "Low Battery level \n please plug on a charger or ", SimpleDialogBuilder.DIALOG_STYLE_WARNING);
        dialogBuilder.setCancelable(true);

        baterryDialog = dialogBuilder.create();

        dialogBuilder.setPositiveButtonListener("continue", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baterryDialog.cancel();
            }
        });

        dialogBuilder.setNegativeButtonListener("close", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baterryDialog.dismiss();
                checkBatteryLevel();
            }
        });

    }

    private void editSettings() {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.setting_layout, null);

        SettingDialogBuilder dialogBuilder = new SettingDialogBuilder(this, alertView);
        dialogBuilder.setCancelable(true);

        final AlertDialog dialog = dialogBuilder.create();


        dialogBuilder.setPositiveButtonListener("save", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.cancel();
                Toast.makeText(getApplication(), new Settings().toString(), Toast.LENGTH_LONG).show();
            }
        });
        dialogBuilder.setNegativeButtonListener("cancel", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(getApplication(),
                        "No button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void editPotholes(Potholes p) {
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.edit_potholes_dialog_layout, null);

        final EditPotholesDialog dialogBuilder = new EditPotholesDialog(this, alertView, p);
        dialogBuilder.setCancelable(true);

        potholesEditDialog = dialogBuilder.create();
        dialogBuilder.setPositiveButtonListener("save", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!dialogBuilder.attemptEdit()) {
                    EditPotholesTask mPotholesEditTask = new EditPotholesTask(dialogBuilder.potholes);
                    mPotholesEditTask.execute();
                }
            }
        });
        dialogBuilder.setNegativeButtonListener("close", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                potholesEditDialog.dismiss();
                Toast.makeText(getApplication(),
                        "No button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        potholesEditDialog.show();
    }

    private void init() {
        Log.d(TAG, "init: initializing");


        hideTools();
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                        onBackPressed();
                    }

                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        hideSoftKeyboard();
    }

    private void hideTools() {
        placesearchLayout.animate().alpha(0).translationY(-50);

        //mGps.animate().translationX(50);
        ic_magnify.animate().translationX(50);
    }

    private void showTools() {
        placesearchLayout.animate().alpha(1).setDuration(400).translationY(0);

        //mGps.animate().alpha(1).setDuration(400).translationX(0);
        ic_magnify.animate().translationX(0);

    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(Navigation.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }

    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                if (location != null) {
                    location.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {

                                try {
                                    lastLocation = (Location) task.getResult();
                                    Log.d(TAG, "onComplete:::" + lastLocation.getLatitude() + " // " + lastLocation.getLongitude());
                                    moveCamera(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                            DEFAULT_ZOOM,
                                            "My Location");
                                    updateMylocationMarker();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Log.d(TAG, "onComplete: current location is null");
                                Toast.makeText(Navigation.this, "getDeviceLocation: task not successful", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        } catch (NullPointerException e) {
            Toast.makeText(this, "Null pointer Exception :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(Navigation.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    void updateMylocationMarker() {


        Marker marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_car))
                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .title("Driver Car")
                .snippet("Lat : " + lastLocation.getLatitude() + "\n"
                        + "Lng : " + lastLocation.getLongitude() + "\n"
                        + "Vitesse : " + lastLocation.getSpeed() + "\n"
                        + "Hour : " + DateFormat.getTimeInstance().format(new Date(lastLocation.getTime())) + "\n")
        );
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResume() {
        super.onResume();

        // Resuming location updates depending on button state and
        // allowed permissions
        if (myLocationManager.mRequestingLocationUpdates && myLocationManager.checkPermissions()) {
            myLocationManager.startLocationUpdates();
        }

        //updateLocationUI();
    }

    public void updateLocation() {
        if (navigation_mode) {
            if (myLocationManager.mCurrentLocation != null)
                lastLocation = myLocationManager.mCurrentLocation;
        } else {
            getDeviceLocation();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", myLocationManager.mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", myLocationManager.mCurrentLocation);
        outState.putString("last_updated_on", myLocationManager.mLastUpdateTime);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myLocationManager.stopLocationUpdates();
    }

    private int getBatteryLevel() {
        int batteryLevel = -1;
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            if (batteryManager != null) {
                batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }

        return batteryLevel;
    }

    private void addNavigationsWidgets() {

        bottom_bar.setVisibility(View.VISIBLE);
        try {
            startLocation = new Location(lastLocation);
            mMap.addMarker(new MarkerOptions()
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
                    .position(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()))
                    .snippet("Your Trajet has Starte here")
                    .title("Point de Départ du trajet")
                    .rotation(60)
            );
            destLocation = new Location(lastLocation);
            destLocation.setLatitude(destLocation.getLatitude() + 0.3);
            destLocation.setLongitude(destLocation.getLongitude() + 0.3);
            mMap.addMarker(new MarkerOptions()
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrival))
                    .position(new LatLng(destLocation.getLatitude() + 0.3, destLocation.getLongitude() + 0.3))
                    .snippet("Your Trajet will End here")
                    .title("Arrivée")
                    .rotation(-60)
                    .flat(true)
            );


            start_zoom_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLocation.getLatitude(), startLocation.getLongitude()), DEFAULT_ZOOM));
                }
            });
            arrival_zoom_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(destLocation.getLatitude(), destLocation.getLongitude()), DEFAULT_ZOOM));
                }
            });
            stop_nav_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });


        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private class PotholesRetriever extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "starting retrieving", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (lastLocation != null) {
                HttpHandler sh = new HttpHandler("GET");
                // Making a request to url and getting response
                String url = "http://192.168.43.108/potholes/app/findPotholes.php?lat=" + lastLocation.getLatitude() +
                        "&lng=" + lastLocation.getLongitude() +
                        "&rayon=" + Settings.getRayonDetection();
                String jsonStr = sh.makeServiceCall(url);

                Log.d(TAG, "Response from url: " + jsonStr);
                if (jsonStr != null) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.clear();
                                mMap.addCircle(new CircleOptions()
                                        .radius(Settings.getRayonDetection() * 1000)
                                        .fillColor(R.color.colorDriverSafer_warning)
                                        .center(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                                        .strokeColor(R.color.colorDriverSafer_warning)

                                );
                                updateMylocationMarker();
                            }
                        });

                        JSONObject jsonObjet = new JSONObject(jsonStr);
                        JSONArray list = jsonObjet.getJSONArray("list");
                        // looping through All Contacts
                        int count = jsonObjet.getInt("count");
                        if (list != null) {
                            // Getting JSON Array node

                            for (int i = 0; i < list.length(); i++) {

                                JSONObject jsonObj = list.getJSONObject(i);
                                final Double surface = jsonObj.getDouble("surface");
                                final Double profondeur = jsonObj.getDouble("profondeur");
                                final int pothole_id = jsonObj.getInt("id");
                                final Boolean etat = jsonObj.getBoolean("etat");

                                //retrieve lat and long of potholes

                                JSONObject pos = jsonObj.getJSONObject("position");
                                final double lng = pos.getDouble("lng");
                                final double lat = pos.getDouble("lat");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(lat, lng))
                                                .title("Potholes")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                .snippet("ID :" + pothole_id + "\n" +
                                                        "Surface : " + surface + "\n" +
                                                        "Profondeur : " + profondeur + "\n" +
                                                        "Etat : " + etat + "\n"
                                                )
                                        ).setTag(new Potholes(pothole_id, lat, lng, surface, etat, profondeur));
                                    }
                                });


                            }
                        }
                        // tmp hash map for single contact

                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    return null;
                }
            }
            return null;
        }
    }

    private class EditPotholesTask extends AsyncTask<Void, Void, Void> {
        Potholes p;

        public EditPotholesTask(Potholes potholes) {

            this.p = potholes;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler("GET");
            // Making a request to url and getting response
            String url = "http://192.168.43.108/potholes/app/editPotholes.php?id=" + p.getId() +
                    "&lng=" + p.getLng() +
                    "&lat=" + p.getLat() +
                    "&surface=" + p.getSurface() +
                    "&etat=" + p.isEtat() +
                    "&profondeur=" + p.getProfondeur();
            String res = sh.makeServiceCall(url);
            if (res.contains("true")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Potholes Success modified", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to modified potholes", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Log.d(TAG, "Response from url: " + res);
            return null;
        }
    }

    public class LocationUpdateManager {

        // location updates interval - 10sec
        private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
        // fastest updates interval - 5 sec
        // location updates will be received if another app is requesting the locations
        // than your app can handle
        private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
        private static final int REQUEST_CHECK_SETTINGS = 100;
        public Location mCurrentLocation;
        // location last updated time
        private String mLastUpdateTime;
        // bunch of location related apis
        private FusedLocationProviderClient mFusedLocationClient;
        private SettingsClient mSettingsClient;
        private LocationRequest mLocationRequest;
        private LocationSettingsRequest mLocationSettingsRequest;
        private LocationCallback mLocationCallback;
        // boolean flag to toggle the ui
        private Boolean mRequestingLocationUpdates;

        LocationUpdateManager() {
            // initialize the necessary libraries
            initClient();

            // restore the values from saved instance state
            restoreValuesFromBundle(bundleSavedInstance);
        }

        private void initClient() {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Navigation.this);
            mSettingsClient = LocationServices.getSettingsClient(Navigation.this);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // location is received
                    mCurrentLocation = locationResult.getLastLocation();
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    updateLocation();
                    updateMylocationMarker();
                    if (navigation_mode) new PotholesRetriever().execute();

                }
            };

            mRequestingLocationUpdates = false;

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();
        }

        private void restoreValuesFromBundle(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey("is_requesting_updates")) {
                    mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
                }

                if (savedInstanceState.containsKey("last_known_location")) {
                    mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
                }

                if (savedInstanceState.containsKey("last_updated_on")) {
                    mLastUpdateTime = savedInstanceState.getString("last_updated_on");
                }
            }

            //updateLocationUI();
        }

        /**
         * Starting location updates
         * Check whether location settings are satisfied and then
         * location updates will be requested
         */
        private void startLocationUpdates() {
            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(Navigation.this, new OnSuccessListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "All location settings are satisfied.");

                            Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                            //noinspection MissingPermission
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                            mRequestingLocationUpdates = true;

                            //updateLocationUI();
                        }
                    })
                    .addOnFailureListener(Navigation.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                            "location settings ");
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(Navigation.this, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    Log.e(TAG, errorMessage);

                                    Toast.makeText(Navigation.this, errorMessage, Toast.LENGTH_LONG).show();
                            }

                            //updateLocationUI();
                        }
                    });
        }

        public void stopLocationUpdates() {
            mRequestingLocationUpdates = false;

            // Removing location updates
            mFusedLocationClient
                    .removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(Navigation.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                            //toggleButtons();
                        }
                    });

        }

        private boolean checkPermissions() {
            int permissionState = ActivityCompat.checkSelfPermission(Navigation.this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return permissionState == PackageManager.PERMISSION_GRANTED;
        }

    }
    /*boolean isCharging(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;  
                    return isCharging;
    }*/
}
