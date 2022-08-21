package McLauncher.Utils.Installer;

import McLauncher.App;
import McLauncher.Json.ForgeManifest;
import McLauncher.Json.Game;
import McLauncher.Utils.Event.Observable;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;
import McLauncher.Utils.SaveUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModLoaderInstaller extends Observable {
    public static String libSubFolder = "libraries/";
    private final String serverUrl = "https://mcupdater.seb6596.ovh/";
    private final String forgeManifestUrl = "getForgeManifest/";
    private final String fabricManifestUrl = "getFabricManifest/";
    private final Logger logger = LogManager.getLogger();
    public long downloaded;
    private String version;
    private Downloader downloader;

    private ForgeManifest getForgeManifest(String loader, String version) throws IOException, InterruptedException {
        String manifestUrl;
        switch (loader) {
            case "FORGE" -> manifestUrl = forgeManifestUrl;
            case "FABRIC" -> manifestUrl = fabricManifestUrl;
            default -> manifestUrl = "";
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + manifestUrl + version))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Game.ArgValue.class, new Game.ArgValueDeserialize());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(response.body(), ForgeManifest.class);
    }

    public int getTotalSize(String loader, String version) throws IOException, InterruptedException {
        ForgeManifest manifest = getForgeManifest(loader, version);
        int size = 0;
        for (ForgeManifest.Libraries lib : manifest.libraries) {
            if (lib.downloads != null) {
                size += lib.downloads.artifact.size;
            }
        }
        return size;
    }

    public void install(String loader, String version) throws IOException, DownloadFailException, InterruptedException {
        ForgeManifest manifest = getForgeManifest(loader, version);
        logger.info("Downloading " + loader + " " + version + "...");
        downloader(manifest, App.gamePath);
        SaveUtils.getINSTANCE().save("mainClass", manifest.mainClass);
        SaveUtils.getINSTANCE().save("modLoaderJvmArgs", VaniaGameInstaller.getArgsAsString(manifest.arguments.jvm));
        SaveUtils.getINSTANCE().save("modLoaderGameArgs", VaniaGameInstaller.getArgsAsString(manifest.arguments.game));

    }

    public void downloader(ForgeManifest manifest, String path) throws MalformedURLException, InterruptedException, DownloadFailException {
        for (ForgeManifest.Libraries item : manifest.libraries) {
            String url;
            String artifactPath;
            if (item.downloads != null) {
                if (item.downloads.artifact.url.startsWith("/"))
                    url = serverUrl + item.downloads.artifact.url;
                else
                    url = item.downloads.artifact.url;
                artifactPath = item.downloads.artifact.path;
            } else {
                url = item.url;

//              0: group
//              1: artifact id
//              2: version
                String[] spitted = item.name.split(":");
                artifactPath = spitted[0].replaceAll("\\.", "/") + "/" + spitted[1] + "/" + spitted[2] + "/" + spitted[1] + "-" + spitted[2] + ".jar";
                url = url + artifactPath;
            }

            downloader = new Downloader(new URL(url), path + libSubFolder + artifactPath);
            downloader.addObserver(new DlObserver());
            while (downloader.getStatus() == Downloader.DOWNLOADING) {
                Thread.sleep(10);
            }
            if (downloader.getStatus() != Downloader.COMPLETE)
                throw new DownloadFailException();

        }
    }

    private class DlObserver implements Observer {

        private int oldValue = 0;

        @Override
        public void update(Object subject) {
            Downloader downloader = (Downloader) subject;
            if (downloader.getStatus() == Downloader.DOWNLOADING) {
                int current = downloader.getProgress();
                downloaded += current - oldValue;
                oldValue = current;
                change();
            }

        }
    }
}
