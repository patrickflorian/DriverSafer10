package com.potholes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lelouch on 15/07/2018.
 */

public class LocalDB extends SQLiteOpenHelper {

    private static final String TABLE_POTHOLES = "pothole";
    private static final String COL_ID = "id";
    private static final String COL_SURFACE = "surface";
    private static final String COL_LAT = "lat";
    private static final String COL_LNG = "lng";

    private static final String CREATE_BDD = "CREATE TABLE " + TABLE_POTHOLES + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_SURFACE + " REAL NOT NULL, "
            + COL_LAT + " REAL NOT NULL, "
            + COL_LNG + " REAL NOT NULL);";

    public LocalDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //on crée la table à partir de la requête écrite dans la variable CREATE_BDD
        db.execSQL(CREATE_BDD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //On peut faire ce qu'on veut ici moi j'ai décidé de supprimer la table et de la recréer
        //comme ça lorsque je change la version les id repartent de 0
        db.execSQL("DROP TABLE " + TABLE_POTHOLES + ";");
        onCreate(db);
    }
}
