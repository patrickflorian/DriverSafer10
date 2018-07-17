package com.potholes.db;

import android.content.Context;


/**
 * Created by Lelouch on 07/05/2018.
 */

public final class Settings {
    public static final int ID = 0;
    public static String SERVER_IP = "192.168.43.108";
    public static double Alert_Distance = 500;
    private static double distance = 150.0;
    private static Boolean notification_enabled = true;
    private static double hauteur = 80.0;

    public Settings(Boolean notification_enabled, Double hauteur, Double distance, Context context) {
        Settings.hauteur = hauteur;
        Settings.distance = distance;
        Settings.notification_enabled = notification_enabled;
    }

    public Settings() {

    }

    public static Double getDistance() {
        return distance;
    }

    public static void setDistance(Double distance) {
        Settings.distance = distance;
    }

    public static Double getHauteur() {
        return hauteur;
    }

    public static void setHauteur(Double hauteur) {
        Settings.hauteur = hauteur;
    }

    public static Boolean isNotification_enabled() {
        return notification_enabled;
    }

    public static void setNotification_enabled(Boolean notification_enabled) {
        Settings.notification_enabled = notification_enabled;
    }

    @Override
    public String toString() {
        return
                "isNotification enabled" + isNotification_enabled() + "\n"
                        + "distance" + getDistance() + "\n"
                        + "hauteur" + getHauteur() + "\n";
    }
}
