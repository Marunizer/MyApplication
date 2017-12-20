package sadappp.myapplication.model3D.util;

import com.firebase.geofire.GeoLocation;

/**
 * Created by mende on 12/13/2017.
 * Influenced by Flynn
 */

public class Restaurant {

    private String name;
    private String latitude;
    private String longitude;
    private String coordinateKey;
    private GeoLocation geoLocation;
    private String distanceAway; //to be used when finally calculating distance from user

    public Restaurant(String name, String latitude, String longitude ,String coordinateKey, GeoLocation geoLocation) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.coordinateKey  = coordinateKey;
        this.geoLocation = geoLocation;
    }

    public String getName() {
        return name;
    }

    public GeoLocation getGeoLocation(){
        return geoLocation;
    }

    public String getCoordinateKey() {
        return coordinateKey;
    }
}
