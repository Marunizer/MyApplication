package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

import sadappp.myapplication.model3D.services.SceneLoader;
import sadappp.myapplication.model3D.util.AmazonS3Helper;
import sadappp.myapplication.model3D.util.Menu;
import sadappp.myapplication.model3D.util.Restaurant;
import sadappp.myapplication.util.Utils;
import sadappp.myapplication.R;

import java.io.File;
import java.util.ArrayList;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * This activity represents the container for our 3D viewer.
 * 
 * @author andresoviedo
 *
 * --With many Changes by mende
 *
 * TODO List:
 *
 * 		* first, by using the known location key of the restaurant picked, access firebase and in order (0-1) (gross, should be changed..)
 * 	          Make an ArrayList that holds the names of the restaurant menu.
 *
 * 	    * After the first is downloaded, begin a system that downloads each successive item on the list until they're all there.
 * 	          - There shouldn't be more than one item being downloaded at a time, Have some sort of flag check for this.
 *
 * 	    * Have a previous button only show if the first item is NOT the FIRST item. so on start up, previous button should not be shown,
 * 	         and should be removed if we're back to the first item.
 *
 * 	    * Do not have a next button show if we are at the last item, therefore, keep track of MAX items there are !
 *
 * 	    * Implement latest UI design, floating circle back button on top left, Name of item on top right with a clickable text for details for later
 *
 * 	    * After button functionality works, Start making the bottom navigatior that has pictures of the items displayed within small circles to choose the item wanted.
 * 	          - When this is implemented, there will be major changes on how downloads are kept track of
 *
 * 	    * There should be a method to remove all the files related to the menu items, might be onDestroy() or maybe just move everything to cache
 *
 * 	    * Have an (AR) Button to change to an Augmented Reality view. should just change the surface view.
 *
 * 	    * 3d Model Viewer, if zooming in and out, do not allow user to rotate the screen !
 * 	                       When user has 2 fingsers not zooming on the screen, allow user to move camera position? maybe not
 *
 * 	    * After the final 3d model production is decided, will need to change how xyz-axis are disaplyed so model is shown from the front.
 */
public class ModelActivity extends Activity {

	AmazonS3Helper s3Helper;
	private static TransferObserver observer;
	//private ArrayList<Menu> menu = new ArrayList<Menu>();

	private String paramAssetDir;
	private String paramAssetFilename;
	private String model_file;
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

	private ModelSurfaceView gLView;
	private SceneLoader scene;

	private Handler handler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();

		if (b != null) {
			this.paramAssetDir = b.getString("assetDir");
			this.model_file = b.getString("modelLocation");
			this.coordinateKey = b.getString("coordinateKey");
			this.paramAssetFilename = b.getString("assetFilename");
		//	this.paramAssetFilename = this.paramAssetFilename.toLowerCase();
			this.paramFilename = b.getString("uri");
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

		prepareMenuArray();
		// Show the Up button in the action bar.
		//setupActionBar();

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

				}//update menuArray? if we're here then that means we are done with firebase, start downloading !
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
		//this.paramFilename = "mickyd.obj";
		this.paramFilename = menu.allItems.get(0).getObjPath();

		//String name = "mickyd";

		String path1 = getFilesDir().toString() + "/" + menu.allItems.get(0).getObjPath();
		String path2 = getFilesDir().toString() + "/" + menu.allItems.get(0).getMtlPath();
		String path3 = getFilesDir().toString() + "/" + menu.allItems.get(0).getJpgPath();
		//will get folder data/data/packagename/file
		File files_folder1 = new File(path1);
		File files_folder2 = new File(path2);
		File files_folder3 = new File(path3);

		//file does not exist, so download it !
		if(!files_folder1.exists()) {
			downloadModel(files_folder1, menu.allItems.get(0).getObjPath(), 1);
			downloadModel(files_folder2, menu.allItems.get(0).getMtlPath(), 2);
			downloadModel(files_folder3, menu.allItems.get(0).getJpgPath(), 3);
		}
		else
			beginLoadingModel();

	}

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

	void beginLoadingModel()
	{
		handler = new Handler(getMainLooper());

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity.
		setContentView(R.layout.activity_model);
		gLView = (ModelSurfaceView) findViewById(R.id.myglsurfaceView);
		gLView.setModelActivity(this);

		System.out.println(paramAssetDir);
		System.out.println(paramAssetFilename);

		// Create our 3D sceneario
		scene = new SceneLoader(this);
		scene.init();

		// TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom for
		// example
		Utils.printTouchCapabilities(getPackageManager());

		//setupOnSystemVisibilityChangeListener();
		scene.toggleLighting();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.model_toggle_wireframe:
			scene.toggleWireframe();
			break;
		case R.id.model_toggle_boundingbox:
			scene.toggleBoundingBox();
			break;
		case R.id.model_toggle_textures:
			scene.toggleTextures();
			break;
		case R.id.model_toggle_lights:
			scene.toggleLighting();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public File getParamFile() {
		return getParamFilename() != null ? new File(getParamFilename()) : null;
	}

	public String getParamAssetDir() {
		return paramAssetDir;
	}

	public String getParamAssetFilename() {
		return paramAssetFilename;
	}

	public String getParamFilename() {
		return paramFilename;
	}

	public float[] getBackgroundColor(){
		return backgroundColor;
	}

	public SceneLoader getScene() {
		return scene;
	}

	public GLSurfaceView getgLView() {
		return gLView;
	}

	//  _   _ ___   _____                 _
	// | | | |_ _| | ____|_   _____ _ __ | |_ ___
	// | | | || |  |  _| \ \ / / _ \ '_ \| __/ __|
	// | |_| || |  | |___ \ V /  __/ | | | |_\__ \
	//  \___/|___| |_____| \_/ \___|_| |_|\__|___/
	//

	public void next_model(View view){
//will crash if try to go to next model too soon, still downloading
		this.paramFilename = menu.allItems.get(this.menuIndex + 1).getObjPath();
		this.gLView = (ModelSurfaceView) findViewById(R.id.myglsurfaceView);
		this.gLView.setModelActivity(this);
		scene = new SceneLoader(this);
		scene.init();
		scene.toggleLighting();
	}

	public void previous_model(View view){

	//	onBackPressed();
		this.paramFilename = menu.allItems.get(this.menuIndex).getObjPath();
		this.gLView = (ModelSurfaceView) findViewById(R.id.myglsurfaceView);
		this.gLView.setModelActivity(this);
		scene = new SceneLoader(this);
		scene.init();
		scene.toggleLighting();
	}
}
