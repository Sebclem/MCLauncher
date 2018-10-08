package Broken.Json;

import java.util.ArrayList;

public class CustomManifestItem {

    public String id;
    public String path;
    public String hash;
    public int size;


    public class CustomManifestItemList extends ArrayList{

//        public boolean contaisnFile(String path){
////            boolean found = false;
////            for(CustomManifestItem item : this){
////                if(item.path.equals(path)){
////                       found = true;
////                       break;
////                }
////            }
////            return found;
//        }
    }
}
