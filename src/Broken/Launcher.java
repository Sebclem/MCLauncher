package Broken;

import fr.theshark34.openauth.AuthPoints;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openauth.Authenticator;
import fr.theshark34.openauth.model.AuthAgent;
import fr.theshark34.openauth.model.response.AuthResponse;
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
import nologin.account.Account;


import java.io.File;
import java.io.IOException;

/**
 * Created by seb65 on 06/02/2017.
 */

public class Launcher {
    public static final GameVersion MC_VERSION = new GameVersion("1.7.10", GameType.V1_7_10);
    public static final GameInfos MC_INFOS = new GameInfos("Imerir",MC_VERSION,new GameTweak[]{GameTweak.FORGE});
    public static final File MC_DIR = MC_INFOS.getGameDir();
    private static AuthInfos authInfos;


    public static void auth(String user, String password) throws AuthenticationException {
        Authenticator authenticator;
        if (Main.saver.get("authType").equals("0"))
            authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);
        else
            authenticator = new Authenticator("http://seb6596.freeboxos.fr/", AuthPoints.NORMAL_AUTH_POINTS);

        AuthResponse authResponse = authenticator.authenticate(AuthAgent.MINECRAFT, user, password, "");
        authInfos = new AuthInfos(authResponse.getSelectedProfile().getName(), authResponse.getClientToken(), authResponse.getSelectedProfile().getId());
        new Account(authInfos.getUuid(), authInfos.getUsername(), authInfos.getAccessToken(), authResponse.getSelectedProfile().getId(),user);
    }
    public static SUpdate update() throws BadServerResponseException, IOException, BadServerVersionException, ServerDisabledException, ServerMissingSomethingException {
        SUpdate su = new SUpdate("http://imerir-launcher.livehost.fr/",MC_DIR);
        su.getServerRequester().setRewriteEnabled(true);
        su.addApplication(new FileDeleter());
        return su;


    }

    public static void lauch() throws LaunchException {

        InternalLaunchProfile profile = MinecraftLauncher.createInternalProfile(MC_INFOS,GameFolder.BASIC,authInfos);
        InternalLauncher launcher = new InternalLauncher(profile);

        launcher.launch();


    }

    public static AuthInfos getAuthInfos() {

        return authInfos;
    }
}
