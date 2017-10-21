package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sadappp.myapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sadappp.myapplication.model3D.MainActivity;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by mende on 10/16/2017.
 */

public class StoreActivity extends ListActivity{

    ListView listView;
    List<RowStore> rowItems;
    Map<String, Object> objectHashMap;
    ArrayList<String> objectARests;
    ArrayList<Object> objectArrayList;
    String[] restsArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        Intent intent = getIntent();
        objectARests  = (ArrayList<String>)intent.getSerializableExtra("hashmap");
       // objectARests = new ArrayList<String>(objectHashMap.keySet());
     //   objectArrayList = new ArrayList<Object>(objectHashMap.values());

     //   restsArr = new String[objectARests.size()];
      //  restsArr = objectARests.toArray()(restsArr);
      //  String key = String.valueOf(objectArrayList.get(1));


      //  Log.d(TAG, "Key is " + objectHashMap.keySet() + " value for sadbois IN STORE ACTIVITY is " + String.valueOf(objectArrayList.get(1)));

        //WIll need once a system for store is set up
//        AssetManager assets = getApplicationContext().getAssets();
//        String[] models = null;
//        try {
//            models = assets.list("models");
//        } catch (IOException ex) {
//            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
//            return;
//        }

        // add 1 entry per store found
        rowItems = new ArrayList<RowStore>();
        for (String model : objectARests) {
                RowStore item = new RowStore(model,"android.jpg");
                rowItems.add(item);
        }

        RowStore item = new RowStore("Vietnomz", "android.jpg");
        rowItems.add(item);
        CustomStoreListViewAdapter adapter = new CustomStoreListViewAdapter(this, R.layout.activity_demo, rowItems);
        setListAdapter(adapter);
    }

    private void loadItems(final String selectedItem) {
        Intent intent = new Intent(StoreActivity.this.getApplicationContext(), DemoActivity.class);
        Bundle b = new Bundle();
//        b.putString("assetDir", "models");
//        b.putString("assetFilename", selectedItem);
//        b.putString("immersiveMode", "true");
//        intent.putExtras(b);
        StoreActivity.this.startActivity(intent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final RowStore selectedItem = (RowStore) getListView().getItemAtPosition(position);
        loadItems(selectedItem.name);

        // TODO: enable this when we have something to do with the dialog
        if (true)
            return;

        try {
            // custom dialog
            final Dialog dialog = new Dialog(StoreActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.setContentView(R.layout.dialog_load_model);

            TextView text = (TextView) dialog.findViewById(R.id.dialog_load_model_name);
            text.setText(selectedItem.name);
            TextView texture = (TextView) dialog.findViewById(R.id.dialog_load_model_texture);
            texture.setText("Not yet implemented");
            Button loadTextureButton = (Button) dialog.findViewById(R.id.browse_texture_button);
            // if button is clicked, close the custom dialog
            loadTextureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button loadButton = (Button) dialog.findViewById(R.id.dialog_load_model_load);
            // if button is clicked, close the custom dialog
            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    loadItems(selectedItem.name);
                }

            });

            dialog.show();

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}

class RowStore {
    /**
     * Image of the Store Logo (snapshot of what the model looks like)
     */
    String image;
    /**
     * Logical name of the Store that the user selected
     */
    String name;

//took out a parameter String path, will probably need later when there is a list of stores
    public RowStore(String name, String image) {
        this.name = name;
        this.image = image;
    }
}

class CustomStoreListViewAdapter extends ArrayAdapter<RowStore> {

    Context context;

    public CustomStoreListViewAdapter(Context context, int resourceId, List<RowStore> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /* private view holder class */
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowStore rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_demo_item, null);
            holder = new ViewHolder();
            // holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.demo_item_title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.demo_item_icon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtTitle.setText(rowItem.name);
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(rowItem.image));
            holder.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.ic_launcher2);
        }

        return convertView;
    }
}