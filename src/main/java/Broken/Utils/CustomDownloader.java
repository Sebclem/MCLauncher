package Broken.Utils;

import Broken.Json.CustomManifestItem;
import Broken.Main;
import Broken.Utils.Exception.DownloadFailException;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class CustomDownloader extends Observable{


    public int state = 0;
    public long totalSize = 0;
    public long downloaded = 0;

    public int IDLE = 0;
    public int DOWNLADING = 1;
    public int FINISH = 2;
    public int ERROR = 3;


    private static CustomDownloader INSTANCE = new CustomDownloader();

    public static  CustomDownloader getINSTANCE(){
        return INSTANCE;
    }

    private CustomDownloader() {
    }

    private Logger logger = LogManager.getLogger();
    private Downloader downloader;
    private String customURL = "https://mcupdater.seb6596.ovh/";



    private CustomManifestItem[] manifest = null;
    private List<CustomManifestItem> needDownload = new ArrayList<>();


    private void downloadManifest() throws IOException {
        String json = HttpsGet.get(customURL + "getFilesManifest");
        manifest = new Gson().fromJson(json, CustomManifestItem[].class);

    }

    public void check(String path) throws IOException {
        change();
        downloadManifest();
        checkFiles(path);
        state = IDLE;
        change();


    }


    private void checkFiles(String path){
        modsDeleter(path);
        checkToDownload(path);
    }


    private void checkToDownload(String path){
        for(CustomManifestItem item: manifest){
            File file = new File(path + item.path);
            if(!file.exists()){
                logger.debug("File does not exist: " + item.path);
                needDownload.add(item);
                totalSize += item.size;
            }
        }
    }


    private void modsDeleter(String path){

        File filePath = new File(path + "/mods");
        Collection<File> files = FileUtils.listFiles(filePath, new String[]{"jar"}, false);
        for(File file : files){
            boolean found = false;
            for(CustomManifestItem item : manifest){
                if(item.id.equals(file.getName())){
                    found = true;
                    break;
                }
            }

            if(!found){
                logger.info("Del " + file.getName());
                file.delete();
            }
        }

    }


    public void install(String path) throws MalformedURLException, InterruptedException, DownloadFailException {

        for(CustomManifestItem item : needDownload){
            logger.debug(item.id);
            downloader = new Downloader(new URL(customURL + item.path), path + item.path);
            downloader.addObserver(new DlObserver());
            while (downloader.getStatus() == Downloader.DOWNLOADING) {
                Thread.sleep(10);
            }
            if (downloader.getStatus() != Downloader.COMPLETE)
                throw new DownloadFailException();

        }
    }



    private class DlObserver implements Observer{

        private int oldValue = 0;

        @Override
        public void update(Observable observable, Object o) {
            Downloader downloader = (Downloader) observable;
            if (downloader.getStatus() == Downloader.DOWNLOADING) {
                int current = downloader.getProgress();
                downloaded += current - oldValue;
                oldValue = current;
                setChanged();
                notifyObservers();
            }
        }
    }



    private void change(){
        setChanged();
        notifyObservers();
    }
}
