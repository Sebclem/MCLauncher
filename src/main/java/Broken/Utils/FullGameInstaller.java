package Broken.Utils;

import Broken.Main;
import Broken.Utils.Exception.DownloadFailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class FullGameInstaller extends Observable {


    private Logger logger = LogManager.getLogger();

    public long totalSize = 0;
    public long downloaded = 0;

    private long vanillaSize = 0;

    private boolean needVania = false;

    public int state = 0;

    public int IDLE = 0;
    public int DOWNLADING = 1;
    public int FINISH = 2;
    public int ERROR = 3;



    public void init(String installPath) throws IOException{
        VaniaGameInstaller vaniaGameInstaller = new VaniaGameInstaller();
        if( !vaniaGameInstaller.checkInstall()){
            logger.info("Vania Game install needed!");
            totalSize = vaniaGameInstaller.getTotalSize(Main.version);
            needVania = true;
        }

        CustomDownloader customDownloader = CustomDownloader.getINSTANCE();
        customDownloader.check(installPath);

        totalSize += customDownloader.totalSize;



    }


    public void download(String installPath, String version) throws InterruptedException, IOException, DownloadFailException {
        state = DOWNLADING;
        if(needVania){
            VaniaGameInstaller vaniaGameInstaller = new VaniaGameInstaller();
            vaniaGameInstaller.addObserver(new InstallObserver());
            vaniaGameInstaller.installGame(installPath, version);
        }
        CustomDownloader customDownloader = CustomDownloader.getINSTANCE();
        if(customDownloader.totalSize != 0){
            customDownloader.addObserver(new InstallObserver());
            customDownloader.install(Main.gamePath);
        }
    }




    class InstallObserver implements Observer {
        private long oldValue = 0;
        @Override
        public void update(Observable o, Object arg) {
            if(o instanceof  VaniaGameInstaller){
                state = DOWNLADING;
                VaniaGameInstaller gameInstaller = (VaniaGameInstaller) o;
                long current = gameInstaller.downloaded;
                downloaded += current - oldValue;
                oldValue = current;

            }
            else{
                CustomDownloader downloader = (CustomDownloader) o;
                if(downloader.state != ERROR && downloader.state != IDLE){
                    state = downloader.state;
                }
                long current = downloader.downloaded + vanillaSize;
                downloaded += current - oldValue;
                oldValue = current;

            }
            change();
        }
    }

    private void change(){
        setChanged();
        notifyObservers();
    }


}
