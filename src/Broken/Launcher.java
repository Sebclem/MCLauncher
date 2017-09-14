package Broken;

import Broken.Utils.Account;
import Broken.Utils.LoadingSaveException;
import fr.theshark34.openauth.AuthPoints;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openauth.Authenticator;
import fr.theshark34.openauth.model.AuthAgent;
import fr.theshark34.openauth.model.response.AuthResponse;
import fr.theshark34.openauth.model.response.RefreshResponse;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.internal.InternalLaunchProfile;
import fr.theshark34.openlauncherlib.internal.InternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.*;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import fr.theshark34.supdate.exception.BadServerResponseException;
import fr.theshark34.supdate.exception.BadServerVersionException;
import fr.theshark34.supdate.exception.ServerDisabledException;
import fr.theshark34.supdate.exception.ServerMissingSomethingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.File;
import java.io.IOException;

/**
 * Created by seb65 on 06/02/2017.
 */

public class Launcher {
    public static final GameVersion MC_VERSION = new GameVersion("1.12", GameType.V1_8_HIGHER);
    public static final GameInfos MC_INFOS = new GameInfos("Imerir",MC_VERSION,new GameTweak[]{GameTweak.FORGE});
    public static final File MC_DIR = MC_INFOS.getGameDir();
    public static final String crackAuthURL = "http://minecraft-imerir.ovh/openauth/";
    public static final String majURL = "http://minecraft-imerir.ovh/game/";
    static Logger logger = LogManager.getLogger();

    public static Account auth(String user, String password, boolean isLogged, Account account) throws AuthenticationException {
        Authenticator authenticator;
        if (Main.saver.get("authType").equals("0"))
            authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);
        else
            authenticator = new Authenticator(crackAuthURL, AuthPoints.NORMAL_AUTH_POINTS);
        AuthResponse authResponse;
        if(!isLogged)
        {
            authResponse = authenticator.authenticate(AuthAgent.MINECRAFT, user, password, "");

            Main.saver.set("name",authResponse.getSelectedProfile().getName());
            Main.saver.set("uuid",convertToUUID(authResponse.getSelectedProfile().getId()));
            Main.saver.set("id",authResponse.getSelectedProfile().getId());
            Main.saver.set("accessToken",authResponse.getAccessToken());
            Main.saver.set("clientToken",authResponse.getClientToken());
            Main.saver.set("username",user);
            return new Account(authResponse.getSelectedProfile().getId(),authResponse.getSelectedProfile().getName(),authResponse.getAccessToken(),authResponse.getClientToken(),authResponse.getSelectedProfile().getId(),user);

        }

        else
            return refreshAccount(account,authenticator);
    }
    public static SUpdate update() throws BadServerResponseException, IOException, BadServerVersionException, ServerDisabledException, ServerMissingSomethingException {
        SUpdate su = new SUpdate(majURL, MC_DIR);
        su.getServerRequester().setRewriteEnabled(true);
        su.addApplication(new FileDeleter());
        return su;


    }

    public static void lauch(Account account) throws LaunchException {

        AuthInfos authInfos = new AuthInfos(account.getDisplayName(),account.getAccessToken(),account.getUserId());
        InternalLaunchProfile profile = MinecraftLauncher.createInternalProfile(MC_INFOS,GameFolder.BASIC,authInfos);
        InternalLauncher launcher = new InternalLauncher(profile);
        logger.info("End of launcher logs, Launching Game...\n\n\n\n");
        logger.info("/******************************************************************/");
        logger.info("/**************************Minecraft Logs**************************/");
        logger.info("/******************************************************************/");

        launcher.launch();


    }



    private static Account refreshAccount(Account account,Authenticator authenticator) throws AuthenticationException {
        try {
            authenticator.validate(account.getAccessToken());
            logger.info("Account validation success!");
        } catch (AuthenticationException e) {
            logger.warn("Validation fail, refresh account...");
            RefreshResponse refreshR = authenticator.refresh(account.getAccessToken(),account.getClientToken());
            account.setAccessToken(refreshR.getAccessToken());
            Main.saver.set("accessToken",refreshR.getAccessToken());
        }
        return  account;
    }

    public static Account getSavedAcount() throws LoadingSaveException
    {
        String name = Main.saver.get("name");
        String uuid = Main.saver.get("uuid");
        String id = Main.saver.get("id");
        String userName = Main.saver.get("username");
        String accessToken = Main.saver.get("accessToken");
        String clientToken = Main.saver.get("clientToken");
        if(name==null || uuid==null || id == null || userName == null || accessToken == null)
            throw new LoadingSaveException("Failed to load saved Account!");
        if(!name.equals("") && !uuid.equals("") && !accessToken.equals("") && !id.equals("") && !userName.equals("")){
            return new Account(uuid,name,accessToken,clientToken,id,userName);
        }

        else
            throw new LoadingSaveException("Failed to load saved Account!");

    }

    private static String convertToUUID(String id)
    {
        String temp = id.substring(0,8)+"-"+id.substring(8,12)+"-"+id.substring(12,16)+"-"+id.substring(16,32);
        return temp;
    }
}
