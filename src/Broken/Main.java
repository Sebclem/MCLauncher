package Broken;

import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.openlauncherlib.util.ramselector.RamSelector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    public static Saver saver = new Saver(new File(Launcher.MC_DIR,"launcher.properties"));
    public static RamSelector selector;
    @Override
    public void start(Stage primaryStage) throws Exception{
        if(!Launcher.MC_DIR.exists())
        {
            Launcher.MC_DIR.mkdir();
        }
        Parent root = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
        primaryStage.setTitle("Minecraft IMERIR Launcher");
        primaryStage.setScene(new Scene(root, 1014, 595));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {

        launch(args);
    }
}
