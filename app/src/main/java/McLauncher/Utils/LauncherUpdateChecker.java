package McLauncher.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Json.LauncherUpdateResponse;

public class LauncherUpdateChecker {
    private static final String versionFile = "version.properties";
    private static final Logger logger = LogManager.getLogger();

    public static String getVersion(){
        File file = new File(versionFile);
        if(!file.exists())
            return "DEV";

        try {
            Properties prop = new Properties();
            FileInputStream input = new FileInputStream(versionFile);
            prop.load(input);
            String version = prop.getProperty("version");
            return version == "unspecified" ? "DEV" : version;
        } catch (IOException e) {
            logger.catching(e);
            return "ERROR";
        }
    }

    public static LauncherUpdateResponse getLastVersion() throws IOException{
        String githubResponseRaw = HttpsGet.get("https://api.github.com/repos/Sebclem/MCLauncher/releases/latest");
        return new GsonBuilder().create().fromJson(githubResponseRaw, LauncherUpdateResponse.class);

    }
}
