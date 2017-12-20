package sadappp.myapplication.model3D.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sadappp.myapplication.util.Utils;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by mende on 12/13/2017.
 * Heavily influenced by Flynn
 */

//Should be created when User sees a menu
//A new instance of this object is created with new menus
public class MenuAndModelLoader {

//    String coordinateKey;
//    Menu menu;
//    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
//
////    GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
////    Map<String, Object> objectHashMap;
////    ArrayList<String> modelFiles;
//
//
//    //CoordinateKey is all thats needed to query Firebase for a restaurants menu.
//    public MenuAndModelLoader(String coordinateKey) {
//        this.menu = new Menu();
//        this.coordinateKey = coordinateKey;
//    }
//
//
//    // Call Firebase, create a Menu object, assign it to self.
//    //TODO Test this. Theoretically works. Not Tested
//    public void createMenu()
//    {
//        myRef.child("menus/" + Utils.cleanLatLongKey(coordinateKey) + "/items").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot item: dataSnapshot.getChildren()) {
//                    menu.allItems.add(new Menu.MenuItem(item.toString()));
//                }
//                Log.w(TAG, "Properly loaded Menu from Firebase with " + menu.allItems.size() + " items.");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", databaseError.toException());
//            }
//        });
//    }
}
