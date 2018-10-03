package Broken;



import Broken.Utils.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Main extends Application {

    public static String gamePath = "/home/sebastien/test/";
    public static String version = "1.10.2";

    private static SaveUtils saveUtils;
    private static Stage primaryStageS;
    public static Controller controller;
    public static int screenCorecter = 0;
    static Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {

        screenCorecter = 0;
        String os = System.getProperty("os.name");

        if(os.toLowerCase().contains("linux"))
            screenCorecter = 10;

        logger.info("/*****************************************************************/");
        logger.info("/**************************Launcher Logs**************************/");
        logger.info("/*****************************************************************/");
        logger.info("OS: "+os);
        saveUtils = SaveUtils.getINSTANCE(gamePath + "launcher.properties");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/mainPage.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("Minecraft IMERIR Launcher");

        primaryStage.setScene(new Scene(root, 1014, 595+screenCorecter));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/minecraftLogo.png")));
        primaryStageS=primaryStage;

        primaryStage.show();

    }

    public static Stage getPrimaryStage() {
        return primaryStageS;
    }


    public static void main(String[] args) {
        launch(args);

        System.exit(0);


    }
}
