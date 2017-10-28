package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.Editable;
import android.widget.TextView;

import sadappp.myapplication.R;

/**
 * Created by mauricio mendez on 10/20/2017.
 */

public class LocationActivity  extends Activity{

    TextView queryText;
    EditText zipcodeText;
    Button enterQuery;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);
        queryText = (TextView) findViewById(R.id.zipcode_text);
        zipcodeText = (EditText) findViewById(R.id.add_zip);
        enterQuery = (Button) findViewById(R.id.zipcode_button);

    }

}
