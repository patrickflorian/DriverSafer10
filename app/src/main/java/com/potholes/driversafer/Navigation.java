package com.potholes.driversafer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.potholes.View.Dialog.EditPotholesDialog;
import com.potholes.View.Dialog.SettingDialogBuilder;
import com.potholes.View.Dialog.SimpleDialogBuilder;
import com.potholes.View.Map.CustomInfoWindowAdapter;
import com.potholes.View.Map.Retriever;
import com.potholes.View.Map.Road;
import com.potholes.db.HttpHandler;
import com.potholes.db.Settings;
import com.potholes.db.local.potholes.Potholes;
import com.potholes.db.local.potholes.PotholesDB;

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
    private static final LatLng DEFAULT_LAT_LNG = new LatLng(5.3491364, 10.423561);
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
    private Location lastLocation;
    private Marker mylocationMarker;
    private Location destLocation;
    private Location startLocation;


    private AsyncTask mPotholesEditTask;
    private Boolean navigation_mode = false;
    private AlertDialog baterryDialog;
    private BroadcastReceiver mLocationUpdateMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(GeoLocationService.LOCATION_DATA);
            //SEND LOCATION TO YOUR API HERE
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.bundleSavedInstance = savedInstanceState;
        setContentView(R.layout.activity_navigation);
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
                if (lastLocation != null) {
                    /*Retriever my_pr = new Retriever(getApplicationContext());
                    my_pr.findPotholesOn(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),10000000.0 , mMap);*/
                    //Création d'une instance de ma classe PotholesDB
                    PotholesDB potholesDB = new PotholesDB(Navigation.this);
                    //On ouvre la base de données pour écrire dedans
                    potholesDB.open();
                    //On insère le trou que l'on vient de recuperer
                    List<Potholes> list = potholesDB.getPotholes();


                    if (list != null) {
                        Toast.makeText(Navigation.this, "pas vide", Toast.LENGTH_SHORT).show();

                        for (int i = 0; i < list.size(); i++) {
                            Potholes p = list.get(i);
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(p.getLat(), p.getLng()))
                                    .title("Potholes")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                    .snippet("ID :" + p.getId() + "\n" +
                                            "Surface : " + p.getSurface() + "\n"
                                    )
                            ).setTag(p);
                            Toast.makeText(Navigation.this, p.toString(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Navigation.this, "vide", Toast.LENGTH_SHORT).show();
                    }
                    potholesDB.close();
                }
            }
        });
        initBatteryDialog();
        checkBatteryLevel();


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

                mMap.setOnMapClickListener(new OnMapClickListener() {
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
                        LatLng pos = marker.getPosition();
                        destLocation.setLatitude(pos.latitude);
                        destLocation.setLongitude(pos.longitude);
                        if (marker.getTitle().equals("Arrivée")) {
                            drawRoads();

                        }
                    }
                });

                mMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LAT_LNG));
                final Marker carMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_car))
                        .position(DEFAULT_LAT_LNG)
                        .title("Driver Car")
                        .snippet("Lat : " + String.valueOf(DEFAULT_LAT_LNG.latitude) + "\n"
                                + "Lng : " + String.valueOf(DEFAULT_LAT_LNG.longitude) + "\n"
                                + "Vitesse : " + "not yet moving" + "\n"
                                + "Hour : " + DateFormat.getTimeInstance().format(new Date()) + "\n")
                );

                mGps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: clicked gps icon");
                        try {
                            getDeviceLocation();
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                BroadcastReceiver broadcastReceiverForMap = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        lastLocation = intent.getParcelableExtra(GeoLocationService.LOCATION_DATA);
                        LatLng cur_pos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        carMarker.setPosition(cur_pos);
                        carMarker.setSnippet("Lat : " + String.valueOf(cur_pos.latitude) + "\n"
                                + "Lng : " + String.valueOf(cur_pos.longitude) + "\n"
                                + "Vitesse : " + lastLocation.getSpeed() + " m/s" + "\n"
                                + "Hour : " + DateFormat.getTimeInstance().format(new Date(lastLocation.getTime())) + "\n");


                        //Création d'une instance de ma classe PotholesDB
                        PotholesDB potholesDB = new PotholesDB(context);
                        //On ouvre la base de données pour écrire dedans
                        potholesDB.open();
                        //On insère le trou que l'on vient de recuperer
                        List<Potholes> list = potholesDB.getPotholes();
                        if (list != null) {
                            for (int j = 0; j < list.size(); j++) {
                                Potholes p = list.get(j);
                                float[] dist_2 = new float[2];
                                Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), p.getLat(), p.getLng(), dist_2);
                                if (dist_2[0] <= Settings.Alert_Distance) {
                                    TextView textView = findViewById(R.id.p_vitesse);
                                    textView.setText(String.valueOf(lastLocation.getSpeed()));
                                    LinearLayout not = findViewById(R.id.notif_area);
                                    not.animate().translationX(0).setDuration(300);
                                    not.animate().translationX(-800).setDuration(300).setStartDelay(1000);
                                }

                            }
                        } else {
                            Toast.makeText(context, "vide", Toast.LENGTH_SHORT).show();
                        }
                        potholesDB.close();
                    }
                };
                LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverForMap,
                        new IntentFilter(GeoLocationService.LOCATION_UPDATE));

                init();
            }
        } catch (Exception e) {
            Log.e(TAG, "onMapReady: " + e.getMessage());
        }
    }

    private void drawRoads() {
        Road r = new Road(this, mMap);
        r.findAndDrawRoadOnMap(startLocation.getLatitude(), startLocation.getLongitude(), destLocation.getLatitude(), destLocation.getLongitude());
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

    public String getDistance(LatLng my_latlong, LatLng frnd_latlong) {
        Location l1 = new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);

        Location l2 = new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);

        float distance = l1.distanceTo(l2);
        String dist = distance + " M";

        if (distance > 1000.0f) {
            distance = distance / 1000.0f;
            dist = distance + " KM";
        }
        return dist;
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
                item.setTitle("Stop Navigation");
                addNavigationsWidgets();
                mSearchText.setVisibility(View.VISIBLE);
                mSearchText.setEnabled(true);
            } else {
                item.setTitle("Navigation");
                if (mMap != null)
                navigation_mode = true;
                mSearchText.setVisibility(View.INVISIBLE);
                mSearchText.setEnabled(false);
            }

            placesearchLayout.animate().alpha(1).setDuration(400).translationY(0);

        } else if (id == R.id.nav_manage) {
            editSettings();
        } else if (id == R.id.nav_share) {
            //unavailableModule();
            TextView textView = findViewById(R.id.p_vitesse);
            textView.setText(String.valueOf(lastLocation.getSpeed()));
            LinearLayout not = findViewById(R.id.notif_area);
            not.animate().translationX(0).setDuration(200);
        } else if (id == R.id.nav_send) {
            Retriever r = new Retriever(Navigation.this);
            r.findPotholesOn();
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
        dialogBuilder.setNegativeButtonListener("CLOSE", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(getApplication(), new Settings().toString(), Toast.LENGTH_LONG).show();
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

    private void getDeviceLocation() throws Exception {
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

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

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        GeoLocationService.activity = Navigation.this;
        GeoLocationService.start(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationUpdateMessageReceiver,
                new IntentFilter(GeoLocationService.LOCATION_UPDATE));
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationUpdateMessageReceiver);
        GeoLocationService.stop(this);
        super.onPause();
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
                    .position(new LatLng(5.471352, 10.419416)) //bafoussam
                    .snippet("Your Trajet will End here")
                    .title("Arrivée")
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

            Road r = new Road(this, mMap);
            r.findAndDrawRoadOnMap(startLocation.getLatitude(), startLocation.getLongitude(), destLocation.getLatitude(), destLocation.getLongitude());

        } catch (NullPointerException e) {
            e.printStackTrace();
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
            String url = "http://" + Settings.SERVER_IP + "/potholes/app/editPotholes.php?id=" + p.getId() +
                    "&lng=" + p.getLng() +
                    "&lat=" + p.getLat() +
                    "&surface=" + p.getSurface();
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

    /*boolean isCharging(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;  
                    return isCharging;
    }*/
}
