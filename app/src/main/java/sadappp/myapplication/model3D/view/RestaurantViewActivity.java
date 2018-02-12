package sadappp.myapplication.model3D.view;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.support.v4.app.DialogFragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.util.AmazonS3Helper;
import sadappp.myapplication.model3D.util.LocationHelper;
import sadappp.myapplication.model3D.util.Restaurant;
import sadappp.myapplication.util.Utils;

import static android.content.ContentValues.TAG;

/**
 * Created by mende on 12/16/2017.
 */

/**
 * In this Activity, the list of available restaurants nea the user is displayed
 * TODO List:
 *     * (IGNORE FOR NOW) When checking if the restaurant found is already within the ArrayList, check based on
 *      2 parameters (name AND location) instead of just one. (Just in case for the future, multiple restaurants with same geoLocation)
 *
 *     * When a default picture was added to the imageView inside card_view_restaurant,
 *      it seems to delay the process of showing items, and does it all at once instead.
 *          - Replace default image with a very small default picture unlike the one currently there

 *     * Implement a reload when user swipes all the way down from the top of the screen to update the list
 *     X I think Nick has done this, Have not actually tested in real time environment.
 *            Would be nice if the swipe up moved the entire screen down instead of having a circle come out of nowhere
 *
 *     * Not sure where this will be implemented. But maybe onDestroy(); there should be some call to a method to remove
 *           all the png's downloaded to relieve storage. Will be EXTREMELY necessary in the future.
 *           Probably change location of where png's are added to, into it's own folder
 *           then delete contents of folder
 *           UPDATE: Have made a delete function, just needs to be called
 */

public class RestaurantViewActivity extends AppCompatActivity implements MyAdapter.AdapterCallback {

    private ArrayList<Restaurant> restaurant = new ArrayList<Restaurant>();
    private ArrayList<GeoLocation> restaurantGeoChecker = new ArrayList<GeoLocation>();
    AmazonS3Helper s3Helper;
    private static TransferObserver observer; //make want to make local where called
    private RecyclerView mRecyclerView;

    //may want to make local where called
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager; //may want to make local where called
    private SwipeRefreshLayout mySwipeRefreshLayout;
    Toolbar toolbar;
    TextView textView;
    double radius;// = .6; //should default be something
    public static final int DIALOG_FRAGMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        toolbar= (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        setContentView(R.layout.activity_demo);
        textView = (TextView) findViewById(R.id.address_text);
        textView.setText(LocationHelper.getAddress());
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        swipeUpdate();
                    }
                }
        );

        prepareRestaurantArray();

        //initial loading data here seems to lessen the time it takes to make everything show up.
        reloadData();
    }

    public void swipeUpdate(){
        //Toast to notify a refresh has started, calls the prepareRestaurantArray() method again
        Toast.makeText(getApplicationContext(),"Refreshing",Toast.LENGTH_SHORT).show();
        prepareRestaurantArray();
    }

    //Communicates with Adapter
    @Override
    public void onMethodCallback(String key) {
        onRestaurantClick(key);
    }

    //Access AWS S3
    //Download image from Bucket
    /*void downloadImageFromAWS(String imageKey)
    {
        imageKey = imageKey + "_main_image.png";
        String path = getFilesDir().toString() + "/card/" + imageKey;

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
                    System.out.println("Progress of download is at : " + percentage + "%" );
                }

                @Override
                public void onError(int id, Exception ex) {
                    // do something
                }
            });
        }
    }*/

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
        restaurant.clear();
        restaurantGeoChecker.clear();

        myRef.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                GeoFire geoFire;
                geoFire = new GeoFire(myRef.child("geofire_restaurants"));

                //Dynamic and will actually be used - Crashes, mLastLocation seems to be null :(
                double latitude = LocationHelper.getLatitude();
                double longitude  = LocationHelper.getLongitude();
                //TODO: make radius a variable that can be changed by the user, default wll be 10 miles

                //hardcoded values for testing purposes
                latitude = 28.546373;
                longitude = -81.162192;
                //radius = LocationHelper.getRadius();
                radius = 10;
                LocationHelper.setRadius((int) radius);

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
                                Location rest_location = new Location(String.valueOf(location));
                                rest_location.setLongitude(Double.parseDouble(item_long));
                                rest_location.setLatitude(Double.parseDouble(item_lat));

                                //Checks if we already have this restaurant in our list
                                if(!restaurantGeoChecker.contains(location)){

                                    restaurant.add(new Restaurant(name, rest_location, item.getKey()));
                                    restaurantGeoChecker.add(location);
                                    System.out.println("RestaurantViewActivity: ADDING NEW RESTAURANT : " + name + ", " + item_lat + ", " + item_long + "  item.getKey() = " + item.getKey() + " location = " + location);

                                    //While we're at it, lets download the image linked with the restaurant
                                    //downloadImageFromAWS(name);
                                    setRestaurant(restaurant);
                                }
                            }
                        }
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
                        //stackoverflow.com/questions/4066538/sort-an-arraylist-based-on-an-object-field
                        Collections.sort(restaurant, new Comparator<Restaurant>(){
                            public int compare(Restaurant o1, Restaurant o2){
                                if(o1.getDistanceAway() == o2.getDistanceAway())
                                    return 0;
                                return o1.getDistanceAway() < o2.getDistanceAway() ? -1 : 1;
                            }
                        });
                        reloadData();
                        //This is where the list should be loaded and where I should sort my restaurants
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

    //Set new list of restaurants
    void setRestaurant(ArrayList restaurant)
    {
        this.restaurant = restaurant;
    }
    //set restaurant clicked key

    void reloadData()
    {
        textView.setText(LocationHelper.getAddress());

        final Context context = this;
        if (mySwipeRefreshLayout.isRefreshing())
        {
            mySwipeRefreshLayout.setRefreshing(false);
        }
        mAdapter = new MyAdapter(restaurant, context);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onRestaurantClick(String key){

        Intent intent = new Intent(RestaurantViewActivity.this.getApplicationContext(), ModelActivity.class);
        Bundle b = new Bundle();
        b.putString("assetDir", getFilesDir().getAbsolutePath());
        b.putString("modelLocation", "small");
        b.putString("coordinateKey", key);
        b.putString("immersiveMode", "true");
        intent.putExtras(b);
        RestaurantViewActivity.this.startActivity(intent);
    }

    @SuppressLint("WrongConstant")
    public void changeAddress(View view) {

        FragmentManager fragmentManager = getFragmentManager();
        LocationDialogFragment locationDialogFragment =  new LocationDialogFragment();

        android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(android.R.id.content,locationDialogFragment)
                .addToBackStack(null).commit();
    }

    public void deleteFiles() throws IOException {
        File file = new File(getFilesDir().toString() + "/card");
        FileUtils.deleteDirectory(file);
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private AdapterCallback adapterCallback;
    private Context context;
    private ArrayList mDataset;
    private StorageReference fbStorageReference = FirebaseStorage.getInstance().getReference();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
     static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
         CardView cv;
         TextView restName;
         TextView restDistance;
         ImageView restImage;
         String coordinateKey = " ";

         ViewHolder(final View itemView){
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
        try {
            this.adapterCallback = ((AdapterCallback) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
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
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //String path = context.getFilesDir().toString() + "/card/" + ((Restaurant) mDataset.get(position)).getName() + "_main_image.png";
        //File imgFile = new File(path);

        //Get the path to the file from the dataset
        String path = ((Restaurant) mDataset.get(position)).getName() + "_main_image.png";
        //Create a StorageRefrence variable to store the path to the image
        StorageReference image = fbStorageReference.child(path);
        //Serve this path to Glide which is put into the image holder and cached for us
        GlideApp.with(context)
                .load(image)
                .override(600,600)
                .into(holder.restImage);

        //Picasso.with(context).load(imgFile).into(holder.restImage);
        holder.restName.setText(((Restaurant) mDataset.get(position)).getName());

        float milesAway = metersToMiles(((Restaurant) mDataset.get(position)).getDistanceAway());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        milesAway = Float.valueOf(decimalFormat.format(milesAway));

        if (milesAway < 1)
        {
            holder.restDistance.setText("less than a mile away");
        }
        else if (milesAway == 1)
        {
            holder.restDistance.setText(String.valueOf(milesAway) + " mile away");
        }
        else
        {
            holder.restDistance.setText(String.valueOf(milesAway) + " miles away");
        }

        holder.coordinateKey = (((Restaurant) mDataset.get(position)).getCoordinateKey());

        holder.cv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                adapterCallback.onMethodCallback(holder.coordinateKey);
            }}
        );
    }

    public float metersToMiles(float meters)
    {
        return (float) (meters*0.000621371192);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

  //  public String getKey(){return this.coordinateKey;}

    public interface AdapterCallback {
        void onMethodCallback(String key);
    }
}