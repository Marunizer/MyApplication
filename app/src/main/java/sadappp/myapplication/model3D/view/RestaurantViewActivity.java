package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.util.AmazonS3Helper;
import sadappp.myapplication.model3D.util.Restaurant;
import sadappp.myapplication.util.Utils;

import static android.content.ContentValues.TAG;

/**
 * Created by mende on 12/16/2017.
 */

/**
 * In this Activity, the list of available restaurants nea the user is displayed
 * TODO List:
 *     * Make multiple GeoLocation searches, having a small radius(km) to a larger radius(km) each time
 *              This will update the list of restaurant's in order of what is closest
 *              - For now a simple for loop when making GeoLocation calls, incrementing the radius each time will do.
 *
 *      * Calculate and show how far away the user is from the restaurant like (less than 1 mile away), (1 mile away), .. (n miles away)
 *
 *     * When checking if the restaurant found is already within the ArrayList, check based on
 *      2 parameters (name AND location) instead of just one. (Just in case for the future, multiple restaurants with same geoLocation)
 *
 *     * When a default picture was added to the imageView inside card_view_restaurant,
 *      it seems to delay the process of showing items, and does it all at once instead.
 *          - Replace default image with a very small default picture unlike the one currently there
 *
 *     * After all that, make sure the recycler view is properly displaying everything dynamically
 *
 *     * Implement a reload when user swipes all the way down from the top of the screen to update the list
 *
 *     * Have the same functionality of finding geographical coordinates currently in mainActivity for the reload on swipe
 *          - This probably means there should be a Class for finding your current coordinates to avoid a ton of repeat code.
 *
 *     * Not sure where this will be implemented. But maybe onDestroy(); there should be some call to a method to remove
 *           all the png's downloaded to relieve storage. Will be EXTREMELY necessary in the future.
 */

public class RestaurantViewActivity extends Activity {

    private ArrayList<Restaurant> restaurant = new ArrayList<Restaurant>();
    private ArrayList<GeoLocation> restaurantGeoChecker = new ArrayList<GeoLocation>();
    AmazonS3Helper s3Helper;
    private static TransferObserver observer; //make want to make local where called

    private Location mLastLocation;  //only not referenced because currently there is a test location
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter; //may want to make local where called
    private RecyclerView.LayoutManager mLayoutManager; //may want to make local where called
    private RecyclerView.OnClickListener myOnClickListener; //need this reference even though not used.

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        //Grab Location received from start of app
        Intent intent = getIntent();
        this.mLastLocation = (Location) intent.getSerializableExtra("LOCATION");

        myOnClickListener = new MyOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        prepareRestaurantArray();

        //reloadData();
    }


    //Access AWS S3
    //Download image from Bucket
    void downloadImageFromAWS(String imageKey)
    {
        imageKey = imageKey + "_main_image.png";
        String path = getFilesDir().toString() + "/" + imageKey;

        //will get folder data/data/packagename/file
        File files_folder = new File(path);

        if(!files_folder.exists())
        {
            s3Helper = new AmazonS3Helper();
            s3Helper.initiate(this.getApplicationContext());
            observer = s3Helper.getTransferUtility().download(s3Helper.getBucketName(), imageKey,files_folder);

            observer.setTransferListener(new TransferListener(){

                @Override
                public void onStateChanged(int id, TransferState state) {
                    System.out.println("THIS IS OUR STATE : " + state + " or : " + state.toString() + " TRANSFER UTILITY");
                    if (state.toString().compareTo("COMPLETED") == 0 )
                    {
                        reloadData();
                        System.out.println(state.toString() + " Completed?  " + state.toString().compareTo("COMPLETED"));
                    }

                }

                //Keeps track of progress of download but isn't really needed. knowing when the state changes to complete is good enough
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int percentage = 0;
                    if (bytesTotal > 0) {
                        percentage = (int) (bytesCurrent / bytesTotal * 100);
                    }
                    System.out.println("YO THIS DOWNLOAD AT *** : " + percentage + "%" );
                }

                @Override
                public void onError(int id, Exception ex) {
                    // do something
                }

            });
        }
    }


    //  Access Firebase
    //  Access GeoFire
    //  Prepare List to display
    void prepareRestaurantArray()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final String NAME_KEY = "restaurant_name";
        final String LAT_KEY = "lat";
        final String LONG_KEY = "long";

        myRef.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                GeoFire geoFire;
                geoFire = new GeoFire(myRef.child("geofire_restaurants"));

                //Dynamic and will actually be used - Crashes, mLastLocation seems to be null :(
                double latitude;// = mLastLocation.getLatitude();
                double longitude;// = mLastLocation.getLongitude();
                double radius;// = .6;

                //hardcoded values for testing purposes
                latitude = 28.546373;
                longitude = -81.162192;
                radius = 5000;

                //A GeoFire GeoQuery takes in the latitude, longitude, and finally the radius based on kilometers.
                //Probably want to make multiple queries incrementing the radius based on some calculation
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude,longitude), radius);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {

                        String myLat = String.format(java.util.Locale.US,"%.6f", location.latitude);
                        String myLong =  String.format(java.util.Locale.US,"%.6f", location.longitude);
                        String locationKey = myLat + myLong;
                        locationKey = Utils.cleanLatLongKey(locationKey);

                        System.out.println("Looking for Location Key : " + locationKey);

                        for (DataSnapshot item: dataSnapshot.getChildren()) {
                            if (item.getKey().compareTo(locationKey) >= 0)
                            {
                                String name = item.child(NAME_KEY).getValue().toString();
                                String item_lat = item.child(LAT_KEY).getValue().toString();
                                String item_long = item.child(LONG_KEY).getValue().toString();

                                //Checks if we already have this restaurant in our list
                                if(!restaurantGeoChecker.contains(location)){

                                    restaurant.add(new Restaurant(name, item_lat, item_long, location));
                                    restaurantGeoChecker.add(location);
                                    System.out.println("!!!!!!!ADDING NEW RESTAURANT : " + name + ", " + item_lat + ", " + item_long + "  item.getKey() = " + item.getKey());

                                    //While we're at it, lets download the image linked with the restaurant
                                    downloadImageFromAWS(name);

                                    setRestaurant(restaurant);
                                }
                            }
                        }
                        //RELOADS LIST now that the arrayList has been set a million times
                        reloadData();
                     //   System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                    }

                    @Override
                    public void onKeyExited(String key) {
                 //       System.out.println(String.format("Key %s is no longer in the search area", key));
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                 //       System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                    }

                    @Override
                    public void onGeoQueryReady() {
                        System.out.println("All initial data has been loaded and events have been fired!");
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        System.err.println("There was an error with this query: " + error);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    //Set new restaurant
    void setRestaurant(ArrayList restaurant)
    {
        this.restaurant = restaurant;
    }

    void reloadData()
    {
        mAdapter = new MyAdapter(this.restaurant, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onRestaurantClick(View view){

        //Although here is an arrayList with the restaurants, probably don't want to send.
        //Instead what should be sent is the geolocation Key for firebase access to the restaurants menu.
        // private ArrayList<Restaurant> restaurant = new ArrayList<Restaurant>();
        //


        Intent intent = new Intent(RestaurantViewActivity.this.getApplicationContext(), ModelActivity.class);
        Bundle b = new Bundle();
        b.putString("assetDir", getFilesDir().getAbsolutePath());
        b.putString("modelLocation", "small");
       // b.putString("assetFilename", selectedItem+".obj");//b.putString("assetFilename", selectedItem+".obj");
        b.putString("immersiveMode", "true");
        intent.putExtras(b);
        RestaurantViewActivity.this.startActivity(intent);

    }

}


//This is NOT being used at all,
//the onClick method is actually routed from the XML
class MyOnClickListener implements View.OnClickListener {
    public MyOnClickListener(RestaurantViewActivity restaurantViewActivity) {
    }

    @Override
    public void onClick(View view) {

    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private Context context;
    private ArrayList mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
     static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
         CardView cv;
         TextView restName;
         TextView restDistance;
         ImageView restImage;

         ViewHolder(View itemView){
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.restaurant_card);
            restName = (TextView)itemView.findViewById(R.id.restaurant_name);
            restDistance = (TextView)itemView.findViewById(R.id.restaurant_distance);

            restImage = (ImageView)itemView.findViewById(R.id.restaurant_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MyAdapter(ArrayList myDataset, Context context) {
        this.mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_restaurant, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        String path = context.getFilesDir().toString() + "/" + ((Restaurant) mDataset.get(position)).getName() + "_main_image.png";

        File imgFile = new File(path);

        Picasso.with(context).load(imgFile).into(holder.restImage);
        holder.restName.setText(((Restaurant) mDataset.get(position)).getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}


