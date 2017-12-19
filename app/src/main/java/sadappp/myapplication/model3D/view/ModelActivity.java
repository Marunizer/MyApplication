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
