package com.potholes.db;

import android.content.Context;


/**
 * Created by Lelouch on 07/05/2018.
 */

public final class Settings {
    public static final int ID = 0;
    private static int RayonDetection = 3;
    private static Boolean Camera_enabled = true;
    private static Boolean notification_enabled = true;
    private static Boolean collaborate_enabled = true;
    private static Context context;

    public Settings(int rayonDetection, Boolean camera_enabled, Boolean notification_enabled, Boolean collaborate_enabled, Context context) {
        Settings.RayonDetection = rayonDetection;
        Settings.Camera_enabled = camera_enabled;
        Settings.notification_enabled = notification_enabled;
        Settings.collaborate_enabled = collaborate_enabled;
        Settings.context = context;
    }

    public Settings() {

    }


    public static double getRayonDetection() {
        return RayonDetection;
    }

    public static void setRayonDetection(int rayonDetection) {
        RayonDetection = rayonDetection;
    }

    public static Boolean isCamera_enabled() {
        return Camera_enabled;
    }

    public static void setCamera_enabled(Boolean camera_enabled) {
        Camera_enabled = camera_enabled;
    }

    public static Boolean isNotification_enabled() {
        return notification_enabled;
    }

    public static void setNotification_enabled(Boolean notification_enabled) {
        Settings.notification_enabled = notification_enabled;
    }

    public static Boolean isCollaborate_enabled() {
        return collaborate_enabled;
    }

    public static void setCollaborate_enabled(Boolean collaborate_enabled) {
        Settings.collaborate_enabled = collaborate_enabled;
    }

    @Override
    public String toString() {
        return "isCameraenabled" + isCamera_enabled() + "\n"
                + "isNotification enabled" + isNotification_enabled() + "\n"
                + "isCollaborateEnabled" + isCollaborate_enabled() + "\n"
                + "Rayon" + getRayonDetection() + "\n";
    }
}
