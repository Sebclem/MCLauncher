package Broken;


import Broken.Utils.GameProfile;
import Broken.Utils.OsIdentifer;
import Broken.Utils.SaveUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    public static String gamePath = OsIdentifer.getInstallPath();
    public static String version = "1.12.2";
    public static GameProfile.MainClass gameType = GameProfile.MainClass.FORGE;


    private static SaveUtils saveUtils;
    private static Stage primaryStageS;
    public static Controller controller;
    public static int screenCorecter;
    static Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {

        screenCorecter = 10;
        String os = System.getProperty("os.name");

        if (OsIdentifer.isMac() || OsIdentifer.isLinux())
            screenCorecter = 10;

        logger.info("/*****************************************************************/");
        logger.info("/**************************Launcher Logs**************************/");
        logger.info("/*****************************************************************/");
        logger.info("OS: " + os);
        logger.info("Install path: " + gamePath);
        saveUtils = SaveUtils.getINSTANCE(gamePath + "launcher.properties");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/mainPage.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.MyBundle", Locale.getDefault()));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Minecraft IMERIR Launcher");

        primaryStage.setScene(new Scene(root, 1014, 605 ));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/minecraftLogo.png")));
        primaryStageS = primaryStage;


        primaryStage.show();

    }

    public static Stage getPrimaryStage() {
        return primaryStageS;
    }


    public static void main(String[] args) {
        System.setProperty("user.dir", gamePath);

        launch(args);

//        System.exit(0);


    }
}
