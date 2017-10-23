package Broken;

import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.openlauncherlib.util.ramselector.RamSelector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Main extends Application {
    public static Saver saver = new Saver(new File(Launcher.MC_DIR,"launcher.properties"));
    private static Stage primaryStageS;
    public static Controller controller;
    Logger logger = LogManager.getLogger();
    @Override
    public void start(Stage primaryStage) throws Exception{
        if(!Launcher.MC_DIR.exists())
        {
            Launcher.MC_DIR.mkdir();
        }
        int screenCorecter = 0;
        String os = System.getProperty("os.name");

        if(os.toLowerCase().contains("linux"))
            screenCorecter = 10;
        logger.info("/*****************************************************************/");
        logger.info("/**************************Launcher Logs**************************/");
        logger.info("/*****************************************************************/");
        logger.info("OS: "+os);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainPage.fxml"));
        Parent root = (Parent) loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Minecraft IMERIR Launcher");

        primaryStage.setScene(new Scene(root, 1014, 595+screenCorecter));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            Controller.dlListenner.interrupt();
            Controller.threadSpeed.interrupt();
            Platform.exit();
        });
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Resources/minecraftLogo.png")));
        primaryStageS=primaryStage;

        primaryStage.show();

    }

    public static Stage getPrimaryStage() {
        return primaryStageS;
    }



    public static void main(String[] args) {
        launch(args);
    }
}
