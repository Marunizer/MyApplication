package sadappp.myapplication.model3D.view;

import android.app.Activity;
import android.app.Dialog;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
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
    Context context;

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Activity activity = getActivity();
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    /** The system calls this only when creating the layout in a dialog. */
    //TODO: make the radius change a bar that's scrolled for better user interaction, Max : 100 miles
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.ThemeOverlay_Material_Dialog_Alert);

        //better but title doesn't appear in right place
        //Theme_Holo_Light_Dialog

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.location_dialog_frag,null);
        newRadius = rootView.findViewById(R.id.newRadius);
        newRadius.setText(String.valueOf(LocationHelper.getRadius()));
        newZip = rootView.findViewById(R.id.newAddress);
        newZip.setText(LocationHelper.getZipcode());

        // Set the dialog title
        builder.setTitle("Search Settings")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                // Set the action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        submitButton();
                        mListener.onDialogPositiveClick(LocationDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        LocationDialogFragment.this.getDialog().cancel();
                    }
                });

        builder.setView(rootView);

        return builder.create();
    }

    public void submitButton()
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
         dismiss();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
