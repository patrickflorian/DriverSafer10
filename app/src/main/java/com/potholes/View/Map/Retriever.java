package com.potholes.View.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.potholes.db.HttpHandler;
import com.potholes.db.Potholes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by Lelouch on 30/06/2018.
 */

public class Retriever {

    public void findPotholesOn(LatLng point, double rayon, GoogleMap map) {
        new PotholesRetriever(point, rayon, map).execute();
    }

    private class PotholesRetriever extends AsyncTask<Void, Void, String> {
        LatLng point;
        double rayon;
        GoogleMap googleMap;


        public PotholesRetriever(LatLng point, double rayon, GoogleMap map) {
            this.point = point;
            this.rayon = rayon;
            this.googleMap = map;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Retriving potholes started");
        }


        @Override
        protected String doInBackground(Void... voids) {
            if (point != null) {
                HttpHandler sh = new HttpHandler("GET");
                // Making a request to url and getting response
                String url = "http://192.168.43.193/potholes/app/findPotholes.php?lat=" + point.latitude +
                        "&lng=" + point.longitude +
                        "&rayon=" + rayon;
                String jsonStr = sh.makeServiceCall(url);
                Log.d(TAG, "Response from url: " + jsonStr);

                return jsonStr;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            if (jsonStr != null) {
                try {

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


                            googleMap.addMarker(new MarkerOptions()
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


                    }
                }
                // tmp hash map for single contact

                catch (final JSONException e) {

                    Log.e(TAG, e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
            }
        }
    }
}

