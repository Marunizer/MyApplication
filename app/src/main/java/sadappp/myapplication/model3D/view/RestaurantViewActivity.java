package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.util.Restaurant;

/**
 * Created by mende on 12/16/2017.
 */

public class RestaurantViewActivity extends Activity {

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

        myOnClickListener = new MyOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        cards = new ArrayList<Restaurant>();
        for(int i = 0; i < sampleData.length; i++)
        {
            cards.add( new Restaurant(sampleData[i],"long", "lat"));
        }

        mAdapter = new MyAdapter(cards);
        mRecyclerView.setAdapter(mAdapter);

        //setContentView(R.layout.activity_demo);


//        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
//        mRecyclerView.setHasFixedSize(true);
//
//
//        // use a linear layout manager
//        mLayoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//
//
//        // specify an adapter (see also next example)
//        mAdapter = new MyAdapter(myDataSet);
//        mRecyclerView.setAdapter(mAdapter);
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


