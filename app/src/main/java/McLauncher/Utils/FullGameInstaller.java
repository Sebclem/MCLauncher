package McLauncher.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.App;
import McLauncher.Utils.Event.Observable;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;


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



    public void init(String installPath, String gameVersion) throws IOException{
        VaniaGameInstaller vaniaGameInstaller = new VaniaGameInstaller();
        if( !vaniaGameInstaller.checkInstall()){
            logger.info("Vania Game install needed!");
            totalSize = vaniaGameInstaller.getTotalSize(gameVersion);
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
            customDownloader.install(App.gamePath);
        }
    }

    public void wipper(String installPath, GameProfileLoader gameProfileLoader){
        File filePath = new File(installPath);
        if (!filePath.exists())
            return;
        Collection<File> files = FileUtils.listFilesAndDirs(filePath, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        for (File file : files) {
            if(!file.getName().equals("launcher.properties") && !filePath.getPath().equals(file.getPath())){
                logger.info("Delleting " + file.getName() + "...");
                file.delete();
            }
        }
        SaveUtils.getINSTANCE().save("packUUID", gameProfileLoader.getPackUUID());
        SaveUtils.getINSTANCE().save("gameVersion", gameProfileLoader.getVersion());
        SaveUtils.getINSTANCE().save("gameType", gameProfileLoader.getRawGameType());
        SaveUtils.getINSTANCE().save("install", "false");

    }




    class InstallObserver implements Observer {
        private long oldValue = 0;
        @Override
        public void update(Object subObject) {
            if(subObject instanceof  VaniaGameInstaller){
                state = DOWNLADING;
                VaniaGameInstaller gameInstaller = (VaniaGameInstaller) subObject;
                long current = gameInstaller.downloaded;
                downloaded += current - oldValue;
                oldValue = current;

            }
            else{
                CustomDownloader downloader = (CustomDownloader) subObject;
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
}
