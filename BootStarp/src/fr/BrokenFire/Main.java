package fr.BrokenFire;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import fr.theshark34.*;

import java.io.File;

public class Main extends Application {


    static File MC_DIR = GameDirGenerator.createGameDir("Imerir");


    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("splashScreen.fxml"));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Launcher BootStrap");
        primaryStage.setScene(new Scene(root, 300, 350));
        primaryStage.show();

    }


    public static void main(String[] args) {

        launch(args);

    }

    public static void launchB() throws LaunchException, InterruptedException {
        System.out.println("test");
        ClasspathConstructor constructor = new ClasspathConstructor();
        ExploredDirectory gamedir = Explorer.dir(MC_DIR);
        constructor.add(gamedir.get("Launcher.jar"));
        ExternalLaunchProfile profile = new ExternalLaunchProfile("Broken.Main",constructor.make());
        ExternalLauncher externalLauncher = new ExternalLauncher(profile);

        Process p = externalLauncher.launch();
        Platform.exit();
        System.exit(0);
    }

    public static SUpdate getUpdater()
    {
        SUpdate su = new SUpdate("http://imerir-launcher.livehost.fr/bootstarp",MC_DIR);
        su.getServerRequester().setRewriteEnabled(true);
        return su;
    }
}
