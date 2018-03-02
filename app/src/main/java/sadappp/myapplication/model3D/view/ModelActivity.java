package sadappp.myapplication.model3D.view;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import sadappp.myapplication.model3D.util.Menu;
import sadappp.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.joooonho.SelectableRoundedImageView;

import org.apache.commons.io.FileUtils;

/**
 * This activity represents the container for our 3D viewer.
 * TODO List:
 *
 * 		X* first, by using the known location key of the restaurant picked, access firebase and in order (0-1) (gross, should be changed..)
 * 	          Make an ArrayList that holds the names of the restaurant menu.
 *
 * 	    * Implement latest UI design, floating circle back button on top left, Name of item on top right with a clickable text for details for later
 *
 * 	    * 3d Model Viewer, if zooming in and out, do not allow user to rotate the screen !
 * 	                       When user has 2 fingers not zooming on the screen, allow user to move camera position? maybe not
 *
 * 	    * After the final 3d model production is decided, will need to change how xyz-axis are disaplyed so model is shown from the front.
 *
 * 	    * Make a onCreateOptionsMenu with a back button  at top left corner of screen
 * 	    * Add item name and description option and have a textView float for at least the name
 */
public class ModelActivity extends FragmentActivity implements MyCircleAdapter.AdapterCallback{

	private String paramAssetDir;
	private String paramAssetFilename;
	private String coordinateKey;
	Menu menu;
	TextView foodTitle;
	TextView moneyNumber;
	private int menuIndex = 0;
	/**
	 * The file to load. Passed as input parameter
	 */
	private String paramFilename;
	/**
	 * Enter into Android Immersive mode so the renderer is full screen or not
	 */
	private boolean immersiveMode = true;
	/**
	 * Background GL clear color. Default is light gray
	 */
	private float[] backgroundColor = new float[]{0.2f, 0.2f, 0.2f, 1.0f};

	private Handler handler;

	private int testingNumber = 0;
	private int downloadCheck = 0;

	private RecyclerView mRecyclerView;

	private MyCircleAdapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager; //may want to make local where called

	private static final String CONTENT_VIEW_TAG = "MODEL_FRAG";
	private FragmentManager fragMgr;
	private ModelFragment modelFragment;
	private ARModelFragment arModelFragment;
	private boolean viewFlag = false; //If viewFlag = false -> 3D viewer (default)|| If viewFlag = true -> AR viewer

	private StorageReference fbStorageReference = FirebaseStorage.getInstance().getReference();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();

		if (b != null) {
			this.paramAssetDir = b.getString("assetDir");//the directory where the files are stored perfectly
			this.coordinateKey = b.getString("coordinateKey");
			this.paramAssetFilename = b.getString("assetFilename");//NULL should remove everywhere
			this.paramFilename = b.getString("uri");//the important one
			this.immersiveMode = "true".equalsIgnoreCase(b.getString("immersiveMode"));
			try{
				String[] backgroundColors = b.getString("backgroundColor").split(" ");
				backgroundColor[0] = Float.parseFloat(backgroundColors[0]);
				backgroundColor[1] = Float.parseFloat(backgroundColors[1]);
				backgroundColor[2] = Float.parseFloat(backgroundColors[2]);
				backgroundColor[3] = Float.parseFloat(backgroundColors[3]);
			}catch(Exception ex){
				// Assuming default background color
			}
		}

		//Initiate the Menu class to be used that will hold all the menu items
		menu = new Menu(this.coordinateKey);

		prepareMenuArray();
		handler = new Handler(getMainLooper());

		setContentView(R.layout.activity_model);
		foodTitle = findViewById(R.id.title_text);
		moneyNumber = findViewById(R.id.moneySign);
	}

	private void prepareMenuArray() {

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		final DatabaseReference myRef = database.getReference();

		myRef.child("menus/" + this.coordinateKey + "/items").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(final DataSnapshot dataSnapshot) {

				for (DataSnapshot item: dataSnapshot.getChildren()) {

					//for each item  in this menu, add it to the the menu class as a menuItem and put it in an arrayList
					//retrieve both the key and the value
					//With this line I believe I gain the file name and the basic filename for all needed and an ID
					menu.allItems.add(new Menu.MenuItem(item.getKey(), item.getValue().toString()));
					System.out.println("ADDING NEW item : " + item.getValue().toString() + " to the Menu at coordinate: " + coordinateKey);

					//download ?
					if(item.getKey().compareTo("0") == 0)
					{
						firstAccess();
						foodTitle.setText(menu.allItems.get(menuIndex).getName());
						//first item in the list, so should immediately notify that this should be downloaded and loaded
					}

				}
			}

			@Override
			public void onCancelled(DatabaseError error) {
				// Failed to read value
				Log.w(ContentValues.TAG, "Failed to read value.", error.toException());
			}
		});
	}

	private void firstAccess() {
		//very first instructions called when Activity is accessed
		//first time this Activity is created, should just load the very first model
		//in the restaurant so what should be loaded here is should probably just be the very first model

		this.paramFilename = menu.allItems.get(menuIndex).getObjPath();

		Bundle b= new Bundle();

		b.putString("assetDir",getParamAssetDir());
		b.putString("assetFilename",getParamAssetFilename());
		b.putString("uri", getParamFilename());

		//Used for deleting  files
//		File file = new File(getFilesDir().toString() + "/model/ryan.mtl");
//		File file2 = new File(getFilesDir().toString() + "/model/ryan.obj");
//		File file3 = new File(getFilesDir().toString() + "/model/ryan.jpg");
//		file.delete();
//		file2.delete();
//		file3.delete();


		//Always start out with Viewer for direct access to user
		//3D model Viwer***********************
		modelFragment = new ModelFragment();
		modelFragment.setArguments(b);

		fragMgr = getSupportFragmentManager(); //getFragmentManager();
		FragmentTransaction xact = fragMgr.beginTransaction();//.beginTransaction();
		if (null == fragMgr.findFragmentByTag(CONTENT_VIEW_TAG)) {
			xact.add(R.id.modelFrame,  modelFragment ,CONTENT_VIEW_TAG).commit();
		}
		//3D model Viwer***********************

		mRecyclerView = findViewById(R.id.model_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		mAdapter = new MyCircleAdapter(this.menu.allItems, this);
		mRecyclerView.setAdapter(mAdapter);

		//Threads for the purpose of running multiple (3 at a time) downloads at the same time
		Thread thread0 = new Thread(){
			public void run(){
				//Currently unknown if the shadow will be included
//
//				String path1 = getFilesDir().toString() + "/model/andy_shadow.obj";
//				String path3 = getFilesDir().toString() + "/model/andy_shadow.jpg";
//
//				//will get folder data/data/packagename/file
//				File files_folder1 = new File(path1);
//				File files_folder3 = new File(path3);
//
//				System.out.println("CHECK IF EXISTS: "+ files_folder1 + "  Exists?  " + files_folder1.exists());
//				//download only the mtl for testing
//				//	downloadModel(files_folder2, menu.allItems.get(menuIndex).getMtlPath(), testingNumb);
//				//file object does not exist, so download it !
//				if(!files_folder1.exists()) {
//					downloadModel(files_folder1, "andy_shadow.obj", 0);
//					downloadModel(files_folder3, "andy_shadow.jpg", 0);
//				}
				System.out.println("Thread0 Running");
				System.out.println("TESTING MARU FILE LOCTION :  " + getFilesDir().getAbsolutePath()+"/model/");
				downloadOneModel(testingNumber);
				testingNumber++;
			}
		};

		//Run a download
		thread0.start();
	}

	private void downloadOneModel(final int testingNumb){
		String path1 = getFilesDir().toString() + "/model/" +  menu.allItems.get(menuIndex).getObjPath();
		String path2 = getFilesDir().toString() + "/model/" +  menu.allItems.get(menuIndex).getMtlPath();
		String path3 = getFilesDir().toString() + "/model/" +  menu.allItems.get(menuIndex).getJpgPath();

		//will get folder data/data/packagename/file
		File files_folder1 = new File(path1);
		File files_folder2 = new File(path2);
		File files_folder3 = new File(path3);

		//file object does not exist, so download it !
		if(!files_folder1.exists()) {
			downloadModel(files_folder1, menu.allItems.get(menuIndex).getObjPath(), testingNumb);
			downloadModel(files_folder2, menu.allItems.get(menuIndex).getMtlPath(), testingNumb);
			downloadModel(files_folder3, menu.allItems.get(menuIndex).getJpgPath(), testingNumb);
		}
//		else
//			beginLoadingModel();
	}

	//Not currently used
	private void access(){

		//download 1 model at a time
		//hard coded downloading the next file. (

		String path1 = getFilesDir().toString() + "/" + menu.allItems.get(1).getObjPath();
		String path2 = getFilesDir().toString() + "/" + menu.allItems.get(1).getMtlPath();
		String path3 = getFilesDir().toString() + "/" + menu.allItems.get(1).getJpgPath();
		//will get folder data/data/packagename/file
		File files_folder1 = new File(path1);
		File files_folder2 = new File(path2);
		File files_folder3 = new File(path3);

		//file does not exist, so download it !
		if(!files_folder1.exists()) {
			downloadModel(files_folder1, menu.allItems.get(1).getObjPath(), 1);
			downloadModel(files_folder2, menu.allItems.get(1).getMtlPath(), 2);
			downloadModel(files_folder3, menu.allItems.get(1).getJpgPath(), 3);
		}

	}

	private void downloadModel(File files_folder, String imageKey, final int fileNumber) {

		if(!files_folder.exists()) {

			final StorageReference fileToDownload = fbStorageReference.child(imageKey);

			//Make a folder if one does not exist
			final File folder = new File(getFilesDir() + File.separator + "model");
			if (!folder.exists())
			{
				folder.mkdirs();
			}

			fileToDownload.getFile(files_folder).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
					downloadCheck++;//listens to make sure all three files are ready
					//doesnt seem to work file number is never 3 because it is a final variable
					//fileNumber == 3 &&
					if(downloadCheck == 3)
							beginLoadingModel();

					System.out.println("FINISHED DOWNLOADING...fileNumber = " + fileNumber + "    downlaodCheck = " + downloadCheck);


				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
				}
			});
		}
	}

	//Should check what view is currently on (3D or AR) then change appropriate fragment
	void beginLoadingModel()
	{
		Bundle b= new Bundle();
		b.putString("assetDir",getParamAssetDir());
		b.putString("assetFilename",getParamAssetFilename());
		b.putString("uri", getParamFilename());

		modelFragment = new ModelFragment();
		modelFragment.setArguments(b);

		//TODO: Handle normal download and loading handling
		fragMgr.beginTransaction().replace(R.id.modelFrame, modelFragment).commit();
		//Threads for the purpose of running multiple (3 at a time) downloads at the same time
//			Thread thread1 = new Thread(){
//				public void run(){
//					System.out.println("Thread1 Running");
//					downloadOneModel(testingNumber);
//					testingNumber++;
//				}
//			};
//
//			thread1.start();

		// TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom for
		// example
		//Utils.printTouchCapabilities(getPackageManager());
	}

	public void deleteFiles() throws IOException {
		File file = new File(getFilesDir().toString() + "/model");
		FileUtils.deleteDirectory(file);
	}

	//This refers to the menubar that can optionally be placed. Might just remove entirely though.
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.model_toggle_wireframe:
//			//	scene.toggleWireframe();
//				break;
//			case R.id.model_toggle_boundingbox:
//			//	scene.toggleBoundingBox();
//				break;
//			case R.id.model_toggle_textures:
//			//	scene.toggleTextures();
//				break;
//			case R.id.model_toggle_lights:
//				////scene.toggleLighting();
//				break;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	public String getParamAssetDir() {
		return this.paramAssetDir;
	}

	public String getParamAssetFilename() {
		return this.paramAssetFilename;
	}

	public String getParamFilename() {
		return this.paramFilename;
	}

	//  _   _ ___   _____                 _
	// | | | |_ _| | ____|_   _____ _ __ | |_ ___
	// | | | || |  |  _| \ \ / / _ \ '_ \| __/ __|
	// | |_| || |  | |___ \ V /  __/ | | | |_\__ \
	//  \___/|___| |_____| \_/ \___|_| |_|\__|___/
	//

	//When bubble gets hit
	@Override
	public void onMethodCallback(int key) {
		this.paramFilename = menu.allItems.get(key).getObjPath();
		this.menuIndex = key;
		if (viewFlag)
		{
			arModelFragment.passData(getParamFilename());
		}
		else
			beginLoadingModel();
//		{
//			//Threads for the purpose of running multiple (3 at a time) downloads at the same time
//			Thread thread1 = new Thread(){
//				public void run(){
//					System.out.println("Thread1 Running");
//					downloadOneModel(testingNumber);
//					testingNumber++;
//				}
//			};
//
//			thread1.start();
//		}

	}

	public void onBackPress(View view)
	{
		finish();
	}

	//AR Button on top right of screen
	//Should be made into a flag system, to go from AR to 3D view interchangeably
	public void loadMode(View view) {

		//MARU - made a change here where AR now 'replaces' the 3d view frag instead of technically creating one over it.
		//Also made it so it goes back to the 3D model view if it's already in AR view

		if (!viewFlag)
		{
			//TODO: Change how the check is done, don't even have a button there if phone does not support AR
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
				System.out.println(Build.VERSION.SDK_INT);

				viewFlag = true;

				Bundle bundle= new Bundle();

				bundle.putString("uri", getParamFilename());

				//*******************AR
				arModelFragment = new ARModelFragment();
				arModelFragment.setArguments(bundle);

				fragMgr = getSupportFragmentManager();
				fragMgr.beginTransaction().replace(R.id.modelFrame, arModelFragment, arModelFragment.getTag()).commit();
				//*******************AR

			} else{
				Toast.makeText(ModelActivity.this,
						"Sorry, This Device does not support Augmented Reality", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			viewFlag = false;
			beginLoadingModel();
		}
	}
	}

class MyCircleAdapter extends RecyclerView.Adapter<MyCircleAdapter.ViewHolder> {

	private AdapterCallback adapterCallback;
	private ArrayList mDataset;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	static class ViewHolder extends RecyclerView.ViewHolder {
		SelectableRoundedImageView sriv;

		ViewHolder(final View itemView){
			super(itemView);
			sriv = itemView.findViewById(R.id.circle_image);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	MyCircleAdapter(ArrayList myDataset, Context context) {
		this.mDataset = myDataset;
		try {
			this.adapterCallback = ((AdapterCallback) context);
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement AdapterCallback.");
		}
	}

	// Create new views (invoked by the layout manager)
	@Override
	public MyCircleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
												   int viewType) {
		// create a new view
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.circle_view_menu, parent, false);

		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {

		holder.sriv.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				adapterCallback.onMethodCallback(position);
			}}
		);
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	public interface AdapterCallback {
		void onMethodCallback(int key);
	}
}

