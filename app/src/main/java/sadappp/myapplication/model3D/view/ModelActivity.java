package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
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

	private GLSurfaceView gLView;
	//private ModelSurfaceView gLView;
	private SceneLoader scene;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Try to get input parameters
		Bundle b = getIntent().getExtras();
		if (b != null) {
			this.paramAssetDir = b.getString("assetDir");
			this.paramAssetFilename = b.getString("assetFilename");
			this.paramAssetFilename = this.paramAssetFilename.toLowerCase();
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
		handler = new Handler(getMainLooper());

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity.
		gLView = new ModelSurfaceView(this);
		//setContentView(gLView);

		FrameLayout frame = new FrameLayout(this);

        TextView titleText = new TextView(this);
     //   titleText.setBackgroundResource(findViewById(R.id.title_text));
        titleText.setText("This is a 3D Model !");
        titleText.setTextSize(36);
		titleText.setTextColor(Color.BLUE);
        titleText.setPadding(16,16,16,16);

//        Button nextItemButton = (Button) findViewById(R.id.click_here);
//		Button nextItemButton = new Button(this);
//		nextItemButton.setBackgroundResource(R.drawable.custom_style);
//		nextItem.setText("Click to go to next item");
//		nextItem.setMaxWidth(1);
//		nextItem.setMaxHeight(1);
//		nextItem.setTextColor(Color.RED);

		frame.addView(gLView);
		frame.addView(titleText);
//		frame.addView(nextItemButton);

		setContentView(frame);

//		gLView = (ModelSurfaceView) findViewById(R.id.myglsurfaceView);
//		gLView.setModelActivity(this);
//		setContentView(R.layout.activity_model);


		System.out.println(paramAssetDir);
		System.out.println(paramAssetFilename);

		// Create our 3D sceneario
		scene = new SceneLoader(this);
		scene.init();

		// Show the Up button in the action bar.
		//setupActionBar();

		// TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom for
		// example
		Utils.printTouchCapabilities(getPackageManager());

		//setupOnSystemVisibilityChangeListener();
		scene.toggleLighting();
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
}
