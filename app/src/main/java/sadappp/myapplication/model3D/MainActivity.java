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
	Map<String, Object> objectHashMap;//HOLDS ALL INFO FROM FIREBASE
	ArrayList<Object> objectArrayList;//HOLDS VALUES IN HASHMAP
	ArrayList<String> objectARests; //HOLDS KEYS(RESAURANTS) IN HASHMAP

	static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		// Create an instance of GoogleAPIClient.
		createGoogleAPIClient();

		textLastLocation   = (TextView) findViewById(R.id.location_text);
		btnGetLastLocation = (Button) findViewById(R.id.location_button);
		btnGetLastLocation.setOnClickListener(btnGetLastLocationOnClickListener);
		btnCheckDownload= (Button) findViewById(R.id.check_download);
		btnCheckDownload.setOnClickListener(btnCheckDownloadLocationOnClickListener);

		// TODO: Enable this when I have stabilized the app
		//MARU NOTES: WE MIGHT WANT TO INCORPORATE SOME KIND OF LOADING LOGO
		// This is the animated logo
		// From here we get the WebView component then we load the gif from the jar
		// WebView myWebView = (WebView) findViewById(R.id.main_logo_webview);
		// myWebView.loadUrl("file:///android_res/raw/ic_launcher.gif");
		// init();
	}

	View.OnClickListener btnGetLastLocationOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {

			//TODO: EITHER DELETE CODE OR FIND OUT IF WE NEED IT, SEEMS LIKE NOT USEFUL BECAUSE WE ALREADY ASK FOR LOCATION
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
					//CAN USE THIS TO CHECK WHATS INSIDE THE MAP AND STUFF
					//Log.d(TAG, "Key is " + objectHashMap.keySet() + " value for sadbois is " + String.valueOf(objectArrayList.get(1)));
					//Log.d(TAG, "here are THESE: "+ objectARests.listIterator());

					//TODO: probably need to close connection to firebase before moving on to next activity??

					//Should have if success, do this
					Intent intent = new Intent(MainActivity.this.getApplicationContext(), StoreActivity.class);
					intent.putExtra("FOOD_STORE", objectARests);
					MainActivity.this.startActivity(intent);
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
			//TODO: Implement a fail safe when user doesn't want to share location and use zipcode
				Intent intent = new Intent(MainActivity.this.getApplicationContext(), LocationActivity.class);
				MainActivity.this.startActivity(intent);
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
