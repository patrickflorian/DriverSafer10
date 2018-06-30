package com.potholes.db;

/**
 * Created by Lelouch on 07/05/2018.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Bitmap;

public class Potholes implements Parcelable {
    public static final Creator<Potholes> CREATOR = new Creator<Potholes>() {
        @Override
        public Potholes createFromParcel(Parcel in) {
            return new Potholes(in);
        }

        @Override
        public Potholes[] newArray(int size) {
            return new Potholes[size];
        }
    };
    private int id;
    private double lat;
    private double lng;
    private double surface;
    private boolean etat;
    private double profondeur;
    private Bitmap image;

    public Potholes(double lat, double lng, double surface, boolean etat, double profondeur) {
        this.lat = lat;
        this.lng = lng;
        this.surface = surface;
        this.etat = etat;
        this.profondeur = profondeur;
    }

    public Potholes(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.surface = 0;
        this.etat = false;
        this.profondeur = 0;
    }

    public Potholes(int id, double lat, double lng, double surface, boolean etat, double profondeur) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.surface = surface;
        this.etat = etat;
        this.profondeur = profondeur;
    }

    protected Potholes(Parcel in) {
        id = in.readInt();
        lat = in.readDouble();
        lng = in.readDouble();
        surface = in.readDouble();
        etat = in.readByte() != 0;
        profondeur = in.readDouble();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public boolean isEtat() {
        return etat;
    }

    public void setEtat(boolean etat) {
        this.etat = etat;
    }

    public double getProfondeur() {
        return profondeur;
    }

    public void setProfondeur(double profondeur) {
        this.profondeur = profondeur;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeDouble(surface);
        parcel.writeByte((byte) (etat ? 1 : 0));
        parcel.writeDouble(profondeur);
        parcel.writeParcelable(image, i);
    }
}
