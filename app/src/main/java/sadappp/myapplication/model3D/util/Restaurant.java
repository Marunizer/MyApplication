package sadappp.myapplication.model3D.util;

import android.location.Location;

import com.firebase.geofire.GeoLocation;

/**
 * Created by mende on 12/13/2017.
 * Influenced by Flynn
 */

public class Restaurant {

    private String name;
    private String coordinateKey;
    private float distanceAway; //to be used when finally calculating distance from user

    public Restaurant(String name,Location location,String coordinateKey) {
        this.name = name;
        this.coordinateKey  = coordinateKey;
        this.distanceAway = location.distanceTo(LocationHelper.getLocation());
    }

    public String getName() {
        return name;
    }

    public float getDistanceAway() {
        return distanceAway;
    }

    public String getCoordinateKey() {
        return coordinateKey;
    }
}
