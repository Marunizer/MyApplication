package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import sadappp.myapplication.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Delete or tur to categories

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class DemoActivity extends ListActivity {

	List<RowItem> rowItems;
	ArrayList<String> menuList;
	String store;
	TextView title;

	GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
	Map<String, Object> objectHashMap;
	ArrayList<String> modelFiles;
	ArrayList<Object> objectArrayList;//HOLDS VALUES IN HASHMAP

	//ALL REQUIRED FOR DOWNLOADING FILES IN S3
	String BUCKET_NAME = "verysadbucket";
	FileOutputStream fos;


//	//used to load C++ library
//	//public native String  stringFromJNI();
//	static {
//		System.loadLibrary("native-lib");
//	}
//    //from native library
//	public native String  decoder(String dracoFile, String objFile);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);

		Intent intent = getIntent();

		menuList = (ArrayList<String>)intent.getSerializableExtra("MENU");
		store = intent.getStringExtra("STORE_NAME");
		title = (TextView) findViewById(R.id.title);
		title.setText("Menu Items");


		// add 1 entry per store found
		rowItems = new ArrayList<RowItem>();
		for (String model : menuList) {
			RowItem item = new RowItem(getFilesDir().getAbsolutePath(),model,"android.jpg");
			rowItems.add(item);
		}

		//TODO: Now that we have a list of the items, Probably need to sort in some kind of order
		//such as alphabetical, categories, yada yada, right now sorts in order

		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.activity_demo, rowItems);
		setListAdapter(adapter);
	}

	private void loadDemo(final String selectedItem) {
		Intent in = new Intent(DemoActivity.this.getApplicationContext(), ModelActivity.class);
		Bundle b = new Bundle();
		b.putString("assetDir", getFilesDir().getAbsolutePath());
		b.putString("assetFilename", selectedItem+".obj");//b.putString("assetFilename", selectedItem+".obj");
		b.putString("immersiveMode", "true");
		in.putExtras(b);
		DemoActivity.this.startActivity(in);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		final RowItem selectedItem = (RowItem) getListView().getItemAtPosition(position);

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference("Rests/" + store + "/Menu" + "/" + selectedItem.name + "/Key");
		System.out.println(myRef.toString());

			// Read from the database
			myRef.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					// This method is called once with the initial value and again
					// whenever data at this location is updated.

					//hashMap is made
					objectHashMap = dataSnapshot.getValue(objectsGTypeInd);
					//   objectArrayList = new ArrayList<Object>(objectHashMap.values());

					//DO NOT USE OR CRASH//String value = dataSnapshot.getValue(String.class);
					modelFiles = new ArrayList<String>();

					//for every key, go through and assign string to arraylist from hasmap
					for (String key: objectHashMap.keySet()) {
						modelFiles.add(key);
					}
					objectArrayList = new ArrayList<Object>(objectHashMap.values());

					new Thread(new Runnable() {
						public void run() {
							loadDemo(selectedItem.name);
//							downloadFileFromS3(store + "/Menu" + "/" + selectedItem.name + "/Key/" + String.valueOf(objectArrayList.get(0)),
//									String.valueOf(objectArrayList.get(0)));//.jpg  //DRC WITH DRC
//							Log.d(TAG, "this is obj/drc: "+ String.valueOf(objectArrayList.get(0)));
//
//							downloadFileFromS3(store + "/Menu" + "/" + selectedItem.name + "/Key/" + String.valueOf(objectArrayList.get(1)),
//									String.valueOf(objectArrayList.get(1)));//.obj //JPG WITH DRC
//							Log.d(TAG, "this is jpg for drc: "+ String.valueOf(objectArrayList.get(1)));
//
//							//using path (0) for drc
////							String path = String.valueOf(objectArrayList.get(0));
////							if (path.endsWith(".drc")) {
////								path = path.substring(0, path.length() - 3);
////								path = path + "obj";
////							}
////							Log.d(TAG, "this is the new obj: "+ path);
////
////							draco_decode(String.valueOf(objectArrayList.get(0)), path);
//
//							downloadFileFromS3(store + "/Menu" + "/" + selectedItem.name + "/Key/" + String.valueOf(objectArrayList.get(2)),
//									String.valueOf(objectArrayList.get(2)));//.mtl
//							Log.d(TAG, "this is mtl: "+ String.valueOf(objectArrayList.get(2)));
//							//APPERANTLY THIS IS MTL?????? ^^^^^^^^^^^
//							//WHEN DRACO FILE INVOLVED, THIS IS THE ORDER 0) drc, jpg, mtl
//
//							loadDemo(selectedItem.name);
						}
					}
					).start();
					//TODO: probably need to close connection to firebase before moving on to next activity??
				}
				@Override
				public void onCancelled(DatabaseError error) {
					// Failed to read value
					Log.w(TAG, "Failed to read value.", error.toException());
				}
			});

		// TODO: enable this when we have something to do with the dialog
		if (true)
			return;

		try {
			// custom dialog
			final Dialog dialog = new Dialog(DemoActivity.this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			dialog.setContentView(R.layout.dialog_load_model);

			TextView text = (TextView) dialog.findViewById(R.id.dialog_load_model_name);
			text.setText(selectedItem.name);
			TextView texture = (TextView) dialog.findViewById(R.id.dialog_load_model_texture);
			texture.setText("Not yet implemented");
			Button loadTextureButton = (Button) dialog.findViewById(R.id.browse_texture_button);
			// if button is clicked, close the custom dialog
			loadTextureButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			Button loadButton = (Button) dialog.findViewById(R.id.dialog_load_model_load);
			// if button is clicked, close the custom dialog
			loadButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					loadDemo(selectedItem.name);
				}

			});

			dialog.show();

		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

//
//	public void draco_decode(String dracoFile, String objFile) {
//		Log.d(TAG, "/data/user/0/sadappp.myapplication/files/" + dracoFile + "   /data/user/0/sadappp.myapplication/files/" + objFile);
//		decoder("/data/user/0/sadappp.myapplication/files/" + dracoFile, "/data/user/0/sadappp.myapplication/files/" + objFile);
//	}

	//  _   _ ___   _____                 _
	// | | | |_ _| | ____|_   _____ _ __ | |_ ___
	// | | | || |  |  _| \ \ / / _ \ '_ \| __/ __|
	// | |_| || |  | |___ \ V /  __/ | | | |_\__ \
	//  \___/|___| |_____| \_/ \___|_| |_|\__|___/
	//

}

class RowItem {
	/**
	 * Image of the 3D object (snapshot of what the model looks like)
	 * Instead might make this a some type of logo depicting food, soup, desert, or store logo
	 */
	String image;
	/**
	 * Logical name of the 3D model that the user selected
	 */
	String name;
	/**
	 * Assets path from where to build the .obj file
	 */
	String path;

	public RowItem(String path, String name, String image) {
		this.path = path;
		this.name = name;
		this.image = image;
	}
}

class CustomListViewAdapter extends ArrayAdapter<RowItem> {

	Context context;

	public CustomListViewAdapter(Context context, int resourceId, List<RowItem> items) {
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
		RowItem rowItem = getItem(position);

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