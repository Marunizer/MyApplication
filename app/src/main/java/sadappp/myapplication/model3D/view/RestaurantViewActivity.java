package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.util.Restaurant;
import sadappp.myapplication.util.Utils;

import static android.content.ContentValues.TAG;

/**
 * Created by mende on 12/16/2017.
 */

public class RestaurantViewActivity extends Activity {

    public ArrayList<Restaurant> restaurant;

    String valueToPass;

    private Location mLastLocation;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.OnClickListener myOnClickListener;
    private ArrayList cards;
    String[] sampleData = {"Bento", "mickyD", "sadbois", "Chipotle" , "Flippers", "Vietnomz"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        //Grab Location received from start of app
        Intent intent = getIntent();
        this.mLastLocation = (Location) intent.getSerializableExtra("LOCATION");
       // prepareRestaurantArray();

        myOnClickListener = new MyOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //FOR TESTING PURPOSES, WILL BE REMOVED AFTER CONNECTION TO FIREBASE IS FINISHED
        cards = new ArrayList<Restaurant>();
        for(int i = 0; i < sampleData.length; i++)
        {
            cards.add( new Restaurant(sampleData[i],"long", "lat", null));
        }

        mAdapter = new MyAdapter(cards);
        mRecyclerView.setAdapter(mAdapter);
    }



    //  Access Firebase
    //  Access GeoFire
    //  Prepare List to display
    void prepareRestaurantArray() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final String restaurantNameKey = "restaurant_name";
        final String restaurantLatKey = "lat";
        final String restaurantLongKey = "long";


        myRef.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                GeoFire geoFire;
                geoFire = new GeoFire(myRef.child("geofire_restaurants"));

                //Dynamic and will actually be used
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                double radius = .6;

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

                        //if for loops through location and does not return null
                        //if () if restaurant is found with the right requirements for latitude and longitude
                        //for loop  to all locationKeys
                        for (DataSnapshot item: dataSnapshot.getChildren()) {
                            if (item.getKey().compareTo(locationKey) >= 0)
                            {
                                String name = myRef.child("restaurants/" + locationKey + "/" + restaurantNameKey).getKey();
                                String item_lat = myRef.child("restaurants/" + locationKey + "/" + restaurantLatKey).getKey();
                                String item_long = myRef.child("restaurants/" + locationKey + "/" + restaurantLongKey).getKey();

                                restaurant.add(new Restaurant(name, item_lat, item_long, location));
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

        holder.restName.setText(rest.getName());
       // holder.restName.setText(mDataset.get(position).get);
        holder.restImage.setImageResource(R.drawable.sample_food);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}


