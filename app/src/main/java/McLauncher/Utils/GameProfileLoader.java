package McLauncher.Utils;

import java.io.IOException;

import McLauncher.Auth.Account;
import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Utils.Exception.LoadingSaveException;
import McLauncher.Utils.Exception.RefreshProfileFailException;

public class GameProfileLoader {
    private GameProfile.MainClass mainClass;
    private String version;
    private Account account;
    private boolean logged;
    private boolean offline;
    private boolean canOffline = true;
    private boolean needWipe = false;
    private String packUUID;
    private String rawGameType;

    private SaveUtils saveUtils;
    private String profileURL = "https://mcupdater.seb6596.ovh/profile.json";
    private Logger  logger = LogManager.getLogger();
    

    public GameProfileLoader() {
        saveUtils = SaveUtils.getINSTANCE();
        try {
            this.account = saveUtils.getAccount();
            this.logged = true;
        } catch (LoadingSaveException e) {
            logger.info(e.getMessage());
            logged = false;
            this.canOffline = false;
        }

        try {
            McLauncher.Json.GameProfile profile = saveUtils.getGameProfile();
            this.mainClass = profile.gameType.equals("FORGE") ? GameProfile.MainClass.FORGE : GameProfile.MainClass.VANILLA;
            this.version = profile.gameVersion;
            this.packUUID = profile.packUUID;
            this.rawGameType = profile.gameType;
        } catch (LoadingSaveException e) {
            logger.info(e.getMessage());
            this.canOffline = false;
        }


    }   

    public void refreshProfile() throws RefreshProfileFailException{
        try {
            String rawProfile = HttpsGet.get(profileURL);
            McLauncher.Json.GameProfile profile = new Gson().fromJson(rawProfile, McLauncher.Json.GameProfile.class);
            this.needWipe = ! profile.packUUID.equals(this.packUUID);
            this.packUUID = profile.packUUID;
            this.mainClass = profile.gameType.equals("FORGE") ? GameProfile.MainClass.FORGE : GameProfile.MainClass.VANILLA;
            this.version = profile.gameVersion;
            this.rawGameType = profile.gameType;
        } catch (IOException e) {
            logger.catching(e);
            throw new RefreshProfileFailException(e.getMessage());
        }
    } 

    public GameProfile.MainClass getMainClass() {
        return mainClass;
    }

    public String getVersion() {
        return version;
    }

    public boolean isOffline() {
        return offline;
    }

    public boolean canOffline() {
        return canOffline;
    }

    public Account getAccount() {
        return account;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public boolean needWipe(){
        return needWipe;
    }

    public void updateCanOffline(){
        canOffline =  account != null && packUUID != null && mainClass != null && version != null;
    }

    public void resetPackUUID(){
        try {
            McLauncher.Json.GameProfile profile = saveUtils.getGameProfile();
            this.packUUID = profile.packUUID;
        } catch (LoadingSaveException e) {
            logger.info(e.getMessage());
            this.canOffline = false;
            this.packUUID = null;
        }
    }

    public String getPackUUID(){
        return packUUID;
    }

    public String getRawGameType() {
        return rawGameType;
    }
    
    
}
