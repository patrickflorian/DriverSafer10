package com.potholes.View.Map;

/**
 * Created by Lelouch on 25/06/2018.
 */


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.potholes.db.HttpHandler;
import com.potholes.db.local.potholes.Potholes;
import com.potholes.db.local.potholes.PotholesDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class Road {
    public static List<LatLng> roads;
    Context context;
    GoogleMap map;

    public Road(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;
    }

    public void findAndDrawRoadOnMap(double sourcelat, double sourcelog, double destlat, double destlog) {
        String url = makeURLForRoad(sourcelat, sourcelog, destlat, destlog);
        new RoadAsyncTask(url).execute();
    }

    public List<LatLng> drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            Polyline line = map.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(9)
                    .color(Color.BLACK)//Google maps blue color
                    .geodesic(true)
            );
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
            return list;
        } catch (JSONException e) {

        }
        return null;
    }

    private void findAndDrawRoadPotholes(List<LatLng> road) {
        List<Potholes> plist = new ArrayList<Potholes>();
        Retriever r = new Retriever(context);

        for (int i = 0; i < road.size() - 1; i++) {
            LatLng s = road.get(i), e = road.get(i + 1);

            try {
                float[] dist = new float[2];
                Location.distanceBetween(s.latitude, s.longitude, e.latitude, e.longitude, dist);
                LatLng center = new LatLngBounds(s, e).getCenter();
                r.findPotholesOn();

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
                        Location.distanceBetween(center.latitude, center.longitude, p.getLat(), p.getLng(), dist_2);
                        if (dist_2[0] <= dist[0]) map.addMarker(new MarkerOptions()
                                .position(new LatLng(p.getLat(), p.getLng()))
                                .title("Potholes")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .snippet("ID :" + p.getId() + "\n" +
                                        "Surface : " + p.getSurface() + "\n"
                                )
                        ).setTag(p);

                    }
                } else {
                    Toast.makeText(context, "vide", Toast.LENGTH_SHORT).show();
                }
                potholesDB.close();
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "gtRoadPotholes: " + ex.getMessage());
            }

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public String makeURLForRoad(double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyDue0KI-y5-OdPOfNJ3Bp_ptrDHEl7ng7M");
        return urlString.toString();
    }

    private class RoadAsyncTask extends AsyncTask<Void, Void, String> {
        String url;
        private ProgressDialog progressDialog;

        RoadAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpHandler jParser = new HttpHandler(HttpHandler.GET);
            String json = jParser.makeServiceCall(url);
            return json;
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
            };
            progressDialog.hide();
            if (result != null) {
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Good", Toast.LENGTH_SHORT).show();
                    }
                };
                roads = drawPath(result);
                findAndDrawRoadPotholes(roads);
            }
        }
    }

}
