package sadappp.myapplication.model3D.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.Objects;

import sadappp.myapplication.R;
import sadappp.myapplication.model3D.util.LocationHelper;

/**
 * Created by mende on 2/8/2018.
 */

public class LocationDialogFragment extends DialogFragment {

    EditText newRadius;
    EditText newZip;
    Button submitButton;
    Button cancelButton;
    Context context;

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_dialog_frag, container, false);
        // Inflate the layout to use as dialog or embedded fragment
        newRadius = (EditText) rootView.findViewById(R.id.newRadius);
        newRadius.setText(String.valueOf(LocationHelper.getRadius()));
        newZip = (EditText) rootView.findViewById(R.id.newAddress);
        newZip.setText(LocationHelper.getZipcode());
        submitButton= (Button)rootView.findViewById(R.id.submit_butt);
        cancelButton = (Button)rootView.findViewById(R.id.cancel_butt);
        context = getContext();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButton(view);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelButton(view);
            }
        });

        return rootView;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //DIM/BLUR THE SCREEN BEHIND DIALOG SOMEWHERE PLEASE

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void submitButton(View view)
    {
        try {
            //if we have a new zip code -> change current location AND change shared pref
            if (!Objects.equals(newZip.getText().toString(), "")) {
                LocationHelper.setZipcodeAndAll(newZip.getText().toString(), context);

                SharedPreferences.Editor editor = context.getSharedPreferences("ZIP_PREF", context.MODE_PRIVATE).edit();
                editor.putString("zipCode", newZip.getText().toString());
                editor.apply();
            }
            //if we have a new radius
            if (!Objects.equals(newRadius.getText().toString(), "")) {
                LocationHelper.setRadius(Integer.parseInt(newRadius.getText().toString()));
            }

            //CALL Parent activity to reset list
            ((RestaurantViewActivity)getActivity()).prepareRestaurantArray();
            ((RestaurantViewActivity) getActivity()).reloadData();

            dismiss();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelButton(View view)
    {
        dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
