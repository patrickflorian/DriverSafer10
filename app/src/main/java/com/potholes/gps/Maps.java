package com.potholes.gps;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.potholes.driversafer.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Lelouch on 17/05/2018.
 */

public class Maps {
    static Location dlocation;
    private static Context context;
    private static LatLng currentLocation;
    private static LatLng destLocation;
    private static Marker current_location_marker;
    private static Marker dest_location_marker;

    public static void init(Context cont, GoogleMap mMap) {
        context = cont;
        currentLocation = getDeviceLocation();
        destLocation = getDeviceLocation();

        GoogleMapOptions options = new GoogleMapOptions();


        options.mapType(GoogleMap.MAP_TYPE_HYBRID)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        addCurrentLocationMarker(mMap);
        addDestinationMarker(mMap);

    }

    public static void addDestinationMarker(final GoogleMap map) {

        MarkerOptions markerOptions = new MarkerOptions()
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                );
        map.setOnMarkerDragListener(
                new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        dest_location_marker = marker;
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                        destLocation = marker.getPosition();
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        drawRoad(map);
                    }
                }
        );

    }

    public static void addCurrentLocationMarker(final GoogleMap map) {

        MarkerOptions markerOptions = new MarkerOptions()
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                );
        map.addMarker(markerOptions);


    }

    private static LatLng getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);


        try {
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete : found location");
                        dlocation = (Location) task.getResult();
                    } else {
                        Log.d(TAG, "onComplete : current location is null");
                        Toast.makeText(context, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation : Security Exceptioin" + e.getMessage());
        }
        return new LatLng(dlocation.getLatitude(), dlocation.getLatitude());
    }

    public static void drawRoad(GoogleMap map) {

    }

}
