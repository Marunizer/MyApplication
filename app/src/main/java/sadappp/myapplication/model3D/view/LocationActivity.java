package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.util.LocationHelper;

/**
 * Created by mauricio mendez on 10/20/2017.
 */

public class LocationActivity  extends Activity{

    TextView queryText;
    EditText zipcodeText;
    Button enterQuery;
    Location mLastLocation;
    String zip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);
        queryText = (TextView) findViewById(R.id.zipcode_text);
        zipcodeText = (EditText) findViewById(R.id.add_zip);
        enterQuery = (Button) findViewById(R.id.zipcode_button);

        enterQuery.setOnClickListener(btnCheckDownloadLocationOnClickListener);
    }

    View.OnClickListener btnCheckDownloadLocationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            zip = zipcodeText.getText().toString();
            if (zip.length() == 0)
                Toast.makeText(LocationActivity.this, "Cannot leave zip code empty", Toast.LENGTH_SHORT).show();
            else if(zip.length() != 5)
                Toast.makeText(LocationActivity.this, "Please enter a full zip code", Toast.LENGTH_LONG).show();
            else
            {
                final Geocoder geocoder = new Geocoder(LocationActivity.this);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(zip, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
//                        // Use the address as needed

                        mLastLocation = new Location(zip);
                        mLastLocation.setLatitude((float) address.getLatitude());
                        mLastLocation.setLongitude((float)address.getLongitude());

                        LocationHelper.setLocation(mLastLocation);
                        LocationHelper.setLongitude((float)address.getLongitude());
                        LocationHelper.setLatitude((float)address.getLatitude());
                        LocationHelper.setAddress(address.getAddressLine(0));
                        LocationHelper.setZipcode(zip);
                        LocationHelper.setLocationPermission(false);

                        //If true
                        Intent intent = new Intent(LocationActivity.this.getApplicationContext(), RestaurantViewActivity.class);
                        LocationActivity.this.startActivity(intent);


                    } else {
                        // Display appropriate message when Geocoder services are not available
                        Toast.makeText(LocationActivity.this, "Unable to geocode zipcode, Please allow the noni to access location", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    // handle exception
                }
            }
        }
    };
}
