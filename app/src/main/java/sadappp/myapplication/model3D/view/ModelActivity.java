package sadappp.myapplication.model3D.view;

import android.app.Activity;
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

import sadappp.myapplication.model3D.services.SceneLoader;
import sadappp.myapplication.model3D.util.AmazonS3Helper;
import sadappp.myapplication.util.Utils;
import sadappp.myapplication.R;

import java.io.File;

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
 * 	          Possibly instead add to a menu Class with the restaurant name, and fill in the MenuItems class
 *
 * 	    * Now that there should be a set list, download and display the very first contents! (starting at 0)
 * 	      May actually be better to use a hastable if the number is unique to the item.
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

	private String paramAssetDir;
	private String paramAssetFilename;
	private String model_file;
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

	private static DemoActivity parent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//parent = savedInstanceState.getClass().
       // parent = savedInstanceState.describeContents().getTheActivity();
		// Try to get input parameters
		Bundle b = getIntent().getExtras();
	//	parent = getIntent().get
		if (b != null) {
			this.paramAssetDir = b.getString("assetDir");
			this.model_file = b.getString("modelLocation");
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

		//even though model may not be downloaded, probably atleast want to set up the environment so it isn't blank
		beginLoadingModel();
		firstAccess();
		// Show the Up button in the action bar.
		//setupActionBar();

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	private void firstAccess() {
		//very first instructions called when Activity is accessed
		//first time this Activity is created, should just load the very first model
		//in the restaurant so what should be loaded here is should probably just be the very first model

		//hard coding one model to download for testing purposes
		this.paramFilename = "mickyd.obj";

		String name = "mickyd";
		String objName = name + ".obj";
		String mtlName = name +".mtl";
		String jpgName = name +"01.jpg";
		String path1 = getFilesDir().toString() + "/" + objName;
		String path2 = getFilesDir().toString() + "/" + mtlName;
		String path3 = getFilesDir().toString() + "/" + jpgName;
		//will get folder data/data/packagename/file
		File files_folder1 = new File(path1);
		File files_folder2 = new File(path2);
		File files_folder3 = new File(path3);

		//file does not exist, so download it !
		if(!files_folder1.exists()) {
			downloadModel(files_folder1, objName, 1);
			downloadModel(files_folder2, mtlName, 2);
			downloadModel(files_folder3, jpgName, 3);
		}
		else
			beginLoadingModel();

	}

	private void access(){

	}

	private void downloadModel(File files_folder, String imageKey, final int fileNumber) {

		if(!files_folder.exists())
		{
			//TODO Move this somewhere else so it's only called once per Activity maybe?
			s3Helper = new AmazonS3Helper();
			s3Helper.maruInitiate(this.getApplicationContext());

			//hardcoded for testing purposes both th bucket name and key to download
			observer = s3Helper.getTransferUtility().download(
					"verysadbucket",
					"sadbois" + "/Menu" + "/" + "Mickyd" + "/Key/" + imageKey,
					 files_folder);



			observer.setTransferListener(new TransferListener(){

				@Override
				public void onStateChanged(int id, TransferState state) {
					System.out.println("THIS IS OUR STATE : " + state + " or : " + state.toString() + " TRANSFER UTILITY");
					if (state.toString().compareTo("COMPLETED") == 0 )
					{
						//This is the last file required, when finished, load the model
						if(fileNumber == 3)
							beginLoadingModel();

						System.out.println(state.toString() + " Completed?  " + state.toString().compareTo("COMPLETED"));
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
					beginLoadingModel();
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
		this.paramFilename = "mickyd"+".obj";
		this.gLView = (ModelSurfaceView) findViewById(R.id.myglsurfaceView);
		this.gLView.setModelActivity(this);
		scene = new SceneLoader(this);
		scene.init();
		scene.toggleLighting();

//		Intent in = new Intent(ModelActivity.this.getApplicationContext(), ModelActivity.class);
//		Bundle b = new Bundle();
//		b.putString("assetDir", getFilesDir().getAbsolutePath());
//		b.putString("assetFilename", "mickyd"+".obj");//b.putString("assetFilename", selectedItem+".obj");
//		b.putString("immersiveMode", "true");
//		in.putExtras(b);
//		ModelActivity.this.startActivity(in);

		//DemoActivity demoActivity = new DemoActivity();
//		parent.clickNextModel();
	}

	public void previous_model(View view){

		onBackPressed();
//		Intent in = new Intent(ModelActivity.this.getApplicationContext(), ModelActivity.class);
//		Bundle b = new Bundle();
//		b.putString("assetDir", getFilesDir().getAbsolutePath());
//		b.putString("assetFilename", "cookies"+".obj");//b.putString("assetFilename", selectedItem+".obj");
//		b.putString("immersiveMode", "true");
//		in.putExtras(b);
//		ModelActivity.this.startActivity(in);

	}
}
