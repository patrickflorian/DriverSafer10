package com.potholes.driversafer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.potholes.db.Potholes;

/**
 * Created by Lelouch on 25/06/2018.
 */

class EditActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = "Edit Pothole Activity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static Button positive_button;
    private static Button negative_button;
    private static Button next;
    private static ImageView prev;
    private static ImageView image_view;
    public Potholes potholes;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private ViewSwitcher switcher;
    private TextInputEditText lat_view;
    private TextInputEditText lng_view;

    @Override
    public void onMapReady(GoogleMap googleMap) {

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detection_layout);
        Intent intent = getIntent();
        if (intent != null) {
            potholes = intent.getParcelableExtra("trou");
            if (potholes != null) {

            }
        }

        positive_button = findViewById(R.id.dialog_positive_btn);
        negative_button = findViewById(R.id.dialog_negative_btn);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        switcher = findViewById(R.id.detection_switcher);

        lat_view = findViewById(R.id.lat);

        lng_view = findViewById(R.id.lng);

        image_view = findViewById(R.id.image_view);
        image_view.setImageBitmap(potholes.getImage());


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switcher.showNext();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switcher.showPrevious();
            }
        });
    }

    public void setNegativeButtonListener(String label, View.OnClickListener onClickListener) {
        negative_button.setOnClickListener(onClickListener);
        negative_button.setText(label);
    }

    public void setPositiveButtonListener(String label, View.OnClickListener onClickListener) {
        positive_button.setOnClickListener(onClickListener);
        positive_button.setText(label);
    }

    public boolean checkEntry() {

        lat_view.setError(null);
        lng_view.setError(null);

        View focusView = null;
        boolean cancel = false;


        String lat = lat_view.getText().toString();
        String lng = lng_view.getText().toString();
        //check valid lat
        if (TextUtils.isEmpty(lat)) {
            lat_view.setError(getString(R.string.error_field_required));
            focusView = lat_view;
            cancel = true;
        }
        //check valid lng
        if (TextUtils.isEmpty(lng)) {
            lng_view.setError(getString(R.string.error_field_required));
            focusView = lng_view;
            cancel = true;
        }
        if (!cancel) {
            potholes.setLat(Double.valueOf(lat));
            potholes.setLng(Double.valueOf(lng));
        }
        return cancel;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }
}
