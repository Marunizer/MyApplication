package sadappp.myapplication.model3D.util;

import com.firebase.geofire.GeoLocation;

/**
 * Created by mende on 12/13/2017.
 * Heavily influenced by Flynn
 */

public class Restaurant {

    String name;
    String latitude;
    String longitude;
    GeoLocation geoLocation;

    public Restaurant(String name, String longitude, String latitude, GeoLocation geoLocation) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geoLocation = geoLocation;
    }

}
