package com.potholes.driversafer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.potholes.db.local.potholes.Potholes;

/**
 * Created by Lelouch on 25/06/2018.
 */

public class EditActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = "Edit Pothole Activity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 16f;

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
    private Bitmap bmp;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        if (mLocationPermissionGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    LatLng po = marker.getPosition();
                    lat_view.setText(String.valueOf(po.latitude));
                    lng_view.setText(String.valueOf(po.longitude));
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                }
            });


            final Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(potholes.getLat(), potholes.getLng()))
                    .visible(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .draggable(true)
            );
            lat_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE
                            || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                        marker.setPosition(new LatLng(Double.valueOf(lat_view.getText().toString()), Double.valueOf(lng_view.getText().toString())));
                    }
                    return false;
                }
            });
            lng_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE
                            || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                        marker.setPosition(new LatLng(Double.valueOf(lat_view.getText().toString()), Double.valueOf(lng_view.getText().toString())));
                    }
                    return false;
                }
            });
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(potholes.getLat(), potholes.getLng()), DEFAULT_ZOOM));
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
            bmp = intent.getParcelableExtra("img");
        }

        positive_button = findViewById(R.id.dialog_positive_btn);
        negative_button = findViewById(R.id.dialog_negative_btn);

        negative_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("MESSAGE", "NOT_A_POTHOLE");
                setResult(2, intent);
                finish();
            }
        });
        positive_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("MESSAGE", "success");
                setResult(2, intent);
                finish();
            }
        });

        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        switcher = findViewById(R.id.detection_switcher);

        lat_view = findViewById(R.id.lat);
        lat_view.setText(String.valueOf(potholes.getLat()));



        lng_view = findViewById(R.id.lng);
        lng_view.setText(String.valueOf(potholes.getLng()));

        image_view = findViewById(R.id.image_view);
        image_view.setImageBitmap(bmp);


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

    @Override
    public void onBackPressed() {
        if (switcher.getCurrentView().getId() == R.id.view2) {
            switcher.showPrevious();
        } else {
            Intent intent = new Intent();
            intent.putExtra("MESSAGE", "NOT_A_POTHOLE");
            setResult(2, intent);
            super.onBackPressed();
        }
    }
}
