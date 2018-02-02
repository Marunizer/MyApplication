package sadappp.myapplication.model3D.view;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sadappp.myapplication.model3D.util.AmazonS3Helper;
import sadappp.myapplication.model3D.util.Menu;
import sadappp.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import com.joooonho.SelectableRoundedImageView;


/**
 * This activity represents the container for our 3D viewer.
 * 
 * @author andresoviedo
 *
 * --With many Changes by mende
 *
 * TODO List:
 *
 * 		X* first, by using the known location key of the restaurant picked, access firebase and in order (0-1) (gross, should be changed..)
 * 	          Make an ArrayList that holds the names of the restaurant menu.
 *
 * 	    * After the first is downloaded, begin a system that downloads each successive item on the list until they're all there.
 * 	          - There shouldn't be more than one item being downloaded at a time, Have some sort of flag check for this.
 *
 * 	    * Implement latest UI design, floating circle back button on top left, Name of item on top right with a clickable text for details for later
 *
 * 	    * There should be a method to remove all the files related to the menu items, might be onDestroy() or maybe just move everything to cache
 *				Download into a models folder, delete contents

 * 	    * 3d Model Viewer, if zooming in and out, do not allow user to rotate the screen !
 * 	                       When user has 2 fingsers not zooming on the screen, allow user to move camera position? maybe not
 *
 * 	    * After the final 3d model production is decided, will need to change how xyz-axis are disaplyed so model is shown from the front.
 */
public class ModelActivity extends FragmentActivity implements MyCircleAdapter.AdapterCallback{

	AmazonS3Helper s3Helper;
	private static TransferObserver observer;

	private String paramAssetDir;
	private String paramAssetFilename;
	private String coordinateKey;
	Menu menu;
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
	private static final String CONTENT_VIEW_TAG_AR = "MODEL_FRAG_AR";
	private FragmentManager fragMgr;
	private ModelFragment modelFragment;
	private ARModelFragment arModelFragment;
	private boolean viewFlag = false; //If viewFlag = false -> 3D viewer (default)|| If viewFlag = true -> AR viewer

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();

		if (b != null) {
			this.paramAssetDir = b.getString("assetDir");//the directory where the files are stored perfectly
			this.coordinateKey = b.getString("coordinateKey");
			this.paramAssetFilename = b.getString("assetFilename");//NULL should remove everywhere
			//	this.paramAssetFilename = this.paramAssetFilename.toLowerCase();
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

		//even though model may not be downloaded, probably at least want to set up the environment so it isn't blank
		//beginLoadingModel();

		s3Helper = new AmazonS3Helper();
		s3Helper.initiate(this.getApplicationContext());

		prepareMenuArray();
		handler = new Handler(getMainLooper());

		// Show the Up button in the action bar. aka. the menu bar on top
		//setupActionBar();

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_model);
	}

	private void prepareMenuArray() {

		System.out.println("DO I EVEN GET HERE?***************LOCATION KEY: " + this.coordinateKey + "   !");
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
					System.out.println("!!!!!!!ADDING NEW item : " + item.getValue().toString() + " to the Menu at coordinate: " + coordinateKey);

					//download ?
					if(item.getKey().compareTo("0") == 0)
					{
						firstAccess();
						//first item in the list, so should immediately notify that this should be downloaded and loaded
					}

				}

				//update menuArray? if we're here then that means we are done with firebase, start downloading !
				//access();
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

		//hard coding one model to download for testing purposes
//		this.paramFilename = testingNumber + menu.allItems.get(menuIndex).getObjPath();

		this.paramFilename = menu.allItems.get(menuIndex).getObjPath();

		Bundle b= new Bundle();

		b.putString("assetDir",getParamAssetDir());
		b.putString("assetFilename",getParamAssetFilename());
		b.putString("uri", getParamFilename());
		//Used for deleting  files
//		File file = new File(getFilesDir().toString() + "/TheRyanBurger.mtl");
//		File file2 = new File(getFilesDir().toString() + "/TheRyanBurger.obj");
//		File file3 = new File(getFilesDir().toString() + "/TheRyanBurger.jpg");
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


		mRecyclerView = (RecyclerView) findViewById(R.id.model_recycler_view);
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		mAdapter = new MyCircleAdapter(this.menu.allItems, this);
		mRecyclerView.setAdapter(mAdapter);

		//Threads for the purpose of running multiple (3 at a time) downloads at the same time
		Thread thread0 = new Thread(){
			public void run(){
				System.out.println("Thread0 Running");
				downloadOneModel(testingNumber);
				testingNumber++;
			}
		};

		Thread thread1 = new Thread(){
			public void run(){
				System.out.println("Thread1 Running");
				downloadOneModel(1);
				testingNumber++;
			}
		};

		Thread thread2 = new Thread(){
			public void run(){
				System.out.println("Thread2 Running");
				downloadOneModel(2);
				testingNumber++;
			}
		};

		Thread thread3 = new Thread(){
			public void run(){
				System.out.println("Thread3 Running");
				downloadOneModel(3);
				testingNumber++;
			}
		};

		Thread thread4 = new Thread(){
			public void run(){
				System.out.println("Thread4 Running");
				downloadOneModel(4);
				testingNumber++;
			}
		};

		//Run a download or not
		thread0.start();
//		thread1.start();
//		thread2.start();
//		thread3.start();
//		thread4.start();
	}

	private void downloadOneModel(final int testingNumb){
		String path1 = getFilesDir().toString() + "/" +  menu.allItems.get(menuIndex).getObjPath();
		String path2 = getFilesDir().toString() + "/" +  menu.allItems.get(menuIndex).getMtlPath();
		String path3 = getFilesDir().toString() + "/" +  menu.allItems.get(menuIndex).getJpgPath();

		//will get folder data/data/packagename/file
		File files_folder1 = new File(path1);
		File files_folder2 = new File(path2);
		File files_folder3 = new File(path3);

		//download only the mtl for testing
	//	downloadModel(files_folder2, menu.allItems.get(menuIndex).getMtlPath(), testingNumb);
		//file object does not exist, so download it !
		if(!files_folder1.exists()) {
			downloadModel(files_folder1, menu.allItems.get(menuIndex).getObjPath(), testingNumb);
			downloadModel(files_folder2, menu.allItems.get(menuIndex).getMtlPath(), testingNumb);
			downloadModel(files_folder3, menu.allItems.get(menuIndex).getJpgPath(), testingNumb);
		}
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
//			downloadModel(files_folder2, menu.allItems.get(1).getMtlPath(), 2);
//			downloadModel(files_folder3, menu.allItems.get(1).getJpgPath(), 3);
		}

	}

//	private void downloadModel(File files_folder, String imageKey, final int fileNumber) {
//
//		final long timeStart = System.currentTimeMillis();
//
//		if(!files_folder.exists())
//		{
//			//TODO Move this somewhere else so it's only called once per Activity maybe?
//
//			System.out.println(" I WONDER IF WE GET TO AWS with " + s3Helper.getBucketName() + " at path: FishFilet/" + imageKey + "   file_dest" + files_folder);
//																						//"FishFilet/"
//			observer = s3Helper.getTransferUtility().download(s3Helper.getBucketName(), "small/" + imageKey,files_folder);
//			observer.setTransferListener(new TransferListener(){
//
//				@Override
//				public void onStateChanged(int id, TransferState state) {
//					System.out.println("THIS IS OUR STATE : " + state + " or : " + state.toString() + " TRANSFER UTILITY");
//					if (state.toString().compareTo("COMPLETED") == 0 )
//					{
//						menu.allItems.get(menuIndex).incrementDownloadChecker();
//						//This is the last file required, when finished, load the model
//						if(fileNumber == 0)
//							//beginLoadingModel();
//
//						System.out.println("For : " + fileNumber + ",  " + state.toString() + " Completed?  " + state.toString().compareTo("COMPLETED"));
//						System.out.println("For : " + fileNumber + " , The time it took was: " + (System.currentTimeMillis() - timeStart));
//					}
//
//				}
//
//				@Override
//				public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//					int percentage = 0;
//					if (bytesTotal > 0) {
//						percentage = (int) (bytesCurrent / bytesTotal * 100);
//					}
//					System.out.println("YO THIS DOWNLOAD AT *** : " + percentage + "%" );
//				}
//
//				@Override
//				public void onError(int id, Exception ex) {
//					//beginLoadingModel();
//					System.out.println("There was an error downloading !!" );
//				}
//
//			});
//		}
//	}

	private void downloadModel(File files_folder, String imageKey, final int fileNumber) {

		if(!files_folder.exists())
		{
			//TODO Move this somewhere else so it's only called once per Activity maybe?
			s3Helper = new AmazonS3Helper();
			s3Helper.initiate(this.getApplicationContext());

			System.out.println(" I WONDER IF WE GET TO AWS with " + s3Helper.getBucketName() + " at path: small/" + imageKey + "   file_dest" + files_folder);

			observer = s3Helper.getTransferUtility().download(s3Helper.getBucketName(), "small/" + imageKey,files_folder);
			observer.setTransferListener(new TransferListener(){

				@Override
				public void onStateChanged(int id, TransferState state) {
					System.out.println("THIS IS OUR STATE : " + state + " or : " + state.toString() + " TRANSFER UTILITY");
					if (state.toString().compareTo("COMPLETED") == 0 )
					{
						//This is the last file required, when finished, load the model
						if(fileNumber == 3)
							beginLoadingModel();

						System.out.println("For : " + fileNumber + ",  " + state.toString() + " Completed?  " + state.toString().compareTo("COMPLETED"));
					}

				}

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
					//beginLoadingModel();
					System.out.println("There was an error downloading !!" );
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

		fragMgr.beginTransaction().replace(R.id.modelFrame, modelFragment).commit();

		downloadCheck++;//listens to make sure all three files are ready
//		if (menu.allItems.get(menuIndex).getDownloadChecker() != 3)
//			return;

		System.out.println(paramAssetDir);

		// TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom for
		// example
		//Utils.printTouchCapabilities(getPackageManager());

	}

	//This refers to the menubar that can optionally be placed. Might just remove entirely though.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.model_toggle_wireframe:
			//	scene.toggleWireframe();
				break;
			case R.id.model_toggle_boundingbox:
			//	scene.toggleBoundingBox();
				break;
			case R.id.model_toggle_textures:
			//	scene.toggleTextures();
				break;
			case R.id.model_toggle_lights:
				////scene.toggleLighting();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

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

	@Override
	public void onMethodCallback(int key) {
		this.paramFilename = menu.allItems.get(key).getObjPath();
		if (viewFlag)
		{
			arModelFragment.passData(getParamFilename());
		}
		else
			beginLoadingModel();
	}

	//AR Button on top right of screen
	//Should be made into a flag system, to go from AR to 3D view interchangeably
	public void loadMode(View view) {


		//MARU - made a change here where AR now 'replaces' the 3d view frag instead of technically creating one over it.
		//Also made it so it goes back to the 3D model view if it's already in AR view

		if (!viewFlag)
		{
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
				System.out.println("I hope this is not reached");

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
				Toast.makeText(this,
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
			sriv = (SelectableRoundedImageView)itemView.findViewById(R.id.circle_image);
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

