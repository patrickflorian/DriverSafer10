package com.potholes.View.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.potholes.db.HttpHandler;
import com.potholes.db.Settings;
import com.potholes.db.local.potholes.Potholes;
import com.potholes.db.local.potholes.PotholesDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by Lelouch on 30/06/2018.
 */

public class Retriever {
    Context ct;

    public Retriever(Context ct) {
        this.ct = ct;
    }

    public void findPotholesOn() {
        new PotholesRetriever().execute();
    }

    private class PotholesRetriever extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Retriving potholes started");
        }


        @Override
        protected String doInBackground(Void... voids) {
                HttpHandler sh = new HttpHandler("GET");
                // Making a request to url and getting response
            String url = "http://" + Settings.SERVER_IP + "/potholes/app/findPotholes.php?all";
                String jsonStr = sh.makeServiceCall(url);
                Log.d(TAG, "Response from url: " + jsonStr);

                return jsonStr;
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
                            final int pothole_id = jsonObj.getInt("id");


                            //retrieve lat and long of potholes

                            JSONObject pos = jsonObj.getJSONObject("position");
                            final double lng = pos.getDouble("lng");
                            final double lat = pos.getDouble("lat");

                            //Création d'une instance de ma classe PotholesDB
                            PotholesDB potholesDB = new PotholesDB(ct);


                            //On ouvre la base de données pour écrire dedans
                            potholesDB.open();
                            //On insère le trou que l'on vient de recuperer
                            potholesDB.insertPotholes(new Potholes(pothole_id, lat, lng, surface));
                            potholesDB.close();

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

