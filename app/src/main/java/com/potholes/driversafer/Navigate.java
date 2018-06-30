package com.potholes.driversafer;


/**
 * Created by Lelouch on 25/06/2018.
 */

import android.content.*;
import android.support.v7.app.AppCompatActivity;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class Navigate extends AppCompatActivity {

    private BroadcastReceiver mLocationUpdateMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(GeoLocationService.LOCATION_DATA);
            Toast.makeText(context, String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
            //SEND LOCATION TO YOUR API HERE
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        GeoLocationService.activity = Navigate.this;
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
}
