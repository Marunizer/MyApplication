package sadappp.myapplication.model3D.util;

import java.util.ArrayList;

/**
 * Created by mende on 12/12/2017.
 * Heavily influenced by Flynn
 */

public class Menu {

    private String key;
    public ArrayList<MenuItem> allItems = new ArrayList<>();

    public Menu(String key) {
        this.key = key;
    }

    public static class MenuItem {

        String id; // <-- This is because of how firebase is set up.. for now this stays
        String name;
        String objPath;
        String mtlPath;
        String jpgPath;

      //  private Boolean currentlyDownloading = false;
      //  private Boolean taskAssigned = false; //? not sure what for yet
      //  private Boolean loadedNode = false; //I think it's suppose to be an object, not a boolean, not sure what for yet

        public MenuItem(String id, String name) {
            this.id = id;
            this.name = name;
            this.objPath = name + ".obj";
            this.mtlPath = name + ".mtl";
            this.jpgPath = name + ".jpg";
        }

        public String getObjPath() {
            return objPath;
        }

        public String getMtlPath() {
            return mtlPath;
        }

        public String getJpgPath() {
            return jpgPath;
        }

    }
}
