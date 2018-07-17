package com.potholes.db.local.potholes;

/**
 * Created by Lelouch on 07/05/2018.
 */

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

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
    private Bitmap image;

    public Potholes(double lat, double lng, double surface) {
        this.lat = lat;
        this.lng = lng;
        this.surface = surface;
    }

    public Potholes(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.surface = 0;
    }

    public Potholes(int id, double lat, double lng, double surface) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.surface = surface;
    }

    public Potholes() {
    }

    protected Potholes(Parcel in) {
        id = in.readInt();
        lat = in.readDouble();
        lng = in.readDouble();
        surface = in.readDouble();
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
        parcel.writeParcelable(image, i);
    }

    @Override
    public String toString() {
        return id + " " + lat + " " + lng;
    }
}
