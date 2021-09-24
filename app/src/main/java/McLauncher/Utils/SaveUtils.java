package McLauncher.Utils;

import McLauncher.Auth.Account;
import McLauncher.Utils.Exception.LoadingSaveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Properties;

public class SaveUtils {

    static private SaveUtils INSTANCE;

    private String path;

    private Properties prop = new Properties();
    private OutputStream output = null;
    private InputStream input = null;

    private Logger logger = LogManager.getLogger();


    private SaveUtils(String savePath) {
        this.path = savePath;

        try {
            input = new FileInputStream(path);
            prop.load(input);
        } catch (IOException e) {
            logger.catching(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }
        }
    }

    public static SaveUtils getINSTANCE(String savePath) {
        File file = new File(savePath);
        if (!file.exists()) {
            new File(savePath.substring(0, savePath.lastIndexOf("/"))).mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new SaveUtils(savePath);
        }
        return INSTANCE;
    }

    public static SaveUtils getINSTANCE() {
        return INSTANCE;
    }

    public void save(Account account) {
        try {
            output = new FileOutputStream(path);
            prop.setProperty("uuid", account.getUUID());
            prop.setProperty("displayName", account.getDisplayName());
            prop.setProperty("accessToken", account.getAccessToken());
            prop.setProperty("clientToken", account.getClientToken());
            prop.setProperty("tokenExpireDate", account.getTokenExpireDate() != null ? account.getTokenExpireDate().toString() : "");
            prop.setProperty("refreshToken", account.getRefreshToken());
            prop.setProperty("username", account.getUsername() != null ? account.getUsername() : "");
            prop.store(output, null);


        } catch (IOException e) {
            logger.catching(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }

        }

    }

    public void save(String id, String value) {
        try {
            output = new FileOutputStream(path);
            prop.setProperty(id, value);
            prop.store(output, null);


        } catch (IOException e) {
            logger.catching(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }

        }
    }


    public String get(String id) {
        return prop.getProperty(id);
    }

    public Account getAccount() throws LoadingSaveException {

        if (notPresent("clientToken")) {
            throw new LoadingSaveException("Not logged");
        }
        LocalDateTime tokenExpireDate = null;
        try {
            tokenExpireDate = LocalDateTime.parse(prop.getProperty("tokenExpireDate"));
        } catch (Exception ignored) {
        }

        return new Account(
                prop.getProperty("uuid"),
                prop.getProperty("displayName"),
                prop.getProperty("accessToken"),
                prop.getProperty("clientToken"),
                tokenExpireDate,
                prop.getProperty("refreshToken"),
                prop.getProperty("username")
        );
    }

    public McLauncher.Json.GameProfile getGameProfile() throws LoadingSaveException {
        if (notPresent("packUUID") || notPresent("gameVersion") || notPresent("gameType") || notPresent("mainClass")) {
            throw new LoadingSaveException("No Game Profile Saved");
        }
        return new McLauncher.Json.GameProfile(
                prop.getProperty("packUUID"),
                prop.getProperty("gameVersion"),
                prop.getProperty("gameType"),
                prop.getProperty("forgeVersion"),
                prop.getProperty("mainClass")
                );
    }


    private boolean notPresent(String key) {
        return prop.getProperty(key) == null || prop.getProperty(key).equals("");
    }


    public void checkConfig() {
        String ramType = get("ramType");
        String ramMax = get("ramMax");

        String authType = get("authType");
        //Init all ram param if it don't exits
        if (ramType == null || ramMax == null) {
            save("ramType", "0");
            save("ramMax", "6G");
        }

        if (authType == null) {
            save("authType", "1");
        }
    }
}
