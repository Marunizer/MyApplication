package sadappp.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import sadappp.myapplication.model3D.MainActivity;

/**
 * Created by mende on 1/31/2018.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
