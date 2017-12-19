package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import sadappp.myapplication.model3D.services.SceneLoader;
import sadappp.myapplication.util.Utils;
import sadappp.myapplication.R;

import java.io.File;

/**
 * This activity represents the container for our 3D viewer.
 * 
 * @author andresoviedo
 */
public class ModelActivity extends Activity {

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
		beginLoadingModel();
		// Show the Up button in the action bar.
		//setupActionBar();

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	private void firstAccess() {
		//very first instructions called when Activity is accessed
		//first time this Activity is created, should just load the very first model
		//in the restaurant so what should be loaded here is should probably just be the very first model

		this.paramFilename = "mickyd.obj";

	}

	private void access(){

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
