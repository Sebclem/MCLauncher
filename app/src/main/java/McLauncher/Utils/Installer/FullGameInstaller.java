package McLauncher.Utils.Installer;

import McLauncher.App;
import McLauncher.Utils.Event.Observable;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;
import McLauncher.Utils.GameProfileLoader;
import McLauncher.Utils.SaveUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;


public class FullGameInstaller extends Observable {


    public long totalSize = 0;
    public long downloaded = 0;
    public int state = 0;
    public String stage = "";
    public int IDLE = 0;
    public int DOWNLADING = 1;
    public int FINISH = 2;
    public int ERROR = 3;
    private Logger logger = LogManager.getLogger();
    private long vanillaSize = 0;
    private long forgeSize = 0;
    private boolean needVania = false;

    public void init(String installPath, GameProfileLoader gameProfileLoader) throws IOException, InterruptedException {
        VaniaGameInstaller vaniaGameInstaller = new VaniaGameInstaller();
        if (!vaniaGameInstaller.checkInstall()) {
            logger.info("Vania Game install needed!");
            vanillaSize = vaniaGameInstaller.getTotalSize(gameProfileLoader.getVersion());
            needVania = true;

            if (gameProfileLoader.getRawGameType().equals("FORGE")) {
                ForgeInstaller forgeInstaller = new ForgeInstaller();
                forgeSize = forgeInstaller.getTotalSize(gameProfileLoader.getForgeVersion());
            }
        }

        CustomDownloader customDownloader = CustomDownloader.getINSTANCE();
        customDownloader.check(installPath);

        totalSize = customDownloader.totalSize + vanillaSize + forgeSize;
    }


    public void download(String installPath, GameProfileLoader gameProfileLoader) throws InterruptedException, IOException, DownloadFailException {
        state = DOWNLADING;
        if (needVania) {
            stage = "VANILLA";
            VaniaGameInstaller vaniaGameInstaller = new VaniaGameInstaller();
            vaniaGameInstaller.addObserver(new InstallObserver());
            vaniaGameInstaller.installGame(installPath, gameProfileLoader.getVersion());
            if (Objects.equals(gameProfileLoader.getRawGameType(), "FORGE")) {
                stage = "FORGE";
                ForgeInstaller forgeInstaller = new ForgeInstaller();
                forgeInstaller.addObserver(new InstallObserver());
                forgeInstaller.install(gameProfileLoader.getForgeVersion());
            }

        }
        CustomDownloader customDownloader = CustomDownloader.getINSTANCE();
        if (customDownloader.totalSize != 0) {
            stage = "MODS";
            customDownloader.addObserver(new InstallObserver());
            customDownloader.install(App.gamePath);
        }
    }

    public void wipper(String installPath, GameProfileLoader gameProfileLoader) {
        File filePath = new File(installPath);
        if (!filePath.exists())
            return;
        Collection<File> files = FileUtils.listFilesAndDirs(filePath, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
        for (File file : files) {
            if (!file.getName().equals("launcher.properties") && !filePath.getPath().equals(file.getPath())) {
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
        private long oldForge = 0;
        private long oldCustom = 0;
        private boolean forgeFirst = true;

        @Override
        public void update(Object subObject) {
            if (subObject instanceof VaniaGameInstaller gameInstaller) {
                state = DOWNLADING;
                long current = gameInstaller.downloaded;
                downloaded += current - oldValue;
                oldValue = current;

            } else if (subObject instanceof ForgeInstaller forgeInstaller) {
                long current = forgeInstaller.downloaded;
                downloaded += current - oldForge;
                oldForge = current;
            } else if (subObject instanceof CustomDownloader customDownloader) {
                if (customDownloader.state != ERROR && customDownloader.state != IDLE) {
                    state = customDownloader.state;
                }
                long current = customDownloader.downloaded;
                downloaded += current - oldCustom;
                oldCustom = current;
            }
            change();
        }
    }
}
