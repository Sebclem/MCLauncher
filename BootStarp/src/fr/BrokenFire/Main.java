package fr.BrokenFire;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.openlauncherlib.util.explorer.ExploredDirectory;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.Updater;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import fr.theshark34.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main extends Application {


    static File MC_DIR = GameDirGenerator.createGameDir("Imerir");


    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("splashScreen.fxml"));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Launcher BootStrap");
        primaryStage.setScene(new Scene(root, 300, 350));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Resources/icon.png")));
        primaryStage.show();

    }


    public static void main(String[] args) {

        launch(args);

    }

    public static void launchB() throws LaunchException, InterruptedException {
        ClasspathConstructor constructor = new ClasspathConstructor();
        constructor.add(new File(MC_DIR,"launcher.jar"));
        ExternalLaunchProfile profile = new ExternalLaunchProfile("Broken.Main",constructor.make());
        profile.setDirectory(MC_DIR);
        Saver saver = new Saver(new File(MC_DIR,"launcher.properties"));
        String min = saver.get("ramMin");
        String max = saver.get("ramMax");
        if(min==null)
            min="256m";
        if(max==null)
            max="2G";
        ArrayList<String> ram = new ArrayList<String>();
        ram.add("-Xms"+min);
        ram.add("-Xmx"+max);
        profile.setVmArgs(ram);
        ExternalLauncher externalLauncher = new ExternalLauncher(profile);

        Process p = externalLauncher.launch();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.exit();
                System.exit(0);
            }
        }).start();

    }

    public static SUpdate getUpdater()
    {
        if(!MC_DIR.exists())
            MC_DIR.mkdir();
        SUpdate su = new SUpdate("http://imerir-launcher.livehost.fr/bootstarp",MC_DIR);
        su.getServerRequester().setRewriteEnabled(true);
        return su;
    }

}
