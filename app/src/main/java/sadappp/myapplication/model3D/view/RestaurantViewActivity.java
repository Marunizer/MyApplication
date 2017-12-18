package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.util.ArraySet;
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

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class RestaurantViewActivity extends Activity {

     private ArrayList<Restaurant> restaurant = new ArrayList<Restaurant>();
     private ArrayList<GeoLocation> restaurantGeoChecker = new ArrayList<GeoLocation>();
     ArraySet<GeoLocation> restaurants = new ArraySet<GeoLocation>();

    String valueToPass;

    private Location mLastLocation;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.OnClickListener myOnClickListener;
    private ArrayList cards;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        //Grab Location received from start of app
        Intent intent = getIntent();
        this.mLastLocation = (Location) intent.getSerializableExtra("LOCATION");
        prepareRestaurantArray();

        myOnClickListener = new MyOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //reloadData();
    }


    //Access AWS S3
    //Download image from Bucket
    void downloadImageFromAWS(String imageKey)
    {
        AmazonS3Helper s3Helper = new AmazonS3Helper();
        imageKey = imageKey + "_main_image.png";
        String path = getFilesDir().toString() + "/" + imageKey;

        //will get folder data/data/packagename/file
        File files_folder = new File(path);

        s3Helper.download(this.getApplicationContext(), imageKey, files_folder);
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
                                    //RELOADS LIST
                                    reloadData();
                                }
                            }
                        }
                        System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                    }

                    @Override
                    public void onKeyExited(String key) {
                        System.out.println(String.format("Key %s is no longer in the search area", key));
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
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
        mAdapter = new MyAdapter(this.restaurant);
        mRecyclerView.setAdapter(mAdapter);
    }
}



class MyOnClickListener implements View.OnClickListener {
    public MyOnClickListener(RestaurantViewActivity restaurantViewActivity) {
    }

    @Override
    public void onClick(View view) {

    }
}



class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private ArrayList mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cv;
        public TextView restName;
        public ImageView restImage;

        public ViewHolder(View itemView){
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.restaurant_card);
            restName = (TextView)itemView.findViewById(R.id.restaurant_name);
            restImage = (ImageView)itemView.findViewById(R.id.restaurant_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList myDataset) {
        mDataset = myDataset;
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

        Restaurant rest = (Restaurant) mDataset.get(position);
        String path = holder.restImage.getContext().getFilesDir().toString() + "/" + rest.getName() + "_main_image.png";
        File imgFile = new File(path);
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        holder.restName.setText(rest.getName());
       // holder.restName.setText(mDataset.get(position).get);

       // holder.restImage.setImageResource(R.drawable.sample_food);
        holder.restImage.setImageBitmap(bitmap);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}


