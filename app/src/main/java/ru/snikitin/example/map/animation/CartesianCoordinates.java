package ru.snikitin.example.map.animation;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by snikitin on 20.10.2016.
 */
public class CartesianCoordinates {
    private static final int R = 6371; // approximate radius of earth

    public double x;
    public double y;
    public double z;

    public CartesianCoordinates(LatLng p) {
        this(p.latitude, p.longitude);
    }

    public CartesianCoordinates(double lat, double lon) {
        double _lat = Math.toRadians(lat);
        double _lon = Math.toRadians(lon);

        x = R * Math.cos(_lat) * Math.cos(_lon);
        y = R * Math.cos(_lat) * Math.sin(_lon);
        z = R * Math.sin(_lat);
    }

    public static LatLng toLatLng(double x, double y, double z) {
        return new LatLng(Math.toDegrees(Math.asin(z / R)), Math.toDegrees(Math.atan2(y, x)));
    }
}