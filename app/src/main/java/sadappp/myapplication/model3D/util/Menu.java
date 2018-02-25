package sadappp.myapplication.model3D.util;

import java.util.ArrayList;

/**
 * Created by mende on 12/12/2017.
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

        int downloadChecker;

        public MenuItem(String id, String name) {
            this.id = id;
            this.name = name;
            this.objPath = name + ".obj";
            this.mtlPath = name + ".mtl";
            this.jpgPath = name + ".jpg";
            this.downloadChecker = 0;
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

        public String getName() {
            return splitCamelCase(name);
        }

        static String splitCamelCase(String s) {
            return s.replaceAll(
                    String.format("%s|%s|%s",
                            "(?<=[A-Z])(?=[A-Z][a-z])",
                            "(?<=[^A-Z])(?=[A-Z])",
                            "(?<=[A-Za-z])(?=[^A-Za-z])"
                    ),
                    " "
            );
        }

        public int getDownloadChecker() {
            return downloadChecker;
        }

        public void incrementDownloadChecker() {
            this.downloadChecker++;
        }

    }
}
