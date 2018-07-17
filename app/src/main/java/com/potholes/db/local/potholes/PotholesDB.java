package com.potholes.db.local.potholes;

/**
 * Created by Lelouch on 15/07/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.potholes.db.LocalDB;

import java.util.ArrayList;
import java.util.List;

public class PotholesDB {

    private static final int VERSION_BDD = 1;
    private static final String NOM_BDD = "potholes.db";

    private static final String TABLE_POTHOLES = "pothole";
    private static final String COL_ID = "id";
    private static final int NUM_COL_ID = 0;
    private static final String COL_SURFACE = "surface";
    private static final int NUM_COL_SURFACE = 1;
    private static final String COL_LAT = "lat";
    private static final int NUM_COL_LAT = 2;
    private static final String COL_LNG = "lng";
    private static final int NUM_COL_LNG = 3;
    Context ct;
    private SQLiteDatabase bdd;
    private LocalDB maBaseSQLite;

    public PotholesDB(Context context) {
        //On crée la BDD et sa table
        ct = context;
        maBaseSQLite = new LocalDB(context, NOM_BDD, null, VERSION_BDD);
    }

    public void open() {
        //on ouvre la BDD en écriture
        bdd = maBaseSQLite.getWritableDatabase();
    }

    public void close() {
        //on ferme l'accès à la BDD
        bdd.close();
    }

    public SQLiteDatabase getBDD() {
        return bdd;
    }

    public long insertPotholes(Potholes potholes) {
        //Création d'un ContentValues (fonctionne comme une HashMap)
        ContentValues values = new ContentValues();
        //on lui ajoute une valeur associée à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
        values.put(COL_SURFACE, potholes.getSurface());
        values.put(COL_LAT, potholes.getLat());
        values.put(COL_LNG, potholes.getLng());
        //on insère l'objet dans la BDD via le ContentValues
        Log.d("DAtabase insertion", "database  :  saveeeeed");
        return bdd.insert(TABLE_POTHOLES, null, values);
    }

    public int updatePotholes(int id, Potholes potholes) {
        //La mise à jour d'un livre dans la BDD fonctionne plus ou moins comme une insertion
        //il faut simplement préciser quel livre on doit mettre à jour grâce à l'ID
        ContentValues values = new ContentValues();
        values.put(COL_SURFACE, potholes.getSurface());
        values.put(COL_LAT, potholes.getLat());
        values.put(COL_LNG, potholes.getLng());
        return bdd.update(TABLE_POTHOLES, values, COL_ID + " = " + id, null);
    }

    public int removePotholesWithID(int id) {
        //Suppression d'un livre de la BDD grâce à l'ID
        return bdd.delete(TABLE_POTHOLES, COL_ID + " = " + id, null);
    }

    public List<Potholes> getPotholes() {

        List<Potholes> po = new ArrayList<Potholes>();
        //Récupère dans un Cursor les valeurs correspondant à un livre contenu dans la BDD (ici on sélectionne le livre grâce à son titre)
        Cursor c = bdd.rawQuery("SELECT  * FROM " + TABLE_POTHOLES, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Potholes potholes = new Potholes();
            //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            potholes.setId(c.getInt(NUM_COL_ID));
            potholes.setSurface(c.getDouble(NUM_COL_SURFACE));
            potholes.setLat(c.getDouble(NUM_COL_LAT));
            potholes.setLng(c.getDouble(NUM_COL_LNG));
            po.add(potholes);
            c.moveToNext();
        }
        // assurez-vous de la fermeture du curseur
        c.close();
        return po;
    }

    //Cette méthode permet de convertir un cursor en un livre
    private Potholes cursorToPothole(Cursor c) {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) return null;

        //Sinon on se place sur le premier élément
        c.moveToFirst();
        //On créé un livre
        Potholes potholes = new Potholes();
        //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
        potholes.setId(c.getInt(NUM_COL_ID));
        potholes.setSurface(c.getDouble(NUM_COL_SURFACE));
        potholes.setLat(c.getDouble(NUM_COL_LAT));
        potholes.setLng(c.getDouble(NUM_COL_LNG));
        //On retourne le livre
        return potholes;
    }
}