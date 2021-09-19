package McLauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Utils.LauncherUpdateChecker;
import McLauncher.Utils.OsIdentifer;
import McLauncher.Utils.SaveUtils;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    public static String gamePath = OsIdentifer.getInstallPath();


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
        logger.info("Java Home: " + System.getProperty("java.home"));
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("Launcher Version: " + LauncherUpdateChecker.getVersion());
        saveUtils = SaveUtils.getINSTANCE(gamePath + "launcher.properties");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/mainPage.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.MyBundle", Locale.getDefault()));
        Parent root = loader.load();

        controller = loader.getController();
        primaryStage.setTitle("Mc Launcher SC");
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
    }
}
