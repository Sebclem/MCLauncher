package McLauncher.Utils;

import McLauncher.App;
import McLauncher.Json.Game;
import McLauncher.Json.Manifest;
import McLauncher.Utils.Event.Observable;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VaniaGameInstaller extends Observable {
    public static String libSubFolder = "libraries/";
    public static String sysLibSubFolder = "sysLib/";
    private final Logger logger = LogManager.getLogger();
    private final String manifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private final String resourcesURL = "http://resources.download.minecraft.net/";
    public long totalSize = 0;
    public long downloaded = 0;

    private Manifest getManifest() throws IOException {
        String result = HttpsGet.get(manifestUrl);
        Gson gson = new Gson();
        return gson.fromJson(result, Manifest.class);

    }


    public Game getGame(String version) throws IOException {
        logger.debug("Try to find game version " + version);
        Manifest manifest = getManifest();
        ArrayList<Manifest.VersionItem> versions = manifest.versions;
        Manifest.VersionItem found = null;

        for (Manifest.VersionItem versionItem : versions) {
            if (versionItem.id.equals(version)) {
                found = versionItem;
                break;
            }
        }
        if (found != null) {
            logger.debug("Game found, JSON: " + found.url);
            String gameString = HttpsGet.get(found.url);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Game.ArgValue.class, new Game.ArgValueDeserialize());
            Gson gson = gsonBuilder.create();
            Game game = gson.fromJson(gameString, Game.class);
            return game;
        } else {
            logger.error("Game Not Found");
        }
        return null;
    }

    private void downloadGame(String path, Game game) throws DownloadFailException, InterruptedException, MalformedURLException {
        logger.info("Downloading Main...");
        int size = game.downloads.client.size;
        //Getting download size
        for (Game.Libraries lib : game.libraries) {
            if (needToDownloadThis(lib)) {
                //Download only classifiers if we have it
                if (lib.downloads.classifiers == null) {
                    size += lib.downloads.artifact.size;
                } else {
                    if (OsIdentifer.isLinux() && lib.downloads.classifiers.linux != null) {
                        size += lib.downloads.classifiers.linux.size;
                    } else if (OsIdentifer.isMac() && lib.downloads.classifiers.osx != null) {
                        size += lib.downloads.classifiers.osx.size;
                    } else if (OsIdentifer.isWindows() && lib.downloads.classifiers.windows != null) {
                        size += lib.downloads.classifiers.windows.size;
                    }
                }
            }

        }

        logger.debug("Downloading : client:" + game.id);
        Downloader downloader = new Downloader(new URL(game.downloads.client.url), path + "client.jar");
        downloader.addObserver(new DownloadObserver());
        while (downloader.getStatus() == Downloader.DOWNLOADING) {
            Thread.sleep(100);
        }
        if (downloader.getStatus() != Downloader.COMPLETE)
            throw new DownloadFailException();
        for (Game.Libraries lib : game.libraries) {
            if (needToDownloadThis(lib)) {
                logger.debug("Downloading : " + lib.name);
                if (lib.downloads.classifiers == null) {
                    downloader = new Downloader(new URL(lib.downloads.artifact.url), path + libSubFolder + lib.downloads.artifact.path);
                    downloader.addObserver(new DownloadObserver());
                    while (downloader.getStatus() == Downloader.DOWNLOADING) {
                        Thread.sleep(100);
                    }
                    if (downloader.getStatus() != Downloader.COMPLETE)
                        throw new DownloadFailException();
                } else {
                    String url = null;
                    String libPath = null;
                    if (OsIdentifer.isLinux() && lib.downloads.classifiers.linux != null) {
                        url = lib.downloads.classifiers.linux.url;
                        libPath = lib.downloads.classifiers.linux.path;
                    } else if (OsIdentifer.isMac() && lib.downloads.classifiers.osx != null) {
                        url = lib.downloads.classifiers.osx.url;
                        libPath = lib.downloads.classifiers.osx.path;

                    } else if (OsIdentifer.isWindows() && lib.downloads.classifiers.windows != null) {
                        url = lib.downloads.classifiers.windows.url;
                        libPath = lib.downloads.classifiers.windows.path;
                    }
                    if (url != null) {
                        downloader = new Downloader(new URL(url), path + libSubFolder + libPath);
                        downloader.addObserver(new DownloadObserver());

                        while (downloader.getStatus() == Downloader.DOWNLOADING) {
                            Thread.sleep(100);
                        }

                        if (downloader.getStatus() != Downloader.COMPLETE)
                            throw new DownloadFailException();

                        String finalLibPath = libPath;
                        Thread thread = new Thread(() -> {
                            try {
                                new Extractor().extrac(path + sysLibSubFolder, path + libSubFolder + finalLibPath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        thread.start();
                    }
                }
            }
        }
    }

    /**
     * Check if we need to download this library via os rules check
     *
     * @param lib The lib to check
     * @return True if we need to download this lib
     */
    private boolean needToDownloadThis(Game.Libraries lib) {
        // if we don't have any rules we download it anyway, by default, if rules !empty, we don't need to dl this lib
        boolean needDownload = resolveRule(lib.rules);
        logger.debug("Need to download " + lib.name + "? " + needDownload);
        return needDownload;
    }


    private boolean resolveRule(List<Game.Rules> rules) {
        if (rules.isEmpty())
            return true;
        for (Game.Rules rule : rules) {
            // if os is null this is the default value
            if (rule.os == null) {
                return rule.action.equals("allow");
            } else {
                if (OsIdentifer.isLinux() && rule.os.name.equals("linux")) {
                    return rule.action.equals("allow");
                } else if (OsIdentifer.isMac() && rule.os.name.equals("osx")) {
                    return rule.action.equals("allow");
                } else if (OsIdentifer.isWindows() && rule.os.name.equals("windows")) {
                    return rule.action.equals("allow");
                }
            }
        }
        return false;
    }

    public void installGame(String installPath, String version) throws IOException, InterruptedException, DownloadFailException {

        Game game = getGame(version);
        logger.info("Size to download: " + totalSize / 1024 + "KB");
        getLogConfig(installPath + "assets/log_configs/", game);

        downloadGame(installPath, game);
        assetsDownloader(installPath + "assets/", game);

        SaveUtils saveUtils = SaveUtils.getINSTANCE();
        if(game.arguments != null){
            saveUtils.save("defaultGameArgs", getArgsAsString(game.arguments.game));
            saveUtils.save("defaultJvmArgs", getArgsAsString(game.arguments.jvm));
        }else{
            saveUtils.save("defaultGameArgs", "");
            saveUtils.save("defaultJvmArgs", "");
        }
        saveUtils.save("assetId", game.assetIndex.id);
        saveUtils.save("mainClass", game.mainClass);
        saveUtils.save("install", "true");
        saveUtils.save("logConfigPath", installPath + "assets/log_configs/" + game.logging.client.file.id);
    }


    private void assetsDownloader(String path, Game game) throws IOException, InterruptedException, DownloadFailException {
        logger.info("Downloading Assets...");
        String result = HttpsGet.get(game.assetIndex.url);
        JsonObject objects = (JsonObject) JsonParser.parseString(result).getAsJsonObject().get("objects");
        Downloader downloader;
        for (String key : objects.keySet()) {
            JsonObject obj = (JsonObject) objects.get(key);
            String hash = obj.get("hash").getAsString();
            downloader = new Downloader(new URL(resourcesURL + hash.substring(0, 2) + "/" + hash), path + "objects/" + hash.substring(0, 2) + "/" + hash);
            downloader.addObserver(new DownloadObserver());

            while (downloader.getStatus() == Downloader.DOWNLOADING) {
                Thread.sleep(10);
            }
            if (downloader.getStatus() != Downloader.COMPLETE)
                throw new DownloadFailException();
            logger.debug(resourcesURL + hash.substring(0, 2) + "/" + hash);
        }

        downloader = new Downloader(new URL(game.assetIndex.url), path + "indexes/" + game.assetIndex.id + ".json");
        downloader.addObserver(new DownloadObserver());

        while (downloader.getStatus() == Downloader.DOWNLOADING) {
            Thread.sleep(10);
        }
        if (downloader.getStatus() != Downloader.COMPLETE)
            throw new DownloadFailException();

        logger.debug("stop");


    }


    public boolean checkInstall() {
        return SaveUtils.getINSTANCE().get("install") != null && SaveUtils.getINSTANCE().get("install").equals("true");
    }


    private long getTotalSize(Game game) {

        totalSize = game.assetIndex.totalSize;
        for (Game.Libraries lib : game.libraries) {
            if (lib.downloads.classifiers == null) {
                totalSize += lib.downloads.artifact.size;
            } else {
                if (OsIdentifer.isLinux() && lib.downloads.classifiers.linux != null) {
                    totalSize += lib.downloads.classifiers.linux.size;
                } else if (OsIdentifer.isMac() && lib.downloads.classifiers.osx != null) {
                    totalSize += lib.downloads.classifiers.osx.size;

                } else if (OsIdentifer.isWindows()) {
                    if (lib.downloads.classifiers.windows != null)
                        totalSize += lib.downloads.classifiers.windows.size;
                    if (lib.downloads.classifiers.windows32 != null)
                        totalSize += lib.downloads.classifiers.windows32.size;
                    if (lib.downloads.classifiers.windows64 != null)
                        totalSize += lib.downloads.classifiers.windows64.size;
                }
            }
        }
        return totalSize;
    }

    public long getTotalSize(String version) throws IOException {
        Game game = getGame(version);

        return getTotalSize(game);
    }

    private void getLogConfig(String path, Game game) throws InterruptedException, DownloadFailException, MalformedURLException {
        Downloader downloader = new Downloader(new URL(game.logging.client.file.url), path + game.logging.client.file.id);
        downloader.addObserver(new DownloadObserver());
        while (downloader.getStatus() == Downloader.DOWNLOADING) {
            Thread.sleep(100);
        }
        if (downloader.getStatus() != Downloader.COMPLETE)
            throw new DownloadFailException();
        editLogs(path + game.logging.client.file.id);
    }


    public void editLogs(String path) {
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder oldtext = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                oldtext.append(line).append("\r\n");
            }
            reader.close();
            // replace a word in a file
            String newText = oldtext.toString().replaceAll("logs/", App.gamePath.replaceAll("\\\\", "/") + "log/");


            FileWriter writer = new FileWriter(path);
            writer.write(newText);
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private String getArgsAsString(List<Game.ArgValue> args) {
        StringBuilder toReturn = new StringBuilder();
        for (Game.ArgValue argValue : args) {
            if (argValue != null && resolveRule(argValue.rules)) {
                for (String value : argValue.values)
                    toReturn.append(value).append(";");
            }

        }
        return toReturn.toString();
    }


    class DownloadObserver implements Observer {
        private int oldValue = 0;

        @Override
        public void update(Object observable) {
            Downloader downloader = (Downloader) observable;
            if (downloader.getStatus() == Downloader.DOWNLOADING) {
                int current = downloader.getProgress();
                downloaded += current - oldValue;
                oldValue = current;
                change();
            }
        }
    }


}
