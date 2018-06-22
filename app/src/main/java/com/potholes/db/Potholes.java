package com.potholes.db;

/**
 * Created by Lelouch on 07/05/2018.
 */

public class Potholes {
    private int id;
    private double lat;
    private double lng;
    private double surface;
    private boolean etat;
    private double profondeur;

    public Potholes(double lat, double lng, double surface, boolean etat, double profondeur) {
        this.lat = lat;
        this.lng = lng;
        this.surface = surface;
        this.etat = etat;
        this.profondeur = profondeur;
    }

    public Potholes(int id, double lat, double lng, double surface, boolean etat, double profondeur) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.surface = surface;
        this.etat = etat;
        this.profondeur = profondeur;
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
}
