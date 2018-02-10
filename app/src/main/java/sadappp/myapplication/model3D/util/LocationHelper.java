package sadappp.myapplication.model3D.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;

/**
 * Created by mende on 10/30/2017.
 */

public final class LocationHelper {

    private static Location location;
    private static float latitude;
    private static float longitude;
    private static String address;
    private static String city;
    private static String state;
    private static String zipcode;
    private static int radius;
    private static boolean locationPermission = false;

    public static int getRadius() {
        return radius;
    }

    public static void setRadius(int radius) {
        LocationHelper.radius = radius;
    }

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

    public static void setZipcodeAndAll(String zipcode, Context context) throws IOException {
        LocationHelper.zipcode = zipcode;
        Location mLastLocation;

        //Maybe do all of this within the LocationHelper instead, just pass in the zip
        final Geocoder geocoder = new Geocoder(context);

            List<Address> addresses = geocoder.getFromLocationName(zipcode, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                mLastLocation = new Location(zipcode);
                mLastLocation.setLatitude((float) address.getLatitude());
                mLastLocation.setLongitude((float) address.getLongitude());

                setLocation(mLastLocation);
                setLongitude((float) address.getLongitude());
                setLatitude((float) address.getLatitude());
                setAddress(address.getAddressLine(0));
            }
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
