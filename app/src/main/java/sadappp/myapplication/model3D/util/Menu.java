package sadappp.myapplication.model3D.util;

import java.util.ArrayList;

/**
 * Created by mende on 12/12/2017.
 * Heavily influenced by Flynn
 */

public class Menu {

    ArrayList<MenuItem> allItems = new ArrayList<>();

    public static class MenuItem {

        String name;
        String objPath;
        String mtlPath;
        String jpgPath;

        private Boolean currentlyDownloading = false;
        private Boolean taskAssigned = false; //? not sure what for yet
        private Boolean loadedNode = false; //I think it's suppose to be an object, not a boolean, not sure what for yet

        public MenuItem(String name) {
            this.name = name;
        }

    }
}
