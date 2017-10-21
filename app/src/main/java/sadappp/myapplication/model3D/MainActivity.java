package sadappp.myapplication.model3D;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import sadappp.myapplication.model3D.view.LocationActivity;
import sadappp.myapplication.model3D.view.StoreActivity;
import sadappp.myapplication.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * This is the main android activity. From here we launch the whole stuff.
 * 
 * Basically, this activity may serve to show a Splash screen and copy the assets (obj models) from the jar to external
 * directory.
 * 
 * @author andresoviedo
 *
 */
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	/**
	 * User's directory where we are going store the assets (models, textures, etc). It will be copied to
	 * /storage/OpenSource3DModelViewer
	 */
	private static final String ASSETS_TARGET_DIRECTORY = Environment.getExternalStorageDirectory() + File.separator
			+ "3DModelViewerOS";

	GoogleApiClient mGoogleApiClient;
	Location mLastLocation;
	TextView textLastLocation;
	Button btnGetLastLocation;
	Button btnCheckDownload;

	GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
	Map<String, Object> objectHashMap;
	//probably need one more map for the actual menu items
	ArrayList<Object> objectArrayList;
	ArrayList<String> objectARests;
	List<String> listing;
	// Create an S3 client
	AmazonS3 s3Client;
	String BUCKET_NAME = "verysadbucket";
	TransferUtility transferUtility;
	String[] models = null;
	String myKey = "mickyd01.jpg";
	String filename = "myFile";
	FileOutputStream fos;
	File file[];


	static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// callback method to call credentialsProvider method.
		s3credentialsProvider();

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Set main layout controls.
		// Basically, this is a screen with the app name just in the middle of the scree
		setContentView(R.layout.activity_main);

		// Create an instance of GoogleAPIClient.
		createGoogleAPIClient();

		textLastLocation   = (TextView) findViewById(R.id.location_text);
		btnGetLastLocation = (Button) findViewById(R.id.location_button);
		btnGetLastLocation.setOnClickListener(btnGetLastLocationOnClickListener);
		btnCheckDownload= (Button) findViewById(R.id.check_download);
		btnCheckDownload.setOnClickListener(btnCheckDownloadLocationOnClickListener);


		// TODO: Enable this when I have stabilized the app
		// This is the animated logo
		// From here we get the WebView component then we load the gif from the jar
		// WebView myWebView = (WebView) findViewById(R.id.main_logo_webview);
		// myWebView.loadUrl("file:///android_res/raw/ic_launcher.gif");
		// init();
	}

	public void s3credentialsProvider(){
		// Initialize the Amazon Cognito credentials provider
		CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
				getApplicationContext(),
				"us-east-1:1f71d265-5641-4934-a734-1cae7eb1ff47", // Identity pool ID
				Regions.US_EAST_1 // Region
		);
		createAmazonS3Client(cognitoCachingCredentialsProvider);
	}

	/**
	 * Create a AmazonS3Client constructor and pass the credentialsProvider.
	 * @param credentialsProvider
	 */
	public void createAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider){
		// Create an S3 client
		s3Client = new AmazonS3Client(credentialsProvider);

		// Set the region of your S3 bucket
		s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
	}

	public void downloadFileFromS3(View view){

//		File files_folder = getFilesDir();                       //will get folder data/data/packagename/file
//		File files_child = new File(files_folder, "files_child");
//		files_child.mkdirs();
//		File created_folder = getDir("custom", MODE_PRIVATE);     //Will create a folder named app+custom in internal folder
//		File f1_child = new File(created_folder, "custom_child");
//		f1_child.mkdirs();

		new Thread(new Runnable() {
			public void run() {
				System.out.println("Im here");
				S3Object o = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, myKey));

				try {

					fos = openFileOutput(myKey, Context.MODE_PRIVATE);
					InputStream s3is = o.getObjectContent();
					byte[] read_buf = new byte[1024];
					int read_len = 0;
					while ((read_len = s3is.read(read_buf)) > 0) {
						fos.write(read_buf, 0, read_len);
					}
					s3is.close();
				} catch (AmazonServiceException e) {
					System.err.println(e.getErrorMessage());
					System.exit(1);
				} catch (FileNotFoundException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				} catch (IOException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
			}
		}
		).start();
		//TODO: Should always delete files after no longer needed with myContext.deleteFile(filename);
	}

	private static void displayTextInputStream(InputStream input) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true) {
			String line = reader.readLine();
			if (line == null) break;

			System.out.println("    " + line);
		}
		System.out.println();
	}

	/**
	 * @desc This method is used to return list of files name from S3 Bucket
	 * @param// bucket
	 * @param//s3Client
	 * @return object with list of files
	 */
	public void fetchFileFromS3(View view){

		System.out.println("gets here 0");
		// Get List of files from S3 Bucket
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {

				try {
					Looper.prepare();
					listing = getObjectNamesForBucket(BUCKET_NAME, s3Client);
					System.out.println("gets here 1");
					for (int i=0; i< listing.size(); i++){
						Toast.makeText(MainActivity.this,
								listing.get(i),Toast.LENGTH_LONG).show();
					}
					Looper.loop();
				}
				catch (Exception e) {System.out.println("gets here 2");
					e.printStackTrace();
					Log.e("tag", "Exception found while listing "+ e);
				}

			}
		});System.out.println("gets here 3");
		thread.start();
	}

	private List<String> getObjectNamesForBucket(String bucket, AmazonS3 s3Client) {
		ObjectListing objects = s3Client.listObjects(bucket);
		List<String> objectNames=new ArrayList<String>(objects.getObjectSummaries().size());
		Iterator<S3ObjectSummary> iterator=objects.getObjectSummaries().iterator();
		while (iterator.hasNext()) {
			objectNames.add(iterator.next().getKey());
		}
		while (objects.isTruncated()) {
			objects=s3Client.listNextBatchOfObjects(objects);
			iterator=objects.getObjectSummaries().iterator();
			while (iterator.hasNext()) {
				objectNames.add(iterator.next().getKey());
			}
		}
		return objectNames;
	}

	private void setUpAmazonWebService() {

		String key = String.valueOf(objectArrayList.get(1));
		String obj_key = "cookie_v2.obj";
		String mtl_key = key.substring(key.lastIndexOf("mtl=") + 4).split("\\,")[0];
		String jpg_key = key.substring(key.lastIndexOf("jpg=") + 4).split("\\,")[0];

		Log.d(TAG, "Obj_key is: " + obj_key);
		Log.d(TAG, "Mtl_key is: " + mtl_key);
		Log.d(TAG, "Jpg_key is: " + jpg_key);
	}

	View.OnClickListener btnGetLastLocationOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {

			//Might want to consider commenting this out. No help for functionality, only testing
//			if (mGoogleApiClient != null) {
//				if (mGoogleApiClient.isConnected()) {
//					getMyLocation();
//				} else {
//					Toast.makeText(MainActivity.this,
//							"!mGoogleApiClient.isConnected()", Toast.LENGTH_LONG).show();
//				}
//			} else {
//				Toast.makeText(MainActivity.this,
//						"mGoogleApiClient == null", Toast.LENGTH_LONG).show();
//			}

			// Write a message to the database
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			DatabaseReference myRef = database.getReference("Rests");
			//"Rests/Bento/Menu"
			//myRef.setValue(textLastLocation);

			// Read from the database
			myRef.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					// This method is called once with the initial value and again
					// whenever data at this location is updated.
					objectHashMap = dataSnapshot.getValue(objectsGTypeInd);
					objectArrayList = new ArrayList<Object>(objectHashMap.values());

					//DO NOT USE OR CRASH//String value = dataSnapshot.getValue(String.class);
					objectARests = new ArrayList<String>();

					//for every key, go through and assign string to arraylist from hasmap
					for (String key: objectHashMap.keySet()) {
						objectARests.add(key);
					}
					Log.d(TAG, "Key is " + objectHashMap.keySet() + " value for sadbois is " + String.valueOf(objectArrayList.get(1)));
					Log.d(TAG, "here are THESE: "+ objectARests.listIterator());
					setUpAmazonWebService();

////					//Should have if success, do this
					Intent intent = new Intent(MainActivity.this.getApplicationContext(), StoreActivity.class);
					intent.putExtra("hashmap", objectARests);
					MainActivity.this.startActivity(intent);

				//	Intent intent = new Intent(MainActivity.this.getApplicationContext(), LocationActivity.class);
				//	MainActivity.this.startActivity(intent);
					downloadFileFromS3(v);
				}

				@Override
				public void onCancelled(DatabaseError error) {
					// Failed to read value
					Log.w(TAG, "Failed to read value.", error.toException());
				}
			});
		}
	};

	View.OnClickListener btnCheckDownloadLocationOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {

				try {
				FileInputStream fis = openFileInput(myKey);
				InputStreamReader inputStreamReader = new InputStreamReader(fis);
				BufferedReader bufferReader = new BufferedReader(inputStreamReader);
					String line;
				StringBuffer stringBuffer =  new StringBuffer();
					while((line = bufferReader.readLine()) !=null)
					{
						stringBuffer.append(line + "\n");
					}
			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "FILE IS HERE =  " + getFilesDir().getAbsolutePath() );
		}
	};

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

	private void createGoogleAPIClient() {
		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}
	}
	//------------------------------------------------------------------------------
	//ref: Requesting Permissions at Run Time
	//http://developer.android.com/training/permissions/requesting.html
	//------------------------------------------------------------------------------
	private void getMyLocation() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.


			//------------------------------------------------------------------------------
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

			return;
		}
		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			textLastLocation.setText(
					String.valueOf(mLastLocation.getLatitude()) + "\n"
							+ String.valueOf(mLastLocation.getLongitude()));
			Toast.makeText(MainActivity.this,
					String.valueOf(mLastLocation.getLatitude()) + "\n"
							+ String.valueOf(mLastLocation.getLongitude()),
					Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(MainActivity.this,
					"mLastLocation == null",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(
			int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(MainActivity.this,
							"permission was granted, :)",
							Toast.LENGTH_LONG).show();
					getMyLocation();

				} else {
					Toast.makeText(MainActivity.this,
							"permission denied, ...:(",
							Toast.LENGTH_LONG).show();
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	@Override
	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	@Override
	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		getMyLocation();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Toast.makeText(MainActivity.this,
				"onConnectionSuspended: " + String.valueOf(i),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Toast.makeText(MainActivity.this,
				"onConnectionFailed: \n" + connectionResult.toString(),
				Toast.LENGTH_LONG).show();
	}
}
