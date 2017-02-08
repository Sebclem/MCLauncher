package Broken;

import com.sun.org.apache.bcel.internal.generic.LADD;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.exception.BadServerResponseException;
import fr.theshark34.supdate.exception.BadServerVersionException;
import fr.theshark34.supdate.exception.ServerDisabledException;
import fr.theshark34.supdate.exception.ServerMissingSomethingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button optionButton;

    @FXML
    private TextField userText;

    @FXML
    private Label labelBar;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button playButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Pane body;

    @FXML
    private GridPane grid;

    @FXML
    private VBox vbox;



    boolean firstTime = true;


    @FXML
    void initialize() {
        userText.setText(Main.saver.get("username"));
        passwordField.setText(Main.saver.get("password"));
        if(!userText.textProperty().isEmpty().get()&&!passwordField.textProperty().isEmpty().get())
        {
            playButton.setDisable(false);
        }
        Platform.runLater(()->optionButton.requestFocus());
        userText.textProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(newValue.trim().isEmpty()||passwordField.textProperty().isEmpty().get());

        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(newValue.trim().isEmpty()||userText.textProperty().isEmpty().get());

        });
        passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode()== KeyCode.ENTER)
                {
                    new LaunchThread().start();
                }
            }
        });


        playButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new LaunchThread().start();
            }
        });






    }


    class LaunchThread extends Thread{
        @Override
        public void run() {
            Platform.runLater(()->
            {
                grid.setDisable(true);
                labelBar.setText("Authentification...");
                progressBar.setProgress(-1);
            });

            try {
                Launcher.auth(userText.getText(),passwordField.getText());
                Main.saver.set("username",userText.getText());
                Main.saver.set("password",passwordField.getText());
                SUpdate su = Launcher.update();
                Thread thread = new Thread(){
                    long value = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
                    long max = BarAPI.getNumberOfTotalBytesToDownload()/1000;

                    @Override
                    public void run() {
                        Platform.runLater(()-> labelBar.setText("Verification des fichiers..."));

                        while (max == 0) max = BarAPI.getNumberOfTotalBytesToDownload() / 1000;
                        Platform.runLater(()-> labelBar.setText("Télécharment: 0%"));
                        DecimalFormat myFormatter = new DecimalFormat("###.##");
                        while (!this.isInterrupted()) {

                            if (value != BarAPI.getNumberOfTotalDownloadedBytes() / 1000) {
                                value = BarAPI.getNumberOfTotalDownloadedBytes() / 1000;
                                double pour = (value*100.0) / max;
                                System.out.println(value + "/" + max + "   -> " + pour);
                                Platform.runLater(() -> {
                                    progressBar.setProgress(pour/100.0);
                                    labelBar.setText("Téléchargement: "+myFormatter.format(pour)+"%");

                                });
                            }

                        }

                    }
                };
                thread.start();
                su.start();
                thread.interrupt();
                Platform.runLater(() -> {
                    progressBar.setProgress(-1);
                    labelBar.setText("Lancement du jeu...");

                });
                try {
                    Launcher.lauch();
                    labelBar.getScene().getWindow().hide();
                } catch (LaunchException e) {
                    System.out.println(e.getMessage());
                    Platform.runLater(()->{
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText("Echec de lancement!");
                        alert.setContentText("Echec lors du lancement du jeu:\n"+e.getMessage());
                        alert.setTitle("Erreur");
                        progressBar.setProgress(0);
                        labelBar.setText("Echec de lancement du jeu!");
                        alert.showAndWait();
                        grid.setDisable(false);
                    });
                }

            } catch (AuthenticationException e) {
                e.printStackTrace();

                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Echec d'Authentification!");
                    alert.setContentText("Echec d'Authentification: "+e.getErrorModel().getErrorMessage());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Echec d'Authentification!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });

            } catch (IOException e) {
                e.printStackTrace();
                grid.setDisable(false);
            } catch (BadServerVersionException |ServerMissingSomethingException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Ereur Serveur: "+e.getMessage());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur serveur!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
                grid.setDisable(false);

            } catch (ServerDisabledException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Le serveur de mise à jour est désactivé.");
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur serveur!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
                grid.setDisable(false);
            } catch (BadServerResponseException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Mauvaise reponsse du serveur: \n"+e.getMessage());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur serveur!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
                grid.setDisable(false);
        }


        }
    }
}
