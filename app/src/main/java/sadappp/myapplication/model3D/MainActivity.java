package sadappp.myapplication.model3D;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import sadappp.myapplication.model3D.view.LocationActivity;
import sadappp.myapplication.model3D.view.RestaurantViewActivity;
import sadappp.myapplication.R;

import java.io.File;

/**
 * The purpose of this Activity is to be the very first Screen the user see's and find location or choose to include their own.
 * TODO List:
 *
 *      * Have a nice inviting button to " Start ! "
 *
 * 		* A better button, have a nicer "Search with Zip-code" instead for a different user option(:
 *
 * 	    * Just like in RestaurantActivity, may want to move GoogleMaps functionality to it's own class and access it here
 *
 * 	    * Should have an automatic way to check if the user has already given permission for location
 *
 * 	    * If user denies permission, send them to the Zip-code activity
 *
 *      * If user accepts, cool . Have a way to keep that so you never ask them again !
 *      	(Although I'm pretty sure that is already handled somewhere in here)
 *
 *      * Have a ready animation view for first time access of the app ready for later (:
 *      	Minght not ever be used. low-low-low priority.
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

	static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		// Create an instance of GoogleAPIClient.
		createGoogleAPIClient();

		textLastLocation   = (TextView) findViewById(R.id.location_text);
		textLastLocation.setTextColor(Color.BLUE);
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
	}

	View.OnClickListener btnGetLastLocationOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			Intent intent = new Intent(MainActivity.this.getApplicationContext(), RestaurantViewActivity.class);
			intent.putExtra("LOCATION", mLastLocation);
			MainActivity.this.startActivity(intent);
		}
	};

	View.OnClickListener btnCheckDownloadLocationOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
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


	//    EVERYTHING
	//
	//              GOOGLE
	//
	//                       API
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
		this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			textLastLocation.setText(
					String.valueOf(mLastLocation.getLatitude()) + "\n"
							+ String.valueOf(mLastLocation.getLongitude()));
//			Toast.makeText(MainActivity.this,
//					String.valueOf(mLastLocation.getLatitude()) + "\n"
//							+ String.valueOf(mLastLocation.getLongitude()),
//					Toast.LENGTH_LONG).show();
		}else{
//			Toast.makeText(MainActivity.this,
//					"mLastLocation == null",
//					Toast.LENGTH_LONG).show();
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
//		Toast.makeText(MainActivity.this,
//				"onConnectionSuspended: " + String.valueOf(i),
//				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//		Toast.makeText(MainActivity.this,
//				"onConnectionFailed: \n" + connectionResult.toString(),
//				Toast.LENGTH_LONG).show();
	}
}
