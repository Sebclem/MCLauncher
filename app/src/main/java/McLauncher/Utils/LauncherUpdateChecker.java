package McLauncher.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Json.LauncherUpdateResponse;

public class LauncherUpdateChecker {
    private static final String versionFile = "version.properties";
    private static final Logger logger = LogManager.getLogger();

    public static String getVersion(){
        try {
            InputStream resource = LauncherUpdateChecker.class.getClassLoader().getResourceAsStream(versionFile);
            if(resource == null){
                return "DEV";
            }
            Properties prop = new Properties();
            prop.load(resource);
            String version = prop.getProperty("version");
            return version.equals("unspecified") ? "DEV" : version;
        } catch (IOException | NullPointerException e) {
            logger.catching(e);
            return "ERROR";
        }
    }

    public static LauncherUpdateResponse getLastVersion() throws IOException{
        String githubResponseRaw = HttpsGet.get("https://api.github.com/repos/Sebclem/MCLauncher/releases/latest");
        return new GsonBuilder().create().fromJson(githubResponseRaw, LauncherUpdateResponse.class);

    }
}
