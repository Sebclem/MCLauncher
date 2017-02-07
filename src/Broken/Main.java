package Broken;

import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.supdate.exception.BadServerResponseException;
import fr.theshark34.supdate.exception.BadServerVersionException;
import fr.theshark34.supdate.exception.ServerDisabledException;
import fr.theshark34.supdate.exception.ServerMissingSomethingException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1024, 600));

        primaryStage.show();
//        try {
//            Launcher.auth("","");
//            Launcher.update();
//            Launcher.lauch();
//        } catch (AuthenticationException e) {
//            System.out.println("Erreur d'authentification: "+e.getErrorModel().getErrorMessage());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (BadServerVersionException e) {
//            e.printStackTrace();
//        } catch (ServerMissingSomethingException e) {
//            e.printStackTrace();
//        } catch (ServerDisabledException e) {
//            e.printStackTrace();
//        } catch (BadServerResponseException e) {
//            e.printStackTrace();
//        } catch (LaunchException e) {
//            e.printStackTrace();
//        }
    }


    public static void main(String[] args) {

        launch(args);
    }
}
