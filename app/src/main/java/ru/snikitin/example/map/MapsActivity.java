package ru.snikitin.example.map;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Property;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import ru.snikitin.example.map.animation.CartesianCoordinates;
import ru.snikitin.example.map.animation.LatLngInterpolator;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    public static final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();
    public static final TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            return latLngInterpolator.interpolate(fraction, startValue, endValue);
        }
    };
    public static final Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");

    public static final long DURATION = 5000;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng startPosition = new LatLng(10, 0);
        LatLng endPosition = new LatLng(10, 80);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));

        // mMap.addMarker(new MarkerOptions().position(startPosition));
        mMap.addPolyline(new PolylineOptions()
                .add(bezier(startPosition, endPosition, 20, 0, true))
                .width(1)
                .color(Color.RED).geodesic(true));


        LatLng[] line = bezier(startPosition, endPosition, 20, 0, true);
        Marker marker = mMap.addMarker(new MarkerOptions().position(startPosition));
        animateMarker(marker, 0, line);
    }

    private static void animateMarker(final Marker marker, final int current, final LatLng[] line) {
        if (line == null || line.length == 0 || current >= line.length) {
            return;
        }

        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, line[current]);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animateMarker(marker, current + 1, line);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.setDuration(DURATION);
        animator.start();
    }

    private static LatLng[] bezier(LatLng p1, LatLng p2, double arcHeight, double skew, boolean up) {
        ArrayList<LatLng> list = new ArrayList<>();
        try {
            if (p1.longitude > p2.longitude) {
                LatLng tmp = p1;
                p1 = p2;
                p2 = tmp;
            }

            LatLng c = new LatLng((p1.latitude + p2.latitude) / 2, (p1.longitude + p2.longitude) / 2);

            double cLat = c.latitude;
            double cLon = c.longitude;

            //add skew and arcHeight to move the midPoint
            if (Math.abs(p1.longitude - p2.longitude) < 0.0001) {
                if (up) {
                    cLon -= arcHeight;
                } else {
                    cLon += arcHeight;
                    cLat += skew;
                }
            } else {
                if (up) {
                    cLat += arcHeight;
                } else {
                    cLat -= arcHeight;
                    cLon += skew;
                }
            }

            list.add(p1);
            //calculating points for bezier
            double tDelta = 1.0 / 10;
            CartesianCoordinates cart1 = new CartesianCoordinates(p1);
            CartesianCoordinates cart2 = new CartesianCoordinates(p2);
            CartesianCoordinates cart3 = new CartesianCoordinates(cLat, cLon);

            for (double t = 0; t <= 1.0; t += tDelta) {
                double oneMinusT = (1.0 - t);
                double t2 = Math.pow(t, 2);

                double y = oneMinusT * oneMinusT * cart1.y + 2 * t * oneMinusT * cart3.y + t2 * cart2.y;
                double x = oneMinusT * oneMinusT * cart1.x + 2 * t * oneMinusT * cart3.x + t2 * cart2.x;
                double z = oneMinusT * oneMinusT * cart1.z + 2 * t * oneMinusT * cart3.z + t2 * cart2.z;
                LatLng control = CartesianCoordinates.toLatLng(x, y, z);
                list.add(control);
            }

            list.add(p2);
        } catch (Exception e) {
            Log.e(TAG, "bezier error : ", e);
        }

        LatLng[] result = new LatLng[list.size()];
        result = list.toArray(result);

        return result;
    }
}


