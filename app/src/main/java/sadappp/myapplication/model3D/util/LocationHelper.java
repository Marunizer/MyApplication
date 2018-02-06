package sadappp.myapplication.model3D.util;

import android.location.Location;

/**
 * Created by mende on 10/30/2017.
 */

public class LocationHelper {

    private static Location location;
    private static float latitude;
    private static float longitude;
    private static String address;
    private static String city;
    private static String state;
    private static String zipcode;
    private static boolean locationPermission = false;

    public static boolean isLocationPermission() {
        return locationPermission;
    }

    public static void setLocationPermission(boolean locationPermission) {
        LocationHelper.locationPermission = locationPermission;
    }

    public static String getZipcode() {
        return zipcode;
    }

    public static void setZipcode(String zipcode) {
        LocationHelper.zipcode = zipcode;
    }

    public static float getLatitude() {
        return latitude;
    }

    public static void setLatitude(float latitude) {
        LocationHelper.latitude = latitude;
    }

    public static float getLongitude() {
        return longitude;
    }

    public static void setLongitude(float longitude) {
        LocationHelper.longitude = longitude;
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        LocationHelper.address = address;
    }

    public static String getCity() {
        return city;
    }

    public static void setCity(String city) {
        LocationHelper.city = city;
    }

    public static String getState() {
        return state;
    }

    public static void setState(String state) {
        LocationHelper.state = state;
    }

    public static Location getLocation() {
        return location;
    }

    public static void setLocation(Location location) {
        LocationHelper.location = location;
    }
}
